package com.example.demo1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Entry point of the Student Attendance System.
 * The screen layout is defined in attendance-view.fxml and loaded with FXMLLoader;
 * AttendanceController handles all user interaction.
 */
public class AttendanceSheet extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(AttendanceSheet.class.getResource("attendance-view.fxml"));
        Scene scene = new Scene(loader.load(), 1100, 850);
        primaryStage.setTitle("Student Attendance System");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
