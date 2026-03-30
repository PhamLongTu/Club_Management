package com.clubmanagement.entity;

import jakarta.persistence.*;
import java.util.List;

/**
 * Entity: Role (Vai trò)
 * Map tới bảng 'roles' trong database.
 * Mỗi thành viên có một vai trò (Admin, Leader, Member).
 */
@Entity
@Table(name = "roles")
public class Role {

    // ============ PRIMARY KEY ============
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    // ============ ATTRIBUTES ============
    /** Tên vai trò (UNIQUE, NOT NULL) */
    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    /** Mô tả vai trò */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Cấp độ quyền hạn:
     * 1 = Member (thành viên thường)
     * 2 = Leader (trưởng/phó)
     * 3 = Admin  (quản trị viên)
     */
    @Column(name = "permission_level")
    private Integer permissionLevel = 1;

    // ============ RELATIONSHIPS ============
    /** Một vai trò có thể có nhiều thành viên (1-N) */
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private List<Member> members;

    // ============ CONSTRUCTORS ============
    public Role() {}

    public Role(String roleName, String description, Integer permissionLevel) {
        this.roleName = roleName;
        this.description = description;
        this.permissionLevel = permissionLevel;
    }

    // ============ GETTERS & SETTERS ============
    public Integer getRoleId()       { return roleId; }
    public void setRoleId(Integer v) { this.roleId = v; }

    public String getRoleName()        { return roleName; }
    public void setRoleName(String v)  { this.roleName = v; }

    public String getDescription()       { return description; }
    public void setDescription(String v) { this.description = v; }

    public Integer getPermissionLevel()        { return permissionLevel; }
    public void setPermissionLevel(Integer v)  { this.permissionLevel = v; }

    public List<Member> getMembers()         { return members; }
    public void setMembers(List<Member> v)   { this.members = v; }

    @Override
    public String toString() { return roleName; }
}
