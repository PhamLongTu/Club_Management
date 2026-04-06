package com.clubmanagement.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.dto.TaskDTO;

/**
 * View for listing and managing tasks.
 */
public class TaskView {

    private static final String[] COLUMNS = {
        "ID", "Nhiệm vụ", "Người thực hiện", "Người giao", "Sự kiện", "Độ ưu tiên", "Trạng thái", "Hạn chót"
    };

    private JPanel mainPanel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private JComboBox<String> filterBox;
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JLabel countLabel;
    private JLabel statusBar;

    private static final Color PRIMARY     = new Color(37,  99,  235);
    private static final Color SUCCESS_CLR = new Color(16,  185, 129);
    private static final Color WARNING_CLR = new Color(245, 158, 11);
    private static final Color DANGER_CLR  = new Color(239, 68,  68);
    private static final Color INFO_CLR    = new Color(14, 165, 233);
    private static final Color BG          = new Color(241, 245, 249);
    private static final Color TEXT_DARK   = new Color(15,  23,  42);
    private static final Color TEXT_GRAY   = new Color(100, 116, 139);
    private static final Color ACCENT      = new Color(168, 85, 247);
    private static final Color ACCENT_SOFT = new Color(243, 232, 255);
    private static final Color ACCENT_ROW  = new Color(250, 245, 255);

    private final MemberDTO currentUser;

    /**
     * Creates the view for the current user.
     *
     * @param currentUser the logged-in member
     */
    public TaskView(MemberDTO currentUser) {
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

        JLabel title = new JLabel("Phân công Nhiệm vụ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ACCENT);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(true);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));
        toolbar.setBackground(ACCENT_SOFT);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        filterBox = new JComboBox<>(new String[]{
            "Tất cả",
            "Chưa chỉ định (Public)",
            "Đã chỉ định (Public/Private)",
            "Nhiệm vụ của tôi"
        });
        filterBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filterBox.setPreferredSize(new Dimension(220, 34));

        leftPanel.add(new JLabel("Bộ lọc:"));
        leftPanel.add(filterBox);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        btnRefresh = makeBtn("Làm mới",  new Color(100,116,139), Color.WHITE);
        btnAdd     = makeBtn("Giao việc", SUCCESS_CLR, Color.WHITE);
        btnEdit    = makeBtn("Sửa",       WARNING_CLR, Color.WHITE);
        btnDelete  = makeBtn("Xóa",      DANGER_CLR, Color.WHITE);

        if (!currentUser.isLeader()) {
            btnAdd.setVisible(false);
            btnDelete.setVisible(false);
        }

        countLabel = new JLabel("Tổng: 0 Nhiệm vụ");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(TEXT_GRAY);

        rightPanel.add(countLabel);
        rightPanel.add(Box.createHorizontalStrut(8));
        rightPanel.add(btnRefresh);
        rightPanel.add(btnAdd);
        rightPanel.add(btnEdit);
        rightPanel.add(btnDelete);

        toolbar.add(leftPanel, BorderLayout.WEST);
        toolbar.add(rightPanel, BorderLayout.EAST);

        panel.add(title, BorderLayout.NORTH);
        panel.add(toolbar, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Builds the table container for tasks.
     *
     * @return the scroll pane containing the table
     */
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        taskTable = new JTable(tableModel);
        styleTable(taskTable);

        taskTable.getColumnModel().getColumn(0).setMinWidth(0);
        taskTable.getColumnModel().getColumn(0).setMaxWidth(0);
        taskTable.getColumnModel().getColumn(0).setWidth(0);

        // Status Renderer
        taskTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(table, value, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                String val = value != null ? value.toString() : "";
                if (!sel) {
                    switch (val) {
                        case "Done" -> { setBackground(new Color(220, 252, 231)); setForeground(new Color(22, 101, 52)); }
                        case "InProgress" -> { setBackground(new Color(254, 243, 199)); setForeground(new Color(180, 83, 9)); }
                        case "Overdue" -> { setBackground(new Color(254, 226, 226)); setForeground(new Color(185, 28, 28)); }
                        default -> { setBackground(new Color(241, 245, 249)); setForeground(new Color(71, 85, 105)); }
                    }
                }
                return this;
            }
        });

        int[] colWidths = {0, 220, 150, 150, 150, 90, 100, 150};
        for (int i = 0; i < colWidths.length; i++) {
            if (colWidths[i] > 0)
                taskTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane sp = new JScrollPane(taskTable);
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
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : ACCENT_ROW);
                    setForeground(TEXT_DARK);
                }
                switch (col) {
                    case 1 -> {
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                        setForeground(PRIMARY);
                    }
                    case 5 -> {
                        // Priority rendering text style
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                        if ("High".equals(v) || "Critical".equals(v)) setForeground(DANGER_CLR);
                        else if ("Medium".equals(v)) setForeground(WARNING_CLR);
                        else setForeground(INFO_CLR);
                    }
                    default -> setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
        btn.setPreferredSize(new Dimension(110, 34));
        return btn;
    }

    /**
     * Loads task rows into the table.
     *
     * @param data task data list
     */
    public void loadData(List<TaskDTO> data) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (TaskDTO t : data) {
            String dlFormat = t.getDeadline() != null ? t.getDeadline().format(formatter) : "Không xác định";
            tableModel.addRow(new Object[]{
                t.getTaskId(),
                t.getTitle(),
                t.getAssigneeName(),
                t.getAssignerName(),
                t.getEventName(),
                t.getPriority(),
                t.getStatus(),
                dlFormat
            });
        }
        countLabel.setText("Tổng: " + data.size() + " Nhiệm vụ");
        statusBar.setText("Đã tải " + data.size() + " Nhiệm vụ.");
    }

    /**
     * Gets the selected task id.
     *
     * @return selected id or null
     */
    public Integer getSelectedId() {
        int row = taskTable.getSelectedRow();
        if (row < 0) return null;
        return (Integer) tableModel.getValueAt(row, 0);
    }

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
    public JPanel getPanel() { return mainPanel; }

    /** @return add button */
    public JButton getBtnAdd()         { return btnAdd; }
    /** @return edit button */
    public JButton getBtnEdit()        { return btnEdit; }
    /** @return delete button */
    public JButton getBtnDelete()      { return btnDelete; }
    /** @return refresh button */
    public JButton getBtnRefresh()     { return btnRefresh; }
    /** @return task table */
    public JTable  getTable()          { return taskTable; }
    /** @return filter combo box */
    public JComboBox<String> getFilterBox() { return filterBox; }
}
