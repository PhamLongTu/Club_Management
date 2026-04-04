package com.clubmanagement.dto;

import java.time.LocalDateTime;

/**
 * DTO: AnnouncementDTO (Data Transfer Object cho Thông báo)
 */
public class AnnouncementDTO {

    private Integer announcementId;
    private String title;
    private String content;
    private LocalDateTime createdDate;
    private Boolean isPinned;
    private String targetAudience;
    private String authorName; // Tên người đăng
    private Integer targetTeamId;
    private String targetTeamName;

    /** Constructor mặc định. */
    public AnnouncementDTO() {}

    /**
     * Constructor đầy đủ.
     */
    public AnnouncementDTO(Integer announcementId, String title, String content,
                           LocalDateTime createdDate, Boolean isPinned,
                           String targetAudience, String authorName,
                           Integer targetTeamId, String targetTeamName) {
        this.announcementId = announcementId;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
        this.isPinned = isPinned;
        this.targetAudience = targetAudience;
        this.authorName = authorName;
        this.targetTeamId = targetTeamId;
        this.targetTeamName = targetTeamName;
    }

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

    /** @return Ngày tạo. */
    public LocalDateTime getCreatedDate()        { return createdDate; }
    /** @param v Ngày tạo mới. */
    public void setCreatedDate(LocalDateTime v)  { this.createdDate = v; }

    /** @return Trạng thái ghim. */
    public Boolean getIsPinned()           { return isPinned; }
    /** @param v Trạng thái ghim mới. */
    public void setIsPinned(Boolean v)     { this.isPinned = v; }

    /** @return Đối tượng nhận thông báo. */
    public String getTargetAudience()      { return targetAudience; }
    /** @param v Đối tượng nhận mới. */
    public void setTargetAudience(String v){ this.targetAudience = v; }

    /** @return Tên người đăng. */
    public String getAuthorName()          { return authorName; }
    /** @param v Tên người đăng mới. */
    public void setAuthorName(String v)    { this.authorName = v; }

    /** @return ID ban/nhóm nhận. */
    public Integer getTargetTeamId()       { return targetTeamId; }
    /** @param v ID ban/nhóm nhận mới. */
    public void setTargetTeamId(Integer v) { this.targetTeamId = v; }

    /** @return Tên ban/nhóm nhận. */
    public String getTargetTeamName()      { return targetTeamName; }
    /** @param v Tên ban/nhóm nhận mới. */
    public void setTargetTeamName(String v){ this.targetTeamName = v; }

    /**
     * Hiển thị tiêu đề thông báo trong UI.
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() { return title; }
}
