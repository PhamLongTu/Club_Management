package com.clubmanagement.dto;

/**
 * AttendanceDTO - Data Transfer Object của Điểm danh
 */
public class AttendanceDTO {
    
    private Integer attendanceId;
    private Integer memberId;
    private Integer eventId;

    /** Constructor mặc định. */
    public AttendanceDTO() {}

    /**
     * Constructor đầy đủ.
     */
    public AttendanceDTO(Integer attendanceId, Integer memberId, Integer eventId) {
        this.attendanceId = attendanceId;
        this.memberId = memberId;
        this.eventId = eventId;
    }

    /** @return ID điểm danh. */
    public Integer getAttendanceId() { return attendanceId; }
    /** @param attendanceId ID điểm danh mới. */
    public void setAttendanceId(Integer attendanceId) { this.attendanceId = attendanceId; }

    /** @return ID thành viên. */
    public Integer getMemberId() { return memberId; }
    /** @param memberId ID thành viên mới. */
    public void setMemberId(Integer memberId) { this.memberId = memberId; }

    /** @return ID sự kiện. */
    public Integer getEventId() { return eventId; }
    /** @param eventId ID sự kiện mới. */
    public void setEventId(Integer eventId) { this.eventId = eventId; }
}
