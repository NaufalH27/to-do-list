package org.uns.todolist.ui;

import java.time.LocalDate;

import org.uns.todolist.service.FilterMethod;
import org.uns.todolist.service.SortingMethod;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class Navigator extends VBox {

    private final Button showAllButton;
    private final Button defaultButton;
    private final Button recentButton;
    private final Button oldestButton;
    private final Button nameButton;
    private final Button completedButton;
    private final Button incompleteButton;

    //state control
    private final ObjectProperty<LocalDate> globalDateState;
    private final ObjectProperty<FilterState> globalFilterState;
    private final ObjectProperty<SortState> globalSortState;

    public Navigator(ObjectProperty<LocalDate> globalDateState, 
                     ObjectProperty<FilterState> globalFilterState, 
                     ObjectProperty<SortState> globalSortState) {
        this.globalDateState = globalDateState;
        this.globalFilterState = globalFilterState;
        this.globalSortState = globalSortState;

        this.setStyle("-fx-padding: 0 0 0 30;");
        this.setMaxWidth(440);
        
        showAllButton = new Button("Show All");
        showAllButton.getStyleClass().add("show-all-button");

        HBox showAllBox = new HBox();
        showAllBox.setPadding(new Insets(0,0,20, 0)); 
        showAllBox.setAlignment(Pos.CENTER);
        showAllBox.getChildren().add(showAllButton);

        Label sortLabel = new Label("SORT");
        sortLabel.getStyleClass().add("section-title");

        HBox sortBox = new HBox(8);
        sortBox.setStyle("-fx-padding: 8 0 0 8;");
        sortBox.setAlignment(Pos.CENTER_LEFT);

        defaultButton = new Button("Default");
        defaultButton.getStyleClass().add("sort-button");

        recentButton = new Button("Recent");
        recentButton.getStyleClass().add("sort-button");

        oldestButton = new Button("Oldest");
        oldestButton.getStyleClass().add("sort-button");

        nameButton = new Button("Name");
        nameButton.getStyleClass().add("sort-button");

        sortBox.getChildren().addAll(defaultButton, recentButton, oldestButton, nameButton);

        // Spacer Region
        Region spacer = new Region();
        spacer.setPrefHeight(40);
        VBox.setVgrow(spacer, Priority.NEVER);

        // Filter Section
        Label filterLabel = new Label("FILTER");
        filterLabel.getStyleClass().add("section-title");

        FlowPane filterPane = new FlowPane(8, 8);
        filterPane.setStyle("-fx-padding: 8 0 0 8;");
        filterPane.setAlignment(Pos.CENTER_LEFT);

        completedButton = new Button("Completed");
        completedButton.getStyleClass().add("filter-button");

        incompleteButton = new Button("Incomplete");
        incompleteButton.getStyleClass().add("filter-button");

        filterPane.getChildren().addAll(completedButton, incompleteButton);

        showAllButton.getStyleClass().add("selected");
        defaultButton.getStyleClass().add("selected");

        this.getChildren().addAll(showAllBox, sortLabel, sortBox, spacer, filterLabel, filterPane);
        buttonNavigationListener();
    }


    private void buttonNavigationListener() {  
        defaultButton.setOnAction(e -> {
            clearSortButtonSelection();
            defaultButton.getStyleClass().add("selected");
            globalSortState.set(SortState.DEFAULT);
            });
        recentButton.setOnAction(e -> {
            clearSortButtonSelection();
            recentButton.getStyleClass().add("selected");
            globalSortState.set(SortState.RECENT);
        });
        oldestButton.setOnAction(e -> {
            clearSortButtonSelection();
            oldestButton.getStyleClass().add("selected");
            globalSortState.set(SortState.OLDEST);
        });
        nameButton.setOnAction(e -> {
            clearSortButtonSelection();
            nameButton.getStyleClass().add("selected");
            globalSortState.set(SortState.NAME);
        });
        

        //filter
        showAllButton.setOnAction(e -> {
            clearFilterButtonSelection();
            globalFilterState.set(FilterState.SHOW_ALL);
            globalDateState.set(null);
            clearFilterButtonSelection();       
            showAllButton.getStyleClass().add("selected");
        });
        completedButton.setOnAction(e -> {
            clearFilterButtonSelection();
            completedButton.getStyleClass().add("selected");
            globalFilterState.set(FilterState.COMPLETED);
        });
        incompleteButton.setOnAction(e ->  {
            clearFilterButtonSelection();
            incompleteButton.getStyleClass().add("selected");
            globalFilterState.set(FilterState.INCOMPLETE);
        });
    }
   

    private void clearSortButtonSelection() {
        defaultButton.getStyleClass().remove("selected");
        recentButton.getStyleClass().remove("selected");
        oldestButton.getStyleClass().remove("selected");
        nameButton.getStyleClass().remove("selected");
    }

    public void clearFilterButtonSelection() {
        showAllButton.getStyleClass().remove("selected");
        completedButton.getStyleClass().remove("selected");
        incompleteButton.getStyleClass().remove("selected");
    }


}
