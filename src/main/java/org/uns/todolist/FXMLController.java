package org.uns.todolist;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.kordamp.ikonli.javafx.FontIcon;
import org.uns.todolist.models.Task;
import org.uns.todolist.service.DataManager;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class FXMLController {

    private final DataManager dataManager;

    public FXMLController(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @FXML
    private TextField taskNameField;

    @FXML
    private TextField deadlineField;

    @FXML
    private VBox calendarContainer;

    @FXML
    private VBox taskContainer;
    
    @FXML
    private HBox addContainer;

    @FXML
    private ScrollPane taskScrollPane;

    @FXML
    private Button cancelTaskButton; 

    @FXML
    private VBox dateContainer;

    @FXML
    private VBox addTaskForm;

    @FXML
private Label Month;

    @FXML
    private Label date;

    @FXML
    private Label greeting;

    private final ObjectProperty<AppFlag> flag = new SimpleObjectProperty<>(AppFlag.FREE);

 

    @FXML
    public void initialize() {
        cancelTaskButton.setGraphic(new FontIcon("fas-plus"));
        taskNameField.setFocusTraversable(false);
        taskScrollPane.requestFocus();
        scrollSpeedListener(500);
        inputFieldListener();
        setupFlagListener();
        refreshTaskContainer();
        updateDate();
        updateGreeting(); 
              Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(1), e -> {
            updateDate();
            updateGreeting();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        VBox navCalendar = new NavigationCalendar();
        calendarContainer.getChildren().add(navCalendar);
        VBox.setVgrow(navCalendar, Priority.ALWAYS);
		HBox.setHgrow(navCalendar, Priority.ALWAYS);
    
    }



    @FXML
    private void inputFieldListener() {
        taskNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                flag.set(AppFlag.CREATE);
            } 
        });

        cancelTaskButton.setOnAction(event -> {
            if (flag.get() == AppFlag.CREATE) {
                flag.set(AppFlag.FREE); 
            } else if (flag.get() == AppFlag.FREE) {
                flag.set(AppFlag.CREATE); 
            }
        });

    }


    private void setupFlagListener() {
        flag.addListener((observable, oldFlag, newFlag) -> {
            if (newFlag == AppFlag.CREATE) {
                addContainer.setMaxHeight(200);
                addContainer.setMinHeight(200);
                Node dateInput = loadFXML("focusedInput");
                
                addTaskForm.getChildren().add(dateInput);
                cancelTaskButton.setGraphic(new FontIcon("fas-times"));

                Button addButton = (Button) addTaskForm.lookup("#addTaskButton");
                DatePicker datePicker = (DatePicker) addTaskForm.lookup("#datePicker");
                addButton.setOnAction(e -> handleAddTask(datePicker));
            }

            if(newFlag == AppFlag.FREE) {
                addContainer.setMaxHeight(80);
                addContainer.setMinHeight(80);
                Node dateContainer = addTaskForm.lookup("#dateContainer");
                if (dateContainer != null) {
                    addTaskForm.getChildren().remove(dateContainer);
                }
                cancelTaskButton.setGraphic(new FontIcon("fas-plus"));
                taskNameField.clear();
            }
        });
    }
    

 

    private void scrollSpeedListener(double scrollSpeed) {
        taskScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        taskScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    
        taskScrollPane.setOnScroll(event -> {
            double deltaY = event.getDeltaY() * scrollSpeed;
            double newVvalue = taskScrollPane.getVvalue() - deltaY;
            newVvalue = Math.min(Math.max(newVvalue, 0.0), 1.0);
            taskScrollPane.setVvalue(newVvalue);
            event.consume();
        });
    }
    
  
    @FXML 
    public Node loadFXML(String fileName) {
        try {   
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fileName + ".fxml"));
            return loader.load();   
        } catch (IOException e) {
            e.printStackTrace();
            return new VBox(20);
        }
    }
    
    
    @FXML
    public void handleAddTask(DatePicker datePicker) {
        String taskName = taskNameField.getText().trim();
        String dateText = datePicker.getEditor().getText().trim();


        if (taskName.isEmpty()) {
            showError("Task Name cannot be empty.");
            return;
        }

        Date deadline = null;
        if (!dateText.isEmpty()) {
            try {
                deadline = new SimpleDateFormat("dd/MM/yyyy").parse(dateText);
            } catch (Exception e) {
                showError("Invalid deadline format. Use dd/MM/yyyy.");
                return;
            }
        }

        try {
            dataManager.addTask(taskName, deadline);
            refreshTaskContainer();
            this.flag.set(AppFlag.FREE);
        } catch (IOException e) {
            showError("Failed to add task. Try again.");
        }
    }

    private void refreshTaskContainer() {
        taskContainer.getChildren().clear();
        Date today = getDayToday();
        List<Task> sortedTasks = dataManager.getAllTasks()
        .stream()
        .sorted(Comparator
            .   comparing((Task task) -> task.getCompletedDate() == null ? 0 : 1) 
                .thenComparing((Task task) -> task.getCompletedDate() == null ? Long.MAX_VALUE : task.getCompletedDate().getTime(), Comparator.reverseOrder())
                .thenComparing((Task task) -> task.getIsCompleted() ? 1 : 0) 
                .thenComparing(task -> task.getDeadline() != null && task.getDeadline().before(today) ? 1 : 0) 
                .thenComparing(Task::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()))
        )
        .collect(Collectors.toList());

    
        for (Task task : sortedTasks) {
            HBox taskBox = createTaskBox(task);
            VBox taskDetails = (VBox) taskBox.getChildren().get(1);
            Label taskNameLabel = (Label) taskDetails.getChildren().get(0);
            Label deadlineLabel = (Label) taskDetails.getChildren().get(1);
    
            taskBox.getStyleClass().removeAll("task-box-completed", "task-box-overdue", "task-box-today");
            deadlineLabel.getStyleClass().removeAll("deadline-label-overdue", "deadline-label-today");

            if (task.getIsCompleted()) {
                taskNameLabel.getStyleClass().add("task-name-completed");
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
        taskBox.getStyleClass().add("task-box"); 
        taskBox.setAlignment(Pos.CENTER_LEFT);
    
        CheckBox completeCheckBox = new CheckBox();
        completeCheckBox.getStyleClass().add("check-box");
        completeCheckBox.setSelected(task.getIsCompleted());
        completeCheckBox.setOnAction(e -> toggleTaskCompletion(task));
    
        VBox taskDetails = new VBox(8);
        taskDetails.setAlignment(Pos.CENTER_LEFT);
    
        Label taskNameLabel = new Label(task.getNamaTask());
        taskNameLabel.getStyleClass().add("task-name");
    
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
        try {
            if (task.getIsCompleted()) {
                dataManager.uncompleteTask(task.getTaskId());
            } else {
                dataManager.completeTask(task.getTaskId());
            }
            refreshTaskContainer();
        } catch (IOException e) {
            showError("Failed to complete task. Try again.");
        }
       
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

    private void updateDate() {
        LocalDate currentDate = LocalDate.now();
        String monthName = currentDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String dayOfMonth = String.valueOf(currentDate.getDayOfMonth());

        Month.setText(monthName);
        date.setText(dayOfMonth);
    }

    private void updateGreeting() {
        LocalTime currentTime = LocalTime.now();
        String greetingText;

        if (currentTime.isBefore(LocalTime.NOON)) {
            greetingText = "Selamat Pagi!"; 
        } else if (currentTime.isBefore(LocalTime.of(15, 0))) {
            greetingText = "Selamat Siang!"; 
        } else if (currentTime.isBefore(LocalTime.of(18, 0))) {
            greetingText = "Selamat Sore!"; 
        } else {
            greetingText = "Selamat Malam!"; 
        }

        greeting.setText(greetingText);
    }
}

