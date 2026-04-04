package com.clubmanagement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.dao.TeamDAO;
import com.clubmanagement.dto.TeamDTO;
import com.clubmanagement.entity.Member;
import com.clubmanagement.entity.Team;
import com.clubmanagement.util.HibernateUtil;

/**
 * TeamService - Tầng nghiệp vụ cho Nhóm/Ban.
 */
public class TeamService {

    private static final Logger logger = LoggerFactory.getLogger(TeamService.class);
    private final TeamDAO teamDAO = new TeamDAO();

    /**
     * Tạo nhóm/ban mới.
     * @param teamName Tên nhóm/ban
     * @param description Mô tả
     * @param leaderId ID trưởng ban
     * @return TeamDTO đã lưu
     */
    public TeamDTO createTeam(String teamName, String description, Integer leaderId) {
        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("Tên ban không được để trống!");
        }

        Member leader = findMemberById(leaderId);
        Team team = new Team(teamName.trim(), description, leader);
        return toDTO(teamDAO.save(team));
    }

    /**
     * Lấy tất cả nhóm/ban.
     * @return Danh sách TeamDTO
     */
    public List<TeamDTO> getAllTeams() {
        return teamDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Lấy nhóm/ban theo trưởng ban.
     * @param leaderId ID trưởng ban
     * @return Danh sách TeamDTO
     */
    public List<TeamDTO> getTeamsByLeader(Integer leaderId) {
        if (leaderId == null) return java.util.Collections.emptyList();
        return teamDAO.findAll().stream()
            .filter(t -> t.getLeader() != null && leaderId.equals(t.getLeader().getMemberId()))
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Cập nhật thông tin nhóm/ban.
     * @param teamId ID nhóm/ban
     * @param teamName Tên nhóm/ban
     * @param description Mô tả
     * @param leaderId ID trưởng ban
     * @return TeamDTO đã cập nhật
     */
    public TeamDTO updateTeam(Integer teamId, String teamName, String description, Integer leaderId) {
        Team team = teamDAO.findById(teamId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin Ban!"));

        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("Tên ban không được để trống!");
        }

        team.setTeamName(teamName.trim());
        team.setDescription(description);
        team.setLeader(findMemberById(leaderId));
        
        return toDTO(teamDAO.update(team));
    }

    /**
     * Xóa nhóm/ban theo ID.
     * @param teamId ID nhóm/ban
     */
    public void deleteTeam(Integer teamId) {
        if (!teamDAO.deleteById(teamId)) {
            throw new IllegalArgumentException("Không thể tìm thấy Ban để xóa!");
        }
    }

    /**
     * Tìm Member theo ID.
     * @param memberId ID thành viên
     * @return Member entity hoặc null
     */
    private Member findMemberById(Integer memberId) {
        if (memberId == null) return null;
        try (Session session = HibernateUtil.openSession()) {
            return session.get(Member.class, memberId);
        } catch (Exception e) {
            logger.error("Lỗi khi tìm Member làm trưởng ban: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Map Team entity -> TeamDTO.
     * @param t Team entity
     * @return TeamDTO
     */
    private TeamDTO toDTO(Team t) {
        if (t == null) return null;
        return new TeamDTO(
            t.getTeamId(),
            t.getTeamName(),
            t.getDescription(),
            t.getCreatedDate(),
            t.getLeader() != null ? t.getLeader().getMemberId() : null,
            t.getLeader() != null ? t.getLeader().getFullName() : "Chưa có"
        );
    }
}
