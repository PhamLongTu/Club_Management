package com.clubmanagement.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.clubmanagement.dto.AnnouncementDTO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.dto.TeamDTO;
import com.clubmanagement.service.AnnouncementService;
import com.clubmanagement.service.TeamService;
import com.clubmanagement.view.AnnouncementView;

public class AnnouncementController {

    private final AnnouncementView view;
    private final MemberDTO currentUser;
    private final AnnouncementService announcementService = new AnnouncementService();
    private final TeamService teamService = new TeamService();

    public AnnouncementController(AnnouncementView view, MemberDTO currentUser) {
        this.view = view;
        this.currentUser = currentUser;
        attachListeners();
        loadAllAnnouncementsInternal(); // Tải data lúc mới vào
    }

    private void attachListeners() {
        view.getBtnRefresh().addActionListener(e -> loadAllAnnouncements());

        view.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    handleViewDetail();
                }
            }
        });

        if (currentUser.isLeader()) {
            view.getBtnAdd().addActionListener(e -> handleAdd());
            view.getBtnEdit().addActionListener(e -> handleEdit());
            view.getBtnDelete().addActionListener(e -> handleDelete());
        }
    }

    private void loadAllAnnouncementsInternal() {
        view.setStatusMessage("Đang tải danh sách thông báo...");
        SwingWorker<List<AnnouncementDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AnnouncementDTO> doInBackground() {
                return announcementService.getAnnouncementsForUser(currentUser);
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

    public final void loadAllAnnouncements() {
        loadAllAnnouncementsInternal();
    }

    private void handleAdd() {
        JPanel form = buildForm(null);
        int res = JOptionPane.showConfirmDialog(null, form,
            "Tạo Thông báo mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                AnnounceFormData d = extractData(form);
                announcementService.createAnnouncement(
                    d.title, d.content, d.isPinned, d.targetAudience, d.targetTeamId, currentUser.getMemberId()
                );
                JOptionPane.showMessageDialog(null, "Đã đăng thông báo!");
                loadAllAnnouncements();
            } catch (RuntimeException ex) {
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
        List<AnnouncementDTO> currentData = announcementService.getAnnouncementsForUser(currentUser);
        Optional<AnnouncementDTO> opt = currentData.stream().filter(a -> a.getAnnouncementId().equals(id)).findFirst();
        
        if (opt.isEmpty()) return;
        AnnouncementDTO ann = opt.get();

        JPanel form = buildForm(ann);
        int res = JOptionPane.showConfirmDialog(null, form,
            "Sửa Thông báo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                AnnounceFormData d = extractData(form);
                announcementService.updateAnnouncement(id, d.title, d.content, d.isPinned, d.targetAudience, d.targetTeamId);
                JOptionPane.showMessageDialog(null, "Đã cập nhật thông báo!");
                loadAllAnnouncements();
            } catch (RuntimeException ex) {
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
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel buildForm(AnnouncementDTO ann) {
        JPanel form = new JPanel(new BorderLayout(8, 8));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        JTextField tfTitle = new JTextField(ann != null ? ann.getTitle() : "");
        tfTitle.setFont(f);
        tfTitle.setName("title");

        JComboBox<String> cbTarget = new JComboBox<>(new String[]{"All", "Leaders", "Members"});
        cbTarget.setFont(f);
        cbTarget.setName("target");
        if (ann != null) cbTarget.setSelectedItem(ann.getTargetAudience());

        JComboBox<TeamDTO> cbTeam = new JComboBox<>();
        cbTeam.setFont(f);
        cbTeam.setName("team");
        TeamDTO allTeamsOption = new TeamDTO(-1, "Tất cả ban", null, null, null, null);
        cbTeam.addItem(allTeamsOption);
        List<TeamDTO> teams = currentUser.isAdmin()
            ? teamService.getAllTeams()
            : teamService.getTeamsByLeader(currentUser.getMemberId());
        for (TeamDTO t : teams) cbTeam.addItem(t);
        if (ann != null && ann.getTargetTeamId() != null) {
            for (int i = 0; i < cbTeam.getItemCount(); i++) {
                TeamDTO item = cbTeam.getItemAt(i);
                if (item != null && ann.getTargetTeamId().equals(item.getTeamId())) {
                    cbTeam.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            cbTeam.setSelectedItem(allTeamsOption);
        }

        JCheckBox chkPinned = new JCheckBox("Ghim thông báo lên đầu");
        chkPinned.setFont(f);
        chkPinned.setName("pinned");
        if (ann != null && Boolean.TRUE.equals(ann.getIsPinned())) chkPinned.setSelected(true);

        topPanel.add(new JLabel("Tiêu đề *:")); topPanel.add(tfTitle);
        topPanel.add(new JLabel("Đối tượng:"));  topPanel.add(cbTarget);
        topPanel.add(new JLabel("Ban (tùy chọn):")); topPanel.add(cbTeam);
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
            else if (c instanceof JComboBox<?> cb && "team".equals(cb.getName())) {
                TeamDTO team = (TeamDTO) cb.getSelectedItem();
                d.targetTeamId = (team == null || Integer.valueOf(-1).equals(team.getTeamId()))
                    ? null
                    : team.getTeamId();
            }
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
        Integer targetTeamId;
    }

    private void handleViewDetail() {
        Integer id = view.getSelectedId();
        if (id == null) return;

        List<AnnouncementDTO> currentData = announcementService.getAnnouncementsForUser(currentUser);
        Optional<AnnouncementDTO> opt = currentData.stream().filter(a -> a.getAnnouncementId().equals(id)).findFirst();
        if (opt.isEmpty()) return;
        AnnouncementDTO ann = opt.get();

        JDialog dialog = new JDialog((Frame) null, "Thông báo", true);
        dialog.setSize(720, 520);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel(ann.getTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        String metaText = "Đăng bởi: " + ann.getAuthorName();
        if (ann.getCreatedDate() != null) {
            metaText += " | " + ann.getCreatedDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
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

        JTextArea content = new JTextArea();
        content.setText(ann.getContent());
        content.setEditable(false);
        content.setLineWrap(true);
        content.setWrapStyleWord(true);
        content.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        content.setBorder(new EmptyBorder(12, 16, 12, 16));

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(new JScrollPane(content), BorderLayout.CENTER);
        dialog.setVisible(true);
    }
}
