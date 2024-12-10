package org.uns.todolist.ui;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import org.kordamp.ikonli.javafx.FontIcon;
import org.uns.todolist.helper.ResponsiveHelper;
import org.uns.todolist.service.DataManager;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class TaskCreationPanel extends HBox {

    private final TextField nameField;
    private final Button triggerExtensionButton;
    private final VBox createForm;
    private final BooleanProperty isCreating = new SimpleBooleanProperty(false);
    private final DataManager dataManager;

    private HBox deadlineInputContainer = null;
    
    public TaskCreationPanel(DataManager dataManager) {
        this.dataManager = dataManager;
        
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(10));
        this.getStyleClass().add("create-pane"); 

        this.createForm = new VBox(10);
        this.createForm.setAlignment(Pos.TOP_CENTER);
        this.createForm.setPadding(new Insets(20));
        this.createForm.getStyleClass().add("create-form"); 
        HBox.setHgrow(createForm, Priority.ALWAYS);

        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER_LEFT);

        nameField = new TextField();
        nameField.setPromptText("Tambah Aktivitas...");
        nameField.getStyleClass().add("create-name-field"); 
        HBox.setHgrow(nameField, Priority.ALWAYS);
        
        triggerExtensionButton = new Button();
        triggerExtensionButton.getStyleClass().add("trigger-extension-button"); 
        triggerExtensionButton.setGraphic(new FontIcon("fas-plus"));
        triggerExtensionButton.setOnAction(e -> isCreating.set(true));

        inputBox.getChildren().addAll(nameField, triggerExtensionButton);
        createForm.getChildren().add(inputBox);
        this.getChildren().add(createForm);

        nameField.setFocusTraversable(false);
        flagListener();
    }


    public void handleFieldExtension() {
        ResponsiveHelper.animateResize(this, 200, Duration.seconds(0.3));
        nameField.setPromptText("Nama Aktivitas");
        triggerExtensionButton.setGraphic(new FontIcon("fas-times"));
        this.deadlineInputContainer = createDateInputField();
        createForm.getChildren().add(deadlineInputContainer);
        nameField.requestFocus();
        triggerExtensionButton.setOnAction(e -> handleShrinkingFIeld());
        isCreating.set(true);
    }

    public void handleShrinkingFIeld() {
        ResponsiveHelper.animateResize(this, 80, Duration.seconds(0.3));
        createForm.getChildren().remove(deadlineInputContainer);
        triggerExtensionButton.setGraphic(new FontIcon("fas-plus"));
        triggerExtensionButton.setOnAction(e -> isCreating.set(true));
        nameField.setPromptText("Tambahkan Aktivitas...");
        nameField.clear();
        isCreating.set(false);
    }

    public HBox createDateInputField() {
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
        Button addTaskButton = new Button("ADD");
        addTaskButton.getStyleClass().add("add-task-button"); 
        HBox addButtonContainer = new HBox(addTaskButton);
        addButtonContainer.setAlignment(Pos.BOTTOM_RIGHT); 

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox dateContainer = new HBox(10, datePickerContainer, spacer, addButtonContainer);
        dateContainer.setId("dateContainer");
        dateContainer.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(dateContainer, Priority.ALWAYS);
        VBox.setVgrow(dateContainer, Priority.ALWAYS);

        addTaskButton.setOnAction(e -> handleAddTask(datePicker));
        return dateContainer;
    }

    private void clearDatePicker(DatePicker datePicker) {
        datePicker.setValue(null); 
    }

    private void handleAddTask(DatePicker datePicker) {
        try{
            String taskName = nameField.getText().trim();
            Date deadline = getDateFromDatePickerInput(datePicker);
            dataManager.addTask(taskName, deadline);
            handleShrinkingFIeld();
            nameField.clear();
        } catch (IOException e) {
            showError("gagal menambahkan aktivitas. coba lagi.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (ParseException e) {
            showError("format tangga salah. pakai dd/MM/yyyy.");
        } 
    }
    

    public BooleanProperty getCreationState() {
        return isCreating;
    }

    private Date getDateFromDatePickerInput(DatePicker datePicker) throws ParseException {
        String dateText = datePicker.getEditor().getText().trim();
        Date date = null;
        if (!dateText.isEmpty()) {
            date = new SimpleDateFormat("dd/MM/yyyy").parse(dateText);
        }
        return date;
    }


   private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }
    
    private void flagListener() {
        nameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                isCreating.set(true);
            } 
        });

        isCreating.addListener((observable, oldFlag, newFlag) -> {
            if (newFlag == true && oldFlag != true) { 
                handleFieldExtension(); 
            }
    });
}
}

