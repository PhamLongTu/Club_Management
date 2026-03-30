package com.clubmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity: Attendance (Điểm danh)
 * Map tới bảng 'attendances'.
 * Ghi nhận thời gian thành viên check-in/check-out tại sự kiện.
 */
@Entity
@Table(name = "attendances",
    uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "event_id"}))
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Integer attendanceId;

    /** Thành viên được điểm danh */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** Sự kiện được điểm danh */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /** Thời gian check in (đến) */
    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    /** Thời gian check out (về) */
    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    /**
     * Trạng thái điểm danh:
     * Present / Late / Absent / Excused
     */
    @Column(name = "status", length = 20)
    private String status = "Present";

    /** Ghi chú */
    @Column(name = "note", length = 500)
    private String note;

    // ============ CONSTRUCTORS ============
    public Attendance() {}

    public Attendance(Member member, Event event, LocalDateTime checkInTime, String status) {
        this.member      = member;
        this.event       = event;
        this.checkInTime = checkInTime;
        this.status      = status;
    }

    // ============ GETTERS & SETTERS ============
    public Integer getAttendanceId()       { return attendanceId; }
    public void setAttendanceId(Integer v) { this.attendanceId = v; }

    public Member getMember()          { return member; }
    public void setMember(Member v)    { this.member = v; }

    public Event getEvent()            { return event; }
    public void setEvent(Event v)      { this.event = v; }

    public LocalDateTime getCheckInTime()        { return checkInTime; }
    public void setCheckInTime(LocalDateTime v)  { this.checkInTime = v; }

    public LocalDateTime getCheckOutTime()        { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime v)  { this.checkOutTime = v; }

    public String getStatus()              { return status; }
    public void setStatus(String v)        { this.status = v; }

    public String getNote()                { return note; }
    public void setNote(String v)          { this.note = v; }
}
