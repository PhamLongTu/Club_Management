package com.clubmanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity: Document (Tài liệu)
 * Map tới bảng 'documents'.
 * Lưu trữ đường dẫn và metadata của các file tài liệu CLB.
 */
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Integer documentId;

    /** Tiêu đề tài liệu */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** Đường dẫn file trên hệ thống */
    @Column(name = "file_path", length = 500)
    private String filePath;

    /** Loại file: PDF, DOCX, XLSX, ... */
    @Column(name = "file_type", length = 50)
    private String fileType;

    /** Ngày upload */
    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    /** Mô tả nội dung tài liệu */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Công khai (true) hay nội bộ (false) */
    @Column(name = "is_public")
    private Boolean isPublic = true;

    /** Người upload (N-1 với Member) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uploader_id")
    private Member uploader;

    /** Sự kiện liên quan (nullable) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id")
    private Event event;

    /** Dự án liên quan (nullable) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private Project project;

    // ============ CONSTRUCTORS ============
    public Document() {
        this.uploadDate = LocalDateTime.now();
    }

    public Document(String title, String filePath, String fileType,
                    String description, Boolean isPublic, Member uploader) {
        this.title       = title;
        this.filePath    = filePath;
        this.fileType    = fileType;
        this.description = description;
        this.isPublic    = isPublic;
        this.uploader    = uploader;
        this.uploadDate  = LocalDateTime.now();
    }

    // ============ GETTERS & SETTERS ============
    public Integer getDocumentId()         { return documentId; }
    public void setDocumentId(Integer v)   { this.documentId = v; }

    public String getTitle()               { return title; }
    public void setTitle(String v)         { this.title = v; }

    public String getFilePath()            { return filePath; }
    public void setFilePath(String v)      { this.filePath = v; }

    public String getFileType()            { return fileType; }
    public void setFileType(String v)      { this.fileType = v; }

    public LocalDateTime getUploadDate()        { return uploadDate; }
    public void setUploadDate(LocalDateTime v)  { this.uploadDate = v; }

    public String getDescription()         { return description; }
    public void setDescription(String v)   { this.description = v; }

    public Boolean getIsPublic()           { return isPublic; }
    public void setIsPublic(Boolean v)     { this.isPublic = v; }

    public Member getUploader()            { return uploader; }
    public void setUploader(Member v)      { this.uploader = v; }

    public Event getEvent()                { return event; }
    public void setEvent(Event v)          { this.event = v; }

    public Project getProject()            { return project; }
    public void setProject(Project v)      { this.project = v; }

    @Override
    public String toString() { return title; }
}
