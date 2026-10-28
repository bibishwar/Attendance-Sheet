package com.example.demo1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttendanceServiceTest {

    private static final String SUBJECT = "Comprehensive Design of Data Structure";
    private static final LocalDate OCT_6 = LocalDate.of(2026, 10, 6);    // Week 1
    private static final LocalDate OCT_13 = LocalDate.of(2026, 10, 13);  // Week 2
    private static final LocalDate OCT_22 = LocalDate.of(2026, 10, 22);  // Week 4

    private AttendanceService service;

    @BeforeEach
    void setUp() {
        service = new AttendanceService();
        service.addStudent("S1", "Alice");
        service.addStudent("S2", "Bob");
        service.addStudent("S3", "Cara");
    }

    // ---------------------------------------------------- students (ArrayList + HashSet)

    @Test
    void addStudentAppendsToRoster() {
        assertEquals(AttendanceService.AddResult.ADDED, service.addStudent("S4", "Dan"));
        assertEquals(4, service.getRoster().size());
        assertEquals("S4", service.getRoster().get(3).id());
    }

    @Test
    void duplicateIdIsRejected() {
        assertEquals(AttendanceService.AddResult.DUPLICATE_ID, service.addStudent("S1", "Someone Else"));
        assertEquals(3, service.getRoster().size());
    }

    @Test
    void emptyIdOrNameIsRejected() {
        assertEquals(AttendanceService.AddResult.EMPTY_FIELD, service.addStudent("", "Dan"));
        assertEquals(AttendanceService.AddResult.EMPTY_FIELD, service.addStudent("S4", "   "));
        assertEquals(3, service.getRoster().size());
    }

    @Test
    void idWithCommaOrBarIsRejected() {
        assertEquals(AttendanceService.AddResult.INVALID_ID, service.addStudent("A,B", "Dan"));
        assertEquals(AttendanceService.AddResult.INVALID_ID, service.addStudent("A|B", "Dan"));
    }

    @Test
    void removeStudentUpdatesRosterAndAllowsReuseOfId() {
        assertTrue(service.removeStudent("S2"));
        assertEquals(2, service.getRoster().size());
        assertFalse(service.removeStudent("S2"));
        assertEquals(AttendanceService.AddResult.ADDED, service.addStudent("S2", "Bob again"));
    }

    // ----------------------------------------------------------- subjects

    @Test
    void subjectsAreTrimmedAndDuplicatesRejected() {
        assertTrue(service.addSubject("  Chinese 3  "));
        assertFalse(service.addSubject("Chinese 3"));
        assertFalse(service.addSubject("   "));
        assertFalse(service.addSubject("A|B"));
        assertEquals(1, service.getSubjects().size());
        assertEquals("Chinese 3", service.getSubjects().get(0));
    }

    // ------------------------------------------------- attendance (HashMap)

    @Test
    void defaultStatusIsPresent() {
        assertEquals("Present", service.getStatus(OCT_6, SUBJECT, "S1"));
    }

    @Test
    void setStatusIsStoredPerDateSubjectAndStudent() {
        service.setStatus(OCT_6, SUBJECT, "S1", "Absent");
        assertEquals("Absent", service.getStatus(OCT_6, SUBJECT, "S1"));
        assertEquals("Present", service.getStatus(OCT_6, SUBJECT, "S2"));
        assertEquals("Present", service.getStatus(OCT_13, SUBJECT, "S1"));
        assertEquals("Present", service.getStatus(OCT_6, "Other subject", "S1"));
    }

    @Test
    void bulkAbsentThenBulkPresent() {
        service.setBulk(OCT_6, SUBJECT, "Absent");
        for (Student s : service.getRoster()) {
            assertEquals("Absent", service.getStatus(OCT_6, SUBJECT, s.id()));
        }
        service.setBulk(OCT_6, SUBJECT, "Present");
        for (Student s : service.getRoster()) {
            assertEquals("Present", service.getStatus(OCT_6, SUBJECT, s.id()));
        }
    }

    // -------------------------------------------------- undo (stack, LIFO)

    @Test
    void undoWithNothingToUndoReturnsNull() {
        assertFalse(service.canUndo());
        assertNull(service.undo());
    }

    @Test
    void undoRestoresDefaultStatusAndRemovesRecord() {
        service.setStatus(OCT_6, SUBJECT, "S1", "Absent");
        assertTrue(service.canUndo());
        assertNotNull(service.undo());
        assertEquals("Present", service.getStatus(OCT_6, SUBJECT, "S1"));
        assertEquals(0, service.getRecords().size());
    }

    @Test
    void undoIsLastInFirstOut() {
        service.setStatus(OCT_6, SUBJECT, "S1", "Absent");
        service.setStatus(OCT_6, SUBJECT, "S1", "Present");
        service.setStatus(OCT_6, SUBJECT, "S1", "Absent");
        service.undo();
        assertEquals("Present", service.getStatus(OCT_6, SUBJECT, "S1"));
        service.undo();
        assertEquals("Absent", service.getStatus(OCT_6, SUBJECT, "S1"));
        service.undo();
        assertEquals("Present", service.getStatus(OCT_6, SUBJECT, "S1"));
        assertNull(service.undo());
    }

    @Test
    void undoOfBulkRestoresMixedPreviousStatuses() {
        service.setStatus(OCT_6, SUBJECT, "S1", "Absent");
        service.setBulk(OCT_6, SUBJECT, "Present");
        service.undo();
        assertEquals("Absent", service.getStatus(OCT_6, SUBJECT, "S1"));
        assertEquals("Present", service.getStatus(OCT_6, SUBJECT, "S2"));
        assertEquals(1, service.getRecords().size());
    }

    // ------------------------------------------ week range + algorithm (TreeMap)

    @Test
    void weekRangesInOctober2026() {
        AttendanceService.DateRange w1 = AttendanceService.weekRange(OCT_13, "Week 1");
        assertEquals(LocalDate.of(2026, 10, 1), w1.from());
        assertEquals(LocalDate.of(2026, 10, 7), w1.to());
        AttendanceService.DateRange w2 = AttendanceService.weekRange(OCT_13, "Week 2");
        assertEquals(LocalDate.of(2026, 10, 8), w2.from());
        assertEquals(LocalDate.of(2026, 10, 14), w2.to());
        AttendanceService.DateRange w3 = AttendanceService.weekRange(OCT_13, "Week 3");
        assertEquals(LocalDate.of(2026, 10, 15), w3.from());
        assertEquals(LocalDate.of(2026, 10, 21), w3.to());
        AttendanceService.DateRange w4 = AttendanceService.weekRange(OCT_13, "Week 4");
        assertEquals(LocalDate.of(2026, 10, 22), w4.from());
        assertEquals(LocalDate.of(2026, 10, 31), w4.to());
        AttendanceService.DateRange all = AttendanceService.weekRange(OCT_13, AttendanceService.ALL_WEEKS);
        assertEquals(LocalDate.of(2026, 10, 1), all.from());
        assertEquals(LocalDate.of(2026, 10, 31), all.to());
    }

    @Test
    void week4OfFebruaryEndsOnThe28th() {
        AttendanceService.DateRange w4 = AttendanceService.weekRange(LocalDate.of(2026, 2, 10), "Week 4");
        assertEquals(LocalDate.of(2026, 2, 22), w4.from());
        assertEquals(LocalDate.of(2026, 2, 28), w4.to());
    }

    private void recordThreeSessions() {
        service.setStatus(OCT_6, SUBJECT, "S1", "Absent");
        service.setStatus(OCT_13, SUBJECT, "S1", "Present");
        service.setStatus(OCT_22, SUBJECT, "S1", "Absent");
    }

    @Test
    void attendanceRateOverTheWholeMonth() {
        recordThreeSessions();
        AttendanceService.DateRange month = AttendanceService.weekRange(OCT_13, AttendanceService.ALL_WEEKS);
        AttendanceService.Stats s1 = service.attendanceStats(SUBJECT, "S1", month.from(), month.to());
        assertEquals(1, s1.present());
        assertEquals(3, s1.total());
        assertEquals(33, s1.percent());
        AttendanceService.Stats s2 = service.attendanceStats(SUBJECT, "S2", month.from(), month.to());
        assertEquals(3, s2.present());
        assertEquals(3, s2.total());
        assertEquals(100, s2.percent());
    }

    @Test
    void attendanceRateRespectsTheWeekFilter() {
        recordThreeSessions();
        AttendanceService.DateRange w1 = AttendanceService.weekRange(OCT_13, "Week 1");
        AttendanceService.Stats a = service.attendanceStats(SUBJECT, "S1", w1.from(), w1.to());
        assertEquals(0, a.present());
        assertEquals(1, a.total());
        AttendanceService.DateRange w3 = AttendanceService.weekRange(OCT_13, "Week 3");
        AttendanceService.Stats b = service.attendanceStats(SUBJECT, "S1", w3.from(), w3.to());
        assertEquals(0, b.total());
        assertEquals("N/A", AttendanceService.formatPercent(b));
    }

    @Test
    void sessionsOfOneSubjectDoNotAffectAnother() {
        recordThreeSessions();
        AttendanceService.DateRange month = AttendanceService.weekRange(OCT_13, AttendanceService.ALL_WEEKS);
        AttendanceService.Stats other = service.attendanceStats("Chinese 3", "S1", month.from(), month.to());
        assertEquals(0, other.total());
    }

    @Test
    void undoRemovesTheSessionWhenItsLastRecordIsGone() {
        service.setStatus(OCT_6, SUBJECT, "S1", "Absent");
        service.undo();
        AttendanceService.DateRange month = AttendanceService.weekRange(OCT_6, AttendanceService.ALL_WEEKS);
        assertEquals(0, service.attendanceStats(SUBJECT, "S1", month.from(), month.to()).total());
    }

    @Test
    void sessionIsCountedOncePerDateEvenWithManyRecords() {
        service.setBulk(OCT_6, SUBJECT, "Absent");
        AttendanceService.DateRange month = AttendanceService.weekRange(OCT_6, AttendanceService.ALL_WEEKS);
        assertEquals(1, service.attendanceStats(SUBJECT, "S1", month.from(), month.to()).total());
        service.undo();
        assertEquals(0, service.attendanceStats(SUBJECT, "S1", month.from(), month.to()).total());
    }

    @Test
    void describeRateTextForTooltip() {
        recordThreeSessions();
        String text = service.describeRate(SUBJECT, "S1", OCT_13, AttendanceService.ALL_WEEKS);
        assertEquals("Whole month (1 Oct - 31 Oct)\nPresent 1 of 3 sessions (33%)", text);
        String empty = service.describeRate(SUBJECT, "S1", OCT_13, "Week 3");
        assertEquals("Week 3 (15 Oct - 21 Oct)\nNo attendance sessions recorded in this period.", empty);
    }
}
