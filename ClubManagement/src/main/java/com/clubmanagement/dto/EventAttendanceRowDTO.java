package com.clubmanagement.dto;

/**
 * EventAttendanceRowDTO - Row data for event attendance table.
 */
public class EventAttendanceRowDTO {

    private Integer memberId;
    private Integer attendanceId;
    private String studentId;
    private String fullName;
    private String email;
    private boolean attended;

    public EventAttendanceRowDTO() {}

    public EventAttendanceRowDTO(Integer memberId, Integer attendanceId, String studentId,
                                 String fullName, String email, boolean attended) {
        this.memberId = memberId;
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.fullName = fullName;
        this.email = email;
        this.attended = attended;
    }

    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }

    public Integer getAttendanceId() { return attendanceId; }
    public void setAttendanceId(Integer attendanceId) { this.attendanceId = attendanceId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isAttended() { return attended; }
    public void setAttended(boolean attended) { this.attended = attended; }
}
