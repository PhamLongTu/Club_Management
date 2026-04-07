package com.clubmanagement.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.util.ImageUtil;

/**
 * Màn hình hiển thị hồ sơ cá nhân và các mục liên quan.
 */
public class MyInfoView {

    private static final String[] INFO_COLUMNS = {"Trường", "Dữ liệu"};
    private static final String[] TASK_COLUMNS = {"ID", "Nhiệm vụ", "Hạn chót"};
    private static final String[] EVENT_COLUMNS = {"ID", "Sự kiện", "Bắt đầu", "Kết thúc"};
    private static final String[] PROJECT_COLUMNS = {"ID", "Dự án", "Bắt đầu", "Kết thúc"};

    private JPanel mainPanel;
    private JLabel avatarLabel;
    private JLabel nameLabel;
    private JLabel roleLabel;
    private JTable infoTable;
    private DefaultTableModel infoModel;
    private JButton btnEditProfile;
    private JButton btnRefresh;

    private JTable taskTable;
    private JTable eventTable;
    private JTable projectTable;
    private DefaultTableModel taskModel;
    private DefaultTableModel eventModel;
    private DefaultTableModel projectModel;
    private JComboBox<String> taskFilter;
    private JComboBox<String> eventFilter;
    private JComboBox<String> projectFilter;
    private JLabel taskCountLabel;
    private JLabel eventCountLabel;
    private JLabel projectCountLabel;

    private static final Color PRIMARY     = new Color(37, 99, 235);
    private static final Color BG          = new Color(241, 245, 249);
    private static final Color TEXT_DARK   = new Color(15, 23, 42);
    private static final Color TEXT_GRAY   = new Color(100, 116, 139);
    private static final Color ACCENT      = new Color(99, 102, 241);
    private static final Color ACCENT_SOFT = new Color(224, 231, 255);
    private static final Color ACCENT_ROW  = new Color(238, 242, 255);
    private static final int AVATAR_SIZE   = 200;

    /**
     * Khởi tạo giao diện và nạp thông tin hồ sơ ban đầu.
     *
     * @param currentUser thành viên đang đăng nhập
     */
    public MyInfoView(MemberDTO currentUser) {
        buildUI();
        setProfileInfo(currentUser);
    }

    /**
     * Xây dựng bố cục chính.
     */
    private void buildUI() {
        mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(BG);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        mainPanel.add(buildHeader(), BorderLayout.NORTH);
        mainPanel.add(buildContent(), BorderLayout.CENTER);
    }

    /**
     * Xây dựng phần tiêu đề kèm thông tin hồ sơ và thao tác.
     *
     * @return panel tiêu đề
     */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel infoBlock = new JPanel(new BorderLayout(16, 0));
        infoBlock.setOpaque(false);

        avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        avatarLabel.setMinimumSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        avatarLabel.setMaximumSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        avatarLabel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));

        nameLabel = new JLabel(" ");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(ACCENT);

        roleLabel = new JLabel(" ");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(TEXT_GRAY);

        textBlock.add(nameLabel);
        textBlock.add(Box.createVerticalStrut(4));
        textBlock.add(roleLabel);

        infoModel = new DefaultTableModel(INFO_COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        infoTable = new JTable(infoModel);
        infoTable.setRowHeight(26);
        infoTable.setShowGrid(false);
        infoTable.setIntercellSpacing(new Dimension(0, 0));
        infoTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoTable.setBackground(Color.WHITE);
        infoTable.setSelectionBackground(new Color(226, 232, 240));

        JTableHeader infoHeader = infoTable.getTableHeader();
        infoHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        infoHeader.setBackground(ACCENT_SOFT);
        infoHeader.setForeground(new Color(71, 85, 105));
        infoHeader.setPreferredSize(new Dimension(0, 32));

        infoTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        infoTable.getColumnModel().getColumn(1).setPreferredWidth(260);

        infoTable.setPreferredScrollableViewportSize(new Dimension(320, 200));

        JScrollPane infoScroll = new JScrollPane(infoTable);
        infoScroll.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
        infoScroll.setPreferredSize(new Dimension(320, 200));
        infoScroll.setMinimumSize(new Dimension(320, 180));

        JPanel infoPanel = new JPanel(new BorderLayout(12, 0));
        infoPanel.setOpaque(false);
        infoPanel.add(textBlock, BorderLayout.NORTH);
        infoPanel.add(infoScroll, BorderLayout.CENTER);

        infoBlock.add(avatarLabel, BorderLayout.WEST);
        infoBlock.add(infoPanel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        btnEditProfile = new JButton("Chỉnh sửa");
        btnEditProfile.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEditProfile.setBackground(PRIMARY);
        btnEditProfile.setForeground(Color.WHITE);
        btnEditProfile.setBorderPainted(false);
        btnEditProfile.setFocusPainted(false);
        btnEditProfile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEditProfile.setPreferredSize(new Dimension(110, 34));

        btnRefresh = new JButton("Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setBackground(new Color(100, 116, 139));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.setPreferredSize(new Dimension(100, 34));

        actions.add(btnRefresh);
        actions.add(btnEditProfile);

        header.add(infoBlock, BorderLayout.CENTER);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    /**
     * Xây dựng lưới nội dung gồm nhiệm vụ, sự kiện và dự án.
     *
     * @return panel nội dung
     */
    private JPanel buildContent() {
        JPanel grid = new JPanel(new GridLayout(1, 3, 16, 0));
        grid.setOpaque(false);

        taskModel = new DefaultTableModel(TASK_COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        taskTable = new JTable(taskModel);
        styleTable(taskTable);
        hideIdColumn(taskTable, 0);

        eventModel = new DefaultTableModel(EVENT_COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        eventTable = new JTable(eventModel);
        styleTable(eventTable);
        hideIdColumn(eventTable, 0);

        projectModel = new DefaultTableModel(PROJECT_COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        projectTable = new JTable(projectModel);
        styleTable(projectTable);
        hideIdColumn(projectTable, 0);

        taskFilter = new JComboBox<>(new String[]{"Đã đăng ký", "Đang tham gia", "Đã tham gia"});
        eventFilter = new JComboBox<>(new String[]{"Đã đăng ký", "Đang tham gia", "Đã tham gia"});
        projectFilter = new JComboBox<>(new String[]{"Đã đăng ký", "Đang tham gia", "Đã tham gia"});

        taskCountLabel = new JLabel("0");
        eventCountLabel = new JLabel("0");
        projectCountLabel = new JLabel("0");

        grid.add(buildSection("Nhiệm vụ của tôi", taskFilter, taskCountLabel, taskTable));
        grid.add(buildSection("Sự kiện của tôi", eventFilter, eventCountLabel, eventTable));
        grid.add(buildSection("Dự án của tôi", projectFilter, projectCountLabel, projectTable));
        return grid;
    }

    /**
     * Xây dựng một khối gồm bộ lọc và bảng dữ liệu.
     *
     * @param title tiêu đề khối
     * @param filter bộ lọc
     * @param countLabel nhãn tổng số
     * @param table bảng dữ liệu
     * @return panel của khối
     */
    private JPanel buildSection(String title, JComboBox<String> filter,
                                JLabel countLabel, JTable table) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(ACCENT);

        JLabel countText = new JLabel("Tổng: ");
        countText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countText.setForeground(TEXT_GRAY);
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        countLabel.setForeground(TEXT_DARK);

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(titleLabel);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(countText);
        right.add(countLabel);
        right.add(new JLabel("|"));
        right.add(filter);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Áp dụng style đồng nhất cho bảng.
     *
     * @param table bảng cần style
     */
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT_DARK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(ACCENT_SOFT);
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 36));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : ACCENT_ROW);
                    setForeground(TEXT_DARK);
                }
                if (col == 1) {
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    setForeground(PRIMARY);
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 12));
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    /**
     * Ẩn cột id bằng cách đưa chiều rộng về 0.
     *
     * @param table bảng cần cập nhật
     * @param index vị trí cột cần ẩn
     */
    private void hideIdColumn(JTable table, int index) {
        table.getColumnModel().getColumn(index).setMinWidth(0);
        table.getColumnModel().getColumn(index).setMaxWidth(0);
        table.getColumnModel().getColumn(index).setWidth(0);
    }

    /**
     * Cập nhật thông tin hồ sơ.
     *
     * @param member dữ liệu thành viên
     */
    public void setProfileInfo(MemberDTO member) {
        if (member == null) return;
        String initials = ImageUtil.buildInitials(member.getFullName());
        avatarLabel.setIcon(ImageUtil.loadSquareAvatar(
            member.getAvatarUrl(), AVATAR_SIZE, initials, new Color(226, 232, 240), TEXT_DARK
        ));
        nameLabel.setText(member.getFullName());
        roleLabel.setText(member.getRoleName());

        infoModel.setRowCount(0);
        addInfoRow("Họ và tên", member.getFullName());
        addInfoRow("Mã SV", member.getStudentId());
        addInfoRow("Email", member.getEmail());
        addInfoRow("SĐT", member.getPhone());
        addInfoRow("Giới tính", member.getGender());
        addInfoRow("Ngày sinh", member.getBirthDate() != null ? member.getBirthDate().toString() : "");
        addInfoRow("Ngày vào", member.getJoinDate() != null ? member.getJoinDate().toString() : "");
        addInfoRow("Vai trò", member.getRoleName());
        addInfoRow("Ban/Nhóm", member.getTeamNames());
    }

    /**
     * Thêm một dòng nhãn vào bảng thông tin.
     *
     * @param label nhãn hiển thị
     * @param value giá trị
     */
    private void addInfoRow(String label, String value) {
        infoModel.addRow(new Object[]{label, value != null ? value : ""});
    }

    /**
     * Gán dữ liệu cho bảng nhiệm vụ.
     *
     * @param rows danh sách dòng nhiệm vụ
     */
    public void setTaskRows(List<Object[]> rows) {
        taskModel.setRowCount(0);
        for (Object[] row : rows) taskModel.addRow(row);
        taskCountLabel.setText(String.valueOf(rows.size()));
    }

    /**
     * Gán dữ liệu cho bảng sự kiện.
     *
     * @param rows danh sách dòng sự kiện
     */
    public void setEventRows(List<Object[]> rows) {
        eventModel.setRowCount(0);
        for (Object[] row : rows) eventModel.addRow(row);
        eventCountLabel.setText(String.valueOf(rows.size()));
    }

    /**
     * Gán dữ liệu cho bảng dự án.
     *
     * @param rows danh sách dòng dự án
     */
    public void setProjectRows(List<Object[]> rows) {
        projectModel.setRowCount(0);
        for (Object[] row : rows) projectModel.addRow(row);
        projectCountLabel.setText(String.valueOf(rows.size()));
    }

    /**
     * Lấy id nhiệm vụ được chọn.
     *
     * @return id được chọn hoặc null
     */
    public Integer getSelectedTaskId() {
        return getSelectedId(taskTable, taskModel);
    }

    /**
     * Lấy id sự kiện được chọn.
     *
     * @return id được chọn hoặc null
     */
    public Integer getSelectedEventId() {
        return getSelectedId(eventTable, eventModel);
    }

    /**
     * Lấy id dự án được chọn.
     *
     * @return id được chọn hoặc null
     */
    public Integer getSelectedProjectId() {
        return getSelectedId(projectTable, projectModel);
    }

    /**
     * Lấy id đang chọn từ bảng và model.
     *
     * @param table bảng cần đọc
     * @param model model của bảng
     * @return id được chọn hoặc null
     */
    private Integer getSelectedId(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return (Integer) model.getValueAt(row, 0);
    }

    /** @return panel chính */
    public JPanel getPanel() { return mainPanel; }
    /** @return nút chỉnh sửa hồ sơ */
    public JButton getBtnEditProfile() { return btnEditProfile; }
    /** @return nút làm mới */
    public JButton getBtnRefresh() { return btnRefresh; }
    /** @return combobox lọc nhiệm vụ */
    public JComboBox<String> getTaskFilter() { return taskFilter; }
    /** @return combobox lọc sự kiện */
    public JComboBox<String> getEventFilter() { return eventFilter; }
    /** @return combobox lọc dự án */
    public JComboBox<String> getProjectFilter() { return projectFilter; }
    /** @return bảng nhiệm vụ */
    public JTable getTaskTable() { return taskTable; }
    /** @return bảng sự kiện */
    public JTable getEventTable() { return eventTable; }
    /** @return bảng dự án */
    public JTable getProjectTable() { return projectTable; }
}
