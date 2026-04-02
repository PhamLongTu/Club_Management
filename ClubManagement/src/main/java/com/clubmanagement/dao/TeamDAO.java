package com.clubmanagement.dao;

import com.clubmanagement.entity.Team;
import com.clubmanagement.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class TeamDAO {

    private static final Logger logger = LoggerFactory.getLogger(TeamDAO.class);

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
