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

/**
 * TaskDAO - Lớp truy cập dữ liệu cho thực thể Task (Nhiệm vụ).
 */
public class TaskDAO extends AbstractDAO<Task, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(TaskDAO.class);

    public TaskDAO() {
        super(Task.class);
    }

    /**
     * Lưu nhiệm vụ mới.
     * @param task Nhiệm vụ cần lưu
     * @return Task đã lưu
     */
    public Task save(Task task) {
        return saveEntity(task, "Không thể lưu thông tin Nhiệm vụ");
    }

    /**
     * Lấy tất cả nhiệm vụ, sắp theo ngày tạo giảm dần.
     * @return Danh sách Task
     */
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

    /**
     * Tìm nhiệm vụ theo ID.
     * @param id ID nhiệm vụ
     * @return Optional<Task>
     */
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

    /**
     * Lấy nhiệm vụ public chưa có người nhận.
     * @return Danh sách Task
     */
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

    /**
     * Lấy nhiệm vụ mà thành viên đang tham gia.
     * @param memberId ID thành viên
     * @return Danh sách Task
     */
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

    /**
     * Lấy nhiệm vụ public đã có người nhận.
     * @return Danh sách Task
     */
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

    /**
     * Alias cho findAssignedToMember.
     * @param memberId ID thành viên
     * @return Danh sách Task
     */
    public List<Task> findByMember(Integer memberId) {
        return findAssignedToMember(memberId);
    }

    /**
     * Thêm thành viên vào nhiệm vụ (có kiểm tra giới hạn).
     * @param taskId ID nhiệm vụ
     * @param memberId ID thành viên
     */
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
            Integer maxAssigneesValue = task.getMaxAssignees();
            int maxAssignees = maxAssigneesValue != null ? maxAssigneesValue : 1;
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

    /**
     * Xóa thành viên khỏi nhiệm vụ.
     * @param taskId ID nhiệm vụ
     * @param memberId ID thành viên
     */
    public void removeMemberFromTask(Integer taskId, Integer memberId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Task task = session.get(Task.class, taskId);
            if (task == null) {
                throw new IllegalArgumentException("Không tìm thấy nhiệm vụ");
            }
            var member = session.get(com.clubmanagement.entity.Member.class, memberId);
            if (member == null) {
                throw new IllegalArgumentException("Không tìm thấy thành viên");
            }
            org.hibernate.Hibernate.initialize(task.getAssignees());
            if (task.getAssignees() != null && task.getAssignees().contains(member)) {
                task.getAssignees().remove(member);
                session.merge(task);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể hủy đăng ký nhiệm vụ: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật danh sách người thực hiện của nhiệm vụ.
     * @param taskId ID nhiệm vụ
     * @param memberIds Danh sách ID thành viên
     */
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

            Integer maxAssigneesValue = task.getMaxAssignees();
            int maxAssignees = maxAssigneesValue != null ? maxAssigneesValue : 1;
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

    /**
     * Cập nhật nhiệm vụ.
     * @param task Nhiệm vụ cần cập nhật
     * @return Task đã cập nhật
     */
    public Task update(Task task) {
        return updateEntity(task, "Không thể cập nhật nhiệm vụ");
    }

    /**
     * Xóa nhiệm vụ theo ID.
     * @param id ID nhiệm vụ
     * @return true nếu xóa thành công
     */
    public boolean deleteById(Integer id) {
        return deleteEntityById(id, "Không thể xóa nhiệm vụ");
    }
}
