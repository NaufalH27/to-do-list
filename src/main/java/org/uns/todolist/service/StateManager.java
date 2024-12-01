package org.uns.todolist.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.uns.todolist.helper.SaveStateFileHelper;
import org.uns.todolist.models.SaveState;
import org.uns.todolist.models.Task;


public class StateManager {
    private final SaveState state;

    public StateManager(SaveState state) {
        this.state = state;
    }

    public void addTask(String namaTask, Date deadline) throws IOException {
        if (namaTask == null || namaTask.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama Aktivitas Tidak Boleh Kosong");
        }

        //jika nama valid : buat task baru dengan nama tersebut dan tambahkan ke save state
        Task newTask = new Task(namaTask, deadline);
        this.state.addTask(newTask);
        
        //save dan memperbarui status terakhir kali state di update
        this.state.setLastUpdate(new Date());
        SaveStateFileHelper.save(this.state);
    }


    public void removeTask(int taskIndex)  throws IOException {
        List<Task> tasks = this.state.getTasks();

        if (tasks == null) {
            throw new IllegalStateException("internal error");
        }
    
        if (tasks.isEmpty()) {
            throw new IllegalStateException("Tidak ada Aktivitas yang di hapus");
        }

        if(taskIndex >= tasks.size() || taskIndex < 0) {
            throw new IllegalArgumentException("Illegal input");
        }

        //jika index valid maka hapus task dari list
        Task taskToRemove = this.state.getTasks().get(taskIndex);
        this.state.removeTask(taskToRemove);
        
        //save dan memperbarui status terakhir kali state di update
        this.state.setLastUpdate(new Date());
        SaveStateFileHelper.save(this.state);
    }

    //menmgebalikan semua task
    public List<Task> getAllTasks() {
        return this.state.getTasks();
    }

    //mengembalikan semua task yang sudah di tandakan selesai (isComplete = true)
    public List<Task> getCompletedTask() {
        return this.state.getTasks().stream()
                .filter(Task::getIsCompleted)
                .collect(Collectors.toList()); 
    }

    //mengembalikan semua task yang belum selesai (isComplete = false)
    public List<Task> getIncompleteTask() {
        return this.state.getTasks().stream()
                .filter(task -> !task.getIsCompleted())
                .collect(Collectors.toList()); 
    }
}
