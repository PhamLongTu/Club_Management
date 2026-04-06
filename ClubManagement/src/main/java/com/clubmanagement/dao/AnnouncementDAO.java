package com.clubmanagement.dao;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.entity.Announcement;
import com.clubmanagement.entity.Team;
import com.clubmanagement.util.HibernateUtil;

/**
 * AnnouncementDAO - Lớp truy cập dữ liệu cho thực thể Announcement (Thông báo).
 */
public class AnnouncementDAO extends AbstractDAO<Announcement, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncementDAO.class);

    public AnnouncementDAO() {
        super(Announcement.class);
    }

    /**
     * Lưu thông báo mới vào database.
     */
    public Announcement save(Announcement announcement) {
        Announcement saved = saveEntity(announcement, "Không thể lưu thông báo");
        logger.info("Đã lưu thông báo: {}", announcement.getTitle());
        return saved;
    }

    /**
     * Lấy tất cả thông báo, ghim lên trước, sau đó sắp theo ngày mới nhất.
     * Logic: isPinned=true lên đầu, sau đó sắp xếp theo createdDate giảm dần.
     */
    public List<Announcement> findAll() {
        try (Session session = HibernateUtil.openSession()) {
            Query<Announcement> query = session.createQuery(
                "FROM Announcement a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.targetTeam " +
                "ORDER BY a.isPinned DESC, a.createdDate DESC",
                Announcement.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách thông báo: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy danh sách thông báo", e);
        }
    }

    /**
     * Tìm thông báo theo ID.
     */
    public Optional<Announcement> findById(Integer id) {
        return findByIdSimple(id, null);
    }

    /**
     * Lấy N thông báo mới nhất (dùng cho Dashboard).
     * @param limit Số lượng thông báo lấy về
     */
    public List<Announcement> findLatest(int limit) {
        try (Session session = HibernateUtil.openSession()) {
            Query<Announcement> query = session.createQuery(
                "FROM Announcement a LEFT JOIN FETCH a.author LEFT JOIN FETCH a.targetTeam " +
                "ORDER BY a.isPinned DESC, a.createdDate DESC",
                Announcement.class
            );
            query.setMaxResults(limit);  // giới hạn số dòng trả về = LIMIT trong SQL
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi lấy thông báo mới nhất: {}", e.getMessage());
            throw new RuntimeException("Không thể lấy thông báo", e);
        }
    }

    /**
     * Cập nhật thông báo.
     */
    public Announcement update(Announcement announcement) {
        return updateEntity(announcement, "Không thể cập nhật thông báo");
    }

    /**
     * Xóa thông báo theo ID.
     */
    public boolean deleteById(Integer id) {
        return deleteEntityById(id, "Không thể xóa thông báo");
    }

    /**
     * Lọc danh sách thông báo phù hợp với quyền và ban/nhóm của người dùng.
     * @param user Người dùng hiện tại
     * @return Danh sách thông báo đã lọc
     */
    public List<Announcement> findForUser(MemberDTO user) {
        try (Session session = HibernateUtil.openSession()) {
            List<Team> teamList = user.getMemberId() != null
                ? session.createQuery(
                    "SELECT t FROM Member m JOIN m.teams t WHERE m.memberId = :mid",
                    Team.class
                ).setParameter("mid", user.getMemberId()).getResultList()
                : Collections.emptyList();

            String teamCondition = teamList.isEmpty()
                ? "a.targetTeam IS NULL"
                : "(a.targetTeam IS NULL OR a.targetTeam IN :teams)";

            Query<Announcement> query = session.createQuery(
                "SELECT DISTINCT a FROM Announcement a " +
                "LEFT JOIN FETCH a.author " +
                "LEFT JOIN FETCH a.targetTeam " +
                "WHERE (a.targetAudience = 'All' " +
                "   OR (a.targetAudience = 'Leaders' AND :perm >= 2) " +
                "   OR (a.targetAudience = 'Members' AND :perm = 1)) " +
                "AND " + teamCondition + " " +
                "ORDER BY a.isPinned DESC, a.createdDate DESC",
                Announcement.class
            );
            query.setParameter("perm", user.getPermissionLevel());
            if (!teamList.isEmpty()) {
                query.setParameter("teams", teamList);
            }
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Lỗi khi lọc thông báo theo người dùng: {}", e.getMessage());
            throw new RuntimeException("Không thể lọc thông báo", e);
        }
    }
}
