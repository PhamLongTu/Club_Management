package com.clubmanagement.service;

import com.clubmanagement.dao.AttendanceDAO;
import com.clubmanagement.dto.AttendanceDTO;
import com.clubmanagement.entity.Attendance;
import com.clubmanagement.entity.Event;
import com.clubmanagement.entity.Member;
import com.clubmanagement.util.HibernateUtil;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class AttendanceService {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    public AttendanceDTO checkIn(Integer memberId, Integer eventId, LocalDateTime time, String status, String note) {
        if (memberId == null || eventId == null) {
            throw new IllegalArgumentException("Vui lòng chọn Thành viên và Sự kiện!");
        }

        try (Session session = HibernateUtil.openSession()) {
            Member member = session.get(Member.class, memberId);
            Event event = session.get(Event.class, eventId);

            if (member == null || event == null) {
                throw new IllegalArgumentException("Không tìm thấy dữ liệu Thành viên hoặc Sự kiện!");
            }

            Attendance attendance = new Attendance(member, event, time, status);
            attendance.setNote(note);

            return toDTO(attendanceDAO.save(attendance));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi: " + e.getMessage(), e);
        }
    }

    public List<AttendanceDTO> getAllAttendances() {
        return attendanceDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<AttendanceDTO> getAttendancesByEvent(Integer eventId) {
        return attendanceDAO.findByEventId(eventId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public AttendanceDTO updateAttendance(Integer id, LocalDateTime checkIn, LocalDateTime checkOut, String status, String note) {
        Attendance attendance = attendanceDAO.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin Điểm danh!"));

        attendance.setCheckInTime(checkIn);
        attendance.setCheckOutTime(checkOut);
        attendance.setStatus(status);
        attendance.setNote(note);

        return toDTO(attendanceDAO.update(attendance));
    }

    public void deleteAttendance(Integer id) {
        if (!attendanceDAO.deleteById(id)) {
            throw new IllegalArgumentException("Không tìm thấy dữ liệu để xóa!");
        }
    }

    private AttendanceDTO toDTO(Attendance a) {
        if (a == null) return null;
        return new AttendanceDTO(
            a.getAttendanceId(),
            a.getMember() != null ? a.getMember().getMemberId() : null,
            a.getMember() != null ? a.getMember().getFullName() : "Không xác định",
            a.getEvent() != null ? a.getEvent().getEventId() : null,
            a.getEvent() != null ? a.getEvent().getEventName() : "Không xác định",
            a.getCheckInTime(),
            a.getCheckOutTime(),
            a.getStatus(),
            a.getNote()
        );
    }
}
