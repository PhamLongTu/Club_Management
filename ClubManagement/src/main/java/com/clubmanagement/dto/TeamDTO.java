package com.clubmanagement.dto;

import java.time.LocalDate;

/**
 * TeamDTO - Data Transfer Object của Team
 */
public class TeamDTO {
    
    private Integer teamId;
    private String teamName;
    private String description;
    private LocalDate createdDate;
    
    private Integer leaderId;
    private String leaderName;

    public TeamDTO() {}

    public TeamDTO(Integer teamId, String teamName, String description, 
                   LocalDate createdDate, Integer leaderId, String leaderName) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.description = description;
        this.createdDate = createdDate;
        this.leaderId = leaderId;
        this.leaderName = leaderName;
    }

    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public Integer getLeaderId() { return leaderId; }
    public void setLeaderId(Integer leaderId) { this.leaderId = leaderId; }

    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }

    @Override
    public String toString() {
        return teamName;
    }
}
