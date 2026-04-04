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

    /** Constructor mặc định. */
    public TeamDTO() {}

    /**
     * Constructor đầy đủ.
     */
    public TeamDTO(Integer teamId, String teamName, String description, 
                   LocalDate createdDate, Integer leaderId, String leaderName) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.description = description;
        this.createdDate = createdDate;
        this.leaderId = leaderId;
        this.leaderName = leaderName;
    }

    /** @return ID nhóm/ban. */
    public Integer getTeamId() { return teamId; }
    /** @param teamId ID nhóm/ban mới. */
    public void setTeamId(Integer teamId) { this.teamId = teamId; }

    /** @return Tên nhóm/ban. */
    public String getTeamName() { return teamName; }
    /** @param teamName Tên nhóm/ban mới. */
    public void setTeamName(String teamName) { this.teamName = teamName; }

    /** @return Mô tả nhóm/ban. */
    public String getDescription() { return description; }
    /** @param description Mô tả mới. */
    public void setDescription(String description) { this.description = description; }

    /** @return Ngày thành lập. */
    public LocalDate getCreatedDate() { return createdDate; }
    /** @param createdDate Ngày thành lập mới. */
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    /** @return ID trưởng ban. */
    public Integer getLeaderId() { return leaderId; }
    /** @param leaderId ID trưởng ban mới. */
    public void setLeaderId(Integer leaderId) { this.leaderId = leaderId; }

    /** @return Tên trưởng ban. */
    public String getLeaderName() { return leaderName; }
    /** @param leaderName Tên trưởng ban mới. */
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }

    /**
     * Hiển thị tên nhóm/ban trong UI.
     * @return Chuỗi hiển thị
     */
    @Override
    public String toString() {
        return teamName;
    }
}
