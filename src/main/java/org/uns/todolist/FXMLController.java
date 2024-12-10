package org.uns.todolist;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.uns.todolist.models.Task;
import org.uns.todolist.service.DataManager;
import org.uns.todolist.service.FilterMethod;
import org.uns.todolist.service.SortingMethod;
import org.uns.todolist.ui.Calendar;
import org.uns.todolist.ui.Navigator;
import org.uns.todolist.ui.TaskCreationPanel;
import org.uns.todolist.ui.TaskViewPanel;
import org.uns.todolist.ui.constant.FilterState;
import org.uns.todolist.ui.constant.SortState;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class FXMLController {
    @FXML
    private VBox taskPanel;
    @FXML
    private VBox calendarContainer;
    @FXML
    private VBox navigationPanel;
    @FXML
    private Label Month;
    @FXML
    private Label date;
    @FXML
    private Label greeting;

    private final TaskCreationPanel creationPanel;
    private final Calendar navCalendar;
    private final TaskViewPanel viewPanel;
    private final Navigator navigator;
    private final DataManager dataManager;
    private static final double SCROLL_SPEED = 500;

    //global state
    private final ObjectProperty<LocalDate> dateState = new SimpleObjectProperty<>(null);
    private final ObjectProperty<FilterState> filterState = new SimpleObjectProperty<>(FilterState.SHOW_ALL);
    private final ObjectProperty<SortState> sortState = new SimpleObjectProperty<>(SortState.DEFAULT);

    
    public FXMLController(DataManager dataManager) {

        //setup data
        this.dataManager = dataManager;
        List<Task> initialTasksData = dataManager.getAllTasks();
        
        //setup Task creation field
        this.creationPanel = new TaskCreationPanel(dataManager);
        VBox.setVgrow(creationPanel, Priority.ALWAYS);
        HBox.setHgrow(creationPanel, Priority.ALWAYS);
                
        //setup calendar
        this.navCalendar = new Calendar(initialTasksData, dateState);
        VBox.setVgrow(navCalendar, Priority.ALWAYS);
		HBox.setHgrow(navCalendar, Priority.ALWAYS);
        dataManager.addObserver(navCalendar);

        //setup task container
        this.viewPanel = new TaskViewPanel(initialTasksData, this.dataManager);
        VBox.setVgrow(viewPanel, Priority.ALWAYS);
        HBox.setHgrow(viewPanel, Priority.ALWAYS);
        dataManager.addObserver(viewPanel);

        //setup navigator
        this.navigator = new Navigator(dateState, filterState, sortState);
        VBox.setVgrow(navigator, Priority.ALWAYS);
        dataManager.addObserver(navigator);
    }

    private void stateListener() {
        dateState.addListener((observable, oldValue, newValue) -> {
            viewPanel.setFilterDate(newValue);
            if (newValue == null) {
                navCalendar.resetMarkedCell();
            }
            navigator.clearFilterButtonSelection();
        });

        filterState.addListener((observable, oldValue, newValue) -> {
            viewPanel.removeEdittedTaskBox();
            switch (newValue) {
                case SHOW_ALL -> viewPanel.setFilterMethod(FilterMethod::noFilter);
                case COMPLETED -> viewPanel.setFilterMethod(FilterMethod::ByCompleted);
                case INCOMPLETE -> viewPanel.setFilterMethod(FilterMethod::ByIncomplete);
            }
        });
        
        sortState.addListener((observable, oldValue, newValue) -> {
            viewPanel.removeEdittedTaskBox();
            switch (newValue) {
                case DEFAULT ->viewPanel.setSortMethod(SortingMethod::defaultMethod);
                case RECENT -> viewPanel.setSortMethod(SortingMethod::byRecentlyCreated);
                case OLDEST -> viewPanel.setSortMethod(SortingMethod::byOldestCreated);
                case NAME -> viewPanel.setSortMethod(SortingMethod::byName);
            }
        });
    }


    private void setupEditAndCreationBinding(BooleanProperty isCreating, 
                                             ObjectProperty<HBox> taskBoxEdittingListener
                                            ) {
        isCreating.addListener((obs, oldFlag, newFlag) -> {
            if (newFlag) {
                viewPanel.handleCancelEditButton();
                
            }
        });

        taskBoxEdittingListener.addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                isCreating.set(false);
                creationPanel.handleShrinkingFIeld();
            }
        });
    }

    @FXML
    public void initialize() {
        //placing ui to the apropriate location
        calendarContainer.getChildren().add(this.navCalendar);
        navigationPanel.getChildren().add(this.navigator);
        taskPanel.getChildren().addAll(this.creationPanel, this.viewPanel);
        setupEditAndCreationBinding( creationPanel.getCreationState(), viewPanel.getEdittingState());
        
        //title update
        adjustScrollSpeed(SCROLL_SPEED);
        updateDate();
        updateGreeting();

        //setting up global state listener
        stateListener();
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

    private void adjustScrollSpeed(double speed) {
        viewPanel.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        viewPanel.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        viewPanel.setOnScroll(event -> {
            double deltaY = event.getDeltaY() * speed;
            double newVvalue = viewPanel.getVvalue() - deltaY;
            newVvalue = Math.min(Math.max(newVvalue, 0.0), 1.0);
            viewPanel.setVvalue(newVvalue);
            event.consume();
        });
    }
}

