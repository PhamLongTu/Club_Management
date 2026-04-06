package com.clubmanagement.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import com.clubmanagement.dto.EventAttendanceRowDTO;
import com.clubmanagement.dto.EventDTO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.util.UiFormUtil;
import com.toedter.calendar.JDateChooser;

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

    private JTextField tfName;
    private JTextField tfLocation;
    private JTextField tfBudget;
    private JTextField tfMaxP;
    private JDateChooser startDate;
    private JSpinner startTime;
    private JDateChooser endDate;
    private JSpinner endTime;
    private JDateChooser regDate;
    private JSpinner regTime;
    private JTextArea taDesc;
    private JComboBox<String> cbStatus;
    private JComboBox<String> cbPointType;
    private JSpinner spPointValue;
    private JButton btnSaveEvent;

    private static final Color PRIMARY     = new Color(37, 99, 235);
    private static final Color BG          = new Color(241, 245, 249);
    private static final Color TEXT_DARK   = new Color(15, 23, 42);
    private static final Color TEXT_GRAY   = new Color(100, 116, 139);
    private static final Color ACCENT      = new Color(20, 184, 166);
    private static final Color ACCENT_SOFT = new Color(204, 251, 241);
    private static final Color ACCENT_ROW  = new Color(240, 253, 250);

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
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(buildEventEditor());
        content.add(Box.createVerticalStrut(12));
        content.add(buildTable());
        mainPanel.add(content, BorderLayout.CENTER);
        mainPanel.add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel title = new JLabel("Điểm danh sự kiện");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ACCENT);

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
        toolbar.setBackground(ACCENT_SOFT);
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

    private JPanel buildEventEditor() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel title = new JLabel("Thông tin sự kiện");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_DARK);

        btnSaveEvent = makeBtn("Lưu sự kiện", new Color(16, 185, 129), Color.WHITE, 110);
        btnSaveEvent.setEnabled(currentUser.isLeader());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(btnSaveEvent, BorderLayout.EAST);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        tfName = createField("", f);
        tfLocation = createField("", f);
        tfBudget = createField("0", f);
        tfMaxP = createField("100", f);

        startDate = UiFormUtil.createDateChooser((LocalDateTime) null);
        startTime = UiFormUtil.createTimeSpinner((LocalDateTime) null);
        endDate = UiFormUtil.createDateChooser((LocalDateTime) null);
        endTime = UiFormUtil.createTimeSpinner((LocalDateTime) null);
        regDate = UiFormUtil.createDateChooser((LocalDateTime) null);
        regTime = UiFormUtil.createTimeSpinner((LocalDateTime) null);

        taDesc = new JTextArea(3, 20);
        taDesc.setFont(f);
        taDesc.setLineWrap(true);

        cbStatus = new JComboBox<>(new String[]{"Upcoming", "Ongoing", "Completed", "Cancelled"});
        cbStatus.setFont(f);

        cbPointType = new JComboBox<>(new String[]{"None", "DRL", "CTXH"});
        cbPointType.setFont(f);

        spPointValue = new JSpinner(new javax.swing.SpinnerNumberModel(0, 0, 200, 1));
        spPointValue.setFont(f);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 0;
        gc.gridy = 0;

        addFormPair(form, gc, 0, "Tên sự kiện *:", tfName);
        addFormPair(form, gc, 1, "Trạng thái:", cbStatus);
        gc.gridy++;

        addFormPair(form, gc, 0, "Bắt đầu *:", UiFormUtil.buildDateTimePanel(startDate, startTime));
        addFormPair(form, gc, 1, "Kết thúc *:", UiFormUtil.buildDateTimePanel(endDate, endTime));
        gc.gridy++;

        addFormPair(form, gc, 0, "Hạn đăng ký:", UiFormUtil.buildDateTimePanel(regDate, regTime));
        addFormPair(form, gc, 1, "Địa điểm:", tfLocation);
        gc.gridy++;

        addFormPair(form, gc, 0, "Ngân sách (VNĐ):", tfBudget);
        addFormPair(form, gc, 1, "Số lượng tối đa:", tfMaxP);
        gc.gridy++;

        addFormPair(form, gc, 0, "Loại điểm:", cbPointType);
        addFormPair(form, gc, 1, "Điểm áp dụng:", spPointValue);
        gc.gridy++;

        JLabel descLabel = makeLabel("Mô tả:");
        gc.gridx = 0;
        gc.gridwidth = 1;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        form.add(descLabel, gc);

        JScrollPane descPane = new JScrollPane(taDesc);
        descPane.setPreferredSize(new Dimension(200, 64));
        gc.gridx = 1;
        gc.gridwidth = 3;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.BOTH;
        form.add(descPane, gc);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        return card;
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

    private JTextField createField(String value, Font f) {
        JTextField tf = new JTextField(value);
        tf.setFont(f);
        return tf;
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
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

    public void setEventForm(EventDTO event) {
        if (event == null) {
            tfName.setText("");
            tfLocation.setText("");
            tfBudget.setText("0");
            tfMaxP.setText("100");
            cbStatus.setSelectedItem("Upcoming");
            cbPointType.setSelectedItem("None");
            spPointValue.setValue(0);
            taDesc.setText("");
            setDateTime(startDate, startTime, null);
            setDateTime(endDate, endTime, null);
            setDateTime(regDate, regTime, null);
            return;
        }

        tfName.setText(event.getEventName() != null ? event.getEventName() : "");
        tfLocation.setText(event.getLocation() != null ? event.getLocation() : "");
        tfBudget.setText(event.getBudget() != null ? event.getBudget().toPlainString() : "0");
        tfMaxP.setText(event.getMaxParticipants() != null ? String.valueOf(event.getMaxParticipants()) : "100");
        cbStatus.setSelectedItem(event.getStatus() != null ? event.getStatus() : "Upcoming");
        cbPointType.setSelectedItem(event.getPointType() != null ? event.getPointType() : "None");
        Integer pointValue = event.getPointValue();
        spPointValue.setValue(pointValue != null ? pointValue : Integer.valueOf(0));
        taDesc.setText(event.getDescription() != null ? event.getDescription() : "");
        setDateTime(startDate, startTime, event.getStartDate());
        setDateTime(endDate, endTime, event.getEndDate());
        setDateTime(regDate, regTime, event.getRegistrationDeadline());
    }

    public void setAttendanceEditable(boolean editable) {
        this.attendanceEditable = editable && currentUser.isLeader();
        if (table != null) {
            table.repaint();
        }
    }

    public void setEventFormEditable(boolean editable) {
        boolean enabled = editable && currentUser.isLeader();
        tfName.setEnabled(enabled);
        tfLocation.setEnabled(enabled);
        tfBudget.setEnabled(enabled);
        tfMaxP.setEnabled(enabled);
        startDate.setEnabled(enabled);
        startTime.setEnabled(enabled);
        endDate.setEnabled(enabled);
        endTime.setEnabled(enabled);
        regDate.setEnabled(enabled);
        regTime.setEnabled(enabled);
        taDesc.setEnabled(enabled);
        cbStatus.setEnabled(enabled);
        cbPointType.setEnabled(enabled);
        spPointValue.setEnabled(enabled);
        btnSaveEvent.setEnabled(enabled);
    }

    private void setDateTime(JDateChooser dateChooser, JSpinner timeSpinner, LocalDateTime dateTime) {
        if (dateTime == null) {
            dateChooser.setDate(null);
            LocalTime now = LocalTime.now().withSecond(0).withNano(0);
            Date timeValue = Date.from(now.atDate(java.time.LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
            timeSpinner.setValue(timeValue);
            return;
        }
        dateChooser.setDate(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
        Date timeValue = Date.from(dateTime.toLocalTime().atDate(java.time.LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
        timeSpinner.setValue(timeValue);
    }

    private void addFormPair(JPanel form, GridBagConstraints gc, int pairIndex, String labelText, Component input) {
        int baseX = pairIndex == 0 ? 0 : 2;
        JLabel label = makeLabel(labelText);
        gc.gridx = baseX;
        gc.gridwidth = 1;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        form.add(label, gc);

        gc.gridx = baseX + 1;
        gc.gridwidth = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        form.add(input, gc);
    }

    public String getSearchKeyword() { return searchField.getText().trim(); }

    public void setStatusMessage(String msg) { statusBar.setText(msg); }

    public JPanel getPanel() { return mainPanel; }

    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return btnRefresh; }
    public JButton getBtnBack() { return btnBack; }
    public JButton getBtnSaveEvent() { return btnSaveEvent; }
    public JTable getTable() { return table; }
    public JTextField getSearchField() { return searchField; }
    public JTextField getTfName() { return tfName; }
    public JTextField getTfLocation() { return tfLocation; }
    public JTextField getTfBudget() { return tfBudget; }
    public JTextField getTfMaxP() { return tfMaxP; }
    public JDateChooser getStartDate() { return startDate; }
    public JSpinner getStartTime() { return startTime; }
    public JDateChooser getEndDate() { return endDate; }
    public JSpinner getEndTime() { return endTime; }
    public JDateChooser getRegDate() { return regDate; }
    public JSpinner getRegTime() { return regTime; }
    public JTextArea getTaDesc() { return taDesc; }
    public JComboBox<String> getCbStatus() { return cbStatus; }
    public JComboBox<String> getCbPointType() { return cbPointType; }
    public JSpinner getSpPointValue() { return spPointValue; }
}
