package com.todolist.controller;

import datamodel.ToDoData;
import datamodel.ToDoItem;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class DialogController {

    @FXML
    private TextField shortDescriptionField;

    @FXML
    private TextArea detailsArea;

    @FXML
    private DatePicker deadlinePicker;

    public ToDoItem processResult(){
        String shortDescription = shortDescriptionField.getText().trim();
        String details = detailsArea.getText().trim();
        LocalDate deadlineValue = deadlinePicker.getValue();
        ToDoItem newItem = new ToDoItem(shortDescription,details,deadlineValue);
        ToDoData.getInstance().addToDoItem(newItem);
        return newItem;
    }

    public void prepareDialogForEdit(ToDoItem item){
        shortDescriptionField.setText(item.getShortDescription());
        detailsArea.setText(item.getDetails());
        deadlinePicker.setValue(item.getDeadline());
    }

    public ToDoItem processEdit(ToDoItem oldItem){
        String shortDescription = shortDescriptionField.getText().trim();
        String details = detailsArea.getText().trim();
        LocalDate deadlineValue = deadlinePicker.getValue();
        ToDoItem newItem = new ToDoItem(shortDescription,details,deadlineValue);
        ToDoData.getInstance().updateToDoItem(oldItem,newItem);
        return newItem;
    }
}
