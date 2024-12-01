package org.uns.todolist.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.uns.todolist.models.SaveState;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class StatePersistence {
    private final String SAVE_PATH = System.getProperty("user.dir") + "/save/";
    private final String FILE_NAME = "saveFile.json";
    

    public void save(SaveState state) throws IOException {
        File directory = new File(SAVE_PATH);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //Cek apakah direktori ada
        if (!directory.exists()) {
            directory.mkdirs();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_PATH + FILE_NAME));
        state.setLastUpdate(new Date());
        gson.toJson(state, writer);
    }

    public SaveState loadSavedState() throws IOException {
        Gson gson = new Gson();
        FileReader reader = new FileReader(SAVE_PATH + FILE_NAME);
        return gson.fromJson(reader, SaveState.class);
    }


    public SaveState recreateSaveFile() throws IOException {
        System.out.println("Gagal memuat save File, membuat save baru");
        SaveState newState = new SaveState(new ArrayList<>());

        try {
            this.save(newState);
        } catch (IOException saveException) {
            System.out.println("Unexpected Exception Program gagal dijalankan: " + saveException.getMessage());
            throw new IOException("Gagal untuk membuat save baru", saveException);
        }
        
        return newState;
    }
}
