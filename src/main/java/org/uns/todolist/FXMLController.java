package org.uns.todolist;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import org.kordamp.ikonli.javafx.FontIcon;
import org.uns.todolist.helper.DayHelper;
import org.uns.todolist.models.Task;
import org.uns.todolist.service.DataManager;
import org.uns.todolist.service.FilterMethod;
import org.uns.todolist.service.SortingMethod;
import org.uns.todolist.ui.DateInputField;
import org.uns.todolist.ui.FilterIdentifier;
import org.uns.todolist.ui.NavigationCalendar;
import org.uns.todolist.ui.SortIdentifier;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class FXMLController {
    
    @FXML
    private TextField taskNameField;
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
    private VBox addTaskForm;
    @FXML
    private Label Month;
    @FXML
    private Label date;
    @FXML
    private Label greeting;


    private final NavigationCalendar navCalendar;
    private final ObjectProperty<StateFlag> stateFlag = new SimpleObjectProperty<>(StateFlag.FREE);
    private final ObjectProperty<HBox> edittedTaskBox = new SimpleObjectProperty<>(null);
    private double savedVValue = 0;
    private final DataManager dataManager;
    private static final double SCROLL_SPEED = 500;
    private Function<List<Task>, List<Task>> sortMethod = SortingMethod::defaultMethod;
    private Function<List<Task>, List<Task>> filterMethod = FilterMethod::defaultMethod;
    private final ObjectProperty<LocalDate> filterByDate = new SimpleObjectProperty<>(null);
    private final ObjectProperty<SortIdentifier> sortName = new SimpleObjectProperty<>(SortIdentifier.DEFAULT);
    private final ObjectProperty<FilterIdentifier> filterName = new SimpleObjectProperty<>(FilterIdentifier.SHOW_ALL);

    
    public FXMLController(DataManager dataManager) {
        this.dataManager = dataManager;
        this.navCalendar = new NavigationCalendar(dataManager.getAllTasks(), this);
        dataManager.addListener(navCalendar);
        VBox.setVgrow(navCalendar, Priority.ALWAYS);
		HBox.setHgrow(navCalendar, Priority.ALWAYS);
    }
 

    @FXML
    public void initialize() {
        //one time
        calendarContainer.getChildren().add(0,navCalendar);
        cancelTaskButton.setGraphic(new FontIcon("fas-plus"));
        showAllButton.getStyleClass().add("selected");
        defaultButton.getStyleClass().add("selected");
        
        //dynamic
        taskNameField.setFocusTraversable(false);
        taskContainerListener();
        addTaskFieldListener();
        adjustScrollSpeed(SCROLL_SPEED);
        flagListener();
        refreshTaskContainer();
        updateDate();
        updateGreeting();
        NavigationControlListener();
        buttonNavigationListener();
        navCalendar.refreshCalendar(); 
    }


   

    @FXML
    private void addTaskFieldListener() {
        taskNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                stateFlag.set(StateFlag.CREATE);
            } 
        });
        cancelTaskButton.setOnAction(event -> {
            if (stateFlag.get() == StateFlag.CREATE) {
                stateFlag.set(StateFlag.FREE); 
            } else {
                stateFlag.set(StateFlag.CREATE); 
            }
        });
    }


    private void flagListener() {
        stateFlag.addListener((observable, oldFlag, newFlag) -> {
            //create task state
            if (newFlag == StateFlag.CREATE) {  
                animateResize(addContainer, 200, Duration.seconds(0.3));
                taskNameField.setPromptText("Nama Aktivitas");
                cancelTaskButton.setGraphic(new FontIcon("fas-times"));
                Button addTaskButton = new Button("ADD");
                addTaskButton.getStyleClass().add("add-task-button"); 
                HBox addButtonContainer = new HBox(addTaskButton);
                addButtonContainer.setAlignment(Pos.BOTTOM_RIGHT); 
                HBox dateInputField = DateInputField.createDateInputField();
                dateInputField.getChildren().add(addButtonContainer);
                addTaskForm.getChildren().add(dateInputField);
                DatePicker datePicker = (DatePicker) addTaskForm.lookup("#datePicker");
                addTaskButton.setOnAction(e -> handleAddTask(datePicker));
                taskNameField.requestFocus();
            } else if (oldFlag == StateFlag.CREATE && newFlag != StateFlag.CREATE) {
                animateResize(addContainer, 80, Duration.seconds(0.3));
                Node dateContainer = addTaskForm.lookup("#dateContainer");
                if (dateContainer != null) {
                    addTaskForm.getChildren().remove(dateContainer);
                } 
                cancelTaskButton.setGraphic(new FontIcon("fas-plus"));
                taskNameField.clear();
                taskNameField.setPromptText("Tambah Aktivitas...");
            }
            //edit task state
            if (newFlag == StateFlag.EDIT) {
                animateResize(edittedTaskBox.get(), 200, Duration.seconds(0.3));
            } else if (oldFlag == StateFlag.EDIT && newFlag != StateFlag.EDIT) {
                animateResize(edittedTaskBox.get(), 80, Duration.seconds(0.3));
                edittedTaskBox.set(null);
                refreshTaskContainer();
            }
        });
    }
    
    @FXML
    private void handleAddTask(DatePicker datePicker) {
        try{
            String taskName = taskNameField.getText().trim();
            Date deadline = getDateFromDatePickerInput(datePicker);
            dataManager.addTask(taskName, deadline);
            this.stateFlag.set(StateFlag.FREE);
            refreshTaskContainer();
        } catch (IOException e) {
            showError("gagal menambahkan aktivitas. coba lagi.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (ParseException e) {
            showError("format tangga salah. pakai dd/MM/yyyy.");
        } 
    }

    private void refreshTaskContainer() {
        taskContainer.getChildren().clear();
        List<Task> tasks = dataManager.getAllTasks();
        tasks = sortMethod.apply(tasks);
        if (filterByDate.get() != null) {
            tasks = FilterMethod.bySelectedCalendarDate(tasks, filterByDate.get());
        }
        tasks = filterMethod.apply(tasks);
        for (Task task : tasks) {
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
        taskScrollPane.setVvalue(savedVValue);
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

    private void handleEditButton(HBox taskBox, Task task) {   
        HBox checkboxContainer = new HBox();
        checkboxContainer.setAlignment(Pos.TOP_CENTER);
        checkboxContainer.setPadding(new Insets(0, 0, 0, 0));
        CheckBox completeCheckBox = new CheckBox();
        completeCheckBox.getStyleClass().add("check-box");
        completeCheckBox.setSelected(task.getIsCompleted());
        completeCheckBox.setOnAction(e -> toggleTaskCompletion(task));
        checkboxContainer.getChildren().add(completeCheckBox);
        
        taskBox.getChildren().clear();
        VBox editBox = createEditBox(task);
        animateResize(taskBox, 200, Duration.seconds(0.3));
        taskBox.getChildren().addAll(checkboxContainer, editBox);
        edittedTaskBox.set(taskBox);
    }

    private void handleCancelEditButton() {
        stateFlag.set(StateFlag.FREE);
    }

    private void handleConfirmEditTask(Task task, TextField textField, DatePicker datePicker) {
        try {
            String newName = textField.getText(); 
            Date newDeadline = getDateFromDatePickerInput(datePicker);
            dataManager.editTask(task.getTaskId(), newName, newDeadline);
            animateResize(edittedTaskBox.get(), 80, Duration.seconds(0.3));
            stateFlag.set(StateFlag.FREE);
        } catch (ParseException e) {
            showError("format tangga salah. pakai dd/MM/yyyy.");
        } catch (IOException e) {
            showError("gagal menambahkan aktivitas. coba lagi.");
        } 
        
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void updateDate() {
        LocalDate currentDate = LocalDate.now();
        String monthName = currentDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();
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

    private Date getDateFromDatePickerInput(DatePicker datePicker) throws ParseException {
        String dateText = datePicker.getEditor().getText().trim();
        Date date = null;
        if (!dateText.isEmpty()) {
            date = new SimpleDateFormat("dd/MM/yyyy").parse(dateText);
        }
        return date;
    }

    private void taskContainerListener() {
        taskScrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            savedVValue = newValue.doubleValue(); 
        });

        taskContainer.getChildren().addListener((ListChangeListener<Node>) change -> {
            Platform.runLater(() -> {
                while (change.next()) {
                    if (change.wasAdded() && taskContainer.getChildren().size() > 1) {
                        // Remove the "Tidak Ada Aktivitas" label if it exists
                        taskContainer.getChildren().removeIf(node -> 
                            node instanceof StackPane && 
                            ((StackPane) node).getChildren().stream()
                                .anyMatch(child -> child instanceof Label && "Tidak Ada Aktivitas".equals(((Label) child).getText()))
                        );
                    }

                    if (taskContainer.getChildren().isEmpty()) {
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
                        labelWrapper.setPadding(new Insets(-70, 0, 0, 0));
                        taskContainer.getChildren().add(labelWrapper);
                    }
                }
            });
        });
        
        edittedTaskBox.addListener((observable, oldFlag, newFlag) -> {
            if (newFlag != null) {
                if (oldFlag != null) {
                    refreshTaskContainer();
                }
                TextField editTaskNameField = (TextField) edittedTaskBox.get().lookup("#editTaskNameField");
                if (editTaskNameField != null) {
                    editTaskNameField.requestFocus();
                    editTaskNameField.positionCaret(editTaskNameField.getText().length());  
                } 
                stateFlag.set(StateFlag.EDIT);
            } 
        });
    }

    private void adjustScrollSpeed(double speed) {
        taskScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        taskScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        taskScrollPane.setOnScroll(event -> {
            double deltaY = event.getDeltaY() * speed;
            double newVvalue = taskScrollPane.getVvalue() - deltaY;
            newVvalue = Math.min(Math.max(newVvalue, 0.0), 1.0);
            taskScrollPane.setVvalue(newVvalue);
            event.consume();
        });
    }

    private void animateResize(Region box, double newHeight, Duration duration) {
        Timeline timeline = new Timeline();
        KeyValue maxHeightKeyValue = new KeyValue(box.maxHeightProperty(), newHeight);
        KeyValue minHeightKeyValue = new KeyValue(box.minHeightProperty(), newHeight);
        KeyFrame keyFrame = new KeyFrame(duration, maxHeightKeyValue, minHeightKeyValue);    
        timeline.getKeyFrames().add(keyFrame);    
        timeline.play();
    }

    private HBox createTaskBox(Task task) {
        HBox taskBox = new HBox(20);
        taskBox.setUserData(task.getTaskId());
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
    
        FontIcon clockIcon = new FontIcon("fas-clock");
        clockIcon.getStyleClass().add("clock-icon");
        Label deadlineLabel = new Label(task.getDeadline() != null
                ? new SimpleDateFormat("d MMM yyyy").format(task.getDeadline())
                : "Tidak Ada Deadline");
        deadlineLabel.getStyleClass().add("deadline-label");
        HBox deadlineBox = new HBox(5); 
        deadlineBox.setAlignment(Pos.CENTER_LEFT);
        deadlineBox.getChildren().addAll(clockIcon, deadlineLabel);

        taskDetails.getChildren().addAll(taskNameLabel, deadlineBox);
        HBox actionButtons = new HBox(20);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
    
        Button editButton = new Button();
        editButton.setGraphic(new FontIcon("fas-pencil-alt"));
        editButton.getStyleClass().add("button-edit");
        editButton.setOnAction(e -> handleEditButton(taskBox, task));
    
        Button removeButton = new Button();
        removeButton.setGraphic(new FontIcon("fas-trash"));
        removeButton.getStyleClass().add("button-remove");
        removeButton.setOnAction(e -> handleRemoveTask(task));
    
        actionButtons.getChildren().addAll(editButton, removeButton);
    
        taskBox.setMinHeight(80);
        taskBox.getChildren().addAll(completeCheckBox, taskDetails, actionButtons);
    
        HBox.setHgrow(taskDetails, Priority.ALWAYS);
        Date today = DayHelper.getDayToday();

        if (!task.getIsCompleted() && task.getDeadline() != null && task.getDeadline().before(today)) {
            deadlineLabel.getStyleClass().add("deadline-label-overdue");
        }

        if (!task.getIsCompleted() && task.getDeadline() != null && task.getDeadline().equals(today)) {
            deadlineLabel.getStyleClass().add("deadline-label-today");
        }

        return taskBox;
    }

    private VBox createEditBox(Task task) {
        VBox editContainer = new VBox();
        editContainer.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        HBox.setHgrow(editContainer, Priority.ALWAYS);
        VBox.setVgrow(editContainer, Priority.ALWAYS);

        HBox inputContainer = new HBox();
        inputContainer.setSpacing(10);
        inputContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField editTaskNameField = new TextField(task.getNamaTask());
        editTaskNameField.setId("editTaskNameField");
        editTaskNameField.setPromptText("Nama Aktivitas");
        editTaskNameField.getStyleClass().add("edit-name-field");
        HBox.setHgrow(editTaskNameField, Priority.ALWAYS);
        HBox actionButtons = new HBox(20);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
    
        Button confirmButton = new Button();
        confirmButton.setGraphic(new FontIcon("fas-check"));
        confirmButton.getStyleClass().add("action-button-edit");
    
        Button removeButton = new Button();
        removeButton.setGraphic(new FontIcon("fas-trash"));
        removeButton.getStyleClass().add("action-button-edit-trash");
        HBox RemoveButtonContainer = new HBox(removeButton);
        RemoveButtonContainer.setAlignment(Pos.BOTTOM_RIGHT); 

        Button cancelButton = new Button();
        cancelButton.setGraphic(new FontIcon("fas-times"));
        cancelButton.getStyleClass().add("action-button-edit");

        actionButtons.getChildren().addAll(confirmButton, cancelButton);
        inputContainer.getChildren().addAll(editTaskNameField, actionButtons);

        HBox dateEdit = DateInputField.createDateInputField();
        dateEdit.getChildren().add(RemoveButtonContainer);
        editContainer.getChildren().addAll(inputContainer, dateEdit);
        DatePicker datePicker = (DatePicker) editContainer.lookup("#datePicker");
        if (task.getDeadline() != null) {
            datePicker.setValue(task.getDeadline().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        } else {
            datePicker.setValue(null);
        }
        removeButton.setOnAction(e -> handleRemoveTask(task));
        cancelButton.setOnAction(e -> handleCancelEditButton());
        confirmButton.setOnAction(e -> handleConfirmEditTask(task, editTaskNameField, datePicker));

        return editContainer;
    }


    @FXML
    private Button showAllButton;
    @FXML
    private Button defaultButton;
    @FXML
    private Button recentButton;
    @FXML
    private Button oldestButton;
    @FXML
    private Button nameButton;
    @FXML
    private Button completedButton;
    @FXML
    private Button incompleteButton;


    private void buttonNavigationListener() {  
        defaultButton.setOnAction(e -> sortName.set(SortIdentifier.DEFAULT));
        recentButton.setOnAction(e -> sortName.set(SortIdentifier.NEWEST));
        oldestButton.setOnAction(e -> sortName.set(SortIdentifier.OLDEST));
        nameButton.setOnAction(e -> sortName.set(SortIdentifier.NAME));
        
        showAllButton.setOnAction(e -> filterName.set(FilterIdentifier.SHOW_ALL));
        completedButton.setOnAction(e -> filterName.set(FilterIdentifier.COMPLETED));
        incompleteButton.setOnAction(e -> filterName.set(FilterIdentifier.INCOMPLETE));
    }

    private void setSortMethod(Function<List<Task>, List<Task>> method) {
        this.sortMethod = method;
        refreshTaskContainer();
    }

    private void setFilterMethod(Function<List<Task>, List<Task>> method) {
        this.filterMethod = method;
        refreshTaskContainer();
    }

    public void NavigationControlListener() {
        sortName.addListener((observable, oldValue, newValue) -> {
            clearSortButtonSelection();
            
            if (newValue == SortIdentifier.NEWEST) {
                recentButton.getStyleClass().add("selected");
                setSortMethod(SortingMethod::byRecentlyCreated);
            } else if (newValue == SortIdentifier.OLDEST) {
                oldestButton.getStyleClass().add("selected");
                setSortMethod(SortingMethod::byOldestCreated);
            } else if (newValue == SortIdentifier.NAME) {
                nameButton.getStyleClass().add("selected");
                setSortMethod(SortingMethod::byName);
            } else {
                defaultButton.getStyleClass().add("selected");
                setSortMethod(SortingMethod::defaultMethod);
            }
        });
    
        filterName.addListener((observable, oldValue, newValue) -> {
            clearFilterButtonSelection();
            switch (newValue) {
                case COMPLETED:
                    completedButton.getStyleClass().add("selected");
                    setFilterMethod(FilterMethod::ByCompleted);
                    break;
                case INCOMPLETE:
                    incompleteButton.getStyleClass().add("selected");
                    setFilterMethod(FilterMethod::ByIncomplete);
                    break;     
                case SHOW_ALL:
                    setFilterMethod(FilterMethod::defaultMethod);
                    if (oldValue != FilterIdentifier.RESET) {
                        filterByDate.set(null);
                        navCalendar.resetMarkedCell();
                        clearFilterButtonSelection();
                    } 
                    showAllButton.getStyleClass().add("selected");
                    refreshTaskContainer(); 
                    break;
                case DATE:
                    if (oldValue != FilterIdentifier.SHOW_ALL) {
                        filterName.set(oldValue);
                    } 
                    break;
            }
        });

        filterByDate.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) { 
                filterName.set(FilterIdentifier.RESET);
                filterName.set(FilterIdentifier.SHOW_ALL);
                filterName.set(FilterIdentifier.DATE);
            } 

        });
    }
    
    private void clearSortButtonSelection() {
        defaultButton.getStyleClass().remove("selected");
        recentButton.getStyleClass().remove("selected");
        oldestButton.getStyleClass().remove("selected");
        nameButton.getStyleClass().remove("selected");
    }

    private void clearFilterButtonSelection() {
        showAllButton.getStyleClass().remove("selected");
        completedButton.getStyleClass().remove("selected");
        incompleteButton.getStyleClass().remove("selected");
    }
   
    public void reportDateCalendarChange(LocalDate date) {
        filterByDate.set(date);
    }


 
}

