package com.clubmanagement.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.clubmanagement.dto.EventDTO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.service.EventService;
import com.clubmanagement.util.UiFormUtil;
import com.clubmanagement.view.EventView;
import com.toedter.calendar.JDateChooser;

/**
 * EventController - Bộ điều khiển màn hình Sự kiện.
 *
 * Xử lý các thao tác CRUD cho Sự kiện thông qua dialog nhập liệu.
 */
public class EventController {

    private final EventView    view;
    private final MemberDTO    currentUser;
    private final EventService eventService = new EventService();
    private final Consumer<Integer> adminDetailOpener;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Khởi tạo controller cho màn hình Sự kiện.
     * @param view View hiển thị
     * @param currentUser Người dùng hiện tại
     */
    public EventController(EventView view, MemberDTO currentUser, Consumer<Integer> adminDetailOpener) {
        this.view = view;
        this.currentUser = currentUser;
        this.adminDetailOpener = adminDetailOpener;
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

        // Click → mở chi tiết
        view.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) handleViewDetail();
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
     * Hiển thị dialog nhập liệu để thêm sự kiện mới.
     * Dùng JOptionPane với panel tùy chỉnh vì không có EventFormDialog riêng.
     */
    private void handleAdd() {
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền tạo sự kiện!");
            return;
        }
        showEventDialog(null, false);
    }

    /** Sửa sự kiện đang được chọn. */
    private void handleEdit() {
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền chỉnh sửa sự kiện!",
                "Không đủ quyền", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer id = view.getSelectedEventId();
        if (id == null) { JOptionPane.showMessageDialog(null, "Chưa chọn sự kiện!"); return; }

        Optional<EventDTO> opt = eventService.getEventById(id);
        if (opt.isEmpty()) { JOptionPane.showMessageDialog(null, "Không tìm thấy sự kiện!"); return; }
        showEventDialog(opt.get(), true);
    }

    /** Xóa sự kiện đang được chọn. */
    private void handleDelete() {
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền xóa sự kiện!",
                "Không đủ quyền", JOptionPane.WARNING_MESSAGE);
            return;
        }
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
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Mở dialog thêm/sửa sự kiện.
     * @param event Dữ liệu hiện tại (nullable)
     * @param isEdit true nếu sửa, false nếu thêm mới
     */
    private void showEventDialog(EventDTO event, boolean isEdit) {
        EventFormFields fields = buildEventForm(event);
        String title = isEdit ? "Sửa sự kiện" : "Thêm sự kiện mới";

        JDialog dialog = new JDialog((Frame) null, title, true);
        dialog.setSize(760, 560);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());

        dialog.add(UiFormUtil.buildDialogHeader(title), BorderLayout.NORTH);
        dialog.add(fields.panel, BorderLayout.CENTER);
        dialog.add(buildDialogFooter(dialog, fields, event, isEdit), BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Tạo footer chứa các nút hành động.
     * @param dialog Dialog hiện tại
     * @param fields Trường dữ liệu form
     * @param event Dữ liệu hiện tại
     * @param isEdit true nếu sửa
     * @return JPanel footer
     */
    private JPanel buildDialogFooter(JDialog dialog, EventFormFields fields, EventDTO event, boolean isEdit) {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(new EmptyBorder(8, 16, 12, 16));

        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = new JButton(isEdit ? "Cập nhật" : "Tạo mới");
        btnSave.setBackground(new Color(16, 185, 129));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBorderPainted(false);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> {
            try {
                EventFormData data = extractFormData(fields);
                if (isEdit) {
                    eventService.updateEvent(event.getEventId(), data.name, data.description,
                        data.startDate, data.endDate, data.registrationDeadline,
                        data.location, data.budget, data.status, data.maxParticipants,
                        data.pointType, data.pointValue);
                    JOptionPane.showMessageDialog(dialog, "Đã cập nhật sự kiện!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    eventService.createEvent(
                        data.name, data.description, data.startDate, data.endDate,
                        data.registrationDeadline, data.location, data.budget,
                        data.maxParticipants, currentUser.getMemberId(),
                        data.pointType, data.pointValue
                    );
                    JOptionPane.showMessageDialog(dialog, "Đã tạo sự kiện thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                }
                dialog.dispose();
                loadAllEvents();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(btnCancel);
        footer.add(btnSave);
        return footer;
    }

    /**
     * Xây dựng panel form nhập liệu sự kiện.
     * @param event null = form rỗng (ADD), non-null = form điền sẵn (EDIT)
     */
    private EventFormFields buildEventForm(EventDTO event) {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(new EmptyBorder(12, 16, 12, 16));
        form.setBackground(Color.WHITE);
        Font f = new Font("Segoe UI", Font.PLAIN, 13);

        EventFormFields fields = new EventFormFields();
        fields.panel = form;

        fields.tfName = createField(event != null ? event.getEventName() : "", f);
        fields.tfLocation = createField(event != null ? event.getLocation() : "", f);
        fields.tfBudget = createField(event != null && event.getBudget() != null ? event.getBudget().toPlainString() : "0", f);
        fields.tfMaxP = createField(event != null && event.getMaxParticipants() != null ? String.valueOf(event.getMaxParticipants()) : "100", f);

        fields.startDate = UiFormUtil.createDateChooser(event != null ? event.getStartDate() : null);
        fields.startTime = UiFormUtil.createTimeSpinner(event != null ? event.getStartDate() : null);
        fields.endDate = UiFormUtil.createDateChooser(event != null ? event.getEndDate() : null);
        fields.endTime = UiFormUtil.createTimeSpinner(event != null ? event.getEndDate() : null);
        fields.regDate = UiFormUtil.createDateChooser(event != null ? event.getRegistrationDeadline() : null);
        fields.regTime = UiFormUtil.createTimeSpinner(event != null ? event.getRegistrationDeadline() : null);

        fields.taDesc = new JTextArea(3, 20);
        fields.taDesc.setFont(f);
        fields.taDesc.setText(event != null ? event.getDescription() : "");
        fields.taDesc.setLineWrap(true);

        String[] statuses = {"Upcoming", "Ongoing", "Completed", "Cancelled"};
        fields.cbStatus = new JComboBox<>(statuses);
        fields.cbStatus.setFont(f);
        if (event != null) fields.cbStatus.setSelectedItem(event.getStatus());

        fields.cbPointType = new JComboBox<>(new String[]{"None", "DRL", "CTXH"});
        fields.cbPointType.setFont(f);
        String pointType = event != null ? event.getPointType() : null;
        fields.cbPointType.setSelectedItem(pointType != null ? pointType : "None");

        Integer pointValue = event != null ? event.getPointValue() : null;
        int defaultPointValue = pointValue != null ? pointValue : 0;
        fields.spPointValue = new JSpinner(new javax.swing.SpinnerNumberModel(defaultPointValue, 0, 200, 1));
        fields.spPointValue.setFont(f);

        form.add(makeLabel("Tên sự kiện *:")); form.add(fields.tfName);
        form.add(makeLabel("Bắt đầu *:")); form.add(UiFormUtil.buildDateTimePanel(fields.startDate, fields.startTime));
        form.add(makeLabel("Kết thúc *:")); form.add(UiFormUtil.buildDateTimePanel(fields.endDate, fields.endTime));
        form.add(makeLabel("Địa điểm:")); form.add(fields.tfLocation);
        form.add(makeLabel("Hạn đăng ký:")); form.add(UiFormUtil.buildDateTimePanel(fields.regDate, fields.regTime));
        form.add(makeLabel("Ngân sách (VNĐ):")); form.add(fields.tfBudget);
        form.add(makeLabel("Số lượng tối đa:")); form.add(fields.tfMaxP);
        form.add(makeLabel("Loại điểm:")); form.add(fields.cbPointType);
        form.add(makeLabel("Điểm áp dụng:")); form.add(fields.spPointValue);
        form.add(makeLabel("Trạng thái:")); form.add(fields.cbStatus);
        form.add(makeLabel("Mô tả:")); form.add(new JScrollPane(fields.taDesc));

        return fields;
    }

    /**
     * Đọc dữ liệu từ form panel và validate.
     */
    private EventFormData extractFormData(EventFormFields fields) {
        EventFormData data = new EventFormData();
        data.name = fields.tfName.getText().trim();
        data.startDate = UiFormUtil.toLocalDateTime(fields.startDate, fields.startTime);
        data.endDate = UiFormUtil.toLocalDateTime(fields.endDate, fields.endTime);
        data.registrationDeadline = UiFormUtil.toLocalDateTime(fields.regDate, fields.regTime);
        data.location = fields.tfLocation.getText().trim();
        data.status = (String) fields.cbStatus.getSelectedItem();
        data.description = fields.taDesc.getText().trim();
        data.pointType = (String) fields.cbPointType.getSelectedItem();
        data.pointValue = (Integer) fields.spPointValue.getValue();
        try { data.budget = new BigDecimal(fields.tfBudget.getText().trim()); } catch (NumberFormatException e) { data.budget = BigDecimal.ZERO; }
        data.maxParticipants = parseIntOrDefault(fields.tfMaxP.getText(), 100);

        if (data.name == null || data.name.isBlank())
            throw new IllegalArgumentException("Tên sự kiện không được để trống!");
        if (data.startDate == null || data.endDate == null)
            throw new IllegalArgumentException("Ngày bắt đầu và kết thúc không được để trống!");
        if (data.endDate.isBefore(data.startDate))
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu!");
        if (!"None".equalsIgnoreCase(data.pointType) && (data.pointValue == null || data.pointValue <= 0))
            throw new IllegalArgumentException("Điểm áp dụng phải lớn hơn 0!");
        return data;
    }

    /**
     * Tạo bộ chọn ngày.
     * @param dateTime Ngày giờ mặc định
     * @return JDateChooser
     */

    /**
     * Tạo TextField với giá trị mặc định.
     * @param value Giá trị ban đầu
     * @param f Font áp dụng
     * @return JTextField
     */
    private JTextField createField(String value, Font f) {
        JTextField tf = new JTextField(value);
        tf.setFont(f);
        return tf;
    }

    /**
     * Tạo label cho form nhập liệu.
     * @param text Nội dung
     * @return JLabel
     */
    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }

    /** Inner class để giữ dữ liệu form. */
    private static class EventFormData {
        String name, description, location, status = "Upcoming";
        LocalDateTime startDate, endDate;
        LocalDateTime registrationDeadline;
        BigDecimal budget = BigDecimal.ZERO;
        Integer maxParticipants = 100;
        String pointType = "None";
        Integer pointValue = 0;
    }

    private static class EventFormFields {
        JPanel panel;
        JTextField tfName;
        JTextField tfLocation;
        JTextField tfBudget;
        JTextField tfMaxP;
        JDateChooser startDate;
        JSpinner startTime;
        JDateChooser endDate;
        JSpinner endTime;
        JDateChooser regDate;
        JSpinner regTime;
        JTextArea taDesc;
        JComboBox<String> cbStatus;
        JComboBox<String> cbPointType;
        JSpinner spPointValue;
    }

    /**
     * Mở dialog chi tiết sự kiện từ dòng đang chọn.
     */
    private void handleViewDetail() {
        Integer id = view.getSelectedEventId();
        if (id == null) return;
        if (currentUser.isLeader() && adminDetailOpener != null) {
            adminDetailOpener.accept(id);
            return;
        }
        openDetailById(id, null);
    }

    /**
     * Mở dialog chi tiết sự kiện theo ID.
     * @param id ID sự kiện
     * @param afterClose Callback sau khi đóng (nullable)
     */
    public void openDetailById(Integer id, Runnable afterClose) {
        if (id == null) return;
        Optional<EventDTO> opt = eventService.getEventById(id);
        if (opt.isEmpty()) return;
        showDetailDialog(opt.get(), afterClose);
    }

    /**
     * Hiển thị dialog chi tiết sự kiện.
     * @param event Dữ liệu sự kiện
     * @param afterClose Callback sau khi đóng (nullable)
     */
    private void showDetailDialog(EventDTO event, Runnable afterClose) {
        JDialog dialog = new JDialog((Frame) null, "Chi tiết sự kiện", true);
        dialog.setSize(740, 560);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel(event.getEventName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        String metaText = "Người tạo: " + event.getCreatedByName();
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

        String startText = event.getStartDate() != null ? event.getStartDate().format(DT_FMT) : "";
        String endText = event.getEndDate() != null ? event.getEndDate().format(DT_FMT) : "";
        String regText = event.getRegistrationDeadline() != null ? event.getRegistrationDeadline().format(DT_FMT) : "Không giới hạn";
        String budgetText = event.getBudget() != null ? event.getBudget().toPlainString() + " VND" : "0 VND";
        String regCount = event.getRegisteredCount() + "/" + event.getMaxParticipants();

        content.add(UiFormUtil.makeInfoLabel("Thời gian: " + startText + " → " + endText));
        content.add(UiFormUtil.makeInfoLabel("Hạn đăng ký: " + regText));
        content.add(UiFormUtil.makeInfoLabel("Địa điểm: " + (event.getLocation() != null ? event.getLocation() : "")));
        content.add(UiFormUtil.makeInfoLabel("Ngân sách: " + budgetText));
        content.add(UiFormUtil.makeInfoLabel("Trạng thái: " + event.getStatus()));
        String pointText = (event.getPointType() == null || "None".equalsIgnoreCase(event.getPointType())
            || event.getPointValue() == null || event.getPointValue() <= 0)
            ? "Không áp dụng"
            : event.getPointType() + " +" + event.getPointValue();
        content.add(UiFormUtil.makeInfoLabel("Điểm sự kiện: " + pointText));
        content.add(UiFormUtil.makeInfoLabel("Đăng ký: " + regCount));
        content.add(Box.createVerticalStrut(8));

        JTextArea desc = new JTextArea(event.getDescription() != null ? event.getDescription() : "");
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        desc.setBorder(new EmptyBorder(8, 8, 8, 8));
        content.add(new JScrollPane(desc));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());
        footer.add(btnClose);

        boolean canRegister = true;
        if (event.getRegistrationDeadline() != null
            && LocalDateTime.now().isAfter(event.getRegistrationDeadline())) {
            canRegister = false;
        }
        if (event.getMaxParticipants() != null
            && event.getMaxParticipants() > 0
            && event.getRegisteredCount() >= event.getMaxParticipants()) {
            canRegister = false;
        }

        boolean alreadyRegistered = eventService.isMemberRegistered(event.getEventId(), currentUser.getMemberId());
        if (alreadyRegistered) {
            canRegister = false;
            JButton btnCancel = new JButton("Hủy đăng ký");
            btnCancel.setBackground(new Color(239, 68, 68));
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setBorderPainted(false);
            btnCancel.setFocusPainted(false);
            btnCancel.addActionListener(e -> handleCancelEvent(event, dialog));
            footer.add(btnCancel, 0);
        }

        if (canRegister) {
            JButton btnRegister = new JButton("Đăng ký tham gia");
            btnRegister.setBackground(new Color(16, 185, 129));
            btnRegister.setForeground(Color.WHITE);
            btnRegister.setBorderPainted(false);
            btnRegister.setFocusPainted(false);
            btnRegister.addActionListener(e -> {
                try {
                    eventService.registerForEvent(event.getEventId(), currentUser.getMemberId());
                    JOptionPane.showMessageDialog(dialog, "Đăng ký thành công!");
                    dialog.dispose();
                    loadAllEvents();
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
     * Hủy đăng ký sự kiện.
     * @param event Dữ liệu sự kiện
     * @param dialog Dialog đang hiển thị
     */
    private void handleCancelEvent(EventDTO event, JDialog dialog) {
        if (event.getRegistrationDeadline() != null
            && LocalDateTime.now().isAfter(event.getRegistrationDeadline())) {
            JOptionPane.showMessageDialog(dialog,
                "Đã hết hạn đăng ký, không thể hủy.",
                "Từ chối", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(dialog,
            "Bạn có chắc muốn hủy đăng ký sự kiện này?",
            "Xác nhận hủy", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                eventService.unregisterFromEvent(event.getEventId(), currentUser.getMemberId());
                JOptionPane.showMessageDialog(dialog, "Đã hủy đăng ký sự kiện!");
                dialog.dispose();
                loadAllEvents();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Parse số nguyên với giá trị mặc định.
     * @param text Chuỗi nhập
     * @param fallback Giá trị mặc định
     * @return Giá trị số nguyên
     */
    private int parseIntOrDefault(String text, int fallback) {
        if (text == null) return fallback;
        String t = text.trim();
        if (t.isEmpty()) return fallback;
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
