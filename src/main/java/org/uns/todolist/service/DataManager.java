package org.uns.todolist.service;

import java.io.IOException;
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
        Task newTask = new Task(namaTask, deadline);
        this.data.addTask(newTask);
        
        //save data
        this.persistence.save(this.data);
    }


    public void removeTask(int taskIndex)  throws IOException {
        List<Task> tasks = this.data.getTasks();

        if (tasks == null) {
            throw new IllegalArgumentException("internal error");
        }
    
        if (tasks.isEmpty()) {
            throw new IllegalArgumentException("Tidak ada Aktivitas yang di hapus");
        }

        if(taskIndex >= tasks.size() || taskIndex < 0) {
            throw new IllegalArgumentException("Illegal input");
        }

        //jika index valid maka hapus task dari list
        Task taskToRemove = this.data.getTasks().get(taskIndex);
        this.data.removeTask(taskToRemove);
        
        //save data
        this.persistence.save(this.data);
    }

    //menmgebalikan semua task
    public List<Task> getAllTasks() {
        return this.data.getTasks();
    }

    //mengembalikan semua task yang sudah di tandakan selesai (isComplete = true)
    public List<Task> getCompletedTask() {
        return this.data.getTasks().stream()
                .filter(Task::getIsCompleted)
                .collect(Collectors.toList()); 
    }

    //mengembalikan semua task yang belum selesai (isComplete = false)
    public List<Task> getIncompleteTask() {
        return this.data.getTasks().stream()
                .filter(task -> !task.getIsCompleted())
                .collect(Collectors.toList()); 
    }
}
