package com.clubmanagement.dao;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.entity.Member;
import com.clubmanagement.entity.Project;
import com.clubmanagement.util.HibernateUtil;

/**
 * ProjectDAO - Lớp truy cập dữ liệu cho thực thể Project (Dự án).
 */
public class ProjectDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProjectDAO.class);

    /**
     * Lưu dự án mới vào database.
     * @param project Dự án cần lưu
     * @return Project đã lưu (có ID)
     */
    public Project save(Project project) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            session.persist(project);
            tx.commit();
            logger.info("Đã lưu dự án: {}", project.getProjectName());
            return project;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Lỗi khi lưu dự án: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lưu dự án: " + e.getMessage(), e);
        }
    }

    /**
     * Tìm dự án theo ID.
     * @param projectId ID dự án
     * @return Optional<Project>
     */
    public Optional<Project> findById(Integer projectId) {
        try (Session session = HibernateUtil.openSession()) {
            Project project = session.get(Project.class, projectId);
            if (project != null) {
                org.hibernate.Hibernate.initialize(project.getMembers());
                if (project.getMembers() != null) {
                    project.getMembers().forEach(m -> org.hibernate.Hibernate.initialize(m.getTeams()));
                }
            }
            return Optional.ofNullable(project);
        } catch (Exception e) {
            logger.error("Lỗi khi tìm dự án ID={}: {}", projectId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lấy tất cả dự án sắp xếp theo ngày bắt đầu.
     * @return Danh sách Project
     */
    public List<Project> findAll() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Project> query = session.createQuery(
                "FROM Project p LEFT JOIN FETCH p.manager ORDER BY p.startDate DESC",
                Project.class
            );
            List<Project> results = query.getResultList();
            results.forEach(p -> {
                org.hibernate.Hibernate.initialize(p.getMembers());
                if (p.getMembers() != null) {
                    p.getMembers().forEach(m -> org.hibernate.Hibernate.initialize(m.getTeams()));
                }
            });
            return results;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách dự án: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách dự án", e);
        }
    }

    /**
     * Lấy các dự án theo trạng thái.
     * @param status Trạng thái lọc
     * @return Danh sách Project
     */
    public List<Project> findByStatus(String status) {
        try (Session session = HibernateUtil.openSession()) {
            Query<Project> query = session.createQuery(
                "FROM Project p WHERE p.status = :status ORDER BY p.startDate DESC",
                Project.class
            );
            query.setParameter("status", status);
            List<Project> results = query.getResultList();
            results.forEach(p -> {
                org.hibernate.Hibernate.initialize(p.getMembers());
                if (p.getMembers() != null) {
                    p.getMembers().forEach(m -> org.hibernate.Hibernate.initialize(m.getTeams()));
                }
            });
            return results;
        } catch (Exception e) {
            logger.error("Lỗi khi lọc dự án: {}", e.getMessage());
            throw new RuntimeException("Không thể lọc dự án", e);
        }
    }

    /**
     * Tìm kiếm dự án theo tên hoặc mô tả.
     * @param keyword Từ khóa tìm kiếm
     * @return Danh sách Project phù hợp
     */
    public List<Project> search(String keyword) {
        try (Session session = HibernateUtil.openSession()) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            Query<Project> query = session.createQuery(
                "FROM Project p WHERE LOWER(p.projectName) LIKE :kw " +
                "OR LOWER(p.description) LIKE :kw ORDER BY p.startDate DESC",
                Project.class
            );
            query.setParameter("kw", pattern);
            List<Project> results = query.getResultList();
            results.forEach(p -> {
                org.hibernate.Hibernate.initialize(p.getMembers());
                if (p.getMembers() != null) {
                    p.getMembers().forEach(m -> org.hibernate.Hibernate.initialize(m.getTeams()));
                }
            });
            return results;
        } catch (Exception e) {
            logger.error("Lỗi khi tìm kiếm dự án: {}", e.getMessage());
            throw new RuntimeException("Không thể tìm kiếm dự án", e);
        }
    }

    /**
     * Đếm số dự án đang hoạt động.
     * @return Số dự án Active
     */
    public long countActive() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(p) FROM Project p WHERE p.status = 'Active'", Long.class
            );
            return query.uniqueResult();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Cập nhật thông tin dự án.
     * @param project Project đã chỉnh sửa
     * @return Project sau khi cập nhật
     */
    public Project update(Project project) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Project updated = session.merge(project);
            tx.commit();
            logger.info("Đã cập nhật dự án: {}", project.getProjectName());
            return updated;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Lỗi khi cập nhật dự án: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật dự án: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa dự án theo ID.
     * @param projectId ID dự án
     * @return true nếu thành công
     */
    public boolean deleteById(Integer projectId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Project project = session.get(Project.class, projectId);
            if (project != null) {
                session.remove(project);
                tx.commit();
                logger.info("Đã xóa dự án ID: {}", projectId);
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Lỗi khi xóa dự án: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể xóa dự án: " + e.getMessage(), e);
        }
    }

    /**
     * Thêm một thành viên vào dự án.
     */
    public void addMember(Integer projectId, Integer memberId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Project project = session.get(Project.class, projectId);
            Member member = session.get(Member.class, memberId);
            if (project != null && member != null) {
                org.hibernate.Hibernate.initialize(project.getMembers());
                Integer maxMembersValue = project.getMaxMembers();
                int maxMembers = maxMembersValue != null ? maxMembersValue : 0;
                int current = project.getMembers() != null ? project.getMembers().size() : 0;
                if (maxMembers > 0 && current >= maxMembers) {
                    throw new IllegalStateException("Dự án đã đủ thành viên");
                }
                if (!project.getMembers().contains(member)) {
                    project.getMembers().add(member);
                    session.merge(project);
                    logger.info("Đã thêm thành viên ID: {} vào dự án ID: {}", memberId, projectId);
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Lỗi khi thêm thành viên: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể thêm thành viên vào dự án", e);
        }
    }

    /**
     * Lấy dự án public chưa có thành viên tham gia.
     * @return Danh sách Project
     */
    public List<Project> findPublicUnassigned() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Project> query = session.createQuery(
                "SELECT DISTINCT p FROM Project p " +
                "LEFT JOIN FETCH p.manager " +
                "LEFT JOIN FETCH p.members " +
                "WHERE p.visibility = 'Public' AND p.members IS EMPTY " +
                "ORDER BY p.startDate DESC",
                Project.class
            );
            List<Project> results = query.getResultList();
            results.forEach(p -> {
                if (p.getMembers() != null) {
                    p.getMembers().forEach(m -> org.hibernate.Hibernate.initialize(m.getTeams()));
                }
            });
            return results;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy dự án public chưa chỉ định: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy dự án public chưa chỉ định", e);
        }
    }

    /**
     * Lấy tất cả dự án public.
     * @return Danh sách Project
     */
    public List<Project> findPublic() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Project> query = session.createQuery(
                "SELECT DISTINCT p FROM Project p " +
                "LEFT JOIN FETCH p.manager " +
                "LEFT JOIN FETCH p.members " +
                "WHERE p.visibility = 'Public' " +
                "ORDER BY p.startDate DESC",
                Project.class
            );
            List<Project> results = query.getResultList();
            results.forEach(p -> {
                if (p.getMembers() != null) {
                    p.getMembers().forEach(m -> org.hibernate.Hibernate.initialize(m.getTeams()));
                }
            });
            return results;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy dự án public: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy dự án public", e);
        }
    }

    /**
     * Lấy danh sách dự án mà thành viên đang tham gia.
     * @param memberId ID thành viên
     * @return Danh sách Project
     */
    public List<Project> findByMember(Integer memberId) {
        try (Session session = HibernateUtil.openSession()) {
            Query<Project> query = session.createQuery(
                "SELECT DISTINCT p FROM Project p " +
                "JOIN p.members m " +
                "LEFT JOIN FETCH p.manager " +
                "LEFT JOIN FETCH p.members " +
                "WHERE m.memberId = :memberId " +
                "ORDER BY p.startDate DESC",
                Project.class
            );
            query.setParameter("memberId", memberId);
            List<Project> results = query.getResultList();
            results.forEach(p -> {
                if (p.getMembers() != null) {
                    p.getMembers().forEach(m -> org.hibernate.Hibernate.initialize(m.getTeams()));
                }
            });
            return results;
        } catch (Exception e) {
            logger.error("Lỗi khi lấy dự án của thành viên: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy dự án của thành viên", e);
        }
    }

    /**
     * Xóa một thành viên khỏi dự án.
     */
    public void removeMember(Integer projectId, Integer memberId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Project project = session.get(Project.class, projectId);
            Member member = session.get(Member.class, memberId);
            if (project != null && member != null) {
                org.hibernate.Hibernate.initialize(project.getMembers());
                if (project.getMembers().contains(member)) {
                    project.getMembers().remove(member);
                    session.merge(project);
                    logger.info("Đã xóa thành viên ID: {} khỏi dự án ID: {}", memberId, projectId);
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Lỗi khi xóa thành viên: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể xóa thành viên khỏi dự án", e);
        }
    }

    /**
     * Cập nhật danh sách thành viên tham gia dự án.
     * @param projectId ID dự án
     * @param memberIds Danh sách ID thành viên
     */
    public void replaceMembers(Integer projectId, List<Integer> memberIds) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Project project = session.get(Project.class, projectId);
            if (project == null) {
                throw new IllegalArgumentException("Không tìm thấy dự án");
            }
            org.hibernate.Hibernate.initialize(project.getMembers());
            if (project.getMembers() == null) {
                project.setMembers(new java.util.ArrayList<>());
            } else {
                project.getMembers().clear();
            }

            Integer maxMembersValue = project.getMaxMembers();
            int maxMembers = maxMembersValue != null ? maxMembersValue : 0;
            if (memberIds != null) {
                int count = 0;
                for (Integer memberId : memberIds) {
                    if (memberId == null) continue;
                    if (maxMembers > 0 && count >= maxMembers) break;
                    Member member = session.get(Member.class, memberId);
                    if (member != null && !project.getMembers().contains(member)) {
                        project.getMembers().add(member);
                        count++;
                    }
                }
            }

            session.merge(project);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Lỗi khi cập nhật thành viên dự án: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật thành viên dự án", e);
        }
    }
}
