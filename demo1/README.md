# Student Attendance System (Java 21 + JavaFX 21)

## Run
- IntelliJ: open the folder as a Maven project, then run `AttendanceSheet` (or `Launcher`).
- Terminal:  `mvn clean javafx:run`
- Tests:     `mvn test`

Data files are created/used in the folder you start the program from:
`daily_attendance.csv`, `students.csv`, `subjects.txt`.

## Features (screen is unchanged)
- Subject / Date / Week selection, 1-tap Present/Absent, All Present / All Absent
- Add subject, add student, double-click a row to delete a student, Export to CSV
- NEW (invisible until used):
  - Hover a student row -> tooltip with attendance rate for the selected Week (Week 1-4 or whole month)
  - Ctrl+Z -> undo the last tap / All Present / All Absent
  - Click a column header -> sort the table
  - Students and subjects you add/delete are now saved
  - CSV export has an extra "Period Attendance" column

## Structure
- `attendance-view.fxml`  screen layout        - `AttendanceController`  screen logic
- `AttendanceService`     rules + data structures (no JavaFX)
- `AttendanceStorage`     file load/save/export - `AttendanceRecord`, `Student`, `SeedData`
- `src/test/...`          JUnit 5 tests
