package com.clubmanagement.dao;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.entity.Meeting;
import com.clubmanagement.util.HibernateUtil;

/**
 * MeetingDAO - Lop truy cap du lieu cho thuc the Meeting (Cuoc hop).
 */
public class MeetingDAO {

    private static final Logger logger = LoggerFactory.getLogger(MeetingDAO.class);

    /**
     * Luu cuoc hop moi.
     * @param meeting Cuoc hop can luu
     * @return Meeting da luu
     */
    public Meeting save(Meeting meeting) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            session.persist(meeting);
            tx.commit();
            return meeting;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Loi khi luu cuoc hop: {}", e.getMessage(), e);
            throw new RuntimeException("Khong the luu cuoc hop: " + e.getMessage(), e);
        }
    }

    /**
     * Cap nhat cuoc hop.
     * @param meeting Cuoc hop can cap nhat
     * @return Meeting da cap nhat
     */
    public Meeting update(Meeting meeting) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Meeting updated = session.merge(meeting);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Loi khi cap nhat cuoc hop: {}", e.getMessage(), e);
            throw new RuntimeException("Khong the cap nhat cuoc hop: " + e.getMessage(), e);
        }
    }

    /**
     * Tim cuoc hop theo ID.
     * @param id ID cuoc hop
     * @return Optional<Meeting>
     */
    public Optional<Meeting> findById(Integer id) {
        try (Session session = HibernateUtil.openSession()) {
            Query<Meeting> query = session.createQuery(
                "SELECT m FROM Meeting m LEFT JOIN FETCH m.host WHERE m.meetingId = :id",
                Meeting.class
            );
            query.setParameter("id", id);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("Loi khi tim cuoc hop ID={}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lay tat ca cuoc hop, sap xep theo thoi gian bat dau giam dan.
     * @return Danh sach Meeting
     */
    public List<Meeting> findAll() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Meeting> query = session.createQuery(
                "SELECT m FROM Meeting m LEFT JOIN FETCH m.host ORDER BY m.startTime DESC",
                Meeting.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Loi khi lay danh sach cuoc hop: {}", e.getMessage());
            throw new RuntimeException("Khong the lay danh sach cuoc hop", e);
        }
    }

    /**
     * Xoa cuoc hop theo ID.
     * @param id ID cuoc hop
     * @return true neu xoa thanh cong
     */
    public boolean deleteById(Integer id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Meeting meeting = session.get(Meeting.class, id);
            if (meeting != null) {
                session.remove(meeting);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Loi khi xoa cuoc hop ID={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Khong the xoa cuoc hop: " + e.getMessage(), e);
        }
    }
}
