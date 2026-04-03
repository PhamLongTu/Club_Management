package com.clubmanagement.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.clubmanagement.dao.TaskDAO;
import com.clubmanagement.dto.TaskDTO;
import com.clubmanagement.entity.Event;
import com.clubmanagement.entity.Member;
import com.clubmanagement.entity.Task;
import com.clubmanagement.util.HibernateUtil;

public class TaskService {

    private final TaskDAO taskDAO = new TaskDAO();

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

    public List<TaskDTO> getAllTasks() {
        return taskDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<TaskDTO> getTasksForUser(Integer memberId) {
        return taskDAO.findByMember(memberId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<TaskDTO> getPublicUnassignedTasks() {
        return taskDAO.findPublicUnassigned().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<TaskDTO> getPublicAssignedTasks() {
        return taskDAO.findPublicAssigned().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<TaskDTO> getAssignedTasksForUser(Integer memberId) {
        return taskDAO.findAssignedToMember(memberId).stream().map(this::toDTO).collect(Collectors.toList());
    }

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

    public void registerForTask(Integer taskId, Integer memberId) {
        Task task = taskDAO.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhiệm vụ"));
        if (!"Public".equalsIgnoreCase(task.getVisibility())) {
            throw new IllegalStateException("Nhiệm vụ này là private");
        }
        taskDAO.addMemberToTask(taskId, memberId);
    }

    public void deleteTask(Integer taskId) {
        if (!taskDAO.deleteById(taskId)) {
            throw new IllegalArgumentException("Không thể tìm thấy Nhiệm vụ để xóa!");
        }
    }

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
