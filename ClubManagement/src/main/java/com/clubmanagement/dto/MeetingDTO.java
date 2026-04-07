package com.clubmanagement.dto;

import java.time.LocalDateTime;

/**
 * DTO: MeetingDTO (Data Transfer Object cho Cuoc hop)
 */
public class MeetingDTO {

    private Integer meetingId;
    private String title;
    private String content;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String meetLink;
    private Integer hostId;
    private String hostName;

    /** Constructor mac dinh. */
    public MeetingDTO() {}

    /** Constructor day du. */
    public MeetingDTO(Integer meetingId, String title, String content,
                    LocalDateTime startTime, LocalDateTime endTime,
                    String location, String meetLink,
                    Integer hostId, String hostName) {
        this.meetingId = meetingId;
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.meetLink = meetLink;
        this.hostId = hostId;
        this.hostName = hostName;
    }

    public Integer getMeetingId() { return meetingId; }
    public void setMeetingId(Integer meetingId) { this.meetingId = meetingId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getMeetLink() { return meetLink; }
    public void setMeetLink(String meetLink) { this.meetLink = meetLink; }

    public Integer getHostId() { return hostId; }
    public void setHostId(Integer hostId) { this.hostId = hostId; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    @Override
    public String toString() { return title; }
}
