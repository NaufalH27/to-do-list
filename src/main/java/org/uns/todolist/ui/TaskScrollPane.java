package org.uns.todolist.ui;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import org.kordamp.ikonli.javafx.FontIcon;
import org.uns.todolist.helper.DayHelper;
import org.uns.todolist.helper.ResponsiveHelper;
import org.uns.todolist.models.Task;
import org.uns.todolist.service.DataManager;
import org.uns.todolist.service.FilterMethod;
import org.uns.todolist.service.SortingMethod;
import org.uns.todolist.service.DataObserver;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;


public class TaskScrollPane extends ScrollPane implements DataObserver {
    private List<Task> tasks;
    private Function<List<Task>, List<Task>> sortMethod = SortingMethod::defaultMethod;
    private Function<List<Task>, List<Task>> filterMethod = FilterMethod::noFilter;
    private final ObjectProperty<HBox> edittedTaskBox = new SimpleObjectProperty<>(null);
    private LocalDate filterByDate = null;
    private final VBox taskContainer;
    private final BooleanProperty isEditting = new SimpleBooleanProperty(false);

    private final DataManager dataManager;
    
    public TaskScrollPane(List<Task> initialTasks, DataManager dataManager) {
        this.dataManager = dataManager;
        this.tasks = initialTasks;
        this.taskContainer = new VBox();
        this.taskContainer.setAlignment(Pos.TOP_LEFT);
        this.taskContainer.setSpacing(10);
        VBox.setVgrow(this.taskContainer, Priority.ALWAYS);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        this.taskContainer.getChildren().add(spacer);
        this.taskContainer.getStyleClass().add("task-container");

        this.setContent(this.taskContainer);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.getStyleClass().add("task-scroll-pane");

        refreshTaskContainer();
        edittedTaskBoxListener();
    }

    @Override
    public void updateData(List<Task> tasks) {
        this.tasks = tasks;
        refreshTaskContainer();
    }

    private void refreshTaskContainer() {
        taskContainer.getChildren().clear();
        List<Task> copyOfTasks = this.tasks;
        copyOfTasks = sortMethod.apply(copyOfTasks);
        if (filterByDate != null) {
            copyOfTasks = FilterMethod.bySelectedCalendarDate(tasks, filterByDate);
        }
        copyOfTasks = filterMethod.apply(copyOfTasks);
        if (copyOfTasks.isEmpty()) {
            VBox noActivityLabel = createNoActivityLabel();
            taskContainer.getChildren().add(noActivityLabel);

            return;
        }

        for (Task task : copyOfTasks) {
            HBox taskBox;
            if(edittedTaskBox.get() != null && (int) edittedTaskBox.get().getUserData() == task.getTaskId()) {
                taskBox = edittedTaskBox.get();
            } else {
                taskBox = createTaskBox(task);
            }

            if (task.getIsCompleted()) {
                taskBox.getStyleClass().add("task-box-completed");
            } else {
                taskBox.getStyleClass().clear();
                taskBox.getStyleClass().add("task-box");
            }
            taskContainer.getChildren().add(taskBox);      
        }
    }

    public VBox createNoActivityLabel() {
        Label noActivityLabel = new Label("Tidak Ada Aktivitas");
        noActivityLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: gray");
        noActivityLabel.setMaxWidth(Double.MAX_VALUE);
        noActivityLabel.setMaxHeight(Double.MAX_VALUE);
        noActivityLabel.setWrapText(true);

        VBox labelWrapper = new VBox(noActivityLabel);
        labelWrapper.setAlignment(Pos.CENTER); 
        noActivityLabel.setAlignment(Pos.CENTER); 
        HBox.setHgrow(labelWrapper, Priority.ALWAYS);
        VBox.setVgrow(labelWrapper, Priority.ALWAYS);
        labelWrapper.setPadding(new Insets(-200, 0, 0, 0));

        return labelWrapper;
    }

    private HBox createTaskBox(Task task) {
        HBox taskBox = new HBox(20);
        taskBox.setUserData(task.getTaskId());
        taskBox.getStyleClass().add("task-box"); 
        taskBox.setAlignment(Pos.CENTER_LEFT);
        taskBox.setMinHeight(80);

        CheckBox completeCheckBox = createCheckBox(task);
        completeCheckBox.setOnAction(e -> toggleTaskCompletion(task));

        VBox taskDetails = createTaskDetailContainer(task);
        
        HBox actionButtons = new HBox(20);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
    
        Button editButton = createEditButton();
        editButton.setOnAction(e -> handleEditButton(taskBox, task));
        Button removeButton = createRemoveButton();
        removeButton.setOnAction(e -> handleRemoveTask(task));
        actionButtons.getChildren().addAll(editButton, removeButton);
        
        taskBox.getChildren().addAll(completeCheckBox, taskDetails, actionButtons);
    
        HBox.setHgrow(taskDetails, Priority.ALWAYS);

        return taskBox;
    }

    private CheckBox createCheckBox(Task task) {
        CheckBox completeCheckBox = new CheckBox();
        completeCheckBox.getStyleClass().add("check-box");
        completeCheckBox.setSelected(task.getIsCompleted());
        return completeCheckBox;
    }

    private Button createEditButton() {
        Button editButton = new Button();
        editButton.setGraphic(new FontIcon("fas-pencil-alt"));
        editButton.getStyleClass().add("button-edit");
        return editButton;
    }

    private Button createRemoveButton() {
        Button removeButton = new Button();
        removeButton.setGraphic(new FontIcon("fas-trash"));
        removeButton.getStyleClass().add("button-remove");
        return removeButton;
    }
    
    private VBox createTaskDetailContainer(Task task) {
        VBox taskDetails = new VBox(8);
        taskDetails.setAlignment(Pos.CENTER_LEFT);
        Label taskNameLabel = new Label(task.getNamaTask());
        taskNameLabel.getStyleClass().add("task-name");
        Date deadline = task.getDeadline();
        HBox deadlineContainer =  createDeadlineDetailContainer(deadline, task.getIsCompleted());
        taskDetails.getChildren().addAll(taskNameLabel, deadlineContainer);
        return taskDetails;
    }

    private HBox createDeadlineDetailContainer(Date deadline, Boolean isTaskCompleted) {
        HBox deadlineBox = new HBox(5); 
        deadlineBox.setAlignment(Pos.CENTER_LEFT);
        
        FontIcon clockIcon = new FontIcon("fas-clock");
        clockIcon.getStyleClass().add("clock-icon");
        
        DateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
        String deadlineDetails = deadline != null ? formatter.format(deadline) : "Tidak Ada Deadline";
        Label deadlineLabel = new Label(deadlineDetails);
        deadlineLabel.getStyleClass().add("deadline-label");
        Date today = DayHelper.getDayToday();

        if (!isTaskCompleted && deadline != null && deadline.before(today)) {
            deadlineLabel.getStyleClass().add("deadline-label-overdue");
        }

        if (!isTaskCompleted && deadline != null && deadline.equals(today)) {
            deadlineLabel.getStyleClass().add("deadline-label-today");
        }

        deadlineBox.getChildren().addAll(clockIcon, deadlineLabel);
        return deadlineBox;
    }

    private void handleEditButton(HBox taskBox, Task task) {   
        HBox checkboxContainer = new HBox();
        checkboxContainer.setAlignment(Pos.TOP_CENTER);
        checkboxContainer.setPadding(new Insets(0, 0, 0, 0));
        CheckBox completeCheckBox = createCheckBox(task);
        completeCheckBox.setDisable(true);
        checkboxContainer.getChildren().add(completeCheckBox);
        
        taskBox.getChildren().clear();
        VBox editBox = createEditBox(task);
        ResponsiveHelper.animateResize(taskBox, 200, Duration.seconds(0.3));
        taskBox.getChildren().addAll(checkboxContainer, editBox);
        edittedTaskBox.set(taskBox);
    }

    private VBox createEditBox(Task task) {
        VBox editContainer = new VBox();
        editContainer.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(editContainer, Priority.ALWAYS);
        VBox.setVgrow(editContainer, Priority.ALWAYS);

        HBox inputContainer = new HBox();
        inputContainer.setSpacing(10);
        inputContainer.setAlignment(Pos.CENTER_LEFT);

        TextField editTaskNameField = new TextField(task.getNamaTask());
        editTaskNameField.setId("editTaskNameField");
        editTaskNameField.setPromptText("Nama Aktivitas");
        editTaskNameField.getStyleClass().add("edit-name-field");
        HBox.setHgrow(editTaskNameField, Priority.ALWAYS);
        HBox actionButtons = new HBox(20);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        editTaskNameField.requestFocus();
    
        Button confirmButton = new Button();
        confirmButton.setGraphic(new FontIcon("fas-check"));
        confirmButton.getStyleClass().add("action-button-edit");
    
        Button removeButton = createRemoveButton();
        HBox removeButtonWrapper = new HBox(removeButton);
        removeButtonWrapper.setAlignment(Pos.BOTTOM_RIGHT); 

        Button cancelButton = new Button();
        cancelButton.setGraphic(new FontIcon("fas-times"));
        cancelButton.getStyleClass().add("action-button-edit");

        actionButtons.getChildren().addAll(confirmButton, cancelButton);
        inputContainer.getChildren().addAll(editTaskNameField, actionButtons);

        Label dateLabel = new Label("  Date:  ");
        dateLabel.setId("dateLabel");

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        datePicker.setPromptText("dd/mm/yyyy");
        datePicker.setId("datePicker");

        Date deadline = task.getDeadline();
        if (deadline != null) {
            datePicker.setValue(deadline.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        } else {
            datePicker.setValue(null);
        }

        Button clearButton = new Button("Clear");
        clearButton.setId("clearButton");
        clearButton.getStyleClass().add("modern-blue-button");
        clearButton.setOnAction(e -> clearDatePicker(datePicker));

        HBox datePickerContainer = new HBox(10, dateLabel, datePicker, clearButton);
        datePickerContainer.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox dateContainer = new HBox(10, datePickerContainer, spacer);
        dateContainer.setId("dateContainer");
        dateContainer.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(dateContainer, Priority.ALWAYS);
        VBox.setVgrow(dateContainer, Priority.ALWAYS);

        dateContainer.getChildren().add(removeButtonWrapper);
        editContainer.getChildren().addAll(inputContainer, dateContainer);
        
        removeButton.setOnAction(e -> handleRemoveTask(task));
        cancelButton.setOnAction(e -> handleCancelEditButton());
        confirmButton.setOnAction(e -> handleConfirmEditTask(task, editTaskNameField, datePicker));

        return editContainer;
    }

    private void edittedTaskBoxListener() {
        edittedTaskBox.addListener((observable, oldFlag, newFlag) -> {
            if (newFlag != null) {
                if (oldFlag != null) {
                    refreshTaskContainer();
                }
                
            } 
        });
    }
    
    private void clearDatePicker(DatePicker datePicker) {
        datePicker.setValue(null); 
    }

    public void setSortMethod(Function<List<Task>, List<Task>> method) {
        this.sortMethod = method;
        refreshTaskContainer();
    }

    public void setFilterMethod(Function<List<Task>, List<Task>> method) {
        this.filterMethod = method;
        refreshTaskContainer();
    }

    public void setFilterDate(LocalDate date) {
        this.filterByDate = date;
        refreshTaskContainer();
    }

    private void handleCancelEditButton() {
        edittedTaskBox.set(null);
        refreshTaskContainer();
        isEditting.set(false);
    }

    private void toggleTaskCompletion(Task task) {
        try {
            if (task.getIsCompleted()) {
                dataManager.uncompleteTask(task.getTaskId());
            } else {
                dataManager.completeTask(task.getTaskId());
            }
        } catch (IOException e) {
            showError("Failed to complete task. Try again.");
        }
       
    }


    private void handleRemoveTask(Task task) {
        try {
            dataManager.removeTask(task.getTaskId());
        } catch (IOException e) {
            showError("Failed to remove task. Try again.");
        }
    }

    private void handleConfirmEditTask(Task task, TextField textField, DatePicker datePicker) {
        try {
            String newName = textField.getText(); 
            Date newDeadline = getDateFromDatePickerInput(datePicker);
            dataManager.editTask(task.getTaskId(), newName, newDeadline);
            ResponsiveHelper.animateResize(edittedTaskBox.get(), 80, Duration.seconds(0.3));
            edittedTaskBox.set(null);
            refreshTaskContainer();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } 
        catch (ParseException e) {
            showError("format tangga salah. pakai dd/MM/yyyy.");
        } catch (IOException e) {
            showError("gagal menambahkan aktivitas. coba lagi.");
        }  
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private Date getDateFromDatePickerInput(DatePicker datePicker) throws ParseException {
        String dateText = datePicker.getEditor().getText().trim();
        Date date = null;
        if (!dateText.isEmpty()) {
            date = new SimpleDateFormat("dd/MM/yyyy").parse(dateText);
        }
        return date;
    }
}
