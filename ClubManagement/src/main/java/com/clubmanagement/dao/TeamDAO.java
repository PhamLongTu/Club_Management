package com.clubmanagement.dao;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.entity.Team;
import com.clubmanagement.util.HibernateUtil;

/**
 * TeamDAO - Lớp truy cập dữ liệu cho thực thể Team (Nhóm/Ban).
 */
public class TeamDAO {

    private static final Logger logger = LoggerFactory.getLogger(TeamDAO.class);

    /**
     * Lưu nhóm/ban mới.
     * @param team Nhóm/ban cần lưu
     * @return Team đã lưu
     */
    public Team save(Team team) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            session.persist(team);
            tx.commit();
            return team;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể lưu thông tin Ban: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy tất cả nhóm/ban.
     * @return Danh sách Team
     */
    public List<Team> findAll() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Team> query = session.createQuery(
                "FROM Team t LEFT JOIN FETCH t.leader ORDER BY t.teamName ASC",
                Team.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách Ban: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách Ban", e);
        }
    }

    /**
     * Tìm nhóm/ban theo ID.
     * @param id ID nhóm/ban
     * @return Optional<Team>
     */
    public Optional<Team> findById(Integer id) {
        try (Session session = HibernateUtil.openSession()) {
            Query<Team> query = session.createQuery(
                "FROM Team t LEFT JOIN FETCH t.leader WHERE t.teamId = :id",
                Team.class
            );
            query.setParameter("id", id);
            return query.uniqueResultOptional();
        }
    }

    /**
     * Cập nhật nhóm/ban.
     * @param team Nhóm/ban cần cập nhật
     * @return Team đã cập nhật
     */
    public Team update(Team team) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Team updated = session.merge(team);
            tx.commit();
            return updated;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể cập nhật Ban: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa nhóm/ban theo ID.
     * @param id ID nhóm/ban
     * @return true nếu xóa thành công
     */
    public boolean deleteById(Integer id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            Team team = session.get(Team.class, id);
            if (team != null) {
                session.remove(team);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Không thể xóa Ban: " + e.getMessage(), e);
        }
    }
}
