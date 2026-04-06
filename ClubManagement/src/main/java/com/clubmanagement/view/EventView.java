package com.clubmanagement.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.clubmanagement.dto.EventDTO;
import com.clubmanagement.dto.MemberDTO;

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
        "Trạng thái", "Đăng ký"
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
    private static final Color ACCENT      = new Color(34, 197, 94);
    private static final Color ACCENT_SOFT = new Color(220, 252, 231);
    private static final Color ACCENT_ROW  = new Color(240, 253, 244);

    private final MemberDTO currentUser;

    /**
     * Creates the view for the current user.
     *
     * @param currentUser the logged-in member
     */
    public EventView(MemberDTO currentUser) {
        this.currentUser = currentUser;
        buildUI();
    }

    /**
     * Builds the main layout.
     */
    private void buildUI() {
        mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(BG);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        mainPanel.add(buildHeader(), BorderLayout.NORTH);
        mainPanel.add(buildTable(),  BorderLayout.CENTER);
        mainPanel.add(buildFooter(), BorderLayout.SOUTH);
    }

    /**
     * Builds the header section with filters and actions.
     *
     * @return the header panel
     */
    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel title = new JLabel("Quản lý Sự kiện");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ACCENT);

        // Toolbar
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setOpaque(true);
        toolbar.setBackground(ACCENT_SOFT);
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

        btnSearch = makeBtn("Tìm", PRIMARY, Color.WHITE);

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

        btnRefresh = makeBtn("Làm mới", new Color(100,116,139), Color.WHITE);
        btnAdd     = makeBtn("Thêm",     SUCCESS_CLR,             Color.WHITE);
        btnEdit    = makeBtn("Sửa",      WARNING_CLR,             Color.WHITE);
        btnDelete  = makeBtn("Xóa",     DANGER_CLR,              Color.WHITE);

        if (!currentUser.isLeader()) {
            btnAdd.setVisible(false);
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

    /**
     * Builds the table container for events.
     *
     * @return the scroll pane containing the table
     */
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        eventTable = new JTable(tableModel);
        styleTable(eventTable);

        // Renderer màu cho cột Trạng thái (index 4)
        eventTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
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

        // Ẩn cột ID
        eventTable.getColumnModel().getColumn(0).setMinWidth(0);
        eventTable.getColumnModel().getColumn(0).setMaxWidth(0);
        eventTable.getColumnModel().getColumn(0).setWidth(0);

        int[] colWidths = {0, 260, 130, 130, 100, 90};
        for (int i = 0; i < colWidths.length; i++) {
            if (colWidths[i] > 0)
                eventTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane sp = new JScrollPane(eventTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    /**
     * Builds the status footer.
     *
     * @return the footer panel
     */
    private JPanel buildFooter() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setOpaque(false);
        statusBar = new JLabel(" ");
        statusBar.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusBar.setForeground(TEXT_GRAY);
        bar.add(statusBar);
        return bar;
    }

    /**
     * Applies consistent styling to the table.
     *
     * @param table the table to style
     */
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
        header.setBackground(ACCENT_SOFT);
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 42));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : ACCENT_ROW);
                    setForeground(TEXT_DARK);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    /**
     * Creates a toolbar button.
     *
     * @param text button label
     * @param bg background color
     * @param fg foreground color
     * @return the configured button
     */
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
                e.getStatus(),
                e.getRegisteredCount() + "/" + e.getMaxParticipants()
            });
        }
        countLabel.setText("Tổng: " + events.size() + " sự kiện");
        statusBar.setText("Đã tải " + events.size() + " sự kiện.");
    }

    /**
     * Gets the selected event id.
     *
     * @return selected id or null
     */
    public Integer getSelectedEventId() {
        int row = eventTable.getSelectedRow();
        if (row < 0) return null;
        return (Integer) tableModel.getValueAt(row, 0);
    }

    /**
     * @return search keyword from the input
     */
    public String getSearchKeyword()  { return searchField.getText().trim(); }

    /**
     * @return selected status filter value
     */
    public String getStatusFilter()   { return (String) statusFilter.getSelectedItem(); }

    /**
     * Updates the status bar message.
     *
     * @param msg message to display
     */
    public void setStatusMessage(String msg) { statusBar.setText(msg); }

    /**
     * Returns the root panel for this view.
     *
     * @return main panel
     */
    public JPanel getPanel()          { return mainPanel; }

    /** @return add button */
    public JButton getBtnAdd()     { return btnAdd; }
    /** @return edit button */
    public JButton getBtnEdit()    { return btnEdit; }
    /** @return delete button */
    public JButton getBtnDelete()  { return btnDelete; }
    /** @return refresh button */
    public JButton getBtnRefresh() { return btnRefresh; }
    /** @return search button */
    public JButton getBtnSearch()  { return btnSearch; }
    /** @return event table */
    public JTable  getTable()      { return eventTable; }
    /** @return search text field */
    public JTextField    getSearchField() { return searchField; }
    /** @return status filter combo box */
    public JComboBox<String> getStatusFilter2() { return statusFilter; }
}
