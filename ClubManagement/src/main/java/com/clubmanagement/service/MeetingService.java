package com.clubmanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.clubmanagement.dao.MeetingDAO;
import com.clubmanagement.dto.MeetingDTO;
import com.clubmanagement.entity.Meeting;
import com.clubmanagement.entity.Member;
import com.clubmanagement.util.HibernateUtil;

/**
 * MeetingService - Tang nghiep vu cho Cuoc hop.
 */
public class MeetingService {

    private final MeetingDAO meetingDAO = new MeetingDAO();

    /**
     * Tao cuoc hop moi.
     */
    public MeetingDTO createMeeting(String title, String content,
                                    LocalDateTime startTime, LocalDateTime endTime,
                                    String location, String meetLink, Integer hostId) {
        validate(title, startTime, endTime, location, meetLink, hostId);

        Member host = findMemberById(hostId);
        if (host == null) {
            throw new IllegalArgumentException("Khong tim thay nguoi chu tri");
        }

        Meeting meeting = new Meeting(
            title.trim(),
            content,
            startTime,
            endTime,
            normalize(location),
            normalize(meetLink),
            host
        );
        return toDTO(meetingDAO.save(meeting));
    }

    /**
     * Lay tat ca cuoc hop.
     */
    public List<MeetingDTO> getAllMeetings() {
        return meetingDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Lay cuoc hop theo ID.
     */
    public Optional<MeetingDTO> getMeetingById(Integer id) {
        return meetingDAO.findById(id).map(this::toDTO);
    }

    /**
     * Cap nhat cuoc hop.
     */
    public MeetingDTO updateMeeting(Integer meetingId, String title, String content,
                                    LocalDateTime startTime, LocalDateTime endTime,
                                    String location, String meetLink, Integer hostId) {
        validate(title, startTime, endTime, location, meetLink, hostId);

        Meeting meeting = meetingDAO.findById(meetingId)
            .orElseThrow(() -> new IllegalArgumentException("Khong tim thay cuoc hop"));

        Member host = findMemberById(hostId);
        if (host == null) {
            throw new IllegalArgumentException("Khong tim thay nguoi chu tri");
        }

        meeting.setTitle(title.trim());
        meeting.setContent(content);
        meeting.setStartTime(startTime);
        meeting.setEndTime(endTime);
        meeting.setLocation(normalize(location));
        meeting.setMeetLink(normalize(meetLink));
        meeting.setHost(host);

        return toDTO(meetingDAO.update(meeting));
    }

    /**
     * Xoa cuoc hop.
     */
    public void deleteMeeting(Integer id) {
        if (!meetingDAO.deleteById(id)) {
            throw new IllegalArgumentException("Khong tim thay cuoc hop de xoa");
        }
    }

    // ======================== Helpers ========================

    private void validate(String title, LocalDateTime startTime, LocalDateTime endTime,
                          String location, String meetLink, Integer hostId) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Tieu de khong duoc de trong!");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Thoi gian bat dau va ket thuc khong duoc de trong!");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Thoi gian ket thuc phai sau bat dau!");
        }
        if ((location == null || location.isBlank()) && (meetLink == null || meetLink.isBlank())) {
            throw new IllegalArgumentException("Can dia diem hoac link hop online!");
        }
        if (hostId == null) {
            throw new IllegalArgumentException("Vui long chon nguoi chu tri!");
        }
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private MeetingDTO toDTO(Meeting meeting) {
        return new MeetingDTO(
            meeting.getMeetingId(),
            meeting.getTitle(),
            meeting.getContent(),
            meeting.getStartTime(),
            meeting.getEndTime(),
            meeting.getLocation(),
            meeting.getMeetLink(),
            meeting.getHost() != null ? meeting.getHost().getMemberId() : null,
            meeting.getHost() != null ? meeting.getHost().getFullName() : "N/A"
        );
    }

    private Member findMemberById(Integer memberId) {
        if (memberId == null) return null;
        try (Session session = HibernateUtil.openSession()) {
            return session.get(Member.class, memberId);
        }
    }
}
