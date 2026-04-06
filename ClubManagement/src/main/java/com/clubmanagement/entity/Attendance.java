package com.clubmanagement.entity;

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

    // ============ CONSTRUCTORS ============
    /**
     * Constructor mặc định (bắt buộc cho JPA).
     */
    public Attendance() {}

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

}
