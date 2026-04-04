package com.clubmanagement.dto;

import java.time.LocalDateTime;

/**
 * DocumentDTO - Data Transfer Object của Tài liệu
 */
public class DocumentDTO {

    private Integer documentId;
    private String title;
    private String filePath;
    private String fileType;
    private LocalDateTime uploadDate;
    private String description;
    private Boolean isPublic;

    private Integer uploaderId;
    private String uploaderName;

    private Integer eventId;
    private String eventName;

    private Integer projectId;
    private String projectName;

    /** Constructor mặc định. */
    public DocumentDTO() {}

    /**
     * Constructor đầy đủ.
     */
    public DocumentDTO(Integer documentId, String title, String filePath, String fileType, 
                       LocalDateTime uploadDate, String description, Boolean isPublic, 
                       Integer uploaderId, String uploaderName, 
                       Integer eventId, String eventName, 
                       Integer projectId, String projectName) {
        this.documentId = documentId;
        this.title = title;
        this.filePath = filePath;
        this.fileType = fileType;
        this.uploadDate = uploadDate;
        this.description = description;
        this.isPublic = isPublic;
        this.uploaderId = uploaderId;
        this.uploaderName = uploaderName;
        this.eventId = eventId;
        this.eventName = eventName;
        this.projectId = projectId;
        this.projectName = projectName;
    }

    /** @return ID tài liệu. */
    public Integer getDocumentId() { return documentId; }
    /** @param documentId ID tài liệu mới. */
    public void setDocumentId(Integer documentId) { this.documentId = documentId; }

    /** @return Tiêu đề tài liệu. */
    public String getTitle() { return title; }
    /** @param title Tiêu đề mới. */
    public void setTitle(String title) { this.title = title; }

    /** @return Đường dẫn file. */
    public String getFilePath() { return filePath; }
    /** @param filePath Đường dẫn file mới. */
    public void setFilePath(String filePath) { this.filePath = filePath; }

    /** @return Loại file. */
    public String getFileType() { return fileType; }
    /** @param fileType Loại file mới. */
    public void setFileType(String fileType) { this.fileType = fileType; }

    /** @return Ngày upload. */
    public LocalDateTime getUploadDate() { return uploadDate; }
    /** @param uploadDate Ngày upload mới. */
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    /** @return Mô tả tài liệu. */
    public String getDescription() { return description; }
    /** @param description Mô tả mới. */
    public void setDescription(String description) { this.description = description; }

    /** @return Trạng thái công khai. */
    public Boolean getIsPublic() { return isPublic; }
    /** @param isPublic Trạng thái công khai mới. */
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    /** @return ID người upload. */
    public Integer getUploaderId() { return uploaderId; }
    /** @param uploaderId ID người upload mới. */
    public void setUploaderId(Integer uploaderId) { this.uploaderId = uploaderId; }

    /** @return Tên người upload. */
    public String getUploaderName() { return uploaderName; }
    /** @param uploaderName Tên người upload mới. */
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }

    /** @return ID sự kiện liên quan. */
    public Integer getEventId() { return eventId; }
    /** @param eventId ID sự kiện liên quan mới. */
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    /** @return Tên sự kiện liên quan. */
    public String getEventName() { return eventName; }
    /** @param eventName Tên sự kiện liên quan mới. */
    public void setEventName(String eventName) { this.eventName = eventName; }

    /** @return ID dự án liên quan. */
    public Integer getProjectId() { return projectId; }
    /** @param projectId ID dự án liên quan mới. */
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    /** @return Tên dự án liên quan. */
    public String getProjectName() { return projectName; }
    /** @param projectName Tên dự án liên quan mới. */
    public void setProjectName(String projectName) { this.projectName = projectName; }
}
