package com.clubmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity: Feedback (Phản hồi)
 * Map tới bảng 'feedbacks'.
 * Thành viên có thể phản hồi về sự kiện, dự án, hoặc CLB nói chung.
 */
@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId;

    /** Nội dung phản hồi */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Đánh giá từ 1 đến 5 sao */
    @Column(name = "rating", columnDefinition = "TINYINT")
    private Integer rating;

    /** Ngày gửi phản hồi */
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    /**
     * Loại phản hồi: Event / Project / Club / Other
     */
    @Column(name = "feedback_type", length = 20)
    private String feedbackType = "Event";

    /** Thành viên gửi phản hồi */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

    /** Sự kiện được phản hồi (nullable) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id")
    private Event event;

    /** Dự án được phản hồi (nullable) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private Project project;

    // ============ CONSTRUCTORS ============
    public Feedback() {
        this.createdDate = LocalDateTime.now();
    }

    public Feedback(String content, Integer rating, String feedbackType, Member member) {
        this.content      = content;
        this.rating       = rating;
        this.feedbackType = feedbackType;
        this.member       = member;
        this.createdDate  = LocalDateTime.now();
    }

    // ============ GETTERS & SETTERS ============
    public Integer getFeedbackId()          { return feedbackId; }
    public void setFeedbackId(Integer v)    { this.feedbackId = v; }

    public String getContent()              { return content; }
    public void setContent(String v)        { this.content = v; }

    public Integer getRating()              { return rating; }
    public void setRating(Integer v)        { this.rating = v; }

    public LocalDateTime getCreatedDate()        { return createdDate; }
    public void setCreatedDate(LocalDateTime v)  { this.createdDate = v; }

    public String getFeedbackType()         { return feedbackType; }
    public void setFeedbackType(String v)   { this.feedbackType = v; }

    public Member getMember()               { return member; }
    public void setMember(Member v)         { this.member = v; }

    public Event getEvent()                 { return event; }
    public void setEvent(Event v)           { this.event = v; }

    public Project getProject()             { return project; }
    public void setProject(Project v)       { this.project = v; }
}
