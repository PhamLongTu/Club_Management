package com.clubmanagement.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.dao.ProjectDAO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.dto.ProjectDTO;
import com.clubmanagement.entity.Member;
import com.clubmanagement.entity.Project;
import com.clubmanagement.util.HibernateUtil;

/**
 * ProjectService - Tầng nghiệp vụ cho Dự án.
 */
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final MemberService memberService = new MemberService();

    /**
     * Tạo dự án mới với validation.
     *
     * @param projectName Tên dự án
     * @param description Mô tả
     * @param objective   Mục tiêu
     * @param startDate   Ngày bắt đầu
     * @param endDate     Ngày kết thúc (phải sau startDate)
     * @param budget      Ngân sách (>= 0)
     * @param managerId   ID Quản lý dự án
     * @return ProjectDTO vừa tạo
     */
    public ProjectDTO createProject(String projectName, String description,
                                    String objective, LocalDate startDate,
                                    LocalDate endDate, BigDecimal budget,
                                    String visibility, Integer maxMembers,
                                    Integer managerId, List<Integer> memberIds,
                                    Integer contributionPoints) {
        // --- Validate ---
        if (projectName == null || projectName.isBlank())
            throw new IllegalArgumentException("Tên dự án không được để trống!");
        if (startDate != null && endDate != null && endDate.isBefore(startDate))
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu!");
        if (budget != null && budget.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Ngân sách không được âm!");

        Member manager = findMemberById(managerId);

        Project project = new Project(
            projectName.trim(), description, objective,
            startDate, endDate,
            budget != null ? budget : BigDecimal.ZERO,
            manager
        );
        project.setVisibility(visibility != null ? visibility : "Public");
        project.setMaxMembers(maxMembers != null ? maxMembers : 0);
        project.setContributionPoints(contributionPoints != null ? contributionPoints : 0);

        Project saved = projectDAO.save(project);
        if (memberIds != null && !memberIds.isEmpty()) {
            projectDAO.replaceMembers(saved.getProjectId(), memberIds);
            saved = projectDAO.findById(saved.getProjectId()).orElse(saved);
            List<Integer> assignedIds = saved.getMembers() != null
                ? saved.getMembers().stream().map(Member::getMemberId).collect(java.util.stream.Collectors.toList())
                : java.util.Collections.emptyList();
            applyContributionPoints(assignedIds, 0, project.getContributionPoints());
        }
        return toDTO(saved);
    }

    /** Lấy tất cả dự án. */
    public List<ProjectDTO> getAllProjects() {
        return projectDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Lấy các dự án public chưa có thành viên tham gia.
     * @return Danh sách ProjectDTO
     */
    public List<ProjectDTO> getPublicUnassignedProjects() {
        return projectDAO.findPublicUnassigned().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Lấy các dự án mà thành viên đang tham gia.
     * @param memberId ID thành viên
     * @return Danh sách ProjectDTO
     */
    public List<ProjectDTO> getProjectsForUser(Integer memberId) {
        return projectDAO.findByMember(memberId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Lấy danh sách dự án có thể xem được của thành viên (public + tham gia).
     * @param memberId ID thành viên
     * @return Danh sách ProjectDTO
     */
    public List<ProjectDTO> getVisibleProjectsForUser(Integer memberId) {
        List<ProjectDTO> result = new java.util.ArrayList<>();
        result.addAll(projectDAO.findPublic().stream().map(this::toDTO).collect(Collectors.toList()));
        result.addAll(getProjectsForUser(memberId));
        return result.stream()
            .collect(Collectors.toMap(ProjectDTO::getProjectId, p -> p, (a, b) -> a))
            .values()
            .stream()
            .sorted(java.util.Comparator.comparing(ProjectDTO::getStartDate, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())).reversed())
            .collect(Collectors.toList());
    }

    /** Lấy dự án theo trạng thái. */
    public List<ProjectDTO> getProjectsByStatus(String status) {
        return projectDAO.findByStatus(status).stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** Tìm kiếm dự án theo từ khóa. */
    public List<ProjectDTO> searchProjects(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAllProjects();
        return projectDAO.search(keyword).stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** Lấy dự án theo ID. */
    public Optional<ProjectDTO> getProjectById(Integer projectId) {
        return projectDAO.findById(projectId).map(this::toDTO);
    }

    /**
     * Cập nhật thông tin dự án.
     */
    public ProjectDTO updateProject(Integer projectId, String projectName,
                                    String description, String objective,
                                    LocalDate startDate, LocalDate endDate,
                                    BigDecimal budget, String status,
                                    String visibility, Integer maxMembers,
                                    Integer managerId, List<Integer> memberIds,
                                    Integer contributionPoints) {
        Project project = projectDAO.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dự án ID: " + projectId));

        if (projectName == null || projectName.isBlank())
            throw new IllegalArgumentException("Tên dự án không được để trống!");
        if (endDate != null && startDate != null && endDate.isBefore(startDate))
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu!");

        int oldPoints = safePoints(project.getContributionPoints());
        int newPoints = contributionPoints != null ? contributionPoints : oldPoints;
        List<Integer> oldMembers = project.getMembers() != null
            ? project.getMembers().stream().map(Member::getMemberId).collect(java.util.stream.Collectors.toList())
            : java.util.Collections.emptyList();
        List<Integer> newMembers = memberIds != null ? memberIds : oldMembers;

        project.setProjectName(projectName.trim());
        project.setDescription(description);
        project.setObjective(objective);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        project.setBudget(budget);
        project.setStatus(status);
        project.setVisibility(visibility != null ? visibility : project.getVisibility());
        project.setMaxMembers(maxMembers != null ? maxMembers : project.getMaxMembers());
        project.setManager(findMemberById(managerId));
        project.setContributionPoints(newPoints);

        Project updated = projectDAO.update(project);
        if (memberIds != null) {
            projectDAO.replaceMembers(projectId, memberIds);
            updated = projectDAO.findById(projectId).orElse(updated);
        }
        applyContributionDiff(oldMembers, newMembers, oldPoints, newPoints);
        return toDTO(updated);
    }

    /** Xóa dự án theo ID. */
    public void deleteProject(Integer projectId) {
        if (!projectDAO.deleteById(projectId))
            throw new IllegalArgumentException("Không tìm thấy dự án để xóa!");
    }

    /** Đếm số dự án đang hoạt động (cho Dashboard). */
    public long getActiveCount() { return projectDAO.countActive(); }

    /**
     * Lấy danh sách thành viên tham gia dự án.
     * @param projectId ID dự án
     * @return Danh sách MemberDTO (đã được map trong session để tránh lazy-load)
     */
    public List<MemberDTO> getMembersOfProject(Integer projectId) {
        return projectDAO.findById(projectId)
            .map(project -> {
                List<Member> members = project.getMembers();
                if (members == null) return new java.util.ArrayList<MemberDTO>();
                return members.stream().map(this::memberToDTO).collect(Collectors.toList());
            })
            .orElse(new java.util.ArrayList<>());
    }

    /** Thêm thành viên vào dự án. */
    public void addMemberToProject(Integer projectId, Integer memberId) {
        projectDAO.addMember(projectId, memberId);
    }

    /**
     * Đăng ký thành viên tham gia dự án.
     * @param projectId ID dự án
     * @param memberId ID thành viên
     */
    public void registerForProject(Integer projectId, Integer memberId) {
        Project project = projectDAO.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dự án"));
        boolean alreadyInProject = project.getMembers() != null
            && project.getMembers().stream().anyMatch(m -> m.getMemberId().equals(memberId));
        if (alreadyInProject) {
            throw new IllegalStateException("Bạn đã đăng ký dự án này rồi");
        }
        projectDAO.addMember(projectId, memberId);
        applyContributionPoints(java.util.List.of(memberId), 0, safePoints(project.getContributionPoints()));
    }

    /**
     * Hủy đăng ký dự án (chỉ cho phép khi dự án chưa bắt đầu).
     * @param projectId ID dự án
     * @param memberId ID thành viên
     */
    public void unregisterFromProject(Integer projectId, Integer memberId) {
        if (projectId == null || memberId == null) {
            throw new IllegalArgumentException("Thiếu thông tin hủy đăng ký dự án");
        }
        Project project = projectDAO.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dự án"));
        if (project.getStartDate() != null && !project.getStartDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Dự án đã bắt đầu, không thể hủy");
        }
        projectDAO.removeMember(projectId, memberId);
        applyContributionPoints(java.util.List.of(memberId), safePoints(project.getContributionPoints()), 0);
    }

    /** Xóa thành viên khỏi dự án. */
    public void removeMemberFromProject(Integer projectId, Integer memberId) {
        projectDAO.removeMember(projectId, memberId);
    }

    /** Map Member entity → MemberDTO. */
    private MemberDTO memberToDTO(Member m) {
        String teamNames = "";
        if (m.getRole() != null && m.getRole().getPermissionLevel() != null
            && m.getRole().getPermissionLevel() <= 2) {
            if (m.getTeams() != null && !m.getTeams().isEmpty()) {
                teamNames = m.getTeams().stream()
                    .map(t -> t.getTeamName())
                    .collect(Collectors.joining(", "));
            }
        }
        Integer permValue = m.getRole() != null ? m.getRole().getPermissionLevel() : null;
        int permissionLevel = permValue != null ? permValue : 1;
        MemberDTO dto = new MemberDTO(
            m.getMemberId(),
            m.getFullName(),
            m.getStudentId(),
            m.getEmail(),
            m.getPhone(),
            m.getGender(),
            m.getBirthDate(),
            m.getJoinDate(),
            m.getStatus(),
            m.getAvatarUrl(),
            m.getRole() != null ? m.getRole().getRoleName() : "N/A",
            permissionLevel,
            teamNames
        );
        dto.setDrlPoints(m.getDrlPoints());
        dto.setCtxhPoints(m.getCtxhPoints());
        dto.setContributionPoints(m.getContributionPoints());
        return dto;
    }

    // ===================================================
    // PRIVATE HELPERS
    // ===================================================

    /** Map Project entity → ProjectDTO. */
    private ProjectDTO toDTO(Project p) {
        int memberCount = (p.getMembers() != null) ? p.getMembers().size() : 0;
        return new ProjectDTO(
            p.getProjectId(), p.getProjectName(), p.getDescription(),
            p.getObjective(), p.getStartDate(), p.getEndDate(),
            p.getBudget(), p.getStatus(), p.getVisibility(), p.getMaxMembers(),
            p.getManager() != null ? p.getManager().getFullName() : "N/A",
            memberCount,
            p.getContributionPoints()
        );
    }

    private void applyContributionPoints(List<Integer> memberIds, int oldPoints, int newPoints) {
        if (memberIds == null || memberIds.isEmpty()) return;
        int delta = newPoints - oldPoints;
        if (delta == 0) return;
        java.util.Set<Integer> uniqueIds = new java.util.HashSet<>(memberIds);
        for (Integer memberId : uniqueIds) {
            memberService.adjustPoints(memberId, 0, 0, delta);
        }
    }

    private void applyContributionDiff(List<Integer> oldIds, List<Integer> newIds, int oldPoints, int newPoints) {
        java.util.Set<Integer> oldSet = new java.util.HashSet<>(oldIds != null ? oldIds : java.util.Collections.emptyList());
        java.util.Set<Integer> newSet = new java.util.HashSet<>(newIds != null ? newIds : java.util.Collections.emptyList());

        java.util.Set<Integer> added = new java.util.HashSet<>(newSet);
        added.removeAll(oldSet);

        java.util.Set<Integer> removed = new java.util.HashSet<>(oldSet);
        removed.removeAll(newSet);

        java.util.Set<Integer> stayed = new java.util.HashSet<>(newSet);
        stayed.retainAll(oldSet);

        if (!added.isEmpty() && newPoints != 0) {
            applyContributionPoints(new java.util.ArrayList<>(added), 0, newPoints);
        }
        if (!removed.isEmpty() && oldPoints != 0) {
            applyContributionPoints(new java.util.ArrayList<>(removed), oldPoints, 0);
        }
        int delta = newPoints - oldPoints;
        if (!stayed.isEmpty() && delta != 0) {
            applyContributionPoints(new java.util.ArrayList<>(stayed), 0, delta);
        }
    }

    private int safePoints(Integer value) {
        return value != null ? value : 0;
    }

    /** Tìm Member entity theo ID. */
    private Member findMemberById(Integer memberId) {
        if (memberId == null) return null;
        try (Session session = HibernateUtil.openSession()) {
            return session.get(Member.class, memberId);
        } catch (Exception e) {
            logger.error("Lỗi khi tìm Member ID={}: {}", memberId, e.getMessage());
            return null;
        }
    }
}
