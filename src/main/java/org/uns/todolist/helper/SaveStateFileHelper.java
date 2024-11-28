package org.uns.todolist.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.uns.todolist.models.SaveState;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class SaveStateFileHelper {
    private static final String SAVE_PATH = System.getProperty("user.dir") + "/save/";
    private static final String FILE_NAME = "saveFile.json";
    

    public static void save(SaveState state) throws IOException {
        File directory = new File(SAVE_PATH);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //Cek apakah direktori ada
        if (!directory.exists()) {
            directory.mkdirs();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_PATH + FILE_NAME));
        gson.toJson(state, writer);
    }

    public static SaveState loadSavedState() throws IOException {
        Gson gson = new Gson();
        FileReader reader = new FileReader(SAVE_PATH + FILE_NAME);
        return gson.fromJson(reader, SaveState.class);
    }
}

