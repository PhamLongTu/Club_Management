package com.clubmanagement.dao;

import com.clubmanagement.entity.Sponsor;
import com.clubmanagement.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class SponsorDAO {

    private static final Logger logger = LoggerFactory.getLogger(SponsorDAO.class);

    public Sponsor save(Sponsor sponsor) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            session.persist(sponsor);
            tx.commit();
            return sponsor;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể lưu thông tin Nhà tài trợ: " + e.getMessage(), e);
        }
    }

    public List<Sponsor> findAll() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Sponsor> query = session.createQuery(
                "FROM Sponsor ORDER BY sponsorName ASC",
                Sponsor.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách nhà tài trợ: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách nhà tài trợ", e);
        }
    }

    public Optional<Sponsor> findById(Integer id) {
        try (Session session = HibernateUtil.openSession()) {
            Sponsor sponsor = session.get(Sponsor.class, id);
            return Optional.ofNullable(sponsor);
        }
    }

    public Sponsor update(Sponsor sponsor) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Sponsor updated = session.merge(sponsor);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể cập nhật nhà tài trợ: " + e.getMessage(), e);
        }
    }

    public boolean deleteById(Integer id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Sponsor sponsor = session.get(Sponsor.class, id);
            if (sponsor != null) {
                // Sẽ ném ngoại lệ nếu có khóa ngoại từ event_sponsors trừ khi có cascading
                session.remove(sponsor);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Lỗi xóa nhà tài trợ: {}", e.getMessage());
            throw new RuntimeException("Không thể xóa nhà tài trợ (Có thể lỗi rằng buộc do tài trợ đã được sử dụng): " + e.getMessage(), e);
        }
    }
}
