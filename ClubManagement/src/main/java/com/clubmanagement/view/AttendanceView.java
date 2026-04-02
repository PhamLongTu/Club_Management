package com.clubmanagement.view;

import com.clubmanagement.dto.AttendanceDTO;
import com.clubmanagement.dto.MemberDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AttendanceView {

    private static final String[] COLUMNS = {
        "ID", "Thành viên", "Sự kiện", "Check-In", "Check-Out", "Trạng thái", "Ghi chú"
    };

    private JPanel mainPanel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JLabel countLabel;
    private JLabel statusBar;

    private static final Color PRIMARY     = new Color(37,  99,  235);
    private static final Color SUCCESS_CLR = new Color(16,  185, 129);
    private static final Color WARNING_CLR = new Color(245, 158, 11);
    private static final Color DANGER_CLR  = new Color(239, 68,  68);
    private static final Color BG          = new Color(241, 245, 249);
    private static final Color TEXT_DARK   = new Color(15,  23,  42);
    private static final Color TEXT_GRAY   = new Color(100, 116, 139);

    private final MemberDTO currentUser;

    public AttendanceView(MemberDTO currentUser) {
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

        JLabel title = new JLabel("✅ Máy chấm công / Điểm danh");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(true);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));
        toolbar.setBackground(Color.WHITE);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        btnRefresh = makeBtn("🔄 Làm mới",  new Color(100,116,139), Color.WHITE);
        btnAdd     = makeBtn("➕ Check-in", SUCCESS_CLR, Color.WHITE);
        btnEdit    = makeBtn("✏ Sửa",       WARNING_CLR, Color.WHITE);
        btnDelete  = makeBtn("🗑 Xóa",      DANGER_CLR, Color.WHITE);

        if (!currentUser.isLeader()) {
            btnAdd.setVisible(false); // Thành viên thường nếu muốn tự checkin thì có thể enable lại tuỳ quy trình
            btnDelete.setVisible(false);
            btnEdit.setVisible(false);
        }

        countLabel = new JLabel("Tổng: 0 Lượt");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(TEXT_GRAY);

        rightPanel.add(countLabel);
        rightPanel.add(Box.createHorizontalStrut(8));
        rightPanel.add(btnRefresh);
        rightPanel.add(btnAdd);
        rightPanel.add(btnEdit);
        rightPanel.add(btnDelete);

        toolbar.add(rightPanel, BorderLayout.EAST);

        panel.add(title, BorderLayout.NORTH);
        panel.add(toolbar, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        attendanceTable = new JTable(tableModel);
        styleTable(attendanceTable);

        attendanceTable.getColumnModel().getColumn(0).setMinWidth(0);
        attendanceTable.getColumnModel().getColumn(0).setMaxWidth(0);
        attendanceTable.getColumnModel().getColumn(0).setWidth(0);
        
        attendanceTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(table, value, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                String val = value != null ? value.toString() : "";
                if (!sel) {
                    switch (val) {
                        case "Present" -> { setBackground(new Color(220, 252, 231)); setForeground(new Color(22, 101, 52)); }
                        case "Late" -> { setBackground(new Color(254, 243, 199)); setForeground(new Color(180, 83, 9)); }
                        case "Absent" -> { setBackground(new Color(254, 226, 226)); setForeground(new Color(185, 28, 28)); }
                        case "Excused" -> { setBackground(new Color(224, 242, 254)); setForeground(new Color(3, 105, 161)); }
                        default -> { setBackground(new Color(241, 245, 249)); setForeground(new Color(71, 85, 105)); }
                    }
                }
                return this;
            }
        });

        int[] colWidths = {0, 200, 200, 150, 150, 100, 150};
        for (int i = 0; i < colWidths.length; i++) {
            if (colWidths[i] > 0)
                attendanceTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane sp = new JScrollPane(attendanceTable);
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
                if (col == 1 || col == 2) {
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                    setForeground(PRIMARY);
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
        btn.setPreferredSize(new Dimension(110, 34));
        return btn;
    }

    public void loadData(List<AttendanceDTO> data) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (AttendanceDTO a : data) {
            String checkInFormat = a.getCheckInTime() != null ? a.getCheckInTime().format(formatter) : "Chưa Check-In";
            String checkOutFormat = a.getCheckOutTime() != null ? a.getCheckOutTime().format(formatter) : "Chưa Check-Out";
            
            tableModel.addRow(new Object[]{
                a.getAttendanceId(),
                a.getMemberName(),
                a.getEventName(),
                checkInFormat,
                checkOutFormat,
                a.getStatus(),
                a.getNote()
            });
        }
        countLabel.setText("Tổng: " + data.size() + " Lượt");
        statusBar.setText("Đã tải " + data.size() + " lượt điểm danh.");
    }

    public Integer getSelectedId() {
        int row = attendanceTable.getSelectedRow();
        if (row < 0) return null;
        return (Integer) tableModel.getValueAt(row, 0);
    }
    
    public void setStatusMessage(String msg) { statusBar.setText(msg); }
    public JPanel getPanel() { return mainPanel; }

    public JButton getBtnAdd()         { return btnAdd; }
    public JButton getBtnEdit()        { return btnEdit; }
    public JButton getBtnDelete()      { return btnDelete; }
    public JButton getBtnRefresh()     { return btnRefresh; }
    public JTable  getTable()          { return attendanceTable; }
}
