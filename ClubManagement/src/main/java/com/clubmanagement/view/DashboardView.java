package com.clubmanagement.view;

import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.entity.Announcement;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * DashboardView - Màn hình chính sau khi đăng nhập.
 *
 * Layout:
 * ┌─────────────────────────────────────────────────────┐
 * │  TOPBAR: Logo | Tên user | Nút logout               │
 * ├──────────┬──────────────────────────────────────────┤
 * │          │                                          │
 * │ SIDEBAR  │  CONTENT AREA (thay đổi theo menu)       │
 * │ (menu)   │                                          │
 * │          │                                          │
 * └──────────┴──────────────────────────────────────────┘
 */
public class DashboardView extends JFrame {

    // ====== Sub-panels (content area) ======
    private MemberView    memberView;
    private EventView     eventView;
    private ProjectView   projectView;

    // ====== Components ======
    private JLabel   userNameLabel;
    private JLabel   userRoleLabel;
    private JButton  btnLogout;
    private JPanel   contentArea;    // Vùng thay đổi nội dung
    private CardLayout cardLayout;   // Quản lý chuyển màn hình

    // Stat cards
    private JLabel memberCountLabel;
    private JLabel eventCountLabel;
    private JLabel projectCountLabel;
    private JPanel homePanel;        // Dashboard home

    // Sidebar buttons
    private JButton btnHome;
    private JButton btnMembers;
    private JButton btnEvents;
    private JButton btnProjects;

    // Quick action buttons (Dashboard home)
    private JButton btnQuickAddMember;
    private JButton btnQuickAddEvent;
    private JButton btnQuickAddProject;

    // ====== Colors ======
    private static final Color SIDEBAR_BG  = new Color(15, 23, 42);
    private static final Color SIDEBAR_HOV = new Color(30, 41, 59);
    private static final Color SIDEBAR_SEL = new Color(37, 99, 235);
    private static final Color TOP_BG      = new Color(248, 250, 252);
    private static final Color CONTENT_BG  = new Color(241, 245, 249);
    private static final Color PRIMARY      = new Color(37, 99, 235);
    private static final Color TEXT_DARK   = new Color(15, 23, 42);
    private static final Color TEXT_GRAY   = new Color(100, 116, 139);

    private MemberDTO currentUser;

    public DashboardView(MemberDTO currentUser) {
        this.currentUser = currentUser;
        initUI();
    }

    /**
     * Khởi tạo toàn bộ giao diện Dashboard.
     */
    private void initUI() {
        setTitle("Club Management System - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Khởi động toàn màn hình

        JPanel root = new JPanel(new BorderLayout(0, 0));
        setContentPane(root);

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildContentArea(), BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────
    // TOP BAR
    // ─────────────────────────────────────────────────────

    /** Thanh tiêu đề trên cùng. */
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setPreferredSize(new Dimension(0, 64));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        // Trái: logo
        JLabel logo = new JLabel("  🏫  CLB Manager");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(PRIMARY);

        // Phải: thông tin user + logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightPanel.setOpaque(false);

        userNameLabel = new JLabel(currentUser.getFullName());
        userNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userNameLabel.setForeground(TEXT_DARK);

        userRoleLabel = new JLabel("[" + currentUser.getRoleName() + "]");
        userRoleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userRoleLabel.setForeground(TEXT_GRAY);

        btnLogout = new JButton("Đăng xuất");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(new Color(239, 68, 68));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setPreferredSize(new Dimension(100, 34));

        rightPanel.add(userNameLabel);
        rightPanel.add(userRoleLabel);
        rightPanel.add(btnLogout);

        // Thêm padding bên phải
        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setOpaque(false);
        rightWrapper.setBorder(new EmptyBorder(0, 0, 0, 16));
        rightWrapper.add(rightPanel, BorderLayout.CENTER);

        bar.add(logo, BorderLayout.WEST);
        bar.add(rightWrapper, BorderLayout.EAST);
        return bar;
    }

    // ─────────────────────────────────────────────────────
    // SIDEBAR
    // ─────────────────────────────────────────────────────

    /** Thanh menu bên trái. */
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(16, 0, 16, 0));

        // --- Thông tin user mini ---
        JPanel userCard = new JPanel();
        userCard.setOpaque(false);
        userCard.setLayout(new BoxLayout(userCard, BoxLayout.Y_AXIS));
        userCard.setBorder(new EmptyBorder(8, 20, 20, 20));
        userCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel avatar = new JLabel("👤");
        avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        avatar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameL = new JLabel(currentUser.getFullName());
        nameL.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameL.setForeground(Color.WHITE);
        nameL.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleL = new JLabel(currentUser.getRoleName());
        roleL.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleL.setForeground(new Color(148, 163, 184));
        roleL.setAlignmentX(Component.LEFT_ALIGNMENT);

        userCard.add(avatar);
        userCard.add(Box.createVerticalStrut(8));
        userCard.add(nameL);
        userCard.add(roleL);

        // Đường phân cách
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(51, 65, 85));

        // --- Menu Items ---
        btnHome     = makeSidebarBtn("🏠  Tổng quan",    true);
        btnMembers  = makeSidebarBtn("👥  Thành viên",   false);
        btnEvents   = makeSidebarBtn("📅  Sự kiện",      false);
        btnProjects = makeSidebarBtn("📋  Dự án",        false);

        sidebar.add(userCard);
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnHome);
        sidebar.add(btnMembers);
        sidebar.add(btnEvents);
        sidebar.add(btnProjects);
        sidebar.add(Box.createVerticalGlue());

        // Phiên bản ở dưới cùng
        JLabel versionLabel = new JLabel("  v1.0.0 - 2025");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        versionLabel.setForeground(new Color(71, 85, 105));
        versionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(versionLabel);

        return sidebar;
    }

    /**
     * Tạo nút menu sidebar.
     * @param text     Văn bản nút
     * @param selected Có đang được chọn không
     */
    private JButton makeSidebarBtn(String text, boolean selected) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(selected ? Color.WHITE : new Color(148, 163, 184));
        btn.setBackground(selected ? SIDEBAR_SEL : SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setPreferredSize(new Dimension(220, 46));
        btn.setBorder(new EmptyBorder(0, 20, 0, 20));

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.getBackground() != SIDEBAR_SEL) btn.setBackground(SIDEBAR_HOV);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn.getBackground() != SIDEBAR_SEL) btn.setBackground(SIDEBAR_BG);
            }
        });
        return btn;
    }

    // ─────────────────────────────────────────────────────
    // CONTENT AREA (CardLayout)
    // ─────────────────────────────────────────────────────

    /** Vùng nội dung thay đổi theo menu. */
    private JPanel buildContentArea() {
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(CONTENT_BG);

        // Tạo các sub-views
        homePanel    = buildHomePanel();
        memberView   = new MemberView(currentUser);
        eventView    = new EventView(currentUser);
        projectView  = new ProjectView(currentUser);

        contentArea.add(homePanel,            "HOME");
        contentArea.add(memberView.getPanel(), "MEMBERS");
        contentArea.add(eventView.getPanel(),  "EVENTS");
        contentArea.add(projectView.getPanel(),"PROJECTS");

        return contentArea;
    }

    // ─────────────────────────────────────────────────────
    // HOME PANEL (Dashboard tổng quan)
    // ─────────────────────────────────────────────────────

    /** Tạo màn hình home với các stat cards. */
    private JPanel buildHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // ---- Header ----
        JLabel pageTitle = new JLabel("Tổng quan hệ thống");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        pageTitle.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Xin chào, " + currentUser.getFullName() + "! Đây là tổng quan hoạt động CLB.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_GRAY);

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.add(pageTitle);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);

        // ---- Stat Cards ----
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new EmptyBorder(24, 0, 24, 0));

        memberCountLabel  = new JLabel("...");
        eventCountLabel   = new JLabel("...");
        projectCountLabel = new JLabel("...");

        statsPanel.add(buildStatCard("👥 Thành viên", memberCountLabel,
            "Tổng thành viên đang hoạt động", new Color(37, 99, 235)));
        statsPanel.add(buildStatCard("📅 Sự kiện", eventCountLabel,
            "Sự kiện sắp / đang diễn ra", new Color(16, 185, 129)));
        statsPanel.add(buildStatCard("📋 Dự án", projectCountLabel,
            "Dự án đang triển khai", new Color(245, 158, 11)));

        // ---- Quick Actions ----
        JLabel qaLabel = new JLabel("Truy cập nhanh");
        qaLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        qaLabel.setForeground(TEXT_DARK);

        JPanel qaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        qaPanel.setOpaque(false);

        btnQuickAddMember  = makeQuickBtn("➕ Thêm thành viên");
        btnQuickAddEvent   = makeQuickBtn("📅 Tạo sự kiện");
        btnQuickAddProject = makeQuickBtn("📋 Tạo dự án");

        qaPanel.add(btnQuickAddMember);
        qaPanel.add(btnQuickAddEvent);
        qaPanel.add(btnQuickAddProject);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(qaLabel, BorderLayout.NORTH);
        bottom.add(qaPanel, BorderLayout.CENTER);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Tạo một stat card (thẻ thống kê).
     * @param title       Tiêu đề thẻ
     * @param valueLabel  Label hiển thị số liệu (sẽ được cập nhật sau)
     * @param subtitle    Mô tả phụ
     * @param accentColor Màu accent (thanh màu trái)
     */
    private JPanel buildStatCard(String title, JLabel valueLabel,
                                  String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Vẽ thanh màu bên trái
                g.setColor(accentColor);
                g.fillRect(0, 0, 5, getHeight());
            }
        };
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        valueLabel.setForeground(accentColor);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_DARK);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLabel.setForeground(TEXT_GRAY);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subLabel);

        card.add(textPanel, BorderLayout.CENTER);

        // Hiệu ứng shadow giả
        card.putClientProperty("JComponent.outline", "none");
        return card;
    }

    // ===================================================
    // PUBLIC API (dùng bởi DashboardController)
    // ===================================================

    /** Hiển thị panel Home. */
    public void showHome()     { selectMenu(btnHome);     cardLayout.show(contentArea, "HOME"); }
    /** Hiển thị panel Thành viên. */
    public void showMembers()  { selectMenu(btnMembers);  cardLayout.show(contentArea, "MEMBERS"); }
    /** Hiển thị panel Sự kiện. */
    public void showEvents()   { selectMenu(btnEvents);   cardLayout.show(contentArea, "EVENTS"); }
    /** Hiển thị panel Dự án. */
    public void showProjects() { selectMenu(btnProjects); cardLayout.show(contentArea, "PROJECTS"); }

    /**
     * Cập nhật trạng thái selected cho các nút sidebar.
     * Chỉ một nút được active tại một thời điểm.
     */
    private void selectMenu(JButton selected) {
        for (JButton btn : new JButton[]{btnHome, btnMembers, btnEvents, btnProjects}) {
            boolean isSelected = btn == selected;
            btn.setBackground(isSelected ? SIDEBAR_SEL : SIDEBAR_BG);
            btn.setForeground(isSelected ? Color.WHITE : new Color(148, 163, 184));
        }
    }

    /** Cập nhật số liệu thống kê lên Dashboard. */
    public void updateStats(long members, long events, long projects) {
        memberCountLabel.setText(String.valueOf(members));
        eventCountLabel.setText(String.valueOf(events));
        projectCountLabel.setText(String.valueOf(projects));
    }

    // Getters cho Controller đăng ký sự kiện
    public JButton getBtnHome()     { return btnHome; }
    public JButton getBtnMembers()  { return btnMembers; }
    public JButton getBtnEvents()   { return btnEvents; }
    public JButton getBtnProjects() { return btnProjects; }
    public JButton getBtnLogout()   { return btnLogout; }

    // Quick action button getters
    public JButton getBtnQuickAddMember()  { return btnQuickAddMember; }
    public JButton getBtnQuickAddEvent()   { return btnQuickAddEvent; }
    public JButton getBtnQuickAddProject() { return btnQuickAddProject; }

    public MemberView getMemberView()   { return memberView; }
    public EventView getEventView()     { return eventView; }
    public ProjectView getProjectView() { return projectView; }

    /** Tạo nút quick action với style nhất quán. */
    private JButton makeQuickBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(Color.WHITE);
        btn.setForeground(PRIMARY);
        btn.setBorderPainted(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY, 1, true),
            new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
