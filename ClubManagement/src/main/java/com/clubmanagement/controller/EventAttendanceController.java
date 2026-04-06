package com.clubmanagement.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.clubmanagement.dto.AttendanceDTO;
import com.clubmanagement.dto.EventAttendanceRowDTO;
import com.clubmanagement.dto.EventDTO;
import com.clubmanagement.dto.MemberDTO;
import com.clubmanagement.service.AttendanceService;
import com.clubmanagement.service.EventService;
import com.clubmanagement.view.DashboardView;
import com.clubmanagement.view.EventAttendanceView;

/**
 * EventAttendanceController - attendance management for a single event.
 */
public class EventAttendanceController {

    private final EventAttendanceView view;
    private final DashboardView dashboardView;
    private final MemberDTO currentUser;
    private final EventService eventService = new EventService();
    private final AttendanceService attendanceService = new AttendanceService();

    private Integer currentEventId;
    private List<EventAttendanceRowDTO> cachedRows = new ArrayList<>();
    private boolean suppressTableEvents;
    private Runnable onBack;

    public EventAttendanceController(EventAttendanceView view, DashboardView dashboardView, MemberDTO currentUser) {
        this.view = view;
        this.dashboardView = dashboardView;
        this.currentUser = currentUser;
        attachListeners();
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    private void attachListeners() {
        view.getBtnBack().addActionListener(e -> handleBack());
        view.getBtnRefresh().addActionListener(e -> reload());
        view.getBtnSearch().addActionListener(e -> applyFilter());
        view.getSearchField().addActionListener(e -> applyFilter());

        view.getTable().getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (suppressTableEvents) return;
                if (e.getType() != TableModelEvent.UPDATE) return;
                if (e.getColumn() != EventAttendanceView.COL_ATTENDED) return;
                int row = e.getFirstRow();
                handleToggleAttendance(row);
            }
        });
    }

    public void openForEvent(Integer eventId) {
        if (!currentUser.isLeader()) {
            JOptionPane.showMessageDialog(null, "Bạn không có quyền truy cập điểm danh!");
            return;
        }
        if (eventId == null) return;
        this.currentEventId = eventId;
        reload();
    }

    private void reload() {
        if (currentEventId == null) return;
        loadEventAttendance(currentEventId);
    }

    private void handleBack() {
        if (onBack != null) {
            onBack.run();
        } else {
            dashboardView.showEvents();
        }
    }

    private void loadEventAttendance(Integer eventId) {
        view.setStatusMessage("Đang tải dữ liệu...");
        SwingWorker<EventAttendanceData, Void> worker = new SwingWorker<>() {
            @Override
            protected EventAttendanceData doInBackground() {
                Optional<EventDTO> eventOpt = eventService.getEventById(eventId);
                List<MemberDTO> members = eventService.getRegisteredMembersForEvent(eventId);
                List<AttendanceDTO> attendances = attendanceService.getAttendancesByEvent(eventId);

                Map<Integer, AttendanceDTO> attendanceMap = new HashMap<>();
                for (AttendanceDTO attendance : attendances) {
                    if (attendance.getMemberId() != null) {
                        attendanceMap.put(attendance.getMemberId(), attendance);
                    }
                }

                List<EventAttendanceRowDTO> rows = new ArrayList<>();
                for (MemberDTO member : members) {
                    AttendanceDTO attendance = attendanceMap.get(member.getMemberId());
                    boolean attended = attendance != null && attendance.getStatus() != null
                        && !"Absent".equalsIgnoreCase(attendance.getStatus());
                    rows.add(new EventAttendanceRowDTO(
                        member.getMemberId(),
                        attendance != null ? attendance.getAttendanceId() : null,
                        member.getStudentId(),
                        member.getFullName(),
                        member.getEmail(),
                        attended
                    ));
                }

                return new EventAttendanceData(eventOpt.orElse(null), rows);
            }

            @Override
            protected void done() {
                try {
                    EventAttendanceData data = get();
                    if (data.event != null) {
                        view.setEventInfo(data.event.getEventName(), data.rows.size());
                        boolean editable = "Ongoing".equalsIgnoreCase(data.event.getStatus());
                        view.setAttendanceEditable(editable);
                        if (!editable) {
                            view.setStatusMessage("Chỉ sự kiện Ongoing mới được điểm danh.");
                        }
                    } else {
                        view.setEventInfo("Không tìm thấy sự kiện", 0);
                        view.setAttendanceEditable(false);
                    }
                    cachedRows = data.rows;
                    applyFilter();
                    if (data.event != null && "Ongoing".equalsIgnoreCase(data.event.getStatus())) {
                        view.setStatusMessage("Đã tải " + data.rows.size() + " sinh viên.");
                    }
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

    private void applyFilter() {
        String keyword = view.getSearchKeyword();
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";
        List<EventAttendanceRowDTO> filtered = new ArrayList<>();
        if (kw.isBlank()) {
            filtered.addAll(cachedRows);
        } else {
            for (EventAttendanceRowDTO row : cachedRows) {
                String name = row.getFullName() != null ? row.getFullName().toLowerCase() : "";
                String studentId = row.getStudentId() != null ? row.getStudentId().toLowerCase() : "";
                if (name.contains(kw) || studentId.contains(kw)) {
                    filtered.add(row);
                }
            }
        }
        suppressTableEvents = true;
        view.loadData(filtered);
        suppressTableEvents = false;
    }

    private void handleToggleAttendance(int rowIndex) {
        DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
        Object attendedObj = model.getValueAt(rowIndex, EventAttendanceView.COL_ATTENDED);
        boolean attended = attendedObj instanceof Boolean b && b;
        Integer memberId = (Integer) model.getValueAt(rowIndex, EventAttendanceView.COL_MEMBER_ID);
        Integer attendanceId = (Integer) model.getValueAt(rowIndex, EventAttendanceView.COL_ATTENDANCE_ID);
        String fullName = String.valueOf(model.getValueAt(rowIndex, EventAttendanceView.COL_FULL_NAME));

        try {
            AttendanceDTO updated = attendanceService.setAttendanceStatus(currentEventId, memberId, attended);
            Integer newAttendanceId = updated != null ? updated.getAttendanceId() : null;

            suppressTableEvents = true;
            model.setValueAt(newAttendanceId, rowIndex, EventAttendanceView.COL_ATTENDANCE_ID);
            suppressTableEvents = false;

            updateCachedRow(memberId, newAttendanceId, attended);
            view.setStatusMessage(attended ?
                "Đã điểm danh cho " + fullName :
                "Đã bỏ điểm danh cho " + fullName);
        } catch (RuntimeException ex) {
            suppressTableEvents = true;
            model.setValueAt(!attended, rowIndex, EventAttendanceView.COL_ATTENDED);
            if (attendanceId != null) {
                model.setValueAt(attendanceId, rowIndex, EventAttendanceView.COL_ATTENDANCE_ID);
            }
            suppressTableEvents = false;
            JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCachedRow(Integer memberId, Integer attendanceId, boolean attended) {
        for (EventAttendanceRowDTO row : cachedRows) {
            if (row.getMemberId() != null && row.getMemberId().equals(memberId)) {
                row.setAttendanceId(attendanceId);
                row.setAttended(attended);
                break;
            }
        }
    }

    private static class EventAttendanceData {
        private final EventDTO event;
        private final List<EventAttendanceRowDTO> rows;

        private EventAttendanceData(EventDTO event, List<EventAttendanceRowDTO> rows) {
            this.event = event;
            this.rows = rows;
        }
    }
}
