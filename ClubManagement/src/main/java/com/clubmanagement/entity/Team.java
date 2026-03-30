package com.clubmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

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
    public Team() {}

    public Team(String teamName, String description, Member leader) {
        this.teamName    = teamName;
        this.description = description;
        this.leader      = leader;
        this.createdDate = LocalDate.now();
    }

    // ============ GETTERS & SETTERS ============
    public Integer getTeamId()         { return teamId; }
    public void setTeamId(Integer v)   { this.teamId = v; }

    public String getTeamName()        { return teamName; }
    public void setTeamName(String v)  { this.teamName = v; }

    public String getDescription()       { return description; }
    public void setDescription(String v) { this.description = v; }

    public LocalDate getCreatedDate()        { return createdDate; }
    public void setCreatedDate(LocalDate v)  { this.createdDate = v; }

    public Member getLeader()          { return leader; }
    public void setLeader(Member v)    { this.leader = v; }

    public List<Member> getMembers()         { return members; }
    public void setMembers(List<Member> v)   { this.members = v; }

    @Override
    public String toString() { return teamName; }
}
