package com.clubmanagement.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entity: Role (Vai trò)
 * Map tới bảng 'roles' trong database.
 * Mỗi thành viên có một vai trò (Admin, Leader, Member).
 */
@Entity
@Table(name = "roles")
public class Role {

    //  PRIMARY KEY 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    //  ATTRIBUTES 
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

    //  RELATIONSHIPS 
    /** Một vai trò có thể có nhiều thành viên (1-N) */
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private List<Member> members;

    //  CONSTRUCTORS 
    /**
     * Constructor mặc định (bắt buộc cho JPA).
     */
    public Role() {}

    /**
     * Constructor khởi tạo nhanh vai trò.
     *
     * @param roleName        Tên vai trò
     * @param description     Mô tả vai trò
     * @param permissionLevel Cấp quyền
     */
    public Role(String roleName, String description, Integer permissionLevel) {
        this.roleName = roleName;
        this.description = description;
        this.permissionLevel = permissionLevel;
    }

    //  GETTERS & SETTERS 
    /** @return ID vai trò. */
    public Integer getRoleId()       { return roleId; }
    /** @param v ID vai trò mới. */
    public void setRoleId(Integer v) { this.roleId = v; }

    /** @return Tên vai trò. */
    public String getRoleName()        { return roleName; }
    /** @param v Tên vai trò mới. */
    public void setRoleName(String v)  { this.roleName = v; }

    /** @return Mô tả vai trò. */
    public String getDescription()       { return description; }
    /** @param v Mô tả mới. */
    public void setDescription(String v) { this.description = v; }

    /** @return Cấp quyền. */
    public Integer getPermissionLevel()        { return permissionLevel; }
    /** @param v Cấp quyền mới. */
    public void setPermissionLevel(Integer v)  { this.permissionLevel = v; }

    /** @return Danh sách thành viên thuộc vai trò. */
    public List<Member> getMembers()         { return members; }
    /** @param v Danh sách thành viên mới. */
    public void setMembers(List<Member> v)   { this.members = v; }

    /**
     * Hiển thị tên vai trò trong UI.
     *
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() { return roleName; }
}
