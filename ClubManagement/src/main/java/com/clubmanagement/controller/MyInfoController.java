package com.clubmanagement.controller;

import java.awt.Frame;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.clubmanagement.dto.EventDTO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.dto.ProjectDTO;
import com.clubmanagement.dto.TaskDTO;
import com.clubmanagement.service.EventService;
import com.clubmanagement.service.MemberService;
import com.clubmanagement.service.ProjectService;
import com.clubmanagement.service.TaskService;
import com.clubmanagement.view.DashboardView;
import com.clubmanagement.view.MemberFormDialog;
import com.clubmanagement.view.MyInfoView;

public class MyInfoController {

    private static final String FILTER_REGISTERED = "Đã đăng ký";
    private static final String FILTER_ACTIVE = "Đang tham gia";
    private static final String FILTER_DONE = "Đã tham gia";

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MyInfoView view;
    private final DashboardView dashboardView;
    private final MemberDTO currentUser;

    private final MemberService memberService = new MemberService();
    private final TaskService taskService = new TaskService();
    private final EventService eventService = new EventService();
    private final ProjectService projectService = new ProjectService();

    private final TaskController taskController;
    private final EventController eventController;
    private final ProjectController projectController;

    public MyInfoController(MyInfoView view, DashboardView dashboardView, MemberDTO currentUser,
                            TaskController taskController, EventController eventController,
                            ProjectController projectController) {
        this.view = view;
        this.dashboardView = dashboardView;
        this.currentUser = currentUser;
        this.taskController = taskController;
        this.eventController = eventController;
        this.projectController = projectController;

        attachListeners();
        reloadAll();
    }

    private void attachListeners() {
        view.getBtnRefresh().addActionListener(e -> reloadAll());
        view.getTaskFilter().addActionListener(e -> loadTasks());
        view.getEventFilter().addActionListener(e -> loadEvents());
        view.getProjectFilter().addActionListener(e -> loadProjects());

        view.getTaskTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) openTaskDetail();
            }
        });

        view.getEventTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) openEventDetail();
            }
        });

        view.getProjectTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) openProjectDetail();
            }
        });

        view.getBtnEditProfile().addActionListener(e -> openEditProfile());
    }

    public void reloadAll() {
        refreshProfile();
        loadTasks();
        loadEvents();
        loadProjects();
    }

    private void refreshProfile() {
        Optional<MemberDTO> memberOpt = memberService.getMemberById(currentUser.getMemberId());
        if (memberOpt.isPresent()) {
            MemberDTO updated = memberOpt.get();
            applyUpdatedUser(updated);
            view.setProfileInfo(updated);
            dashboardView.updateCurrentUserInfo(updated);
        }
    }

    private void loadTasks() {
        SwingWorker<List<TaskDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TaskDTO> doInBackground() {
                return taskService.getTasksForUser(currentUser.getMemberId());
            }

            @Override
            protected void done() {
                try {
                    List<TaskDTO> tasks = get();
                    String filter = (String) view.getTaskFilter().getSelectedItem();
                    List<Object[]> rows = tasks.stream()
                        .filter(t -> matchesTaskFilter(t, filter))
                        .map(t -> new Object[]{
                            t.getTaskId(),
                            t.getTitle(),
                            t.getDeadline() != null ? t.getDeadline().format(DATE_TIME_FMT) : "Không xác định"
                        })
                        .toList();
                    view.setTaskRows(rows);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Lỗi tải nhiệm vụ: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private boolean matchesTaskFilter(TaskDTO task, String filter) {
        if (filter == null) return true;
        String bucket = classifyTask(task);
        return filter.equals(bucket);
    }

    private String classifyTask(TaskDTO task) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = task.getCreatedDate() != null ? task.getCreatedDate() : now;
        LocalDateTime end = task.getDeadline();

        if (end != null && now.isAfter(end)) return FILTER_DONE;
        if (now.isBefore(start)) return FILTER_REGISTERED;
        if (end == null) return FILTER_ACTIVE;
        return (now.isBefore(end) || now.isEqual(end)) ? FILTER_ACTIVE : FILTER_DONE;
    }

    private void loadEvents() {
        SwingWorker<List<EventDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<EventDTO> doInBackground() {
                return eventService.getEventsForMember(currentUser.getMemberId());
            }

            @Override
            protected void done() {
                try {
                    List<EventDTO> events = get();
                    String filter = (String) view.getEventFilter().getSelectedItem();
                    List<Object[]> rows = events.stream()
                        .filter(e -> matchesEventFilter(e, filter))
                        .map(e -> new Object[]{
                            e.getEventId(),
                            e.getEventName(),
                            e.getStartDate() != null ? e.getStartDate().format(DATE_TIME_FMT) : "",
                            e.getEndDate() != null ? e.getEndDate().format(DATE_TIME_FMT) : ""
                        })
                        .toList();
                    view.setEventRows(rows);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Lỗi tải sự kiện: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private boolean matchesEventFilter(EventDTO event, String filter) {
        if (filter == null) return true;
        String bucket = classifyEvent(event);
        return filter.equals(bucket);
    }

    private String classifyEvent(EventDTO event) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = event.getStartDate();
        LocalDateTime end = event.getEndDate();

        if (end != null && now.isAfter(end)) return FILTER_DONE;
        if (start != null && now.isBefore(start)) return FILTER_REGISTERED;
        if (start == null || end == null) return FILTER_ACTIVE;
        return (now.isBefore(end) || now.isEqual(end)) ? FILTER_ACTIVE : FILTER_DONE;
    }

    private void loadProjects() {
        SwingWorker<List<ProjectDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ProjectDTO> doInBackground() {
                return projectService.getProjectsForUser(currentUser.getMemberId());
            }

            @Override
            protected void done() {
                try {
                    List<ProjectDTO> projects = get();
                    String filter = (String) view.getProjectFilter().getSelectedItem();
                    List<Object[]> rows = projects.stream()
                        .filter(p -> matchesProjectFilter(p, filter))
                        .map(p -> new Object[]{
                            p.getProjectId(),
                            p.getProjectName(),
                            p.getStartDate() != null ? p.getStartDate().format(DATE_FMT) : "",
                            p.getEndDate() != null ? p.getEndDate().format(DATE_FMT) : ""
                        })
                        .toList();
                    view.setProjectRows(rows);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Lỗi tải dự án: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private boolean matchesProjectFilter(ProjectDTO project, String filter) {
        if (filter == null) return true;
        String bucket = classifyProject(project);
        return filter.equals(bucket);
    }

    private String classifyProject(ProjectDTO project) {
        LocalDate now = LocalDate.now();
        LocalDate start = project.getStartDate();
        LocalDate end = project.getEndDate();

        if (end != null && now.isAfter(end)) return FILTER_DONE;
        if (start != null && now.isBefore(start)) return FILTER_REGISTERED;
        if (start == null || end == null) return FILTER_ACTIVE;
        return (now.isBefore(end) || now.isEqual(end)) ? FILTER_ACTIVE : FILTER_DONE;
    }

    private void openTaskDetail() {
        Integer id = view.getSelectedTaskId();
        if (id == null) return;
        taskController.openDetailById(id, this::reloadAll);
    }

    private void openEventDetail() {
        Integer id = view.getSelectedEventId();
        if (id == null) return;
        eventController.openDetailById(id, this::reloadAll);
    }

    private void openProjectDetail() {
        Integer id = view.getSelectedProjectId();
        if (id == null) return;
        projectController.openDetailById(id, this::reloadAll);
    }

    private void openEditProfile() {
        Optional<MemberDTO> memberOpt = memberService.getMemberById(currentUser.getMemberId());
        if (memberOpt.isEmpty()) return;

        Frame parent = JOptionPane.getFrameForComponent(view.getPanel());
        MemberFormDialog dialog = new MemberFormDialog(parent, memberOpt.get(), true);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                MemberDTO updated = memberService.updateSelfProfile(
                    currentUser.getMemberId(),
                    dialog.getFullName(),
                    dialog.getPhone(),
                    dialog.getGender(),
                    dialog.getBirthDate(),
                    dialog.getAvatarUrl(),
                    dialog.getOldPassword(),
                    dialog.getNewPassword()
                );
                applyUpdatedUser(updated);
                view.setProfileInfo(updated);
                dashboardView.updateCurrentUserInfo(updated);
                JOptionPane.showMessageDialog(null, "Đã cập nhật thông tin cá nhân!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                reloadAll();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyUpdatedUser(MemberDTO updated) {
        currentUser.setFullName(updated.getFullName());
        currentUser.setEmail(updated.getEmail());
        currentUser.setPhone(updated.getPhone());
        currentUser.setGender(updated.getGender());
        currentUser.setBirthDate(updated.getBirthDate());
        currentUser.setAvatarUrl(updated.getAvatarUrl());
        currentUser.setRoleName(updated.getRoleName());
        currentUser.setTeamNames(updated.getTeamNames());
        currentUser.setJoinDate(updated.getJoinDate());
        currentUser.setStatus(updated.getStatus());
    }
}
