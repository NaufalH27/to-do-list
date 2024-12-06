package org.uns.todolist.ui;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.uns.todolist.models.CalendarEvent;

import javafx.beans.property.IntegerProperty;
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

/*
 * Objek berikut di dapatkan dari projek open source
 * berikut link refrensinya : https://github.com/JKostikiadis/JFXCalendar
 */

public class NavigationCalendar extends VBox {

	private final int PREVIOUS = 0;
	private final int CURRENT = 1;
	private final int NEXT = 2;

	private GridPane navigationCalendarGrid;
	private Button selectedCalendarCell;

	private IntegerProperty currentYearProperty = new SimpleIntegerProperty();
	private StringProperty currentMonthProperty = new SimpleStringProperty();
	private IntegerProperty currentDayProperty = new SimpleIntegerProperty();
	private LocalDate selectedDate;

	private IntegerProperty markedCell = new SimpleIntegerProperty();

	public StringProperty selectedDateProperty = new SimpleStringProperty();

	public NavigationCalendar() {
		// Calendar pane
		setId("navigation_calendar");
		setPadding(new Insets(30, 20, 15, 20));

		// Toolbar pane
		HBox navigationPane = new HBox(10);
		navigationPane.setAlignment(Pos.CENTER); // Center-align everything

		// Initialize current selected date
		selectedDate = LocalDate.now();
		createCalendarGrid();
		getChildren().addAll(navigationPane, navigationCalendarGrid);

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

		Button prevMonthButton = new Button();
		Button nextMonthButton = new Button();

		prevMonthButton.getStyleClass().add("circle_buttom_sm");
		nextMonthButton.getStyleClass().add("circle_buttom_sm");

		prevMonthButton.setGraphic(previousMonthIcon);
		nextMonthButton.setGraphic(nextMonthIcon);

		// Button actions
		prevMonthButton.setOnAction(e -> {
			moveMonthBackwardOnNavCalendar();
			clearSelection();
		});

		nextMonthButton.setOnAction(e -> {
			moveMonthForwardOnNavCalendar();
			clearSelection();
		});

		// Add everything to the navigationPane
		navigationPane.getChildren().addAll(prevMonthButton, dateContainer, nextMonthButton);
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
		refreshCalendar();
		
	}

	public void refreshCalendar() {
		// Remove all the nodes inside the 'calendar'
		navigationCalendarGrid.getChildren().clear();

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
		// used in date display
		int selectedMonthIndex = selectedDate.getMonthValue();
		int lenghtOfSelectedMonth = selectedDate.lengthOfMonth();
		int selectedYear = selectedDate.getYear();

		currentMonthProperty.set(CalendarEvent.MONTHS[selectedMonthIndex - 1]);
		currentYearProperty.set(selectedYear);
		currentDayProperty.set(selectedDate.getDayOfMonth());

		// in order to display the day correctly we need to find
		// the start of our calendar which is not always starts on Monday
		// so find the previous month
		LocalDate prevMonthDate = selectedDate.minusMonths(1);
		int lenghtOfPrevMonth = prevMonthDate.lengthOfMonth();

		// Find the when (day) was the first day of the month ( ex. Friday)
		LocalDate firstOfMonthDate = LocalDate.of(selectedYear, selectedMonthIndex, 1);
		int firstDayIndex = findDayIndex(firstOfMonthDate.getDayOfWeek()) - 1;

		
		// Add the previous month's days
		int row = 1;
		int col = 0;
		for (int i = 0; i <= firstDayIndex; i++) {
			String dayIndexStr = String.valueOf(lenghtOfPrevMonth - firstDayIndex + i);
			Button dayButton = createCalendarCell(dayIndexStr, PREVIOUS);
			dayButton.getStyleClass().add("calendar_cell_inactive");
			navigationCalendarGrid.add(dayButton, col++, row);
			this.adjustButton(dayButton);
		}
	
		// Fill the calendar with days of the current month
		LocalDate currentDate = LocalDate.now();
		int currentMonth = currentDate.getMonthValue();
		int currentDay = currentDate.getDayOfMonth();
	
		for (int i = 1; i <= lenghtOfSelectedMonth; i++) {
			if (col == 7) { // Start a new row after 7 columns
				col = 0;
				row++;
			}
			Button dayButton = createCalendarCell(String.valueOf(i), CURRENT);
			dayButton.getStyleClass().add("calendar_cell_active");
			navigationCalendarGrid.add(dayButton, col++, row);
			this.adjustButton(dayButton);
			if (i == markedCell.get()) {
				// Highlight the selected day
				dayButton.setStyle("-fx-background-color : #c0c0c0 ; -fx-text-fill : black");
				selectedCalendarCell = dayButton;
			}
	
			if (selectedMonthIndex == currentMonth && currentDay == i) {
				dayButton.setId("currentDay");
			}
		}
	
		// Add next month's days until the grid is filled (7 rows, 7 columns)
		int index = 1;
		while (row < 6 || (row == 6 && col < 7)) {
			if (col == 7) { // Start a new row after 7 columns
				col = 0;
				row++;
			}
			Button dayButton = createCalendarCell(String.valueOf(index++), NEXT);
			dayButton.getStyleClass().add("calendar_cell_inactive");
			navigationCalendarGrid.add(dayButton, col++, row);
			this.adjustButton(dayButton);
		}
	   
	}

	public void select(int day) {
		markedCell.set(day);
	}

	public void setSelectedDate(LocalDate selectedDate) {
		this.selectedDate = selectedDate;
	}

	private Button createCalendarCell(String text, int monthIndex) {
		Button button = new Button(text);

		button.setOnAction(e -> {
			clearSelection();

			if (monthIndex == CURRENT) {
				button.setStyle("-fx-background-color : #c0c0c0; -fx-text-fill : black");
				selectedCalendarCell = button;
				markedCell.set(Integer.parseInt(text));
				selectedDate = selectedDate.withDayOfMonth(markedCell.get());
				navigateToCurrent(text);
			} else if (monthIndex == PREVIOUS) {
				markedCell.set(Integer.parseInt(text));
				navigateToPrevious(text);
				selectedDate = selectedDate.withDayOfMonth(markedCell.get());
			} else {
				markedCell.set(Integer.parseInt(text));
				navigateToNext(text);
				selectedDate = selectedDate.withDayOfMonth(markedCell.get());
			}

			selectedDateProperty.set(getSelectedDate());
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
		refreshCalendar();
	}

	private void moveMonthForwardOnNavCalendar() {
		selectedDate = selectedDate.plusMonths(1);
		refreshCalendar();
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

	private void navigateToNext(String text) {
		moveMonthForwardOnNavCalendar();
	}

	private void navigateToPrevious(String text) {
		moveMonthBackwardOnNavCalendar();
	}

	private void navigateToCurrent(String text) {

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

	public LocalDate getLocalDate() {
		return selectedDate;
	}


	private void adjustButton(Button dayButton) {
		GridPane.setHalignment(dayButton, HPos.CENTER); 
		GridPane.setValignment(dayButton, VPos.CENTER);
	}
}