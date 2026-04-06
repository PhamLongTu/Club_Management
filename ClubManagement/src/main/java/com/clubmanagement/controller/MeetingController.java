package com.clubmanagement.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.clubmanagement.dto.MeetingDTO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.service.MeetingService;
import com.clubmanagement.service.MemberService;
import com.clubmanagement.util.UiAsyncUtil;
import com.clubmanagement.util.UiFormUtil;
import com.clubmanagement.view.MeetingView;
import com.toedter.calendar.JDateChooser;

/**
 * MeetingController - Dieu khien man hinh Cuoc hop.
 */
public class MeetingController {

    private final MeetingView view;
    private final MemberDTO currentUser;
    private final MeetingService meetingService = new MeetingService();
    private final MemberService memberService = new MemberService();

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Khoi tao controller cho man hinh Cuoc hop.
     */
    public MeetingController(MeetingView view, MemberDTO currentUser) {
        this.view = view;
        this.currentUser = currentUser;
        attachListeners();
        loadAllMeetingsInternal();
    }

    private void attachListeners() {
        view.getBtnRefresh().addActionListener(e -> loadAllMeetings());
        view.getBtnEdit().addActionListener(e -> handleEdit());

        if (currentUser.isLeader()) {
            view.getBtnAdd().addActionListener(e -> handleAdd());
            view.getBtnDelete().addActionListener(e -> handleDelete());
        }

        view.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleViewDetail();
                }
            }
        });
    }

    private void loadAllMeetingsInternal() {
        UiAsyncUtil.runWithStatus(
            "Dang tai danh sach cuoc hop...",
            view::setStatusMessage,
            meetingService::getAllMeetings,
            view::loadData
        );
    }

    /** Refresh danh sach. */
    public final void loadAllMeetings() { loadAllMeetingsInternal(); }

    private void handleAdd() {
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Ban khong co quyen tao cuoc hop!");
            return;
        }
        showMeetingDialog(null);
    }

    private void handleEdit() {
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Ban khong co quyen sua cuoc hop!");
            return;
        }
        Integer id = view.getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(null, "Vui long chon cuoc hop can sua!");
            return;
        }
        Optional<MeetingDTO> opt = meetingService.getMeetingById(id);
        if (opt.isEmpty()) return;
        showMeetingDialog(opt.get());
    }

    private void handleDelete() {
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Ban khong co quyen xoa cuoc hop!");
            return;
        }
        Integer id = view.getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(null, "Vui long chon cuoc hop can xoa!");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(null,
            "Xoa cuoc hop nay?", "Xac nhan", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                meetingService.deleteMeeting(id);
                JOptionPane.showMessageDialog(null, "Da xoa cuoc hop!");
                loadAllMeetings();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null, "Loi: " + ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleViewDetail() {
        Integer id = view.getSelectedId();
        if (id == null) return;
        Optional<MeetingDTO> opt = meetingService.getMeetingById(id);
        if (opt.isEmpty()) return;
        MeetingDTO m = opt.get();

        String start = m.getStartTime() != null ? m.getStartTime().format(DT_FMT) : "";
        String end = m.getEndTime() != null ? m.getEndTime().format(DT_FMT) : "";
        String link = (m.getMeetLink() == null || m.getMeetLink().isBlank()) ? "-" : m.getMeetLink();
        String location = (m.getLocation() == null || m.getLocation().isBlank()) ? "-" : m.getLocation();
        String content = (m.getContent() == null || m.getContent().isBlank()) ? "-" : m.getContent();

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel info = new JPanel(new GridLayout(0, 1, 4, 4));
        info.add(UiFormUtil.makeInfoLabel("Tieu de: " + m.getTitle()));
        info.add(UiFormUtil.makeInfoLabel("Thoi gian: " + start + " - " + end));
        info.add(UiFormUtil.makeInfoLabel("Dia diem: " + location));
        info.add(UiFormUtil.makeInfoLabel("Chu tri: " + m.getHostName()));
        info.add(UiFormUtil.makeInfoLabel("Link: " + link));

        JTextArea taContent = new JTextArea(content, 6, 40);
        taContent.setLineWrap(true);
        taContent.setWrapStyleWord(true);
        taContent.setEditable(false);
        taContent.setBackground(new Color(248, 250, 252));

        panel.add(info, BorderLayout.NORTH);
        panel.add(new JScrollPane(taContent), BorderLayout.CENTER);

        JOptionPane.showMessageDialog(null, panel, "Chi tiet cuoc hop", JOptionPane.PLAIN_MESSAGE);
    }

    private void showMeetingDialog(MeetingDTO meeting) {
        boolean isEdit = meeting != null;
        String title = isEdit ? "Sua cuoc hop" : "Them cuoc hop";

        JDialog dialog = new JDialog((Frame) null, title, true);
        dialog.setSize(760, 560);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());

        MeetingFormFields fields = buildMeetingForm(meeting);

        dialog.add(UiFormUtil.buildDialogHeader(title), BorderLayout.NORTH);
        dialog.add(fields.panel, BorderLayout.CENTER);
        dialog.add(buildDialogFooter(dialog, fields, meeting), BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel buildDialogFooter(JDialog dialog, MeetingFormFields fields, MeetingDTO meeting) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(new EmptyBorder(8, 16, 12, 16));

        JButton btnCancel = new JButton("Huy");
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = new JButton(meeting != null ? "Cap nhat" : "Tao moi");
        btnSave.setBackground(new Color(16, 185, 129));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBorderPainted(false);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> {
            try {
                MeetingFormData data = extractFormData(fields);
                if (meeting == null) {
                    meetingService.createMeeting(
                        data.title, data.content, data.startTime, data.endTime,
                        data.location, data.meetLink, data.hostId
                    );
                    JOptionPane.showMessageDialog(dialog, "Da tao cuoc hop!");
                } else {
                    meetingService.updateMeeting(
                        meeting.getMeetingId(), data.title, data.content, data.startTime, data.endTime,
                        data.location, data.meetLink, data.hostId
                    );
                    JOptionPane.showMessageDialog(dialog, "Da cap nhat cuoc hop!");
                }
                dialog.dispose();
                loadAllMeetings();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(dialog, "Loi: " + ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    private MeetingFormFields buildMeetingForm(MeetingDTO meeting) {
        JPanel form = new JPanel(new BorderLayout(8, 8));
        form.setBorder(new EmptyBorder(12, 16, 12, 16));
        form.setBackground(Color.WHITE);
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
        grid.setBackground(Color.WHITE);

        JTextField txtTitle = new JTextField();
        txtTitle.setFont(f);
        if (meeting != null) txtTitle.setText(meeting.getTitle());

        List<MemberDTO> members = memberService.getAllMembers();
        JComboBox<MemberDTO> cbHost = new JComboBox<>(members.toArray(MemberDTO[]::new));
        cbHost.setFont(f);
        if (meeting != null && meeting.getHostId() != null) {
            for (int i = 0; i < cbHost.getItemCount(); i++) {
                MemberDTO m = cbHost.getItemAt(i);
                if (m != null && meeting.getHostId().equals(m.getMemberId())) {
                    cbHost.setSelectedIndex(i);
                    break;
                }
            }
        }

        LocalDateTime startDefault = meeting != null && meeting.getStartTime() != null
            ? meeting.getStartTime()
            : LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
        LocalDateTime endDefault = meeting != null && meeting.getEndTime() != null
            ? meeting.getEndTime()
            : startDefault.plusHours(2);

        JDateChooser dcStart = UiFormUtil.createDateChooser(startDefault);
        JDateChooser dcEnd = UiFormUtil.createDateChooser(endDefault);
        var spStart = UiFormUtil.createTimeSpinner(startDefault);
        var spEnd = UiFormUtil.createTimeSpinner(endDefault);

        JTextField txtLocation = new JTextField();
        txtLocation.setFont(f);
        if (meeting != null) txtLocation.setText(meeting.getLocation());

        JTextField txtLink = new JTextField();
        txtLink.setFont(f);
        if (meeting != null) txtLink.setText(meeting.getMeetLink());

        JTextArea txtContent = new JTextArea(4, 20);
        txtContent.setFont(f);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        if (meeting != null) txtContent.setText(meeting.getContent());

        grid.add(new JLabel("Tieu de *:"));
        grid.add(txtTitle);
        grid.add(new JLabel("Chu tri *:"));
        grid.add(cbHost);
        grid.add(new JLabel("Bat dau *:"));
        grid.add(UiFormUtil.buildDateTimePanel(dcStart, spStart));
        grid.add(new JLabel("Ket thuc *:"));
        grid.add(UiFormUtil.buildDateTimePanel(dcEnd, spEnd));
        grid.add(new JLabel("Dia diem:"));
        grid.add(txtLocation);
        grid.add(new JLabel("Link Google Meet:"));
        grid.add(txtLink);

        JPanel contentPanel = new JPanel(new BorderLayout(4, 4));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(new JLabel("Noi dung:"), BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(txtContent), BorderLayout.CENTER);

        form.add(grid, BorderLayout.NORTH);
        form.add(contentPanel, BorderLayout.CENTER);

        MeetingFormFields fields = new MeetingFormFields();
        fields.panel = form;
        fields.txtTitle = txtTitle;
        fields.cbHost = cbHost;
        fields.dcStart = dcStart;
        fields.spStart = spStart;
        fields.dcEnd = dcEnd;
        fields.spEnd = spEnd;
        fields.txtLocation = txtLocation;
        fields.txtLink = txtLink;
        fields.txtContent = txtContent;
        return fields;
    }

    private MeetingFormData extractFormData(MeetingFormFields fields) {
        String title = fields.txtTitle.getText();
        MemberDTO host = (MemberDTO) fields.cbHost.getSelectedItem();
        Integer hostId = host != null ? host.getMemberId() : null;
        LocalDateTime startTime = UiFormUtil.toLocalDateTime(fields.dcStart, fields.spStart);
        LocalDateTime endTime = UiFormUtil.toLocalDateTime(fields.dcEnd, fields.spEnd);
        String location = fields.txtLocation.getText();
        String meetLink = fields.txtLink.getText();
        String content = fields.txtContent.getText();

        MeetingFormData data = new MeetingFormData();
        data.title = title;
        data.hostId = hostId;
        data.startTime = startTime;
        data.endTime = endTime;
        data.location = location;
        data.meetLink = meetLink;
        data.content = content;
        return data;
    }

    private static class MeetingFormFields {
        JPanel panel;
        JTextField txtTitle;
        JComboBox<MemberDTO> cbHost;
        JDateChooser dcStart;
        JDateChooser dcEnd;
        javax.swing.JSpinner spStart;
        javax.swing.JSpinner spEnd;
        JTextField txtLocation;
        JTextField txtLink;
        JTextArea txtContent;
    }

    private static class MeetingFormData {
        String title;
        Integer hostId;
        LocalDateTime startTime;
        LocalDateTime endTime;
        String location;
        String meetLink;
        String content;
    }
}
