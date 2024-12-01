package org.uns.todolist;

import java.io.IOException;

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
        fxmlLoader.setControllerFactory(param -> new FXMLController());
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

}