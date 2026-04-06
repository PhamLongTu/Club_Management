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

import com.clubmanagement.dto.DocumentDTO;
import com.clubmanagement.dto.MemberDTO;

/**
 * View for listing and managing documents.
 */
public class DocumentView {

    private static final String[] COLUMNS = {
        "ID", "Tên Tài liệu", "Đường dẫn File", "Liên kết với", "Quyền truy cập", "Người upload", "Ngày tải"
    };

    private JPanel mainPanel;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnOpen;
    private JTable documentTable;
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
    private static final Color ACCENT      = new Color(20, 184, 166);
    private static final Color ACCENT_SOFT = new Color(204, 251, 241);
    private static final Color ACCENT_ROW  = new Color(240, 253, 250);

    private final MemberDTO currentUser;

    /**
     * Creates the view for the current user.
     *
     * @param currentUser the logged-in member
     */
    public DocumentView(MemberDTO currentUser) {
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

        JLabel title = new JLabel("Quản lý Tài liệu & Hồ sơ");
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

        btnRefresh = makeBtn("Làm mới",  new Color(100,116,139), Color.WHITE);
        btnOpen    = makeBtn("Xem File", new Color(14, 165, 233), Color.WHITE);
        btnAdd     = makeBtn("Upload",    SUCCESS_CLR, Color.WHITE);
        btnEdit    = makeBtn("Sửa",       WARNING_CLR, Color.WHITE);
        btnDelete  = makeBtn("Xóa",      DANGER_CLR, Color.WHITE);

        if (!currentUser.isLeader()) {
            btnAdd.setVisible(false);
            btnEdit.setVisible(false);
            btnDelete.setVisible(false);
        }

        countLabel = new JLabel("Tổng: 0 File");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(TEXT_GRAY);

        rightPanel.add(countLabel);
        rightPanel.add(Box.createHorizontalStrut(8));
        rightPanel.add(btnRefresh);
        rightPanel.add(btnOpen);
        rightPanel.add(btnAdd);
        rightPanel.add(btnEdit);
        rightPanel.add(btnDelete);

        toolbar.add(rightPanel, BorderLayout.EAST);

        panel.add(title, BorderLayout.NORTH);
        panel.add(toolbar, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Builds the table container for documents.
     *
     * @return the scroll pane containing the table
     */
    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        documentTable = new JTable(tableModel);
        styleTable(documentTable);

        documentTable.getColumnModel().getColumn(0).setMinWidth(0);
        documentTable.getColumnModel().getColumn(0).setMaxWidth(0);
        documentTable.getColumnModel().getColumn(0).setWidth(0);

        documentTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(table, value, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                String val = value != null ? value.toString() : "";
                if (!sel) {
                    if ("Public (Công khai)".equals(val)) {
                        setBackground(new Color(220, 252, 231)); setForeground(new Color(22, 101, 52));
                    } else {
                        setBackground(new Color(254, 243, 199)); setForeground(new Color(180, 83, 9));
                    }
                }
                return this;
            }
        });

        int[] colWidths = {0, 250, 250, 150, 120, 150, 120};
        for (int i = 0; i < colWidths.length; i++) {
            if (colWidths[i] > 0)
                documentTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        JScrollPane sp = new JScrollPane(documentTable);
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
                if (col == 1 || col == 2) {
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                    if(col == 2) setForeground(new Color(14, 165, 233));
                    else setForeground(PRIMARY);
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
     * Loads document rows into the table.
     *
     * @param data document data list
     */
    public void loadData(List<DocumentDTO> data) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (DocumentDTO d : data) {
            String timeFormat = d.getUploadDate() != null ? d.getUploadDate().format(formatter) : "";
            String link = "Chung (N/A)";
            if (d.getEventName() != null) link = d.getEventName();
            else if (d.getProjectName() != null) link = d.getProjectName();
            
            String access = Boolean.TRUE.equals(d.getIsPublic()) ? "Public (Công khai)" : "Private (Nội bộ)";

            // Nếu user ko phải leader VÀ tài liệu là Private -> ẩn tài liệu ko cho xem
            if (!currentUser.isLeader() && Boolean.FALSE.equals(d.getIsPublic())) {
                continue; // Skip
            }

            tableModel.addRow(new Object[]{
                d.getDocumentId(),
                d.getTitle(),
                d.getFilePath(),
                link,
                access,
                d.getUploaderName(),
                timeFormat
            });
        }
        countLabel.setText("Tổng: " + tableModel.getRowCount() + " File");
        statusBar.setText("Đã tải " + tableModel.getRowCount() + " tài liệu cho bạn.");
    }

    /**
     * Gets the selected document id.
     *
     * @return selected id or null
     */
    public Integer getSelectedId() {
        int row = documentTable.getSelectedRow();
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

    /**
     * @return add button
     */
    public JButton getBtnAdd()         { return btnAdd; }

    /**
     * @return edit button
     */
    public JButton getBtnEdit()        { return btnEdit; }

    /**
     * @return delete button
     */
    public JButton getBtnDelete()      { return btnDelete; }

    /**
     * @return refresh button
     */
    public JButton getBtnRefresh()     { return btnRefresh; }

    /**
     * @return open button
     */
    public JButton getBtnOpen()        { return btnOpen; }

    /**
     * @return document table
     */
    public JTable  getTable()          { return documentTable; }
}
