package com.clubmanagement.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * LoginView - Màn hình đăng nhập của ứng dụng.
 *
 * Thiết kế gồm 2 phần:
 * - Bên trái: panel gradient màu xanh với logo/slogan
 * - Bên phải: form đăng nhập (email + password + nút login)
 *
 * View chỉ chứa code UI, KHÔNG có logic xử lý.
 * Logic do LoginController đảm nhận (MVC pattern).
 */
public class LoginView extends JFrame {

    // ====== UI Components ======
    private JTextField  emailField;
    private JPasswordField passwordField;
    private JButton     loginButton;
    private JLabel      statusLabel;    // Hiển thị thông báo lỗi/thành công
    private JCheckBox   showPasswordCb; // Toggle hiện/ẩn mật khẩu

    // ====== Màu sắc design system ======
    /** Màu xanh đậm chủ đạo */
    private static final Color PRIMARY   = new Color(37, 99, 235);
    /** Màu xanh nhạt cho gradient */
    private static final Color PRIMARY_L = new Color(59, 130, 246);
    /** Màu nền trắng */
    private static final Color BG_WHITE  = new Color(248, 250, 252);
    /** Màu chữ tối */
    private static final Color TEXT_DARK = new Color(15, 23, 42);
    /** Màu chữ xám */
    private static final Color TEXT_GRAY = new Color(100, 116, 139);
    /** Màu đỏ cho thông báo lỗi */
    private static final Color ERROR_RED = new Color(220, 38, 38);
    /** Màu xanh lá cho thành công */
    private static final Color SUCCESS   = new Color(22, 163, 74);

    // ====== Fonts ======
    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_INPUT  = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 12);

    /**
     * Creates the login view.
     */
    public LoginView() {
        initUI();
    }

    /**
     * Khởi tạo toàn bộ giao diện.
     * Cấu trúc: JFrame -> contentPane (BorderLayout)
     *           -> leftPanel (gradient) + rightPanel (form)
     */
    private void initUI() {
        setTitle("Club Management System - Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 580);
        setLocationRelativeTo(null);  // Căn giữa màn hình
        setResizable(false);

        // ===== Main layout (2 cột) =====
        JPanel contentPane = new JPanel(new GridLayout(1, 2));
        setContentPane(contentPane);

        contentPane.add(buildLeftPanel());   // Phần trái: branding
        contentPane.add(buildRightPanel());  // Phần phải: form
    }

    /**
     * Tạo panel bên trái với gradient màu xanh + logo + slogan.
     */
    private JPanel buildLeftPanel() {
        // Panel tự vẽ gradient bằng paintComponent()
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                // Bật anti-aliasing cho đẹp hơn
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Vẽ gradient dọc từ trên (PRIMARY) xuống dưới (PRIMARY_L)
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY, 0, getHeight(), PRIMARY_L);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new GridBagLayout());

        // Container chứa nội dung bên trái
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(0, 40, 0, 40));

        // Tên hệ thống
        JLabel titleLabel = new JLabel("CLB Manager", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Slogan
        JLabel sloganLabel = new JLabel("<html><center>Hệ thống quản lý<br>Câu lạc bộ trường học</center></html>", SwingConstants.CENTER);
        sloganLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sloganLabel.setForeground(new Color(186, 230, 253));
        sloganLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Đường kẻ ngang
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(200, 1));
        sep.setForeground(new Color(147, 197, 253, 100));

        // Các tính năng
        String[] features = {"Quản lý thành viên", "Theo dõi sự kiện", "Quản lý dự án", "Thông báo nội bộ"};
        JPanel featuresPanel = new JPanel();
        featuresPanel.setOpaque(false);
        featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
        featuresPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (String feat : features) {
            JLabel fl = new JLabel(feat);
            fl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            fl.setForeground(new Color(219, 234, 254));
            fl.setAlignmentX(Component.CENTER_ALIGNMENT);
            fl.setBorder(new EmptyBorder(3, 0, 3, 0));
            featuresPanel.add(fl);
        }

        inner.add(Box.createVerticalGlue());
        inner.add(Box.createVerticalStrut(16));
        inner.add(titleLabel);
        inner.add(Box.createVerticalStrut(8));
        inner.add(sloganLabel);
        inner.add(Box.createVerticalStrut(24));
        inner.add(sep);
        inner.add(Box.createVerticalStrut(24));
        inner.add(featuresPanel);
        inner.add(Box.createVerticalGlue());

        panel.add(inner);
        return panel;
    }

    /**
     * Tạo panel bên phải chứa form đăng nhập.
     */
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_WHITE);
        panel.setLayout(new GridBagLayout());

        JPanel formBox = new JPanel();
        formBox.setOpaque(false);
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));
        formBox.setBorder(new EmptyBorder(10, 50, 10, 50));

        // ---- Tiêu đề ----
        JLabel welcomeLabel = new JLabel("Chào mừng trở lại!");
        welcomeLabel.setFont(FONT_TITLE);
        welcomeLabel.setForeground(PRIMARY);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel("Đăng nhập vào hệ thống quản lý CLB");
        subLabel.setFont(FONT_SMALL);
        subLabel.setForeground(TEXT_GRAY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ---- Email ----
        JLabel emailLabel = makeLabel("Email");
        emailField = new JTextField();
        styleTextField(emailField, "example@email.com");

        // ---- Password ----
        JLabel passLabel = makeLabel("Mật khẩu");
        passwordField = new JPasswordField();
        styleTextField(passwordField, "Nhập mật khẩu...");

        // Toggle show/hide password
        showPasswordCb = new JCheckBox("Hiện mật khẩu");
        showPasswordCb.setFont(FONT_SMALL);
        showPasswordCb.setForeground(TEXT_GRAY);
        showPasswordCb.setOpaque(false);
        showPasswordCb.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPasswordCb.addActionListener(e -> {
            // Khi check: hiện ký tự; khi uncheck: ẩn bằng bullet
            if (showPasswordCb.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('●');
            }
        });

        // ---- Status label (lỗi/thành công) ----
        statusLabel = new JLabel(" ");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(ERROR_RED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ---- Nút đăng nhập ----
        loginButton = new JButton("ĐĂNG NHẬP");
        loginButton.setFont(FONT_BUTTON);
        loginButton.setBackground(PRIMARY);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        loginButton.setPreferredSize(new Dimension(300, 48));


        // ---- Footer ----
        JLabel footerLabel = new JLabel("2025 Club Management System", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(148, 163, 184));
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ---- Assembly ----
        formBox.add(welcomeLabel);
        formBox.add(Box.createVerticalStrut(4));
        formBox.add(subLabel);
        formBox.add(Box.createVerticalStrut(32));
        formBox.add(emailLabel);
        formBox.add(Box.createVerticalStrut(6));
        formBox.add(emailField);
        formBox.add(Box.createVerticalStrut(16));
        formBox.add(passLabel);
        formBox.add(Box.createVerticalStrut(6));
        formBox.add(passwordField);
        formBox.add(Box.createVerticalStrut(8));
        formBox.add(showPasswordCb);
        formBox.add(Box.createVerticalStrut(8));
        formBox.add(statusLabel);
        formBox.add(Box.createVerticalStrut(16));
        formBox.add(loginButton);
        formBox.add(Box.createVerticalStrut(24));
        formBox.add(footerLabel);

        panel.add(formBox);
        return panel;
    }

    // ===================================================
    // HELPER METHODS
    // ===================================================

    /** Tạo label với font chuẩn và căn trái. */
    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /** Áp dụng style chung cho TextField và PasswordField. */
    private void styleTextField(JComponent field, String placeholder) {
        field.setFont(FONT_INPUT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setPreferredSize(new Dimension(300, 44));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(Color.WHITE);

        // Placeholder text cho JTextField
        if (field instanceof JTextField && !(field instanceof JPasswordField)) {
            JTextField tf = (JTextField) field;
            tf.setForeground(TEXT_GRAY);
            tf.setText(placeholder);
            tf.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) {
                    if (tf.getText().equals(placeholder)) {
                        tf.setText("");
                        tf.setForeground(TEXT_DARK);
                    }
                }
                @Override public void focusLost(java.awt.event.FocusEvent e) {
                    if (tf.getText().isBlank()) {
                        tf.setText(placeholder);
                        tf.setForeground(TEXT_GRAY);
                    }
                }
            });
        }
    }

    // ===================================================
    // PUBLIC API (dùng bởi Controller)
    // ===================================================

    /** Lấy email người dùng nhập. */
    public String getEmail() {
        String txt = emailField.getText().trim();
        return txt.equals("example@email.com") ? "" : txt;
    }

    /** Lấy mật khẩu người dùng nhập. */
    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    /** Hiển thị thông báo lỗi (chữ đỏ). */
    public void showError(String msg) {
        statusLabel.setForeground(ERROR_RED);
        statusLabel.setText(msg);
    }

    /** Hiển thị thông báo thành công (chữ xanh). */
    public void showSuccess(String msg) {
        statusLabel.setForeground(SUCCESS);
        statusLabel.setText(msg);
    }

    /** Xóa thông báo. */
    public void clearStatus() {
        statusLabel.setText(" ");
    }

    /** Thiết lập trạng thái loading cho nút đăng nhập. */
    public void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        loginButton.setText(loading ? "Đang đăng nhập..." : "ĐĂNG NHẬP");
    }

    /** Đăng ký sự kiện khi nhấn nút đăng nhập. */
    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
    }

    /** Đăng ký sự kiện nhấn Enter ở ô email. */
    public void addEmailEnterListener(ActionListener listener) {
        emailField.addActionListener(listener);
    }

    /** Đăng ký sự kiện nhấn Enter ở ô mật khẩu. */
    public void addPasswordEnterListener(ActionListener listener) {
        passwordField.addActionListener(listener);
    }

    /** Đặt focus vào ô mật khẩu. */
    public void focusPasswordField() {
        passwordField.requestFocusInWindow();
    }

    /** Đặt focus vào ô email khi hiển thị form. */
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) emailField.requestFocusInWindow();
    }
}
