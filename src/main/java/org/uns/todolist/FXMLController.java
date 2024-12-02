package org.uns.todolist;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.uns.todolist.models.Task;
import org.uns.todolist.service.DataManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class FXMLController {

    @FXML
    private TextField taskNameField;

    @FXML
    private TextField deadlineField;

    @FXML
    private Button addTaskButton;

    @FXML
    private TableView<Task> taskTable;

    @FXML
    private TableColumn<Task, Integer> idColumn;

    @FXML
    private TableColumn<Task, String> nameColumn;

    @FXML
    private TableColumn<Task, String> deadlineColumn;

    @FXML
    private TableColumn<Task, String> statusColumn;

    @FXML
    private TableColumn<Task, HBox> actionsColumn;

    private final DataManager dataManager;

    private final ObservableList<Task> taskList = FXCollections.observableArrayList();

    public FXMLController(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("taskId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("namaTask"));
        deadlineColumn.setCellValueFactory(task -> {
            Date deadline = task.getValue().getDeadline();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return new SimpleStringProperty(deadline != null ? sdf.format(deadline) : "-");
        });
        statusColumn.setCellValueFactory(task -> 
            new SimpleStringProperty(task.getValue().getIsCompleted() ? "Completed" : "Pending")
        );

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                    return;
                }

                Task task = getTableRow().getItem();
                if (task == null) return;

                Button completeBtn = new Button("Complete");
                completeBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
                completeBtn.setOnAction(e -> handleCompleteTask(task));

                Button removeBtn = new Button("Remove");
                removeBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                removeBtn.setOnAction(e -> handleRemoveTask(task));

                HBox actions = new HBox(5, completeBtn, removeBtn);
                setGraphic(actions);
            }
        });

        refreshTaskTable();
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
            refreshTaskTable();
            taskNameField.clear();
            deadlineField.clear();
        } catch (IOException e) {
            showError("Failed to add task. Try again.");
        }
    }

    private void handleCompleteTask(Task task) {
        dataManager.completeTask(task.getTaskId());
        refreshTaskTable();
    }

    private void handleRemoveTask(Task task) {
        try {
            dataManager.removeTask(task.getTaskId());
            refreshTaskTable();
        } catch (IOException e) {
            showError("Failed to remove task. Try again.");
        }
    }

    private void refreshTaskTable() {
        taskList.setAll(dataManager.getAllTasks());
        taskTable.setItems(taskList);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }
}
