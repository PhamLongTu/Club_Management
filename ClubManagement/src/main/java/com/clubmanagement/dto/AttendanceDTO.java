package com.clubmanagement.dto;

import java.time.LocalDateTime;

/**
 * AttendanceDTO - Data Transfer Object của Điểm danh
 */
public class AttendanceDTO {
    
    private Integer attendanceId;
    private Integer memberId;
    private String memberName;
    private Integer eventId;
    private String eventName;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String status;
    private String note;

    /** Constructor mặc định. */
    public AttendanceDTO() {}

    /**
     * Constructor đầy đủ.
     */
    public AttendanceDTO(Integer attendanceId, Integer memberId, String memberName, 
                         Integer eventId, String eventName, LocalDateTime checkInTime, 
                         LocalDateTime checkOutTime, String status, String note) {
        this.attendanceId = attendanceId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.eventId = eventId;
        this.eventName = eventName;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
        this.note = note;
    }

    /** @return ID điểm danh. */
    public Integer getAttendanceId() { return attendanceId; }
    /** @param attendanceId ID điểm danh mới. */
    public void setAttendanceId(Integer attendanceId) { this.attendanceId = attendanceId; }

    /** @return ID thành viên. */
    public Integer getMemberId() { return memberId; }
    /** @param memberId ID thành viên mới. */
    public void setMemberId(Integer memberId) { this.memberId = memberId; }

    /** @return Tên thành viên. */
    public String getMemberName() { return memberName; }
    /** @param memberName Tên thành viên mới. */
    public void setMemberName(String memberName) { this.memberName = memberName; }

    /** @return ID sự kiện. */
    public Integer getEventId() { return eventId; }
    /** @param eventId ID sự kiện mới. */
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    /** @return Tên sự kiện. */
    public String getEventName() { return eventName; }
    /** @param eventName Tên sự kiện mới. */
    public void setEventName(String eventName) { this.eventName = eventName; }

    /** @return Thời gian check-in. */
    public LocalDateTime getCheckInTime() { return checkInTime; }
    /** @param checkInTime Thời gian check-in mới. */
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    /** @return Thời gian check-out. */
    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    /** @param checkOutTime Thời gian check-out mới. */
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }

    /** @return Trạng thái điểm danh. */
    public String getStatus() { return status; }
    /** @param status Trạng thái mới. */
    public void setStatus(String status) { this.status = status; }

    /** @return Ghi chú. */
    public String getNote() { return note; }
    /** @param note Ghi chú mới. */
    public void setNote(String note) { this.note = note; }

    /**
     * Hiển thị thông tin điểm danh trong UI.
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() {
        return memberName + " - " + eventName;
    }
}
