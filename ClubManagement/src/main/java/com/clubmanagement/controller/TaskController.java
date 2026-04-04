package com.clubmanagement.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.clubmanagement.dto.EventDTO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.dto.TaskDTO;
import com.clubmanagement.service.EventService;
import com.clubmanagement.service.MemberService;
import com.clubmanagement.service.TaskService;
import com.clubmanagement.view.TaskView;
import com.toedter.calendar.JDateChooser;

/**
 * TaskController - Điều khiển màn hình Nhiệm vụ.
 */
public class TaskController {

    private final TaskView view;
    private final MemberDTO currentUser;
    private final TaskService taskService = new TaskService();
    private final MemberService memberService = new MemberService();
    private final EventService eventService = new EventService();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Khởi tạo controller cho màn hình Nhiệm vụ.
     * @param view View hiển thị
     * @param currentUser Người dùng hiện tại
     */
    public TaskController(TaskView view, MemberDTO currentUser) {
        this.view = view;
        this.currentUser = currentUser;
        attachListeners();
        loadTasksByFilterInternal();
    }

    /**
     * Đăng ký các sự kiện cho view.
     */
    private void attachListeners() {
        view.getBtnRefresh().addActionListener(e -> loadTasksByFilter());
        view.getFilterBox().addActionListener(e -> loadTasksByFilter());
        view.getBtnEdit().addActionListener(e -> handleEdit());

        if (currentUser.isLeader()) {
            view.getBtnAdd().addActionListener(e -> handleAdd());
            view.getBtnDelete().addActionListener(e -> handleDelete());
        }

        view.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) handleViewDetail();
            }
        });
    }

    /**
     * Tải danh sách nhiệm vụ theo bộ lọc (chạy nền).
     */
    private void loadTasksByFilterInternal() {
        view.setStatusMessage("Đang tải danh sách Nhiệm vụ...");
        SwingWorker<List<TaskDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TaskDTO> doInBackground() {
                String filter = (String) view.getFilterBox().getSelectedItem();
                if (filter == null) filter = "Tất cả";

                if ("Chưa chỉ định (Public)".equals(filter)) {
                    return taskService.getPublicUnassignedTasks();
                }

                if ("Nhiệm vụ của tôi".equals(filter)) {
                    return taskService.getAssignedTasksForUser(currentUser.getMemberId());
                }

                if ("Đã chỉ định (Public/Private)".equals(filter)) {
                    List<TaskDTO> source = currentUser.isLeader()
                        ? taskService.getAllTasks()
                        : taskService.getVisibleTasksForUser(currentUser.getMemberId());
                    return source.stream()
                        .filter(t -> t.getAssigneeIds() != null && !t.getAssigneeIds().isEmpty())
                        .toList();
                }

                return currentUser.isLeader()
                    ? taskService.getAllTasks()
                    : taskService.getVisibleTasksForUser(currentUser.getMemberId());
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
     * Refresh danh sách nhiệm vụ theo bộ lọc.
     */
    public final void loadTasksByFilter() {
        loadTasksByFilterInternal();
    }

    /**
     * Mở form thêm nhiệm vụ.
     */
    private void handleAdd() {
        showFormDialog(null);
    }

    /**
     * Mở form sửa nhiệm vụ.
     */
    private void handleEdit() {
        Integer id = view.getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn Nhiệm vụ cần sửa!");
            return;
        }

        List<TaskDTO> all = currentUser.isLeader()
            ? taskService.getAllTasks()
            : taskService.getVisibleTasksForUser(currentUser.getMemberId());
        Optional<TaskDTO> opt = all.stream().filter(t -> t.getTaskId().equals(id)).findFirst();
        if (opt.isEmpty()) return;

        TaskDTO task = opt.get();
        // If normal user, they can only edit if they are the assignee (and usually only status)
        if (!currentUser.isLeader()
            && (task.getAssigneeIds() == null || !task.getAssigneeIds().contains(currentUser.getMemberId()))) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền sửa nhiệm vụ của người khác!", "Từ chối", JOptionPane.ERROR_MESSAGE);
            return;
        }

        showFormDialog(task);
    }

    /**
     * Hiển thị form thêm/sửa nhiệm vụ.
     * @param task Dữ liệu hiện tại (nullable)
     */
    private void showFormDialog(TaskDTO task) {
        String dialogTitle = task == null ? "Tạo Nhiệm vụ" : "Sửa Nhiệm vụ";
        JDialog dialog = new JDialog((Frame) null, dialogTitle, true);
        dialog.setSize(760, 560);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(9, 2, 8, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        JTextField txtTitle = new JTextField();
        txtTitle.setFont(f);
        if (task != null) txtTitle.setText(task.getTitle());

        JTextArea txtDesc = new JTextArea(3, 20);
        txtDesc.setFont(f);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        if (task != null) txtDesc.setText(task.getDescription());

        LocalDateTime defaultDeadline = task != null ? task.getDeadline() : LocalDateTime.now().plusDays(2);
        JDateChooser dcDeadline = createDateChooser(defaultDeadline);
        JSpinner spDeadlineTime = createTimeSpinner(defaultDeadline);

        JComboBox<String> cbPriority = new JComboBox<>(new String[]{"Low", "Medium", "High", "Critical"});
        cbPriority.setFont(f);
        if (task != null) cbPriority.setSelectedItem(task.getPriority());

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Todo", "InProgress", "Done", "Overdue"});
        cbStatus.setFont(f);
        if (task != null) cbStatus.setSelectedItem(task.getStatus());

        JComboBox<String> cbVisibility = new JComboBox<>(new String[]{"Public", "Private"});
        cbVisibility.setFont(f);
        if (task != null && task.getVisibility() != null) cbVisibility.setSelectedItem(task.getVisibility());

        JSpinner spMax = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        spMax.setFont(f);
        if (task != null && task.getMaxAssignees() != null) spMax.setValue(task.getMaxAssignees());

        List<MemberDTO> allMembers = memberService.getAllMembers();
        JList<MemberDTO> listAssignees = new JList<>(allMembers.toArray(MemberDTO[]::new));
        listAssignees.setFont(f);
        listAssignees.setVisibleRowCount(4);
        listAssignees.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (task != null && task.getAssigneeIds() != null) {
            java.util.List<Integer> ids = task.getAssigneeIds();
            int[] indices = java.util.stream.IntStream.range(0, listAssignees.getModel().getSize())
                .filter(i -> ids.contains(listAssignees.getModel().getElementAt(i).getMemberId()))
                .toArray();
            listAssignees.setSelectedIndices(indices);
        }

        List<EventDTO> allEvents = eventService.getAllEvents();
        JComboBox<EventDTO> cbEvent = new JComboBox<>(allEvents.toArray(EventDTO[]::new));
        cbEvent.setFont(f);
        cbEvent.insertItemAt(null, 0); // Allow null
        cbEvent.setSelectedIndex(0);

        if (task != null && task.getEventId() != null) {
            for (int i = 1; i < cbEvent.getItemCount(); i++) {
                if (cbEvent.getItemAt(i).getEventId().equals(task.getEventId())) {
                    cbEvent.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Nếu user thường sửa (chính task của họ) -> Disable mọi thứ trừ status
        if (task != null && !currentUser.isLeader()) {
            txtTitle.setEditable(false);
            txtDesc.setEditable(false);
            dcDeadline.setEnabled(false);
            spDeadlineTime.setEnabled(false);
            cbPriority.setEnabled(false);
            cbVisibility.setEnabled(false);
            spMax.setEnabled(false);
            listAssignees.setEnabled(false);
            cbEvent.setEnabled(false);
        }

        JScrollPane assigneeScroll = new JScrollPane(listAssignees);
        assigneeScroll.setPreferredSize(new Dimension(320, 120));

        panel.add(new JLabel("Tiêu đề *:"));      panel.add(txtTitle);
        panel.add(new JLabel("Mô tả:"));          panel.add(new JScrollPane(txtDesc));
        panel.add(new JLabel("Hạn chót:"));       panel.add(buildDateTimePanel(dcDeadline, spDeadlineTime));
        panel.add(new JLabel("Độ ưu tiên:"));     panel.add(cbPriority);
        panel.add(new JLabel("Trạng thái:"));     panel.add(cbStatus);
        panel.add(new JLabel("Hiển thị:"));       panel.add(cbVisibility);
        panel.add(new JLabel("Số người tối đa:")); panel.add(spMax);
        panel.add(new JLabel("Người thực hiện:")); panel.add(assigneeScroll);
        
        JPanel comboPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        comboPanel.add(cbEvent);
        panel.add(new JLabel("Sự kiện:")); 
        panel.add(comboPanel);

        JButton btnSave = new JButton("Lưu lại");
        btnSave.setBackground(new Color(16, 185, 129));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            try {
                String taskTitle = txtTitle.getText();
                String desc = txtDesc.getText();
                LocalDateTime deadline = toLocalDateTime(dcDeadline, spDeadlineTime);
                String prio = (String) cbPriority.getSelectedItem();
                String status = (String) cbStatus.getSelectedItem();
                String visibility = (String) cbVisibility.getSelectedItem();
                Integer maxAssignees = (Integer) spMax.getValue();
                
                java.util.List<Integer> assigneeIds = new java.util.ArrayList<>();
                for (MemberDTO m : listAssignees.getSelectedValuesList()) {
                    assigneeIds.add(m.getMemberId());
                }
                if (maxAssignees != null && maxAssignees > 0 && assigneeIds.size() > maxAssignees) {
                    JOptionPane.showMessageDialog(dialog,
                        "Số người được chọn vượt quá giới hạn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                EventDTO evt = (EventDTO) cbEvent.getSelectedItem();
                Integer evtId = evt != null ? evt.getEventId() : null;

                if (task == null) {
                    taskService.createTask(taskTitle, desc, deadline, prio, visibility, maxAssignees,
                        currentUser.getMemberId(), evtId, assigneeIds);
                    JOptionPane.showMessageDialog(dialog, "Đã giao việc thành công!");
                } else {
                    taskService.updateTask(task.getTaskId(), taskTitle, desc, deadline, prio, status,
                        visibility, maxAssignees, evtId,
                        currentUser.isLeader() ? assigneeIds : null);
                    JOptionPane.showMessageDialog(dialog, "Đã cập nhật Nhiệm vụ!");
                }
                dialog.dispose();
                loadTasksByFilter();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnCancel);
        bottom.add(btnSave);

        dialog.setLayout(new BorderLayout());
        dialog.add(buildDialogHeader(dialogTitle), BorderLayout.NORTH);
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Tạo header cho dialog.
     * @param title Tiêu đề
     * @return JPanel header
     */
    private JPanel buildDialogHeader(String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(248, 250, 252));
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(30, 41, 59));
        header.add(label, BorderLayout.WEST);
        return header;
    }

    /**
     * Tạo bộ chọn ngày.
     * @param dateTime Ngày giờ mặc định
     * @return JDateChooser
     */
    private JDateChooser createDateChooser(LocalDateTime dateTime) {
        JDateChooser chooser = new JDateChooser();
        chooser.setDateFormatString("yyyy-MM-dd");
        chooser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (dateTime != null) {
            chooser.setDate(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
        }
        return chooser;
    }

    /**
     * Tạo spinner chọn giờ.
     * @param dateTime Ngày giờ mặc định
     * @return JSpinner
     */
    private JSpinner createTimeSpinner(LocalDateTime dateTime) {
        LocalTime time = dateTime != null ? dateTime.toLocalTime() : LocalTime.now().withSecond(0).withNano(0);
        Date timeValue = Date.from(time.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
        SpinnerDateModel model = new SpinnerDateModel(timeValue, null, null, Calendar.MINUTE);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "HH:mm");
        spinner.setEditor(editor);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return spinner;
    }

    /**
     * Gộp input ngày + giờ thành một panel.
     * @param dateChooser Bộ chọn ngày
     * @param timeSpinner Bộ chọn giờ
     * @return JPanel chứa cả hai input
     */
    private JPanel buildDateTimePanel(JDateChooser dateChooser, JSpinner timeSpinner) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        timeSpinner.setPreferredSize(new java.awt.Dimension(90, 28));
        panel.add(dateChooser, BorderLayout.CENTER);
        panel.add(timeSpinner, BorderLayout.EAST);
        return panel;
    }

    /**
     * Chuyển giá trị từ input ngày + giờ sang LocalDateTime.
     * @param dateChooser Bộ chọn ngày
     * @param timeSpinner Bộ chọn giờ
     * @return LocalDateTime hoặc null
     */
    private LocalDateTime toLocalDateTime(JDateChooser dateChooser, JSpinner timeSpinner) {
        Date date = dateChooser.getDate();
        if (date == null) return null;
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Date time = (Date) timeSpinner.getValue();
        LocalTime localTime = time.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
        return LocalDateTime.of(localDate, localTime);
    }

    /**
     * Xóa nhiệm vụ đang chọn.
     */
    private void handleDelete() {
        Integer id = view.getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn Nhiệm vụ cần xóa!");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(null,
            "Bạn có chắc muốn xóa Nhiệm vụ này?", "Xác nhận xóa", 
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                taskService.deleteTask(id);
                JOptionPane.showMessageDialog(null, "Đã xóa!");
                loadTasksByFilter();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Mở dialog chi tiết nhiệm vụ từ dòng đang chọn.
     */
    private void handleViewDetail() {
        openDetailById(view.getSelectedId(), null);
    }

    /**
     * Mở dialog chi tiết nhiệm vụ theo ID.
     * @param id ID nhiệm vụ
     * @param afterClose Callback sau khi đóng (nullable)
     */
    public void openDetailById(Integer id, Runnable afterClose) {
        if (id == null) return;
        List<TaskDTO> source = currentUser.isLeader()
            ? taskService.getAllTasks()
            : taskService.getVisibleTasksForUser(currentUser.getMemberId());
        Optional<TaskDTO> opt = source.stream().filter(t -> t.getTaskId().equals(id)).findFirst();
        if (opt.isEmpty()) return;
        showDetailDialog(opt.get(), afterClose);
    }

    /**
     * Hiển thị dialog chi tiết nhiệm vụ.
     * @param task Dữ liệu nhiệm vụ
     * @param afterClose Callback sau khi đóng (nullable)
     */
    private void showDetailDialog(TaskDTO task, Runnable afterClose) {
        JDialog dialog = new JDialog((Frame) null, "Chi tiết nhiệm vụ", true);
        dialog.setSize(720, 520);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel(task.getTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        String metaText = "Người giao: " + task.getAssignerName();
        if (task.getEventName() != null) metaText += " | Sự kiện: " + task.getEventName();
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

        JTextArea desc = new JTextArea(task.getDescription() != null ? task.getDescription() : "");
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        desc.setBorder(new EmptyBorder(8, 8, 8, 8));

        String assigneesText = (task.getAssigneeNames() == null || task.getAssigneeNames().isEmpty())
            ? "Chưa có" : String.join(", ", task.getAssigneeNames());
        String deadlineText = task.getDeadline() != null ? task.getDeadline().format(DATE_FMT) : "Không xác định";

        content.add(makeInfoLabel("Trạng thái: " + task.getStatus()));
        content.add(makeInfoLabel("Độ ưu tiên: " + task.getPriority()));
        content.add(makeInfoLabel("Hạn chót: " + deadlineText));
        content.add(makeInfoLabel("Hiển thị: " + task.getVisibility()));
        content.add(makeInfoLabel("Số người tối đa: " + task.getMaxAssignees()));
        content.add(makeInfoLabel("Người thực hiện: " + assigneesText));
        content.add(Box.createVerticalStrut(8));
        content.add(new JScrollPane(desc));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());
        footer.add(btnClose);

        boolean alreadyAssigned = task.getAssigneeIds() != null
            && task.getAssigneeIds().contains(currentUser.getMemberId());
        Integer maxValue = task.getMaxAssignees();
        int max = maxValue != null ? maxValue : 1;
        int current = task.getAssigneeIds() != null ? task.getAssigneeIds().size() : 0;
        boolean canRegister = "Public".equalsIgnoreCase(task.getVisibility())
            && !alreadyAssigned
            && (max <= 0 || current < max);

        if (alreadyAssigned) {
            JButton btnCancel = new JButton("Hủy đăng ký");
            btnCancel.setBackground(new Color(239, 68, 68));
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setBorderPainted(false);
            btnCancel.setFocusPainted(false);
            btnCancel.addActionListener(e -> handleCancelTask(task, dialog));
            footer.add(btnCancel, 0);
        }

        if (canRegister) {
            JButton btnRegister = new JButton("Đăng ký nhận nhiệm vụ");
            btnRegister.setBackground(new Color(16, 185, 129));
            btnRegister.setForeground(Color.WHITE);
            btnRegister.setBorderPainted(false);
            btnRegister.setFocusPainted(false);
            btnRegister.addActionListener(e -> {
                try {
                    taskService.registerForTask(task.getTaskId(), currentUser.getMemberId());
                    JOptionPane.showMessageDialog(dialog, "Đăng ký thành công!");
                    dialog.dispose();
                    loadTasksByFilter();
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
        dialog.add(content, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Hủy đăng ký nhiệm vụ.
     * @param task Dữ liệu nhiệm vụ
     * @param dialog Dialog đang hiển thị
     */
    private void handleCancelTask(TaskDTO task, JDialog dialog) {
        LocalDateTime deadline = task.getDeadline();
        if (deadline != null && !LocalDateTime.now().isBefore(deadline.minusDays(3))) {
            JOptionPane.showMessageDialog(dialog,
                "Không thể hủy khi còn 3 ngày hoặc ít hơn đến hạn chót.",
                "Từ chối", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(dialog,
            "Bạn có chắc muốn hủy đăng ký nhiệm vụ này?",
            "Xác nhận hủy", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                taskService.unregisterFromTask(task.getTaskId(), currentUser.getMemberId());
                JOptionPane.showMessageDialog(dialog, "Đã hủy đăng ký nhiệm vụ!");
                dialog.dispose();
                loadTasksByFilter();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Tạo label hiển thị thông tin.
     * @param text Nội dung
     * @return JLabel
     */
    private JLabel makeInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setBorder(new EmptyBorder(2, 0, 2, 0));
        return label;
    }
}
