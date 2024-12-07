package org.uns.todolist.service;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.uns.todolist.helper.DayHelper;
import org.uns.todolist.models.Task;

public class FilterMethod {

    public static List<Task> defaultMethod(List<Task> tasks) {
        return tasks;
    }
    // Filter tasks by completed status
    public static List<Task> ByCompleted(List<Task> tasks) {
        return tasks.stream()
                .filter(Task::getIsCompleted) // Only tasks marked as completed
                .collect(Collectors.toList());
    }

    // Filter tasks by incomplete status
    public static List<Task> ByIncomplete(List<Task> tasks) {
        return tasks.stream()
                .filter(task -> !task.getIsCompleted()) // Only tasks not marked as completed
                .collect(Collectors.toList());
    }

    // Filter tasks due today (ignore time, compare only dates)
    public static List<Task> ByToday(List<Task> tasks) {
        Date today = DayHelper.getDayToday();  // Get today's date with time set to midnight
        return tasks.stream()
                .filter(task -> {
                    Date deadline = task.getDeadline();
                    return deadline != null && deadline.equals(today); // Compare dates only
                })
                .collect(Collectors.toList());
    }

    // Filter tasks with past deadlines
    public static List<Task> ByPast(List<Task> tasks) {
        Date today = DayHelper.getDayToday();  // Get today's date with time set to midnight
        return tasks.stream()
                .filter(task -> {
                    Date deadline = task.getDeadline();
                    return deadline != null && deadline.before(today); // Task deadline before today
                })
                .collect(Collectors.toList());
    }

    // Filter tasks with no deadline (null deadline)
    public static List<Task> ByNoDeadline(List<Task> tasks) {
        return tasks.stream()
                .filter(task -> task.getDeadline() == null) // Only tasks with no deadline
                .collect(Collectors.toList());
    }

    // Filter tasks with future deadlines
    public static List<Task> ByFuture(List<Task> tasks) {
        Date today = DayHelper.getDayToday();  // Get today's date with time set to midnight
        return tasks.stream()
                .filter(task -> {
                    Date deadline = task.getDeadline();
                    return deadline != null && deadline.after(today); // Task deadline after today
                })
                .collect(Collectors.toList());
    }
}
