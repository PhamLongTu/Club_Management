package com.clubmanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.Transaction;

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
    private final MemberService memberService = new MemberService();

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

            LocalDateTime checkInTime = time != null ? time : LocalDateTime.now();
            Attendance attendance = new Attendance(member, event, checkInTime, status);
            attendance.setNote(note);

            AttendanceDTO saved = toDTO(attendanceDAO.save(attendance));
            if ("Present".equalsIgnoreCase(status)) {
                adjustMemberPointsByEvent(memberId, event, 1);
            }
            return saved;
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
     * Lấy điểm danh theo ID.
     * @param attendanceId ID điểm danh
     * @return Optional<AttendanceDTO>
     */
    public Optional<AttendanceDTO> getAttendanceById(Integer attendanceId) {
        if (attendanceId == null) return Optional.empty();
        return attendanceDAO.findById(attendanceId).map(this::toDTO);
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

        String oldStatus = attendance.getStatus();
        boolean wasPresent = "Present".equalsIgnoreCase(oldStatus);
        boolean isPresent = "Present".equalsIgnoreCase(status);

        attendance.setCheckInTime(checkIn);
        attendance.setCheckOutTime(checkOut);
        attendance.setStatus(status);
        attendance.setNote(note);

        AttendanceDTO updated = toDTO(attendanceDAO.update(attendance));
        if (wasPresent != isPresent) {
            Event event = attendance.getEvent();
            Integer memberId = attendance.getMember() != null ? attendance.getMember().getMemberId() : null;
            adjustMemberPointsByEvent(memberId, event, isPresent ? 1 : -1);
        }
        return updated;
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
     * Cập nhật trạng thái điểm danh theo checkbox.
     * @param eventId ID sự kiện
     * @param memberId ID thành viên
     * @param attended true nếu đã điểm danh
     * @return AttendanceDTO sau cập nhật hoặc null nếu không có thay đổi
     */
    public AttendanceDTO setAttendanceStatus(Integer eventId, Integer memberId, boolean attended) {
        if (eventId == null || memberId == null) {
            throw new IllegalArgumentException("Thiếu thông tin điểm danh!");
        }

        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();

            Attendance attendance = session.createQuery(
                "FROM Attendance a LEFT JOIN FETCH a.member LEFT JOIN FETCH a.event " +
                "WHERE a.event.eventId = :eid AND a.member.memberId = :mid",
                Attendance.class
            ).setParameter("eid", eventId)
             .setParameter("mid", memberId)
             .uniqueResult();

            String oldStatus = attendance != null ? attendance.getStatus() : null;
            boolean wasPresent = "Present".equalsIgnoreCase(oldStatus);

            if (attended) {
                if (attendance == null) {
                    Member member = session.get(Member.class, memberId);
                    Event event = session.get(Event.class, eventId);
                    if (member == null || event == null) {
                        throw new IllegalArgumentException("Không tìm thấy dữ liệu Thành viên hoặc Sự kiện!");
                    }
                    attendance = new Attendance();
                    attendance.setMember(member);
                    attendance.setEvent(event);
                    attendance.setStatus("Present");
                    session.persist(attendance);
                } else {
                    attendance.setStatus("Present");
                    session.merge(attendance);
                }
            } else {
                if (attendance == null) {
                    tx.commit();
                    return null;
                }
                attendance.setStatus("Absent");
                session.merge(attendance);
            }

            boolean isPresent = attended;
            if (wasPresent != isPresent) {
                applyEventPoints(attendance.getMember(), attendance.getEvent(), isPresent ? 1 : -1);
            }

            tx.commit();
            return toDTO(attendance);
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi cập nhật điểm danh: " + e.getMessage(), e);
        }
    }

    private void applyEventPoints(Member member, Event event, int sign) {
        if (member == null || event == null || sign == 0) return;
        String type = event.getPointType();
        if (type == null || "None".equalsIgnoreCase(type)) return;
        int value = safePoints(event.getPointValue()) * sign;
        if (value == 0) return;
        if ("DRL".equalsIgnoreCase(type)) {
            member.setDrlPoints(safeAdjust(member.getDrlPoints(), value));
        } else if ("CTXH".equalsIgnoreCase(type)) {
            member.setCtxhPoints(safeAdjust(member.getCtxhPoints(), value));
        }
    }

    private void adjustMemberPointsByEvent(Integer memberId, Event event, int sign) {
        if (memberId == null || event == null || sign == 0) return;
        String type = event.getPointType();
        if (type == null || "None".equalsIgnoreCase(type)) return;
        int value = safePoints(event.getPointValue()) * sign;
        if (value == 0) return;
        if ("DRL".equalsIgnoreCase(type)) {
            memberService.adjustPoints(memberId, value, 0, 0);
        } else if ("CTXH".equalsIgnoreCase(type)) {
            memberService.adjustPoints(memberId, 0, value, 0);
        }
    }

    private int safePoints(Integer value) {
        return value != null ? value : 0;
    }

    private Integer safeAdjust(Integer current, int delta) {
        int base = current != null ? current : 0;
        int updated = base + delta;
        return Math.max(0, updated);
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
