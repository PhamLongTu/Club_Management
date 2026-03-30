package com.clubmanagement.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HibernateUtil - Utility class quản lý Hibernate SessionFactory.
 *
 * SessionFactory là object nặng (heavy), chỉ cần tạo một lần khi ứng dụng khởi động
 * và dùng chung trong suốt vòng đời ứng dụng (Singleton pattern).
 *
 * Mỗi thread cần một Session riêng để tương tác với database.
 */
public class HibernateUtil {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

    /** SessionFactory dùng chung cho toàn ứng dụng (Singleton). */
    private static SessionFactory sessionFactory;

    // Static block: chạy một lần khi class được load vào JVM
    static {
        try {
            logger.info("Đang khởi tạo Hibernate SessionFactory...");

            // Đọc file hibernate.cfg.xml và tạo SessionFactory
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")   // load cấu hình từ resources/hibernate.cfg.xml
                    .buildSessionFactory();

            logger.info("Hibernate SessionFactory đã khởi tạo thành công!");
        } catch (Exception e) {
            logger.error("Không thể khởi tạo Hibernate SessionFactory: " + e.getMessage(), e);
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Lấy SessionFactory (Singleton instance).
     * @return SessionFactory đã được khởi tạo
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Mở một Session mới từ SessionFactory.
     * Caller chịu trách nhiệm đóng Session sau khi dùng xong.
     * @return Session mới
     */
    public static Session openSession() {
        return sessionFactory.openSession();
    }

    /**
     * Đóng SessionFactory khi ứng dụng tắt.
     * Gọi phương thức này trong shutdown hook hoặc cuối main().
     */
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            logger.info("Đang đóng Hibernate SessionFactory...");
            sessionFactory.close();
            logger.info("Hibernate SessionFactory đã đóng.");
        }
    }
}
