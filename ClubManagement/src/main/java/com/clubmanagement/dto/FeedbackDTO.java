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

    /** Constructor mặc định. */
    public FeedbackDTO() {}

    /**
     * Constructor đầy đủ.
     */
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

    /** @return ID phản hồi. */
    public Integer getFeedbackId() { return feedbackId; }
    /** @param feedbackId ID phản hồi mới. */
    public void setFeedbackId(Integer feedbackId) { this.feedbackId = feedbackId; }

    /** @return Nội dung phản hồi. */
    public String getContent() { return content; }
    /** @param content Nội dung mới. */
    public void setContent(String content) { this.content = content; }

    /** @return Đánh giá. */
    public Integer getRating() { return rating; }
    /** @param rating Đánh giá mới. */
    public void setRating(Integer rating) { this.rating = rating; }

    /** @return Loại phản hồi. */
    public String getFeedbackType() { return feedbackType; }
    /** @param feedbackType Loại phản hồi mới. */
    public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }

    /** @return Ngày tạo phản hồi. */
    public LocalDateTime getCreatedDate() { return createdDate; }
    /** @param createdDate Ngày tạo mới. */
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    /** @return ID thành viên. */
    public Integer getMemberId() { return memberId; }
    /** @param memberId ID thành viên mới. */
    public void setMemberId(Integer memberId) { this.memberId = memberId; }

    /** @return Tên thành viên. */
    public String getMemberName() { return memberName; }
    /** @param memberName Tên thành viên mới. */
    public void setMemberName(String memberName) { this.memberName = memberName; }

    /** @return ID sự kiện. */
    public Integer getEventId() { return eventId; }
    /** @param eventId ID sự kiện mới. */
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    /** @return Tên sự kiện. */
    public String getEventName() { return eventName; }
    /** @param eventName Tên sự kiện mới. */
    public void setEventName(String eventName) { this.eventName = eventName; }

    /** @return ID dự án. */
    public Integer getProjectId() { return projectId; }
    /** @param projectId ID dự án mới. */
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    /** @return Tên dự án. */
    public String getProjectName() { return projectName; }
    /** @param projectName Tên dự án mới. */
    public void setProjectName(String projectName) { this.projectName = projectName; }
}
