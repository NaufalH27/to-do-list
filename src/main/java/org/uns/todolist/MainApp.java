package org.uns.todolist;

import java.io.IOException;

import org.uns.todolist.models.SaveData;
import org.uns.todolist.service.DataManager;
import org.uns.todolist.service.DataPersistence;

import com.google.gson.JsonSyntaxException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApp extends Application {
    public static final String TITLE = "TO-Do List";
    public static final String FXML = "primary";

    @Override
    public void start(@SuppressWarnings("exports") Stage s) throws IOException {
        Stage stage = s;
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/fxml/" + FXML + ".fxml"));
        setRoot(fxmlLoader, stage);
    }

    void setRoot(FXMLLoader fxmlLoader, Stage stage) throws IOException {
        FXMLController controller = generateController();
        //mulai aplikasi
        fxmlLoader.setControllerFactory(param -> controller);
        //setup stage
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 1280, 720);
        String cssFile = MainApp.class.getResource("/styles/Styles.css").toExternalForm();
        scene.getStylesheets().add(cssFile);
        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.show();    
    }

    private FXMLController generateController() throws IOException {
        DataPersistence dataPersistence = createDataPersistence();
        SaveData saveData = loadSaveData(dataPersistence);
        DataManager dataManager = createDataManager(saveData, dataPersistence);
        return new FXMLController(dataManager);
    }

    private DataPersistence createDataPersistence() {
        return new DataPersistence();
    }

    private SaveData loadSaveData(DataPersistence dataPersistence) throws IOException {
        try {
            SaveData loadedData = dataPersistence.loadSavedData();
            if (loadedData == null) {
                return dataPersistence.recreateSaveFile();
            }
            return loadedData;
        } catch (IOException | JsonSyntaxException e) {
            return dataPersistence.recreateSaveFile();
        }
    }

    private DataManager createDataManager(SaveData saveData, DataPersistence dataPersistence) {
        return new DataManager(saveData, dataPersistence);
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}