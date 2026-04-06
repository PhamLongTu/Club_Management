package com.clubmanagement.dao;

import java.util.Optional;
import java.util.function.Function;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.util.HibernateUtil;

/**
 * AbstractDAO - Lớp DAO generic dùng chung cho thao tác CRUD cơ bản.
 * Giúp giảm lặp code mở session, begin/commit/rollback transaction.
 */
public abstract class AbstractDAO<T, ID> {

    private final Class<T> entityClass;
    private final Logger logger;

    protected AbstractDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    /**
     * Lưu entity với thông báo lỗi tùy chỉnh.
     */
    protected T saveEntity(T entity, String errorMessage) {
        return executeInTransaction(session -> {
            session.persist(entity);
            return entity;
        }, errorMessage);
    }

    /**
     * Cập nhật entity với thông báo lỗi tùy chỉnh.
     */
    protected T updateEntity(T entity, String errorMessage) {
        return executeInTransaction(session -> session.merge(entity), errorMessage);
    }

    /**
     * Xóa entity theo ID với thông báo lỗi tùy chỉnh.
     */
    protected boolean deleteEntityById(ID id, String errorMessage) {
        return executeInTransaction(session -> {
            T entity = session.get(entityClass, id);
            if (entity == null) {
                return false;
            }
            session.remove(entity);
            return true;
        }, errorMessage);
    }

    /**
     * Tìm entity theo ID đơn giản (không fetch join).
     */
    protected Optional<T> findByIdSimple(ID id, String errorMessage) {
        try (Session session = HibernateUtil.openSession()) {
            return Optional.ofNullable(session.get(entityClass, id));
        } catch (Exception e) {
            logError(errorMessage, e);
            return Optional.empty();
        }
    }

    protected <R> R executeInTransaction(Function<Session, R> work, String errorMessage) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            R result = work.apply(session);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logError(errorMessage, e);
            throw new RuntimeException(buildErrorMessage(errorMessage, e), e);
        }
    }

    private void logError(String errorMessage, Exception e) {
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            logger.error("Lỗi thao tác DB: {}", e.getMessage(), e);
            return;
        }
        logger.error("{}: {}", errorMessage, e.getMessage(), e);
    }

    private String buildErrorMessage(String errorMessage, Exception e) {
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            return "Lỗi thao tác DB: " + e.getMessage();
        }
        return errorMessage + ": " + e.getMessage();
    }
}
