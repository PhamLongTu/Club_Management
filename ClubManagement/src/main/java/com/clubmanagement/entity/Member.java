package com.clubmanagement.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entity: Member (Thành viên CLB)
 * Map tới bảng 'members'.
 * Đây là entity trung tâm của hệ thống.
 */
@Entity
@Table(name = "members")
public class Member {

    // ============ PRIMARY KEY ============
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Integer memberId;

    // ============ ATTRIBUTES ============
    /** Họ và tên đầy đủ */
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    /** Mã sinh viên (UNIQUE) */
    @Column(name = "student_id", nullable = false, unique = true, length = 20)
    private String studentId;

    /** Email (UNIQUE) */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /** Số điện thoại */
    @Column(name = "phone", length = 15)
    private String phone;

    /** Giới tính: Male / Female / Other */
    @Column(name = "gender", length = 10)
    private String gender = "Other";

    /** Ngày sinh */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /** Ngày gia nhập CLB */
    @Column(name = "join_date")
    private LocalDate joinDate;

    /** Trạng thái: Active / Inactive / Suspended */
    @Column(name = "status", length = 20)
    private String status = "Active";

    /** URL ảnh đại diện */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /** Mật khẩu đã mã hóa BCrypt */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** Điểm rèn luyện */
    @Column(name = "drl_points")
    private Integer drlPoints = 0;

    /** Điểm công tác xã hội */
    @Column(name = "ctxh_points")
    private Integer ctxhPoints = 0;

    /** Điểm đóng góp */
    @Column(name = "contribution_points")
    private Integer contributionPoints = 0;

    // ============ RELATIONSHIPS ============
    /** Quan hệ N-1 với Role: Mỗi thành viên có 1 vai trò */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /** Quan hệ N-N với Team thông qua bảng member_team */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "member_team",
        joinColumns = @JoinColumn(name = "member_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private List<Team> teams;

    /** Các sự kiện đã đăng ký (thông qua Participation) */
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Participation> participations;

    /** Các nhiệm vụ tham gia (N-N) */
    @ManyToMany(mappedBy = "assignees", fetch = FetchType.LAZY)
    private List<Task> assignedTasks;

    /** Các thông báo đã đăng */
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<Announcement> announcements;

    // ============ CONSTRUCTORS ============
    /**
     * Constructor mặc định (bắt buộc cho JPA).
     */
    public Member() {}

    /**
     * Constructor khởi tạo nhanh khi tạo mới thành viên.
     *
     * @param fullName     Họ tên
     * @param studentId    Mã sinh viên
     * @param email        Email
     * @param phone        Số điện thoại
     * @param gender       Giới tính
     * @param passwordHash Mật khẩu đã hash
     * @param role         Vai trò
     */
    public Member(String fullName, String studentId, String email,
                  String phone, String gender, String passwordHash, Role role) {
        this.fullName     = fullName;
        this.studentId    = studentId;
        this.email        = email;
        this.phone        = phone;
        this.gender       = gender;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.joinDate     = LocalDate.now();
        this.status       = "Active";
    }

    // ============ GETTERS & SETTERS ============
    /** @return ID thành viên. */
    public Integer getMemberId()          { return memberId; }
    /** @param v ID thành viên mới. */
    public void setMemberId(Integer v)    { this.memberId = v; }

    /** @return Họ tên đầy đủ. */
    public String getFullName()           { return fullName; }
    /** @param v Họ tên mới. */
    public void setFullName(String v)     { this.fullName = v; }

    /** @return Mã sinh viên. */
    public String getStudentId()          { return studentId; }
    /** @param v Mã sinh viên mới. */
    public void setStudentId(String v)    { this.studentId = v; }

    /** @return Email. */
    public String getEmail()              { return email; }
    /** @param v Email mới. */
    public void setEmail(String v)        { this.email = v; }

    /** @return Số điện thoại. */
    public String getPhone()              { return phone; }
    /** @param v Số điện thoại mới. */
    public void setPhone(String v)        { this.phone = v; }

    /** @return Giới tính. */
    public String getGender()             { return gender; }
    /** @param v Giới tính mới. */
    public void setGender(String v)       { this.gender = v; }

    /** @return Ngày sinh. */
    public LocalDate getBirthDate()       { return birthDate; }
    /** @param v Ngày sinh mới. */
    public void setBirthDate(LocalDate v) { this.birthDate = v; }

    /** @return Ngày gia nhập. */
    public LocalDate getJoinDate()        { return joinDate; }
    /** @param v Ngày gia nhập mới. */
    public void setJoinDate(LocalDate v)  { this.joinDate = v; }

    /** @return Trạng thái tài khoản. */
    public String getStatus()             { return status; }
    /** @param v Trạng thái mới. */
    public void setStatus(String v)       { this.status = v; }

    /** @return URL ảnh đại diện. */
    public String getAvatarUrl()          { return avatarUrl; }
    /** @param v URL ảnh đại diện mới. */
    public void setAvatarUrl(String v)    { this.avatarUrl = v; }

    /** @return Mật khẩu đã hash. */
    public String getPasswordHash()       { return passwordHash; }
    /** @param v Mật khẩu đã hash mới. */
    public void setPasswordHash(String v) { this.passwordHash = v; }

    /** @return Điểm rèn luyện. */
    public Integer getDrlPoints()         { return drlPoints; }
    /** @param v Điểm rèn luyện mới. */
    public void setDrlPoints(Integer v)   { this.drlPoints = v; }

    /** @return Điểm công tác xã hội. */
    public Integer getCtxhPoints()        { return ctxhPoints; }
    /** @param v Điểm công tác xã hội mới. */
    public void setCtxhPoints(Integer v)  { this.ctxhPoints = v; }

    /** @return Điểm đóng góp. */
    public Integer getContributionPoints()       { return contributionPoints; }
    /** @param v Điểm đóng góp mới. */
    public void setContributionPoints(Integer v) { this.contributionPoints = v; }

    /** @return Vai trò hiện tại. */
    public Role getRole()                 { return role; }
    /** @param v Vai trò mới. */
    public void setRole(Role v)           { this.role = v; }

    /** @return Danh sách ban/nhóm. */
    public List<Team> getTeams()          { return teams; }
    /** @param v Danh sách ban/nhóm mới. */
    public void setTeams(List<Team> v)    { this.teams = v; }

    /** @return Danh sách tham gia sự kiện. */
    public List<Participation> getParticipations()      { return participations; }
    /** @param v Danh sách tham gia sự kiện mới. */
    public void setParticipations(List<Participation> v){ this.participations = v; }

    /** @return Danh sách nhiệm vụ đã nhận. */
    public List<Task> getAssignedTasks()        { return assignedTasks; }
    /** @param v Danh sách nhiệm vụ đã nhận mới. */
    public void setAssignedTasks(List<Task> v)  { this.assignedTasks = v; }

    /** @return Danh sách thông báo đã đăng. */
    public List<Announcement> getAnnouncements()        { return announcements; }
    /** @param v Danh sách thông báo đã đăng mới. */
    public void setAnnouncements(List<Announcement> v)  { this.announcements = v; }

    /**
     * Hiển thị tên thành viên trong UI (dùng cho ComboBox/JList).
     *
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() { return fullName + " (" + studentId + ")"; }
}
