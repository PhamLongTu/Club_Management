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
    /**
     * Constructor mặc định (bắt buộc cho JPA).
     */
    public Document() {
        this.uploadDate = LocalDateTime.now();
    }

    /**
     * Constructor khởi tạo nhanh tài liệu.
     *
     * @param title       Tiêu đề
     * @param filePath    Đường dẫn file
     * @param fileType    Loại file
     * @param description Mô tả
     * @param isPublic    Công khai hay không
     * @param uploader    Người upload
     */
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
    /** @return ID tài liệu. */
    public Integer getDocumentId()         { return documentId; }
    /** @param v ID tài liệu mới. */
    public void setDocumentId(Integer v)   { this.documentId = v; }

    /** @return Tiêu đề tài liệu. */
    public String getTitle()               { return title; }
    /** @param v Tiêu đề mới. */
    public void setTitle(String v)         { this.title = v; }

    /** @return Đường dẫn file. */
    public String getFilePath()            { return filePath; }
    /** @param v Đường dẫn file mới. */
    public void setFilePath(String v)      { this.filePath = v; }

    /** @return Loại file. */
    public String getFileType()            { return fileType; }
    /** @param v Loại file mới. */
    public void setFileType(String v)      { this.fileType = v; }

    /** @return Ngày upload. */
    public LocalDateTime getUploadDate()        { return uploadDate; }
    /** @param v Ngày upload mới. */
    public void setUploadDate(LocalDateTime v)  { this.uploadDate = v; }

    /** @return Mô tả nội dung. */
    public String getDescription()         { return description; }
    /** @param v Mô tả mới. */
    public void setDescription(String v)   { this.description = v; }

    /** @return Trạng thái công khai. */
    public Boolean getIsPublic()           { return isPublic; }
    /** @param v Trạng thái công khai mới. */
    public void setIsPublic(Boolean v)     { this.isPublic = v; }

    /** @return Người upload. */
    public Member getUploader()            { return uploader; }
    /** @param v Người upload mới. */
    public void setUploader(Member v)      { this.uploader = v; }

    /** @return Sự kiện liên quan. */
    public Event getEvent()                { return event; }
    /** @param v Sự kiện liên quan mới. */
    public void setEvent(Event v)          { this.event = v; }

    /** @return Dự án liên quan. */
    public Project getProject()            { return project; }
    /** @param v Dự án liên quan mới. */
    public void setProject(Project v)      { this.project = v; }

    /**
     * Hiển thị tiêu đề tài liệu trong UI.
     *
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() { return title; }
}
