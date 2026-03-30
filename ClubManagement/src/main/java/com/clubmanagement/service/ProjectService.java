package com.clubmanagement.service;

import com.clubmanagement.dao.ProjectDAO;
import com.clubmanagement.dto.ProjectDTO;
import com.clubmanagement.entity.Member;
import com.clubmanagement.entity.Project;
import com.clubmanagement.util.HibernateUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                                    Integer managerId) {
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

        Project saved = projectDAO.save(project);
        return toDTO(saved);
    }

    /** Lấy tất cả dự án. */
    public List<ProjectDTO> getAllProjects() {
        return projectDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
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
                                    BigDecimal budget, String status, Integer managerId) {
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
        project.setManager(findMemberById(managerId));

        return toDTO(projectDAO.update(project));
    }

    /** Xóa dự án theo ID. */
    public void deleteProject(Integer projectId) {
        if (!projectDAO.deleteById(projectId))
            throw new IllegalArgumentException("Không tìm thấy dự án để xóa!");
    }

    /** Đếm số dự án đang hoạt động (cho Dashboard). */
    public long getActiveCount() { return projectDAO.countActive(); }

    // ===================================================
    // PRIVATE HELPERS
    // ===================================================

    /** Map Project entity → ProjectDTO. */
    private ProjectDTO toDTO(Project p) {
        int memberCount = (p.getMembers() != null) ? p.getMembers().size() : 0;
        return new ProjectDTO(
            p.getProjectId(), p.getProjectName(), p.getDescription(),
            p.getObjective(), p.getStartDate(), p.getEndDate(),
            p.getBudget(), p.getStatus(),
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
