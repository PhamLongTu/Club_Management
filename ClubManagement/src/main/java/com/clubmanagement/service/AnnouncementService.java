package com.clubmanagement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.dao.AnnouncementDAO;
import com.clubmanagement.dto.AnnouncementDTO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.entity.Announcement;
import com.clubmanagement.entity.Member;
import com.clubmanagement.entity.Team;
import com.clubmanagement.util.HibernateUtil;

/**
 * AnnouncementService - Tầng nghiệp vụ cho Thông báo.
 */
public class AnnouncementService {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncementService.class);
    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();

    /**
     * Tạo thông báo mới.
     *
     * @param title          Tiêu đề
     * @param content        Nội dung
     * @param isPinned       Ghim hay không
     * @param targetAudience Đối tượng: All / Leaders / Members
     * @param authorId       ID người đăng
     * @return AnnouncementDTO đã lưu
     */
    public AnnouncementDTO createAnnouncement(String title, String content,
                                           Boolean isPinned, String targetAudience,
                                           Integer targetTeamId, Integer authorId) {
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("Tiêu đề thông báo không được để trống!");
        if (content == null || content.isBlank())
            throw new IllegalArgumentException("Nội dung thông báo không được để trống!");

        Member author = findMemberById(authorId);
        Team targetTeam = findTeamById(targetTeamId);
        Announcement ann = new Announcement(title.trim(), content, isPinned, targetAudience, author, targetTeam);
        return toDTO(announcementDAO.save(ann));
    }

    /**
     * Lấy tất cả thông báo (ghim trước, mới nhất trước).
     * @return List<Announcement>
     */
    public List<AnnouncementDTO> getAllAnnouncements() {
        return announcementDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Lấy danh sách thông báo phù hợp với người dùng hiện tại.
     * @param user Người dùng hiện tại
     * @return Danh sách AnnouncementDTO
     */
    public List<AnnouncementDTO> getAnnouncementsForUser(MemberDTO user) {
        return announcementDAO.findForUser(user).stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Lấy N thông báo mới nhất cho Dashboard.
     * @param limit Số lượng
     * @return List<AnnouncementDTO>
     */
    public List<AnnouncementDTO> getLatestAnnouncements(int limit) {
        return announcementDAO.findLatest(limit).stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** Cập nhật thông báo. */
    public AnnouncementDTO updateAnnouncement(Integer id, String title, String content,
                                           Boolean isPinned, String targetAudience,
                                           Integer targetTeamId) {
        Announcement ann = announcementDAO.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông báo!"));

        if (title == null || title.isBlank())
            throw new IllegalArgumentException("Tiêu đề không được để trống!");

        ann.setTitle(title.trim());
        ann.setContent(content);
        ann.setIsPinned(isPinned);
        ann.setTargetAudience(targetAudience);
        ann.setTargetTeam(findTeamById(targetTeamId));
        return toDTO(announcementDAO.update(ann));
    }

    /** Xóa thông báo theo ID. */
    public void deleteAnnouncement(Integer id) {
        if (!announcementDAO.deleteById(id))
            throw new IllegalArgumentException("Không tìm thấy thông báo để xóa!");
    }

    /**
     * Map Announcement entity -> AnnouncementDTO.
     * @param a Announcement entity
     * @return AnnouncementDTO
     */
    private AnnouncementDTO toDTO(Announcement a) {
        if (a == null) return null;
        return new AnnouncementDTO(
            a.getAnnouncementId(),
            a.getTitle(),
            a.getContent(),
            a.getCreatedDate(),
            a.getIsPinned(),
            a.getTargetAudience(),
            a.getAuthor() != null ? a.getAuthor().getFullName() : "Hệ thống",
            a.getTargetTeam() != null ? a.getTargetTeam().getTeamId() : null,
            a.getTargetTeam() != null ? a.getTargetTeam().getTeamName() : null
        );
    }

    /** Tìm Member theo ID. */
    private Member findMemberById(Integer memberId) {
        if (memberId == null) return null;
        try (Session session = HibernateUtil.openSession()) {
            return session.get(Member.class, memberId);
        } catch (Exception e) {
            logger.error("Lỗi khi tìm Member: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Tìm Team theo ID.
     * @param teamId ID ban/nhóm
     * @return Team entity hoặc null
     */
    private Team findTeamById(Integer teamId) {
        if (teamId == null) return null;
        try (Session session = HibernateUtil.openSession()) {
            return session.get(Team.class, teamId);
        } catch (Exception e) {
            logger.error("Lỗi khi tìm Team: {}", e.getMessage());
            return null;
        }
    }
}
