package com.todolist.controller;

import datamodel.ToDoData;
import datamodel.ToDoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {

    @FXML
    private ListView<ToDoItem> todoListView;

    @FXML
    private TextArea textAreaView;
    @FXML
    private Label deadlineLabel;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private ContextMenu listContextMenu;

    @FXML
    private ToggleButton filterToggleButton;

    @FXML
    private FilteredList<ToDoItem> filteredList;

    private Predicate<ToDoItem> wantAllItems;
    private Predicate<ToDoItem> wantTodaysItems;

    @FXML
    private TextField searchTextField;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private HBox windowBar;



    public void initialize(){

        makeStageDragable();

        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ToDoItem>() {
            @Override
            public void changed(ObservableValue<? extends ToDoItem> observableValue, ToDoItem oldValue, ToDoItem newValue) {
                if (newValue != null){
                    ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("d MMMM yyyy");

                    StringBuilder sb = new StringBuilder("DUE DATE: ");
                    sb.append(df.format(item.getDeadline()));
                    sb.append("\n\n\nSHORT DESCRIPTION: \n");
                    sb.append(item.getShortDescription());
                    sb.append("\n\n\nDETAILS: \n");
                    sb.append(item.getDetails());

                    textAreaView.setText(sb.toString());
                }
            }
        });

        wantAllItems = new Predicate<ToDoItem>() {
            @Override
            public boolean test(ToDoItem toDoItem) {
                return true;
            }
        };

        wantTodaysItems = new Predicate<ToDoItem>() {
            @Override
            public boolean test(ToDoItem toDoItem) {
                return (toDoItem.getDeadline().equals(LocalDate.now()));
            }
        };

        filteredList = new FilteredList<ToDoItem>(ToDoData.getInstance().getToDoItems(), wantAllItems);

        SortedList<ToDoItem> sortedList = new SortedList<ToDoItem>(filteredList,
                new Comparator<ToDoItem>() {
            @Override
            public int compare(ToDoItem o1, ToDoItem o2) {
                return o1.getDeadline().compareTo(o2.getDeadline());
            }
        });


        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();



        todoListView.setCellFactory(new Callback<ListView<ToDoItem>, ListCell<ToDoItem>>() {
            @Override
            public ListCell<ToDoItem> call(ListView<ToDoItem> param) {
                ListCell<ToDoItem> cell = new ListCell<ToDoItem>(){
                    @Override
                    protected void updateItem(ToDoItem item, boolean empty){
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                            getStyleClass().setAll("cell");
                        } else {
                            setText(item.getShortDescription());
                            LocalDate deadline = item.getDeadline();

                            if (deadline.isAfter(LocalDate.now())){
                                getStyleClass().setAll("cell");
                            }else if (deadline.isBefore(LocalDate.now())){
                                getStyleClass().setAll("cell","cell--overdue");
                            } else {
                                getStyleClass().setAll("cell","cell--today");
                            }
                        }
                    }
                };
                cell.getStyleClass().add("cell");

                cell.emptyProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasEmpty, Boolean isNowEmpty) {
                        if(isNowEmpty) {
                            cell.setContextMenu(null);
                        } else {
                            cell.setContextMenu(listContextMenu);
                        }
                    }
                });

                return cell;
            }
        });

        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        MenuItem editMenuItem = new MenuItem("Edit");
        listContextMenu.getItems().addAll(editMenuItem,deleteMenuItem);

        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });

        editMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    showeditItemDialog();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @FXML
    public void showeditItemDialog() throws IOException {
        ToDoItem oldItem = todoListView.getSelectionModel().getSelectedItem();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Edit To-Do Item");
        dialog.setHeaderText("Use this dialog to edit this item");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("toDoItemDialog.fxml"));
        dialog.getDialogPane().setContent(fxmlLoader.load());

        DialogController controller = fxmlLoader.getController();
        controller.prepareDialogForEdit(oldItem);

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            ToDoItem newItem = controller.processEdit(oldItem);
            todoListView.getSelectionModel().select(newItem);
        }
    }

    @FXML
    public void showNewItemDialog() throws IOException{
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add new To-Do Item");
        dialog.setHeaderText("Use this dialog to create a new item");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("toDoItemDialog.fxml"));
        dialog.getDialogPane().setContent(fxmlLoader.load());

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            DialogController controller = fxmlLoader.getController();
            ToDoItem newItem = controller.processResult();
            todoListView.getSelectionModel().select(newItem);
        }
    }

    private void makeStageDragable(){
        windowBar.setOnMousePressed((event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        windowBar.setOnMouseDragged((event)->{
            Main.stage.setX(event.getScreenX()- xOffset);
            Main.stage.setY(event.getScreenY()- yOffset);
            Main.stage.setOpacity(0.8f);

        });

        windowBar.setOnDragDone((event)->{
            Main.stage.setOpacity(1.0f);
        });

        windowBar.setOnMouseReleased((event)->{
            Main.stage.setOpacity(1.0f);
        });
    }


    @FXML
    public void searchItem(){
        String searchShortDescription = searchTextField.getText().trim();
        ToDoItem item = ToDoData.getInstance().searchFromDescription(searchShortDescription);
        if (item!=null) {
            todoListView.getSelectionModel().select(item);
        }
        searchTextField.clear();
    }

    @FXML
    public void handleKeyPressedDelete(KeyEvent keyEvent){
        ToDoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null){
            if(keyEvent.getCode().equals(KeyCode.DELETE)) {
                deleteItem(selectedItem);
            }
        }
    }

    @FXML
    public void handleKeyPressedSearch(KeyEvent keyEvent){

            if(keyEvent.getCode().equals(KeyCode.ENTER)) {
                searchItem();
            }

    }



    @FXML
    public void handleBinButton(){
        ToDoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null){
            deleteItem(selectedItem);
        }
    }

    public void deleteItem(ToDoItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Todo item");
        alert.setHeaderText("Delete item: " + item.getShortDescription());
        alert.setContentText("Are you sure? \nPress OK to confirm, or CANCEL to back out.");
        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && (result.get() == ButtonType.OK)){
            ToDoData.getInstance().deleteToDoItem(item);
        }
    }
    @FXML
    public void handleFilterButton(){
        ToDoItem selectedItem = todoListView.getSelectionModel().getSelectedItem();
        if(filterToggleButton.isSelected()){
            filterToggleButton.setText("All items");
            filteredList.setPredicate(wantTodaysItems);
            if(filteredList.isEmpty()){
                textAreaView.clear();
            } else if (filteredList.contains(selectedItem)) {
                todoListView.getSelectionModel().select(selectedItem);
            } else {
                todoListView.getSelectionModel().selectFirst();
            }
        } else {
            filterToggleButton.setText("Today's items");
            filteredList.setPredicate(wantAllItems);
            todoListView.getSelectionModel().select(selectedItem);
        }
    }
    @FXML
    public void handleExit(){
        Platform.exit();
    }

    @FXML
    public void handleMinimize(){
        Main.stage.setIconified(true);
    }

}