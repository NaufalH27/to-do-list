package org.uns.todolist.service;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.uns.todolist.models.Task;

public class FilterMethod {

    public static List<Task> noFilter(List<Task> tasks) {
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

    public static List<Task> bySelectedCalendarDate(List<Task> tasks, LocalDate selectedDate) {
        return tasks.stream()
                .filter(task -> {
                    if (task.getDeadline() != null) {
                        LocalDate taskDeadline = task.getDeadline().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        return taskDeadline.equals(selectedDate);
                    }
                    return false; 
                })
                .collect(Collectors.toList());  
    }
}
