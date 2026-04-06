package com.clubmanagement.dao;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.entity.Meeting;
import com.clubmanagement.util.HibernateUtil;

/**
 * MeetingDAO - Lop truy cap du lieu cho thuc the Meeting (Cuoc hop).
 */
public class MeetingDAO extends AbstractDAO<Meeting, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(MeetingDAO.class);

    public MeetingDAO() {
        super(Meeting.class);
    }

    /**
     * Luu cuoc hop moi.
     * @param meeting Cuoc hop can luu
     * @return Meeting da luu
     */
    public Meeting save(Meeting meeting) {
        return saveEntity(meeting, "Khong the luu cuoc hop");
    }

    /**
     * Cap nhat cuoc hop.
     * @param meeting Cuoc hop can cap nhat
     * @return Meeting da cap nhat
     */
    public Meeting update(Meeting meeting) {
        return updateEntity(meeting, "Khong the cap nhat cuoc hop");
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
        return deleteEntityById(id, "Khong the xoa cuoc hop");
    }
}
