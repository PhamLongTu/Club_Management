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
                                    Integer managerId, List<Integer> memberIds) {
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

        Project saved = projectDAO.save(project);
        if (memberIds != null && !memberIds.isEmpty()) {
            projectDAO.replaceMembers(saved.getProjectId(), memberIds);
            saved = projectDAO.findById(saved.getProjectId()).orElse(saved);
        }
        return toDTO(saved);
    }

    /** Lấy tất cả dự án. */
    public List<ProjectDTO> getAllProjects() {
        return projectDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ProjectDTO> getPublicUnassignedProjects() {
        return projectDAO.findPublicUnassigned().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ProjectDTO> getProjectsForUser(Integer memberId) {
        return projectDAO.findByMember(memberId).stream().map(this::toDTO).collect(Collectors.toList());
    }

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
                                    Integer managerId, List<Integer> memberIds) {
        Project project = projectDAO.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dự án ID: " + projectId));

        if (projectName == null || projectName.isBlank())
            throw new IllegalArgumentException("Tên dự án không được để trống!");
        if (endDate != null && startDate != null && endDate.isBefore(startDate))
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu!");

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

        Project updated = projectDAO.update(project);
        if (memberIds != null) {
            projectDAO.replaceMembers(projectId, memberIds);
            updated = projectDAO.findById(projectId).orElse(updated);
        }
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

    public void registerForProject(Integer projectId, Integer memberId) {
        projectDAO.addMember(projectId, memberId);
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
        return new MemberDTO(
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
            memberCount
        );
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
