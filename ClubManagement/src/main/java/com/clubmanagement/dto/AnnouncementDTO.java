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

    public AnnouncementDTO() {}

    public AnnouncementDTO(Integer announcementId, String title, String content,
                           LocalDateTime createdDate, Boolean isPinned,
                           String targetAudience, String authorName) {
        this.announcementId = announcementId;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
        this.isPinned = isPinned;
        this.targetAudience = targetAudience;
        this.authorName = authorName;
    }

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

    public String getAuthorName()          { return authorName; }
    public void setAuthorName(String v)    { this.authorName = v; }

    @Override
    public String toString() { return title; }
}
