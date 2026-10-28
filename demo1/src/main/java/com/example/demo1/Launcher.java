package com.example.demo1;

import javafx.application.Application;

/**
 * Plain main class (does not extend Application). Use it to start the program from an IDE
 * or a jar if "JavaFX runtime components are missing" appears when running AttendanceSheet.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(AttendanceSheet.class, args);
    }
}
