package org.uns.todolist.models;

import java.util.Date;
import java.util.List;

//objek untuk menyimpan dan load progress aplikasi  
public class SaveState {

    private List<Task> tasks;
    private Date lastUpdate;


    //getter and setter
    public List<Task> getTasks() {
        return this.tasks;
    }

    public Date getLastUpdate() {
        return this.lastUpdate;
    }


    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
