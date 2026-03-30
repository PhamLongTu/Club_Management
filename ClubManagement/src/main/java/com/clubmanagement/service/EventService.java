package com.clubmanagement.service;

import com.clubmanagement.dao.EventDAO;
import com.clubmanagement.dto.EventDTO;
import com.clubmanagement.entity.Event;
import com.clubmanagement.entity.Member;
import com.clubmanagement.util.HibernateUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * EventService - Tầng nghiệp vụ cho Sự kiện.
 *
 * Xử lý business logic bao gồm:
 * - Validate thời gian sự kiện (end > start)
 * - Chuyển đổi Entity <-> DTO
 * - Gọi EventDAO
 */
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    private final EventDAO eventDAO = new EventDAO();

    /**
     * Tạo sự kiện mới với đầy đủ validation.
     *
     * @param eventName       Tên sự kiện
     * @param description     Mô tả
     * @param startDate       Thời gian bắt đầu
     * @param endDate         Thời gian kết thúc (phải sau startDate)
     * @param location        Địa điểm
     * @param budget          Ngân sách (>= 0)
     * @param maxParticipants Số lượng tối đa (> 0)
     * @param createdById     ID người tạo (thường là user đang đăng nhập)
     * @return EventDTO vừa tạo
     */
    public EventDTO createEvent(String eventName, String description,
                                LocalDateTime startDate, LocalDateTime endDate,
                                String location, BigDecimal budget,
                                Integer maxParticipants, Integer createdById) {
        // --- Validate ---
        if (eventName == null || eventName.isBlank())
            throw new IllegalArgumentException("Tên sự kiện không được để trống!");
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("Ngày bắt đầu và kết thúc không được để trống!");
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate))
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu!");
        if (budget != null && budget.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Ngân sách không được âm!");
        if (maxParticipants != null && maxParticipants <= 0)
            throw new IllegalArgumentException("Số lượng tối đa phải lớn hơn 0!");

        // --- Tìm người tạo ---
        Member creator = findMemberById(createdById);

        // --- Tạo entity ---
        Event event = new Event(
            eventName.trim(), description, startDate, endDate, location,
            budget != null ? budget : BigDecimal.ZERO, creator
        );
        if (maxParticipants != null) event.setMaxParticipants(maxParticipants);

        Event saved = eventDAO.save(event);
        return toDTO(saved);
    }

    /**
     * Lấy tất cả sự kiện dạng DTO.
     * @return List<EventDTO>
     */
    public List<EventDTO> getAllEvents() {
        return eventDAO.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lọc sự kiện theo trạng thái.
     * @param status Trạng thái (Upcoming/Ongoing/Completed/Cancelled)
     * @return List<EventDTO>
     */
    public List<EventDTO> getEventsByStatus(String status) {
        return eventDAO.findByStatus(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm sự kiện theo từ khóa.
     * @param keyword Từ khóa
     * @return List<EventDTO>
     */
    public List<EventDTO> searchEvents(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAllEvents();
        return eventDAO.search(keyword).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy sự kiện theo ID.
     * @param eventId ID sự kiện
     * @return Optional<EventDTO>
     */
    public Optional<EventDTO> getEventById(Integer eventId) {
        return eventDAO.findById(eventId).map(this::toDTO);
    }

    /**
     * Cập nhật thông tin sự kiện.
     *
     * @param eventId    ID sự kiện
     * @param eventName  Tên mới
     * @param description Mô tả mới
     * @param startDate  Ngày bắt đầu mới
     * @param endDate    Ngày kết thúc mới
     * @param location   Địa điểm mới
     * @param budget     Ngân sách mới
     * @param status     Trạng thái mới
     * @return EventDTO sau khi cập nhật
     */
    public EventDTO updateEvent(Integer eventId, String eventName, String description,
                                LocalDateTime startDate, LocalDateTime endDate,
                                String location, BigDecimal budget, String status) {
        Event event = eventDAO.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện ID: " + eventId));

        if (eventName == null || eventName.isBlank())
            throw new IllegalArgumentException("Tên sự kiện không được để trống!");
        if (endDate != null && startDate != null && endDate.isBefore(startDate))
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu!");

        event.setEventName(eventName.trim());
        event.setDescription(description);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setLocation(location);
        event.setBudget(budget);
        event.setStatus(status);

        return toDTO(eventDAO.update(event));
    }

    /**
     * Xóa sự kiện theo ID.
     * @param eventId ID sự kiện
     */
    public void deleteEvent(Integer eventId) {
        if (!eventDAO.deleteById(eventId))
            throw new IllegalArgumentException("Không tìm thấy sự kiện để xóa!");
    }

    /** Đếm tổng số sự kiện (cho Dashboard). */
    public long getTotalCount() { return eventDAO.countAll(); }

    /** Đếm số sự kiện sắp/đang diễn ra (cho Dashboard). */
    public long getUpcomingCount() { return eventDAO.countUpcoming(); }

    // ===================================================
    // PRIVATE HELPERS
    // ===================================================

    /**
     * Chuyển Event Entity thành EventDTO,
     * đồng thời đếm số lượng người đã đăng ký.
     */
    private EventDTO toDTO(Event event) {
        // Đếm số người đăng ký từ collection đã load
        int regCount = (event.getParticipations() != null)
                       ? event.getParticipations().size() : 0;

        return new EventDTO(
            event.getEventId(),
            event.getEventName(),
            event.getDescription(),
            event.getStartDate(),
            event.getEndDate(),
            event.getLocation(),
            event.getStatus(),
            event.getBudget(),
            event.getMaxParticipants(),
            event.getCreatedBy() != null ? event.getCreatedBy().getFullName() : "N/A",
            regCount
        );
    }

    /** Tìm Member entity theo ID. */
    private Member findMemberById(Integer memberId) {
        if (memberId == null) return null;
        try (Session session = HibernateUtil.openSession()) {
            return session.get(Member.class, memberId);
        } catch (Exception e) {
            logger.error("Lỗi khi tìm Member ID={}: {}", memberId, e.getMessage());
            return null;
        }
    }
}
