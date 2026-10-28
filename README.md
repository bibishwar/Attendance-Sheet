# Student Attendance System

## 📌 Project Overview

The **Student Attendance System** is a desktop application developed using Java and JavaFX to manage student attendance efficiently. It provides a simple graphical user interface that allows users to select dates and subjects, view student information, and mark students as present or absent.

The application is designed to make attendance management easier, reduce manual work, and maintain attendance records for future reference.

## 🎯 Project Objectives

- Manage student attendance digitally.
- Display student information in a structured table.
- Mark students as Present or Absent.
- Select attendance dates and subjects.
- Add students and subjects when needed.
- Save attendance records in a CSV file.
- Load previously saved attendance records.
- Provide a simple and user-friendly interface.

## ✨ Features

- **Student Management:** Maintain a list of students with their IDs and names.
- **Date Selection:** Select a specific date to manage attendance.
- **Subject Selection:** Choose a subject before recording attendance.
- **Attendance Marking:** Mark individual students as Present or Absent.
- **Bulk Attendance Actions:** Apply attendance actions to multiple students.
- **Add Students and Subjects:** Add new students and subjects through the application.
- **Attendance Table:** View student information and attendance status in a JavaFX table.
- **CSV Storage:** Save attendance records in `daily_attendance.csv`.
- **Data Loading:** Load saved attendance records when needed.
- **Record Management:** Remove students from the roster using the application's supported interaction.

## 🛠️ Technologies Used

- **Programming Language:** Java
- **GUI Framework:** JavaFX
- **IDE:** IntelliJ IDEA
- **Build Tool:** Maven
- **Data Structures:** ArrayList, ObservableList, HashMap, and arrays
- **Data Storage:** CSV file
- **User Interface Components:** TableView, ComboBox, DatePicker, and Buttons

## 📂 Project Structure

```text
Student-Attendance-System/
│
├── pom.xml
├── README.md
├── daily_attendance.csv
│
└── src/
    └── main/
        └── java/
            └── com/
                └── example/
                    └── demo1/
                        └── AttendanceSheet.java
```

*Note: The structure above represents the main project files. Your actual folder structure may contain additional files.*

## 🧩 Data Structures Used

### 1. ArrayList

An `ArrayList` is used to maintain the student roster. It allows the application to store and manage student objects dynamically.

### 2. ObservableList

An `ObservableList` stores the data displayed in the JavaFX table. It helps update the table when the displayed information changes.

### 3. HashMap

A `HashMap` is used to associate attendance information with a unique key based on the date, subject, and student ID.

This makes it easier to organize and retrieve attendance records.

### 4. String Array

A string array is used when processing CSV records by separating a line into individual values.

## 💾 Data Storage

The application uses a CSV file named `daily_attendance.csv` to store attendance information.

The records can be saved and loaded so that attendance information remains available between application sessions.

Attendance records are associated with:

- Attendance date
- Subject
- Student ID
- Attendance status

The application uses these details to distinguish attendance records for different dates, subjects, and students.

## 🚀 How to Run the Project

### Prerequisites

Before running the application, make sure you have:

- Java Development Kit (JDK)
- IntelliJ IDEA
- Maven
- JavaFX dependencies configured in the project

### Installation Steps

1. Download or clone this repository from GitHub.
2. Open IntelliJ IDEA.
3. Select **Open** and choose the project folder.
4. Allow Maven to load the project dependencies.
5. Check that the correct JDK is selected in the project settings.
6. Open `AttendanceSheet.java`.
7. Run the `AttendanceSheet` class to launch the application.

## 🖥️ Application Workflow

1. Launch the Student Attendance System.
2. Select the attendance date.
3. Choose the subject.
4. View the student roster in the attendance table.
5. Mark students as Present or Absent.
6. Use the available actions to manage students and subjects.
7. Save attendance records to the CSV file.
8. Load the saved records when needed.

## 🔮 Future Improvements

The project could be extended with the following features:

- Attendance percentage calculation.
- Monthly and semester attendance reports.
- Search and filter options for students.
- Export attendance reports to PDF or Excel.
- Database integration using MySQL.
- User login and access control.
- Improved reporting and data visualization.

## 🎓 Project Purpose

This project was developed as an individual academic programming project to apply Java programming, JavaFX interface development, and data structures to a practical student attendance management problem.

## 👨‍💻 Author

**Bibishwar Chakma**

Student Attendance System  
Java and JavaFX Project
