package com.example.demo1;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttendanceStorageTest {

    private static final LocalDate OCT_6 = LocalDate.of(2026, 10, 6);

    private Path tempDir() throws Exception {
        return Files.createTempDirectory("attendance-test");
    }

    @Test
    void firstRunLoadsTheDefaultRosterAndSubjects() throws Exception {
        AttendanceService service = new AttendanceService();
        new AttendanceStorage(tempDir()).load(service);
        assertEquals(18, service.getRoster().size());
        assertEquals("4420250902", service.getRoster().get(0).id());
        assertEquals(6, service.getSubjects().size());
    }

    @Test
    void attendanceSurvivesSaveAndReload() throws Exception {
        Path dir = tempDir();
        AttendanceStorage storage = new AttendanceStorage(dir);
        AttendanceService first = new AttendanceService();
        storage.load(first);
        first.addSubject("Math, Advanced");
        first.setStatus(OCT_6, "Chinese 3", "4420250902", "Absent");
        first.setStatus(OCT_6, "Math, Advanced", "4420250903", "Absent");
        storage.saveAttendance(first);
        storage.saveSubjects(first);
        storage.saveStudents(first);

        AttendanceService second = new AttendanceService();
        storage.load(second);
        assertEquals("Absent", second.getStatus(OCT_6, "Chinese 3", "4420250902"));
        assertEquals("Absent", second.getStatus(OCT_6, "Math, Advanced", "4420250903"));
        assertEquals("Present", second.getStatus(OCT_6, "Chinese 3", "4420250903"));
        assertEquals(7, second.getSubjects().size());
        assertEquals(2, second.getRecords().size());
    }

    @Test
    void addedAndRemovedStudentsArePersisted() throws Exception {
        Path dir = tempDir();
        AttendanceStorage storage = new AttendanceStorage(dir);
        AttendanceService first = new AttendanceService();
        storage.load(first);
        first.addStudent("4420259999", "New Student");
        first.removeStudent("4420250903");
        storage.saveStudents(first);

        AttendanceService second = new AttendanceService();
        storage.load(second);
        assertEquals(18, second.getRoster().size());
        assertEquals("New Student", second.getRoster().get(17).name());
        for (Student s : second.getRoster()) {
            assertTrue(!s.id().equals("4420250903"));
        }
    }

    @Test
    void loadSkipsDamagedLinesAndAcceptsWindowsLineEndings() throws Exception {
        Path dir = tempDir();
        String content = "2026-10-06|Chinese 3|S1,Absent\r\n"
                + "garbage line\r\n"
                + "not-a-date|Chinese 3|S1,Absent\r\n"
                + "2026-10-06|Chinese 3|S2,Maybe\r\n"
                + "2026-10-07|Chinese 3|S2,Present\r\n";
        Files.write(dir.resolve(AttendanceStorage.ATTENDANCE_FILE), content.getBytes(StandardCharsets.UTF_8));
        AttendanceService service = new AttendanceService();
        new AttendanceStorage(dir).load(service);
        assertEquals(2, service.getRecords().size());
        assertEquals("Absent", service.getStatus(OCT_6, "Chinese 3", "S1"));
    }

    @Test
    void exportWritesHeaderRowsAndPeriodAttendance() throws Exception {
        AttendanceService service = new AttendanceService();
        service.addStudent("S1", "Alice");
        service.addStudent("S2", "Doe, John");
        String subject = "Comprehensive Design of Data Structure";
        service.setStatus(OCT_6, subject, "S1", "Absent");

        List<AttendanceRecord> rows = List.of(
                new AttendanceRecord(1, "S1", "Alice", "Absent"),
                new AttendanceRecord(2, "S2", "Doe, John", "Present"));
        Path file = tempDir().resolve("out.csv");
        new AttendanceStorage(tempDir()).exportCsv(file, rows, OCT_6, subject,
                AttendanceService.ALL_WEEKS, service);

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(3, lines.size());
        assertEquals("Index,Student ID,Student Name,Status,Date,Subject,Period Attendance", lines.get(0));
        assertEquals("1,S1,Alice,Absent,2026-10-06,Comprehensive Design of Data Structure,0%", lines.get(1));
        assertEquals("2,S2,\"Doe, John\",Present,2026-10-06,Comprehensive Design of Data Structure,100%",
                lines.get(2));
    }

    @Test
    void theShippedAttendanceFileLoadsWithoutErrors() throws Exception {
        Path real = Path.of(AttendanceStorage.ATTENDANCE_FILE);
        if (!Files.exists(real)) {
            return;
        }
        AttendanceService service = new AttendanceService();
        new AttendanceStorage(Path.of("")).load(service);
        assertTrue(service.getRecords().size() > 0);
    }
}
