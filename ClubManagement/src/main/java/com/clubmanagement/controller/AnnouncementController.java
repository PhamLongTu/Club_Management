package com.clubmanagement.controller;

import com.clubmanagement.dto.AnnouncementDTO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.service.AnnouncementService;
import com.clubmanagement.view.AnnouncementView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class AnnouncementController {

    private final AnnouncementView view;
    private final MemberDTO currentUser;
    private final AnnouncementService announcementService = new AnnouncementService();

    public AnnouncementController(AnnouncementView view, MemberDTO currentUser) {
        this.view = view;
        this.currentUser = currentUser;
        attachListeners();
        loadAllAnnouncements(); // Tải data lúc mới vào
    }

    private void attachListeners() {
        view.getBtnRefresh().addActionListener(e -> loadAllAnnouncements());
        
        if (currentUser.isLeader()) {
            view.getBtnAdd().addActionListener(e -> handleAdd());
            view.getBtnEdit().addActionListener(e -> handleEdit());
            view.getBtnDelete().addActionListener(e -> handleDelete());
            
            view.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) handleEdit();
                }
            });
        }
    }

    public void loadAllAnnouncements() {
        view.setStatusMessage("Đang tải danh sách thông báo...");
        SwingWorker<List<AnnouncementDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AnnouncementDTO> doInBackground() {
                return announcementService.getAllAnnouncements();
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
        JPanel form = buildForm(null);
        int res = JOptionPane.showConfirmDialog(null, form,
            "📣 Tạo Thông báo mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                AnnounceFormData d = extractData(form);
                announcementService.createAnnouncement(
                    d.title, d.content, d.isPinned, d.targetAudience, currentUser.getMemberId()
                );
                JOptionPane.showMessageDialog(null, "Đã đăng thông báo!");
                loadAllAnnouncements();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEdit() {
        Integer id = view.getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn thông báo cần sửa!");
            return;
        }

        // Tìm AnnouncementDTO từ table model
        List<AnnouncementDTO> currentData = announcementService.getAllAnnouncements();
        Optional<AnnouncementDTO> opt = currentData.stream().filter(a -> a.getAnnouncementId().equals(id)).findFirst();
        
        if (opt.isEmpty()) return;
        AnnouncementDTO ann = opt.get();

        JPanel form = buildForm(ann);
        int res = JOptionPane.showConfirmDialog(null, form,
            "✏ Sửa Thông báo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                AnnounceFormData d = extractData(form);
                announcementService.updateAnnouncement(id, d.title, d.content, d.isPinned, d.targetAudience);
                JOptionPane.showMessageDialog(null, "Đã cập nhật thông báo!");
                loadAllAnnouncements();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDelete() {
        Integer id = view.getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn thông báo cần xóa!");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(null,
            "Bạn có chắc chắn muốn xóa thông báo này?", "Xác nhận",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                announcementService.deleteAnnouncement(id);
                JOptionPane.showMessageDialog(null, "Đã xóa!");
                loadAllAnnouncements();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel buildForm(AnnouncementDTO ann) {
        JPanel form = new JPanel(new BorderLayout(8, 8));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        JTextField tfTitle = new JTextField(ann != null ? ann.getTitle() : "");
        tfTitle.setFont(f);
        tfTitle.setName("title");

        JComboBox<String> cbTarget = new JComboBox<>(new String[]{"All", "Leaders", "Members"});
        cbTarget.setFont(f);
        cbTarget.setName("target");
        if (ann != null) cbTarget.setSelectedItem(ann.getTargetAudience());

        JCheckBox chkPinned = new JCheckBox("Ghim thông báo lên đầu");
        chkPinned.setFont(f);
        chkPinned.setName("pinned");
        if (ann != null && Boolean.TRUE.equals(ann.getIsPinned())) chkPinned.setSelected(true);

        topPanel.add(new JLabel("Tiêu đề *:")); topPanel.add(tfTitle);
        topPanel.add(new JLabel("Đối tượng:"));  topPanel.add(cbTarget);
        topPanel.add(new JLabel("Tùy chọn:"));  topPanel.add(chkPinned);

        JTextArea taContent = new JTextArea(8, 40);
        taContent.setFont(f);
        taContent.setName("content");
        taContent.setLineWrap(true);
        taContent.setWrapStyleWord(true);
        if (ann != null) taContent.setText(ann.getContent());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JLabel("Nội dung *:"), BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(taContent), BorderLayout.CENTER);

        form.add(topPanel, BorderLayout.NORTH);
        form.add(centerPanel, BorderLayout.CENTER);
        return form;
    }

    private AnnounceFormData extractData(JPanel form) {
        AnnounceFormData d = new AnnounceFormData();
        JPanel topPanel = (JPanel) form.getComponent(0);
        JPanel centerPanel = (JPanel) form.getComponent(1);

        for (Component c : topPanel.getComponents()) {
            if (c instanceof JTextField tf && "title".equals(tf.getName())) d.title = tf.getText().trim();
            else if (c instanceof JComboBox<?> cb && "target".equals(cb.getName())) d.targetAudience = (String) cb.getSelectedItem();
            else if (c instanceof JCheckBox chk && "pinned".equals(chk.getName())) d.isPinned = chk.isSelected();
        }

        JScrollPane sp = (JScrollPane) centerPanel.getComponent(1);
        JTextArea ta = (JTextArea) sp.getViewport().getView();
        d.content = ta.getText().trim();

        if (d.title == null || d.title.isBlank()) throw new IllegalArgumentException("Tiêu đề không được trống");
        if (d.content == null || d.content.isBlank()) throw new IllegalArgumentException("Nội dung không được trống");

        return d;
    }

    private static class AnnounceFormData {
        String title, content, targetAudience;
        boolean isPinned;
    }
}
