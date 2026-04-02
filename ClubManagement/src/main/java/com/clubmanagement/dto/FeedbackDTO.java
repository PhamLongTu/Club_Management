package com.clubmanagement.dto;

import java.time.LocalDateTime;

/**
 * FeedbackDTO - Data Transfer Object của Phản hồi
 */
public class FeedbackDTO {
    
    private Integer feedbackId;
    private String content;
    private Integer rating;
    private String feedbackType;
    private LocalDateTime createdDate;

    private Integer memberId;
    private String memberName;
    
    private Integer eventId;
    private String eventName;
    
    private Integer projectId;
    private String projectName;

    public FeedbackDTO() {}

    public FeedbackDTO(Integer feedbackId, String content, Integer rating, String feedbackType, 
                       LocalDateTime createdDate, Integer memberId, String memberName, 
                       Integer eventId, String eventName, Integer projectId, String projectName) {
        this.feedbackId = feedbackId;
        this.content = content;
        this.rating = rating;
        this.feedbackType = feedbackType;
        this.createdDate = createdDate;
        this.memberId = memberId;
        this.memberName = memberName;
        this.eventId = eventId;
        this.eventName = eventName;
        this.projectId = projectId;
        this.projectName = projectName;
    }

    public Integer getFeedbackId() { return feedbackId; }
    public void setFeedbackId(Integer feedbackId) { this.feedbackId = feedbackId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getFeedbackType() { return feedbackType; }
    public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
}
