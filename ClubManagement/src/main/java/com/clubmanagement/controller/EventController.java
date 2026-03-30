package com.clubmanagement.controller;

import com.clubmanagement.dto.EventDTO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.service.EventService;
import com.clubmanagement.view.EventView;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * EventController - Bộ điều khiển màn hình Sự kiện.
 *
 * Xử lý các thao tác CRUD cho Sự kiện thông qua dialog nhập liệu.
 */
public class EventController {

    private final EventView    view;
    private final MemberDTO    currentUser;
    private final EventService eventService = new EventService();

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EventController(EventView view, MemberDTO currentUser) {
        this.view        = view;
        this.currentUser = currentUser;
        attachListeners();
    }

    /**
     * Đăng ký tất cả event listeners cho EventView.
     */
    private void attachListeners() {
        view.getBtnRefresh().addActionListener(e -> loadAllEvents());
        view.getBtnSearch().addActionListener(e -> handleSearch());
        view.getSearchField().addActionListener(e -> handleSearch());
        view.getStatusFilter2().addActionListener(e -> handleSearch());
        view.getBtnAdd().addActionListener(e -> handleAdd());
        view.getBtnEdit().addActionListener(e -> handleEdit());
        view.getBtnDelete().addActionListener(e -> handleDelete());

        // Double-click → mở form sửa
        view.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && currentUser.isLeader()) handleEdit();
            }
        });
    }

    /** Mở form Tạo sự kiện mới (gọi từ DashboardController quick action). */
    public void openAddDialog() { handleAdd(); }

    /** Tải tất cả sự kiện vào bảng. */
    public void loadAllEvents() {
        view.setStatusMessage("Đang tải dữ liệu...");
        SwingWorker<List<EventDTO>, Void> worker = new SwingWorker<>() {
            @Override protected List<EventDTO> doInBackground() { return eventService.getAllEvents(); }
            @Override protected void done() {
                try { view.loadData(get()); }
                catch (Exception e) { view.setStatusMessage("Lỗi: " + e.getMessage()); }
            }
        };
        worker.execute();
    }

    /** Tìm kiếm và lọc sự kiện. */
    private void handleSearch() {
        String keyword = view.getSearchKeyword();
        String status  = view.getStatusFilter();
        SwingWorker<List<EventDTO>, Void> worker = new SwingWorker<>() {
            @Override protected List<EventDTO> doInBackground() {
                List<EventDTO> results = eventService.searchEvents(keyword);
                if (!"Tất cả".equals(status)) {
                    results = results.stream().filter(e -> status.equals(e.getStatus())).toList();
                }
                return results;
            }
            @Override protected void done() {
                try { view.loadData(get()); }
                catch (Exception e) { view.setStatusMessage("Lỗi: " + e.getMessage()); }
            }
        };
        worker.execute();
    }

    /**
     * Hiển thị dialog nhập liệu để thêm sự kiện mới.
     * Dùng JOptionPane với panel tùy chỉnh vì không có EventFormDialog riêng.
     */
    private void handleAdd() {
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền tạo sự kiện!");
            return;
        }

        JPanel form = buildEventForm(null);
        int result = JOptionPane.showConfirmDialog(null, form,
            "➕ Thêm sự kiện mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                EventFormData data = extractFormData(form);
                eventService.createEvent(
                    data.name, data.description, data.startDate, data.endDate,
                    data.location, data.budget, data.maxParticipants, currentUser.getMemberId()
                );
                JOptionPane.showMessageDialog(null, "Đã tạo sự kiện thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAllEvents();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Sửa sự kiện đang được chọn. */
    private void handleEdit() {
        Integer id = view.getSelectedEventId();
        if (id == null) { JOptionPane.showMessageDialog(null, "Chưa chọn sự kiện!"); return; }

        Optional<EventDTO> opt = eventService.getEventById(id);
        if (opt.isEmpty()) { JOptionPane.showMessageDialog(null, "Không tìm thấy sự kiện!"); return; }

        EventDTO event = opt.get();
        JPanel form = buildEventForm(event);
        int result = JOptionPane.showConfirmDialog(null, form,
            "✏ Sửa sự kiện", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                EventFormData data = extractFormData(form);
                eventService.updateEvent(id, data.name, data.description,
                    data.startDate, data.endDate, data.location, data.budget, data.status);
                JOptionPane.showMessageDialog(null, "Đã cập nhật sự kiện!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAllEvents();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Xóa sự kiện đang được chọn. */
    private void handleDelete() {
        Integer id = view.getSelectedEventId();
        if (id == null) { JOptionPane.showMessageDialog(null, "Chưa chọn sự kiện!"); return; }

        int choice = JOptionPane.showConfirmDialog(null,
            "Xóa sự kiện này? Dữ liệu đăng ký và điểm danh sẽ bị xóa theo!",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                eventService.deleteEvent(id);
                JOptionPane.showMessageDialog(null, "Đã xóa sự kiện!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAllEvents();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Xây dựng panel form nhập liệu sự kiện.
     * @param event null = form rỗng (ADD), non-null = form điền sẵn (EDIT)
     */
    private JPanel buildEventForm(EventDTO event) {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(new javax.swing.border.EmptyBorder(10, 10, 10, 10));
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        JTextField tfName     = createField(event != null ? event.getEventName()    : "", f);
        JTextField tfStart    = createField(event != null && event.getStartDate() != null ? event.getStartDate().format(DT_FMT) : "", f);
        JTextField tfEnd      = createField(event != null && event.getEndDate()   != null ? event.getEndDate().format(DT_FMT)   : "", f);
        JTextField tfLocation = createField(event != null ? event.getLocation()    : "", f);
        JTextField tfBudget   = createField(event != null && event.getBudget() != null ? event.getBudget().toPlainString() : "0", f);
        JTextField tfMaxP     = createField(event != null && event.getMaxParticipants() != null ? String.valueOf(event.getMaxParticipants()) : "100", f);
        JTextArea  taDesc     = new JTextArea(3, 20);
        taDesc.setFont(f);
        taDesc.setText(event != null ? event.getDescription() : "");
        taDesc.setLineWrap(true);

        String[] statuses = {"Upcoming", "Ongoing", "Completed", "Cancelled"};
        JComboBox<String> cbStatus = new JComboBox<>(statuses);
        cbStatus.setFont(f);
        if (event != null) cbStatus.setSelectedItem(event.getStatus());

        form.add(makeLabel("Tên sự kiện *:")); form.add(tfName);
        form.add(makeLabel("Bắt đầu (yyyy-MM-dd HH:mm) *:")); form.add(tfStart);
        form.add(makeLabel("Kết thúc (yyyy-MM-dd HH:mm) *:")); form.add(tfEnd);
        form.add(makeLabel("Địa điểm:")); form.add(tfLocation);
        form.add(makeLabel("Ngân sách (VNĐ):")); form.add(tfBudget);
        form.add(makeLabel("Số lượng tối đa:")); form.add(tfMaxP);
        form.add(makeLabel("Trạng thái:")); form.add(cbStatus);
        form.add(makeLabel("Mô tả:")); form.add(new JScrollPane(taDesc));

        // Tag components để extract sau
        tfName.setName("name");   tfStart.setName("start");
        tfEnd.setName("end");     tfLocation.setName("location");
        tfBudget.setName("budget"); tfMaxP.setName("maxP");
        cbStatus.setName("status"); taDesc.setName("desc");

        return form;
    }

    /**
     * Đọc dữ liệu từ form panel và validate.
     */
    private EventFormData extractFormData(JPanel form) {
        EventFormData data = new EventFormData();
        for (Component c : form.getComponents()) {
            if (c instanceof JTextField tf) {
                switch (tf.getName()) {
                    case "name"     -> data.name     = tf.getText().trim();
                    case "start"    -> data.startDate = parseDateTime(tf.getText());
                    case "end"      -> data.endDate   = parseDateTime(tf.getText());
                    case "location" -> data.location  = tf.getText().trim();
                    case "budget"   -> { try { data.budget = new BigDecimal(tf.getText().trim()); } catch (Exception e) { data.budget = BigDecimal.ZERO; } }
                    case "maxP"     -> { try { data.maxParticipants = Integer.parseInt(tf.getText().trim()); } catch (Exception e) { data.maxParticipants = 100; } }
                }
            } else if (c instanceof JComboBox<?> cb && "status".equals(cb.getName())) {
                data.status = (String) cb.getSelectedItem();
            } else if (c instanceof JScrollPane sp && sp.getViewport().getView() instanceof JTextArea ta) {
                data.description = ta.getText().trim();
            }
        }
        if (data.name == null || data.name.isBlank())
            throw new IllegalArgumentException("Tên sự kiện không được để trống!");
        if (data.startDate == null || data.endDate == null)
            throw new IllegalArgumentException("Ngày bắt đầu và kết thúc không được để trống!");
        if (data.endDate.isBefore(data.startDate))
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu!");
        return data;
    }

    private LocalDateTime parseDateTime(String text) {
        String t = text.trim();
        if (t.isEmpty()) return null;
        try {
            return LocalDateTime.parse(t, DateTimeFormatter.ofPattern("yyyy-M-d H:m"));
        } catch (DateTimeParseException e) {
            try {
                return java.time.LocalDate.parse(t, DateTimeFormatter.ofPattern("yyyy-M-d")).atStartOfDay();
            } catch (Exception ex) {
                throw new IllegalArgumentException("Sai định dạng ngày: '" + t + "'. Vui lòng dùng: yyyy-MM-dd HH:mm hoặc yyyy-MM-dd");
            }
        }
    }

    private JTextField createField(String value, Font f) {
        JTextField tf = new JTextField(value);
        tf.setFont(f);
        return tf;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    /** Inner class để giữ dữ liệu form. */
    private static class EventFormData {
        String name, description, location, status = "Upcoming";
        LocalDateTime startDate, endDate;
        BigDecimal budget = BigDecimal.ZERO;
        Integer maxParticipants = 100;
    }
}
