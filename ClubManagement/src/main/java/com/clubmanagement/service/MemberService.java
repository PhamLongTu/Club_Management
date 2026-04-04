package com.clubmanagement.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.dao.MemberDAO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.entity.Member;
import com.clubmanagement.entity.Role;
import com.clubmanagement.util.HibernateUtil;
import com.clubmanagement.util.PasswordUtil;

/**
 * MemberService - Tầng nghiệp vụ cho Thành viên.
 *
 * Service layer chứa toàn bộ business logic:
 * - Validate dữ liệu đầu vào
 * - Mã hóa mật khẩu trước khi lưu
 * - Chuyển đổi Entity <-> DTO
 * - Gọi DAO để thực hiện thao tác database
 */
public class MemberService {

    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);
    private final MemberDAO memberDAO = new MemberDAO();

    // ===================================================
    // AUTHENTICATION
    // ===================================================

    /**
     * Xác thực đăng nhập: kiểm tra email + mật khẩu.
     *
     * Quy trình:
     * 1. Tìm Member theo email
     * 2. Dùng BCrypt kiểm tra mật khẩu plain vs hash trong DB
     * 3. Kiểm tra tài khoản có bị khóa không
     *
     * @param email         Email người dùng nhập
     * @param plainPassword Mật khẩu chưa mã hóa
     * @return Optional<MemberDTO> nếu đăng nhập thành công
     */
    public Optional<MemberDTO> login(String email, String plainPassword) {
        // Validate input rỗng
        if (email == null || email.isBlank() || plainPassword == null || plainPassword.isBlank()) {
            logger.warn("Email hoặc mật khẩu trống");
            return Optional.empty();
        }

        // Tìm thành viên theo email
        Optional<Member> memberOpt = memberDAO.findByEmail(email.trim());
        if (memberOpt.isEmpty()) {
            logger.warn("Không tìm thấy tài khoản với email: {}", email);
            return Optional.empty();
        }

        Member member = memberOpt.get();

        // Kiểm tra tài khoản có bị vô hiệu hóa không
        if (!"Active".equals(member.getStatus())) {
            logger.warn("Tài khoản {} đang bị khóa/vô hiệu", email);
            return Optional.empty();
        }

        // Kiểm tra mật khẩu bằng BCrypt
        // BCrypt hash hợp lệ bắt đầu bằng $2a$, $2b$, hoặc $2y$
        boolean passwordCorrect;
        String hash = member.getPasswordHash();
        if (hash != null && (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"))) {
            try {
                passwordCorrect = PasswordUtil.checkPassword(plainPassword, hash);
            } catch (Exception e) {
                logger.error("Lỗi khi verify BCrypt hash cho {}: {}", email, e.getMessage());
                passwordCorrect = false;
            }
        } else {
            // Hash không hợp lệ - từ chối đăng nhập
            logger.warn("Hash mật khẩu không hợp lệ cho tài khoản: {}. Hãy chạy lại script SQL.", email);
            passwordCorrect = false;
        }

        if (!passwordCorrect) {
            logger.warn("Mật khẩu sai cho tài khoản: {}", email);
            return Optional.empty();
        }

        logger.info("Đăng nhập thành công: {} [{}]", member.getFullName(), member.getRole().getRoleName());
        return Optional.of(toDTO(member));
    }

    // ===================================================
    // CRUD OPERATIONS
    // ===================================================

    /**
     * Tạo thành viên mới với validation đầy đủ.
     *
     * @param fullName   Họ và tên
     * @param studentId  Mã sinh viên
     * @param email      Email
     * @param phone      SĐT
     * @param gender     Giới tính
     * @param birthDate  Ngày sinh
     * @param password   Mật khẩu (sẽ được hash)
     * @param roleId     ID vai trò
     * @return MemberDTO của thành viên vừa tạo
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    public MemberDTO createMember(String fullName, String studentId, String email,
                                  String phone, String gender, LocalDate birthDate,
                                  String password, Integer roleId, List<Integer> teamIds,
                                  String avatarUrl) {
        // --- Validate ---
        if (fullName == null || fullName.isBlank())
            throw new IllegalArgumentException("Họ tên không được để trống!");
        if (studentId == null || studentId.isBlank())
            throw new IllegalArgumentException("Mã sinh viên không được để trống!");
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Email không hợp lệ!");
        if (password == null || password.length() < 6)
            throw new IllegalArgumentException("Mật khẩu ít nhất 6 ký tự!");

        // Kiểm tra trùng lặp
        if (memberDAO.findByEmail(email).isPresent())
            throw new IllegalArgumentException("Email đã tồn tại trong hệ thống!");
        if (memberDAO.findByStudentId(studentId).isPresent())
            throw new IllegalArgumentException("Mã sinh viên đã tồn tại!");

        // --- Tìm Role ---
        Role role = findRoleById(roleId);
        if (role == null)
            throw new IllegalArgumentException("Vai trò không tồn tại!");

        // --- Tạo entity ---
        Member member = new Member(
            fullName.trim(), studentId.trim(), email.trim().toLowerCase(),
            phone, gender, PasswordUtil.hashPassword(password), role
        );
        member.setBirthDate(birthDate);
        member.setAvatarUrl(normalizeAvatarUrl(avatarUrl));

        // --- Lưu vào DB ---
        Member saved = memberDAO.save(member);
        if (role.getPermissionLevel() != null && role.getPermissionLevel() <= 2) {
            memberDAO.replaceTeams(saved.getMemberId(), teamIds);
            saved = memberDAO.findById(saved.getMemberId()).orElse(saved);
        } else {
            memberDAO.replaceTeams(saved.getMemberId(), java.util.Collections.emptyList());
        }
        return toDTO(saved);
    }

    /**
     * Lấy danh sách tất cả thành viên dưới dạng DTO.
     * @return List<MemberDTO>
     */
    public List<MemberDTO> getAllMembers() {
        return memberDAO.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm thành viên theo từ khóa.
     * @param keyword Từ khóa
     * @return List<MemberDTO>
     */
    public List<MemberDTO> searchMembers(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAllMembers();
        return memberDAO.search(keyword).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thành viên theo ID.
     * @param memberId ID
     * @return Optional<MemberDTO>
     */
    public Optional<MemberDTO> getMemberById(Integer memberId) {
        return memberDAO.findById(memberId).map(this::toDTO);
    }

    /**
     * Cập nhật thông tin thành viên.
     *
     * @param memberId  ID thành viên cần cập nhật
     * @param fullName  Họ tên mới
     * @param phone     SĐT mới
     * @param gender    Giới tính mới
     * @param birthDate Ngày sinh mới
     * @param status    Trạng thái mới
     * @param roleId    Vai trò mới
     * @return MemberDTO sau khi cập nhật
     */
    public MemberDTO updateMember(Integer memberId, String fullName, String phone,
                                  String gender, LocalDate birthDate,
                                  String status, Integer roleId, List<Integer> teamIds,
                                  String avatarUrl) {
        Member member = memberDAO.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên ID: " + memberId));

        if (fullName == null || fullName.isBlank())
            throw new IllegalArgumentException("Họ tên không được để trống!");

        Role role = findRoleById(roleId);
        if (role == null)
            throw new IllegalArgumentException("Vai trò không tồn tại!");

        member.setFullName(fullName.trim());
        member.setPhone(phone);
        member.setGender(gender);
        member.setBirthDate(birthDate);
        member.setStatus(status);
        member.setRole(role);
        member.setAvatarUrl(normalizeAvatarUrl(avatarUrl));

        Member updated = memberDAO.update(member);
        if (role.getPermissionLevel() != null && role.getPermissionLevel() <= 2) {
            memberDAO.replaceTeams(memberId, teamIds);
            updated = memberDAO.findById(memberId).orElse(updated);
        } else {
            memberDAO.replaceTeams(memberId, java.util.Collections.emptyList());
        }
        return toDTO(updated);
    }

    /**
     * Cập nhật thông tin thành viên, cho phép đặt lại mật khẩu (Admin).
     */
    public MemberDTO updateMemberWithPassword(Integer memberId, String fullName, String phone,
                                              String gender, LocalDate birthDate,
                                              String status, Integer roleId, List<Integer> teamIds,
                                              String avatarUrl, String newPassword) {
        Member member = memberDAO.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên ID: " + memberId));

        if (fullName == null || fullName.isBlank())
            throw new IllegalArgumentException("Họ tên không được để trống!");

        Role role = findRoleById(roleId);
        if (role == null)
            throw new IllegalArgumentException("Vai trò không tồn tại!");

        member.setFullName(fullName.trim());
        member.setPhone(phone);
        member.setGender(gender);
        member.setBirthDate(birthDate);
        member.setStatus(status);
        member.setRole(role);
        member.setAvatarUrl(normalizeAvatarUrl(avatarUrl));

        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 6) {
                throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự!");
            }
            member.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        }

        Member updated = memberDAO.update(member);
        if (role.getPermissionLevel() != null && role.getPermissionLevel() <= 2) {
            memberDAO.replaceTeams(memberId, teamIds);
            updated = memberDAO.findById(memberId).orElse(updated);
        } else {
            memberDAO.replaceTeams(memberId, java.util.Collections.emptyList());
        }
        return toDTO(updated);
    }

    /**
     * Cập nhật thông tin cá nhân (yêu cầu mật khẩu cũ nếu đổi mật khẩu).
     */
    public MemberDTO updateSelfProfile(Integer memberId, String fullName, String phone,
                                       String gender, LocalDate birthDate,
                                       String avatarUrl, String oldPassword, String newPassword) {
        Member member = memberDAO.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên ID: " + memberId));

        if (fullName == null || fullName.isBlank())
            throw new IllegalArgumentException("Họ tên không được để trống!");

        member.setFullName(fullName.trim());
        member.setPhone(phone);
        member.setGender(gender);
        member.setBirthDate(birthDate);
        member.setAvatarUrl(normalizeAvatarUrl(avatarUrl));

        if (newPassword != null && !newPassword.isBlank()) {
            if (oldPassword == null || oldPassword.isBlank()) {
                throw new IllegalArgumentException("Vui lòng nhập mật khẩu cũ!");
            }
            if (!PasswordUtil.checkPassword(oldPassword, member.getPasswordHash())) {
                throw new IllegalArgumentException("Mật khẩu cũ không đúng!");
            }
            if (newPassword.length() < 6) {
                throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự!");
            }
            member.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        }

        Member updated = memberDAO.update(member);
        return toDTO(updated);
    }

    /**
     * Xóa thành viên (soft delete: chuyển status = Inactive).
     * Không xóa vật lý để bảo toàn lịch sử hoạt động.
     *
     * @param memberId ID thành viên
     */
    public void deactivateMember(Integer memberId) {
        Member member = memberDAO.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên!"));
        member.setStatus("Inactive");
        memberDAO.update(member);
        logger.info("Đã vô hiệu hóa thành viên ID: {}", memberId);
    }

    /**
     * Đếm số thành viên đang hoạt động (dùng cho Dashboard).
     * @return Số lượng thành viên Active
     */
    public long getActiveCount() {
        return memberDAO.countActive();
    }

    // ===================================================
    // PRIVATE HELPERS
    // ===================================================

    /**
     * Chuyển đổi Member Entity thành MemberDTO.
     * Đây là mapping function, tránh để UI phụ thuộc Entity.
     *
     * @param member Member entity
     * @return MemberDTO tương ứng
     */
    private MemberDTO toDTO(Member member) {
        String teamNames = "";
        if (member.getRole() != null && member.getRole().getPermissionLevel() != null
            && member.getRole().getPermissionLevel() <= 2) {
            if (member.getTeams() != null && !member.getTeams().isEmpty()) {
                teamNames = member.getTeams().stream()
                    .map(t -> t.getTeamName())
                    .collect(Collectors.joining(", "));
            }
        }
        Integer permValue = member.getRole() != null ? member.getRole().getPermissionLevel() : null;
        int permissionLevel = permValue != null ? permValue : 1;
        return new MemberDTO(
            member.getMemberId(),
            member.getFullName(),
            member.getStudentId(),
            member.getEmail(),
            member.getPhone(),
            member.getGender(),
            member.getBirthDate(),
            member.getJoinDate(),
            member.getStatus(),
            member.getAvatarUrl(),
            member.getRole() != null ? member.getRole().getRoleName() : "N/A",
            permissionLevel,
            teamNames
        );
    }

    private String normalizeAvatarUrl(String avatarUrl) {
        if (avatarUrl == null) return null;
        String trimmed = avatarUrl.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Tìm Role theo ID từ database.
     * @param roleId ID vai trò
     * @return Role entity hoặc null
     */
    private Role findRoleById(Integer roleId) {
        try (Session session = HibernateUtil.openSession()) {
            return session.get(Role.class, roleId);
        } catch (Exception e) {
            logger.error("Lỗi khi tìm Role ID={}: {}", roleId, e.getMessage());
            return null;
        }
    }

    /**
     * Lấy tất cả vai trò (dùng để populate ComboBox trong UI).
     * @return List<Role>
     */
    public List<Role> getAllRoles() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Role> query = session.createQuery(
                "FROM Role ORDER BY permissionLevel", Role.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách vai trò: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách vai trò", e);
        }
    }

    public List<Integer> getTeamIdsForMember(Integer memberId) {
        if (memberId == null) return java.util.Collections.emptyList();
        try (Session session = HibernateUtil.openSession()) {
            return session.createQuery(
                "SELECT t.teamId FROM Member m JOIN m.teams t WHERE m.memberId = :mid",
                Integer.class
            ).setParameter("mid", memberId).getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi lấy ban của thành viên: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
}
