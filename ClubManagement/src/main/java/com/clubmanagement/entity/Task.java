package com.clubmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    /** Người được giao (N-1 với Member) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignee_id")
    private Member assignee;

    /** Người giao nhiệm vụ (N-1 với Member) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigner_id")
    private Member assigner;

    /** Sự kiện liên quan (nullable) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id")
    private Event event;

    // ============ CONSTRUCTORS ============
    public Task() {
        this.createdDate = LocalDateTime.now();
    }

    public Task(String title, String description, LocalDateTime deadline,
                String priority, Member assignee, Member assigner) {
        this.title       = title;
        this.description = description;
        this.deadline    = deadline;
        this.priority    = priority;
        this.assignee    = assignee;
        this.assigner    = assigner;
        this.status      = "Todo";
        this.createdDate = LocalDateTime.now();
    }

    // ============ GETTERS & SETTERS ============
    public Integer getTaskId()             { return taskId; }
    public void setTaskId(Integer v)       { this.taskId = v; }

    public String getTitle()               { return title; }
    public void setTitle(String v)         { this.title = v; }

    public String getDescription()         { return description; }
    public void setDescription(String v)   { this.description = v; }

    public LocalDateTime getDeadline()        { return deadline; }
    public void setDeadline(LocalDateTime v)  { this.deadline = v; }

    public String getPriority()            { return priority; }
    public void setPriority(String v)      { this.priority = v; }

    public String getStatus()              { return status; }
    public void setStatus(String v)        { this.status = v; }

    public LocalDateTime getCreatedDate()        { return createdDate; }
    public void setCreatedDate(LocalDateTime v)  { this.createdDate = v; }

    public Member getAssignee()            { return assignee; }
    public void setAssignee(Member v)      { this.assignee = v; }

    public Member getAssigner()            { return assigner; }
    public void setAssigner(Member v)      { this.assigner = v; }

    public Event getEvent()                { return event; }
    public void setEvent(Event v)          { this.event = v; }

    @Override
    public String toString() { return title; }
}
