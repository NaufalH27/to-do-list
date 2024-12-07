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
    public static List<Task> ByCompleted(List<Task> tasks) {
        return tasks.stream()
                .filter(Task::getIsCompleted) 
                .collect(Collectors.toList());
    }

    public static List<Task> ByIncomplete(List<Task> tasks) {
        return tasks.stream()
                .filter(task -> !task.getIsCompleted()) 
                .collect(Collectors.toList());
    }

    public static List<Task> ByToday(List<Task> tasks) {
        Date today = DayHelper.getDayToday();  
        return tasks.stream()
                .filter(task -> {
                    Date deadline = task.getDeadline();
                    return deadline != null && deadline.equals(today); 
                })
                .collect(Collectors.toList());
    }

    public static List<Task> ByPast(List<Task> tasks) {
        Date today = DayHelper.getDayToday();  
        return tasks.stream()
                .filter(task -> {
                    Date deadline = task.getDeadline();
                    return deadline != null && deadline.before(today); 
                })
                .collect(Collectors.toList());
    }

    public static List<Task> ByNoDeadline(List<Task> tasks) {
        return tasks.stream()
                .filter(task -> task.getDeadline() == null) 
                .collect(Collectors.toList());
    }

    public static List<Task> ByFuture(List<Task> tasks) {
        Date today = DayHelper.getDayToday();  
        return tasks.stream()
                .filter(task -> {
                    Date deadline = task.getDeadline();
                    return deadline != null && deadline.after(today); 
                })
                .collect(Collectors.toList());
    }
}
