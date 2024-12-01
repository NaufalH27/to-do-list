package org.uns.todolist;

import java.io.IOException;

import org.uns.todolist.models.SaveState;
import org.uns.todolist.service.StateManager;
import org.uns.todolist.service.StatePersistence;

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

    static void setRoot(FXMLLoader fxmlLoader, Stage stage) throws IOException {
        //inisiasi objek yang dibutuhkan aplikasi
        StatePersistence statePersistence = createStatePersistence();
        SaveState saveState = loadSaveState(statePersistence);
        StateManager stateManager = createStateManager(saveState, statePersistence);
        
        //setup stage
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.show();

        //mulai aplikasi
        fxmlLoader.setControllerFactory(param -> new FXMLController());
       
    }


    private static StatePersistence createStatePersistence() {
        return new StatePersistence();
    }

    private static SaveState loadSaveState(StatePersistence statePersistence) throws IOException {
        try {
            SaveState loadedState = statePersistence.loadSavedData();

            if (loadedState == null) {
                return statePersistence.recreateSaveFile();
            }
            return loadedState;

        } catch (IOException | JsonSyntaxException e) {
            return statePersistence.recreateSaveFile();
        }
    }

    private static StateManager createStateManager(SaveState saveState, StatePersistence statePersistence) {
        return new StateManager(saveState, statePersistence);
    }

    
    public static void main(String[] args) {
        launch(args);
    }

}