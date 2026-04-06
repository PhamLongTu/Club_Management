package com.clubmanagement.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.clubmanagement.entity.Attendance;
import com.clubmanagement.util.HibernateUtil;

/**
 * AttendanceDAO - Lớp truy cập dữ liệu cho thực thể Attendance (Điểm danh).
 */
public class AttendanceDAO {


    /**
     * Lưu bản ghi điểm danh mới.
     * @param attendance Đối tượng điểm danh
     * @return Attendance đã lưu
     */
    public Attendance save(Attendance attendance) {
        Transaction tx = null;
        try (Session session = HibernateUtil.openSession()) {
            tx = session.beginTransaction();
            session.persist(attendance);
            tx.commit();
            return attendance;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Lỗi lưu điểm danh (Thành viên này có thể đã được điểm danh trong sự kiện): " + e.getMessage(), e);
        }
    }


    /**
     * Lấy điểm danh theo sự kiện.
     * @param eventId ID sự kiện
     * @return Danh sách Attendance
     */
    public List<Attendance> findByEventId(Integer eventId) {
        try (Session session = HibernateUtil.openSession()) {
            Query<Attendance> query = session.createQuery(
                "FROM Attendance a LEFT JOIN FETCH a.member LEFT JOIN FETCH a.event " +
                "WHERE a.event.eventId = :eid ORDER BY a.attendanceId DESC",
                Attendance.class
            );
            query.setParameter("eid", eventId);
            return query.getResultList();
        }
    }

}
