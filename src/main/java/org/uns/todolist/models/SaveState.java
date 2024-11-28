package org.uns.todolist.models;

import java.util.Date;
import java.util.List;

//objek untuk menyimpan dan load progress aplikasi  
public class SaveState {

    private List<Task> tasks;
    private Date lastUpdate;


    //getter and setter
    public List<Task> getTaskList() {
        return this.tasks;
    }

    public Date getLastUpdate() {
        return this.lastUpdate;
    }


    public void setTaskList(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
