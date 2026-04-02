package com.clubmanagement.service;

import com.clubmanagement.dao.TeamDAO;
import com.clubmanagement.dto.TeamDTO;
import com.clubmanagement.entity.Member;
import com.clubmanagement.entity.Team;
import com.clubmanagement.util.HibernateUtil;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class TeamService {

    private static final Logger logger = LoggerFactory.getLogger(TeamService.class);
    private final TeamDAO teamDAO = new TeamDAO();

    public TeamDTO createTeam(String teamName, String description, Integer leaderId) {
        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("Tên ban không được để trống!");
        }

        Member leader = findMemberById(leaderId);
        Team team = new Team(teamName.trim(), description, leader);
        return toDTO(teamDAO.save(team));
    }

    public List<TeamDTO> getAllTeams() {
        return teamDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

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

    public void deleteTeam(Integer teamId) {
        if (!teamDAO.deleteById(teamId)) {
            throw new IllegalArgumentException("Không thể tìm thấy Ban để xóa!");
        }
    }

    private Member findMemberById(Integer memberId) {
        if (memberId == null) return null;
        try (Session session = HibernateUtil.openSession()) {
            return session.get(Member.class, memberId);
        } catch (Exception e) {
            logger.error("Lỗi khi tìm Member làm trưởng ban: {}", e.getMessage());
            return null;
        }
    }

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
