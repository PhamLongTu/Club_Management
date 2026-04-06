package com.clubmanagement.service;

import java.util.List;
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
     * Lấy điểm danh theo sự kiện.
     * @param eventId ID sự kiện
     * @return Danh sách AttendanceDTO
     */
    public List<AttendanceDTO> getAttendancesByEvent(Integer eventId) {
        return attendanceDAO.findByEventId(eventId).stream().map(this::toDTO).collect(Collectors.toList());
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

            boolean wasAttended = attendance != null;
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
                    session.persist(attendance);
                }
            } else {
                if (attendance != null) {
                    session.remove(attendance);
                } else {
                    tx.commit();
                    return null;
                }
            }

            if (wasAttended != attended) {
                Event event = session.get(Event.class, eventId);
                adjustMemberPointsByEvent(memberId, event, attended ? 1 : -1);
            }

            tx.commit();
            return toDTO(attendance);
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi cập nhật điểm danh: " + e.getMessage(), e);
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
            a.getEvent() != null ? a.getEvent().getEventId() : null
        );
    }
}
