package com.clubmanagement;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clubmanagement.controller.LoginController;
import com.clubmanagement.service.MemberService;
import com.clubmanagement.util.HibernateUtil;
import com.formdev.flatlaf.FlatLightLaf;

/**
 *
 * MainApp - Điểm vào của ứng dụng (Entry Point).
 *
 * Thực hiện theo thứ tự:
 * 1. Cài đặt Look & Feel (FlatLaf cho giao diện hiện đại)
 * 2. Đăng ký Shutdown Hook (đảm bảo Hibernate đóng sạch)
 * 3. Khởi tạo Hibernate SessionFactory (kết nối DB)
 * 4. Tạo LoginView + LoginController
 * 5. Hiển thị màn hình đăng nhập
 */
public class MainApp {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    /**
     * Entry point của ứng dụng.
     *
     * @param args Không dùng command-line arguments
     */
    public static void main(String[] args) {

        // BƯỚC 1: Cài đặt Look & Feel
        try {
            FlatLightLaf.setup();
            // Tùy chỉnh thêm: Font, màu accent, bo góc...
            UIManager.put("defaultFont",              new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
            UIManager.put("Button.arc",               12);
            UIManager.put("Component.arc",            8);
            UIManager.put("TextComponent.arc",        6);
            UIManager.put("ScrollBar.thumbArc",       999);
            UIManager.put("TabbedPane.showTabSeparators", true);
            logger.info("FlatLaf Look & Feel đã được cài đặt.");
        } catch (Exception e) {
            logger.warn("Không thể cài FlatLaf, dùng Look & Feel mặc định: {}", e.getMessage());
            // Fallback: dùng system L&F
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                     | UnsupportedLookAndFeelException ex) {
                logger.warn("Không thể cài System L&F: {}", ex.getMessage());
            }
        }

        // BƯỚC 2: Đăng ký Shutdown Hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Ứng dụng đang tắt, đóng Hibernate...");
            HibernateUtil.shutdown();
            logger.info("Hibernate đã đóng. Tạm biệt!");
        }, "ShutdownHook-Hibernate"));

        // BƯỚC 3: Khởi tạo Hibernate (kết nối database)
        try {
            logger.info("Đang kết nối tới MySQL database...");
            HibernateUtil.getSessionFactory(); // Khởi tạo kết nối
            logger.info("Kết nối database thành công!");
        } catch (Exception e) {
            logger.error("FATAL: Không thể kết nối database!", e);
            // Hiển thị thông báo lỗi cho người dùng
            String message = """
                Không thể kết nối tới database!

                Vui lòng kiểm tra:
                  1. MySQL đang chạy tại localhost:3306
                  2. Database 'club_management' đã được tạo
                  3. Username/Password trong hibernate.cfg.xml

                Chi tiết lỗi: %s
                """.formatted(e.getMessage());
            JOptionPane.showMessageDialog(
                null,
                message,
                "Lỗi kết nối Database",
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(1); // Thoát ứng dụng
        }

        // BƯỚC 4 & 5: Tạo và hiển thị LoginView
        SwingUtilities.invokeLater(() -> {
            logger.info("Đang khởi chạy giao diện ứng dụng...");

            // Tạo View
            com.clubmanagement.view.LoginView loginView = new com.clubmanagement.view.LoginView();

            // Tạo Service
            MemberService memberService = new MemberService();

            // Tạo Controller (kết nối View <-> Service)
            LoginController loginController = new LoginController(loginView, memberService);
            loginView.getRootPane().putClientProperty("controller", loginController);

            // Hiển thị màn hình đăng nhập
            loginView.setVisible(true);

            logger.info("Ứng dụng Club Management System đã khởi động!");
            logger.info("Đăng nhập với: admin@gmail.com / admin123");
        });
    }
}
