package org.uns.todolist.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.uns.todolist.models.SaveData;
import org.uns.todolist.models.Task;


public class DataManager {
    private final SaveData data;
    private final DataPersistence persistence;
    private final List<DataObserver> observers = new ArrayList<>();

    public DataManager(SaveData data, DataPersistence persistence) {
        this.data = data;
        this.persistence = persistence;
    }

     /**
     * Menambahkan task baru dengan nama dan tenggat waktu yang ditentukan ke daftar  setiap task yang ditambahkan, id akan bertambah.
     * 
     * @param namaTask Nama task yang akan ditambahkan. Tidak boleh null atau kosong.
     * @param deadline Tenggat waktu untuk task.
     * @throws IllegalArgumentException jika nama task null atau kosong.
     * @throws IOException jika terjadi kesalahan saat menyimpan data task.
     */
    public synchronized void addTask(String namaTask, Date deadline) throws IOException {
        if (namaTask == null || namaTask.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama Aktivitas Tidak Boleh Kosong");
        }

        //jika nama valid : buat task baru dengan nama tersebut dan tambahkan ke save data
        Task newTask = new Task(data.getCurrentTaskId(), namaTask, deadline);
        this.data.addTask(newTask);
        
        //save data
        this.data.incrementTaskId();
        this.persistence.save(this.data);
        this.notifyObserver();
    }

    /**
     * Menghapus task berdasarkan ID task dari daftar task.
     * 
     * @param taskId ID task yang akan dihapus.
     * @throws IllegalArgumentException jika task dengan ID yang ditentukan tidak ditemukan.
     * @throws IOException jika terjadi kesalahan saat menyimpan data setelah penghapusan.
     */
    public synchronized void removeTask(int taskId)  throws IOException {
        Task taskToRemove = this.getTaskById(taskId);
        this.data.removeTask(taskToRemove);
        //save data
        this.persistence.save(this.data);
        this.notifyObserver();
    }

    /**
     * Menandai task sebagai selesai berdasarkan ID task.
     * 
     * @param taskId ID task yang akan ditandai sebagai selesai.
     * @throws IllegalArgumentException jika task dengan ID yang ditentukan tidak ditemukan.
     */
    public synchronized void completeTask(int taskId) throws IOException {
        Task taskToComplete = this.getTaskById(taskId);
        taskToComplete.completeTask();
        this.persistence.save(this.data);
        this.notifyObserver();
    }

    public synchronized void uncompleteTask(int taskId) throws IOException {
        Task taskToComplete = this.getTaskById(taskId);
        taskToComplete.uncompleteTask();
        this.persistence.save(this.data);
        this.notifyObserver();
    }

    /**
     * Mengambil semua task yang sudah ditandai sebagai selesai.
     * 
     * @return Daftar task yang telah selesai.
     */
    public synchronized List<Task> getAllTasks() {     
        List<Task> copyOfTasks = new ArrayList<>(this.data.getTasks());
        return copyOfTasks;
    }

    public synchronized void editTask(int taskId, String newName, Date newDeadline) throws IOException {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama Aktivitas Tidak Boleh Kosong");
        }
        Task editedTask = getTaskById(taskId);
        editedTask.setNamaTask(newName);
        editedTask.setDeadline(newDeadline);
        this.persistence.save(this.data);
        this.notifyObserver();
    }
    
    public synchronized void addObserver(DataObserver listener) {
        observers.add(listener);
    }

    public synchronized void notifyObserver() {
        for (DataObserver observer : observers) {
            observer.updateData(this.getAllTasks());
        }
    }
    
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
