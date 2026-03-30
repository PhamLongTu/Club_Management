package com.clubmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity: Announcement (Thông báo nội bộ CLB)
 * Map tới bảng 'announcements'.
 */
@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announcement_id")
    private Integer announcementId;

    /** Tiêu đề thông báo */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** Nội dung thông báo */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Ngày đăng */
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    /** Ghim lên đầu hay không */
    @Column(name = "is_pinned")
    private Boolean isPinned = false;

    /**
     * Đối tượng nhận thông báo: All / Leaders / Members
     */
    @Column(name = "target_audience", length = 20)
    private String targetAudience = "All";

    /** Người đăng thông báo */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    private Member author;

    // ============ CONSTRUCTORS ============
    public Announcement() {
        this.createdDate = LocalDateTime.now();
    }

    public Announcement(String title, String content, Boolean isPinned,
                        String targetAudience, Member author) {
        this.title          = title;
        this.content        = content;
        this.isPinned       = isPinned;
        this.targetAudience = targetAudience;
        this.author         = author;
        this.createdDate    = LocalDateTime.now();
    }

    // ============ GETTERS & SETTERS ============
    public Integer getAnnouncementId()       { return announcementId; }
    public void setAnnouncementId(Integer v) { this.announcementId = v; }

    public String getTitle()               { return title; }
    public void setTitle(String v)         { this.title = v; }

    public String getContent()             { return content; }
    public void setContent(String v)       { this.content = v; }

    public LocalDateTime getCreatedDate()        { return createdDate; }
    public void setCreatedDate(LocalDateTime v)  { this.createdDate = v; }

    public Boolean getIsPinned()           { return isPinned; }
    public void setIsPinned(Boolean v)     { this.isPinned = v; }

    public String getTargetAudience()      { return targetAudience; }
    public void setTargetAudience(String v){ this.targetAudience = v; }

    public Member getAuthor()              { return author; }
    public void setAuthor(Member v)        { this.author = v; }

    @Override
    public String toString() { return title; }
}
