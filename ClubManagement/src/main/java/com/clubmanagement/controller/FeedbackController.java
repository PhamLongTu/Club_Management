package com.clubmanagement.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.clubmanagement.dto.EventDTO;
import com.clubmanagement.dto.FeedbackDTO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.dto.ProjectDTO;
import com.clubmanagement.service.EventService;
import com.clubmanagement.service.FeedbackService;
import com.clubmanagement.service.ProjectService;
import com.clubmanagement.view.FeedbackView;

public class FeedbackController {

    private final FeedbackView view;
    private final MemberDTO currentUser;
    private final FeedbackService feedbackService = new FeedbackService();
    private final EventService eventService = new EventService();
    private final ProjectService projectService = new ProjectService();

    public FeedbackController(FeedbackView view, MemberDTO currentUser) {
        this.view = view;
        this.currentUser = currentUser;
        attachListeners();
        loadAllFeedbacksInternal();
    }

    private void attachListeners() {
        view.getBtnRefresh().addActionListener(e -> loadAllFeedbacks());
        view.getBtnAdd().addActionListener(e -> handleAdd());
        
        if(currentUser.isLeader()){
            view.getBtnDelete().addActionListener(e -> handleDelete());
        }
    }

    private void loadAllFeedbacksInternal() {
        view.setStatusMessage("Đang tải danh sách Phản hồi...");
        SwingWorker<List<FeedbackDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<FeedbackDTO> doInBackground() {
                return feedbackService.getAllFeedbacks();
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

    public final void loadAllFeedbacks() {
        loadAllFeedbacksInternal();
    }

    private void handleAdd() {
        JDialog dialog = new JDialog((Frame) null, "Gửi Feedback / Góp ý", true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 12));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        JComboBox<String> cbType = new JComboBox<>(new String[]{"Club", "Event", "Project", "Other"});
        cbType.setFont(f);

        JComboBox<EventDTO> cbEvent = new JComboBox<>(eventService.getAllEvents().toArray(EventDTO[]::new));
        cbEvent.insertItemAt(null, 0); cbEvent.setSelectedIndex(0);
        cbEvent.setEnabled(false);
        
        JComboBox<ProjectDTO> cbProject = new JComboBox<>(projectService.getAllProjects().toArray(ProjectDTO[]::new));
        cbProject.insertItemAt(null, 0); cbProject.setSelectedIndex(0);
        cbProject.setEnabled(false);

        cbType.addActionListener(e -> {
            String sel = (String) cbType.getSelectedItem();
            cbEvent.setEnabled("Event".equals(sel));
            cbProject.setEnabled("Project".equals(sel));
            if(cbEvent.isEnabled() == false) cbEvent.setSelectedItem(null);
            if(cbProject.isEnabled() == false) cbProject.setSelectedItem(null);
        });

        JComboBox<Integer> cbRating = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        cbRating.setFont(f);

        JTextArea txtContent = new JTextArea(4, 20);
        txtContent.setFont(f);
        JScrollPane scrollPane = new JScrollPane(txtContent);

        panel.add(new JLabel("Loại * :"));     panel.add(cbType);
        panel.add(new JLabel("Sự kiện:"));     panel.add(cbEvent);
        panel.add(new JLabel("Dự án:"));       panel.add(cbProject);
        panel.add(new JLabel("Đánh giá *:"));  panel.add(cbRating);

        JButton btnSave = new JButton("Gửi ngay");
        btnSave.setBackground(new Color(16, 185, 129));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            try {
                String type = (String) cbType.getSelectedItem();
                Integer rating = (Integer) cbRating.getSelectedItem();
                String content = txtContent.getText();
                
                EventDTO ext = (EventDTO) cbEvent.getSelectedItem();
                ProjectDTO pro = (ProjectDTO) cbProject.getSelectedItem();
                
                Integer evtId = ext != null ? ext.getEventId() : null;
                Integer proId = pro != null ? pro.getProjectId() : null;

                feedbackService.submitFeedback(currentUser.getMemberId(), content, rating, type, evtId, proId);
                JOptionPane.showMessageDialog(dialog, "Cảm ơn bạn đã gửi phản hồi!");
                
                dialog.dispose();
                loadAllFeedbacks();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(0,20,10,20));
        contentPanel.add(new JLabel("Nội dung góp ý *:"), BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnCancel);
        bottom.add(btnSave);

        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.NORTH);
        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void handleDelete() {
        Integer id = view.getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn Phản hồi cần xóa!");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(null,
            "Bạn có chắc muốn xóa ?", "Xác nhận xóa", 
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                feedbackService.deleteFeedback(id);
                JOptionPane.showMessageDialog(null, "Đã xóa!");
                loadAllFeedbacks();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
