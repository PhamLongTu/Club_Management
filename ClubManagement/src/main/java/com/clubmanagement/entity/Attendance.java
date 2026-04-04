package com.clubmanagement.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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
    /**
     * Constructor mặc định (bắt buộc cho JPA).
     */
    public Attendance() {}

    /**
     * Constructor khởi tạo nhanh bản ghi điểm danh.
     *
     * @param member     Thành viên
     * @param event      Sự kiện
     * @param checkInTime Thời gian check-in
     * @param status     Trạng thái điểm danh
     */
    public Attendance(Member member, Event event, LocalDateTime checkInTime, String status) {
        this.member      = member;
        this.event       = event;
        this.checkInTime = checkInTime;
        this.status      = status;
    }

    // ============ GETTERS & SETTERS ============
    /** @return ID điểm danh. */
    public Integer getAttendanceId()       { return attendanceId; }
    /** @param v ID điểm danh mới. */
    public void setAttendanceId(Integer v) { this.attendanceId = v; }

    /** @return Thành viên được điểm danh. */
    public Member getMember()          { return member; }
    /** @param v Thành viên mới. */
    public void setMember(Member v)    { this.member = v; }

    /** @return Sự kiện được điểm danh. */
    public Event getEvent()            { return event; }
    /** @param v Sự kiện mới. */
    public void setEvent(Event v)      { this.event = v; }

    /** @return Thời gian check-in. */
    public LocalDateTime getCheckInTime()        { return checkInTime; }
    /** @param v Thời gian check-in mới. */
    public void setCheckInTime(LocalDateTime v)  { this.checkInTime = v; }

    /** @return Thời gian check-out. */
    public LocalDateTime getCheckOutTime()        { return checkOutTime; }
    /** @param v Thời gian check-out mới. */
    public void setCheckOutTime(LocalDateTime v)  { this.checkOutTime = v; }

    /** @return Trạng thái điểm danh. */
    public String getStatus()              { return status; }
    /** @param v Trạng thái mới. */
    public void setStatus(String v)        { this.status = v; }

    /** @return Ghi chú. */
    public String getNote()                { return note; }
    /** @param v Ghi chú mới. */
    public void setNote(String v)          { this.note = v; }
}
