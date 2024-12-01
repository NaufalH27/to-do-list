package org.uns.todolist.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

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
        List<Task> tasks = this.state.getTasks();
        tasks.add(newTask);
        this.state.setTasks(tasks);
        
        //memperbarui status terakhir kali state di update
        this.state.setLastUpdate(new Date());

        //save current state
        SaveStateFileHelper.save(this.state);
    }


    public void removeTask(int taskIndex)  throws IOException {
        List<Task> tasks = this.state.getTasks();

        if (tasks == null) {
            throw new IllegalStateException("Illegal Null Task List");
        }
    

        if (tasks.isEmpty()) {
            throw new IllegalStateException("Tidak ada Aktivitas yang di hapus");
        }


        if(taskIndex >= tasks.size() || taskIndex < 0) {
            throw new IllegalArgumentException("Illegal input");
        }

        //jika index valid maka hapus task dari list
        tasks.remove(taskIndex);
        this.state.setTasks(tasks);
        
        //memperbarui status terakhir kali state di update
        this.state.setLastUpdate(new Date());

        //save current state
        SaveStateFileHelper.save(this.state);
    }

}
