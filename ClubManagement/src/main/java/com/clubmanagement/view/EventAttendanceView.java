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
import javax.swing.JCheckBox;
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
import javax.swing.table.TableCellRenderer;

import com.clubmanagement.dto.EventAttendanceRowDTO;
import com.clubmanagement.dto.MemberDTO;

/**
 * EventAttendanceView - Attendance page for a single event.
 */
public class EventAttendanceView {

    public static final int COL_MEMBER_ID = 0;
    public static final int COL_ATTENDANCE_ID = 1;
    public static final int COL_STUDENT_ID = 2;
    public static final int COL_FULL_NAME = 3;
    public static final int COL_EMAIL = 4;
    public static final int COL_ATTENDED = 5;

    private static final String[] COLUMNS = {
        "MemberID", "AttendanceID", "MSSV", "Họ tên", "Email", "Điểm danh"
    };

    private JPanel mainPanel;
    private JTextField searchField;
    private JButton btnSearch;
    private JButton btnRefresh;
    private JButton btnBack;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel countLabel;
    private JLabel statusBar;
    private JLabel eventTitleLabel;
    private JLabel eventMetaLabel;
    private boolean attendanceEditable;

    private static final Color PRIMARY     = new Color(37, 99, 235);
    private static final Color BG          = new Color(241, 245, 249);
    private static final Color TEXT_DARK   = new Color(15, 23, 42);
    private static final Color TEXT_GRAY   = new Color(100, 116, 139);

    private final MemberDTO currentUser;

    public EventAttendanceView(MemberDTO currentUser) {
        this.currentUser = currentUser;
        this.attendanceEditable = currentUser.isLeader();
        buildUI();
    }

    private void buildUI() {
        mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(BG);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        mainPanel.add(buildHeader(), BorderLayout.NORTH);
        mainPanel.add(buildTable(), BorderLayout.CENTER);
        mainPanel.add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel title = new JLabel("Điểm danh sự kiện");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_DARK);

        eventTitleLabel = new JLabel(" ");
        eventTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        eventTitleLabel.setForeground(TEXT_DARK);

        eventMetaLabel = new JLabel(" ");
        eventMetaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        eventMetaLabel.setForeground(TEXT_GRAY);

        JPanel eventInfo = new JPanel();
        eventInfo.setLayout(new BoxLayout(eventInfo, BoxLayout.Y_AXIS));
        eventInfo.setOpaque(false);
        eventInfo.add(eventTitleLabel);
        eventInfo.add(Box.createVerticalStrut(4));
        eventInfo.add(eventMetaLabel);

        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setOpaque(true);
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(240, 34));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(4, 10, 4, 10)
        ));
        searchField.setToolTipText("Tìm theo tên hoặc MSSV...");

        btnSearch = makeBtn("Tìm", PRIMARY, Color.WHITE, 80);
        btnRefresh = makeBtn("Làm mới", new Color(100, 116, 139), Color.WHITE, 100);

        leftPanel.add(new JLabel("Tìm kiếm: "));
        leftPanel.add(searchField);
        leftPanel.add(btnSearch);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        btnBack = makeBtn("Quay lại", PRIMARY, Color.WHITE, 100);

        countLabel = new JLabel("Tổng: 0 sinh viên");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(TEXT_GRAY);

        rightPanel.add(countLabel);
        rightPanel.add(Box.createHorizontalStrut(8));
        rightPanel.add(btnRefresh);
        rightPanel.add(btnBack);

        toolbar.add(leftPanel);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(rightPanel);

        JPanel topInfo = new JPanel(new BorderLayout());
        topInfo.setOpaque(false);
        topInfo.add(title, BorderLayout.NORTH);
        topInfo.add(eventInfo, BorderLayout.CENTER);

        panel.add(topInfo, BorderLayout.NORTH);
        panel.add(toolbar, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == COL_ATTENDED && attendanceEditable;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == COL_MEMBER_ID || columnIndex == COL_ATTENDANCE_ID) return Integer.class;
                if (columnIndex == COL_ATTENDED) return Boolean.class;
                return String.class;
            }
        };

        table = new JTable(tableModel);
        styleTable(table);

        table.getColumnModel().getColumn(COL_MEMBER_ID).setMinWidth(0);
        table.getColumnModel().getColumn(COL_MEMBER_ID).setMaxWidth(0);
        table.getColumnModel().getColumn(COL_MEMBER_ID).setWidth(0);

        table.getColumnModel().getColumn(COL_ATTENDANCE_ID).setMinWidth(0);
        table.getColumnModel().getColumn(COL_ATTENDANCE_ID).setMaxWidth(0);
        table.getColumnModel().getColumn(COL_ATTENDANCE_ID).setWidth(0);

        int[] colWidths = {0, 0, 120, 220, 240, 90};
        for (int i = 0; i < colWidths.length; i++) {
            if (colWidths[i] > 0) {
                table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            }
        }

        JScrollPane sp = new JScrollPane(table);
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

        table.setDefaultRenderer(Boolean.class, new CheckBoxRenderer());
    }

    private JButton makeBtn(String text, Color bg, Color fg, int width) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, 34));
        return btn;
    }

    private static class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {
        CheckBoxRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorderPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            boolean checked = value instanceof Boolean b && b;
            setSelected(checked);
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            }
            return this;
        }
    }

    // ===================================================
    // PUBLIC API
    // ===================================================

    public void loadData(List<EventAttendanceRowDTO> rows) {
        tableModel.setRowCount(0);
        for (EventAttendanceRowDTO row : rows) {
            tableModel.addRow(new Object[]{
                row.getMemberId(),
                row.getAttendanceId(),
                row.getStudentId(),
                row.getFullName(),
                row.getEmail(),
                row.isAttended()
            });
        }
        countLabel.setText("Tổng: " + rows.size() + " sinh viên");
    }

    public void setEventInfo(String eventName, int registeredCount) {
        eventTitleLabel.setText(eventName != null ? eventName : "");
        eventMetaLabel.setText("Đăng ký: " + registeredCount + " sinh viên");
    }

    public void setAttendanceEditable(boolean editable) {
        this.attendanceEditable = editable && currentUser.isLeader();
        if (table != null) {
            table.repaint();
        }
    }

    public String getSearchKeyword() { return searchField.getText().trim(); }

    public void setStatusMessage(String msg) { statusBar.setText(msg); }

    public JPanel getPanel() { return mainPanel; }

    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnBack() { return btnBack; }
    public JTable getTable() { return table; }
    public JTextField getSearchField() { return searchField; }
}
