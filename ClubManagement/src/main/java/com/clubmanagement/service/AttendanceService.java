package com.clubmanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.clubmanagement.dao.AttendanceDAO;
import com.clubmanagement.dto.AttendanceDTO;
import com.clubmanagement.entity.Attendance;
import com.clubmanagement.entity.Event;
import com.clubmanagement.entity.Member;
import com.clubmanagement.util.HibernateUtil;

/**
 * AttendanceService - Tầng nghiệp vụ cho Điểm danh.
 */
public class AttendanceService {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    /**
     * Check-in thành viên cho một sự kiện.
     * @param memberId ID thành viên
     * @param eventId ID sự kiện
     * @param time Thời gian check-in
     * @param status Trạng thái điểm danh
     * @param note Ghi chú
     * @return AttendanceDTO đã lưu
     */
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

    /**
     * Lấy tất cả điểm danh.
     * @return Danh sách AttendanceDTO
     */
    public List<AttendanceDTO> getAllAttendances() {
        return attendanceDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Lấy điểm danh theo sự kiện.
     * @param eventId ID sự kiện
     * @return Danh sách AttendanceDTO
     */
    public List<AttendanceDTO> getAttendancesByEvent(Integer eventId) {
        return attendanceDAO.findByEventId(eventId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Cập nhật thông tin điểm danh.
     * @param id ID điểm danh
     * @param checkIn Thời gian check-in
     * @param checkOut Thời gian check-out
     * @param status Trạng thái
     * @param note Ghi chú
     * @return AttendanceDTO đã cập nhật
     */
    public AttendanceDTO updateAttendance(Integer id, LocalDateTime checkIn, LocalDateTime checkOut, String status, String note) {
        Attendance attendance = attendanceDAO.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin Điểm danh!"));

        attendance.setCheckInTime(checkIn);
        attendance.setCheckOutTime(checkOut);
        attendance.setStatus(status);
        attendance.setNote(note);

        return toDTO(attendanceDAO.update(attendance));
    }

    /**
     * Xóa điểm danh theo ID.
     * @param id ID điểm danh
     */
    public void deleteAttendance(Integer id) {
        if (!attendanceDAO.deleteById(id)) {
            throw new IllegalArgumentException("Không tìm thấy dữ liệu để xóa!");
        }
    }

    /**
     * Map Attendance entity -> AttendanceDTO.
     * @param a Attendance entity
     * @return AttendanceDTO
     */
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
