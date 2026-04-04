package com.clubmanagement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.clubmanagement.dao.FeedbackDAO;
import com.clubmanagement.dto.FeedbackDTO;
import com.clubmanagement.entity.Event;
import com.clubmanagement.entity.Feedback;
import com.clubmanagement.entity.Member;
import com.clubmanagement.entity.Project;
import com.clubmanagement.util.HibernateUtil;

/**
 * FeedbackService - Tầng nghiệp vụ cho Phản hồi.
 */
public class FeedbackService {

    private final FeedbackDAO feedbackDAO = new FeedbackDAO();

    /**
     * Gửi phản hồi mới.
     * @param memberId ID thành viên
     * @param content Nội dung
     * @param rating Đánh giá
     * @param type Loại phản hồi
     * @param eventId ID sự kiện (nullable)
     * @param projectId ID dự án (nullable)
     * @return FeedbackDTO đã lưu
     */
    public FeedbackDTO submitFeedback(Integer memberId, String content, Integer rating, 
                                      String type, Integer eventId, Integer projectId) {
        if (memberId == null) throw new IllegalArgumentException("Không tìm thấy người dùng hiện tại!");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("Nội dung không được rỗng!");
        if (rating == null || rating < 1 || rating > 5) throw new IllegalArgumentException("Đánh giá phải từ 1-5 sao!");

        try (Session session = HibernateUtil.openSession()) {
            Member member = session.get(Member.class, memberId);
            Event event = eventId != null ? session.get(Event.class, eventId) : null;
            Project project = projectId != null ? session.get(Project.class, projectId) : null;

            Feedback feedback = new Feedback(content, rating, type, member);
            feedback.setEvent(event);
            feedback.setProject(project);

            return toDTO(feedbackDAO.save(feedback));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy tất cả phản hồi.
     * @return Danh sách FeedbackDTO
     */
    public List<FeedbackDTO> getAllFeedbacks() {
        return feedbackDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Xóa phản hồi theo ID.
     * @param id ID phản hồi
     */
    public void deleteFeedback(Integer id) {
        if (!feedbackDAO.deleteById(id)) {
            throw new IllegalArgumentException("Không tìm thấy dữ liệu để xóa!");
        }
    }

    /**
     * Map Feedback entity -> FeedbackDTO.
     * @param f Feedback entity
     * @return FeedbackDTO
     */
    private FeedbackDTO toDTO(Feedback f) {
        if (f == null) return null;
        return new FeedbackDTO(
            f.getFeedbackId(),
            f.getContent(),
            f.getRating(),
            f.getFeedbackType(),
            f.getCreatedDate(),
            f.getMember() != null ? f.getMember().getMemberId() : null,
            f.getMember() != null ? f.getMember().getFullName() : "Khách",
            f.getEvent() != null ? f.getEvent().getEventId() : null,
            f.getEvent() != null ? f.getEvent().getEventName() : "N/A",
            f.getProject() != null ? f.getProject().getProjectId() : null,
            f.getProject() != null ? f.getProject().getProjectName() : "N/A"
        );
    }
}
