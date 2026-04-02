package com.clubmanagement.controller;

import com.clubmanagement.dto.AttendanceDTO;
import com.clubmanagement.dto.EventDTO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.service.AttendanceService;
import com.clubmanagement.service.EventService;
import com.clubmanagement.service.MemberService;
import com.clubmanagement.view.AttendanceView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AttendanceController {

    private final AttendanceView view;
    private final MemberDTO currentUser;
    private final AttendanceService attendanceService = new AttendanceService();
    private final MemberService memberService = new MemberService();
    private final EventService eventService = new EventService();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AttendanceController(AttendanceView view, MemberDTO currentUser) {
        this.view = view;
        this.currentUser = currentUser;
        attachListeners();
        loadAllAttendances();
    }

    private void attachListeners() {
        view.getBtnRefresh().addActionListener(e -> loadAllAttendances());

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

    public void loadAllAttendances() {
        view.setStatusMessage("Đang tải danh sách Điểm danh...");
        SwingWorker<List<AttendanceDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AttendanceDTO> doInBackground() {
                return attendanceService.getAllAttendances();
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
            JOptionPane.showMessageDialog(null, "Vui lòng chọn lượt Điểm danh cần sửa!");
            return;
        }

        List<AttendanceDTO> all = attendanceService.getAllAttendances();
        Optional<AttendanceDTO> opt = all.stream().filter(a -> a.getAttendanceId().equals(id)).findFirst();
        if (opt.isEmpty()) return;

        showFormDialog(opt.get());
    }

    private void showFormDialog(AttendanceDTO attendance) {
        JDialog dialog = new JDialog((Frame) null, attendance == null ? "✅ Cấp mã Điểm danh" : "✏ Sửa Điểm danh", true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 12));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        List<MemberDTO> allMembers = memberService.getAllMembers();
        JComboBox<MemberDTO> cbMember = new JComboBox<>(allMembers.toArray(new MemberDTO[0]));
        cbMember.setFont(f);

        List<EventDTO> allEvents = eventService.getAllEvents();
        JComboBox<EventDTO> cbEvent = new JComboBox<>(allEvents.toArray(new EventDTO[0]));
        cbEvent.setFont(f);

        JTextField txtCheckIn = new JTextField(LocalDateTime.now().format(DATE_FMT));
        txtCheckIn.setFont(f);
        
        JTextField txtCheckOut = new JTextField();
        txtCheckOut.setFont(f);

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Present", "Late", "Absent", "Excused"});
        cbStatus.setFont(f);

        JTextField txtNote = new JTextField();
        txtNote.setFont(f);

        if (attendance != null) {
            cbMember.setEnabled(false); // Không cho phép đổi người/event khi đã điểm danh
            cbEvent.setEnabled(false);
            
            for (int i = 0; i < cbMember.getItemCount(); i++) {
                if (cbMember.getItemAt(i).getMemberId().equals(attendance.getMemberId())) {
                    cbMember.setSelectedIndex(i); break;
                }
            }
            for (int i = 0; i < cbEvent.getItemCount(); i++) {
                if (cbEvent.getItemAt(i).getEventId().equals(attendance.getEventId())) {
                    cbEvent.setSelectedIndex(i); break;
                }
            }
            if (attendance.getCheckInTime() != null) txtCheckIn.setText(attendance.getCheckInTime().format(DATE_FMT));
            else txtCheckIn.setText("");
            
            if (attendance.getCheckOutTime() != null) txtCheckOut.setText(attendance.getCheckOutTime().format(DATE_FMT));
            
            cbStatus.setSelectedItem(attendance.getStatus());
            txtNote.setText(attendance.getNote());
        }

        panel.add(new JLabel("Thành viên *:"));     panel.add(cbMember);
        panel.add(new JLabel("Sự kiện *:"));        panel.add(cbEvent);
        panel.add(new JLabel("Check-in (yyyy-MM-dd HH:mm):")); panel.add(txtCheckIn);
        panel.add(new JLabel("Check-out:"));        panel.add(txtCheckOut);
        panel.add(new JLabel("Trạng thái:"));       panel.add(cbStatus);
        panel.add(new JLabel("Ghi chú:"));          panel.add(txtNote);

        JButton btnSave = new JButton("Lưu thay đổi");
        btnSave.setBackground(new Color(16, 185, 129));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            try {
                MemberDTO mem = (MemberDTO) cbMember.getSelectedItem();
                EventDTO ext = (EventDTO) cbEvent.getSelectedItem();
                
                LocalDateTime checkIn = null;
                if (!txtCheckIn.getText().isBlank()) {
                    checkIn = LocalDateTime.parse(txtCheckIn.getText().trim(), DATE_FMT);
                }
                LocalDateTime checkOut = null;
                if (!txtCheckOut.getText().isBlank()) {
                    checkOut = LocalDateTime.parse(txtCheckOut.getText().trim(), DATE_FMT);
                }
                
                String status = (String) cbStatus.getSelectedItem();
                String note = txtNote.getText();

                if (attendance == null) {
                    attendanceService.checkIn(mem.getMemberId(), ext.getEventId(), checkIn, status, note);
                    JOptionPane.showMessageDialog(dialog, "Điểm danh hoàn tất!");
                } else {
                    attendanceService.updateAttendance(attendance.getAttendanceId(), checkIn, checkOut, status, note);
                    JOptionPane.showMessageDialog(dialog, "Đã cập nhật thay đổi!");
                }
                dialog.dispose();
                loadAllAttendances();
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
            JOptionPane.showMessageDialog(null, "Vui lòng chọn Điểm danh cần xóa!");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(null,
            "Bạn có chắc muốn xóa ?", "Xác nhận xóa", 
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                attendanceService.deleteAttendance(id);
                JOptionPane.showMessageDialog(null, "Đã xóa!");
                loadAllAttendances();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
