package com.clubmanagement.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.entity.Role;
import com.clubmanagement.service.EventService;
import com.clubmanagement.service.MemberService;
import com.clubmanagement.service.ProjectService;
import com.clubmanagement.service.TaskService;
import com.clubmanagement.service.TeamService;
import com.clubmanagement.util.ImageUtil;
import com.clubmanagement.util.UiFormUtil;
import com.clubmanagement.view.MemberFormDialog;
import com.clubmanagement.view.MemberView;

/**
 * MemberController - Bộ điều khiển màn hình Thành viên.
 *
 * Trách nhiệm:
 * - Kết nối MemberView với MemberService
 * - Xử lý các hành động: Tìm kiếm, Thêm, Sửa, Xóa, Làm mới
 * - Mở form dialog nhập liệu
 * - Hiển thị kết quả lên bảng
 */
public class MemberController {

    private final MemberView    view;
    private final MemberDTO     currentUser;
    private final MemberService memberService = new MemberService();
    private final TeamService teamService = new TeamService();
    private final TaskService taskService = new TaskService();
    private final ProjectService projectService = new ProjectService();
    private final EventService eventService = new EventService();

    /**
     * @param view        MemberView
     * @param currentUser Người dùng đang đăng nhập (dùng để kiểm tra quyền)
     */
    public MemberController(MemberView view, MemberDTO currentUser) {
        this.view        = view;
        this.currentUser = currentUser;
        attachListeners();
    }

    /**
     * Đăng ký tất cả event listeners.
     */
    private void attachListeners() {
        // Nút Làm mới
        view.getBtnRefresh().addActionListener(e -> loadAllMembers());

        // Nút Tìm kiếm
        view.getBtnSearch().addActionListener(e -> handleSearch());

        // Tìm kiếm khi nhấn Enter ngay trong ô search
        view.getSearchField().addActionListener(e -> handleSearch());

        // Bộ lọc trạng thái thay đổi → tự động tìm lại
        view.getStatusFilterComboBox().addActionListener(e -> handleSearch());

        // Nút Thêm mới
        view.getBtnAdd().addActionListener(e -> handleAdd());

        // Nút Sửa
        view.getBtnEdit().addActionListener(e -> handleEdit());

        // Nút Xóa
        view.getBtnDelete().addActionListener(e -> handleDelete());

        // Click vào dòng → mở dialog chi tiết (chỉ Leader/Admin)
        view.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1 && currentUser.isLeader()) {
                    handleViewDetail();
                }
            }
        });
    }

    /**
     * Mở form Thêm thành viên mới (gọi từ DashboardController quick action).
     */
    public void openAddDialog() {
        handleAdd();
    }

    /**
     * Tải tất cả thành viên từ database vào bảng.
     * Sử dụng SwingWorker để database query chạy nền, không đóng băng UI.
     */
    public void loadAllMembers() {
        view.setStatusMessage("Đang tải dữ liệu...");
        SwingWorker<List<MemberDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<MemberDTO> doInBackground() {
                return memberService.getAllMembers();
            }
            @Override
            protected void done() {
                try {
                    view.loadData(get());
                } catch (Exception e) {
                    view.setStatusMessage("Lỗi: " + e.getMessage());
                    JOptionPane.showMessageDialog(null, "Lỗi tải dữ liệu: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /**
     * Tìm kiếm thành viên theo từ khóa và bộ lọc trạng thái.
     * Nếu ô tìm kiếm rỗng → tải tất cả.
     */
    private void handleSearch() {
        String keyword = view.getSearchKeyword();
        String status  = view.getStatusFilter();

        SwingWorker<List<MemberDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<MemberDTO> doInBackground() {
                List<MemberDTO> results = memberService.searchMembers(keyword);
                // Lọc thêm theo trạng thái ở tầng controller (nếu không chọn "Tất cả")
                if (!"Tất cả".equals(status)) {
                    results = results.stream()
                        .filter(m -> status.equals(m.getStatus()))
                        .toList();
                }
                return results;
            }
            @Override
            protected void done() {
                try {
                    List<MemberDTO> results = get();
                    view.loadData(results);
                    view.setStatusMessage("Tìm thấy " + results.size() + " thành viên.");
                } catch (Exception e) {
                    view.setStatusMessage("Lỗi tìm kiếm: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Xử lý thêm thành viên mới:
     * 1. Lấy danh sách roles để populate ComboBox
     * 2. Mở MemberFormDialog (ADD mode)
     * 3. Nếu người dùng nhấn Lưu → gọi Service tạo mới
     * 4. Reload bảng
     */
    private void handleAdd() {
        // Kiểm tra quyền (chỉ Leader/Admin mới thêm được)
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền thêm thành viên!",
                "Không đủ quyền", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<Role> roles = memberService.getAllRoles();
            List<com.clubmanagement.dto.TeamDTO> teams = teamService.getAllTeams();

            // Tìm frame cha (cần để dialog hiển thị đúng vị trí)
            Frame parent = JOptionPane.getFrameForComponent(view.getPanel());
            MemberFormDialog dialog = new MemberFormDialog(parent, roles, teams);
            dialog.setVisible(true); // Chặn lại đây đến khi đóng dialog (modal)

            if (dialog.isConfirmed()) {
                // Người dùng đã nhấn Lưu → gọi Service
                MemberDTO created = memberService.createMember(
                    dialog.getFullName(),
                    dialog.getStudentId(),
                    dialog.getEmail(),
                    dialog.getPhone(),
                    dialog.getGender(),
                    dialog.getBirthDate(),
                    dialog.getPassword(),
                    dialog.getSelectedRole().getRoleId(),
                    dialog.getSelectedTeamIds(),
                    dialog.getAvatarUrl()
                );
                JOptionPane.showMessageDialog(null,
                    "Đã thêm thành viên: " + created.getFullName(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAllMembers(); // Reload để hiển thị thành viên mới
            }
        } catch (IllegalArgumentException ex) {
            // Lỗi validate từ Service (ví dụ: email đã tồn tại)
            JOptionPane.showMessageDialog(null, ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Lỗi hệ thống: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xử lý sửa thành viên được chọn trong bảng.
     */
    private void handleEdit() {
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền chỉnh sửa thành viên!",
                "Không đủ quyền", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Lấy ID của dòng đang chọn
        Integer selectedId = view.getSelectedMemberId();
        if (selectedId == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn một thành viên cần sửa!",
                "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Lấy dữ liệu hiện tại của thành viên
            Optional<MemberDTO> memberOpt = memberService.getMemberById(selectedId);
            if (memberOpt.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Không tìm thấy thành viên!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Role> roles  = memberService.getAllRoles();
            List<com.clubmanagement.dto.TeamDTO> teams = teamService.getAllTeams();
            List<Integer> teamIds = memberService.getTeamIdsForMember(selectedId);
            Frame parent      = JOptionPane.getFrameForComponent(view.getPanel());
            boolean allowPasswordReset = currentUser.isAdmin();
            MemberFormDialog dialog = new MemberFormDialog(parent, roles, teams, memberOpt.get(), teamIds, allowPasswordReset);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                memberService.updateMemberWithPassword(
                    selectedId,
                    dialog.getFullName(),
                    dialog.getPhone(),
                    dialog.getGender(),
                    dialog.getBirthDate(),
                    dialog.getStatus(),
                    dialog.getSelectedRole().getRoleId(),
                    dialog.getSelectedTeamIds(),
                    dialog.getAvatarUrl(),
                    dialog.getNewPassword()
                );
                JOptionPane.showMessageDialog(null, "Đã cập nhật thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAllMembers();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Lỗi hệ thống: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Xử lý xóa (vô hiệu hóa) thành viên được chọn.
     * Dùng soft delete: chuyển status = Inactive, không xóa DB.
     */
    private void handleDelete() {
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền xóa thành viên!",
                "Không đủ quyền", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer selectedId = view.getSelectedMemberId();
        if (selectedId == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn một thành viên cần xóa!",
                "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Không cho xóa chính mình
        if (selectedId.equals(currentUser.getMemberId())) {
            JOptionPane.showMessageDialog(null, "Bạn không thể tự xóa tài khoản của mình!",
                "Không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(null,
            "Bạn có chắc muốn vô hiệu hóa thành viên này?\n" +
            "(Thành viên sẽ bị đổi trạng thái thành Inactive)",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                memberService.deactivateMember(selectedId);
                JOptionPane.showMessageDialog(null, "Đã vô hiệu hóa thành viên!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAllMembers();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Mở dialog chi tiết thành viên khi người dùng chọn một dòng.
     */
    private void handleViewDetail() {
        Integer selectedId = view.getSelectedMemberId();
        if (selectedId == null) return;

        Optional<MemberDTO> memberOpt = memberService.getMemberById(selectedId);
        if (memberOpt.isEmpty()) return;
        MemberDTO member = memberOpt.get();

        List<com.clubmanagement.dto.TaskDTO> tasks = taskService.getTasksForUser(selectedId);
        List<com.clubmanagement.dto.ProjectDTO> projects = projectService.getProjectsForUser(selectedId);
        List<com.clubmanagement.dto.EventDTO> events = eventService.getAttendedEventsForMember(selectedId);

        JDialog dialog = new JDialog((Frame) null, "Chi tiết thành viên", true);
        dialog.setSize(760, 560);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new javax.swing.border.EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel(member.getFullName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        String metaText = member.getRoleName() + " | " + member.getStatus();
        JLabel meta = new JLabel(metaText);
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        meta.setForeground(new Color(100, 116, 139));

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);
        headerText.add(title);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(meta);

        JLabel avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(96, 96));
        avatarLabel.setMinimumSize(new Dimension(96, 96));
        avatarLabel.setMaximumSize(new Dimension(96, 96));
        avatarLabel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        String initials = ImageUtil.buildInitials(member.getFullName());
        avatarLabel.setIcon(ImageUtil.loadSquareAvatar(
            member.getAvatarUrl(), 96, initials, new Color(226, 232, 240), new Color(30, 41, 59)
        ));

        JPanel headerContent = new JPanel(new BorderLayout(12, 0));
        headerContent.setOpaque(false);
        headerContent.add(avatarLabel, BorderLayout.WEST);
        headerContent.add(headerText, BorderLayout.CENTER);

        header.add(headerContent, BorderLayout.CENTER);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new javax.swing.border.EmptyBorder(8, 20, 16, 20));

        content.add(UiFormUtil.makeInfoLabel("Email: " + member.getEmail()));
        content.add(UiFormUtil.makeInfoLabel("SĐT: " + (member.getPhone() != null ? member.getPhone() : "")));
        content.add(UiFormUtil.makeInfoLabel("Ban/Nhóm: " + (member.getTeamNames() != null ? member.getTeamNames() : "")));
        content.add(UiFormUtil.makeInfoLabel("Ngày vào: " + (member.getJoinDate() != null ? member.getJoinDate() : "")));
        content.add(Box.createVerticalStrut(8));

        DefaultListModel<String> taskList = new DefaultListModel<>();
        for (var t : tasks) taskList.addElement(t.getTitle());
        JList<String> taskJList = new JList<>(taskList);
        taskJList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        taskJList.setVisibleRowCount(4);
        content.add(new JLabel("Nhiệm vụ đã tham gia:"));
        content.add(new JScrollPane(taskJList));
        content.add(Box.createVerticalStrut(8));

        DefaultListModel<String> projectList = new DefaultListModel<>();
        for (var p : projects) projectList.addElement(p.getProjectName());
        JList<String> projectJList = new JList<>(projectList);
        projectJList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        projectJList.setVisibleRowCount(4);
        content.add(new JLabel("Dự án đã tham gia:"));
        content.add(new JScrollPane(projectJList));
        content.add(Box.createVerticalStrut(8));

        DefaultListModel<String> eventList = new DefaultListModel<>();
        for (var e : events) eventList.addElement(e.getEventName());
        JList<String> eventJList = new JList<>(eventList);
        eventJList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        eventJList.setVisibleRowCount(4);
        content.add(new JLabel("Sự kiện đã tham gia:"));
        content.add(new JScrollPane(eventJList));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        if (currentUser.isLeader()) {
            JButton btnPoints = new JButton("Xem bảng điểm");
            btnPoints.addActionListener(e -> showPointsPreview(member, events, tasks, projects));
            footer.add(btnPoints);
        }
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());
        footer.add(btnClose);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(new JScrollPane(content), BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Xuất bảng điểm của thành viên ra file.
     */
    private void showPointsPreview(MemberDTO member,
                                   List<com.clubmanagement.dto.EventDTO> events,
                                   List<com.clubmanagement.dto.TaskDTO> tasks,
                                   List<com.clubmanagement.dto.ProjectDTO> projects) {
        if (member == null) return;
        JDialog dialog = new JDialog((Frame) null, "Bảng điểm - " + member.getFullName(), true);
        dialog.setSize(760, 560);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new javax.swing.border.EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("Bảng điểm hoạt động");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel meta = new JLabel(member.getFullName() + " | " + (member.getStudentId() != null ? member.getStudentId() : ""));
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        meta.setForeground(new Color(100, 116, 139));

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);
        headerText.add(title);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(meta);
        header.add(headerText, BorderLayout.CENTER);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Hoạt động", "Điểm số"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        int eventCount = events != null ? events.size() : 0;
        int taskCount = tasks != null ? tasks.size() : 0;
        int projectCount = projects != null ? projects.size() : 0;

        int drlTotal = 0;
        int ctxhTotal = 0;
        int contributionTotal = 0;

        if (events != null) {
            for (var e : events) {
                String type = e.getPointType() != null ? e.getPointType() : "None";
                Integer pointValue = e.getPointValue();
                int value = pointValue != null ? pointValue : 0;
                String pointsText = buildEventPointText(type, value);
                if ("DRL".equalsIgnoreCase(type)) drlTotal += value;
                if ("CTXH".equalsIgnoreCase(type)) ctxhTotal += value;
                model.addRow(new Object[]{"Sự kiện: " + safeText(e.getEventName()), pointsText});
            }
        }

        if (tasks != null) {
            for (var t : tasks) {
                int value = safeInt(t.getContributionPoints());
                contributionTotal += value;
                model.addRow(new Object[]{"Nhiệm vụ: " + safeText(t.getTitle()), "Đóng góp +" + value});
            }
        }

        if (projects != null) {
            for (var p : projects) {
                int value = safeInt(p.getContributionPoints());
                contributionTotal += value;
                model.addRow(new Object[]{"Dự án: " + safeText(p.getProjectName()), "Đóng góp +" + value});
            }
        }

        String summaryActivity = "Tổng hoạt động: " + (eventCount + taskCount + projectCount)
            + " (SK: " + eventCount + ", Nhiệm vụ: " + taskCount + ", Dự án: " + projectCount + ")";
        String summaryPoints = "DRL=" + drlTotal + " | CTXH=" + ctxhTotal + " | Đóng góp=" + contributionTotal;
        model.addRow(new Object[]{summaryActivity, summaryPoints});

        javax.swing.JTable table = new javax.swing.JTable(model);
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnExport = new JButton("Xuất file");
        btnExport.addActionListener(e -> exportPointsToFile(member, events, tasks, projects));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());
        footer.add(btnExport);
        footer.add(btnClose);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void exportPointsToFile(MemberDTO member,
                                    List<com.clubmanagement.dto.EventDTO> events,
                                    List<com.clubmanagement.dto.TaskDTO> tasks,
                                    List<com.clubmanagement.dto.ProjectDTO> projects) {
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền xuất điểm!",
                "Không đủ quyền", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String defaultName = buildDefaultFileName(member);
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất bảng điểm thành viên");
        chooser.setSelectedFile(new File(defaultName));
        chooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));

        int result = chooser.showSaveDialog(view.getPanel());
        if (result != JFileChooser.APPROVE_OPTION) return;

        Path filePath = chooser.getSelectedFile().toPath();
        if (!filePath.toString().toLowerCase().endsWith(".txt")) {
            filePath = Path.of(filePath.toString() + ".txt");
        }

        try {
            String content = buildPointsReport(member, events, tasks, projects);
            Files.writeString(filePath, content, StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(null, "Đã xuất điểm ra file:\n" + filePath,
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Lỗi xuất file: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String buildDefaultFileName(MemberDTO member) {
        String studentId = member.getStudentId() != null ? member.getStudentId().trim() : "member" + member.getMemberId();
        String date = java.time.LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "points_" + studentId + "_" + date + ".txt";
    }

    private String buildPointsReport(MemberDTO member,
                                     List<com.clubmanagement.dto.EventDTO> events,
                                     List<com.clubmanagement.dto.TaskDTO> tasks,
                                     List<com.clubmanagement.dto.ProjectDTO> projects) {
        String nl = System.lineSeparator();
        String line = "-".repeat(90) + nl;

        int eventCount = events != null ? events.size() : 0;
        int taskCount = tasks != null ? tasks.size() : 0;
        int projectCount = projects != null ? projects.size() : 0;

        int drlTotal = 0;
        int ctxhTotal = 0;
        int contributionTotal = 0;

        StringBuilder sb = new StringBuilder();
        sb.append("CLB Manager - Bang Tong Ket Diem").append(nl);
        sb.append("Ngay xuat: ")
            .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            .append(nl);
        sb.append(line);
        sb.append("Ho va ten: ").append(member.getFullName()).append(nl);
        sb.append("Ma SV: ").append(member.getStudentId() != null ? member.getStudentId() : "").append(nl);
        sb.append("Email: ").append(member.getEmail() != null ? member.getEmail() : "").append(nl);
        sb.append("SDT: ").append(member.getPhone() != null ? member.getPhone() : "").append(nl);
        sb.append("Vai tro: ").append(member.getRoleName() != null ? member.getRoleName() : "").append(nl);
        sb.append("Trang thai: ").append(member.getStatus() != null ? member.getStatus() : "").append(nl);
        sb.append(line);
        sb.append("Bang hoat dong:").append(nl);
        sb.append(String.format("%-60s | %-20s%s", "Hoat dong", "Diem so", nl));
        sb.append(line);

        if (events != null) {
            for (var e : events) {
                String type = e.getPointType() != null ? e.getPointType() : "None";
                Integer pointValue = e.getPointValue();
                int value = pointValue != null ? pointValue : 0;
                String pointsText = buildEventPointText(type, value);
                if ("DRL".equalsIgnoreCase(type)) drlTotal += value;
                if ("CTXH".equalsIgnoreCase(type)) ctxhTotal += value;
                String activity = "Su kien: " + safeText(e.getEventName());
                sb.append(formatRow(activity, pointsText)).append(nl);
            }
        }

        if (tasks != null) {
            for (var t : tasks) {
                int value = safeInt(t.getContributionPoints());
                contributionTotal += value;
                String activity = "Nhiem vu: " + safeText(t.getTitle());
                sb.append(formatRow(activity, "Dong gop +" + value)).append(nl);
            }
        }

        if (projects != null) {
            for (var p : projects) {
                int value = safeInt(p.getContributionPoints());
                contributionTotal += value;
                String activity = "Du an: " + safeText(p.getProjectName());
                sb.append(formatRow(activity, "Dong gop +" + value)).append(nl);
            }
        }

        sb.append(line);
        sb.append("Tong hoat dong: ")
            .append(eventCount + taskCount + projectCount)
            .append(" (Su kien: ").append(eventCount)
            .append(", Nhiem vu: ").append(taskCount)
            .append(", Du an: ").append(projectCount).append(")").append(nl);
        sb.append("Tong diem: DRL=").append(drlTotal)
            .append(" | CTXH=").append(ctxhTotal)
            .append(" | Dong gop=").append(contributionTotal)
            .append(nl);
        sb.append(line);
        return sb.toString();
    }

    private String buildEventPointText(String type, int value) {
        if ("DRL".equalsIgnoreCase(type)) return "DRL +" + value;
        if ("CTXH".equalsIgnoreCase(type)) return "CTXH +" + value;
        return "Khong ap dung (0)";
    }

    private String formatRow(String activity, String points) {
        String activityText = trimToLength(activity, 60);
        return String.format("%-60s | %-20s", activityText, points);
    }

    private String trimToLength(String value, int max) {
        if (value == null) return "";
        if (value.length() <= max) return value;
        return value.substring(0, Math.max(0, max - 3)) + "...";
    }

    private String safeText(String value) {
        return value != null ? value : "";
    }

    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }

}
