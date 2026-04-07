package com.clubmanagement.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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

    //  CONSTRUCTORS 
    /**
     * Constructor mặc định (bắt buộc cho JPA).
     */
    public Participation() {
        this.registrationDate = LocalDateTime.now();
    }

    /**
     * Constructor khởi tạo nhanh đăng ký tham gia.
     *
     * @param member     Thành viên đăng ký
     * @param event      Sự kiện đăng ký
     */
    public Participation(Member member, Event event) {
        this.member           = member;
        this.event            = event;
        this.registrationDate = LocalDateTime.now();
    }

    //  GETTERS & SETTERS 
    /** @return ID đăng ký. */
    public Integer getParticipationId()        { return participationId; }
    /** @param v ID đăng ký mới. */
    public void setParticipationId(Integer v)  { this.participationId = v; }

    /** @return Thành viên đăng ký. */
    public Member getMember()          { return member; }
    /** @param v Thành viên mới. */
    public void setMember(Member v)    { this.member = v; }

    /** @return Sự kiện đăng ký. */
    public Event getEvent()            { return event; }
    /** @param v Sự kiện mới. */
    public void setEvent(Event v)      { this.event = v; }

    /** @return Ngày giờ đăng ký. */
    public LocalDateTime getRegistrationDate()        { return registrationDate; }
    /** @param v Ngày giờ đăng ký mới. */
    public void setRegistrationDate(LocalDateTime v)  { this.registrationDate = v; }

}
