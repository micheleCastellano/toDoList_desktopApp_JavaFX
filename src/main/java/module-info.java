module com.todolist.todolist {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.todolist.controller to javafx.fxml;
    exports com.todolist.controller;
}