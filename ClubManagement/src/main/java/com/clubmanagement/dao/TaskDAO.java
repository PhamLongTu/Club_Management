package com.clubmanagement.dao;

import com.clubmanagement.entity.Task;
import com.clubmanagement.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class TaskDAO {

    private static final Logger logger = LoggerFactory.getLogger(TaskDAO.class);

    public Task save(Task task) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            session.persist(task);
            tx.commit();
            return task;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể lưu thông tin Nhiệm vụ: " + e.getMessage(), e);
        }
    }

    public List<Task> findAll() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Task> query = session.createQuery(
                "FROM Task t LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.assigner LEFT JOIN FETCH t.event " +
                "ORDER BY t.createdDate DESC",
                Task.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách nhiệm vụ: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách nhiệm vụ", e);
        }
    }

    public Optional<Task> findById(Integer id) {
        try (Session session = HibernateUtil.openSession()) {
            Query<Task> query = session.createQuery(
                "FROM Task t LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.assigner LEFT JOIN FETCH t.event " +
                "WHERE t.taskId = :id",
                Task.class
            );
            query.setParameter("id", id);
            return query.uniqueResultOptional();
        }
    }

    public Task update(Task task) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Task updated = session.merge(task);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể cập nhật nhiệm vụ: " + e.getMessage(), e);
        }
    }

    public boolean deleteById(Integer id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Task task = session.get(Task.class, id);
            if (task != null) {
                session.remove(task);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể xóa nhiệm vụ: " + e.getMessage(), e);
        }
    }
}
