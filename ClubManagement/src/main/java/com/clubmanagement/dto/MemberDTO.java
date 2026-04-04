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
    private String  avatarUrl;
    private String  roleName;       // Lấy từ Role entity
    private Integer permissionLevel; // Cấp quyền để phân biệt Admin/Leader/Member
    private String  teamNames;      // Danh sách ban (dạng chuỗi)

    // ============ CONSTRUCTORS ============
    /** Constructor mặc định. */
    public MemberDTO() {}

    /**
     * Constructor đầy đủ - khởi tạo từ entity hoặc query result.
     */
    public MemberDTO(Integer memberId, String fullName, String studentId,
                     String email, String phone, String gender,
                     LocalDate birthDate, LocalDate joinDate,
                     String status, String avatarUrl,
                     String roleName, Integer permissionLevel,
                     String teamNames) {
        this.memberId        = memberId;
        this.fullName        = fullName;
        this.studentId       = studentId;
        this.email           = email;
        this.phone           = phone;
        this.gender          = gender;
        this.birthDate       = birthDate;
        this.joinDate        = joinDate;
        this.status          = status;
        this.avatarUrl       = avatarUrl;
        this.roleName        = roleName;
        this.permissionLevel = permissionLevel;
        this.teamNames       = teamNames;
    }

    // ============ GETTERS & SETTERS ============
    /** @return ID thành viên. */
    public Integer getMemberId()              { return memberId; }
    /** @param v ID thành viên mới. */
    public void setMemberId(Integer v)        { this.memberId = v; }

    /** @return Họ tên đầy đủ. */
    public String getFullName()               { return fullName; }
    /** @param v Họ tên mới. */
    public void setFullName(String v)         { this.fullName = v; }

    /** @return Mã sinh viên. */
    public String getStudentId()              { return studentId; }
    /** @param v Mã sinh viên mới. */
    public void setStudentId(String v)        { this.studentId = v; }

    /** @return Email. */
    public String getEmail()                  { return email; }
    /** @param v Email mới. */
    public void setEmail(String v)            { this.email = v; }

    /** @return Số điện thoại. */
    public String getPhone()                  { return phone; }
    /** @param v Số điện thoại mới. */
    public void setPhone(String v)            { this.phone = v; }

    /** @return Giới tính. */
    public String getGender()                 { return gender; }
    /** @param v Giới tính mới. */
    public void setGender(String v)           { this.gender = v; }

    /** @return Ngày sinh. */
    public LocalDate getBirthDate()           { return birthDate; }
    /** @param v Ngày sinh mới. */
    public void setBirthDate(LocalDate v)     { this.birthDate = v; }

    /** @return Ngày gia nhập. */
    public LocalDate getJoinDate()            { return joinDate; }
    /** @param v Ngày gia nhập mới. */
    public void setJoinDate(LocalDate v)      { this.joinDate = v; }

    /** @return Trạng thái tài khoản. */
    public String getStatus()                 { return status; }
    /** @param v Trạng thái mới. */
    public void setStatus(String v)           { this.status = v; }

    /** @return URL ảnh đại diện. */
    public String getAvatarUrl()              { return avatarUrl; }
    /** @param v URL ảnh đại diện mới. */
    public void setAvatarUrl(String v)        { this.avatarUrl = v; }

    /** @return Tên vai trò. */
    public String getRoleName()               { return roleName; }
    /** @param v Tên vai trò mới. */
    public void setRoleName(String v)         { this.roleName = v; }

    /** @return Cấp quyền. */
    public Integer getPermissionLevel()       { return permissionLevel; }
    /** @param v Cấp quyền mới. */
    public void setPermissionLevel(Integer v) { this.permissionLevel = v; }

    /** @return Danh sách ban/nhóm (chuỗi). */
    public String getTeamNames()              { return teamNames; }
    /** @param v Danh sách ban/nhóm mới. */
    public void setTeamNames(String v)        { this.teamNames = v; }

    /** Kiểm tra có phải Admin không */
    public boolean isAdmin()  { return permissionLevel != null && permissionLevel >= 3; }

    /** Kiểm tra có phải Leader không */
    public boolean isLeader() { return permissionLevel != null && permissionLevel >= 2; }

    /**
     * Hiển thị tên thành viên trong UI.
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() { return fullName + " [" + roleName + "]"; }
}
