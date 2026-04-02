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

    public DocumentDTO() {}

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

    public Integer getDocumentId() { return documentId; }
    public void setDocumentId(Integer documentId) { this.documentId = documentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    public Integer getUploaderId() { return uploaderId; }
    public void setUploaderId(Integer uploaderId) { this.uploaderId = uploaderId; }

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
}
