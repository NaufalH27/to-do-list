package org.uns.todolist.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.uns.todolist.helper.DayHelper;
import org.uns.todolist.models.Task;

public class SortingMethod {

    public static List<Task> defaultMethod(List<Task> tasks) {
        Date today = DayHelper.getDayToday();
        Map<DeadlineStatus, List<Task>> groupedByDeadlineStatus = tasks.stream()
            .collect(Collectors.groupingBy(task -> {
                if (task.getDeadline() == null) {
                    return DeadlineStatus.NO_DEADLINE;
                } else if (task.getDeadline().before(today)) {
                    return DeadlineStatus.BEFORE_TODAY;
                } else {
                    return DeadlineStatus.AFTER_TODAY;
                }
            }));

        groupedByDeadlineStatus.forEach((deadlineStatus, taskList) -> { 
            taskList.sort(Comparator
                .comparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Comparator.comparing(Task::getTaskId).reversed()));
        });

        List<DeadlineStatus> orderedStatuses = Arrays.asList(
            DeadlineStatus.AFTER_TODAY,
            DeadlineStatus.NO_DEADLINE,
            DeadlineStatus.BEFORE_TODAY
        );

        List<Task> orderedTasks = new ArrayList<>();
        for (DeadlineStatus status : orderedStatuses) {
            List<Task> groupedTasks = groupedByDeadlineStatus.getOrDefault(status, Collections.emptyList());
            orderedTasks.addAll(groupedTasks); 
        }

        orderedTasks.sort(Comparator.comparing(Task::getIsCompleted) 
                                    .thenComparing(
                                        task -> task.getIsCompleted() ? task.getCompletedDate() : null, 
                                            Comparator.nullsLast(Comparator.reverseOrder())));

        return orderedTasks;
    }
    
    public static List<Task> byRecentlyCreated(List<Task> tasks) {
        return tasks.stream()
                .sorted(Comparator.comparing(Task::getTaskId).reversed())
                .toList();
    }

    public static List<Task> byOldestCreated(List<Task> tasks) {
        return tasks.stream()
                .sorted(Comparator.comparing(Task::getTaskId))
                .toList();
    }

    public static List<Task> byName(List<Task> tasks) {
        return tasks.stream()
                    .sorted(Comparator.comparing(Task::getNamaTask, String.CASE_INSENSITIVE_ORDER))
                    .toList();
    }
}
