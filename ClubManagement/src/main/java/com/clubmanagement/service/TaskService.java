package com.clubmanagement.service;

import com.clubmanagement.dao.TaskDAO;
import com.clubmanagement.dto.TaskDTO;
import com.clubmanagement.entity.Event;
import com.clubmanagement.entity.Member;
import com.clubmanagement.entity.Task;
import com.clubmanagement.util.HibernateUtil;
import org.hibernate.Session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TaskService {

    private final TaskDAO taskDAO = new TaskDAO();

    public TaskDTO createTask(String title, String description, LocalDateTime deadline,
                              String priority, Integer assigneeId, Integer assignerId, Integer eventId) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Tiêu đề không được để trống!");
        }

        try (Session session = HibernateUtil.openSession()) {
            Member assignee = assigneeId != null ? session.get(Member.class, assigneeId) : null;
            Member assigner = assignerId != null ? session.get(Member.class, assignerId) : null;
            Event event = eventId != null ? session.get(Event.class, eventId) : null;

            Task task = new Task(title.trim(), description, deadline, priority, assignee, assigner);
            task.setEvent(event);
            return toDTO(taskDAO.save(task));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo task: " + e.getMessage(), e);
        }
    }

    public List<TaskDTO> getAllTasks() {
        return taskDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public TaskDTO updateTask(Integer taskId, String title, String description, LocalDateTime deadline,
                              String priority, String status, Integer assigneeId, Integer eventId) {
        Task task = taskDAO.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin Nhiệm vụ!"));

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Tiêu đề không được để trống!");
        }

        try (Session session = HibernateUtil.openSession()) {
            Member assignee = assigneeId != null ? session.get(Member.class, assigneeId) : null;
            Event event = eventId != null ? session.get(Event.class, eventId) : null;

            task.setTitle(title.trim());
            task.setDescription(description);
            task.setDeadline(deadline);
            task.setPriority(priority);
            task.setStatus(status);
            task.setAssignee(assignee);
            task.setEvent(event);

            return toDTO(taskDAO.update(task));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật task: " + e.getMessage(), e);
        }
    }

    public void deleteTask(Integer taskId) {
        if (!taskDAO.deleteById(taskId)) {
            throw new IllegalArgumentException("Không thể tìm thấy Nhiệm vụ để xóa!");
        }
    }

    private TaskDTO toDTO(Task t) {
        if (t == null) return null;
        return new TaskDTO(
            t.getTaskId(),
            t.getTitle(),
            t.getDescription(),
            t.getDeadline(),
            t.getPriority(),
            t.getStatus(),
            t.getCreatedDate(),
            t.getAssignee() != null ? t.getAssignee().getFullName() : "Chưa giao",
            t.getAssigner() != null ? t.getAssigner().getFullName() : "Hệ thống",
            t.getEvent() != null ? t.getEvent().getEventName() : "Không có",
            t.getAssignee() != null ? t.getAssignee().getMemberId() : null,
            t.getAssigner() != null ? t.getAssigner().getMemberId() : null,
            t.getEvent() != null ? t.getEvent().getEventId() : null
        );
    }
}
