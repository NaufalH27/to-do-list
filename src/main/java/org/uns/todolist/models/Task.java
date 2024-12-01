package org.uns.todolist.models;

import java.util.Date;

//objek yang merepresentasikan task/aktivitas di dalam to do list
public class Task {

    //getter only
    private final int taskId;
    private final String namaTask;
    private final Date created;
    private final Date deadline;

    //getter and setter
    private Boolean isCompleted = false;
    private Date completedDate = null;


    public Task(int taskId, String namaTask, Date deadline) {
        this.taskId = taskId;
        this.namaTask = namaTask;
        this.deadline = deadline;

        //set variabel "created" sebagai waktu sekarang kapan objek itu terbuat
        this.created = new Date();
    }


    //getter and setter
    public String getNamaTask() {
        return this.namaTask;
    }

    public Date getdateCreated() {
        return this.created;
    }

    public Date getDeadline() {
        return this.deadline;
    }

    public Boolean getIsCompleted() {
        return this.isCompleted;
    }

    public Date getCompletedDate() {
        return this.completedDate;
    }

    public int getTaskId() {
        return taskId;
    }
    
    //fungsi untuk menandai kalau task itu selesai atau tidak    
    public void completeTask() {
        this.isCompleted = true;
        this.completedDate = new Date();
    }

    public void uncompleteTask() {
        this.isCompleted = false;
        this.completedDate = null;
    }

}
