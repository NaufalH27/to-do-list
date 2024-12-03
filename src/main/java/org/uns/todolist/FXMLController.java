package org.uns.todolist;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.uns.todolist.models.Task;
import org.uns.todolist.service.DataManager;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
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
    
        // Get all tasks and sort them by deadline and then completion status
        List<Task> sortedTasks = dataManager.getAllTasks()
                                            .stream()
                                            .sorted(Comparator
                                                .comparing(Task::getIsCompleted)
                                                .thenComparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder())))
                                            .collect(Collectors.toList());
    
        for (Task task : sortedTasks) {
            HBox taskBox = createTaskBox(task);
    
            // Style for overdue tasks (alert style)
            if (!task.getIsCompleted() && task.getDeadline() != null && task.getDeadline().before(new Date())) {
                taskBox.setStyle("-fx-background-color: #ffcccc; -fx-border-color: #ff0000; -fx-border-width: 2;");
            }
    
            // Style for completed tasks
            if (task.getIsCompleted()) {
                taskBox.setStyle("-fx-background-color: #d3d3d3; -fx-opacity: 0.6; -fx-padding: 15;"); // Keep padding consistent
            }
    
            taskContainer.getChildren().add(taskBox);
        }
    }
    
    
    


    private HBox createTaskBox(Task task) {
        HBox taskBox = new HBox(15); // Spacing between elements
        taskBox.setStyle("-fx-padding: 15; -fx-background-color: #e0e0e0; -fx-border-radius: 10; -fx-border-color: #ddd;");
        taskBox.setAlignment(Pos.CENTER_LEFT); // Ensure elements are aligned properly

        // Checklist box
        CheckBox completeCheckBox = new CheckBox();
        completeCheckBox.setStyle("-fx-scale-x: 1.5; -fx-scale-y: 1.5;"); // Increase size of the checkbox
        completeCheckBox.setSelected(task.getIsCompleted());
        completeCheckBox.setOnAction(e -> toggleTaskCompletion(task));

        // Task details
        VBox taskDetails = new VBox(5); // Space between task name and deadline
        taskDetails.setAlignment(Pos.CENTER_LEFT); // Align task details to the left
        Label taskNameLabel = new Label(task.getNamaTask());
        taskNameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        if (task.getIsCompleted()) {
            taskNameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-strikethrough: true;"); // Cross out if completed
        }
        Label deadlineLabel = new Label(task.getDeadline() != null
                ? new SimpleDateFormat("d MMM yyyy").format(task.getDeadline())
                : "No Deadline");
        deadlineLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #757575;");
        taskDetails.getChildren().addAll(taskNameLabel, deadlineLabel);

        // Action buttons
        HBox actionButtons = new HBox(10); // Space between buttons
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-size: 14px;");
        editButton.setOnAction(e -> handleEditTask(task));

        Button removeButton = new Button("Remove");
        removeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px;");
        removeButton.setOnAction(e -> handleRemoveTask(task));
        actionButtons.getChildren().addAll(editButton, removeButton);

        // Add padding and space for the task box
        taskBox.setPadding(new Insets(15)); // Space inside the task box
        taskBox.setMinHeight(100); // Minimum height for a spacious look
        taskBox.getChildren().addAll(completeCheckBox, taskDetails, actionButtons);

        // Adjust horizontal growth and alignment
        HBox.setHgrow(taskDetails, Priority.ALWAYS); // Ensure details expand properly

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
        // Create a dialog for editing the task
        TextField taskNameField = new TextField(task.getNamaTask());
        TextField deadlineField = new TextField(
            task.getDeadline() != null ? new SimpleDateFormat("dd/MM/yyyy").format(task.getDeadline()) : ""
        );
    
        VBox dialogContent = new VBox(10);
        dialogContent.getChildren().addAll(
            new Label("Task Name:"), taskNameField,
            new Label("Deadline (dd/MM/yyyy):"), deadlineField
        );
    
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Edit Task Details");
        dialog.getDialogPane().setContent(dialogContent);
    
        // Wait for user response
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String newName = taskNameField.getText().trim();
                String newDeadlineText = deadlineField.getText().trim();
                Date newDeadline = null;
    
                // Validate inputs
                if (newName.isEmpty()) {
                    showError("Task name cannot be empty.");
                    return;
                }
    
                if (!newDeadlineText.isEmpty()) {
                    try {
                        newDeadline = new SimpleDateFormat("dd/MM/yyyy").parse(newDeadlineText);
                    } catch (Exception e) {
                        showError("Invalid deadline format. Use dd/MM/yyyy.");
                        return;
                    }
                }
    
                try {
                    dataManager.editTask(task.getTaskId(), newName, newDeadline);
                    refreshTaskContainer();
                } catch (Exception e) {
                    showError("Failed to edit the task. Try again.");
                }
            }
        });
    }
    

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }
}

