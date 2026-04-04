package com.clubmanagement.dao;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.entity.Sponsor;
import com.clubmanagement.util.HibernateUtil;

/**
 * SponsorDAO - Lớp truy cập dữ liệu cho thực thể Sponsor (Nhà tài trợ).
 */
public class SponsorDAO {

    private static final Logger logger = LoggerFactory.getLogger(SponsorDAO.class);

    /**
     * Lưu nhà tài trợ mới.
     * @param sponsor Nhà tài trợ cần lưu
     * @return Sponsor đã lưu
     */
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

    /**
     * Lấy tất cả nhà tài trợ.
     * @return Danh sách Sponsor
     */
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

    /**
     * Tìm nhà tài trợ theo ID.
     * @param id ID nhà tài trợ
     * @return Optional<Sponsor>
     */
    public Optional<Sponsor> findById(Integer id) {
        try (Session session = HibernateUtil.openSession()) {
            Sponsor sponsor = session.get(Sponsor.class, id);
            return Optional.ofNullable(sponsor);
        }
    }

    /**
     * Cập nhật nhà tài trợ.
     * @param sponsor Nhà tài trợ cần cập nhật
     * @return Sponsor đã cập nhật
     */
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

    /**
     * Xóa nhà tài trợ theo ID.
     * @param id ID nhà tài trợ
     * @return true nếu xóa thành công
     */
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
