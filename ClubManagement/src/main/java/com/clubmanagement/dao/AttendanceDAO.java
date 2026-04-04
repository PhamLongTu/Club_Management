package com.clubmanagement.dao;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.entity.Attendance;
import com.clubmanagement.util.HibernateUtil;

/**
 * AttendanceDAO - Lớp truy cập dữ liệu cho thực thể Attendance (Điểm danh).
 */
public class AttendanceDAO {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceDAO.class);

    /**
     * Lưu bản ghi điểm danh mới.
     * @param attendance Đối tượng điểm danh
     * @return Attendance đã lưu
     */
    public Attendance save(Attendance attendance) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            session.persist(attendance);
            tx.commit();
            return attendance;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi lưu điểm danh (Thành viên này có thể đã được điểm danh trong sự kiện): " + e.getMessage(), e);
        }
    }

    /**
     * Lấy tất cả điểm danh, sắp theo thời gian check-in giảm dần.
     * @return Danh sách Attendance
     */
    public List<Attendance> findAll() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Attendance> query = session.createQuery(
                "FROM Attendance a LEFT JOIN FETCH a.member LEFT JOIN FETCH a.event " +
                "ORDER BY a.checkInTime DESC",
                Attendance.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi lấy danh sách điểm danh: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách điểm danh", e);
        }
    }

    /**
     * Tìm điểm danh theo ID.
     * @param id ID điểm danh
     * @return Optional<Attendance>
     */
    public Optional<Attendance> findById(Integer id) {
        try (Session session = HibernateUtil.openSession()) {
            Query<Attendance> query = session.createQuery(
                "FROM Attendance a LEFT JOIN FETCH a.member LEFT JOIN FETCH a.event " +
                "WHERE a.attendanceId = :id",
                Attendance.class
            );
            query.setParameter("id", id);
            return query.uniqueResultOptional();
        }
    }

    /**
     * Lấy điểm danh theo sự kiện.
     * @param eventId ID sự kiện
     * @return Danh sách Attendance
     */
    public List<Attendance> findByEventId(Integer eventId) {
        try (Session session = HibernateUtil.openSession()) {
            Query<Attendance> query = session.createQuery(
                "FROM Attendance a LEFT JOIN FETCH a.member LEFT JOIN FETCH a.event " +
                "WHERE a.event.eventId = :eid ORDER BY a.checkInTime DESC",
                Attendance.class
            );
            query.setParameter("eid", eventId);
            return query.getResultList();
        }
    }

    /**
     * Cập nhật bản ghi điểm danh.
     * @param attendance Đối tượng điểm danh
     * @return Attendance đã cập nhật
     */
    public Attendance update(Attendance attendance) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Attendance updated = session.merge(attendance);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Cập nhật điểm danh thất bại: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa điểm danh theo ID.
     * @param id ID điểm danh
     * @return true nếu xóa thành công
     */
    public boolean deleteById(Integer id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Attendance attendance = session.get(Attendance.class, id);
            if (attendance != null) {
                session.remove(attendance);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Xóa điểm danh thất bại: " + e.getMessage(), e);
        }
    }
}
