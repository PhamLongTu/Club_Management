package com.clubmanagement.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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

    /** Hiển thị công khai hay riêng tư */
    @Column(name = "visibility", length = 10)
    private String visibility = "Public";

    /** Số thành viên tối đa (0 = không giới hạn) */
    @Column(name = "max_members")
    private Integer maxMembers = 0;

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


    // ============ CONSTRUCTORS ============
    /**
     * Constructor mặc định (bắt buộc cho JPA).
     */
    public Project() {}

    /**
     * Constructor khởi tạo nhanh dự án.
     *
     * @param projectName Tên dự án
     * @param description Mô tả
     * @param objective   Mục tiêu
     * @param startDate   Ngày bắt đầu
     * @param endDate     Ngày kết thúc
     * @param budget      Ngân sách
     * @param manager     Quản lý dự án
     */
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

    /** @return Ngày kết thúc dự kiến. */
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

    /** @return Quản lý dự án. */
    public Member getManager()             { return manager; }
    /** @param v Quản lý dự án mới. */
    public void setManager(Member v)       { this.manager = v; }

    /** @return Danh sách thành viên tham gia. */
    public List<Member> getMembers()       { return members; }
    /** @param v Danh sách thành viên mới. */
    public void setMembers(List<Member> v) { this.members = v; }

    /**
     * Hiển thị tên dự án trong UI.
     *
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() { return projectName; }
}
