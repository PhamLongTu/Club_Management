package com.clubmanagement.dao;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.entity.Task;
import com.clubmanagement.util.HibernateUtil;

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
                "SELECT DISTINCT t FROM Task t " +
                "LEFT JOIN FETCH t.assignees " +
                "LEFT JOIN FETCH t.assigner " +
                "LEFT JOIN FETCH t.event " +
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
                "SELECT DISTINCT t FROM Task t " +
                "LEFT JOIN FETCH t.assignees " +
                "LEFT JOIN FETCH t.assigner " +
                "LEFT JOIN FETCH t.event " +
                "WHERE t.taskId = :id",
                Task.class
            );
            query.setParameter("id", id);
            return query.uniqueResultOptional();
        }
    }

    public List<Task> findPublicUnassigned() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Task> query = session.createQuery(
                "SELECT DISTINCT t FROM Task t " +
                "LEFT JOIN FETCH t.assignees " +
                "LEFT JOIN FETCH t.assigner " +
                "LEFT JOIN FETCH t.event " +
                "WHERE t.visibility = 'Public' AND t.assignees IS EMPTY " +
                "ORDER BY t.createdDate DESC",
                Task.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi lấy nhiệm vụ public chưa chỉ định: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy nhiệm vụ public chưa chỉ định", e);
        }
    }

    public List<Task> findAssignedToMember(Integer memberId) {
        try (Session session = HibernateUtil.openSession()) {
            Query<Task> query = session.createQuery(
                "SELECT DISTINCT t FROM Task t " +
                "JOIN t.assignees a " +
                "LEFT JOIN FETCH t.assignees " +
                "LEFT JOIN FETCH t.assigner " +
                "LEFT JOIN FETCH t.event " +
                "WHERE a.memberId = :memberId " +
                "ORDER BY t.createdDate DESC",
                Task.class
            );
            query.setParameter("memberId", memberId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi lấy nhiệm vụ của thành viên: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy nhiệm vụ của thành viên", e);
        }
    }

    public List<Task> findPublicAssigned() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Task> query = session.createQuery(
                "SELECT DISTINCT t FROM Task t " +
                "LEFT JOIN FETCH t.assignees " +
                "LEFT JOIN FETCH t.assigner " +
                "LEFT JOIN FETCH t.event " +
                "WHERE t.visibility = 'Public' AND t.assignees IS NOT EMPTY " +
                "ORDER BY t.createdDate DESC",
                Task.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi lấy nhiệm vụ public đã chỉ định: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy nhiệm vụ public đã chỉ định", e);
        }
    }

    public List<Task> findByMember(Integer memberId) {
        return findAssignedToMember(memberId);
    }

    public void addMemberToTask(Integer taskId, Integer memberId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Task task = session.get(Task.class, taskId);
            if (task == null) {
                throw new IllegalArgumentException("Không tìm thấy nhiệm vụ");
            }
            org.hibernate.Hibernate.initialize(task.getAssignees());
            long currentCount = task.getAssignees() != null ? task.getAssignees().size() : 0;
            int maxAssignees = task.getMaxAssignees() != null ? task.getMaxAssignees() : 1;
            if (maxAssignees > 0 && currentCount >= maxAssignees) {
                throw new IllegalStateException("Nhiệm vụ đã đủ người tham gia");
            }

            var member = session.get(com.clubmanagement.entity.Member.class, memberId);
            if (member == null) {
                throw new IllegalArgumentException("Không tìm thấy thành viên");
            }
            if (task.getAssignees() == null) {
                task.setAssignees(new java.util.ArrayList<>());
            }
            if (!task.getAssignees().contains(member)) {
                task.getAssignees().add(member);
            }
            session.merge(task);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể đăng ký nhiệm vụ: " + e.getMessage(), e);
        }
    }

    public void replaceAssignees(Integer taskId, List<Integer> memberIds) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Task task = session.get(Task.class, taskId);
            if (task == null) {
                throw new IllegalArgumentException("Không tìm thấy nhiệm vụ");
            }
            org.hibernate.Hibernate.initialize(task.getAssignees());
            if (task.getAssignees() == null) {
                task.setAssignees(new java.util.ArrayList<>());
            } else {
                task.getAssignees().clear();
            }

            int maxAssignees = task.getMaxAssignees() != null ? task.getMaxAssignees() : 1;
            if (memberIds != null) {
                int count = 0;
                for (Integer memberId : memberIds) {
                    if (memberId == null) continue;
                    if (maxAssignees > 0 && count >= maxAssignees) break;
                    var member = session.get(com.clubmanagement.entity.Member.class, memberId);
                    if (member != null && !task.getAssignees().contains(member)) {
                        task.getAssignees().add(member);
                        count++;
                    }
                }
            }

            session.merge(task);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể cập nhật người thực hiện: " + e.getMessage(), e);
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
