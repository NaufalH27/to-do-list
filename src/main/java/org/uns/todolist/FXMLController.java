package org.uns.todolist;
/*
Put header here


 */

import java.net.URL;
import java.util.ResourceBundle;

import org.uns.todolist.service.DataManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class FXMLController implements Initializable {

    private final DataManager dataManager;

    @FXML
    private Label lblOut;

    public FXMLController(DataManager dataManager) {
        this.dataManager = dataManager;
    }
    
    @FXML
    private void btnClickAction(ActionEvent event) {
        lblOut.setText("Hello World!");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
}
