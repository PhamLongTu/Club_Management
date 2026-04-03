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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.dto.TeamDTO;

public class TeamView {

    private static final String[] COLUMNS = {
        "ID", "Tên Ban / Nhóm", "Mô tả", "Trưởng ban", "Ngày thành lập"
    };

    private JPanel mainPanel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnViewMembers;
    private JTable teamTable;
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

    private final MemberDTO currentUser;

    public TeamView(MemberDTO currentUser) {
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

        JLabel title = new JLabel("Quản lý Ban / Nhóm");
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

        btnRefresh     = makeBtn("Làm mới",          new Color(100,116,139), Color.WHITE);
        btnViewMembers = makeBtn("Xem thành viên",   INFO_CLR, Color.WHITE);
        btnAdd         = makeBtn("Tạo Ban mới",      SUCCESS_CLR, Color.WHITE);
        btnEdit        = makeBtn("Sửa Ban",         WARNING_CLR, Color.WHITE);
        btnDelete      = makeBtn("Xóa Ban",         DANGER_CLR, Color.WHITE);

        // Nút xem thành viên sẽ có ở Controller (hiển thị Dialog các member thuộc nhóm này nếu cần bổ sung)

        if (!currentUser.isAdmin()) { // Chỉ Admin mới có quyền tạo/xóa ban chính thức
            btnAdd.setVisible(false);
            btnEdit.setVisible(false);
            btnDelete.setVisible(false);
        }

        countLabel = new JLabel("Tổng: 0 Ban");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(TEXT_GRAY);

        rightPanel.add(countLabel);
        rightPanel.add(Box.createHorizontalStrut(8));
        rightPanel.add(btnRefresh);
        rightPanel.add(btnViewMembers);
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

        teamTable = new JTable(tableModel);
        styleTable(teamTable);

        teamTable.getColumnModel().getColumn(0).setMinWidth(0);
        teamTable.getColumnModel().getColumn(0).setMaxWidth(0);
        teamTable.getColumnModel().getColumn(0).setWidth(0);

        int[] colWidths = {0, 200, 350, 150, 150};
        for (int i = 0; i < colWidths.length; i++) {
            if (colWidths[i] > 0)
                teamTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane sp = new JScrollPane(teamTable);
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
                if (col == 1) {
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
        btn.setPreferredSize(new Dimension(130, 34));
        return btn;
    }

    public void loadData(List<TeamDTO> data) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (TeamDTO t : data) {
            String dateFormatted = t.getCreatedDate() != null ? t.getCreatedDate().format(formatter) : "N/A";
            tableModel.addRow(new Object[]{
                t.getTeamId(),
                t.getTeamName(),
                t.getDescription(),
                t.getLeaderName(),
                dateFormatted
            });
        }
        countLabel.setText("Tổng: " + data.size() + " Ban");
        statusBar.setText("Đã tải " + data.size() + " Ban.");
    }

    public Integer getSelectedId() {
        int row = teamTable.getSelectedRow();
        if (row < 0) return null;
        return (Integer) tableModel.getValueAt(row, 0);
    }
    
    public String getSelectedName() {
        int row = teamTable.getSelectedRow();
        if (row < 0) return null;
        return (String) tableModel.getValueAt(row, 1);
    }

    public void setStatusMessage(String msg) { statusBar.setText(msg); }
    public JPanel getPanel() { return mainPanel; }

    public JButton getBtnAdd()         { return btnAdd; }
    public JButton getBtnEdit()        { return btnEdit; }
    public JButton getBtnDelete()      { return btnDelete; }
    public JButton getBtnRefresh()     { return btnRefresh; }
    public JButton getBtnViewMembers() { return btnViewMembers; }
    public JTable  getTable()          { return teamTable; }
}
