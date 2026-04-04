package com.clubmanagement.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.clubmanagement.dto.AttendanceDTO;
import com.clubmanagement.dto.EventDTO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.service.AttendanceService;
import com.clubmanagement.service.EventService;
import com.clubmanagement.service.MemberService;
import com.clubmanagement.util.UiFormUtil;
import com.clubmanagement.view.AttendanceView;
import com.toedter.calendar.JDateChooser;

/**
 * AttendanceController - Điều khiển màn hình Điểm danh.
 */
public class AttendanceController {

    private final AttendanceView view;
    private final MemberDTO currentUser;
    private final AttendanceService attendanceService = new AttendanceService();
    private final MemberService memberService = new MemberService();
    private final EventService eventService = new EventService();

    /**
     * Khởi tạo controller cho màn hình Điểm danh.
     * @param view View hiển thị
     * @param currentUser Người dùng hiện tại
     */
    public AttendanceController(AttendanceView view, MemberDTO currentUser) {
        this.view = view;
        this.currentUser = currentUser;
        attachListeners();
        loadAllAttendancesInternal();
    }

    /**
     * Đăng ký các sự kiện cho view.
     */
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

    /**
     * Tải danh sách điểm danh (chạy nền).
     */
    private void loadAllAttendancesInternal() {
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
     * Refresh danh sách điểm danh.
     */
    public final void loadAllAttendances() {
        loadAllAttendancesInternal();
    }

    /**
     * Mở form thêm mới điểm danh.
     */
    private void handleAdd() {
        showFormDialog(null);
    }

    /**
     * Mở form sửa điểm danh đang chọn.
     */
    private void handleEdit() {
        Integer id = view.getSelectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn lượt Điểm danh cần sửa!");
            return;
        }

        Optional<AttendanceDTO> opt = attendanceService.getAttendanceById(id);
        if (opt.isEmpty()) return;

        showFormDialog(opt.get());
    }

    /**
     * Hiển thị form nhập liệu điểm danh.
     * @param attendance Dữ liệu hiện tại (nullable)
     */
    private void showFormDialog(AttendanceDTO attendance) {
        String title = attendance == null ? "Cấp mã Điểm danh" : "Sửa Điểm danh";
        JDialog dialog = new JDialog((Frame) null, title, true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        List<MemberDTO> allMembers = memberService.getAllMembers();
        JComboBox<MemberDTO> cbMember = new JComboBox<>(allMembers.toArray(MemberDTO[]::new));
        cbMember.setFont(f);

        List<EventDTO> allEvents = eventService.getAllEvents();
        JComboBox<EventDTO> cbEvent = new JComboBox<>(allEvents.toArray(EventDTO[]::new));
        cbEvent.setFont(f);

        LocalDateTime defaultCheckIn = attendance != null ? attendance.getCheckInTime() : LocalDateTime.now();
        JDateChooser dcCheckIn = UiFormUtil.createDateChooser(defaultCheckIn);
        JSpinner spCheckInTime = UiFormUtil.createTimeSpinner(defaultCheckIn);

        LocalDateTime defaultCheckOut = attendance != null ? attendance.getCheckOutTime() : null;
        JDateChooser dcCheckOut = UiFormUtil.createDateChooser(defaultCheckOut);
        JSpinner spCheckOutTime = UiFormUtil.createTimeSpinner(defaultCheckOut != null ? defaultCheckOut : LocalDateTime.now());

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
            if (attendance.getCheckInTime() == null) {
                dcCheckIn.setDate(null);
            }
            if (attendance.getCheckOutTime() == null) {
                dcCheckOut.setDate(null);
            }

            cbStatus.setSelectedItem(attendance.getStatus());
            txtNote.setText(attendance.getNote());
        }

        panel.add(new JLabel("Thành viên *:"));     panel.add(cbMember);
        panel.add(new JLabel("Sự kiện *:"));        panel.add(cbEvent);
        panel.add(new JLabel("Check-in:"));        panel.add(UiFormUtil.buildDateTimePanel(dcCheckIn, spCheckInTime));
        panel.add(new JLabel("Check-out:"));       panel.add(UiFormUtil.buildDateTimePanel(dcCheckOut, spCheckOutTime));
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
                
                LocalDateTime checkIn = UiFormUtil.toLocalDateTime(dcCheckIn, spCheckInTime);
                LocalDateTime checkOut = UiFormUtil.toLocalDateTime(dcCheckOut, spCheckOutTime);
                
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
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnCancel);
        bottom.add(btnSave);

        dialog.setLayout(new BorderLayout());
        dialog.add(UiFormUtil.buildDialogHeader(title), BorderLayout.NORTH);
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Xóa lượt điểm danh đang chọn.
     */
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
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
