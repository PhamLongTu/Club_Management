package com.clubmanagement.dao;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.entity.Member;
import com.clubmanagement.util.HibernateUtil;

/**
 * MemberDAO - Lớp truy cập dữ liệu cho thực thể Member.
 *
 * DAO (Data Access Object) pattern: tách biệt logic truy vấn database
 * ra khỏi business logic (Service), giúp code dễ test và bảo trì.
 *
 * Tất cả các thao tác đọc/ghi database đều thực hiện tại đây.
 */
public class MemberDAO {

    private static final Logger logger = LoggerFactory.getLogger(MemberDAO.class);

    // ===========================================================
    // CREATE
    // ===========================================================

    /**
     * Lưu một thành viên mới vào database.
     *
     * @param member Object Member cần lưu (chưa có ID)
     * @return Member đã lưu (đã có ID do DB tự sinh)
     * @throws RuntimeException nếu có lỗi khi lưu
     */
    public Member save(Member member) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            session.persist(member);   // persist() = INSERT INTO members ...
            tx.commit();
            logger.info("Đã lưu thành viên: {}", member.getFullName());
            return member;
        } catch (Exception e) {
            // Rollback nếu có lỗi để tránh dữ liệu không nhất quán
            if (tx != null) tx.rollback();
            logger.error("Lỗi khi lưu thành viên: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lưu thành viên: " + e.getMessage(), e);
        }
    }

    // ===========================================================
    // READ
    // ===========================================================

    /**
     * Tìm một thành viên theo ID (Primary Key).
     *
     * @param memberId ID của thành viên
     * @return Optional chứa Member nếu tìm thấy, Optional.empty() nếu không
     */
    public Optional<Member> findById(Integer memberId) {
        try (Session session = HibernateUtil.openSession()) {
            // session.get() trả về null nếu không tìm thấy, không ném exception
            Query<Member> query = session.createQuery(
                "SELECT DISTINCT m FROM Member m JOIN FETCH m.role LEFT JOIN FETCH m.teams WHERE m.memberId = :id",
                Member.class
            );
            query.setParameter("id", memberId);
            Member member = query.uniqueResult();
            return Optional.ofNullable(member);
        } catch (Exception e) {
            logger.error("Lỗi khi tìm thành viên ID={}: {}", memberId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Lấy tất cả thành viên, sắp xếp theo tên.
     *
     * @return Danh sách tất cả Member
     */
    public List<Member> findAll() {
        try (Session session = HibernateUtil.openSession()) {
            // HQL (Hibernate Query Language) - tương tự SQL nhưng dùng tên Entity/field
            Query<Member> query = session.createQuery(
                "SELECT DISTINCT m FROM Member m JOIN FETCH m.role LEFT JOIN FETCH m.teams ORDER BY m.fullName",
                Member.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách thành viên: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách thành viên", e);
        }
    }

    /**
     * Tìm thành viên theo email (dùng cho đăng nhập).
     *
     * @param email Email cần tìm
     * @return Optional chứa Member nếu tìm thấy
     */
    public Optional<Member> findByEmail(String email) {
        try (Session session = HibernateUtil.openSession()) {
            // setParameter() chống SQL injection
            Query<Member> query = session.createQuery(
                "SELECT DISTINCT m FROM Member m JOIN FETCH m.role LEFT JOIN FETCH m.teams WHERE m.email = :email",
                Member.class
            );
            query.setParameter("email", email);
            return query.uniqueResultOptional();  // trả về Optional
        } catch (Exception e) {
            logger.error("Lỗi khi tìm thành viên theo email: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Tìm thành viên theo mã sinh viên.
     *
     * @param studentId Mã sinh viên
     * @return Optional<Member>
     */
    public Optional<Member> findByStudentId(String studentId) {
        try (Session session = HibernateUtil.openSession()) {
            Query<Member> query = session.createQuery(
                "SELECT DISTINCT m FROM Member m JOIN FETCH m.role LEFT JOIN FETCH m.teams WHERE m.studentId = :sid",
                Member.class
            );
            query.setParameter("sid", studentId);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            logger.error("Lỗi khi tìm thành viên theo mã SV: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Tìm kiếm thành viên theo từ khóa (tìm trong name, email, studentId).
     *
     * @param keyword Từ khóa tìm kiếm
     * @return Danh sách Member phù hợp
     */
    public List<Member> search(String keyword) {
        try (Session session = HibernateUtil.openSession()) {
            // LIKE với % ở hai đầu = tìm kiếm substring
            String pattern = "%" + keyword.toLowerCase() + "%";
            Query<Member> query = session.createQuery(
                "SELECT DISTINCT m FROM Member m JOIN FETCH m.role LEFT JOIN FETCH m.teams " +
                "WHERE LOWER(m.fullName) LIKE :kw " +
                "   OR LOWER(m.email) LIKE :kw " +
                "   OR LOWER(m.studentId) LIKE :kw " +
                "ORDER BY m.fullName", Member.class
            );
            query.setParameter("kw", pattern);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi tìm kiếm thành viên: {}", e.getMessage());
            throw new RuntimeException("Không thể tìm kiếm thành viên", e);
        }
    }

    /**
     * Đếm tổng số thành viên đang hoạt động.
     *
     * @return Số thành viên Active
     */
    public long countActive() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(m) FROM Member m WHERE m.status = 'Active'", Long.class
            );
            return query.uniqueResult();
        } catch (Exception e) {
            logger.error("Lỗi khi đếm thành viên: {}", e.getMessage());
            return 0;
        }
    }

    // ===========================================================
    // UPDATE
    // ===========================================================

    /**
     * Cập nhật thông tin thành viên đã tồn tại trong database.
     *
     * @param member Member với thông tin đã được chỉnh sửa
     * @return Member sau khi cập nhật
     */
    public Member update(Member member) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            // merge() = UPDATE nếu entity đã có ID, INSERT nếu chưa
            Member updated = session.merge(member);
            tx.commit();
            logger.info("Đã cập nhật thành viên: {}", member.getFullName());
            return updated;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Lỗi khi cập nhật thành viên: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật thành viên: " + e.getMessage(), e);
        }
    }

    // ===========================================================
    // DELETE
    // ===========================================================

    /**
     * Xóa thành viên khỏi database theo ID.
     * Lưu ý: Nên dùng soft delete (status = Inactive) thay vì xóa hoàn toàn
     * để bảo toàn lịch sử hoạt động.
     *
     * @param memberId ID thành viên cần xóa
     * @return true nếu xóa thành công
     */
    public boolean deleteById(Integer memberId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Member member = session.get(Member.class, memberId);
            if (member != null) {
                session.remove(member);  // DELETE FROM members WHERE member_id = ?
                tx.commit();
                logger.info("Đã xóa thành viên ID: {}", memberId);
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Lỗi khi xóa thành viên ID={}: {}", memberId, e.getMessage(), e);
            throw new RuntimeException("Không thể xóa thành viên: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật danh sách ban/nhóm của thành viên.
     *
     * @param memberId ID thành viên
     * @param teamIds  Danh sách ID ban/nhóm mới
     */
    public void replaceTeams(Integer memberId, List<Integer> teamIds) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Member member = session.get(Member.class, memberId);
            if (member == null) {
                throw new IllegalArgumentException("Không tìm thấy thành viên");
            }
            org.hibernate.Hibernate.initialize(member.getTeams());
            if (member.getTeams() == null) {
                member.setTeams(new java.util.ArrayList<>());
            } else {
                member.getTeams().clear();
            }
            if (teamIds != null) {
                for (Integer teamId : teamIds) {
                    if (teamId == null) continue;
                    com.clubmanagement.entity.Team team = session.get(com.clubmanagement.entity.Team.class, teamId);
                    if (team != null && !member.getTeams().contains(team)) {
                        member.getTeams().add(team);
                    }
                }
            }
            session.merge(member);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Lỗi khi cập nhật ban của thành viên: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật ban của thành viên", e);
        }
    }
}
