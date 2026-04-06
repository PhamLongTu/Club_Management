package com.clubmanagement.dao;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.entity.Event;
import com.clubmanagement.util.HibernateUtil;

/**
 * EventDAO - Lớp truy cập dữ liệu cho thực thể Event (Sự kiện).
 */
public class EventDAO extends AbstractDAO<Event, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(EventDAO.class);

    public EventDAO() {
        super(Event.class);
    }

    /**
     * Lưu sự kiện mới vào database.
     * @param event Sự kiện cần lưu
     * @return Event đã lưu (có ID)
     */
    public Event save(Event event) {
        Event saved = saveEntity(event, "Không thể lưu sự kiện");
        logger.info("Đã lưu sự kiện: {}", event.getEventName());
        return saved;
    }

    /**
     * Tìm sự kiện theo ID.
     * @param eventId ID sự kiện
     * @return Optional<Event>
     */
    public Optional<Event> findById(Integer eventId) {
        try (Session session = HibernateUtil.openSession()) {
            Event event = session.get(Event.class, eventId);
            if (event != null) {
                org.hibernate.Hibernate.initialize(event.getParticipations());
            }
            return Optional.ofNullable(event);
        } catch (Exception e) {
            logger.error("Lỗi khi tìm sự kiện ID={}: {}", eventId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lấy tất cả sự kiện, sắp xếp theo ngày bắt đầu giảm dần (mới nhất trước).
     * @return Danh sách Event
     */
    public List<Event> findAll() {
        try (Session session = HibernateUtil.openSession()) {
            // JOIN FETCH để load luôn createdBy, tránh N+1 query
            Query<Event> query = session.createQuery(
                "FROM Event e LEFT JOIN FETCH e.createdBy ORDER BY e.startDate DESC",
                Event.class
            );
            List<Event> results = query.getResultList();
            results.forEach(e -> org.hibernate.Hibernate.initialize(e.getParticipations()));
            return results;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách sự kiện: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách sự kiện", e);
        }
    }

    /**
     * Lấy các sự kiện theo trạng thái.
     * @param status Trạng thái cần lọc (Upcoming, Ongoing, Completed, Cancelled)
     * @return Danh sách Event
     */
    public List<Event> findByStatus(String status) {
        try (Session session = HibernateUtil.openSession()) {
            Query<Event> query = session.createQuery(
                "FROM Event e WHERE e.status = :status ORDER BY e.startDate DESC",
                Event.class
            );
            query.setParameter("status", status);
            List<Event> results = query.getResultList();
            results.forEach(e -> org.hibernate.Hibernate.initialize(e.getParticipations()));
            return results;
        } catch (Exception e) {
            logger.error("Lỗi khi lọc sự kiện theo trạng thái: {}", e.getMessage());
            throw new RuntimeException("Không thể lọc sự kiện", e);
        }
    }

    /**
     * Tìm kiếm sự kiện theo tên hoặc địa điểm.
     * @param keyword Từ khóa
     * @return Danh sách Event phù hợp
     */
    public List<Event> search(String keyword) {
        try (Session session = HibernateUtil.openSession()) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            Query<Event> query = session.createQuery(
                "FROM Event e WHERE LOWER(e.eventName) LIKE :kw " +
                "OR LOWER(e.location) LIKE :kw ORDER BY e.startDate DESC",
                Event.class
            );
            query.setParameter("kw", pattern);
            List<Event> results = query.getResultList();
            results.forEach(e -> org.hibernate.Hibernate.initialize(e.getParticipations()));
            return results;
        } catch (Exception e) {
            logger.error("Lỗi khi tìm kiếm sự kiện: {}", e.getMessage());
            throw new RuntimeException("Không thể tìm kiếm sự kiện", e);
        }
    }

    /**
     * Đếm tổng số sự kiện.
     * @return Tổng số Event
     */
    public long countAll() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(e) FROM Event e", Long.class
            );
            return query.uniqueResult();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Đếm số sự kiện đang diễn ra hoặc sắp tới.
     * @return Số sự kiện sắp/đang diễn ra
     */
    public long countUpcoming() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(e) FROM Event e WHERE e.status IN ('Upcoming','Ongoing')",
                Long.class
            );
            return query.uniqueResult();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Cập nhật thông tin sự kiện.
     * @param event Event cần cập nhật
     * @return Event sau khi cập nhật
     */
    public Event update(Event event) {
        Event updated = updateEntity(event, "Không thể cập nhật sự kiện");
        logger.info("Đã cập nhật sự kiện: {}", event.getEventName());
        return updated;
    }

    /**
     * Xóa sự kiện theo ID.
     * @param eventId ID sự kiện
     * @return true nếu thành công
     */
    public boolean deleteById(Integer eventId) {
        boolean deleted = deleteEntityById(eventId, "Không thể xóa sự kiện");
        if (deleted) {
            logger.info("Đã xóa sự kiện ID: {}", eventId);
        }
        return deleted;
    }
}
