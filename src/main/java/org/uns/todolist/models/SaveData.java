package org.uns.todolist.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//objek untuk menyimpan dan load progress aplikasi  
public class SaveData {

    private final List<Task> tasks;
    private Date lastUpdate;

    
    public SaveData(List<Task> tasks) {
        this.tasks = tasks;
        lastUpdate = new Date();
    }

    //getter and setter
    public List<Task> getTasks() {
        return new ArrayList<>(this.tasks);
    }

    public Date getLastUpdate() {
        return this.lastUpdate;
    }
    
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    //utilitas untuk update data List Task
    public void addTask(Task task) {
        this.tasks.add(task);
    }
    
    public void removeTask(Task task) {
        this.tasks.remove(task);
    }
}
