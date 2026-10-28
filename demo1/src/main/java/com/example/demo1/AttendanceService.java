package com.example.demo1;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * All attendance logic, with no JavaFX code, so it can be unit-tested on its own.
 *
 * Data structures used here:
 *  1. ArrayList   roster                 - ordered list of students (table order)
 *  2. HashSet     idIndex                - O(1) duplicate-ID check
 *  3. HashMap     attendance             - "date|subject|id" -> status, O(1) lookup
 *  4. TreeMap     sessions (per subject) - date -> number of records, sorted by date
 *                                          (range queries for the Week filter)
 *  5. ArrayDeque  undoStack              - LIFO stack for the Undo feature
 */
public class AttendanceService {

    public static final String PRESENT = "Present";
    public static final String ABSENT = "Absent";
    public static final String ALL_WEEKS = "All Weeks (Month View)";

    private static final int MAX_UNDO = 100;
    private static final DateTimeFormatter SHORT_DATE =
            DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);

    public enum AddResult { ADDED, DUPLICATE_ID, EMPTY_FIELD, INVALID_ID }

    /** One changed record: the status it had before (null = it had no stored record). */
    public record Change(LocalDate date, String subject, String studentId, String oldStatus) {
    }

    /** One undoable operation (a single tap, or a whole All Present / All Absent). */
    public record UndoAction(LocalDate date, String subject, List<Change> changes) {
    }

    /** Present count and number of sessions in a period. */
    public record Stats(int present, int total) {
        public int percent() {
            return total == 0 ? 0 : (int) Math.round(present * 100.0 / total);
        }
    }

    public record DateRange(LocalDate from, LocalDate to) {
    }

    private final List<Student> roster = new ArrayList<>();
    private final Set<String> idIndex = new HashSet<>();
    private final List<String> subjects = new ArrayList<>();
    private final Map<String, String> attendance = new HashMap<>();
    private final Map<String, TreeMap<LocalDate, Integer>> sessions = new HashMap<>();
    private final Deque<UndoAction> undoStack = new ArrayDeque<>();

    // ------------------------------------------------------------------ keys

    public static String makeKey(LocalDate date, String subject, String studentId) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "|" + subject + "|" + studentId;
    }

    // -------------------------------------------------------------- students

    public AddResult addStudent(String id, String name) {
        String sId = id == null ? "" : id.trim();
        String sName = name == null ? "" : name.trim();
        if (idIndex.contains(sId)) {
            return AddResult.DUPLICATE_ID;
        }
        if (sId.isEmpty() || sName.isEmpty()) {
            return AddResult.EMPTY_FIELD;
        }
        if (sId.indexOf(',') >= 0 || sId.indexOf('|') >= 0) {
            return AddResult.INVALID_ID;
        }
        roster.add(new Student(sId, sName));
        idIndex.add(sId);
        return AddResult.ADDED;
    }

    public boolean removeStudent(String id) {
        if (!idIndex.remove(id)) {
            return false;
        }
        roster.removeIf(s -> s.id().equals(id));
        return true;
    }

    public List<Student> getRoster() {
        return Collections.unmodifiableList(roster);
    }

    // -------------------------------------------------------------- subjects

    public boolean addSubject(String name) {
        String s = name == null ? "" : name.trim();
        if (s.isEmpty() || s.indexOf('|') >= 0 || subjects.contains(s)) {
            return false;
        }
        subjects.add(s);
        return true;
    }

    public List<String> getSubjects() {
        return Collections.unmodifiableList(subjects);
    }

    // ------------------------------------------------------------ attendance

    /** A student with no stored record is Present by default. */
    public String getStatus(LocalDate date, String subject, String studentId) {
        return attendance.getOrDefault(makeKey(date, subject, studentId), PRESENT);
    }

    public void setStatus(LocalDate date, String subject, String studentId, String status) {
        requireStatus(status);
        String old = put(date, subject, studentId, status);
        pushUndo(new UndoAction(date, subject,
                List.of(new Change(date, subject, studentId, old))));
    }

    /** All Present / All Absent for the whole roster, undone as one step. */
    public void setBulk(LocalDate date, String subject, String status) {
        requireStatus(status);
        List<Change> changes = new ArrayList<>();
        for (Student s : roster) {
            String old = put(date, subject, s.id(), status);
            changes.add(new Change(date, subject, s.id(), old));
        }
        if (!changes.isEmpty()) {
            pushUndo(new UndoAction(date, subject, changes));
        }
    }

    /** Loads a saved record without creating an undo step. Invalid statuses are ignored. */
    public void loadRecord(LocalDate date, String subject, String studentId, String status) {
        if (PRESENT.equals(status) || ABSENT.equals(status)) {
            put(date, subject, studentId, status);
        }
    }

    public Map<String, String> getRecords() {
        return Collections.unmodifiableMap(attendance);
    }

    // ------------------------------------------------------------------ undo

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /** Reverts the most recent change (LIFO). Returns it, or null if there is nothing to undo. */
    public UndoAction undo() {
        UndoAction action = undoStack.poll();
        if (action == null) {
            return null;
        }
        for (int i = action.changes().size() - 1; i >= 0; i--) {
            restore(action.changes().get(i));
        }
        return action;
    }

    // ------------------------------------------------------------ statistics

    /**
     * ALGORITHM - attendance rate over a date range.
     * 1. Take the subject's sessions (TreeMap of dates) and extract the sub-range [from, to]
     *    in O(log S + k).
     * 2. For each session date, look up the student's status in the HashMap in O(1)
     *    (no stored record = Present).
     * 3. Count the sessions (total) and the Present ones.
     * Time: O(log S + k) per student, O(N (log S + k)) for a whole class.
     */
    public Stats attendanceStats(String subject, String studentId, LocalDate from, LocalDate to) {
        TreeMap<LocalDate, Integer> dates = sessions.get(subject);
        if (dates == null || from.isAfter(to)) {
            return new Stats(0, 0);
        }
        int present = 0;
        int total = 0;
        for (LocalDate d : dates.subMap(from, true, to, true).keySet()) {
            total++;
            if (PRESENT.equals(getStatus(d, subject, studentId))) {
                present++;
            }
        }
        return new Stats(present, total);
    }

    /**
     * Week 1 = days 1-7, Week 2 = 8-14, Week 3 = 15-21, Week 4 = 22-end of month.
     * "All Weeks (Month View)" (or anything else) = the whole month of the given date.
     */
    public static DateRange weekRange(LocalDate anyDayInMonth, String weekLabel) {
        YearMonth ym = YearMonth.from(anyDayInMonth);
        int start = 1;
        int end = ym.lengthOfMonth();
        if (weekLabel != null) {
            switch (weekLabel) {
                case "Week 1": start = 1;  end = 7;  break;
                case "Week 2": start = 8;  end = 14; break;
                case "Week 3": start = 15; end = 21; break;
                case "Week 4": start = 22; break;
                default: break;
            }
        }
        return new DateRange(ym.atDay(start), ym.atDay(end));
    }

    public static String formatPercent(Stats stats) {
        return stats.total() == 0 ? "N/A" : stats.percent() + "%";
    }

    /** Text shown in the tooltip when the mouse rests on a student row. */
    public String describeRate(String subject, String studentId, LocalDate date, String weekLabel) {
        if (subject == null || date == null) {
            return "";
        }
        DateRange range = weekRange(date, weekLabel);
        Stats stats = attendanceStats(subject, studentId, range.from(), range.to());
        String label = (weekLabel == null || ALL_WEEKS.equals(weekLabel)) ? "Whole month" : weekLabel;
        String period = label + " (" + SHORT_DATE.format(range.from()) + " - "
                + SHORT_DATE.format(range.to()) + ")";
        if (stats.total() == 0) {
            return period + "\nNo attendance sessions recorded in this period.";
        }
        return period + "\nPresent " + stats.present() + " of " + stats.total()
                + " sessions (" + stats.percent() + "%)";
    }

    // --------------------------------------------------------------- helpers

    private String put(LocalDate date, String subject, String studentId, String status) {
        String old = attendance.put(makeKey(date, subject, studentId), status);
        if (old == null) {
            sessions.computeIfAbsent(subject, k -> new TreeMap<>()).merge(date, 1, Integer::sum);
        }
        return old;
    }

    private void restore(Change c) {
        String key = makeKey(c.date(), c.subject(), c.studentId());
        if (c.oldStatus() == null) {
            if (attendance.remove(key) != null) {
                TreeMap<LocalDate, Integer> dates = sessions.get(c.subject());
                if (dates != null) {
                    dates.computeIfPresent(c.date(), (d, n) -> n <= 1 ? null : n - 1);
                }
            }
        } else {
            attendance.put(key, c.oldStatus());
        }
    }

    private void pushUndo(UndoAction action) {
        undoStack.push(action);
        if (undoStack.size() > MAX_UNDO) {
            undoStack.removeLast();
        }
    }

    private static void requireStatus(String status) {
        if (!PRESENT.equals(status) && !ABSENT.equals(status)) {
            throw new IllegalArgumentException("Status must be Present or Absent: " + status);
        }
    }
}
