package com.clubmanagement.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.dto.ProjectDTO;
import com.clubmanagement.service.MemberService;
import com.clubmanagement.service.ProjectService;
import com.clubmanagement.util.UiFormUtil;
import com.clubmanagement.view.ProjectView;
import com.toedter.calendar.JDateChooser;

/**
 * ProjectController - Bộ điều khiển màn hình Dự án.
 */
public class ProjectController {

    private final ProjectView view;
    private final MemberDTO currentUser;
    private final ProjectService projectService = new ProjectService();
    private final MemberService memberService = new MemberService();

    /**
     * Khởi tạo controller cho màn hình Dự án.
     * @param view View hiển thị
     * @param currentUser Người dùng hiện tại
     */
    public ProjectController(ProjectView view, MemberDTO currentUser) {
        this.view = view;
        this.currentUser = currentUser;
        attachListeners();
    }

    /**
     * Đăng ký các sự kiện cho view.
     */
    private void attachListeners() {
        view.getBtnRefresh().addActionListener(e -> loadAllProjects());
        view.getBtnSearch().addActionListener(e -> handleSearch());
        view.getSearchField().addActionListener(e -> handleSearch());
        view.getStatusFilterBox().addActionListener(e -> handleSearch());
        view.getAssignmentFilterBox().addActionListener(e -> handleSearch());
        view.getBtnAdd().addActionListener(e -> handleAdd());
        view.getBtnEdit().addActionListener(e -> handleEdit());
        view.getBtnDelete().addActionListener(e -> handleDelete());

        view.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) handleViewDetail();
            }
        });
    }

    /** Mở form Tạo dự án mới (gọi từ DashboardController quick action). */
    public void openAddDialog() {
        handleAdd();
    }

    /** Tải danh sách dự án theo bộ lọc hiện tại. */
    public void loadAllProjects() {
        handleSearch();
    }

    /**
     * Tìm kiếm và lọc dự án theo điều kiện hiện tại.
     */
    private void handleSearch() {
        String keyword = view.getSearchKeyword();
        String status = (String) view.getStatusFilterBox().getSelectedItem();
        String assignment = (String) view.getAssignmentFilterBox().getSelectedItem();

        SwingWorker<List<ProjectDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ProjectDTO> doInBackground() {
                List<ProjectDTO> results;
                if (currentUser.isLeader()) {
                    results = projectService.searchProjects(keyword);
                } else {
                    results = projectService.getVisibleProjectsForUser(currentUser.getMemberId());
                    if (keyword != null && !keyword.isBlank()) {
                        String kw = keyword.toLowerCase();
                        results = results.stream()
                            .filter(p -> (p.getProjectName() != null && p.getProjectName().toLowerCase().contains(kw))
                                      || (p.getDescription() != null && p.getDescription().toLowerCase().contains(kw)))
                            .toList();
                    }
                }

                if (!"Tất cả".equals(status)) {
                    results = results.stream().filter(p -> status.equals(p.getStatus())).toList();
                }

                if (assignment != null) {
                    switch (assignment) {
                        case "Chưa chỉ định (Public)" -> results = results.stream()
                            .filter(p -> "Public".equalsIgnoreCase(p.getVisibility())
                                && p.getMemberCount() != null
                                && p.getMemberCount() == 0)
                            .toList();
                        case "Đã chỉ định (Public/Private)" -> results = results.stream()
                            .filter(p -> p.getMemberCount() != null && p.getMemberCount() > 0)
                            .toList();
                        case "Dự án của tôi" -> {
                            List<ProjectDTO> mine = projectService.getProjectsForUser(currentUser.getMemberId());
                            java.util.Set<Integer> mineIds = mine.stream()
                                .map(ProjectDTO::getProjectId)
                                .collect(java.util.stream.Collectors.toSet());
                            results = results.stream()
                                .filter(p -> mineIds.contains(p.getProjectId()))
                                .toList();
                        }
                        default -> { }
                    }
                }

                return results;
            }

            @Override
            protected void done() {
                try {
                    view.loadData(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    view.setStatusMessage("Lỗi: " + ex.getMessage());
                } catch (ExecutionException ex) {
                    view.setStatusMessage("Lỗi: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Mở form thêm dự án.
     */
    private void handleAdd() {
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền tạo dự án!");
            return;
        }
        showProjectDialog(null, false);
    }

    /**
     * Mở form sửa dự án.
     */
    private void handleEdit() {
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền sửa dự án!");
            return;
        }
        Integer id = view.getSelectedProjectId();
        if (id == null) {
            JOptionPane.showMessageDialog(null, "Chưa chọn dự án!");
            return;
        }

        Optional<ProjectDTO> opt = projectService.getProjectById(id);
        if (opt.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Không tìm thấy dự án!");
            return;
        }
        showProjectDialog(opt.get(), true);
    }

    /**
     * Mở dialog thêm/sửa dự án.
     * @param project Dữ liệu hiện tại (nullable)
     * @param isEdit true nếu sửa
     */
    private void showProjectDialog(ProjectDTO project, boolean isEdit) {
        ProjectFormFields fields = buildProjectForm(project);
        String title = isEdit ? "Sửa dự án" : "Tạo dự án mới";

        JDialog dialog = new JDialog((Frame) null, title, true);
        dialog.setSize(780, 620);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());

        dialog.add(UiFormUtil.buildDialogHeader(title), BorderLayout.NORTH);
        dialog.add(fields.panel, BorderLayout.CENTER);
        dialog.add(buildDialogFooter(dialog, fields, project, isEdit), BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Tạo footer cho dialog.
     * @param dialog Dialog hiện tại
     * @param fields Trường dữ liệu form
     * @param project Dữ liệu hiện tại
     * @param isEdit true nếu sửa
     * @return JPanel footer
     */
    private JPanel buildDialogFooter(JDialog dialog, ProjectFormFields fields, ProjectDTO project, boolean isEdit) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(new EmptyBorder(8, 16, 12, 16));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = new JButton(isEdit ? "Cập nhật" : "Tạo mới");
        btnSave.setBackground(new Color(16, 185, 129));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBorderPainted(false);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> {
            try {
                ProjectFormData d = extractData(fields);
                if (isEdit) {
                    projectService.updateProject(project.getProjectId(), d.name, d.description, d.objective,
                        d.startDate, d.endDate, d.budget, d.status, d.visibility,
                        d.maxMembers, currentUser.getMemberId(), d.memberIds, d.contributionPoints);
                    JOptionPane.showMessageDialog(dialog, "Đã cập nhật!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    projectService.createProject(d.name, d.description, d.objective,
                        d.startDate, d.endDate, d.budget, d.visibility, d.maxMembers,
                        currentUser.getMemberId(), d.memberIds, d.contributionPoints);
                    JOptionPane.showMessageDialog(dialog, "Đã tạo dự án!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
                loadAllProjects();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    /**
     * Xóa dự án đang chọn.
     */
    private void handleDelete() {
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền xóa dự án!");
            return;
        }
        Integer id = view.getSelectedProjectId();
        if (id == null) {
            JOptionPane.showMessageDialog(null, "Chưa chọn dự án!");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(null,
            "Xóa dự án này?", "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                projectService.deleteProject(id);
                JOptionPane.showMessageDialog(null, "Đã xóa dự án!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAllProjects();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Tạo panel form nhập liệu dự án. */
    private ProjectFormFields buildProjectForm(ProjectDTO project) {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(new EmptyBorder(12, 16, 12, 16));
        form.setBackground(Color.WHITE);
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        ProjectFormFields fields = new ProjectFormFields();
        fields.panel = form;

        fields.tfName = createField(project != null ? project.getProjectName() : "", f);
        fields.tfBudget = createField(project != null && project.getBudget() != null ? project.getBudget().toPlainString() : "0", f);

        fields.startDate = UiFormUtil.createDateChooser(project != null ? project.getStartDate() : null);
        fields.endDate = UiFormUtil.createDateChooser(project != null ? project.getEndDate() : null);

        fields.taDesc = new JTextArea(3, 20);
        fields.taDesc.setFont(f);
        fields.taDesc.setText(project != null ? project.getDescription() : "");
        fields.taDesc.setLineWrap(true);

        fields.taObj = new JTextArea(3, 20);
        fields.taObj.setFont(f);
        fields.taObj.setText(project != null ? project.getObjective() : "");
        fields.taObj.setLineWrap(true);

        String[] statuses = {"Planning", "Active", "OnHold", "Completed", "Cancelled"};
        fields.cbStatus = new JComboBox<>(statuses);
        fields.cbStatus.setFont(f);
        if (project != null) fields.cbStatus.setSelectedItem(project.getStatus());

        fields.cbVisibility = new JComboBox<>(new String[]{"Public", "Private"});
        fields.cbVisibility.setFont(f);
        if (project != null && project.getVisibility() != null) fields.cbVisibility.setSelectedItem(project.getVisibility());

        fields.spMaxMembers = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
        fields.spMaxMembers.setFont(f);
        if (project != null && project.getMaxMembers() != null) fields.spMaxMembers.setValue(project.getMaxMembers());

        fields.spContribution = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
        fields.spContribution.setFont(f);
        if (project != null && project.getContributionPoints() != null) {
            fields.spContribution.setValue(project.getContributionPoints());
        }

        List<MemberDTO> allMembers = memberService.getAllMembers();
        fields.listMembers = new JList<>(allMembers.toArray(MemberDTO[]::new));
        fields.listMembers.setFont(f);
        fields.listMembers.setVisibleRowCount(4);
        fields.listMembers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (project != null) {
            List<MemberDTO> currentMembers = projectService.getMembersOfProject(project.getProjectId());
            java.util.Set<Integer> memberIds = currentMembers.stream()
                .map(MemberDTO::getMemberId)
                .collect(java.util.stream.Collectors.toSet());
            int[] indices = java.util.stream.IntStream.range(0, fields.listMembers.getModel().getSize())
                .filter(i -> memberIds.contains(fields.listMembers.getModel().getElementAt(i).getMemberId()))
                .toArray();
            fields.listMembers.setSelectedIndices(indices);
        }

        form.add(makeLabel("Tên dự án *:")); form.add(fields.tfName);
        form.add(makeLabel("Ngày bắt đầu:")); form.add(fields.startDate);
        form.add(makeLabel("Ngày kết thúc:")); form.add(fields.endDate);
        form.add(makeLabel("Ngân sách (VNĐ):")); form.add(fields.tfBudget);
        form.add(makeLabel("Trạng thái:")); form.add(fields.cbStatus);
        form.add(makeLabel("Hiển thị:")); form.add(fields.cbVisibility);
        form.add(makeLabel("Số thành viên tối đa:")); form.add(fields.spMaxMembers);
        form.add(makeLabel("Điểm đóng góp:")); form.add(fields.spContribution);
        form.add(makeLabel("Thành viên:")); form.add(new JScrollPane(fields.listMembers));
        form.add(makeLabel("Mô tả:")); form.add(new JScrollPane(fields.taDesc));
        form.add(makeLabel("Mục tiêu:")); form.add(new JScrollPane(fields.taObj));
        return fields;
    }

    /**
     * Đọc dữ liệu từ form và validate.
     * @param fields Trường dữ liệu form
     * @return Dữ liệu form
     */
    private ProjectFormData extractData(ProjectFormFields fields) {
        ProjectFormData d = new ProjectFormData();
        d.name = fields.tfName.getText().trim();
        d.startDate = toLocalDate(fields.startDate.getDate());
        d.endDate = toLocalDate(fields.endDate.getDate());
        d.description = fields.taDesc.getText().trim();
        d.objective = fields.taObj.getText().trim();
        d.status = (String) fields.cbStatus.getSelectedItem();
        d.visibility = (String) fields.cbVisibility.getSelectedItem();
        d.maxMembers = (Integer) fields.spMaxMembers.getValue();
        d.memberIds = fields.listMembers.getSelectedValuesList().stream()
            .map(MemberDTO::getMemberId)
            .toList();
        d.contributionPoints = (Integer) fields.spContribution.getValue();
        try { d.budget = new BigDecimal(fields.tfBudget.getText().trim()); }
        catch (NumberFormatException e) { d.budget = BigDecimal.ZERO; }

        if (d.name == null || d.name.isBlank()) {
            throw new IllegalArgumentException("Tên dự án không được để trống!");
        }
        return d;
    }


    /**
     * Tạo TextField với giá trị mặc định.
     * @param v Giá trị ban đầu
     * @param f Font áp dụng
     * @return JTextField
     */
    private JTextField createField(String v, Font f) {
        JTextField tf = new JTextField(v);
        tf.setFont(f);
        return tf;
    }

    /**
     * Tạo label cho form nhập liệu.
     * @param text Nội dung
     * @return JLabel
     */
    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    private static class ProjectFormData {
        String name, description, objective, status = "Planning";
        String visibility = "Public";
        LocalDate startDate, endDate;
        BigDecimal budget = BigDecimal.ZERO;
        Integer maxMembers = 0;
        List<Integer> memberIds = java.util.Collections.emptyList();
        Integer contributionPoints = 0;
    }

    private static class ProjectFormFields {
        JPanel panel;
        JTextField tfName;
        JTextField tfBudget;
        JDateChooser startDate;
        JDateChooser endDate;
        JTextArea taDesc;
        JTextArea taObj;
        JComboBox<String> cbStatus;
        JComboBox<String> cbVisibility;
        JSpinner spMaxMembers;
        JSpinner spContribution;
        JList<MemberDTO> listMembers;
    }

    /**
     * Chuyển Date sang LocalDate.
     * @param date Ngày
     * @return LocalDate hoặc null
     */
    private LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Mở dialog chi tiết dự án từ dòng đang chọn.
     */
    private void handleViewDetail() {
        openDetailById(view.getSelectedProjectId(), null);
    }

    /**
     * Mở dialog chi tiết dự án theo ID.
     * @param id ID dự án
     * @param afterClose Callback sau khi đóng (nullable)
     */
    public void openDetailById(Integer id, Runnable afterClose) {
        if (id == null) return;
        Optional<ProjectDTO> opt = projectService.getProjectById(id);
        if (opt.isEmpty()) return;
        ProjectDTO project = opt.get();

        view.setStatusMessage("Đang tải chi tiết dự án...");
        SwingWorker<List<MemberDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<MemberDTO> doInBackground() {
                return projectService.getMembersOfProject(id);
            }

            @Override
            protected void done() {
                try {
                    List<MemberDTO> members = get();
                    showDetailDialog(project, members, afterClose);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(null,
                        "Không thể tải chi tiết dự án:\n" + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException ex) {
                    JOptionPane.showMessageDialog(null,
                        "Không thể tải chi tiết dự án:\n" + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * Hiển thị dialog chi tiết dự án.
     * @param project Dữ liệu dự án
     * @param members Danh sách thành viên
     * @param afterClose Callback sau khi đóng (nullable)
     */
    private void showDetailDialog(ProjectDTO project, List<MemberDTO> members, Runnable afterClose) {
        JDialog dialog = new JDialog((Frame) null, "Chi tiết dự án", true);
        dialog.setSize(760, 560);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel(project.getProjectName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        String metaText = "Quản lý: " + project.getManagerName();
        JLabel meta = new JLabel(metaText);
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        meta.setForeground(new Color(100, 116, 139));

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);
        headerText.add(title);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(meta);

        header.add(headerText, BorderLayout.CENTER);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(8, 20, 16, 20));

        int memberCountValue = safeInt(project.getMemberCount());
        String memberCount = memberCountValue + " thành viên";
        Integer maxMembersValue = project.getMaxMembers();
        String maxText = maxMembersValue != null && maxMembersValue > 0
            ? maxMembersValue.toString()
            : "Không giới hạn";

        content.add(UiFormUtil.makeInfoLabel("Trạng thái: " + project.getStatus()));
        content.add(UiFormUtil.makeInfoLabel("Hiển thị: " + project.getVisibility()));
        content.add(UiFormUtil.makeInfoLabel("Số thành viên tối đa: " + maxText));
        content.add(UiFormUtil.makeInfoLabel("Thành viên: " + memberCount));
        content.add(UiFormUtil.makeInfoLabel("Điểm đóng góp: " + safeInt(project.getContributionPoints())));
        content.add(UiFormUtil.makeInfoLabel("Ngân sách: " + (project.getBudget() != null ? project.getBudget().toPlainString() : "0") + " VND"));
        content.add(UiFormUtil.makeInfoLabel("Bắt đầu: " + (project.getStartDate() != null ? project.getStartDate() : "")));
        content.add(UiFormUtil.makeInfoLabel("Kết thúc: " + (project.getEndDate() != null ? project.getEndDate() : "")));
        content.add(Box.createVerticalStrut(8));

        JTextArea desc = new JTextArea(project.getDescription() != null ? project.getDescription() : "");
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        desc.setBorder(new EmptyBorder(8, 8, 8, 8));

        JTextArea obj = new JTextArea(project.getObjective() != null ? project.getObjective() : "");
        obj.setEditable(false);
        obj.setLineWrap(true);
        obj.setWrapStyleWord(true);
        obj.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        obj.setBorder(new EmptyBorder(8, 8, 8, 8));

        content.add(new JLabel("Mô tả:"));
        content.add(new JScrollPane(desc));
        content.add(Box.createVerticalStrut(8));
        content.add(new JLabel("Mục tiêu:"));
        content.add(new JScrollPane(obj));
        content.add(Box.createVerticalStrut(8));

        DefaultListModel<String> memberList = new DefaultListModel<>();
        for (MemberDTO m : members) memberList.addElement(m.getFullName() + " [" + m.getRoleName() + "]");
        JList<String> memberJList = new JList<>(memberList);
        memberJList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        memberJList.setVisibleRowCount(5);
        content.add(new JLabel("Danh sách thành viên:"));
        content.add(new JScrollPane(memberJList));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());
        footer.add(btnClose);

        boolean alreadyInProject = members.stream().anyMatch(m -> m.getMemberId().equals(currentUser.getMemberId()));
        if (alreadyInProject) {
            JButton btnCancel = new JButton("Hủy đăng ký");
            btnCancel.setBackground(new Color(239, 68, 68));
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setBorderPainted(false);
            btnCancel.setFocusPainted(false);
            btnCancel.addActionListener(e -> handleCancelProject(project, dialog));
            footer.add(btnCancel, 0);
        }

        int maxMembers = safeInt(project.getMaxMembers());
        int current = safeInt(project.getMemberCount());
        boolean canRegister = "Public".equalsIgnoreCase(project.getVisibility())
            && !alreadyInProject
            && (maxMembers <= 0 || current < maxMembers);

        if (canRegister) {
            JButton btnRegister = new JButton("Đăng ký tham gia dự án");
            btnRegister.setBackground(new Color(16, 185, 129));
            btnRegister.setForeground(Color.WHITE);
            btnRegister.setBorderPainted(false);
            btnRegister.setFocusPainted(false);
            btnRegister.addActionListener(e -> {
                try {
                    projectService.registerForProject(project.getProjectId(), currentUser.getMemberId());
                    JOptionPane.showMessageDialog(dialog, "Đăng ký thành công!");
                    dialog.dispose();
                    loadAllProjects();
                } catch (RuntimeException ex) {
                    JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
            footer.add(btnRegister, 0);
        }

        if (afterClose != null) {
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                private boolean handled = false;
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    if (handled) return;
                    handled = true;
                    afterClose.run();
                }

                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    if (handled) return;
                    handled = true;
                    afterClose.run();
                }
            });
        }

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(new JScrollPane(content), BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Hủy đăng ký dự án.
     * @param project Dữ liệu dự án
     * @param dialog Dialog đang hiển thị
     */
    private void handleCancelProject(ProjectDTO project, JDialog dialog) {
        if (project.getStartDate() != null && !project.getStartDate().isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(dialog,
                "Dự án đã bắt đầu, không thể hủy.",
                "Từ chối", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(dialog,
            "Bạn có chắc muốn hủy đăng ký dự án này?",
            "Xác nhận hủy", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                projectService.unregisterFromProject(project.getProjectId(), currentUser.getMemberId());
                JOptionPane.showMessageDialog(dialog, "Đã hủy đăng ký dự án!");
                dialog.dispose();
                loadAllProjects();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Trả về giá trị int an toàn.
     * @param value Giá trị nullable
     * @return Giá trị int
     */
    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }
}
