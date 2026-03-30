package com.clubmanagement.controller;

import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.dto.ProjectDTO;
import com.clubmanagement.service.ProjectService;
import com.clubmanagement.view.ProjectView;

import javax.swing.*;
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

        view.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && currentUser.isLeader()) handleEdit();
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
}
