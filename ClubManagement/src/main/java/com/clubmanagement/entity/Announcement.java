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

    /** Thông báo theo ban (nullable) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_team_id")
    private Team targetTeam;

    // ============ CONSTRUCTORS ============
    /**
     * Constructor mặc định (bắt buộc cho JPA).
     */
    public Announcement() {
        this.createdDate = LocalDateTime.now();
    }

    /**
     * Constructor khởi tạo nhanh thông báo.
     *
     * @param title          Tiêu đề
     * @param content        Nội dung
     * @param isPinned       Trạng thái ghim
     * @param targetAudience Đối tượng nhận
     * @param author         Người đăng
     * @param targetTeam     Ban/nhóm nhận (nullable)
     */
    public Announcement(String title, String content, Boolean isPinned,
                        String targetAudience, Member author, Team targetTeam) {
        this.title          = title;
        this.content        = content;
        this.isPinned       = isPinned;
        this.targetAudience = targetAudience;
        this.author         = author;
        this.targetTeam     = targetTeam;
        this.createdDate    = LocalDateTime.now();
    }

    // ============ GETTERS & SETTERS ============
    /** @return ID thông báo. */
    public Integer getAnnouncementId()       { return announcementId; }
    /** @param v ID thông báo mới. */
    public void setAnnouncementId(Integer v) { this.announcementId = v; }

    /** @return Tiêu đề thông báo. */
    public String getTitle()               { return title; }
    /** @param v Tiêu đề mới. */
    public void setTitle(String v)         { this.title = v; }

    /** @return Nội dung thông báo. */
    public String getContent()             { return content; }
    /** @param v Nội dung mới. */
    public void setContent(String v)       { this.content = v; }

    /** @return Ngày đăng. */
    public LocalDateTime getCreatedDate()        { return createdDate; }
    /** @param v Ngày đăng mới. */
    public void setCreatedDate(LocalDateTime v)  { this.createdDate = v; }

    /** @return Trạng thái ghim. */
    public Boolean getIsPinned()           { return isPinned; }
    /** @param v Trạng thái ghim mới. */
    public void setIsPinned(Boolean v)     { this.isPinned = v; }

    /** @return Đối tượng nhận thông báo. */
    public String getTargetAudience()      { return targetAudience; }
    /** @param v Đối tượng nhận mới. */
    public void setTargetAudience(String v){ this.targetAudience = v; }

    /** @return Người đăng thông báo. */
    public Member getAuthor()              { return author; }
    /** @param v Người đăng mới. */
    public void setAuthor(Member v)        { this.author = v; }

    /** @return Ban/nhóm nhận thông báo. */
    public Team getTargetTeam()            { return targetTeam; }
    /** @param v Ban/nhóm nhận mới. */
    public void setTargetTeam(Team v)      { this.targetTeam = v; }

    /**
     * Hiển thị tiêu đề thông báo trong UI.
     *
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() { return title; }
}
