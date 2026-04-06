package com.clubmanagement.entity;

import java.time.LocalDateTime;
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
 * Entity: Task (Nhiệm vụ)
 * Map tới bảng 'tasks'.
 * Một nhiệm vụ được giao bởi người này cho người khác,
 * có thể liên quan đến một sự kiện cụ thể.
 */
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Integer taskId;

    /** Tiêu đề nhiệm vụ */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /** Mô tả chi tiết */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Hạn hoàn thành */
    @Column(name = "deadline")
    private LocalDateTime deadline;

    /**
     * Mức độ ưu tiên: Low / Medium / High / Critical
     */
    @Column(name = "priority", length = 20)
    private String priority = "Medium";

    /**
     * Trạng thái: Todo / InProgress / Done / Overdue
     */
    @Column(name = "status", length = 20)
    private String status = "Todo";

    /** Ngày tạo nhiệm vụ */
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    /** Hiển thị công khai hay riêng tư */
    @Column(name = "visibility", length = 10)
    private String visibility = "Public";

    /** Số người tối đa có thể tham gia */
    @Column(name = "max_assignees")
    private Integer maxAssignees = 1;

    /** Điểm đóng góp cho nhiệm vụ */
    @Column(name = "contribution_points")
    private Integer contributionPoints = 0;

    /** Người giao nhiệm vụ (N-1 với Member) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigner_id")
    private Member assigner;

    /** Danh sách người thực hiện (N-N với Member) */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_members",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private List<Member> assignees;

    /** Sự kiện liên quan (nullable) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id")
    private Event event;

    // ============ CONSTRUCTORS ============
    /**
     * Constructor mặc định (bắt buộc cho JPA).
     */
    public Task() {
        this.createdDate = LocalDateTime.now();
    }

    /**
     * Constructor khởi tạo nhanh nhiệm vụ mới.
     *
     * @param title       Tiêu đề
     * @param description Mô tả
     * @param deadline    Hạn hoàn thành
     * @param priority    Mức độ ưu tiên
     * @param assigner    Người giao nhiệm vụ
     */
    public Task(String title, String description, LocalDateTime deadline,
                String priority, Member assigner) {
        this.title       = title;
        this.description = description;
        this.deadline    = deadline;
        this.priority    = priority;
        this.assigner    = assigner;
        this.status      = "Todo";
        this.createdDate = LocalDateTime.now();
    }

    // ============ GETTERS & SETTERS ============
    /** @return ID nhiệm vụ. */
    public Integer getTaskId()             { return taskId; }
    /** @param v ID nhiệm vụ mới. */
    public void setTaskId(Integer v)       { this.taskId = v; }

    /** @return Tiêu đề nhiệm vụ. */
    public String getTitle()               { return title; }
    /** @param v Tiêu đề mới. */
    public void setTitle(String v)         { this.title = v; }

    /** @return Mô tả nhiệm vụ. */
    public String getDescription()         { return description; }
    /** @param v Mô tả mới. */
    public void setDescription(String v)   { this.description = v; }

    /** @return Hạn hoàn thành. */
    public LocalDateTime getDeadline()        { return deadline; }
    /** @param v Hạn hoàn thành mới. */
    public void setDeadline(LocalDateTime v)  { this.deadline = v; }

    /** @return Mức độ ưu tiên. */
    public String getPriority()            { return priority; }
    /** @param v Mức độ ưu tiên mới. */
    public void setPriority(String v)      { this.priority = v; }

    /** @return Trạng thái nhiệm vụ. */
    public String getStatus()              { return status; }
    /** @param v Trạng thái mới. */
    public void setStatus(String v)        { this.status = v; }

    /** @return Ngày tạo nhiệm vụ. */
    public LocalDateTime getCreatedDate()        { return createdDate; }
    /** @param v Ngày tạo mới. */
    public void setCreatedDate(LocalDateTime v)  { this.createdDate = v; }

    /** @return Chế độ hiển thị. */
    public String getVisibility()          { return visibility; }
    /** @param v Chế độ hiển thị mới. */
    public void setVisibility(String v)    { this.visibility = v; }

    /** @return Số người tối đa tham gia. */
    public Integer getMaxAssignees()       { return maxAssignees; }
    /** @param v Số người tối đa mới. */
    public void setMaxAssignees(Integer v) { this.maxAssignees = v; }

    /** @return Điểm đóng góp. */
    public Integer getContributionPoints()       { return contributionPoints; }
    /** @param v Điểm đóng góp mới. */
    public void setContributionPoints(Integer v) { this.contributionPoints = v; }

    /** @return Người giao nhiệm vụ. */
    public Member getAssigner()            { return assigner; }
    /** @param v Người giao nhiệm vụ mới. */
    public void setAssigner(Member v)      { this.assigner = v; }

    /** @return Danh sách người thực hiện. */
    public List<Member> getAssignees()         { return assignees; }
    /** @param v Danh sách người thực hiện mới. */
    public void setAssignees(List<Member> v)   { this.assignees = v; }

    /** @return Sự kiện liên quan. */
    public Event getEvent()                { return event; }
    /** @param v Sự kiện liên quan mới. */
    public void setEvent(Event v)          { this.event = v; }

    /**
     * Hiển thị tên nhiệm vụ trong UI.
     *
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() { return title; }
}
