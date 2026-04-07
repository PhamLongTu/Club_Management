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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.clubmanagement.dto.AnnouncementDTO;
import com.clubmanagement.dto.MemberDTO;

/**
 * Màn hình danh sách và quản lý thông báo.
 */
public class AnnouncementView {

    private static final String[] COLUMNS = {
        "ID", "Trạng thái", "Tiêu đề", "Người đăng", "Ngày đăng"
    };

    private JPanel mainPanel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh;
    private JTable announcementTable;
    private DefaultTableModel tableModel;
    private JLabel countLabel;
    private JLabel statusBar;

    private static final Color SUCCESS_CLR = new Color(16,  185, 129);
    private static final Color WARNING_CLR = new Color(245, 158, 11);
    private static final Color DANGER_CLR  = new Color(239, 68,  68);
    private static final Color BG          = new Color(241, 245, 249);
    private static final Color TEXT_DARK   = new Color(15,  23,  42);
    private static final Color TEXT_GRAY   = new Color(100, 116, 139);
    private static final Color ACCENT      = new Color(239, 68, 68);
    private static final Color ACCENT_SOFT = new Color(254, 226, 226);
    private static final Color ACCENT_ROW  = new Color(254, 242, 242);

    private final MemberDTO currentUser;

    /**
     * Khởi tạo giao diện cho người dùng hiện tại.
     *
     * @param currentUser thành viên đang đăng nhập
     */
    public AnnouncementView(MemberDTO currentUser) {
        this.currentUser = currentUser;
        buildUI();
    }

    /**
     * Xây dựng bố cục chính.
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
     * Xây dựng phần tiêu đề và các nút thao tác.
     *
     * @return panel tiêu đề
     */
    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel title = new JLabel("Bảng thông báo");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ACCENT);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(true);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));
        toolbar.setBackground(ACCENT_SOFT);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        btnRefresh = makeBtn("Làm mới", new Color(100,116,139), Color.WHITE);
        btnAdd     = makeBtn("Đăng bài",   SUCCESS_CLR,             Color.WHITE);
        btnEdit    = makeBtn("Sửa",      WARNING_CLR,             Color.WHITE);
        btnDelete  = makeBtn("Xóa",     DANGER_CLR,              Color.WHITE);

        if (!currentUser.isLeader()) {
            btnAdd.setVisible(false);
            btnEdit.setVisible(false);
            btnDelete.setVisible(false);
        }

        countLabel = new JLabel("Tổng: 0 thông báo");
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
     * Xây dựng vùng bảng thông báo.
     *
     * @return scroll pane chứa bảng
     */
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        announcementTable = new JTable(tableModel);
        styleTable(announcementTable);

        // Ẩn cột ID
        announcementTable.getColumnModel().getColumn(0).setMinWidth(0);
        announcementTable.getColumnModel().getColumn(0).setMaxWidth(0);
        announcementTable.getColumnModel().getColumn(0).setWidth(0);

        // Renderer trạng thái
        announcementTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(table, value, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (value != null && value.toString().equals("Đã ghim")) {
                    setForeground(new Color(239, 68, 68)); // Đỏ
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else {
                    setForeground(TEXT_GRAY);
                    setFont(new Font("Segoe UI", Font.PLAIN, 12));
                }
                return this;
            }
        });

        int[] colWidths = {0, 100, 360, 140, 160};
        for (int i = 0; i < colWidths.length; i++) {
            if (colWidths[i] > 0)
                announcementTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane sp = new JScrollPane(announcementTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    /**
     * Xây dựng thanh trạng thái.
     *
     * @return panel chân trang
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
     * Áp dụng style đồng nhất cho bảng.
     *
     * @param table bảng cần style
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
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    /**
     * Tạo nút trên thanh công cụ.
     *
     * @param text nhãn nút
     * @param bg màu nền
     * @param fg màu chữ
     * @return nút đã cấu hình
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

    // API công khai

    /**
     * Nạp danh sách thông báo vào bảng.
     *
     * @param data danh sách thông báo
     */
    public void loadData(List<AnnouncementDTO> data) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (AnnouncementDTO a : data) {
            String dateFormatted = a.getCreatedDate() != null ? a.getCreatedDate().format(formatter) : "";
            tableModel.addRow(new Object[]{
                a.getAnnouncementId(),
                Boolean.TRUE.equals(a.getIsPinned()) ? "Đã ghim" : "Bình thường",
                a.getTitle(),
                a.getAuthorName(),
                dateFormatted
            });
        }
        countLabel.setText("Tổng: " + data.size() + " thông báo");
        statusBar.setText("Đã tải " + data.size() + " thông báo.");
    }
    

    /**
     * Lấy id thông báo được chọn.
     *
     * @return id được chọn hoặc null
     */
    public Integer getSelectedId() {
        int row = announcementTable.getSelectedRow();
        if (row < 0) return null;
        return (Integer) tableModel.getValueAt(row, 0);
    }

    /**
     * Cập nhật thông báo trên status bar.
     *
     * @param msg nội dung hiển thị
     */
    public void setStatusMessage(String msg) { statusBar.setText(msg); }

    /**
     * Trả về panel gốc của view.
     *
     * @return panel chính
     */
    public JPanel getPanel() { return mainPanel; }

    /** @return nút thêm */
    public JButton getBtnAdd()     { return btnAdd; }

    /** @return nút sửa */
    public JButton getBtnEdit()    { return btnEdit; }

    /** @return nút xóa */
    public JButton getBtnDelete()  { return btnDelete; }

    /** @return nút làm mới */
    public JButton getBtnRefresh() { return btnRefresh; }

    /** @return bảng thông báo */
    public JTable  getTable()      { return announcementTable; }
}
