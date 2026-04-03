package com.clubmanagement.dto;

import java.time.LocalDateTime;

/**
 * TaskDTO - Data Transfer Object của Nhiệm vụ
 */
public class TaskDTO {

    private Integer taskId;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private String priority;
    private String status;
    private LocalDateTime createdDate;
    private String visibility;
    private Integer maxAssignees;

    // Tên người dùng liên quan
    private String assigneeName;
    private String assignerName;
    private String eventName;

    // ID liên quan (để update/delete nếu cần)
    private Integer assigneeId;
    private Integer assignerId;
    private Integer eventId;

    private java.util.List<Integer> assigneeIds;
    private java.util.List<String> assigneeNames;

    public TaskDTO() {}

    public TaskDTO(Integer taskId, String title, String description, LocalDateTime deadline,
                   String priority, String status, LocalDateTime createdDate,
                   String visibility, Integer maxAssignees,
                   String assigneeName, String assignerName, String eventName,
                   Integer assigneeId, Integer assignerId, Integer eventId,
                   java.util.List<Integer> assigneeIds, java.util.List<String> assigneeNames) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
        this.createdDate = createdDate;
        this.visibility = visibility;
        this.maxAssignees = maxAssignees;
        this.assigneeName = assigneeName;
        this.assignerName = assignerName;
        this.eventName = eventName;
        this.assigneeId = assigneeId;
        this.assignerId = assignerId;
        this.eventId = eventId;
        this.assigneeIds = assigneeIds;
        this.assigneeNames = assigneeNames;
    }

    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public Integer getMaxAssignees() { return maxAssignees; }
    public void setMaxAssignees(Integer maxAssignees) { this.maxAssignees = maxAssignees; }

    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }

    public String getAssignerName() { return assignerName; }
    public void setAssignerName(String assignerName) { this.assignerName = assignerName; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public Integer getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Integer assigneeId) { this.assigneeId = assigneeId; }

    public Integer getAssignerId() { return assignerId; }
    public void setAssignerId(Integer assignerId) { this.assignerId = assignerId; }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public java.util.List<Integer> getAssigneeIds() { return assigneeIds; }
    public void setAssigneeIds(java.util.List<Integer> assigneeIds) { this.assigneeIds = assigneeIds; }

    public java.util.List<String> getAssigneeNames() { return assigneeNames; }
    public void setAssigneeNames(java.util.List<String> assigneeNames) { this.assigneeNames = assigneeNames; }

    @Override
    public String toString() {
        return title;
    }
}
