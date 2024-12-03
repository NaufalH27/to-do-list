package org.uns.todolist;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.kordamp.ikonli.javafx.FontIcon;
import org.uns.todolist.models.Task;
import org.uns.todolist.service.DataManager;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class FXMLController {

    @FXML
    private TextField taskNameField;

    @FXML
    private TextField deadlineField;

    @FXML
    private Button addTaskButton;

    @FXML
    private VBox taskContainer;

    private final DataManager dataManager;

    public FXMLController(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @FXML
    public void initialize() {
        refreshTaskContainer();
    }

    @FXML
    public void handleAddTask() {
        String taskName = taskNameField.getText().trim();
        String deadlineText = deadlineField.getText().trim();

        if (taskName.isEmpty()) {
            showError("Task Name cannot be empty.");
            return;
        }

        Date deadline = null;
        if (!deadlineText.isEmpty()) {
            try {
                deadline = new SimpleDateFormat("dd/MM/yyyy").parse(deadlineText);
            } catch (Exception e) {
                showError("Invalid deadline format. Use dd/MM/yyyy.");
                return;
            }
        }

        try {
            dataManager.addTask(taskName, deadline);
            refreshTaskContainer();
            taskNameField.clear();
            deadlineField.clear();
        } catch (IOException e) {
            showError("Failed to add task. Try again.");
        }
    }

    private void refreshTaskContainer() {
        taskContainer.getChildren().clear();
    
        List<Task> sortedTasks = dataManager.getAllTasks()
                .stream()
                .sorted(Comparator
                        .comparing(Task::getIsCompleted)
                        .thenComparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    
        Date today = getDayToday();
    
        for (Task task : sortedTasks) {
            HBox taskBox = createTaskBox(task);
            VBox taskDetails = (VBox) taskBox.getChildren().get(1);
            Label deadlineLabel = (Label) taskDetails.getChildren().get(1);
    
            taskBox.getStyleClass().removeAll("task-box-completed", "task-box-overdue", "task-box-today");
            deadlineLabel.getStyleClass().removeAll("deadline-label-overdue", "deadline-label-today");
    
            if (task.getIsCompleted()) {
                taskBox.getStyleClass().add("task-box-completed");
            }
    
            if (!task.getIsCompleted() && task.getDeadline() != null && task.getDeadline().before(today)) {
                deadlineLabel.getStyleClass().add("deadline-label-overdue");
            }
    
            if (!task.getIsCompleted() && task.getDeadline() != null && task.getDeadline().equals(today)) {
                deadlineLabel.getStyleClass().add("deadline-label-today");
            }
    
            taskContainer.getChildren().add(taskBox);
        }
    }
    

    private HBox createTaskBox(Task task) {
        HBox taskBox = new HBox(20);
        taskBox.getStyleClass().add("task-box"); // Add base style class
        taskBox.setAlignment(Pos.CENTER_LEFT);
    
        CheckBox completeCheckBox = new CheckBox();
        completeCheckBox.setStyle("-fx-scale-x: 1.5; -fx-scale-y: 1.5;");
        completeCheckBox.setSelected(task.getIsCompleted());
        completeCheckBox.setOnAction(e -> toggleTaskCompletion(task));
    
        VBox taskDetails = new VBox(8);
        taskDetails.setAlignment(Pos.CENTER_LEFT);
    
        Label taskNameLabel = new Label(task.getNamaTask());
        taskNameLabel.getStyleClass().add("task-name");
        if (task.getIsCompleted()) {
            taskNameLabel.getStyleClass().add("task-name-completed");
        }
    
        Label deadlineLabel = new Label(task.getDeadline() != null
                ? new SimpleDateFormat("d MMM yyyy").format(task.getDeadline())
                : "No Deadline");
        deadlineLabel.getStyleClass().add("deadline-label");
        taskDetails.getChildren().addAll(taskNameLabel, deadlineLabel);
    
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
    
        Button editButton = new Button();
        editButton.setGraphic(new FontIcon("fas-pencil-alt"));
        editButton.getStyleClass().add("button-edit");
        editButton.setOnAction(e -> handleEditTask(task));
    
        Button removeButton = new Button();
        removeButton.setGraphic(new FontIcon("fas-trash"));
        removeButton.getStyleClass().add("button-remove");
        removeButton.setOnAction(e -> handleRemoveTask(task));
    
        actionButtons.getChildren().addAll(editButton, removeButton);
    
        taskBox.setMinHeight(80);
        taskBox.getChildren().addAll(completeCheckBox, taskDetails, actionButtons);
    
        HBox.setHgrow(taskDetails, Priority.ALWAYS);
    
        return taskBox;
    }
    

    

    private void toggleTaskCompletion(Task task) {
        if (task.getIsCompleted()) {
            dataManager.uncompleteTask(task.getTaskId()); // Method to mark as incomplete
        } else {
            dataManager.completeTask(task.getTaskId());
        }
        refreshTaskContainer();
    }
    

    private void handleRemoveTask(Task task) {
        try {
            dataManager.removeTask(task.getTaskId());
            refreshTaskContainer();
        } catch (IOException e) {
            showError("Failed to remove task. Try again.");
        }
    }

    private void handleEditTask(Task task) {
        // Handle edit functionality (e.g., show dialog to edit task details)
        showError("Edit functionality is not implemented yet.");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private Date getDayToday() {
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}

