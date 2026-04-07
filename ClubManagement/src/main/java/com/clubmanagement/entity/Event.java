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

    /** Loại điểm áp dụng: None / DRL / CTXH */
    @Column(name = "point_type", length = 10)
    private String pointType = "None";

    /** Điểm áp dụng cho sự kiện */
    @Column(name = "point_value")
    private Integer pointValue = 0;

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

    //CONSTRUCTORS =
    /**
     * Constructor mặc định (bắt buộc cho JPA).
     */
    public Event() {}

    /**
     * Constructor khởi tạo nhanh sự kiện.
     *
     * @param eventName  Tên sự kiện
     * @param description Mô tả
     * @param startDate  Thời gian bắt đầu
     * @param endDate    Thời gian kết thúc
     * @param location   Địa điểm
     * @param budget     Ngân sách
     * @param createdBy  Người tạo
     */
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
    /** @return ID sự kiện. */
    public Integer getEventId()              { return eventId; }
    /** @param v ID sự kiện mới. */
    public void setEventId(Integer v)        { this.eventId = v; }

    /** @return Tên sự kiện. */
    public String getEventName()             { return eventName; }
    /** @param v Tên sự kiện mới. */
    public void setEventName(String v)       { this.eventName = v; }

    /** @return Mô tả sự kiện. */
    public String getDescription()           { return description; }
    /** @param v Mô tả mới. */
    public void setDescription(String v)     { this.description = v; }

    /** @return Thời gian bắt đầu. */
    public LocalDateTime getStartDate()          { return startDate; }
    /** @param v Thời gian bắt đầu mới. */
    public void setStartDate(LocalDateTime v)    { this.startDate = v; }

    /** @return Thời gian kết thúc. */
    public LocalDateTime getEndDate()            { return endDate; }
    /** @param v Thời gian kết thúc mới. */
    public void setEndDate(LocalDateTime v)      { this.endDate = v; }

    /** @return Hạn đăng ký. */
    public LocalDateTime getRegistrationDeadline()       { return registrationDeadline; }
    /** @param v Hạn đăng ký mới. */
    public void setRegistrationDeadline(LocalDateTime v) { this.registrationDeadline = v; }

    /** @return Địa điểm tổ chức. */
    public String getLocation()              { return location; }
    /** @param v Địa điểm mới. */
    public void setLocation(String v)        { this.location = v; }

    /** @return Trạng thái sự kiện. */
    public String getStatus()                { return status; }
    /** @param v Trạng thái mới. */
    public void setStatus(String v)          { this.status = v; }

    /** @return Ngân sách sự kiện. */
    public BigDecimal getBudget()            { return budget; }
    /** @param v Ngân sách mới. */
    public void setBudget(BigDecimal v)      { this.budget = v; }

    /** @return Số lượng tham gia tối đa. */
    public Integer getMaxParticipants()      { return maxParticipants; }
    /** @param v Số lượng tham gia tối đa mới. */
    public void setMaxParticipants(Integer v){ this.maxParticipants = v; }

    /** @return Loại điểm áp dụng. */
    public String getPointType()             { return pointType; }
    /** @param v Loại điểm áp dụng mới. */
    public void setPointType(String v)       { this.pointType = v; }

    /** @return Điểm áp dụng. */
    public Integer getPointValue()           { return pointValue; }
    /** @param v Điểm áp dụng mới. */
    public void setPointValue(Integer v)     { this.pointValue = v; }

    /** @return Người tạo sự kiện. */
    public Member getCreatedBy()             { return createdBy; }
    /** @param v Người tạo mới. */
    public void setCreatedBy(Member v)       { this.createdBy = v; }

    /** @return Danh sách đăng ký tham gia. */
    public List<Participation> getParticipations()       { return participations; }
    /** @param v Danh sách đăng ký mới. */
    public void setParticipations(List<Participation> v) { this.participations = v; }

    /** @return Danh sách điểm danh. */
    public List<Attendance> getAttendances()       { return attendances; }
    /** @param v Danh sách điểm danh mới. */
    public void setAttendances(List<Attendance> v) { this.attendances = v; }

    /** @return Danh sách nhiệm vụ liên quan. */
    public List<Task> getTasks()         { return tasks; }
    /** @param v Danh sách nhiệm vụ mới. */
    public void setTasks(List<Task> v)   { this.tasks = v; }

    /**
     * Hiển thị tên sự kiện trong UI.
     *
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() { return eventName; }
}
