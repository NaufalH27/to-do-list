package org.uns.todolist.models;

import java.util.Collections;
import java.util.Date;
import java.util.List;

//objek untuk menyimpan dan load progress aplikasi  
public class SaveState {

    private final List<Task> tasks;
    private Date lastUpdate = null;

    
    public SaveState(List<Task> tasks) {
        this.tasks = tasks;
    }

    //getter and setter
    public List<Task> getTasks() {
        return Collections.unmodifiableList(this.tasks);
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
