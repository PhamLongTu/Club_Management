package com.clubmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity: Participation (Đăng ký tham gia sự kiện)
 * Bảng liên kết N-N giữa Member và Event.
 * Map tới bảng 'participations'.
 */
@Entity
@Table(name = "participations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "event_id"}))
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participation_id")
    private Integer participationId;

    /** Thành viên đăng ký */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** Sự kiện được đăng ký */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /** Ngày giờ đăng ký */
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    /**
     * Trạng thái tham gia:
     * Registered / Attended / Absent / Cancelled
     */
    @Column(name = "status", length = 20)
    private String status = "Registered";

    /** Vai trò trong sự kiện (ví dụ: MC, Tình nguyện viên, Ban tổ chức) */
    @Column(name = "role_in_event", length = 100)
    private String roleInEvent;

    // ============ CONSTRUCTORS ============
    public Participation() {
        this.registrationDate = LocalDateTime.now();
    }

    public Participation(Member member, Event event, String roleInEvent) {
        this.member           = member;
        this.event            = event;
        this.roleInEvent      = roleInEvent;
        this.registrationDate = LocalDateTime.now();
        this.status           = "Registered";
    }

    // ============ GETTERS & SETTERS ============
    public Integer getParticipationId()        { return participationId; }
    public void setParticipationId(Integer v)  { this.participationId = v; }

    public Member getMember()          { return member; }
    public void setMember(Member v)    { this.member = v; }

    public Event getEvent()            { return event; }
    public void setEvent(Event v)      { this.event = v; }

    public LocalDateTime getRegistrationDate()        { return registrationDate; }
    public void setRegistrationDate(LocalDateTime v)  { this.registrationDate = v; }

    public String getStatus()                  { return status; }
    public void setStatus(String v)            { this.status = v; }

    public String getRoleInEvent()             { return roleInEvent; }
    public void setRoleInEvent(String v)       { this.roleInEvent = v; }
}
