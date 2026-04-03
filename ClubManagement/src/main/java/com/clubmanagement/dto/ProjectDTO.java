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

    // ============ CONSTRUCTORS ============
    public ProjectDTO() {}

    public ProjectDTO(Integer projectId, String projectName, String description,
                      String objective, LocalDate startDate, LocalDate endDate,
                      BigDecimal budget, String status, String visibility,
                      Integer maxMembers, String managerName, Integer memberCount) {
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
    }

    // ============ GETTERS & SETTERS ============
    public Integer getProjectId()          { return projectId; }
    public void setProjectId(Integer v)    { this.projectId = v; }

    public String getProjectName()         { return projectName; }
    public void setProjectName(String v)   { this.projectName = v; }

    public String getDescription()         { return description; }
    public void setDescription(String v)   { this.description = v; }

    public String getObjective()           { return objective; }
    public void setObjective(String v)     { this.objective = v; }

    public LocalDate getStartDate()        { return startDate; }
    public void setStartDate(LocalDate v)  { this.startDate = v; }

    public LocalDate getEndDate()          { return endDate; }
    public void setEndDate(LocalDate v)    { this.endDate = v; }

    public BigDecimal getBudget()          { return budget; }
    public void setBudget(BigDecimal v)    { this.budget = v; }

    public String getStatus()              { return status; }
    public void setStatus(String v)        { this.status = v; }

    public String getVisibility()          { return visibility; }
    public void setVisibility(String v)    { this.visibility = v; }

    public Integer getMaxMembers()         { return maxMembers; }
    public void setMaxMembers(Integer v)   { this.maxMembers = v; }

    public String getManagerName()         { return managerName; }
    public void setManagerName(String v)   { this.managerName = v; }

    public Integer getMemberCount()        { return memberCount; }
    public void setMemberCount(Integer v)  { this.memberCount = v; }

    @Override
    public String toString() { return projectName; }
}
