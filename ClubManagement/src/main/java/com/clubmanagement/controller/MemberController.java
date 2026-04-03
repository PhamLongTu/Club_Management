package com.clubmanagement.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.entity.Role;
import com.clubmanagement.service.EventService;
import com.clubmanagement.service.MemberService;
import com.clubmanagement.service.ProjectService;
import com.clubmanagement.service.TaskService;
import com.clubmanagement.service.TeamService;
import com.clubmanagement.util.ImageUtil;
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
            MemberFormDialog dialog = new MemberFormDialog(parent, roles, teams, memberOpt.get(), teamIds);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                memberService.updateMember(
                    selectedId,
                    dialog.getFullName(),
                    dialog.getPhone(),
                    dialog.getGender(),
                    dialog.getBirthDate(),
                    dialog.getStatus(),
                    dialog.getSelectedRole().getRoleId(),
                    dialog.getSelectedTeamIds(),
                    dialog.getAvatarUrl()
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

    private void handleViewDetail() {
        Integer selectedId = view.getSelectedMemberId();
        if (selectedId == null) return;

        Optional<MemberDTO> memberOpt = memberService.getMemberById(selectedId);
        if (memberOpt.isEmpty()) return;
        MemberDTO member = memberOpt.get();

        List<com.clubmanagement.dto.TaskDTO> tasks = taskService.getTasksForUser(selectedId);
        List<com.clubmanagement.dto.ProjectDTO> projects = projectService.getProjectsForUser(selectedId);
        List<com.clubmanagement.dto.EventDTO> events = eventService.getEventsForMember(selectedId);

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

        content.add(makeInfoLabel("Email: " + member.getEmail()));
        content.add(makeInfoLabel("SĐT: " + (member.getPhone() != null ? member.getPhone() : "")));
        content.add(makeInfoLabel("Ban/Nhóm: " + (member.getTeamNames() != null ? member.getTeamNames() : "")));
        content.add(makeInfoLabel("Ngày vào: " + (member.getJoinDate() != null ? member.getJoinDate() : "")));
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
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());
        footer.add(btnClose);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(new JScrollPane(content), BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JLabel makeInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setBorder(new javax.swing.border.EmptyBorder(2, 0, 2, 0));
        return label;
    }
}
