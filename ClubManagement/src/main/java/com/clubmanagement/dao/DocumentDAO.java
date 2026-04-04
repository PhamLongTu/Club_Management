package com.clubmanagement.dao;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.entity.Document;
import com.clubmanagement.util.HibernateUtil;

/**
 * DocumentDAO - Lớp truy cập dữ liệu cho thực thể Document (Tài liệu).
 */
public class DocumentDAO {

    private static final Logger logger = LoggerFactory.getLogger(DocumentDAO.class);

    /**
     * Lưu tài liệu mới.
     * @param document Tài liệu cần lưu
     * @return Document đã lưu
     */
    public Document save(Document document) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            session.persist(document);
            tx.commit();
            return document;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể lưu Tài liệu: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy tất cả tài liệu, sắp theo ngày upload giảm dần.
     * @return Danh sách Document
     */
    public List<Document> findAll() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Document> query = session.createQuery(
                "FROM Document d LEFT JOIN FETCH d.uploader LEFT JOIN FETCH d.event LEFT JOIN FETCH d.project " +
                "ORDER BY d.uploadDate DESC",
                Document.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi lấy danh sách Tài liệu: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách Tài liệu", e);
        }
    }

    /**
     * Tìm tài liệu theo ID.
     * @param id ID tài liệu
     * @return Optional<Document>
     */
    public Optional<Document> findById(Integer id) {
        try (Session session = HibernateUtil.openSession()) {
            Query<Document> query = session.createQuery(
                "FROM Document d LEFT JOIN FETCH d.uploader LEFT JOIN FETCH d.event LEFT JOIN FETCH d.project " +
                "WHERE d.documentId = :id",
                Document.class
            );
            query.setParameter("id", id);
            return query.uniqueResultOptional();
        }
    }

    /**
     * Cập nhật tài liệu.
     * @param document Tài liệu cần cập nhật
     * @return Document đã cập nhật
     */
    public Document update(Document document) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Document updated = session.merge(document);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Cập nhật Tài liệu thất bại: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa tài liệu theo ID.
     * @param id ID tài liệu
     * @return true nếu xóa thành công
     */
    public boolean deleteById(Integer id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Document doc = session.get(Document.class, id);
            if (doc != null) {
                session.remove(doc);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Xóa Tài liệu thất bại: " + e.getMessage(), e);
        }
    }
}
