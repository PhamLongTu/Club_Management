package com.clubmanagement.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.dao.EventDAO;
import com.clubmanagement.dto.EventDTO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.entity.Event;
import com.clubmanagement.entity.Member;
import com.clubmanagement.util.HibernateUtil;

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
                                LocalDateTime registrationDeadline, String location,
                                BigDecimal budget, Integer maxParticipants,
                                Integer createdById, String pointType, Integer pointValue) {
        // --- Validate ---
        if (eventName == null || eventName.isBlank())
            throw new IllegalArgumentException("Tên sự kiện không được để trống!");
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("Ngày bắt đầu và kết thúc không được để trống!");
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate))
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu!");
        if (registrationDeadline != null && registrationDeadline.isAfter(startDate))
            throw new IllegalArgumentException("Hạn đăng ký phải trước ngày bắt đầu!");
        if (budget != null && budget.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Ngân sách không được âm!");
        if (maxParticipants != null && maxParticipants <= 0)
            throw new IllegalArgumentException("Số lượng tối đa phải lớn hơn 0!");
        if (pointValue != null && pointValue < 0)
            throw new IllegalArgumentException("Điểm sự kiện không được âm!");

        // --- Tìm người tạo ---
        Member creator = findMemberById(createdById);

        // --- Tạo entity ---
        Event event = new Event(
            eventName.trim(), description, startDate, endDate, location,
            budget != null ? budget : BigDecimal.ZERO, creator
        );
        if (maxParticipants != null) event.setMaxParticipants(maxParticipants);
        event.setRegistrationDeadline(registrationDeadline);
        String normalizedPointType = normalizePointType(pointType);
        event.setPointType(normalizedPointType);
        if ("None".equalsIgnoreCase(normalizedPointType)) {
            event.setPointValue(0);
        } else {
            event.setPointValue(pointValue != null ? pointValue : 0);
        }

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
     * Lấy danh sách sự kiện mà thành viên đã tham gia/đăng ký.
     * @param memberId ID thành viên
     * @return Danh sách EventDTO
     */
    public List<EventDTO> getEventsForMember(Integer memberId) {
        if (memberId == null) return java.util.Collections.emptyList();
        try (Session session = HibernateUtil.openSession()) {
            List<Event> events = session.createQuery(
                "SELECT DISTINCT e FROM Event e " +
                "JOIN e.participations p " +
                "JOIN p.member m " +
                "LEFT JOIN FETCH e.createdBy " +
                "LEFT JOIN FETCH e.participations " +
                "WHERE m.memberId = :mid",
                Event.class
            ).setParameter("mid", memberId).getResultList();
            return events.stream().map(this::toDTO).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Lỗi khi lấy sự kiện của thành viên: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Lấy danh sách sự kiện đã điểm danh (không tính Absent).
     * @param memberId ID thành viên
     * @return List<EventDTO>
     */
    public List<EventDTO> getAttendedEventsForMember(Integer memberId) {
        if (memberId == null) return java.util.Collections.emptyList();
        try (Session session = HibernateUtil.openSession()) {
            List<Event> events = session.createQuery(
                "SELECT DISTINCT e FROM Attendance a " +
                "JOIN a.event e " +
                "LEFT JOIN FETCH e.createdBy " +
                "LEFT JOIN FETCH e.participations " +
                "WHERE a.member.memberId = :mid",
                Event.class
            ).setParameter("mid", memberId).getResultList();
            return events.stream().map(this::toDTO).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Lỗi khi lấy sự kiện đã điểm danh: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Lấy danh sách thành viên đã đăng ký sự kiện.
     * @param eventId ID sự kiện
     * @return List<MemberDTO>
     */
    public List<MemberDTO> getRegisteredMembersForEvent(Integer eventId) {
        if (eventId == null) return java.util.Collections.emptyList();
        try (Session session = HibernateUtil.openSession()) {
            List<Object[]> rows = session.createQuery(
                "SELECT m.memberId, m.fullName, m.studentId, m.email " +
                "FROM Participation p JOIN p.member m " +
                "WHERE p.event.eventId = :eid " +
                "ORDER BY m.fullName",
                Object[].class
            ).setParameter("eid", eventId).getResultList();

            List<MemberDTO> results = new java.util.ArrayList<>();
            for (Object[] row : rows) {
                MemberDTO dto = new MemberDTO();
                dto.setMemberId((Integer) row[0]);
                dto.setFullName((String) row[1]);
                dto.setStudentId((String) row[2]);
                dto.setEmail((String) row[3]);
                results.add(dto);
            }
            return results;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách đăng ký sự kiện: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
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
                                LocalDateTime registrationDeadline, String location,
                                BigDecimal budget, String status, Integer maxParticipants,
                                String pointType, Integer pointValue) {
        Event event = eventDAO.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện ID: " + eventId));

        if (eventName == null || eventName.isBlank())
            throw new IllegalArgumentException("Tên sự kiện không được để trống!");
        if (endDate != null && startDate != null && endDate.isBefore(startDate))
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu!");
        if (registrationDeadline != null && startDate != null && registrationDeadline.isAfter(startDate))
            throw new IllegalArgumentException("Hạn đăng ký phải trước ngày bắt đầu!");
        if (pointValue != null && pointValue < 0)
            throw new IllegalArgumentException("Điểm sự kiện không được âm!");
        if (maxParticipants != null && maxParticipants <= 0)
            throw new IllegalArgumentException("Số lượng tối đa phải lớn hơn 0!");

        event.setEventName(eventName.trim());
        event.setDescription(description);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setRegistrationDeadline(registrationDeadline);
        event.setLocation(location);
        event.setBudget(budget);
        event.setStatus(status);
        if (maxParticipants != null) {
            event.setMaxParticipants(maxParticipants);
        }
        String normalizedPointType = normalizePointType(pointType);
        event.setPointType(normalizedPointType);
        if ("None".equalsIgnoreCase(normalizedPointType)) {
            event.setPointValue(0);
        } else {
            event.setPointValue(pointValue != null ? pointValue : event.getPointValue());
        }

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
            event.getRegistrationDeadline(),
            event.getCreatedBy() != null ? event.getCreatedBy().getFullName() : "N/A",
            regCount,
            event.getPointType(),
            event.getPointValue()
        );
    }

    private String normalizePointType(String pointType) {
        if (pointType == null || pointType.isBlank()) return "None";
        String normalized = pointType.trim();
        if ("DRL".equalsIgnoreCase(normalized)) return "DRL";
        if ("CTXH".equalsIgnoreCase(normalized)) return "CTXH";
        return "None";
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

    /**
     * Đăng ký thành viên tham gia sự kiện.
     * @param eventId ID sự kiện
     * @param memberId ID thành viên
     */
    public void registerForEvent(Integer eventId, Integer memberId) {
        if (eventId == null || memberId == null) {
            throw new IllegalArgumentException("Thiếu thông tin đăng ký sự kiện");
        }
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Event event = session.get(Event.class, eventId);
            Member member = session.get(Member.class, memberId);
            if (event == null || member == null) {
                throw new IllegalArgumentException("Không tìm thấy sự kiện hoặc thành viên");
            }
            String status = event.getStatus();
            if (status != null && ("Cancelled".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status))) {
                throw new IllegalStateException("Sự kiện đã kết thúc hoặc bị hủy, không thể đăng ký");
            }
            if (event.getRegistrationDeadline() != null
                && LocalDateTime.now().isAfter(event.getRegistrationDeadline())) {
                throw new IllegalStateException("Đã hết thời gian đăng ký sự kiện");
            }
            org.hibernate.Hibernate.initialize(event.getParticipations());
            long currentCount = event.getParticipations() != null ? event.getParticipations().size() : 0;
            Integer maxParticipants = event.getMaxParticipants();
            int max = maxParticipants != null ? maxParticipants : 0;
            if (max > 0 && currentCount >= max) {
                throw new IllegalStateException("Sự kiện đã đủ số lượng đăng ký");
            }
            boolean alreadyRegistered = event.getParticipations() != null
                && event.getParticipations().stream().anyMatch(p -> p.getMember().getMemberId().equals(memberId));
            if (alreadyRegistered) {
                throw new IllegalStateException("Bạn đã đăng ký sự kiện này rồi");
            }
            com.clubmanagement.entity.Participation participation = new com.clubmanagement.entity.Participation();
            participation.setEvent(event);
            participation.setMember(member);
            participation.setRegistrationDate(LocalDateTime.now());
            session.persist(participation);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể đăng ký sự kiện: " + e.getMessage(), e);
        }
    }

    /**
     * Hủy đăng ký sự kiện.
     * @param eventId ID sự kiện
     * @param memberId ID thành viên
     */
    public void unregisterFromEvent(Integer eventId, Integer memberId) {
        if (eventId == null || memberId == null) {
            throw new IllegalArgumentException("Thiếu thông tin hủy đăng ký sự kiện");
        }
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Event event = session.get(Event.class, eventId);
            if (event == null) {
                throw new IllegalArgumentException("Không tìm thấy sự kiện");
            }
            if (event.getRegistrationDeadline() != null
                && LocalDateTime.now().isAfter(event.getRegistrationDeadline())) {
                throw new IllegalStateException("Đã hết hạn đăng ký sự kiện");
            }

            com.clubmanagement.entity.Participation participation = session.createQuery(
                "FROM Participation p WHERE p.event.eventId = :eid AND p.member.memberId = :mid",
                com.clubmanagement.entity.Participation.class
            )
            .setParameter("eid", eventId)
            .setParameter("mid", memberId)
            .uniqueResult();

            if (participation == null) {
                throw new IllegalStateException("Bạn chưa đăng ký sự kiện này");
            }

            session.remove(participation);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể hủy đăng ký sự kiện: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra thành viên đã đăng ký sự kiện hay chưa.
     * @param eventId ID sự kiện
     * @param memberId ID thành viên
     * @return true nếu đã đăng ký
     */
    public boolean isMemberRegistered(Integer eventId, Integer memberId) {
        if (eventId == null || memberId == null) return false;
        try (Session session = HibernateUtil.openSession()) {
            Long count = session.createQuery(
                "SELECT COUNT(p) FROM Participation p WHERE p.event.eventId = :eid AND p.member.memberId = :mid",
                Long.class
            )
            .setParameter("eid", eventId)
            .setParameter("mid", memberId)
            .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra đăng ký sự kiện: {}", e.getMessage());
            return false;
        }
    }
}
