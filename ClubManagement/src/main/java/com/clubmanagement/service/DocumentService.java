package com.clubmanagement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.clubmanagement.dao.DocumentDAO;
import com.clubmanagement.dto.DocumentDTO;
import com.clubmanagement.entity.Document;
import com.clubmanagement.entity.Event;
import com.clubmanagement.entity.Member;
import com.clubmanagement.entity.Project;
import com.clubmanagement.util.HibernateUtil;

/**
 * DocumentService - Tầng nghiệp vụ cho Tài liệu.
 */
public class DocumentService {

    private final DocumentDAO documentDAO = new DocumentDAO();

    /**
     * Tải lên tài liệu mới.
     * @param title Tiêu đề
     * @param filePath Đường dẫn file
     * @param fileType Loại file
     * @param description Mô tả
     * @param isPublic Công khai hay không
     * @param uploaderId ID người upload
     * @param eventId ID sự kiện liên quan (nullable)
     * @param projectId ID dự án liên quan (nullable)
     * @return DocumentDTO đã lưu
     */
    public DocumentDTO uploadDocument(String title, String filePath, String fileType, String description,
                                      Boolean isPublic, Integer uploaderId, Integer eventId, Integer projectId) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Tiêu đề không được để trống!");
        if (uploaderId == null) throw new IllegalArgumentException("Không xác định người tải lên!");

        try (Session session = HibernateUtil.openSession()) {
            Member uploader = session.get(Member.class, uploaderId);
            Event event = eventId != null ? session.get(Event.class, eventId) : null;
            Project project = projectId != null ? session.get(Project.class, projectId) : null;

            Document doc = new Document(title.trim(), filePath, fileType, description, isPublic, uploader);
            doc.setEvent(event);
            doc.setProject(project);

            return toDTO(documentDAO.save(doc));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi Service: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy tất cả tài liệu.
     * @return Danh sách DocumentDTO
     */
    public List<DocumentDTO> getAllDocuments() {
        return documentDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Cập nhật thông tin tài liệu.
     * @param docId ID tài liệu
     * @param title Tiêu đề
     * @param filePath Đường dẫn file
     * @param fileType Loại file
     * @param description Mô tả
     * @param isPublic Công khai hay không
     * @return DocumentDTO đã cập nhật
     */
    public DocumentDTO updateDocument(Integer docId, String title, String filePath, String fileType,
                                      String description, Boolean isPublic) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Tiêu đề không được để trống!");
        
        Document doc = documentDAO.findById(docId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Tài liệu để cập nhật!"));
            
        doc.setTitle(title.trim());
        doc.setFilePath(filePath);
        doc.setFileType(fileType);
        doc.setDescription(description);
        doc.setIsPublic(isPublic);

        return toDTO(documentDAO.update(doc));
    }

    /**
     * Xóa tài liệu theo ID.
     * @param id ID tài liệu
     */
    public void deleteDocument(Integer id) {
        if (!documentDAO.deleteById(id)) {
            throw new IllegalArgumentException("Không tìm thấy Tài liệu để xóa!");
        }
    }

    /**
     * Map Document entity -> DocumentDTO.
     * @param d Document entity
     * @return DocumentDTO
     */
    private DocumentDTO toDTO(Document d) {
        if (d == null) return null;
        return new DocumentDTO(
            d.getDocumentId(), d.getTitle(), d.getFilePath(), d.getFileType(), d.getUploadDate(),
            d.getDescription(), d.getIsPublic(),
            d.getUploader() != null ? d.getUploader().getMemberId() : null,
            d.getUploader() != null ? d.getUploader().getFullName() : "N/A",
            d.getEvent() != null ? d.getEvent().getEventId() : null,
            d.getEvent() != null ? d.getEvent().getEventName() : null,
            d.getProject() != null ? d.getProject().getProjectId() : null,
            d.getProject() != null ? d.getProject().getProjectName() : null
        );
    }
}
