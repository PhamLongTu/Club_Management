package com.clubmanagement.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entity: Event (Sự kiện của CLB)
 * Map tới bảng 'events'.
 */
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Integer eventId;

    /** Tên sự kiện */
    @Column(name = "event_name", nullable = false, length = 200)
    private String eventName;

    /** Mô tả chi tiết */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Thời gian bắt đầu */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /** Thời gian kết thúc */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /** Hạn đăng ký tham gia */
    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    /** Địa điểm tổ chức */
    @Column(name = "location", length = 255)
    private String location;

    /**
     * Trạng thái: Upcoming / Ongoing / Completed / Cancelled
     */
    @Column(name = "status", length = 20)
    private String status = "Upcoming";

    /** Ngân sách sự kiện (VNĐ) */
    @Column(name = "budget", precision = 15, scale = 2)
    private BigDecimal budget = BigDecimal.ZERO;

    /** Số lượng tham gia tối đa */
    @Column(name = "max_participants")
    private Integer maxParticipants = 100;

    /** Người tạo sự kiện */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private Member createdBy;

    /** Danh sách đăng ký tham gia */
    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Participation> participations;

    /** Danh sách điểm danh */
    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Attendance> attendances;

    /** Nhiệm vụ liên quan đến sự kiện */
    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private List<Task> tasks;

    // ============ CONSTRUCTORS ============
    public Event() {}

    public Event(String eventName, String description, LocalDateTime startDate,
                 LocalDateTime endDate, String location, BigDecimal budget, Member createdBy) {
        this.eventName  = eventName;
        this.description = description;
        this.startDate  = startDate;
        this.endDate    = endDate;
        this.location   = location;
        this.budget     = budget;
        this.createdBy  = createdBy;
    }

    // ============ GETTERS & SETTERS ============
    public Integer getEventId()              { return eventId; }
    public void setEventId(Integer v)        { this.eventId = v; }

    public String getEventName()             { return eventName; }
    public void setEventName(String v)       { this.eventName = v; }

    public String getDescription()           { return description; }
    public void setDescription(String v)     { this.description = v; }

    public LocalDateTime getStartDate()          { return startDate; }
    public void setStartDate(LocalDateTime v)    { this.startDate = v; }

    public LocalDateTime getEndDate()            { return endDate; }
    public void setEndDate(LocalDateTime v)      { this.endDate = v; }

    public LocalDateTime getRegistrationDeadline()       { return registrationDeadline; }
    public void setRegistrationDeadline(LocalDateTime v) { this.registrationDeadline = v; }

    public String getLocation()              { return location; }
    public void setLocation(String v)        { this.location = v; }

    public String getStatus()                { return status; }
    public void setStatus(String v)          { this.status = v; }

    public BigDecimal getBudget()            { return budget; }
    public void setBudget(BigDecimal v)      { this.budget = v; }

    public Integer getMaxParticipants()      { return maxParticipants; }
    public void setMaxParticipants(Integer v){ this.maxParticipants = v; }

    public Member getCreatedBy()             { return createdBy; }
    public void setCreatedBy(Member v)       { this.createdBy = v; }

    public List<Participation> getParticipations()       { return participations; }
    public void setParticipations(List<Participation> v) { this.participations = v; }

    public List<Attendance> getAttendances()       { return attendances; }
    public void setAttendances(List<Attendance> v) { this.attendances = v; }

    public List<Task> getTasks()         { return tasks; }
    public void setTasks(List<Task> v)   { this.tasks = v; }

    @Override
    public String toString() { return eventName; }
}
