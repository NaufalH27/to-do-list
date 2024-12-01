package org.uns.todolist.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.uns.todolist.models.SaveData;
import org.uns.todolist.models.Task;


public class DataManager {
    private final SaveData data;
    private final DataPersistence persistence;

    public DataManager(SaveData data, DataPersistence persistence) {
        this.data = data;
        this.persistence = persistence;
    }

    public void addTask(String namaTask, Date deadline) throws IOException {
        if (namaTask == null || namaTask.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama Aktivitas Tidak Boleh Kosong");
        }

        //jika nama valid : buat task baru dengan nama tersebut dan tambahkan ke save data
        Task newTask = new Task(data.getCurrentTaskId(), namaTask, deadline);
        this.data.addTask(newTask);
        
        //save data
        this.persistence.save(this.data);
    }

    public void removeTask(int taskId)  throws IOException {
        Task taskToRemove = this.getTaskById(taskId);
        this.data.removeTask(taskToRemove);
        
        //save data
        this.data.incrementTaskId();
        this.persistence.save(this.data);
    }

    public void completeTask(int taskId) {
        Task taskToComplete = this.getTaskById(taskId);
        taskToComplete.completeTask();
    }

    //menmgebalikan semua task
    public List<Task> getAllTasks() {
        return new ArrayList<>(this.data.getTasks());
    }

    //mengembalikan semua task yang sudah di tandakan selesai (isComplete = true)
    public List<Task> getCompletedTasks() {
        return this.data.getTasks().stream()
                .filter(Task::getIsCompleted)
                .collect(Collectors.toList()); 
    }

    //mengembalikan semua task yang belum selesai (isComplete = false)
    public List<Task> getIncompleteTasks() {
        return this.data.getTasks().stream()
                .filter(task -> !task.getIsCompleted())
                .collect(Collectors.toList()); 
    }

    //mengembalikan satu task sesuai index
    private Task getTaskById(int taskId) {
        List<Task> tasks = this.data.getTasks();

        if (tasks == null) {
            throw new IllegalArgumentException("internal error");
        }
    
        if (tasks.isEmpty()) {
            throw new IllegalArgumentException("Aktivitas Kosong");
        }

        Task task = this.data.getTasks().stream()
                    .filter(t -> t.getTaskId() == taskId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Aktivitas tidak ditemukan"));
                    
        return task;
    }
}
