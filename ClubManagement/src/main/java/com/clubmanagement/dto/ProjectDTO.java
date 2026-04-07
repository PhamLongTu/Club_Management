package com.clubmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO: ProjectDTO (Data Transfer Object cho Dự án)
 */
public class ProjectDTO {

    private Integer    projectId;
    private String     projectName;
    private String     description;
    private String     objective;
    private LocalDate  startDate;
    private LocalDate  endDate;
    private BigDecimal budget;
    private String     status;
    private String     visibility;
    private Integer    maxMembers;
    private String     managerName;   // Tên quản lý dự án
    private Integer    memberCount;   // Số thành viên tham gia (computed)
    private Integer    contributionPoints;

    // ============ CONSTRUCTORS ============
    /** Constructor mặc định. */
    public ProjectDTO() {}

    /**
     * Constructor đầy đủ.
     */
    public ProjectDTO(Integer projectId, String projectName, String description,
                      String objective, LocalDate startDate, LocalDate endDate,
                      BigDecimal budget, String status, String visibility,
                      Integer maxMembers, String managerName, Integer memberCount,
                      Integer contributionPoints) {
        this.projectId   = projectId;
        this.projectName = projectName;
        this.description = description;
        this.objective   = objective;
        this.startDate   = startDate;
        this.endDate     = endDate;
        this.budget      = budget;
        this.status      = status;
        this.visibility  = visibility;
        this.maxMembers  = maxMembers;
        this.managerName = managerName;
        this.memberCount = memberCount;
        this.contributionPoints = contributionPoints;
    }

    //GETTERS & SETTERS
    /** @return ID dự án. */
    public Integer getProjectId()          { return projectId; }
    /** @param v ID dự án mới. */
    public void setProjectId(Integer v)    { this.projectId = v; }

    /** @return Tên dự án. */
    public String getProjectName()         { return projectName; }
    /** @param v Tên dự án mới. */
    public void setProjectName(String v)   { this.projectName = v; }

    /** @return Mô tả dự án. */
    public String getDescription()         { return description; }
    /** @param v Mô tả mới. */
    public void setDescription(String v)   { this.description = v; }

    /** @return Mục tiêu dự án. */
    public String getObjective()           { return objective; }
    /** @param v Mục tiêu mới. */
    public void setObjective(String v)     { this.objective = v; }

    /** @return Ngày bắt đầu. */
    public LocalDate getStartDate()        { return startDate; }
    /** @param v Ngày bắt đầu mới. */
    public void setStartDate(LocalDate v)  { this.startDate = v; }

    /** @return Ngày kết thúc. */
    public LocalDate getEndDate()          { return endDate; }
    /** @param v Ngày kết thúc mới. */
    public void setEndDate(LocalDate v)    { this.endDate = v; }

    /** @return Ngân sách dự án. */
    public BigDecimal getBudget()          { return budget; }
    /** @param v Ngân sách mới. */
    public void setBudget(BigDecimal v)    { this.budget = v; }

    /** @return Trạng thái dự án. */
    public String getStatus()              { return status; }
    /** @param v Trạng thái mới. */
    public void setStatus(String v)        { this.status = v; }

    /** @return Chế độ hiển thị. */
    public String getVisibility()          { return visibility; }
    /** @param v Chế độ hiển thị mới. */
    public void setVisibility(String v)    { this.visibility = v; }

    /** @return Số thành viên tối đa. */
    public Integer getMaxMembers()         { return maxMembers; }
    /** @param v Số thành viên tối đa mới. */
    public void setMaxMembers(Integer v)   { this.maxMembers = v; }

    /** @return Tên quản lý dự án. */
    public String getManagerName()         { return managerName; }
    /** @param v Tên quản lý mới. */
    public void setManagerName(String v)   { this.managerName = v; }

    /** @return Số thành viên tham gia. */
    public Integer getMemberCount()        { return memberCount; }
    /** @param v Số thành viên mới. */
    public void setMemberCount(Integer v)  { this.memberCount = v; }

    /** @return Điểm đóng góp. */
    public Integer getContributionPoints() { return contributionPoints; }
    /** @param v Điểm đóng góp mới. */
    public void setContributionPoints(Integer v) { this.contributionPoints = v; }

    /**
     * Hiển thị tên dự án trong UI.
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() { return projectName; }
}
