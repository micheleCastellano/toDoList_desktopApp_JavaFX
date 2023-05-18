package com.todolist.controller;

import datamodel.ToDoData;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch();
    }

    public static Stage stage;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main-window.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 500);
        this.stage=stage;
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("To do list");
        stage.setScene(scene);

        stage.show();
    }

    @Override
    public void init() {
        try{
            ToDoData.getInstance().loadToDoItems();
        } catch (IOException e){
            System.out.println(e);
        }
    }

    @Override
    public void stop() {
        try{
            ToDoData.getInstance().storeToDoItems();
        } catch (IOException e){
            System.out.println(e);
        }
    }


}