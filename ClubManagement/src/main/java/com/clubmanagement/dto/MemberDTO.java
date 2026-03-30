package com.clubmanagement.dto;

import java.time.LocalDate;

/**
 * DTO: MemberDTO (Data Transfer Object cho Thành viên)
 *
 * DTO được dùng để truyền dữ liệu giữa các tầng (Service -> Controller -> View)
 * mà KHÔNG expose trực tiếp Entity (tránh lazy loading, tuần hoàn JSON...).
 * Chỉ chứa những trường cần thiết cho UI.
 */
public class MemberDTO {

    private Integer memberId;
    private String  fullName;
    private String  studentId;
    private String  email;
    private String  phone;
    private String  gender;
    private LocalDate birthDate;
    private LocalDate joinDate;
    private String  status;
    private String  roleName;       // Lấy từ Role entity
    private Integer permissionLevel; // Cấp quyền để phân biệt Admin/Leader/Member

    // ============ CONSTRUCTORS ============
    public MemberDTO() {}

    /**
     * Constructor đầy đủ - khởi tạo từ entity hoặc query result.
     */
    public MemberDTO(Integer memberId, String fullName, String studentId,
                     String email, String phone, String gender,
                     LocalDate birthDate, LocalDate joinDate,
                     String status, String roleName, Integer permissionLevel) {
        this.memberId        = memberId;
        this.fullName        = fullName;
        this.studentId       = studentId;
        this.email           = email;
        this.phone           = phone;
        this.gender          = gender;
        this.birthDate       = birthDate;
        this.joinDate        = joinDate;
        this.status          = status;
        this.roleName        = roleName;
        this.permissionLevel = permissionLevel;
    }

    // ============ GETTERS & SETTERS ============
    public Integer getMemberId()              { return memberId; }
    public void setMemberId(Integer v)        { this.memberId = v; }

    public String getFullName()               { return fullName; }
    public void setFullName(String v)         { this.fullName = v; }

    public String getStudentId()              { return studentId; }
    public void setStudentId(String v)        { this.studentId = v; }

    public String getEmail()                  { return email; }
    public void setEmail(String v)            { this.email = v; }

    public String getPhone()                  { return phone; }
    public void setPhone(String v)            { this.phone = v; }

    public String getGender()                 { return gender; }
    public void setGender(String v)           { this.gender = v; }

    public LocalDate getBirthDate()           { return birthDate; }
    public void setBirthDate(LocalDate v)     { this.birthDate = v; }

    public LocalDate getJoinDate()            { return joinDate; }
    public void setJoinDate(LocalDate v)      { this.joinDate = v; }

    public String getStatus()                 { return status; }
    public void setStatus(String v)           { this.status = v; }

    public String getRoleName()               { return roleName; }
    public void setRoleName(String v)         { this.roleName = v; }

    public Integer getPermissionLevel()       { return permissionLevel; }
    public void setPermissionLevel(Integer v) { this.permissionLevel = v; }

    /** Kiểm tra có phải Admin không */
    public boolean isAdmin()  { return permissionLevel != null && permissionLevel >= 3; }

    /** Kiểm tra có phải Leader không */
    public boolean isLeader() { return permissionLevel != null && permissionLevel >= 2; }

    @Override
    public String toString() { return fullName + " [" + roleName + "]"; }
}
