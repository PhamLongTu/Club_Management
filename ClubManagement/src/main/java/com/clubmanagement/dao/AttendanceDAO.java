package com.clubmanagement.dao;

import com.clubmanagement.entity.Attendance;
import com.clubmanagement.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class AttendanceDAO {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceDAO.class);

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
