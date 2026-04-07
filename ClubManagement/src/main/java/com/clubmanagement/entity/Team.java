package com.clubmanagement.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity: Team (Nhóm/Ban trong CLB)
 * Map tới bảng 'teams'.
 */
@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Integer teamId;

    /** Tên nhóm/ban (UNIQUE) */
    @Column(name = "team_name", nullable = false, unique = true, length = 100)
    private String teamName;

    /** Mô tả chức năng của nhóm */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Ngày thành lập nhóm */
    @Column(name = "created_date")
    private LocalDate createdDate;

    /** Trưởng nhóm (quan hệ N-1 với Member) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leader_id")
    private Member leader;

    /** Danh sách thành viên (N-N với Member) */
    @ManyToMany(mappedBy = "teams", fetch = FetchType.LAZY)
    private List<Member> members;

    // ============ CONSTRUCTORS ============
    /**
     * Constructor mặc định (bắt buộc cho JPA).
     */
    public Team() {}

    /**
     * Constructor khởi tạo nhanh nhóm/ban.
     *
     * @param teamName    Tên nhóm/ban
     * @param description Mô tả
     * @param leader      Trưởng nhóm
     */
    public Team(String teamName, String description, Member leader) {
        this.teamName    = teamName;
        this.description = description;
        this.leader      = leader;
        this.createdDate = LocalDate.now();
    }

    //  GETTERS & SETTERS 
    /** @return ID nhóm/ban. */
    public Integer getTeamId()         { return teamId; }
    /** @param v ID nhóm/ban mới. */
    public void setTeamId(Integer v)   { this.teamId = v; }

    /** @return Tên nhóm/ban. */
    public String getTeamName()        { return teamName; }
    /** @param v Tên nhóm/ban mới. */
    public void setTeamName(String v)  { this.teamName = v; }

    /** @return Mô tả nhóm/ban. */
    public String getDescription()       { return description; }
    /** @param v Mô tả mới. */
    public void setDescription(String v) { this.description = v; }

    /** @return Ngày thành lập. */
    public LocalDate getCreatedDate()        { return createdDate; }
    /** @param v Ngày thành lập mới. */
    public void setCreatedDate(LocalDate v)  { this.createdDate = v; }

    /** @return Trưởng nhóm. */
    public Member getLeader()          { return leader; }
    /** @param v Trưởng nhóm mới. */
    public void setLeader(Member v)    { this.leader = v; }

    /** @return Danh sách thành viên. */
    public List<Member> getMembers()         { return members; }
    /** @param v Danh sách thành viên mới. */
    public void setMembers(List<Member> v)   { this.members = v; }

    /**
     * Hiển thị tên nhóm/ban trong UI.
     *
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() { return teamName; }
}
