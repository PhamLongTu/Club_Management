package com.clubmanagement.view;

import com.clubmanagement.dto.MemberDTO;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * MemberView - Màn hình quản lý Thành viên.
 *
 * Gồm:
 * - Toolbar: thanh tìm kiếm + nút thêm/sửa/xóa
 * - JTable: hiển thị danh sách thành viên
 * - Trạng thái phân biệt màu (Active=xanh, Inactive=đỏ, Suspended=cam)
 *
 * View KHÔNG chứa logic. Mọi sự kiện đẩy lên MemberController.
 */
public class MemberView {

    // ====== Table columns ======
    private static final String[] COLUMNS = {
        "ID", "Họ và tên", "Mã SV", "Email", "SĐT", "Giới tính", "Ngày vào", "Trạng thái", "Vai trò"
    };

    // ====== Components ======
    private JPanel      mainPanel;
    private JTextField  searchField;
    private JComboBox<String> statusFilter;
    private JButton     btnAdd, btnEdit, btnDelete, btnRefresh, btnSearch;
    private JTable      memberTable;
    private DefaultTableModel tableModel;
    private JLabel      countLabel;
    private JLabel      statusBar;

    // ====== Colors ======
    private static final Color PRIMARY     = new Color(37, 99, 235);
    private static final Color SUCCESS_CLR = new Color(16, 185, 129);
    private static final Color DANGER_CLR  = new Color(239, 68, 68);
    private static final Color WARNING_CLR = new Color(245, 158, 11);
    private static final Color BG          = new Color(241, 245, 249);
    private static final Color TEXT_DARK   = new Color(15, 23, 42);
    private static final Color TEXT_GRAY   = new Color(100, 116, 139);

    private final MemberDTO currentUser;

    public MemberView(MemberDTO currentUser) {
        this.currentUser = currentUser;
        buildUI();
    }

    /**
     * Xây dựng giao diện màn hình thành viên.
     */
    private void buildUI() {
        mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(BG);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        mainPanel.add(buildHeaderArea(), BorderLayout.NORTH);
        mainPanel.add(buildTableArea(),  BorderLayout.CENTER);
        mainPanel.add(buildStatusBar(),  BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────────
    // Header: tiêu đề + toolbar
    // ─────────────────────────────────────────────────────

    private JPanel buildHeaderArea() {
        JPanel panel = new JPanel(new BorderLayout(16, 8));
        panel.setOpaque(false);

        // Tiêu đề
        JLabel title = new JLabel("👥 Quản lý Thành viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);

        panel.add(title, BorderLayout.NORTH);
        panel.add(buildToolbar(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setOpaque(true);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));
        toolbar.setBackground(Color.WHITE);

        // ---- Bên trái: tìm kiếm ----
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(250, 34));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        searchField.setToolTipText("Tìm theo tên, email, mã SV...");

        btnSearch = makeBtn("🔍 Tìm", PRIMARY, Color.WHITE);

        statusFilter = new JComboBox<>(new String[]{"Tất cả", "Active", "Inactive", "Suspended"});
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.setPreferredSize(new Dimension(130, 34));

        searchPanel.add(new JLabel("Tìm kiếm: "));
        searchPanel.add(searchField);
        searchPanel.add(btnSearch);
        searchPanel.add(Box.createHorizontalStrut(8));
        searchPanel.add(new JLabel("Trạng thái: "));
        searchPanel.add(statusFilter);

        // ---- Bên phải: CRUD buttons ----
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        btnRefresh = makeBtn("🔄 Làm mới",      new Color(100,116,139), Color.WHITE);
        btnAdd     = makeBtn("➕ Thêm",          SUCCESS_CLR,             Color.WHITE);
        btnEdit    = makeBtn("✏ Sửa",           WARNING_CLR,             Color.WHITE);
        btnDelete  = makeBtn("🗑 Xóa",          DANGER_CLR,              Color.WHITE);

        // Nút sửa/xóa chỉ hiện với Admin/Leader
        if (!currentUser.isLeader()) {
            btnEdit.setVisible(false);
            btnDelete.setVisible(false);
        }

        countLabel = new JLabel("Tổng: 0 thành viên");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(TEXT_GRAY);

        btnPanel.add(countLabel);
        btnPanel.add(Box.createHorizontalStrut(8));
        btnPanel.add(btnRefresh);
        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);

        toolbar.add(searchPanel);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(btnPanel);
        
        return toolbar;
    }

    // ─────────────────────────────────────────────────────
    // Table Area
    // ─────────────────────────────────────────────────────

    private JScrollPane buildTableArea() {
        // Model không cho sửa trực tiếp trên table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        memberTable = new JTable(tableModel);
        styleTable(memberTable);

        // Custom renderer để tô màu cột "Trạng thái"
        memberTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean selected, boolean focused, int row, int col) {
                super.getTableCellRendererComponent(table, value, selected, focused, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!selected) {
                    String status = value != null ? value.toString() : "";
                    switch (status) {
                        case "Active"    -> { setBackground(new Color(220,252,231)); setForeground(new Color(22,101,52)); }
                        case "Inactive"  -> { setBackground(new Color(254,226,226)); setForeground(new Color(185,28,28)); }
                        case "Suspended" -> { setBackground(new Color(254,243,199)); setForeground(new Color(146,64,14)); }
                        default          -> { setBackground(Color.WHITE); setForeground(TEXT_GRAY); }
                    }
                }
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });

        // Custom renderer cho cột Vai trò
        memberTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean selected, boolean focused, int row, int col) {
                super.getTableCellRendererComponent(table, value, selected, focused, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!selected) {
                    String role = value != null ? value.toString() : "";
                    switch (role) {
                        case "Admin"  -> { setBackground(new Color(237,233,254)); setForeground(new Color(109,40,217)); }
                        case "Leader" -> { setBackground(new Color(219,234,254)); setForeground(new Color(30,64,175)); }
                        default       -> { setBackground(Color.WHITE); setForeground(TEXT_DARK); }
                    }
                }
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });

        // Ẩn cột ID (dùng nội bộ, không hiển thị)
        memberTable.getColumnModel().getColumn(0).setMinWidth(0);
        memberTable.getColumnModel().getColumn(0).setMaxWidth(0);
        memberTable.getColumnModel().getColumn(0).setWidth(0);

        // Chiều rộng cột
        int[] colWidths = {0, 160, 90, 190, 110, 80, 100, 90, 80};
        for (int i = 0; i < colWidths.length; i++) {
            if (colWidths[i] > 0)
                memberTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane scrollPane = new JScrollPane(memberTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        return scrollPane;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setOpaque(false);
        statusBar = new JLabel(" ");
        statusBar.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusBar.setForeground(TEXT_GRAY);
        bar.add(statusBar);
        return bar;
    }

    // ─────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────

    /** Style chung cho JTable. */
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT_DARK);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        // Zebra striping (dòng xen kẽ)
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    setForeground(TEXT_DARK);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    /** Tạo button với màu nền và màu chữ chỉ định. */
    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 34));
        return btn;
    }

    // ===================================================
    // PUBLIC API (dùng bởi MemberController)
    // ===================================================

    /** Xóa và nạp lại dữ liệu vào bảng. */
    public void loadData(List<MemberDTO> members) {
        tableModel.setRowCount(0);
        for (MemberDTO m : members) {
            tableModel.addRow(new Object[]{
                m.getMemberId(),
                m.getFullName(),
                m.getStudentId(),
                m.getEmail(),
                m.getPhone(),
                m.getGender(),
                m.getJoinDate(),
                m.getStatus(),
                m.getRoleName()
            });
        }
        countLabel.setText("Tổng: " + members.size() + " thành viên");
        statusBar.setText("Đã tải " + members.size() + " thành viên.");
    }

    /**
     * Lấy ID của dòng đang được chọn trong bảng.
     * @return member_id hoặc -1 nếu không có dòng nào được chọn
     */
    public Integer getSelectedMemberId() {
        int row = memberTable.getSelectedRow();
        if (row < 0) return null;
        return (Integer) tableModel.getValueAt(row, 0);
    }

    /** Lấy từ khóa tìm kiếm. */
    public String getSearchKeyword() { return searchField.getText().trim(); }

    /** Lấy bộ lọc trạng thái đang chọn. */
    public String getStatusFilter()  { return (String) statusFilter.getSelectedItem(); }

    /** Hiển thị thông báo trên status bar. */
    public void setStatusMessage(String msg) { statusBar.setText(msg); }

    /** Trả về panel để gắn vào DashboardView. */
    public JPanel getPanel() { return mainPanel; }

    // Getters cho Controller đăng ký listeners
    public JButton getBtnAdd()     { return btnAdd; }
    public JButton getBtnEdit()    { return btnEdit; }
    public JButton getBtnDelete()  { return btnDelete; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnSearch()  { return btnSearch; }
    public JTable  getTable()      { return memberTable; }
    public JTextField getSearchField()   { return searchField; }
    public JComboBox<String> getStatusFilterComboBox() { return statusFilter; }
}
