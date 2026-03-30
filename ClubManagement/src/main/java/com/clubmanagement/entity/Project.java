package com.clubmanagement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Entity: Project (Dự án của CLB)
 * Map tới bảng 'projects'.
 * Dự án là hoạt động dài hạn của CLB,
 * khác với sự kiện (Event) là hoạt động đơn lẻ ngắn hạn.
 */
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Integer projectId;

    /** Tên dự án */
    @Column(name = "project_name", nullable = false, length = 200)
    private String projectName;

    /** Mô tả dự án */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Mục tiêu cần đạt được */
    @Column(name = "objective", columnDefinition = "TEXT")
    private String objective;

    /** Ngày bắt đầu */
    @Column(name = "start_date")
    private LocalDate startDate;

    /** Ngày kết thúc dự kiến */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** Ngân sách dự án (VNĐ) */
    @Column(name = "budget", precision = 15, scale = 2)
    private BigDecimal budget = BigDecimal.ZERO;

    /**
     * Trạng thái: Planning / Active / OnHold / Completed / Cancelled
     */
    @Column(name = "status", length = 20)
    private String status = "Planning";

    /** Quản lý dự án (N-1 với Member) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manager_id")
    private Member manager;

    /** Danh sách thành viên tham gia (N-N với Member) */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "project_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private List<Member> members;

    /** Danh sách phản hồi */
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private List<Feedback> feedbacks;

    // ============ CONSTRUCTORS ============
    public Project() {}

    public Project(String projectName, String description, String objective,
                   LocalDate startDate, LocalDate endDate, BigDecimal budget, Member manager) {
        this.projectName = projectName;
        this.description = description;
        this.objective   = objective;
        this.startDate   = startDate;
        this.endDate     = endDate;
        this.budget      = budget;
        this.manager     = manager;
        this.status      = "Planning";
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

    public Member getManager()             { return manager; }
    public void setManager(Member v)       { this.manager = v; }

    public List<Member> getMembers()       { return members; }
    public void setMembers(List<Member> v) { this.members = v; }

    public List<Feedback> getFeedbacks()         { return feedbacks; }
    public void setFeedbacks(List<Feedback> v)   { this.feedbacks = v; }

    @Override
    public String toString() { return projectName; }
}
