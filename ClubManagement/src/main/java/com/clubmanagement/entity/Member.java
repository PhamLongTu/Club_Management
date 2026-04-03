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
    public Member() {}

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
    public Integer getMemberId()          { return memberId; }
    public void setMemberId(Integer v)    { this.memberId = v; }

    public String getFullName()           { return fullName; }
    public void setFullName(String v)     { this.fullName = v; }

    public String getStudentId()          { return studentId; }
    public void setStudentId(String v)    { this.studentId = v; }

    public String getEmail()              { return email; }
    public void setEmail(String v)        { this.email = v; }

    public String getPhone()              { return phone; }
    public void setPhone(String v)        { this.phone = v; }

    public String getGender()             { return gender; }
    public void setGender(String v)       { this.gender = v; }

    public LocalDate getBirthDate()       { return birthDate; }
    public void setBirthDate(LocalDate v) { this.birthDate = v; }

    public LocalDate getJoinDate()        { return joinDate; }
    public void setJoinDate(LocalDate v)  { this.joinDate = v; }

    public String getStatus()             { return status; }
    public void setStatus(String v)       { this.status = v; }

    public String getAvatarUrl()          { return avatarUrl; }
    public void setAvatarUrl(String v)    { this.avatarUrl = v; }

    public String getPasswordHash()       { return passwordHash; }
    public void setPasswordHash(String v) { this.passwordHash = v; }

    public Role getRole()                 { return role; }
    public void setRole(Role v)           { this.role = v; }

    public List<Team> getTeams()          { return teams; }
    public void setTeams(List<Team> v)    { this.teams = v; }

    public List<Participation> getParticipations()      { return participations; }
    public void setParticipations(List<Participation> v){ this.participations = v; }

    public List<Task> getAssignedTasks()        { return assignedTasks; }
    public void setAssignedTasks(List<Task> v)  { this.assignedTasks = v; }

    public List<Announcement> getAnnouncements()        { return announcements; }
    public void setAnnouncements(List<Announcement> v)  { this.announcements = v; }

    @Override
    public String toString() { return fullName + " (" + studentId + ")"; }
}
