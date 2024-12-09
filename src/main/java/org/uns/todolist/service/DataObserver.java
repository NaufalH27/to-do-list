package org.uns.todolist.service;

import java.util.List;

import org.uns.todolist.models.Task;

public interface DataObserver {
    public void updateData(List<Task> tasks);
}
