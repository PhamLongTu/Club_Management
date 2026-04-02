package com.clubmanagement.dao;

import com.clubmanagement.entity.Feedback;
import com.clubmanagement.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class FeedbackDAO {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackDAO.class);

    public Feedback save(Feedback feedback) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            session.persist(feedback);
            tx.commit();
            return feedback;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi lưu Feedback: " + e.getMessage(), e);
        }
    }

    public List<Feedback> findAll() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Feedback> query = session.createQuery(
                "FROM Feedback f LEFT JOIN FETCH f.member LEFT JOIN FETCH f.event LEFT JOIN FETCH f.project " +
                "ORDER BY f.createdDate DESC",
                Feedback.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi lấy danh sách Feedback: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách Feedback", e);
        }
    }

    public Optional<Feedback> findById(Integer id) {
        try (Session session = HibernateUtil.openSession()) {
            Query<Feedback> query = session.createQuery(
                "FROM Feedback f LEFT JOIN FETCH f.member LEFT JOIN FETCH f.event LEFT JOIN FETCH f.project " +
                "WHERE f.feedbackId = :id",
                Feedback.class
            );
            query.setParameter("id", id);
            return query.uniqueResultOptional();
        }
    }

    public Feedback update(Feedback feedback) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Feedback updated = session.merge(feedback);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Cập nhật Feedback thất bại: " + e.getMessage(), e);
        }
    }

    public boolean deleteById(Integer id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Feedback fb = session.get(Feedback.class, id);
            if (fb != null) {
                session.remove(fb);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Xóa Feedback thất bại: " + e.getMessage(), e);
        }
    }
}
