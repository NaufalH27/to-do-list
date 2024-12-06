package org.uns.todolist.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class Controls {
    public static HBox createAddButton(){
        Button addTaskButton = new Button("ADD");
        addTaskButton.getStyleClass().add("add-task-button"); 
        HBox addButtonContainer = new HBox(addTaskButton);
        addButtonContainer.setAlignment(Pos.BOTTOM_RIGHT); 
        return addButtonContainer;
    }
}
