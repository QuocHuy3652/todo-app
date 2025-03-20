package com.huydevcorn.todo_app.notification;

import com.huydevcorn.todo_app.dto.response.NotificationResponse;
import com.huydevcorn.todo_app.entity.Task;
import com.huydevcorn.todo_app.enums.TaskStatus;
import com.huydevcorn.todo_app.repository.TaskRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationScheduler {
    ScheduledExecutorService scheduledExecutorService;
    SimpMessagingTemplate simpMessagingTemplate;
    Map<Long, List<ScheduledFuture<?>>> scheduledTasks = new ConcurrentHashMap<>();
    DateTimeFormatter dateTimeFormatter;
    TaskRepository taskRepository;

    public void cancelTask(Long taskId) {
        List<ScheduledFuture<?>> futures = scheduledTasks.get(taskId);
        if (futures != null) {
            for (ScheduledFuture<?> future : futures) {
                future.cancel(false);
            }
            scheduledTasks.remove(taskId);
            log.info("Cancelled all schedules for task {}", taskId);
        }
    }

    public void scheduleTask(Long taskId, String title, LocalDateTime dueDate) {
        LocalDateTime now = LocalDateTime.now();
        Duration timeUntilDue = Duration.between(now, dueDate);

        if (timeUntilDue.isNegative()) {
            log.warn("Task {} is already overdue, sending overdue notification now.", taskId);
            sendNotification("/notification/overdue-tasks", "Task Overdue",
                    "Your task is overdue: " + title + " at " + dueDate.format(dateTimeFormatter));
            markTaskAsOverdue(taskId);
            return;
        }

        List<ScheduledFuture<?>> futures = new ArrayList<>();

        Duration reminderDelay = timeUntilDue.minus(Duration.ofHours(1));
        if (!reminderDelay.isNegative()) {
            futures.add(scheduleNotification(
                    taskId,
                    "/notification/upcoming-tasks",
                    "Task Reminder",
                    "You have an upcoming task: " + title + " at " + dueDate.format(dateTimeFormatter),
                    reminderDelay,
                    false
            ));
        }

        futures.add(scheduleNotification(
                taskId,
                "/notification/overdue-tasks",
                "Task Overdue",
                "Your task is overdue: " + title + " at " + dueDate.format(dateTimeFormatter),
                timeUntilDue,
                true
        ));

        scheduledTasks.put(taskId, futures);
        log.info("Scheduled {} notifications for task {}", futures.size(), taskId);
    }

    private ScheduledFuture<?> scheduleNotification(Long taskId, String destination, String title, String message, Duration delay, boolean isOverdue) {
        if (isOverdue) {
            return scheduledExecutorService.schedule(() -> {
                sendNotification(destination, title, message);
                markTaskAsOverdue(taskId);
                cleanupTask(taskId);
            }, delay.toMillis(), TimeUnit.MILLISECONDS);
        }
        return scheduledExecutorService.schedule(() -> {
            sendNotification(destination, title, message);
            cleanupTask(taskId);
        }, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void sendNotification(String destination, String title, String message) {
        simpMessagingTemplate.convertAndSend(destination, NotificationResponse.builder()
                .title(title)
                .message(message)
                .build());
    }

    private void cleanupTask(Long taskId) {
        List<ScheduledFuture<?>> futures = scheduledTasks.get(taskId);
        if (futures != null) {
            futures.removeIf(ScheduledFuture::isDone);
            if (futures.isEmpty()) scheduledTasks.remove(taskId);
        }
    }

    private void markTaskAsOverdue(Long taskId) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();
            task.setStatus(TaskStatus.OVERDUE);
            taskRepository.save(task);
            log.info("Task {} is now overdue.", taskId);
        }
    }

}
