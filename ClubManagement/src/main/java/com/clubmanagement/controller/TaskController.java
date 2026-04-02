package com.clubmanagement.controller;

import com.clubmanagement.dto.EventDTO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.dto.TaskDTO;
import com.clubmanagement.service.EventService;
import com.clubmanagement.service.MemberService;
import com.clubmanagement.service.TaskService;
import com.clubmanagement.view.TaskView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TaskController {

    private final TaskView view;
    private final MemberDTO currentUser;
    private final TaskService taskService = new TaskService();
    private final MemberService memberService = new MemberService();
    private final EventService eventService = new EventService();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TaskController(TaskView view, MemberDTO currentUser) {
        this.view = view;
        this.currentUser = currentUser;
        attachListeners();
        loadAllTasks();
    }

    private void attachListeners() {
        view.getBtnRefresh().addActionListener(e -> loadAllTasks());
        view.getBtnEdit().addActionListener(e -> handleEdit());

        if (currentUser.isLeader()) {
            view.getBtnAdd().addActionListener(e -> handleAdd());
            view.getBtnDelete().addActionListener(e -> handleDelete());
        }

        view.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) handleEdit();
            }
        });
    }

    public void loadAllTasks() {
        view.setStatusMessage("Đang tải danh sách Nhiệm vụ...");
        SwingWorker<List<TaskDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TaskDTO> doInBackground() {
                return taskService.getAllTasks();
            }
            @Override
            protected void done() {
                try {
                    view.loadData(get());
                } catch (Exception e) {
                    view.setStatusMessage("Lỗi: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleAdd() {
        showFormDialog(null);
    }

    private void handleEdit() {
        Integer id = view.getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn Nhiệm vụ cần sửa!");
            return;
        }

        List<TaskDTO> all = taskService.getAllTasks();
        Optional<TaskDTO> opt = all.stream().filter(t -> t.getTaskId().equals(id)).findFirst();
        if (opt.isEmpty()) return;

        TaskDTO task = opt.get();
        // If normal user, they can only edit if they are the assignee (and usually only status)
        if (!currentUser.isLeader() && !currentUser.getMemberId().equals(task.getAssigneeId())) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền sửa nhiệm vụ của người khác!", "Từ chối", JOptionPane.ERROR_MESSAGE);
            return;
        }

        showFormDialog(task);
    }

    private void showFormDialog(TaskDTO task) {
        JDialog dialog = new JDialog((Frame) null, task == null ? "🎯 Tạo Nhiệm vụ" : "✏ Sửa Nhiệm vụ", true);
        dialog.setSize(550, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 12));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        JTextField txtTitle = new JTextField();
        txtTitle.setFont(f);
        if (task != null) txtTitle.setText(task.getTitle());

        JTextField txtDesc = new JTextField();
        txtDesc.setFont(f);
        if (task != null) txtDesc.setText(task.getDescription());

        JTextField txtDeadline = new JTextField(LocalDateTime.now().plusDays(2).format(DATE_FMT));
        txtDeadline.setFont(f);
        if (task != null && task.getDeadline() != null) txtDeadline.setText(task.getDeadline().format(DATE_FMT));

        JComboBox<String> cbPriority = new JComboBox<>(new String[]{"Low", "Medium", "High", "Critical"});
        cbPriority.setFont(f);
        if (task != null) cbPriority.setSelectedItem(task.getPriority());

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Todo", "InProgress", "Done", "Overdue"});
        cbStatus.setFont(f);
        if (task != null) cbStatus.setSelectedItem(task.getStatus());

        List<MemberDTO> allMembers = memberService.getAllMembers();
        JComboBox<MemberDTO> cbAssignee = new JComboBox<>(allMembers.toArray(new MemberDTO[0]));
        cbAssignee.setFont(f);

        List<EventDTO> allEvents = eventService.getAllEvents();
        JComboBox<EventDTO> cbEvent = new JComboBox<>(allEvents.toArray(new EventDTO[0]));
        cbEvent.setFont(f);
        cbEvent.insertItemAt(null, 0); // Allow null
        cbEvent.setSelectedIndex(0);

        if (task != null && task.getAssigneeId() != null) {
            for (int i = 0; i < cbAssignee.getItemCount(); i++) {
                if (cbAssignee.getItemAt(i).getMemberId().equals(task.getAssigneeId())) {
                    cbAssignee.setSelectedIndex(i);
                    break;
                }
            }
        }

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
            txtDeadline.setEditable(false);
            cbPriority.setEnabled(false);
            cbAssignee.setEnabled(false);
            cbEvent.setEnabled(false);
        }

        panel.add(new JLabel("Tiêu đề *:"));      panel.add(txtTitle);
        panel.add(new JLabel("Mô tả:"));          panel.add(txtDesc);
        panel.add(new JLabel("Hạn (yyyy-MM-dd HH:mm):")); panel.add(txtDeadline);
        panel.add(new JLabel("Độ ưu tiên:"));     panel.add(cbPriority);
        panel.add(new JLabel("Trạng thái:"));     panel.add(cbStatus);
        
        JPanel comboPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        comboPanel.add(cbAssignee);
        comboPanel.add(cbEvent);
        panel.add(new JLabel("Giao cho / Sự kiện:")); 
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
                String title = txtTitle.getText();
                String desc = txtDesc.getText();
                LocalDateTime deadline = null;
                try {
                    deadline = LocalDateTime.parse(txtDeadline.getText().trim(), DATE_FMT);
                } catch (Exception ex) {
                    // Ignore or show error
                }
                String prio = (String) cbPriority.getSelectedItem();
                String status = (String) cbStatus.getSelectedItem();
                
                MemberDTO assignee = (MemberDTO) cbAssignee.getSelectedItem();
                Integer assigneeId = assignee != null ? assignee.getMemberId() : null;
                
                EventDTO evt = (EventDTO) cbEvent.getSelectedItem();
                Integer evtId = evt != null ? evt.getEventId() : null;

                if (task == null) {
                    taskService.createTask(title, desc, deadline, prio, assigneeId, currentUser.getMemberId(), evtId);
                    JOptionPane.showMessageDialog(dialog, "Đã giao việc thành công!");
                } else {
                    taskService.updateTask(task.getTaskId(), title, desc, deadline, prio, status, assigneeId, evtId);
                    JOptionPane.showMessageDialog(dialog, "Đã cập nhật Nhiệm vụ!");
                }
                dialog.dispose();
                loadAllTasks();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnCancel);
        bottom.add(btnSave);

        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

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
                loadAllTasks();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
