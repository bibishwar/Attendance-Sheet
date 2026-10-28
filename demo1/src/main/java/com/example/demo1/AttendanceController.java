package com.example.demo1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Controller of attendance-view.fxml. It only handles the screen; the rules and the data
 * structures are in AttendanceService, the files are handled by AttendanceStorage.
 */
public class AttendanceController {

    private static final String DEFAULT_SUBJECT = "Comprehensive Design of Data Structure";

    // ---- controls defined in attendance-view.fxml
    @FXML private Label lblSubjectHeader;
    @FXML private ComboBox<String> comboSubject;
    @FXML private ComboBox<String> comboWeek;
    @FXML private DatePicker datePicker;
    @FXML private TextField txtNewSubject;
    @FXML private TextField txtNewStudentId;
    @FXML private TextField txtNewStudentName;
    @FXML private TableView<AttendanceRecord> table;
    @FXML private TableColumn<AttendanceRecord, Integer> colIndex;
    @FXML private TableColumn<AttendanceRecord, String> colStudentId;
    @FXML private TableColumn<AttendanceRecord, String> colStudentName;
    @FXML private TableColumn<AttendanceRecord, String> colStatus;
    @FXML private TableColumn<AttendanceRecord, Void> colAction;

    // ---- model and storage
    private final AttendanceService service = new AttendanceService();
    private final AttendanceStorage storage = new AttendanceStorage();
    private final ObservableList<String> subjectList = FXCollections.observableArrayList();
    private final ObservableList<AttendanceRecord> tableData = FXCollections.observableArrayList();

    /** Called by FXMLLoader after the FXML has been loaded. */
    @FXML
    private void initialize() {
        try {
            storage.load(service);
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }

        subjectList.setAll(service.getSubjects());
        comboSubject.setItems(subjectList);
        comboSubject.setValue(subjectList.contains(DEFAULT_SUBJECT) || subjectList.isEmpty()
                ? DEFAULT_SUBJECT : subjectList.get(0));
        lblSubjectHeader.setText(comboSubject.getValue());

        comboWeek.getItems().addAll("Week 1", "Week 2", "Week 3", "Week 4", AttendanceService.ALL_WEEKS);
        comboWeek.setValue(AttendanceService.ALL_WEEKS);

        datePicker.setValue(LocalDate.now());

        setupTable();

        // Listeners
        comboSubject.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblSubjectHeader.setText(newVal);
                loadDailyRoster();
            }
        });
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> loadDailyRoster());
        comboWeek.valueProperty().addListener((obs, oldVal, newVal) -> table.refresh());

        // Ctrl+Z = undo (the scene exists only after the FXML is attached to a window)
        table.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene != null) {
                scene.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN), this::undoLast);
            }
        });

        loadDailyRoster();
    }

    // ------------------------------------------------------------------ table

    private void setupTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Row: double-click deletes, tooltip shows the attendance rate of the chosen week
        table.setRowFactory(tv -> new AttendanceRow());

        colIndex.setCellValueFactory(new PropertyValueFactory<>("index"));
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Present".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #198754; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
                    } else {
                        setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
                    }
                }
            }
        });

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnTap = new Button();

            {
                btnTap.setStyle("-fx-font-weight: bold; -fx-font-size: 10px; -fx-cursor: hand; -fx-padding: 1 8;");
                btnTap.setOnAction(e -> {
                    AttendanceRecord record = getTableView().getItems().get(getIndex());
                    String newStatus = "Present".equalsIgnoreCase(record.getStatus()) ? "Absent" : "Present";
                    record.setStatus(newStatus);
                    service.setStatus(datePicker.getValue(), comboSubject.getValue(),
                            record.getStudentId(), newStatus);
                    persistAttendance();
                    getTableView().refresh();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    AttendanceRecord record = getTableView().getItems().get(getIndex());
                    if ("Present".equalsIgnoreCase(record.getStatus())) {
                        btnTap.setText("Tap -> ABSENT");
                        btnTap.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 1 8;");
                    } else {
                        btnTap.setText("Tap -> PRESENT");
                        btnTap.setStyle("-fx-background-color: #198754; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 1 8;");
                    }
                    setGraphic(btnTap);
                }
            }
        });

        table.setItems(tableData);
    }

    /** Table row with double-click-to-delete and a tooltip showing the period attendance rate. */
    private class AttendanceRow extends TableRow<AttendanceRecord> {
        AttendanceRow() {
            setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !isEmpty()) {
                    confirmAndDelete(getItem());
                }
            });
        }

        @Override
        protected void updateItem(AttendanceRecord item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setTooltip(null);
            } else {
                String text = service.describeRate(comboSubject.getValue(),
                        item.getStudentId(), datePicker.getValue(), comboWeek.getValue());
                setTooltip(text.isEmpty() ? null : new Tooltip(text));
            }
        }
    }

    private void confirmAndDelete(AttendanceRecord record) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to remove " + record.getStudentName() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                service.removeStudent(record.getStudentId());
                persistStudents();
                loadDailyRoster(); // reload the table to update the UI
            }
        });
    }

    // --------------------------------------------------------------- handlers

    @FXML
    private void onAddSubject() {
        String newSub = txtNewSubject.getText().trim();
        if (service.addSubject(newSub)) {
            subjectList.add(newSub);
            persistSubjects();
            comboSubject.setValue(newSub);
            txtNewSubject.clear();
        }
    }

    @FXML
    private void onAddStudent() {
        String sId = txtNewStudentId.getText().trim();
        String sName = txtNewStudentName.getText().trim();

        switch (service.addStudent(sId, sName)) {
            case ADDED -> {
                txtNewStudentId.clear();
                txtNewStudentName.clear();
                persistStudents();
                loadDailyRoster();
            }
            case DUPLICATE_ID -> new Alert(Alert.AlertType.WARNING, "Student ID already exists!").show();
            case INVALID_ID -> new Alert(Alert.AlertType.WARNING,
                    "Student ID cannot contain a comma (,) or a bar (|).").show();
            case EMPTY_FIELD -> new Alert(Alert.AlertType.WARNING,
                    "Please enter both Student ID and Name!").show();
        }
    }

    @FXML
    private void onAllPresent() {
        setBulkStatus(AttendanceService.PRESENT);
    }

    @FXML
    private void onAllAbsent() {
        setBulkStatus(AttendanceService.ABSENT);
    }

    @FXML
    private void onExport() {
        LocalDate date = datePicker.getValue();
        String subject = comboSubject.getValue();
        if (date == null || subject == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Attendance to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName(subject.replaceAll("\\s+", "_") + "_" + date + ".csv");

        File file = fileChooser.showSaveDialog(table.getScene().getWindow());
        if (file != null) {
            try {
                storage.exportCsv(file.toPath(), new ArrayList<>(tableData), date, subject,
                        comboWeek.getValue(), service);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Export Successful!");
                alert.setHeaderText(null);
                alert.show();
            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR, "Export Failed: " + ex.getMessage()).show();
            }
        }
    }

    private void undoLast() {
        AttendanceService.UndoAction action = service.undo();
        if (action == null) {
            return;
        }
        persistAttendance();
        // jump to the date/subject that was changed, so the user can see the result
        if (!action.date().equals(datePicker.getValue())) {
            datePicker.setValue(action.date());
        }
        if (!action.subject().equals(comboSubject.getValue())) {
            comboSubject.setValue(action.subject());
        }
        loadDailyRoster();
    }

    // ---------------------------------------------------------------- helpers

    private void setBulkStatus(String targetStatus) {
        LocalDate date = datePicker.getValue();
        String subject = comboSubject.getValue();
        if (date == null || subject == null) {
            return;
        }
        service.setBulk(date, subject, targetStatus);
        for (AttendanceRecord record : tableData) {
            record.setStatus(targetStatus);
        }
        persistAttendance();
        table.refresh();
    }

    private void loadDailyRoster() {
        tableData.clear();
        LocalDate date = datePicker.getValue();
        String subject = comboSubject.getValue();
        if (date == null || subject == null) {
            return;
        }
        int i = 1;
        for (Student st : service.getRoster()) {
            tableData.add(new AttendanceRecord(i++, st.id(), st.name(),
                    service.getStatus(date, subject, st.id())));
        }
    }

    private void persistAttendance() {
        try {
            storage.saveAttendance(service);
        } catch (IOException e) {
            System.err.println("Error saving attendance: " + e.getMessage());
        }
    }

    private void persistStudents() {
        try {
            storage.saveStudents(service);
        } catch (IOException e) {
            System.err.println("Error saving students: " + e.getMessage());
        }
    }

    private void persistSubjects() {
        try {
            storage.saveSubjects(service);
        } catch (IOException e) {
            System.err.println("Error saving subjects: " + e.getMessage());
        }
    }
}
