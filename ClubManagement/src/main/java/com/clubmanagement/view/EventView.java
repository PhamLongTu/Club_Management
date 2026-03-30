package com.clubmanagement.view;

import com.clubmanagement.dto.EventDTO;
import com.clubmanagement.dto.MemberDTO;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * EventView - Màn hình quản lý Sự kiện.
 *
 * Hiển thị danh sách sự kiện với các màu trạng thái:
 * - Upcoming  → xanh dương
 * - Ongoing   → xanh lá
 * - Completed → xám
 * - Cancelled → đỏ
 */
public class EventView {

    private static final String[] COLUMNS = {
        "ID", "Tên sự kiện", "Ngày bắt đầu", "Ngày kết thúc",
        "Địa điểm", "Trạng thái", "Ngân sách", "Đăng ký", "Người tạo"
    };

    private JPanel        mainPanel;
    private JTextField    searchField;
    private JComboBox<String> statusFilter;
    private JButton       btnAdd, btnEdit, btnDelete, btnRefresh, btnSearch;
    private JTable        eventTable;
    private DefaultTableModel tableModel;
    private JLabel        countLabel;
    private JLabel        statusBar;

    private static final Color PRIMARY     = new Color(37, 99, 235);
    private static final Color SUCCESS_CLR = new Color(16, 185, 129);
    private static final Color DANGER_CLR  = new Color(239, 68, 68);
    private static final Color WARNING_CLR = new Color(245, 158, 11);
    private static final Color BG          = new Color(241, 245, 249);
    private static final Color TEXT_DARK   = new Color(15, 23, 42);
    private static final Color TEXT_GRAY   = new Color(100, 116, 139);

    private final MemberDTO currentUser;

    public EventView(MemberDTO currentUser) {
        this.currentUser = currentUser;
        buildUI();
    }

    private void buildUI() {
        mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(BG);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        mainPanel.add(buildHeader(), BorderLayout.NORTH);
        mainPanel.add(buildTable(),  BorderLayout.CENTER);
        mainPanel.add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel title = new JLabel("📅 Quản lý Sự kiện");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);

        // Toolbar
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setOpaque(true);
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));

        // Phần tìm kiếm
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(240, 34));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        searchField.setToolTipText("Tìm theo tên hoặc địa điểm...");

        btnSearch = makeBtn("🔍 Tìm", PRIMARY, Color.WHITE);

        statusFilter = new JComboBox<>(new String[]{"Tất cả", "Upcoming", "Ongoing", "Completed", "Cancelled"});
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.setPreferredSize(new Dimension(140, 34));

        leftPanel.add(new JLabel("Tìm kiếm: "));
        leftPanel.add(searchField);
        leftPanel.add(btnSearch);
        leftPanel.add(Box.createHorizontalStrut(8));
        leftPanel.add(new JLabel("Trạng thái: "));
        leftPanel.add(statusFilter);

        // Phần nút CRUD
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        btnRefresh = makeBtn("🔄 Làm mới", new Color(100,116,139), Color.WHITE);
        btnAdd     = makeBtn("➕ Thêm",     SUCCESS_CLR,             Color.WHITE);
        btnEdit    = makeBtn("✏ Sửa",      WARNING_CLR,             Color.WHITE);
        btnDelete  = makeBtn("🗑 Xóa",     DANGER_CLR,              Color.WHITE);

        if (!currentUser.isLeader()) {
            btnEdit.setVisible(false);
            btnDelete.setVisible(false);
        }

        countLabel = new JLabel("Tổng: 0 sự kiện");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(TEXT_GRAY);

        rightPanel.add(countLabel);
        rightPanel.add(Box.createHorizontalStrut(8));
        rightPanel.add(btnRefresh);
        rightPanel.add(btnAdd);
        rightPanel.add(btnEdit);
        rightPanel.add(btnDelete);

        toolbar.add(leftPanel);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(rightPanel);

        panel.add(title, BorderLayout.NORTH);
        panel.add(toolbar, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        eventTable = new JTable(tableModel);
        styleTable(eventTable);

        // Renderer màu cho cột Trạng thái (index 5)
        eventTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean sel, boolean focused, int row, int col) {
                super.getTableCellRendererComponent(table, value, sel, focused, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel) {
                    String s = value != null ? value.toString() : "";
                    switch (s) {
                        case "Upcoming"  -> { setBackground(new Color(219,234,254)); setForeground(new Color(30,64,175)); }
                        case "Ongoing"   -> { setBackground(new Color(220,252,231)); setForeground(new Color(22,101,52)); }
                        case "Completed" -> { setBackground(new Color(241,245,249)); setForeground(new Color(71,85,105)); }
                        case "Cancelled" -> { setBackground(new Color(254,226,226)); setForeground(new Color(185,28,28)); }
                        default          -> { setBackground(Color.WHITE); setForeground(TEXT_GRAY); }
                    }
                }
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });

        // Renderer căn phải cho cột Ngân sách (index 6)
        eventTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean sel, boolean focused, int row, int col) {
                super.getTableCellRendererComponent(table, value, sel, focused, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                return this;
            }
        });

        // Ẩn cột ID
        eventTable.getColumnModel().getColumn(0).setMinWidth(0);
        eventTable.getColumnModel().getColumn(0).setMaxWidth(0);
        eventTable.getColumnModel().getColumn(0).setWidth(0);

        int[] colWidths = {0, 220, 130, 130, 140, 90, 110, 70, 120};
        for (int i = 0; i < colWidths.length; i++) {
            if (colWidths[i] > 0)
                eventTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane sp = new JScrollPane(eventTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private JPanel buildFooter() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setOpaque(false);
        statusBar = new JLabel(" ");
        statusBar.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusBar.setForeground(TEXT_GRAY);
        bar.add(statusBar);
        return bar;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT_DARK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 42));

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
    // PUBLIC API
    // ===================================================

    /** Nạp dữ liệu vào bảng. */
    public void loadData(List<EventDTO> events) {
        tableModel.setRowCount(0);
        for (EventDTO e : events) {
            tableModel.addRow(new Object[]{
                e.getEventId(),
                e.getEventName(),
                e.getStartDate() != null ? e.getStartDate().toLocalDate().toString() : "",
                e.getEndDate()   != null ? e.getEndDate().toLocalDate().toString()   : "",
                e.getLocation(),
                e.getStatus(),
                formatCurrency(e.getBudget()),
                e.getRegisteredCount() + "/" + e.getMaxParticipants(),
                e.getCreatedByName()
            });
        }
        countLabel.setText("Tổng: " + events.size() + " sự kiện");
        statusBar.setText("Đã tải " + events.size() + " sự kiện.");
    }

    private String formatCurrency(java.math.BigDecimal amount) {
        if (amount == null) return "0 ₫";
        return String.format("%,.0f ₫", amount);
    }

    public Integer getSelectedEventId() {
        int row = eventTable.getSelectedRow();
        if (row < 0) return null;
        return (Integer) tableModel.getValueAt(row, 0);
    }

    public String getSearchKeyword()  { return searchField.getText().trim(); }
    public String getStatusFilter()   { return (String) statusFilter.getSelectedItem(); }
    public void setStatusMessage(String msg) { statusBar.setText(msg); }
    public JPanel getPanel()          { return mainPanel; }

    public JButton getBtnAdd()     { return btnAdd; }
    public JButton getBtnEdit()    { return btnEdit; }
    public JButton getBtnDelete()  { return btnDelete; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnSearch()  { return btnSearch; }
    public JTable  getTable()      { return eventTable; }
    public JTextField    getSearchField() { return searchField; }
    public JComboBox<String> getStatusFilter2() { return statusFilter; }
}
