package org.uns.todolist.ui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.uns.todolist.models.Task;
import org.uns.todolist.service.DataObserver;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

/*
 * Objek berikut di dapatkan dari projek open source
 * berikut link refrensinya : https://github.com/JKostikiadis/JFXCalendar
 */

public class NavigationCalendar extends VBox implements DataObserver {
	private static final String AFTER_TODAY_COLOR = "#A5D8A5";
	private static final String PAST_TODAY_COLOR = "#FF6F61";
	private static final String TODAY_COLOR ="#4285F4";

	private final int PREVIOUS = 0;
	private final int CURRENT = 1;
	private final int NEXT = 2;

	private GridPane navigationCalendarGrid;
	private Button selectedCalendarCell;
	private final HBox navigationPane;

	private final IntegerProperty currentYearProperty = new SimpleIntegerProperty();
	private final StringProperty currentMonthProperty = new SimpleStringProperty();
	private final IntegerProperty currentDayProperty = new SimpleIntegerProperty();
	private LocalDate selectedDate;
	private LocalDate markedDate;

	private final IntegerProperty markedCell = new SimpleIntegerProperty();

	public StringProperty selectedDateProperty = new SimpleStringProperty();

	private Set<LocalDate> taskDates;
	private final ObjectProperty<LocalDate> globalDateState;

	@Override
	public void updateData(List<Task> tasks) {
		updateDates(tasks);
		resetMarkedCell();
		this.refreshCalendar();
	}

	private void updateDates(List<Task> tasks) {
		this.taskDates = tasks.stream()
			.filter(task -> task.getIsCompleted() != null && !task.getIsCompleted()) 
			.map(Task::getDeadline) 
			.filter(Objects::nonNull) 
			.map(deadline -> deadline.toInstant() 
				.atZone(ZoneId.systemDefault())  
				.toLocalDate())  
			.collect(Collectors.toUnmodifiableSet()); 
	}
	

	public NavigationCalendar(List<Task> tasks, ObjectProperty<LocalDate> globalDateState) {
		this.globalDateState = globalDateState;
		updateDates(tasks);
		// Calendar pane
		setId("navigation_calendar");
		setPadding(new Insets(30, 20, 15, 20));

		// Toolbar pane
		this.navigationPane = new HBox(10);
		navigationPane.setAlignment(Pos.CENTER); // Center-align everything

		// Initialize current selected date
		selectedDate = LocalDate.now();
		createCalendarGrid();
		getChildren().addAll(navigationPane, navigationCalendarGrid);
		
	}

	private void createCalendarGrid() {
		navigationCalendarGrid = new GridPane();
		VBox.setVgrow(navigationCalendarGrid, Priority.ALWAYS);
		HBox.setHgrow(navigationCalendarGrid, Priority.ALWAYS);
		for (int i = 0; i < 7; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setHgrow(Priority.ALWAYS);
            navigationCalendarGrid.getColumnConstraints().add(columnConstraints);
        }
		for (int i = 0; i < 7; i++) { 
			RowConstraints rowConstraints = new RowConstraints();
			rowConstraints.setVgrow(Priority.ALWAYS);
			navigationCalendarGrid.getRowConstraints().add(rowConstraints);
		}
		
		navigationCalendarGrid.setPadding(new Insets(10, 0, 0, 0));
		this.refreshCalendar();
	}




	public void refreshCalendar() {
		// Remove all the nodes inside the 'calendar'
		navigationCalendarGrid.getChildren().clear();
		this.refreshNavigationPane();
	
		// Add headers (days) and their tooltips
		String tooltipText[] = CalendarEvent.DAYS_FULL_NAMES;
		String dayText[] = CalendarEvent.DAYS_NAMES_LETTERS;
		for (int col = 0; col < 7; col++) {
			int row = 0;
			Button dayButton = new Button(dayText[col]);
			dayButton.setTooltip(new Tooltip(tooltipText[col]));
			dayButton.getStyleClass().add("flat_button");
			navigationCalendarGrid.add(dayButton, col, row);
			this.adjustButton(dayButton);
		}
	
		// Find selected date informations and update properties
		int selectedMonthIndex = selectedDate.getMonthValue();
		int lengthOfSelectedMonth = selectedDate.lengthOfMonth();
		int selectedYear = selectedDate.getYear();
	
		currentMonthProperty.set(CalendarEvent.MONTHS[selectedMonthIndex - 1]);
		currentYearProperty.set(selectedYear);
		currentDayProperty.set(selectedDate.getDayOfMonth());
	
		// In order to display the day correctly we need to find
		// the start of our calendar, which doesn't always start on Monday.
		// So, find the previous month
		LocalDate prevMonthDate = selectedDate.minusMonths(1);
		int lengthOfPrevMonth = prevMonthDate.lengthOfMonth();
	
		// Find the first day of the month
		LocalDate firstOfMonthDate = LocalDate.of(selectedYear, selectedMonthIndex, 1);
		int firstDayIndex = findDayIndex(firstOfMonthDate.getDayOfWeek()) - 1;
	
		// Get today's date
		LocalDate currentDate = LocalDate.now();
		// Add the previous month's days
		int row = 1;
		int col = 0;
		for (int i = 0; i <= firstDayIndex; i++) {
			int dayIndex = lengthOfPrevMonth - firstDayIndex + i;
			String dayIndexStr = String.valueOf(dayIndex);
			Button dayButton = createCalendarCell(dayIndexStr, PREVIOUS);
			dayButton.getStyleClass().add("calendar_cell_inactive");
			LocalDate currDateIteration = LocalDate.of(prevMonthDate.getYear(), prevMonthDate.getMonthValue(), dayIndex);
			if(taskDates.contains(currDateIteration)) {
				String currentStyle = dayButton.getStyle();
				if(currDateIteration.isBefore(currentDate)) {
					dayButton.setStyle(currentStyle + "-fx-background-color:" + lightenHexColor(PAST_TODAY_COLOR, 0.8));
				} else if (currDateIteration.isAfter(currentDate)) {
					dayButton.setStyle(currentStyle + "-fx-background-color:" + lightenHexColor(AFTER_TODAY_COLOR, 0.8));
				}
			}
			
			navigationCalendarGrid.add(dayButton, col++, row);
			this.adjustButton(dayButton);
		}

	
		// Fill the calendar with days of the current month
		for (int i = 1; i <= lengthOfSelectedMonth; i++) {
			if (col == 7) { // Start a new row after 7 columns
				col = 0;
				row++;
			}
			LocalDate currDateIteration = LocalDate.of(selectedYear, selectedMonthIndex, i);
			Button dayButton = createCalendarCell(String.valueOf(i), CURRENT);
			dayButton.getStyleClass().add("calendar_cell_active");
			if(taskDates.contains(currDateIteration)) {
				String currentStyle = dayButton.getStyle();
				if(currDateIteration.isBefore(currentDate)) {
					dayButton.setStyle(currentStyle + "-fx-background-color:" + PAST_TODAY_COLOR + "; -fx-opacity: 1");
				} else if (currDateIteration.isAfter(currentDate)) {
					dayButton.setStyle(currentStyle + "-fx-background-color:" + AFTER_TODAY_COLOR);
				}
			}
			
			navigationCalendarGrid.add(dayButton, col++, row);
			this.adjustButton(dayButton);

			if (currDateIteration.equals(currentDate)) {
				dayButton.setId("currentDay");
			}

			if (taskDates.contains(currDateIteration) && currDateIteration.isEqual(currentDate)) {
				String currentStyle = dayButton.getStyle();
				dayButton.setStyle(currentStyle + "-fx-background-color:  #FFD54F; -fx-text-fill : black;");
			}
	
			if (i == markedCell.get() && markedDate != null) {
				// Highlight the selected day
				dayButton.setStyle("-fx-background-color : #c0c0c0 ; -fx-text-fill : black;");
				selectedCalendarCell = dayButton;
			}
	
		}
	
		// Add next month's days until the grid is filled (7 rows, 7 columns)
		LocalDate nextMonth = selectedDate.plusMonths(1);
		int currentDateIncrement = 1;
		while (row < 6 || (row == 6 && col < 7)) {
			if (col == 7) { // Start a new row after 7 columns
				col = 0;
				row++;
			}
			Button dayButton = createCalendarCell(String.valueOf(currentDateIncrement), NEXT);
			dayButton.getStyleClass().add("calendar_cell_inactive");
			LocalDate currentDateIteration = LocalDate.of(nextMonth.getYear(),nextMonth.getMonthValue(), currentDateIncrement);
			if (taskDates.contains(currentDateIteration)) {
				String currentStyle = dayButton.getStyle();
				if(currentDateIteration.isBefore(currentDate)) {
					dayButton.setStyle(currentStyle + "-fx-background-color:" + lightenHexColor(PAST_TODAY_COLOR, 0.8));
				} else if (currentDateIteration.isAfter(currentDate)) {
					dayButton.setStyle(currentStyle + "-fx-background-color:" + lightenHexColor(AFTER_TODAY_COLOR, 0.8));
				}
			}
			navigationCalendarGrid.add(dayButton, col, row);
			this.adjustButton(dayButton);
			currentDateIncrement++;
			col++;
		}
	}


	private void refreshNavigationPane() {
		// Tool bar controls initialization
		Label yearLabel = new Label();
		Label monthLabel = new Label();

		yearLabel.textProperty().bind(currentYearProperty.asString()); // Bind year
		monthLabel.textProperty().bind(currentMonthProperty);          // Bind month

		yearLabel.setId("calendar_year_label");
		monthLabel.setId("calendar_month_label");

		// Stack year and month labels vertically
		VBox dateContainer = new VBox(5, yearLabel, monthLabel);
		dateContainer.setAlignment(Pos.CENTER);

		// Navigation buttons
		FontIcon previousMonthIcon = new FontIcon(FontAwesomeSolid.ANGLE_LEFT);
		FontIcon nextMonthIcon = new FontIcon(FontAwesomeSolid.ANGLE_RIGHT);
		previousMonthIcon.setIconSize(20);
		nextMonthIcon.setIconSize(20);

		Button prevMonthButton = new Button();
		Button nextMonthButton = new Button();

		prevMonthButton.getStyleClass().add("circle_buttom_sm");
		nextMonthButton.getStyleClass().add("circle_buttom_sm");

		prevMonthButton.setGraphic(previousMonthIcon);
		nextMonthButton.setGraphic(nextMonthIcon);
		LocalDate currentDay = LocalDate.now();

		LocalDate currentMonthFirstDay = selectedDate.with(TemporalAdjusters.firstDayOfMonth());
		Optional<LocalDate> nearestBeforeCurrentMonth = taskDates.stream()
                .filter(date -> date.isBefore(currentMonthFirstDay)) 
                 .min(Comparator.comparingLong(date -> 
						Math.abs(ChronoUnit.DAYS.between(date, currentMonthFirstDay)) 
					));
		
		nearestBeforeCurrentMonth.ifPresent(date -> {
			if (date.isBefore(currentDay)) {
				previousMonthIcon.setIconColor(Paint.valueOf(PAST_TODAY_COLOR));
			} else {
				previousMonthIcon.setIconColor(Paint.valueOf(AFTER_TODAY_COLOR));
			} 

			if (date.getYear() == currentDay.getYear() && date.getMonthValue() == currentDay.getMonthValue()) {
				previousMonthIcon.setIconColor(Paint.valueOf(TODAY_COLOR));
			}
		});

		LocalDate currentMonthLastDay = selectedDate.with(TemporalAdjusters.lastDayOfMonth());
		Optional<LocalDate> nearestAfterCurrentMonth = taskDates.stream()
                .filter(date -> date.isAfter(currentMonthLastDay)) 
                 .min(Comparator.comparingLong(date -> 
						Math.abs(ChronoUnit.DAYS.between(date, currentMonthLastDay)) 
					));

		nearestAfterCurrentMonth.ifPresent(date -> {
			if (date.isBefore(currentDay)) {
				nextMonthIcon.setIconColor(Paint.valueOf(PAST_TODAY_COLOR));
			} else {
				nextMonthIcon.setIconColor(Paint.valueOf(AFTER_TODAY_COLOR));
			}

			if (date.getYear() == currentDay.getYear() && date.getMonthValue() == currentDay.getMonthValue()) {
				nextMonthIcon.setIconColor(Paint.valueOf(TODAY_COLOR));
			}
		});

		// Button actions
		prevMonthButton.setOnAction(e -> {
			moveMonthBackwardOnNavCalendar();
			refreshCalendar();
			clearSelection();
		});

		nextMonthButton.setOnAction(e -> {
			moveMonthForwardOnNavCalendar();
			refreshCalendar();
			clearSelection();
		});

		navigationPane.getChildren().clear();
		navigationPane.getChildren().addAll(prevMonthButton, dateContainer, nextMonthButton);
	}
			
	public void select(int day) {
		markedCell.set(day);
	}

	public void setSelectedDate(LocalDate selectedDate) {
		this.selectedDate = selectedDate;
	}

	private Button createCalendarCell(String text, int monthIndex) {
		Button button = new Button(text);
		button.setPrefWidth(28); 
		button.setPrefHeight(25); 
		button.setStyle("-fx-alignment: center; -fx-font-size: 11;");


		button.setOnAction(e -> {
			clearSelection();

			if (monthIndex == CURRENT) {
				button.setStyle("-fx-background-color : #c0c0c0; -fx-text-fill : black");
				selectedCalendarCell = button;
				markedCell.set(Integer.parseInt(text));
			} else if (monthIndex == PREVIOUS) {
				markedCell.set(Integer.parseInt(text));
				moveMonthBackwardOnNavCalendar();
			} else {
				markedCell.set(Integer.parseInt(text));
				moveMonthForwardOnNavCalendar();
			}
			selectedDate = selectedDate.withDayOfMonth(markedCell.get());
			markedDate = selectedDate.withDayOfMonth(markedCell.get());
			refreshCalendar();
			globalDateState.set(markedDate);
			
			// Notify for the change ( We are using a InvalidationListener )
			// TODO : Change to StringBinding or something in order to use
			// ChangeListener instead.
			selectedDateProperty.get();

		});

		return button;
	}

	private void clearSelection() {
		if (selectedCalendarCell != null) {
			selectedCalendarCell.setStyle("");
		}
	}

	private void moveMonthBackwardOnNavCalendar() {
		selectedDate = selectedDate.minusMonths(1);
	}

	private void moveMonthForwardOnNavCalendar() {
		selectedDate = selectedDate.plusMonths(1);
	}

	private int findDayIndex(DayOfWeek dayOfWeek) {
		switch (dayOfWeek) {
		case MONDAY:
			return 0;
		case TUESDAY:
			return 1;
		case WEDNESDAY:
			return 2;
		case THURSDAY:
			return 3;
		case FRIDAY:
			return 4;
		case SATURDAY:
			return 5;
		case SUNDAY:
			return 6;
		}
		return 0;
	}


	public StringProperty getSelectedDateProperty() {
		return selectedDateProperty;
	}

	public String getSelectedDate() {
		String date = markedCell.get() + " "
				+ CalendarEvent.MONTHS[selectedDate.getMonthValue() - 1].substring(0, 3) + " "
				+ selectedDate.getYear();
		return date;
	}

	public LocalDate getMarkedDate() {
		return markedDate;
	}

	public void resetMarkedCell() {
		markedDate = null;
		refreshCalendar();
	}


	private void adjustButton(Button dayButton) {
		GridPane.setHalignment(dayButton, HPos.CENTER); 
		GridPane.setValignment(dayButton, VPos.CENTER);
	}


	private static String lightenHexColor(String hex, double factor) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        r = (int) Math.min(255, r + (255 - r) * factor);
        g = (int) Math.min(255, g + (255 - g) * factor);
        b = (int) Math.min(255, b + (255 - b) * factor);
        return String.format("#%02x%02x%02x", r, g, b);
    }
}
	
