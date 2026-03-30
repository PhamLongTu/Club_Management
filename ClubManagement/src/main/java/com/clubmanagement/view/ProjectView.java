package com.clubmanagement.view;

import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.dto.ProjectDTO;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * ProjectView - Màn hình quản lý Dự án.
 */
public class ProjectView {

    private static final String[] COLUMNS = {
        "ID", "Tên dự án", "Ngày bắt đầu", "Ngày kết thúc",
        "Ngân sách", "Trạng thái", "Quản lý", "Thành viên"
    };

    private JPanel        mainPanel;
    private JTextField    searchField;
    private JComboBox<String> statusFilter;
    private JButton       btnAdd, btnEdit, btnDelete, btnRefresh, btnSearch;
    private JTable        projectTable;
    private DefaultTableModel tableModel;
    private JLabel        countLabel;
    private JLabel        statusBar;

    private static final Color PRIMARY     = new Color(37,  99,  235);
    private static final Color SUCCESS_CLR = new Color(16,  185, 129);
    private static final Color DANGER_CLR  = new Color(239, 68,  68);
    private static final Color WARNING_CLR = new Color(245, 158, 11);
    private static final Color BG          = new Color(241, 245, 249);
    private static final Color TEXT_DARK   = new Color(15,  23,  42);
    private static final Color TEXT_GRAY   = new Color(100, 116, 139);

    private final MemberDTO currentUser;

    public ProjectView(MemberDTO currentUser) {
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

        JLabel title = new JLabel("📋 Quản lý Dự án");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);

        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setOpaque(true);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));
        toolbar.setBackground(Color.WHITE);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(240, 34));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));

        btnSearch = makeBtn("🔍 Tìm", PRIMARY, Color.WHITE);

        statusFilter = new JComboBox<>(new String[]{"Tất cả", "Planning", "Active", "OnHold", "Completed", "Cancelled"});
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilter.setPreferredSize(new Dimension(150, 34));

        leftPanel.add(new JLabel("Tìm kiếm: "));
        leftPanel.add(searchField);
        leftPanel.add(btnSearch);
        leftPanel.add(Box.createHorizontalStrut(8));
        leftPanel.add(new JLabel("Trạng thái: "));
        leftPanel.add(statusFilter);

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

        countLabel = new JLabel("Tổng: 0 dự án");
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

        projectTable = new JTable(tableModel);
        styleTable(projectTable);

        // Renderer màu cho cột Trạng thái (index 5)
        projectTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(table, value, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel) {
                    String s = value != null ? value.toString() : "";
                    switch (s) {
                        case "Planning"  -> { setBackground(new Color(237,233,254)); setForeground(new Color(109,40,217)); }
                        case "Active"    -> { setBackground(new Color(220,252,231)); setForeground(new Color(22,101,52)); }
                        case "OnHold"    -> { setBackground(new Color(254,243,199)); setForeground(new Color(146,64,14)); }
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
        projectTable.getColumnModel().getColumn(0).setMinWidth(0);
        projectTable.getColumnModel().getColumn(0).setMaxWidth(0);
        projectTable.getColumnModel().getColumn(0).setWidth(0);

        int[] colWidths = {0, 250, 110, 110, 120, 100, 150, 80};
        for (int i = 0; i < colWidths.length; i++) {
            if (colWidths[i] > 0)
                projectTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane sp = new JScrollPane(projectTable);
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
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
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

    public void loadData(List<ProjectDTO> projects) {
        tableModel.setRowCount(0);
        for (ProjectDTO p : projects) {
            tableModel.addRow(new Object[]{
                p.getProjectId(),
                p.getProjectName(),
                p.getStartDate(),
                p.getEndDate(),
                formatCurrency(p.getBudget()),
                p.getStatus(),
                p.getManagerName(),
                p.getMemberCount() + " người"
            });
        }
        countLabel.setText("Tổng: " + projects.size() + " dự án");
        statusBar.setText("Đã tải " + projects.size() + " dự án.");
    }

    private String formatCurrency(java.math.BigDecimal amount) {
        if (amount == null) return "0 ₫";
        return String.format("%,.0f ₫", amount);
    }

    public Integer getSelectedProjectId() {
        int row = projectTable.getSelectedRow();
        if (row < 0) return null;
        return (Integer) tableModel.getValueAt(row, 0);
    }

    public String getSearchKeyword() { return searchField.getText().trim(); }
    public void setStatusMessage(String msg) { statusBar.setText(msg); }
    public JPanel getPanel() { return mainPanel; }

    public JButton getBtnAdd()     { return btnAdd; }
    public JButton getBtnEdit()    { return btnEdit; }
    public JButton getBtnDelete()  { return btnDelete; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnSearch()  { return btnSearch; }
    public JTable  getTable()      { return projectTable; }
    public JTextField    getSearchField()    { return searchField; }
    public JComboBox<String> getStatusFilterBox() { return statusFilter; }
}
