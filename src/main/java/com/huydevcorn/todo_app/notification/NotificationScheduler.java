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

/**
* Component for scheduling and managing task notifications.
*/
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

    /**
     * Cancels all scheduled notifications for a given task.
     *
     * @param taskId the ID of the task
     */
    public void cancelTask(Long taskId) {
        // Retrieve the list of scheduled futures for the task
        List<ScheduledFuture<?>> futures = scheduledTasks.get(taskId);

        if (futures != null) {
            // Cancel each scheduled future
            for (ScheduledFuture<?> future : futures) {
                future.cancel(false);
            }

            // Remove the task from the scheduled tasks map
            scheduledTasks.remove(taskId);
            log.info("Cancelled all schedules for task {}", taskId);
        }
    }

    /**
     * Schedules notifications for a task based on its due date.
     *
     * @param taskId the ID of the task
     * @param title the title of the task
     * @param dueDate the due date of the task
     */
    public void scheduleTask(Long taskId, String title, LocalDateTime dueDate) {
        LocalDateTime now = LocalDateTime.now();
        Duration timeUntilDue = Duration.between(now, dueDate);

        // If the task is already overdue, send an overdue notification immediately
        if (timeUntilDue.isNegative()) {
            log.warn("Task {} is already overdue, sending overdue notification now.", taskId);
            sendNotification("/notification/overdue-tasks", "Task Overdue",
                    "Your task is overdue: " + title + " at " + dueDate.format(dateTimeFormatter));
            markTaskAsOverdue(taskId);
            return;
        }

        List<ScheduledFuture<?>> futures = new ArrayList<>();

        // Schedule a reminder notification 1 hour before the due date
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

        // Schedule an overdue notification at the due date
        futures.add(scheduleNotification(
                taskId,
                "/notification/overdue-tasks",
                "Task Overdue",
                "Your task is overdue: " + title + " at " + dueDate.format(dateTimeFormatter),
                timeUntilDue,
                true
        ));

        // Store the scheduled futures in the map
        scheduledTasks.put(taskId, futures);
        log.info("Scheduled {} notifications for task {}", futures.size(), taskId);
    }

    /**
     * Schedules a notification to be sent after a specified delay.
     *
     * @param taskId the ID of the task
     * @param destination the destination of the notification
     * @param title the title of the notification
     * @param message the message of the notification
     * @param delay the delay before sending the notification
     * @param isOverdue whether the notification is for an overdue task
     * @return the scheduled future representing the scheduled notification
     */
    private ScheduledFuture<?> scheduleNotification(Long taskId, String destination, String title, String message, Duration delay, boolean isOverdue) {
        if (isOverdue) {
            // Schedule an overdue notification and mark the task as overdue
            return scheduledExecutorService.schedule(() -> {
                sendNotification(destination, title, message);
                markTaskAsOverdue(taskId);
                cleanupTask(taskId);
            }, delay.toMillis(), TimeUnit.MILLISECONDS);
        }

        // Schedule a regular notification
        return scheduledExecutorService.schedule(() -> {
            sendNotification(destination, title, message);
            cleanupTask(taskId);
        }, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Sends a notification to a specified destination.
     *
     * @param destination the destination of the notification
     * @param title the title of the notification
     * @param message the message of the notification
     */
    private void sendNotification(String destination, String title, String message) {
        // Send the notification using the messaging template
        simpMessagingTemplate.convertAndSend(destination, NotificationResponse.builder()
                .title(title)
                .message(message)
                .build());
    }

    /**
     * Cleans up completed tasks from the scheduled tasks map.
     *
     * @param taskId the ID of the task to clean up
     */
    private void cleanupTask(Long taskId) {
        // Retrieve the list of scheduled futures for the task
        List<ScheduledFuture<?>> futures = scheduledTasks.get(taskId);

        if (futures != null) {
            // Remove completed futures from the list
            futures.removeIf(ScheduledFuture::isDone);

            // If no futures remain, remove the task from the map
            if (futures.isEmpty()) scheduledTasks.remove(taskId);
        }
    }

    /**
     * Marks a task as overdue in the repository.
     *
     * @param taskId the ID of the task to mark as overdue
     */
    private void markTaskAsOverdue(Long taskId) {
        // Retrieve the task from the repository
        Optional<Task> optionalTask = taskRepository.findById(taskId);

        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();

            // Update the task status to overdue
            task.setStatus(TaskStatus.OVERDUE);
            taskRepository.save(task);
            log.info("Task {} is now overdue.", taskId);
        }
    }

}
