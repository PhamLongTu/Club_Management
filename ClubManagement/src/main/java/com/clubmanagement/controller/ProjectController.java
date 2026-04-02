package com.clubmanagement.controller;

import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.dto.ProjectDTO;
import com.clubmanagement.service.MemberService;
import com.clubmanagement.service.ProjectService;
import com.clubmanagement.view.ProjectView;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * ProjectController - Bộ điều khiển màn hình Dự án.
 */
public class ProjectController {

    private final ProjectView    view;
    private final MemberDTO      currentUser;
    private final ProjectService projectService = new ProjectService();
    private final MemberService  memberService  = new MemberService();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ProjectController(ProjectView view, MemberDTO currentUser) {
        this.view        = view;
        this.currentUser = currentUser;
        attachListeners();
    }

    private void attachListeners() {
        view.getBtnRefresh().addActionListener(e -> loadAllProjects());
        view.getBtnSearch().addActionListener(e -> handleSearch());
        view.getSearchField().addActionListener(e -> handleSearch());
        view.getStatusFilterBox().addActionListener(e -> handleSearch());
        view.getBtnAdd().addActionListener(e -> handleAdd());
        view.getBtnEdit().addActionListener(e -> handleEdit());
        view.getBtnDelete().addActionListener(e -> handleDelete());
        view.getBtnMembers().addActionListener(e -> handleViewMembers());

        view.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (currentUser.isLeader()) handleEdit();
                    else handleViewMembers();
                }
            }
        });
    }

    /** Mở form Tạo dự án mới (gọi từ DashboardController quick action). */
    public void openAddDialog() { handleAdd(); }

    /** Tải tất cả dự án vào bảng. */
    public void loadAllProjects() {
        view.setStatusMessage("Đang tải dữ liệu...");
        SwingWorker<List<ProjectDTO>, Void> worker = new SwingWorker<>() {
            @Override protected List<ProjectDTO> doInBackground() { return projectService.getAllProjects(); }
            @Override protected void done() {
                try { view.loadData(get()); }
                catch (Exception e) { view.setStatusMessage("Lỗi: " + e.getMessage()); }
            }
        };
        worker.execute();
    }

    private void handleSearch() {
        String keyword = view.getSearchKeyword();
        String status  = (String) view.getStatusFilterBox().getSelectedItem();
        SwingWorker<List<ProjectDTO>, Void> worker = new SwingWorker<>() {
            @Override protected List<ProjectDTO> doInBackground() {
                List<ProjectDTO> results = projectService.searchProjects(keyword);
                if (!"Tất cả".equals(status)) {
                    results = results.stream().filter(p -> status.equals(p.getStatus())).toList();
                }
                return results;
            }
            @Override protected void done() {
                try { view.loadData(get()); }
                catch (Exception ex) { view.setStatusMessage("Lỗi: " + ex.getMessage()); }
            }
        };
        worker.execute();
    }

    private void handleAdd() {
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền tạo dự án!");
            return;
        }
        JPanel form = buildProjectForm(null);
        int res = JOptionPane.showConfirmDialog(null, form,
            "➕ Tạo dự án mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                ProjectFormData d = extractData(form);
                projectService.createProject(d.name, d.description, d.objective,
                    d.startDate, d.endDate, d.budget, currentUser.getMemberId());
                JOptionPane.showMessageDialog(null, "Đã tạo dự án!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAllProjects();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEdit() {
        Integer id = view.getSelectedProjectId();
        if (id == null) { JOptionPane.showMessageDialog(null, "Chưa chọn dự án!"); return; }

        Optional<ProjectDTO> opt = projectService.getProjectById(id);
        if (opt.isEmpty()) { JOptionPane.showMessageDialog(null, "Không tìm thấy dự án!"); return; }

        ProjectDTO project = opt.get();
        JPanel form = buildProjectForm(project);
        int res = JOptionPane.showConfirmDialog(null, form,
            "✏ Sửa dự án", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                ProjectFormData d = extractData(form);
                projectService.updateProject(id, d.name, d.description, d.objective,
                    d.startDate, d.endDate, d.budget, d.status, currentUser.getMemberId());
                JOptionPane.showMessageDialog(null, "Đã cập nhật!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAllProjects();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDelete() {
        Integer id = view.getSelectedProjectId();
        if (id == null) { JOptionPane.showMessageDialog(null, "Chưa chọn dự án!"); return; }

        int choice = JOptionPane.showConfirmDialog(null,
            "Xóa dự án này?", "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                projectService.deleteProject(id);
                JOptionPane.showMessageDialog(null, "Đã xóa dự án!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAllProjects();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Tạo panel form nhập liệu dự án. */
    private JPanel buildProjectForm(ProjectDTO project) {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(new javax.swing.border.EmptyBorder(10, 10, 10, 10));
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        JTextField tfName   = cf(project != null ? project.getProjectName() : "", f); tfName.setName("name");
        JTextField tfStart  = cf(project != null && project.getStartDate() != null ? project.getStartDate().toString() : "", f); tfStart.setName("start");
        JTextField tfEnd    = cf(project != null && project.getEndDate() != null ? project.getEndDate().toString() : "", f); tfEnd.setName("end");
        JTextField tfBudget = cf(project != null && project.getBudget() != null ? project.getBudget().toPlainString() : "0", f); tfBudget.setName("budget");
        JTextArea taDesc = new JTextArea(3, 20); taDesc.setName("desc"); taDesc.setFont(f); taDesc.setText(project != null ? project.getDescription() : ""); taDesc.setLineWrap(true);
        JTextArea taObj  = new JTextArea(3, 20); taObj.setName("obj");  taObj.setFont(f); taObj.setText(project != null ? project.getObjective()   : ""); taObj.setLineWrap(true);

        String[] statuses = {"Planning", "Active", "OnHold", "Completed", "Cancelled"};
        JComboBox<String> cbStatus = new JComboBox<>(statuses); cbStatus.setName("status"); cbStatus.setFont(f);
        if (project != null) cbStatus.setSelectedItem(project.getStatus());

        form.add(ml("Tên dự án *:")); form.add(tfName);
        form.add(ml("Ngày bắt đầu (yyyy-MM-dd):")); form.add(tfStart);
        form.add(ml("Ngày kết thúc (yyyy-MM-dd):")); form.add(tfEnd);
        form.add(ml("Ngân sách (VNĐ):")); form.add(tfBudget);
        form.add(ml("Trạng thái:")); form.add(cbStatus);
        form.add(ml("Mô tả:")); form.add(new JScrollPane(taDesc));
        form.add(ml("Mục tiêu:")); form.add(new JScrollPane(taObj));
        return form;
    }

    private ProjectFormData extractData(JPanel form) {
        ProjectFormData d = new ProjectFormData();
        for (Component c : form.getComponents()) {
            if (c instanceof JTextField tf) {
                switch (tf.getName()) {
                    case "name"   -> d.name   = tf.getText().trim();
                    case "start"  -> d.startDate = parseDate(tf.getText());
                    case "end"    -> d.endDate   = parseDate(tf.getText());
                    case "budget" -> { try { d.budget = new BigDecimal(tf.getText().trim()); } catch (Exception e) { d.budget = BigDecimal.ZERO; } }
                }
            } else if (c instanceof JComboBox<?> cb && "status".equals(cb.getName())) {
                d.status = (String) cb.getSelectedItem();
            } else if (c instanceof JScrollPane sp && sp.getViewport().getView() instanceof JTextArea ta) {
                if ("desc".equals(ta.getName())) d.description = ta.getText().trim();
                else if ("obj".equals(ta.getName())) d.objective = ta.getText().trim();
            }
        }
        if (d.name == null || d.name.isBlank()) throw new IllegalArgumentException("Tên dự án không được để trống!");
        return d;
    }

    private LocalDate parseDate(String text) {
        String t = text.trim();
        if (t.isEmpty()) return null;
        try { 
            return LocalDate.parse(t, java.time.format.DateTimeFormatter.ofPattern("yyyy-M-d")); 
        } catch (Exception e) { 
            throw new IllegalArgumentException("Ngày sai định dạng: '" + t + "'. Vui lòng dùng (yyyy-MM-dd)"); 
        }
    }

    private JTextField cf(String v, Font f) { JTextField tf = new JTextField(v); tf.setFont(f); return tf; }
    private JLabel ml(String text) { JLabel l = new JLabel(text); l.setFont(new Font("Segoe UI", Font.BOLD, 12)); return l; }

    private static class ProjectFormData {
        String name, description, objective, status = "Planning";
        LocalDate startDate, endDate;
        BigDecimal budget = BigDecimal.ZERO;
    }

    // ===================================================
    // XEM THÀNH VIÊN THAM GIA DỰ ÁN
    // ===================================================

    private void handleViewMembers() {
        Integer id = view.getSelectedProjectId();
        if (id == null) {
            JOptionPane.showMessageDialog(null,
                "Vui lòng chọn một dự án trước!", "Chưa chọn dự án",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lấy tên dự án từ bảng để hiển thị trên dialog title
        int row = view.getTable().getSelectedRow();
        String projectName = (String) view.getTable().getModel().getValueAt(row, 1);

        view.setStatusMessage("Đang tải danh sách thành viên...");
        SwingWorker<List<MemberDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<MemberDTO> doInBackground() {
                return projectService.getMembersOfProject(id);
            }
            @Override
            protected void done() {
                try {
                    List<MemberDTO> members = get();
                    view.setStatusMessage("Đã tải " + members.size() + " thành viên.");
                    showMembersDialog(projectName, members, id);
                } catch (Exception ex) {
                    view.setStatusMessage("Lỗi: " + ex.getMessage());
                    JOptionPane.showMessageDialog(null,
                        "Không thể tải danh sách thành viên:\n" + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showMembersDialog(String projectName, List<MemberDTO> members, Integer projectId) {
        final JTable[] tableRef = {null};
        
        // ---- Colors ----
        Color BG        = new Color(241, 245, 249);
        Color HEADER_BG = new Color(37,  99,  235);
        Color ROW_EVEN  = Color.WHITE;
        Color ROW_ODD   = new Color(248, 250, 252);
        Color TEXT_DARK = new Color(15,  23,  42);
        Color TEXT_GRAY = new Color(100, 116, 139);

        // ---- Dialog ----
        JDialog dialog = new JDialog((Frame) null, "👥 Thành viên dự án: " + projectName, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(780, 520);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(true);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);

        // ---- HEADER ----
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel titleLbl = new JLabel("👥 " + projectName);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);

        JLabel countLbl = new JLabel(members.size() + " thành viên");
        countLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        countLbl.setForeground(new Color(191, 219, 254));

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);
        headerText.add(titleLbl);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(countLbl);

        header.add(headerText, BorderLayout.CENTER);

        // ---- THÊM / XÓA THÀNH VIÊN BUTTONS (Nếu Leader/Admin) ----
        if (currentUser.isLeader()) {
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actionPanel.setOpaque(false);

            // 1. Nút Thêm
            JButton btnAddMem = new JButton("➕ Thêm thành viên");
            btnAddMem.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnAddMem.setBackground(new Color(16, 185, 129));
            btnAddMem.setForeground(Color.WHITE);
            btnAddMem.setBorderPainted(false);
            btnAddMem.setFocusPainted(false);
            btnAddMem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btnAddMem.addActionListener(e -> {
                // Tải all members để cho phép add
                List<MemberDTO> allMembers = memberService.getAllMembers();
                List<Integer> existingIds = members.stream().map(MemberDTO::getMemberId).toList();

                List<MemberDTO> available = allMembers.stream()
                        .filter(m -> !existingIds.contains(m.getMemberId()))
                        .toList();

                if (available.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Không còn thành viên nào để thêm!");
                    return;
                }

                JComboBox<MemberDTO> cboMembers = new JComboBox<>(available.toArray(new MemberDTO[0]));
                int res = JOptionPane.showConfirmDialog(dialog, cboMembers, "Chọn thành viên để thêm", JOptionPane.OK_CANCEL_OPTION);
                if (res == JOptionPane.OK_OPTION) {
                    MemberDTO selected = (MemberDTO) cboMembers.getSelectedItem();
                    if (selected != null) {
                        try {
                            projectService.addMemberToProject(projectId, selected.getMemberId());
                            JOptionPane.showMessageDialog(dialog, "Đã thêm thành viên!");
                            dialog.dispose();
                            handleViewMembers(); // reload lists and reopen dialog
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });

            // 2. Nút Xóa
            JButton btnDeleteMem = new JButton("➖ Xóa thành viên");
            btnDeleteMem.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnDeleteMem.setBackground(new Color(239, 68, 68)); // DANGER_CLR
            btnDeleteMem.setForeground(Color.WHITE);
            btnDeleteMem.setBorderPainted(false);
            btnDeleteMem.setFocusPainted(false);
            btnDeleteMem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btnDeleteMem.addActionListener(e -> {
                if (tableRef[0] == null) {
                    JOptionPane.showMessageDialog(dialog, "Không có thành viên nào để xóa!");
                    return;
                }
                int selectedRow = tableRef[0].getSelectedRow();
                if (selectedRow < 0) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng chọn một thành viên trong bảng để xóa!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                MemberDTO selectedMember = members.get(selectedRow);
                int confirm = JOptionPane.showConfirmDialog(dialog, 
                    "Bạn có chắc muốn xóa thành viên " + selectedMember.getFullName() + " khỏi dự án này?", 
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        projectService.removeMemberFromProject(projectId, selectedMember.getMemberId());
                        JOptionPane.showMessageDialog(dialog, "Đã xóa thành viên khỏi dự án!");
                        dialog.dispose();
                        handleViewMembers(); // reload lists and reopen dialog
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            actionPanel.add(btnAddMem);
            actionPanel.add(btnDeleteMem);
            header.add(actionPanel, BorderLayout.EAST);
        }

        root.add(header, BorderLayout.NORTH);

        // ---- TABLE ----
        if (members.isEmpty()) {
            JLabel emptyLbl = new JLabel("Dự án này chưa có thành viên nào.", SwingConstants.CENTER);
            emptyLbl.setFont(new Font("Segoe UI", Font.ITALIC, 15));
            emptyLbl.setForeground(TEXT_GRAY);
            emptyLbl.setBorder(new EmptyBorder(60, 0, 0, 0));
            root.add(emptyLbl, BorderLayout.CENTER);
        } else {
            String[] cols = {"", "Họ tên", "MSSV", "Email", "Vai trò", "Trạng thái", "Ngày tham gia"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
                @Override public Class<?> getColumnClass(int c) {
                    return c == 0 ? JLabel.class : String.class;
                }
            };

            for (MemberDTO m : members) {
                String joinDate = m.getJoinDate() != null ? m.getJoinDate().toString() : "N/A";
                model.addRow(new Object[]{
                    m.getFullName(),        // col 0: dùng cho avatar renderer
                    m.getFullName(),
                    m.getStudentId() != null ? m.getStudentId() : "—",
                    m.getEmail() != null    ? m.getEmail()      : "—",
                    m.getRoleName()  != null ? m.getRoleName()  : "Member",
                    m.getStatus()    != null ? m.getStatus()    : "Active",
                    joinDate
                });
            }

            JTable table = new JTable(model);
            tableRef[0] = table;
            table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            table.setRowHeight(52);
            table.setShowGrid(false);
            table.setIntercellSpacing(new Dimension(0, 0));
            table.setSelectionBackground(new Color(219, 234, 254));
            table.setSelectionForeground(TEXT_DARK);
            table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JTableHeader th = table.getTableHeader();
            th.setFont(new Font("Segoe UI", Font.BOLD, 12));
            th.setBackground(new Color(248, 250, 252));
            th.setForeground(new Color(71, 85, 105));
            th.setPreferredSize(new Dimension(0, 40));

            // Column widths
            int[] widths = {52, 180, 100, 200, 100, 90, 110};
            for (int i = 0; i < widths.length; i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }

            // Avatar renderer (cột 0)
            Color[] avatarColors = {
                new Color(99,102,241), new Color(16,185,129), new Color(245,158,11),
                new Color(239,68,68),  new Color(6,182,212),  new Color(168,85,247)
            };
            table.getColumnModel().getColumn(0).setCellRenderer((t, val, sel, foc, row2, col) -> {
                String name2 = val != null ? val.toString() : "?";
                char initial = name2.trim().isEmpty() ? '?' : name2.trim().charAt(0);
                Color avatarBg = avatarColors[row2 % avatarColors.length];

                JPanel cell = new JPanel(new GridBagLayout());
                cell.setBackground(sel ? new Color(219,234,254) : (row2 % 2 == 0 ? ROW_EVEN : ROW_ODD));
                cell.setBorder(new EmptyBorder(4, 8, 4, 4));

                JLabel avatar = new JLabel(String.valueOf(initial).toUpperCase(), SwingConstants.CENTER) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(avatarBg);
                        g2.fillOval(0, 0, getWidth(), getHeight());
                        super.paintComponent(g);
                    }
                };
                avatar.setFont(new Font("Segoe UI", Font.BOLD, 16));
                avatar.setForeground(Color.WHITE);
                avatar.setOpaque(false);
                avatar.setPreferredSize(new Dimension(36, 36));
                cell.add(avatar);
                return cell;
            });

            // Status renderer (cột 5)
            table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t2, Object val,
                        boolean sel, boolean foc, int row2, int col) {
                    super.getTableCellRendererComponent(t2, val, sel, foc, row2, col);
                    setHorizontalAlignment(CENTER);
                    String s = val != null ? val.toString() : "";
                    if (!sel) {
                        switch (s) {
                            case "Active"    -> { setBackground(new Color(220,252,231)); setForeground(new Color(22,101,52)); }
                            case "Inactive"  -> { setBackground(new Color(241,245,249)); setForeground(new Color(71,85,105)); }
                            case "Suspended" -> { setBackground(new Color(254,226,226)); setForeground(new Color(185,28,28)); }
                            default          -> { setBackground(Color.WHITE); setForeground(TEXT_GRAY); }
                        }
                    }
                    setFont(new Font("Segoe UI", Font.BOLD, 11));
                    setBorder(new EmptyBorder(4, 8, 4, 8));
                    return this;
                }
            });

            // Default alternating row renderer for other cols
            DefaultTableCellRenderer altRenderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t2, Object val,
                        boolean sel, boolean foc, int row2, int col) {
                    super.getTableCellRendererComponent(t2, val, sel, foc, row2, col);
                    if (!sel) {
                        setBackground(row2 % 2 == 0 ? ROW_EVEN : ROW_ODD);
                        setForeground(TEXT_DARK);
                    }
                    setBorder(new EmptyBorder(0, 8, 0, 8));
                    return this;
                }
            };
            for (int c = 1; c < cols.length; c++) {
                if (c != 5) table.getColumnModel().getColumn(c).setCellRenderer(altRenderer);
            }

            JScrollPane sp = new JScrollPane(table);
            sp.setBorder(BorderFactory.createLineBorder(new Color(226,232,240)));
            sp.getViewport().setBackground(Color.WHITE);

            JPanel tableWrap = new JPanel(new BorderLayout());
            tableWrap.setBackground(BG);
            tableWrap.setBorder(new EmptyBorder(16, 20, 0, 20));
            tableWrap.add(sp, BorderLayout.CENTER);
            root.add(tableWrap, BorderLayout.CENTER);
        }

        // ---- FOOTER ----
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(BG);
        footer.setBorder(new EmptyBorder(12, 20, 16, 20));

        JButton btnClose = new JButton("✕ Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.setBackground(new Color(100, 116, 139));
        btnClose.setForeground(Color.WHITE);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(110, 36));
        btnClose.addActionListener(e -> dialog.dispose());
        footer.add(btnClose);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }
}
