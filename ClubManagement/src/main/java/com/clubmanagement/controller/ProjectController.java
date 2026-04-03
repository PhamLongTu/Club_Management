package com.clubmanagement.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
import com.clubmanagement.view.ProjectView;

/**
 * ProjectController - Bộ điều khiển màn hình Dự án.
 */
public class ProjectController {

    private final ProjectView view;
    private final MemberDTO currentUser;
    private final ProjectService projectService = new ProjectService();
    private final MemberService memberService = new MemberService();

    public ProjectController(ProjectView view, MemberDTO currentUser) {
        this.view = view;
        this.currentUser = currentUser;
        attachListeners();
    }

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

    private void handleAdd() {
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền tạo dự án!");
            return;
        }
        JPanel form = buildProjectForm(null);
        int res = JOptionPane.showConfirmDialog(null, form,
            "Tạo dự án mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                ProjectFormData d = extractData(form);
                projectService.createProject(d.name, d.description, d.objective,
                    d.startDate, d.endDate, d.budget, d.visibility, d.maxMembers,
                    currentUser.getMemberId(), d.memberIds);
                JOptionPane.showMessageDialog(null, "Đã tạo dự án!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAllProjects();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

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

        ProjectDTO project = opt.get();
        JPanel form = buildProjectForm(project);
        int res = JOptionPane.showConfirmDialog(null, form,
            "Sửa dự án", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                ProjectFormData d = extractData(form);
                projectService.updateProject(id, d.name, d.description, d.objective,
                    d.startDate, d.endDate, d.budget, d.status, d.visibility,
                    d.maxMembers, currentUser.getMemberId(), d.memberIds);
                JOptionPane.showMessageDialog(null, "Đã cập nhật!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAllProjects();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

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
    private JPanel buildProjectForm(ProjectDTO project) {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        JTextField tfName = createField(project != null ? project.getProjectName() : "", f); tfName.setName("name");
        JTextField tfStart = createField(project != null && project.getStartDate() != null ? project.getStartDate().toString() : "", f); tfStart.setName("start");
        JTextField tfEnd = createField(project != null && project.getEndDate() != null ? project.getEndDate().toString() : "", f); tfEnd.setName("end");
        JTextField tfBudget = createField(project != null && project.getBudget() != null ? project.getBudget().toPlainString() : "0", f); tfBudget.setName("budget");

        JTextArea taDesc = new JTextArea(3, 20); taDesc.setName("desc"); taDesc.setFont(f); taDesc.setText(project != null ? project.getDescription() : ""); taDesc.setLineWrap(true);
        JTextArea taObj = new JTextArea(3, 20); taObj.setName("obj"); taObj.setFont(f); taObj.setText(project != null ? project.getObjective() : ""); taObj.setLineWrap(true);

        String[] statuses = {"Planning", "Active", "OnHold", "Completed", "Cancelled"};
        JComboBox<String> cbStatus = new JComboBox<>(statuses); cbStatus.setName("status"); cbStatus.setFont(f);
        if (project != null) cbStatus.setSelectedItem(project.getStatus());

        JComboBox<String> cbVisibility = new JComboBox<>(new String[]{"Public", "Private"});
        cbVisibility.setFont(f);
        cbVisibility.setName("visibility");
        if (project != null && project.getVisibility() != null) cbVisibility.setSelectedItem(project.getVisibility());

        JSpinner spMaxMembers = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
        spMaxMembers.setFont(f);
        spMaxMembers.setName("maxMembers");
        if (project != null && project.getMaxMembers() != null) spMaxMembers.setValue(project.getMaxMembers());

        List<MemberDTO> allMembers = memberService.getAllMembers();
        JList<MemberDTO> listMembers = new JList<>(allMembers.toArray(MemberDTO[]::new));
        listMembers.setFont(f);
        listMembers.setVisibleRowCount(4);
        listMembers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listMembers.setName("members");
        if (project != null) {
            List<MemberDTO> currentMembers = projectService.getMembersOfProject(project.getProjectId());
            java.util.Set<Integer> memberIds = currentMembers.stream()
                .map(MemberDTO::getMemberId)
                .collect(java.util.stream.Collectors.toSet());
            int[] indices = java.util.stream.IntStream.range(0, listMembers.getModel().getSize())
                .filter(i -> memberIds.contains(listMembers.getModel().getElementAt(i).getMemberId()))
                .toArray();
            listMembers.setSelectedIndices(indices);
        }

        form.add(makeLabel("Tên dự án *:")); form.add(tfName);
        form.add(makeLabel("Ngày bắt đầu (yyyy-MM-dd):")); form.add(tfStart);
        form.add(makeLabel("Ngày kết thúc (yyyy-MM-dd):")); form.add(tfEnd);
        form.add(makeLabel("Ngân sách (VNĐ):")); form.add(tfBudget);
        form.add(makeLabel("Trạng thái:")); form.add(cbStatus);
        form.add(makeLabel("Hiển thị:")); form.add(cbVisibility);
        form.add(makeLabel("Số thành viên tối đa:")); form.add(spMaxMembers);
        form.add(makeLabel("Thành viên:")); form.add(new JScrollPane(listMembers));
        form.add(makeLabel("Mô tả:")); form.add(new JScrollPane(taDesc));
        form.add(makeLabel("Mục tiêu:")); form.add(new JScrollPane(taObj));
        return form;
    }

    private ProjectFormData extractData(JPanel form) {
        ProjectFormData d = new ProjectFormData();
        for (Component c : form.getComponents()) {
            if (c instanceof JTextField tf) {
                switch (tf.getName()) {
                    case "name" -> d.name = tf.getText().trim();
                    case "start" -> d.startDate = parseDate(tf.getText());
                    case "end" -> d.endDate = parseDate(tf.getText());
                    case "budget" -> {
                        try { d.budget = new BigDecimal(tf.getText().trim()); }
                        catch (NumberFormatException e) { d.budget = BigDecimal.ZERO; }
                    }
                }
            } else if (c instanceof JComboBox<?> cb && "status".equals(cb.getName())) {
                d.status = (String) cb.getSelectedItem();
            } else if (c instanceof JComboBox<?> cb && "visibility".equals(cb.getName())) {
                d.visibility = (String) cb.getSelectedItem();
            } else if (c instanceof JScrollPane sp && sp.getViewport().getView() instanceof JTextArea ta) {
                if ("desc".equals(ta.getName())) d.description = ta.getText().trim();
                else if ("obj".equals(ta.getName())) d.objective = ta.getText().trim();
            } else if (c instanceof JSpinner spn && "maxMembers".equals(spn.getName())) {
                d.maxMembers = (Integer) spn.getValue();
            } else if (c instanceof JScrollPane sp && sp.getViewport().getView() instanceof JList<?> list
                    && "members".equals(list.getName())) {
                @SuppressWarnings("unchecked")
                JList<MemberDTO> l = (JList<MemberDTO>) list;
                d.memberIds = l.getSelectedValuesList().stream()
                    .map(MemberDTO::getMemberId)
                    .toList();
            }
        }

        if (d.name == null || d.name.isBlank()) {
            throw new IllegalArgumentException("Tên dự án không được để trống!");
        }
        return d;
    }

    private LocalDate parseDate(String text) {
        String t = text.trim();
        if (t.isEmpty()) return null;
        try {
            return LocalDate.parse(t, java.time.format.DateTimeFormatter.ofPattern("yyyy-M-d"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày sai định dạng: '" + t + "'. Vui lòng dùng (yyyy-MM-dd)");
        }
    }

    private JTextField createField(String v, Font f) {
        JTextField tf = new JTextField(v);
        tf.setFont(f);
        return tf;
    }

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
    }

    private void handleViewDetail() {
        Integer id = view.getSelectedProjectId();
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
                    showDetailDialog(project, members);
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

    private void showDetailDialog(ProjectDTO project, List<MemberDTO> members) {
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

        content.add(makeInfoLabel("Trạng thái: " + project.getStatus()));
        content.add(makeInfoLabel("Hiển thị: " + project.getVisibility()));
        content.add(makeInfoLabel("Số thành viên tối đa: " + maxText));
        content.add(makeInfoLabel("Thành viên: " + memberCount));
        content.add(makeInfoLabel("Ngân sách: " + (project.getBudget() != null ? project.getBudget().toPlainString() : "0") + " VND"));
        content.add(makeInfoLabel("Bắt đầu: " + (project.getStartDate() != null ? project.getStartDate() : "")));
        content.add(makeInfoLabel("Kết thúc: " + (project.getEndDate() != null ? project.getEndDate() : "")));
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

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(new JScrollPane(content), BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JLabel makeInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setBorder(new EmptyBorder(2, 0, 2, 0));
        return label;
    }

    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }
}
