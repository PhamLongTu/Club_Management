package com.clubmanagement.controller;

import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.service.EventService;
import com.clubmanagement.service.MemberService;
import com.clubmanagement.service.ProjectService;
import com.clubmanagement.view.DashboardView;
import com.clubmanagement.view.LoginView;

/**
 * DashboardController - Điều khiển màn hình Dashboard chính.
 *
 * Trách nhiệm:
 * - Điều hướng giữa các màn hình qua sidebar buttons
 * - Load số liệu thống kê cho Dashboard home
 * - Khởi tạo các Sub-controllers (Member/Event/Project)
 * - Xử lý đăng xuất
 */
public class DashboardController {

    private final DashboardView  view;
    private final MemberDTO      currentUser;

    private final MemberService  memberService  = new MemberService();
    private final EventService   eventService   = new EventService();
    private final ProjectService projectService = new ProjectService();

    // Sub-controllers
    private MemberController        memberController;
    private EventController         eventController;
    private ProjectController       projectController;
    private AnnouncementController  announcementController;
    private TaskController          taskController;
    private DocumentController      documentController;
    private MyInfoController        myInfoController;

    /**
     * @param view        DashboardView đã khởi tạo
     * @param currentUser Thông tin người dùng đang đăng nhập
     */
    public DashboardController(DashboardView view, MemberDTO currentUser) {
        this.view        = view;
        this.currentUser = currentUser;

        initSubControllers();
        attachListeners();
        loadDashboardStats();
    }

    /**
     * Khởi tạo các Controller con cho từng màn hình con.
     */
    private void initSubControllers() {
        announcementController = new AnnouncementController(view.getAnnouncementView(), currentUser);
        taskController         = new TaskController(view.getTaskView(), currentUser);
        documentController     = new DocumentController(view.getDocumentView(), currentUser);
        memberController       = new MemberController(view.getMemberView(),  currentUser);
        eventController        = new EventController(view.getEventView(),    currentUser);
        projectController      = new ProjectController(view.getProjectView(), currentUser);
        myInfoController       = new MyInfoController(
            view.getMyInfoView(), view, currentUser,
            taskController, eventController, projectController
        );
    }

    /**
     * Đăng ký event listener cho sidebar buttons và nút logout.
     */
    private void attachListeners() {
        // Nút Tổng quan
        view.getBtnHome().addActionListener(e -> {
            view.showHome();
            loadDashboardStats(); // Reload stats mỗi lần quay về Home
        });

        // Nút Thông báo
        view.getBtnAnnouncements().addActionListener(e -> {
            view.showAnnouncements();
            announcementController.loadAllAnnouncements();
        });

        // Nút Nhiệm vụ
        view.getBtnTasks().addActionListener(e -> {
            view.showTasks();
            taskController.loadTasksByFilter();
        });

        // Nút Thông tin của tôi
        view.getBtnMyInfo().addActionListener(e -> {
            view.showMyInfo();
            myInfoController.reloadAll();
        });

        // Nút Tài liệu
        view.getBtnDocuments().addActionListener(e -> {
            view.showDocuments();
            documentController.loadAllDocuments();
        });

        // Nút Thành viên
        view.getBtnMembers().addActionListener(e -> {
            view.showMembers();
            memberController.loadAllMembers(); // Load dữ liệu khi vào màn hình
        });

        // Nút Sự kiện
        view.getBtnEvents().addActionListener(e -> {
            view.showEvents();
            eventController.loadAllEvents();
        });

        // Nút Dự án
        view.getBtnProjects().addActionListener(e -> {
            view.showProjects();
            projectController.loadAllProjects();
        });

        // Nút Đăng xuất
        view.getBtnLogout().addActionListener(e -> handleLogout());

        // ---- Quick Actions (từ Dashboard home) ----
        // "Thêm thành viên" → chuyển sang tab Members rồi mở form thêm
        view.getBtnQuickAddMember().addActionListener(e -> {
            view.showMembers();
            memberController.loadAllMembers();
            memberController.openAddDialog();
        });

        // "Tạo sự kiện" → chuyển sang tab Events rồi mở form thêm
        view.getBtnQuickAddEvent().addActionListener(e -> {
            view.showEvents();
            eventController.loadAllEvents();
            eventController.openAddDialog();
        });

        // "Tạo dự án" → chuyển sang tab Projects rồi mở form thêm
        view.getBtnQuickAddProject().addActionListener(e -> {
            view.showProjects();
            projectController.loadAllProjects();
            projectController.openAddDialog();
        });
    }

    /**
     * Load số liệu thống kê từ database và cập nhật stat cards.
     * Chạy trong SwingWorker để không đóng băng UI.
     */
    private void loadDashboardStats() {
        SwingWorker<long[], Void> worker = new SwingWorker<>() {
            @Override
            protected long[] doInBackground() {
                // Đếm song song 3 chỉ số
                long members  = memberService.getActiveCount();
                long events   = eventService.getUpcomingCount();
                long projects = projectService.getActiveCount();
                return new long[]{members, events, projects};
            }

            @Override
            protected void done() {
                try {
                    long[] stats = get();
                    view.updateStats(stats[0], stats[1], stats[2]);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    System.err.println("Lỗi load dashboard stats: " + ex.getMessage());
                } catch (ExecutionException ex) {
                    // Không hiển thị lỗi nghiêm trọng, chỉ log
                    System.err.println("Lỗi load dashboard stats: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Xử lý đăng xuất: hỏi xác nhận → quay về LoginView.
     */
    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(
            view,
            "Bạn có chắc muốn đăng xuất?",
            "Xác nhận đăng xuất",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            view.dispose();
            // Quay về màn hình đăng nhập
            LoginView loginView = new LoginView();
            MemberService ms = new MemberService();
            LoginController loginController = new LoginController(loginView, ms);
            loginView.getRootPane().putClientProperty("controller", loginController);
            loginView.setVisible(true);
        }
    }
}
