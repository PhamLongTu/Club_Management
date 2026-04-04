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
    /**
     * Constructor mặc định (bắt buộc cho JPA).
     */
    public Feedback() {
        this.createdDate = LocalDateTime.now();
    }

    /**
     * Constructor khởi tạo nhanh phản hồi.
     *
     * @param content      Nội dung phản hồi
     * @param rating       Đánh giá
     * @param feedbackType Loại phản hồi
     * @param member       Thành viên gửi
     */
    public Feedback(String content, Integer rating, String feedbackType, Member member) {
        this.content      = content;
        this.rating       = rating;
        this.feedbackType = feedbackType;
        this.member       = member;
        this.createdDate  = LocalDateTime.now();
    }

    // ============ GETTERS & SETTERS ============
    /** @return ID phản hồi. */
    public Integer getFeedbackId()          { return feedbackId; }
    /** @param v ID phản hồi mới. */
    public void setFeedbackId(Integer v)    { this.feedbackId = v; }

    /** @return Nội dung phản hồi. */
    public String getContent()              { return content; }
    /** @param v Nội dung mới. */
    public void setContent(String v)        { this.content = v; }

    /** @return Đánh giá. */
    public Integer getRating()              { return rating; }
    /** @param v Đánh giá mới. */
    public void setRating(Integer v)        { this.rating = v; }

    /** @return Ngày gửi phản hồi. */
    public LocalDateTime getCreatedDate()        { return createdDate; }
    /** @param v Ngày gửi mới. */
    public void setCreatedDate(LocalDateTime v)  { this.createdDate = v; }

    /** @return Loại phản hồi. */
    public String getFeedbackType()         { return feedbackType; }
    /** @param v Loại phản hồi mới. */
    public void setFeedbackType(String v)   { this.feedbackType = v; }

    /** @return Thành viên gửi phản hồi. */
    public Member getMember()               { return member; }
    /** @param v Thành viên gửi mới. */
    public void setMember(Member v)         { this.member = v; }

    /** @return Sự kiện được phản hồi. */
    public Event getEvent()                 { return event; }
    /** @param v Sự kiện được phản hồi mới. */
    public void setEvent(Event v)           { this.event = v; }

    /** @return Dự án được phản hồi. */
    public Project getProject()             { return project; }
    /** @param v Dự án được phản hồi mới. */
    public void setProject(Project v)       { this.project = v; }
}
