package com.clubmanagement.dao;

import com.clubmanagement.entity.Announcement;
import com.clubmanagement.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * AnnouncementDAO - Lớp truy cập dữ liệu cho thực thể Announcement (Thông báo).
 */
public class AnnouncementDAO {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncementDAO.class);

    /**
     * Lưu thông báo mới vào database.
     */
    public Announcement save(Announcement announcement) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            session.persist(announcement);
            tx.commit();
            logger.info("Đã lưu thông báo: {}", announcement.getTitle());
            return announcement;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể lưu thông báo: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy tất cả thông báo, ghim lên trước, sau đó sắp theo ngày mới nhất.
     * Logic: isPinned=true lên đầu, sau đó sắp xếp theo createdDate giảm dần.
     */
    public List<Announcement> findAll() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Announcement> query = session.createQuery(
                "FROM Announcement a LEFT JOIN FETCH a.author " +
                "ORDER BY a.isPinned DESC, a.createdDate DESC",
                Announcement.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách thông báo: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách thông báo", e);
        }
    }

    /**
     * Tìm thông báo theo ID.
     */
    public Optional<Announcement> findById(Integer id) {
        try (Session session = HibernateUtil.openSession()) {
            return Optional.ofNullable(session.get(Announcement.class, id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Lấy N thông báo mới nhất (dùng cho Dashboard).
     * @param limit Số lượng thông báo lấy về
     */
    public List<Announcement> findLatest(int limit) {
        try (Session session = HibernateUtil.openSession()) {
            Query<Announcement> query = session.createQuery(
                "FROM Announcement a LEFT JOIN FETCH a.author " +
                "ORDER BY a.isPinned DESC, a.createdDate DESC",
                Announcement.class
            );
            query.setMaxResults(limit);  // giới hạn số dòng trả về = LIMIT trong SQL
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi lấy thông báo mới nhất: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy thông báo", e);
        }
    }

    /**
     * Cập nhật thông báo.
     */
    public Announcement update(Announcement announcement) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Announcement updated = session.merge(announcement);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể cập nhật thông báo: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa thông báo theo ID.
     */
    public boolean deleteById(Integer id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Announcement ann = session.get(Announcement.class, id);
            if (ann != null) {
                session.remove(ann);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể xóa thông báo: " + e.getMessage(), e);
        }
    }
}
