package org.uns.todolist.service;

import java.util.List;

import org.uns.todolist.models.Task;

public interface UiObserver {
    public void update(List<Task> task);
}
