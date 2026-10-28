package com.example.demo1;

/**
 * One row of the attendance table (a student's status for the selected date and subject).
 * The table columns read the values through the public getters (PropertyValueFactory).
 */
public class AttendanceRecord {
    private final int index;
    private final String studentId;
    private final String studentName;
    private String status;

    public AttendanceRecord(int index, String studentId, String studentName, String status) {
        this.index = index;
        this.studentId = studentId;
        this.studentName = studentName;
        this.status = status;
    }

    public int getIndex() {
        return index;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String newStatus) {
        this.status = newStatus;
    }
}
