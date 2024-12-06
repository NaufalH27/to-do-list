package org.uns.todolist.ui;

import java.time.LocalDate;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DateInputField {

    public static HBox createDateInputField() {
        Label dateLabel = new Label("  Date:  ");
        dateLabel.setId("dateLabel");

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        datePicker.setPromptText("dd/mm/yyyy");
        datePicker.setId("datePicker");

        // Create the Clear button
        Button clearButton = new Button("Clear");
        clearButton.setId("clearButton");
        clearButton.getStyleClass().add("modern-blue-button");
        clearButton.setOnAction(e -> clearDatePicker(datePicker));

        HBox datePickerContainer = new HBox(10, dateLabel, datePicker, clearButton);
        datePickerContainer.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox dateContainer = new HBox(10, datePickerContainer, spacer);
        dateContainer.setId("dateContainer");
        dateContainer.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(dateContainer, javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(dateContainer, javafx.scene.layout.Priority.ALWAYS);

        return dateContainer;
    }

    private static void clearDatePicker(DatePicker datePicker) {
        datePicker.setValue(null); 
    }
}