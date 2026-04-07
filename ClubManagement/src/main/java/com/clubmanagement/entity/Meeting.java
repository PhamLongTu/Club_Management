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

/**
 * Entity: Meeting (Cuoc hop)
 * Map toi bang 'meetings'.
 */
@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id")
    private Integer meetingId;

    /** Tieu de cuoc hop */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /** Noi dung cuoc hop */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** Thoi gian bat dau */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /** Thoi gian ket thuc */
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /** Dia diem hop (offline) */
    @Column(name = "location", length = 255)
    private String location;

    /** Link Google Meet (neu hop online) */
    @Column(name = "meet_link", length = 500)
    private String meetLink;

    /** Ngay tao */
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    /** Nguoi chu tri (N-1 voi Member) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "host_id", nullable = false)
    private Member host;

    // CONSTRUCTORS
    /** Constructor mac dinh (bat buoc cho JPA). */
    public Meeting() {
        this.createdDate = LocalDateTime.now();
    }

    /**
     * Constructor khoi tao nhanh cuoc hop.
     */
    public Meeting(String title, String content, LocalDateTime startTime,
                   LocalDateTime endTime, String location, String meetLink, Member host) {
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.meetLink = meetLink;
        this.host = host;
        this.createdDate = LocalDateTime.now();
    }

    //  GETTERS & SETTERS 
    public Integer getMeetingId() { return meetingId; }
    public void setMeetingId(Integer meetingId) { this.meetingId = meetingId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getMeetLink() { return meetLink; }
    public void setMeetLink(String meetLink) { this.meetLink = meetLink; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public Member getHost() { return host; }
    public void setHost(Member host) { this.host = host; }

    @Override
    public String toString() { return title; }
}
