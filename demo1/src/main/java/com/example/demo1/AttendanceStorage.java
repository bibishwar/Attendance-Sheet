package com.example.demo1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reads and writes the data files (all in the working directory, like the original program):
 *  - daily_attendance.csv  lines "date|subject|studentId,Status"
 *  - students.csv          lines "studentId,Student Name"
 *  - subjects.txt          one subject per line
 */
public class AttendanceStorage {

    public static final String ATTENDANCE_FILE = "daily_attendance.csv";
    public static final String STUDENTS_FILE = "students.csv";
    public static final String SUBJECTS_FILE = "subjects.txt";

    private final Path dir;

    public AttendanceStorage() {
        this(Path.of(""));
    }

    public AttendanceStorage(Path dir) {
        this.dir = dir;
    }

    // ------------------------------------------------------------------ load

    /** Fills the service. Missing students/subjects files fall back to the default seed data. */
    public void load(AttendanceService service) throws IOException {
        Path studentsFile = dir.resolve(STUDENTS_FILE);
        List<Student> students = Files.exists(studentsFile) ? readStudents(studentsFile) : SeedData.students();
        for (Student s : students) {
            service.addStudent(s.id(), s.name());
        }

        Path subjectsFile = dir.resolve(SUBJECTS_FILE);
        List<String> subjects = Files.exists(subjectsFile)
                ? Files.readAllLines(subjectsFile, StandardCharsets.UTF_8) : SeedData.subjects();
        for (String s : subjects) {
            service.addSubject(s);
        }

        Path attendanceFile = dir.resolve(ATTENDANCE_FILE);
        if (Files.exists(attendanceFile)) {
            for (String line : Files.readAllLines(attendanceFile, StandardCharsets.UTF_8)) {
                parseAttendanceLine(service, line);
            }
        }
    }

    private List<Student> readStudents(Path file) throws IOException {
        List<Student> list = new ArrayList<>();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            int comma = line.indexOf(',');
            if (comma > 0) {
                list.add(new Student(line.substring(0, comma).trim(), line.substring(comma + 1).trim()));
            }
        }
        return list;
    }

    private void parseAttendanceLine(AttendanceService service, String line) {
        int comma = line.lastIndexOf(',');
        if (comma <= 0) {
            return;
        }
        String key = line.substring(0, comma);
        String status = line.substring(comma + 1).trim();
        int first = key.indexOf('|');
        int last = key.lastIndexOf('|');
        if (first <= 0 || last <= first) {
            return;
        }
        try {
            LocalDate date = LocalDate.parse(key.substring(0, first));
            String subject = key.substring(first + 1, last);
            String studentId = key.substring(last + 1);
            service.loadRecord(date, subject, studentId, status);
        } catch (DateTimeParseException ignored) {
            // skip a damaged line
        }
    }

    // ------------------------------------------------------------------ save

    public void saveAttendance(AttendanceService service) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> e : new TreeMap<>(service.getRecords()).entrySet()) {
            lines.add(e.getKey() + "," + e.getValue());
        }
        Files.write(dir.resolve(ATTENDANCE_FILE), lines, StandardCharsets.UTF_8);
    }

    public void saveStudents(AttendanceService service) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Student s : service.getRoster()) {
            lines.add(s.id() + "," + s.name());
        }
        Files.write(dir.resolve(STUDENTS_FILE), lines, StandardCharsets.UTF_8);
    }

    public void saveSubjects(AttendanceService service) throws IOException {
        Files.write(dir.resolve(SUBJECTS_FILE), service.getSubjects(), StandardCharsets.UTF_8);
    }

    // ---------------------------------------------------------------- export

    /** Export to CSV: same columns as before plus the attendance rate of the selected period. */
    public void exportCsv(Path file, List<AttendanceRecord> rows, LocalDate date, String subject,
                          String weekLabel, AttendanceService service) throws IOException {
        AttendanceService.DateRange range = AttendanceService.weekRange(date, weekLabel);
        List<String> lines = new ArrayList<>();
        lines.add("Index,Student ID,Student Name,Status,Date,Subject,Period Attendance");
        for (AttendanceRecord r : rows) {
            AttendanceService.Stats stats =
                    service.attendanceStats(subject, r.getStudentId(), range.from(), range.to());
            lines.add(r.getIndex() + "," + csv(r.getStudentId()) + "," + csv(r.getStudentName()) + ","
                    + r.getStatus() + "," + date + "," + csv(subject) + ","
                    + AttendanceService.formatPercent(stats));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    private static String csv(String value) {
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
