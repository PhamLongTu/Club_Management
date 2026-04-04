package com.clubmanagement.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.clubmanagement.dto.MeetingDTO;
import com.clubmanagement.dto.MemberDTO;

/**
 * View for listing and managing meetings.
 */
public class MeetingView {

    private static final String[] COLUMNS = {
        "ID", "Tieu de", "Bat dau", "Ket thuc", "Dia diem", "Chu tri", "Link"
    };

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private JPanel mainPanel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private JTable meetingTable;
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
    private static final Color OVERDUE_TXT = new Color(107, 114, 128);

    private final MemberDTO currentUser;

    /**
     * Creates the view for the current user.
     *
     * @param currentUser the logged-in member
     */
    public MeetingView(MemberDTO currentUser) {
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
     * Builds the header section with toolbar actions.
     *
     * @return the header panel
     */
    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel title = new JLabel("Quan ly Cuoc hop");
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

        btnRefresh = makeBtn("Lam moi",  new Color(100,116,139), Color.WHITE);
        btnAdd     = makeBtn("Them",     SUCCESS_CLR, Color.WHITE);
        btnEdit    = makeBtn("Sua",      WARNING_CLR, Color.WHITE);
        btnDelete  = makeBtn("Xoa",     DANGER_CLR, Color.WHITE);

        if (!currentUser.isLeader()) {
            btnAdd.setVisible(false);
            btnEdit.setVisible(false);
            btnDelete.setVisible(false);
        }

        countLabel = new JLabel("Tong: 0 cuoc hop");
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

    /**
     * Builds the table container for meetings.
     *
     * @return the scroll pane containing the table
     */
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        meetingTable = new JTable(tableModel);
        styleTable(meetingTable);

        meetingTable.getColumnModel().getColumn(0).setMinWidth(0);
        meetingTable.getColumnModel().getColumn(0).setMaxWidth(0);
        meetingTable.getColumnModel().getColumn(0).setWidth(0);

        int[] colWidths = {0, 230, 140, 140, 150, 140, 220};
        for (int i = 0; i < colWidths.length; i++) {
            if (colWidths[i] > 0)
                meetingTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane sp = new JScrollPane(meetingTable);
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
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 42));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (v instanceof LocalDateTime dt) {
                    setText(dt.format(DATE_FMT));
                }

                boolean overdue = isOverdueRow(row);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    setForeground(TEXT_DARK);
                    if (overdue) {
                        setForeground(OVERDUE_TXT);
                    }
                }

                if (col == 1) {
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                    if (!sel && !overdue) setForeground(PRIMARY);
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    private boolean isOverdueRow(int row) {
        Object end = tableModel.getValueAt(row, 3);
        if (end instanceof LocalDateTime dt) {
            return dt.isBefore(LocalDateTime.now());
        }
        return false;
    }

    /**
     * Creates a toolbar button.
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
     * Loads meeting rows into the table.
     *
     * @param data meeting data list
     */
    public void loadData(List<MeetingDTO> data) {
        tableModel.setRowCount(0);
        for (MeetingDTO m : data) {
            String location = (m.getLocation() == null || m.getLocation().isBlank()) ? "-" : m.getLocation();
            String hostName = (m.getHostName() == null || m.getHostName().isBlank()) ? "-" : m.getHostName();
            String link = (m.getMeetLink() == null || m.getMeetLink().isBlank()) ? "-" : m.getMeetLink();
            tableModel.addRow(new Object[]{
                m.getMeetingId(),
                m.getTitle(),
                m.getStartTime(),
                m.getEndTime(),
                location,
                hostName,
                link
            });
        }
        countLabel.setText("Tong: " + data.size() + " cuoc hop");
        statusBar.setText("Da tai " + data.size() + " muc.");
    }

    /**
     * Gets the selected meeting id.
     *
     * @return selected id or null
     */
    public Integer getSelectedId() {
        int row = meetingTable.getSelectedRow();
        if (row < 0) return null;
        return (Integer) tableModel.getValueAt(row, 0);
    }

    /**
     * Updates the status bar message.
     */
    public void setStatusMessage(String msg) { statusBar.setText(msg); }

    /** @return main panel */
    public JPanel getPanel() { return mainPanel; }

    /** @return add button */
    public JButton getBtnAdd()         { return btnAdd; }
    /** @return edit button */
    public JButton getBtnEdit()        { return btnEdit; }
    /** @return delete button */
    public JButton getBtnDelete()      { return btnDelete; }
    /** @return refresh button */
    public JButton getBtnRefresh()     { return btnRefresh; }
    /** @return meeting table */
    public JTable  getTable()          { return meetingTable; }
}
