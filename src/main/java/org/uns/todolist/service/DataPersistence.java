package org.uns.todolist.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.uns.todolist.models.SaveData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class DataPersistence {
    private final String SAVE_PATH = System.getProperty("user.dir") + "/save/";
    private final String FILE_NAME = "saveFile.json";
    

    public void save(SaveData data) throws IOException {
        File directory = new File(SAVE_PATH);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //Cek apakah direktori ada
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_PATH + FILE_NAME))) {
            data.setLastUpdate(new Date());
            gson.toJson(data, writer);
            writer.flush();
            writer.close(); 
        }
       
    }

    public SaveData loadSavedData() throws IOException {
        Gson gson = new Gson();
        try(FileReader reader = new FileReader(SAVE_PATH + FILE_NAME)) {
            return gson.fromJson(reader, SaveData.class);
        }
       
    }


    public SaveData recreateSaveFile() throws IOException {
        System.out.println("Gagal memuat save File, membuat save baru");
        SaveData newData = new SaveData(new ArrayList<>());

        try {
            this.save(newData);
        } catch (IOException saveException) {
            System.out.println("Unexpected Exception Program gagal dijalankan: " + saveException.getMessage());
            throw new IOException("Gagal untuk membuat save baru", saveException);
        }
        
        return newData;
    }
}
