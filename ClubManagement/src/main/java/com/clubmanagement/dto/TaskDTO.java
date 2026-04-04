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

    /** Constructor mặc định. */
    public TaskDTO() {}

    /**
     * Constructor đầy đủ.
     */
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

    /** @return ID nhiệm vụ. */
    public Integer getTaskId() { return taskId; }
    /** @param taskId ID nhiệm vụ mới. */
    public void setTaskId(Integer taskId) { this.taskId = taskId; }

    /** @return Tiêu đề nhiệm vụ. */
    public String getTitle() { return title; }
    /** @param title Tiêu đề mới. */
    public void setTitle(String title) { this.title = title; }

    /** @return Mô tả nhiệm vụ. */
    public String getDescription() { return description; }
    /** @param description Mô tả mới. */
    public void setDescription(String description) { this.description = description; }

    /** @return Hạn chót. */
    public LocalDateTime getDeadline() { return deadline; }
    /** @param deadline Hạn chót mới. */
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    /** @return Mức độ ưu tiên. */
    public String getPriority() { return priority; }
    /** @param priority Mức độ ưu tiên mới. */
    public void setPriority(String priority) { this.priority = priority; }

    /** @return Trạng thái nhiệm vụ. */
    public String getStatus() { return status; }
    /** @param status Trạng thái mới. */
    public void setStatus(String status) { this.status = status; }

    /** @return Ngày tạo. */
    public LocalDateTime getCreatedDate() { return createdDate; }
    /** @param createdDate Ngày tạo mới. */
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    /** @return Chế độ hiển thị. */
    public String getVisibility() { return visibility; }
    /** @param visibility Chế độ hiển thị mới. */
    public void setVisibility(String visibility) { this.visibility = visibility; }

    /** @return Số người tối đa. */
    public Integer getMaxAssignees() { return maxAssignees; }
    /** @param maxAssignees Số người tối đa mới. */
    public void setMaxAssignees(Integer maxAssignees) { this.maxAssignees = maxAssignees; }

    /** @return Tên người thực hiện. */
    public String getAssigneeName() { return assigneeName; }
    /** @param assigneeName Tên người thực hiện mới. */
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }

    /** @return Tên người giao. */
    public String getAssignerName() { return assignerName; }
    /** @param assignerName Tên người giao mới. */
    public void setAssignerName(String assignerName) { this.assignerName = assignerName; }

    /** @return Tên sự kiện liên quan. */
    public String getEventName() { return eventName; }
    /** @param eventName Tên sự kiện mới. */
    public void setEventName(String eventName) { this.eventName = eventName; }

    /** @return ID người thực hiện. */
    public Integer getAssigneeId() { return assigneeId; }
    /** @param assigneeId ID người thực hiện mới. */
    public void setAssigneeId(Integer assigneeId) { this.assigneeId = assigneeId; }

    /** @return ID người giao. */
    public Integer getAssignerId() { return assignerId; }
    /** @param assignerId ID người giao mới. */
    public void setAssignerId(Integer assignerId) { this.assignerId = assignerId; }

    /** @return ID sự kiện. */
    public Integer getEventId() { return eventId; }
    /** @param eventId ID sự kiện mới. */
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    /** @return Danh sách ID người thực hiện. */
    public java.util.List<Integer> getAssigneeIds() { return assigneeIds; }
    /** @param assigneeIds Danh sách ID người thực hiện mới. */
    public void setAssigneeIds(java.util.List<Integer> assigneeIds) { this.assigneeIds = assigneeIds; }

    /** @return Danh sách tên người thực hiện. */
    public java.util.List<String> getAssigneeNames() { return assigneeNames; }
    /** @param assigneeNames Danh sách tên người thực hiện mới. */
    public void setAssigneeNames(java.util.List<String> assigneeNames) { this.assigneeNames = assigneeNames; }

    /**
     * Hiển thị tiêu đề nhiệm vụ trong UI.
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() {
        return title;
    }
}
