package com.clubmanagement.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.clubmanagement.dao.TaskDAO;
import com.clubmanagement.dto.TaskDTO;
import com.clubmanagement.entity.Event;
import com.clubmanagement.entity.Member;
import com.clubmanagement.entity.Task;
import com.clubmanagement.util.HibernateUtil;

/**
 * TaskService - Tầng nghiệp vụ cho Nhiệm vụ.
 */
public class TaskService {

    private final TaskDAO taskDAO = new TaskDAO();

    /**
     * Tạo nhiệm vụ mới.
     * @param title Tiêu đề
     * @param description Mô tả
     * @param deadline Hạn hoàn thành
     * @param priority Mức độ ưu tiên
     * @param visibility Công khai hay riêng tư
     * @param maxAssignees Số người tối đa
     * @param assignerId ID người giao
     * @param eventId ID sự kiện liên quan (nullable)
     * @param assigneeIds Danh sách ID người thực hiện
     * @return TaskDTO đã lưu
     */
    public TaskDTO createTask(String title, String description, LocalDateTime deadline,
                              String priority, String visibility, Integer maxAssignees,
                              Integer assignerId, Integer eventId,
                              List<Integer> assigneeIds) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Tiêu đề không được để trống!");
        }

        try (Session session = HibernateUtil.openSession()) {
            Member assigner = assignerId != null ? session.get(Member.class, assignerId) : null;
            Event event = eventId != null ? session.get(Event.class, eventId) : null;

            Task task = new Task(title.trim(), description, deadline, priority, assigner);
            task.setEvent(event);
            task.setVisibility(visibility != null ? visibility : "Public");
            task.setMaxAssignees(maxAssignees != null ? maxAssignees : 1);
            Task saved = taskDAO.save(task);
            if (assigneeIds != null && !assigneeIds.isEmpty()) {
                taskDAO.replaceAssignees(saved.getTaskId(), assigneeIds);
                saved = taskDAO.findById(saved.getTaskId()).orElse(saved);
            }
            return toDTO(saved);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo task: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy tất cả nhiệm vụ.
     * @return Danh sách TaskDTO
     */
    public List<TaskDTO> getAllTasks() {
        return taskDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Lấy nhiệm vụ của thành viên.
     * @param memberId ID thành viên
     * @return Danh sách TaskDTO
     */
    public List<TaskDTO> getTasksForUser(Integer memberId) {
        return taskDAO.findByMember(memberId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Lấy nhiệm vụ public chưa có người nhận.
     * @return Danh sách TaskDTO
     */
    public List<TaskDTO> getPublicUnassignedTasks() {
        return taskDAO.findPublicUnassigned().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Lấy nhiệm vụ public đã có người nhận.
     * @return Danh sách TaskDTO
     */
    public List<TaskDTO> getPublicAssignedTasks() {
        return taskDAO.findPublicAssigned().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Lấy nhiệm vụ đã nhận của thành viên.
     * @param memberId ID thành viên
     * @return Danh sách TaskDTO
     */
    public List<TaskDTO> getAssignedTasksForUser(Integer memberId) {
        return taskDAO.findAssignedToMember(memberId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Lấy nhiệm vụ có thể xem của thành viên (public + đã nhận).
     * @param memberId ID thành viên
     * @return Danh sách TaskDTO
     */
    public List<TaskDTO> getVisibleTasksForUser(Integer memberId) {
        List<TaskDTO> result = new java.util.ArrayList<>();
        result.addAll(getPublicUnassignedTasks());
        result.addAll(getPublicAssignedTasks());
        result.addAll(getAssignedTasksForUser(memberId));
        return result.stream()
            .collect(Collectors.toMap(TaskDTO::getTaskId, t -> t, (a, b) -> a))
            .values()
            .stream()
            .sorted(java.util.Comparator.comparing(TaskDTO::getCreatedDate, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Lấy nhiệm vụ theo ID và kiểm tra quyền xem.
     * @param taskId ID nhiệm vụ
     * @param memberId ID thành viên hiện tại
     * @param isLeader true nếu user có quyền quản lý
     * @return Optional<TaskDTO> nếu có quyền xem
     */
    public Optional<TaskDTO> getTaskByIdForUser(Integer taskId, Integer memberId, boolean isLeader) {
        if (taskId == null) return Optional.empty();
        Optional<Task> opt = taskDAO.findById(taskId);
        if (opt.isEmpty()) return Optional.empty();

        Task task = opt.get();
        if (isLeader) return Optional.ofNullable(toDTO(task));

        boolean isPublic = "Public".equalsIgnoreCase(task.getVisibility());
        boolean isAssignee = memberId != null
            && task.getAssignees() != null
            && task.getAssignees().stream().anyMatch(m -> memberId.equals(m.getMemberId()));
        if (isPublic || isAssignee) {
            return Optional.ofNullable(toDTO(task));
        }
        return Optional.empty();
    }

    /**
     * Cập nhật nhiệm vụ.
     * @param taskId ID nhiệm vụ
     * @param title Tiêu đề
     * @param description Mô tả
     * @param deadline Hạn hoàn thành
     * @param priority Mức độ ưu tiên
     * @param status Trạng thái
     * @param visibility Công khai hay riêng tư
     * @param maxAssignees Số người tối đa
     * @param eventId ID sự kiện liên quan (nullable)
     * @param assigneeIds Danh sách ID người thực hiện
     * @return TaskDTO đã cập nhật
     */
    public TaskDTO updateTask(Integer taskId, String title, String description, LocalDateTime deadline,
                              String priority, String status, String visibility, Integer maxAssignees,
                              Integer eventId, List<Integer> assigneeIds) {
        Task task = taskDAO.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin Nhiệm vụ!"));

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Tiêu đề không được để trống!");
        }

        try (Session session = HibernateUtil.openSession()) {
            Event event = eventId != null ? session.get(Event.class, eventId) : null;

            task.setTitle(title.trim());
            task.setDescription(description);
            task.setDeadline(deadline);
            task.setPriority(priority);
            task.setStatus(status);
            task.setVisibility(visibility != null ? visibility : task.getVisibility());
            task.setMaxAssignees(maxAssignees != null ? maxAssignees : task.getMaxAssignees());
            task.setEvent(event);

            Task updated = taskDAO.update(task);
            if (assigneeIds != null) {
                taskDAO.replaceAssignees(taskId, assigneeIds);
                updated = taskDAO.findById(taskId).orElse(updated);
            }
            return toDTO(updated);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật task: " + e.getMessage(), e);
        }
    }

    /**
     * Đăng ký tham gia nhiệm vụ (chỉ public).
     * @param taskId ID nhiệm vụ
     * @param memberId ID thành viên
     */
    public void registerForTask(Integer taskId, Integer memberId) {
        Task task = taskDAO.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhiệm vụ"));
        if (!"Public".equalsIgnoreCase(task.getVisibility())) {
            throw new IllegalStateException("Nhiệm vụ này là private");
        }
        taskDAO.addMemberToTask(taskId, memberId);
    }

    /**
     * Hủy đăng ký nhiệm vụ.
     * @param taskId ID nhiệm vụ
     * @param memberId ID thành viên
     */
    public void unregisterFromTask(Integer taskId, Integer memberId) {
        Task task = taskDAO.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhiệm vụ"));
        if (task.getAssignees() == null || task.getAssignees().stream()
            .noneMatch(m -> m.getMemberId().equals(memberId))) {
            throw new IllegalStateException("Bạn chưa đăng ký nhiệm vụ này");
        }
        taskDAO.removeMemberFromTask(taskId, memberId);
    }

    /**
     * Xóa nhiệm vụ theo ID.
     * @param taskId ID nhiệm vụ
     */
    public void deleteTask(Integer taskId) {
        if (!taskDAO.deleteById(taskId)) {
            throw new IllegalArgumentException("Không thể tìm thấy Nhiệm vụ để xóa!");
        }
    }

    /**
     * Map Task entity -> TaskDTO.
     * @param t Task entity
     * @return TaskDTO
     */
    private TaskDTO toDTO(Task t) {
        if (t == null) return null;
        List<Member> assignees = t.getAssignees() != null ? t.getAssignees() : new ArrayList<>();
        List<Member> sortedAssignees = assignees.stream()
            .sorted(Comparator.comparing(Member::getFullName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
        List<Integer> assigneeIds = sortedAssignees.stream().map(Member::getMemberId).collect(Collectors.toList());
        List<String> assigneeNames = sortedAssignees.stream().map(Member::getFullName).collect(Collectors.toList());
        String assigneeName = assigneeNames.isEmpty() ? "Chưa giao" : String.join(", ", assigneeNames);

        return new TaskDTO(
            t.getTaskId(),
            t.getTitle(),
            t.getDescription(),
            t.getDeadline(),
            t.getPriority(),
            t.getStatus(),
            t.getCreatedDate(),
            t.getVisibility(),
            t.getMaxAssignees(),
            assigneeName,
            t.getAssigner() != null ? t.getAssigner().getFullName() : "Hệ thống",
            t.getEvent() != null ? t.getEvent().getEventName() : "Không có",
            assigneeIds.isEmpty() ? null : assigneeIds.get(0),
            t.getAssigner() != null ? t.getAssigner().getMemberId() : null,
            t.getEvent() != null ? t.getEvent().getEventId() : null,
            assigneeIds,
            assigneeNames
        );
    }
}
