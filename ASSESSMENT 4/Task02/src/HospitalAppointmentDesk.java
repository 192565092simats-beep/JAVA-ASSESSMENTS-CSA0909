import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Task 2: Hospital Outpatient Appointment Desk
 * Demonstrates:
 * 1. Event-driven architecture with ItemListener, ActionListener, and WindowListener
 * 2. Cascading dynamic dropdowns (Department -> Doctors)
 * 3. In-flight transaction state check on window closing
 */
public class HospitalAppointmentDesk extends Frame {

    // Component Controls
    private final TextField patientIdField = new TextField(12);
    private final Choice deptChoice = new Choice();
    private final Choice doctorChoice = new Choice();
    private final Choice slotChoice = new Choice();
    private final Button bookButton = new Button("Book Appointment");
    private final Button clearButton = new Button("Clear Form");
    private final Label statusLabel = new Label("Ready to book appointment.");

    // In-memory catalog (Department -> List of Doctors)
    private final Map<String, List<String>> departmentDoctorMap = new LinkedHashMap<>();

    // Booking in-progress tracking flag
    private boolean isBookingInProgress = false;

    public HospitalAppointmentDesk() {
        super("SIMATS Outpatient Appointment Desk");
        initializeCatalog();
        setupUI();
        registerEventHandlers();
    }

    private void initializeCatalog() {
        departmentDoctorMap.put("Cardiology", Arrays.asList("Dr. Sarah Vance (Senior Interventionalist)", "Dr. Neil Adams (Consultant)"));
        departmentDoctorMap.put("Orthopedics", Arrays.asList("Dr. Robert Chen (Spine Specialist)", "Dr. Maya Patel (Joint Replacement)"));
        departmentDoctorMap.put("Pediatrics", Arrays.asList("Dr. Emily Howard (Neonatal)", "Dr. Arjun Kumar (General Pediatrics)"));
        departmentDoctorMap.put("Neurology", Arrays.asList("Dr. Alan Turing (Neurosurgeon)", "Dr. Grace Hopper (Stroke Care)"));
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));

        // Form Grid Panel
        Panel formPanel = new Panel(new GridLayout(4, 2, 8, 8));

        formPanel.add(new Label("Patient ID:"));
        formPanel.add(patientIdField);

        formPanel.add(new Label("Select Department:"));
        for (String dept : departmentDoctorMap.keySet()) {
            deptChoice.add(dept);
        }
        formPanel.add(deptChoice);

        formPanel.add(new Label("Select Doctor:"));
        formPanel.add(doctorChoice);
        refreshDoctorList(deptChoice.getSelectedItem());

        formPanel.add(new Label("Select Time Slot:"));
        slotChoice.add("09:00 AM - 09:30 AM");
        slotChoice.add("10:00 AM - 10:30 AM");
        slotChoice.add("11:30 AM - 12:00 PM");
        slotChoice.add("02:00 PM - 02:30 PM");
        slotChoice.add("03:30 PM - 04:00 PM");
        formPanel.add(slotChoice);

        add(formPanel, BorderLayout.NORTH);

        // Status Feedback in Center
        statusLabel.setAlignment(Label.CENTER);
        statusLabel.setForeground(Color.BLUE);
        add(statusLabel, BorderLayout.CENTER);

        // Action Buttons in South Panel
        Panel buttonPanel = new Panel(new FlowLayout());
        buttonPanel.add(bookButton);
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(520, 260);
    }

    private void registerEventHandlers() {
        // 1. ItemListener on Department choice to update Doctor choice
        deptChoice.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selectedDept = (String) e.getItem();
                    refreshDoctorList(selectedDept);
                    statusLabel.setText("Department changed to " + selectedDept + ". Updated doctor list.");
                    markBookingInProgress();
                }
            }
        });

        // 2. ActionListener on Book Button
        bookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processBooking();
            }
        });

        // 3. ActionListener on Clear Button
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetForm();
            }
        });

        // 4. WindowListener to confirm before closing when booking in progress
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });
    }

    /**
     * Cascading update: repopulates doctor choice based on department.
     */
    public void refreshDoctorList(String department) {
        doctorChoice.removeAll();
        List<String> doctors = departmentDoctorMap.get(department);
        if (doctors != null) {
            for (String doc : doctors) {
                doctorChoice.add(doc);
            }
        }
    }

    /**
     * Validates input fields and books the appointment.
     */
    public boolean processBooking() {
        String patientId = patientIdField.getText().trim();
        if (patientId.isEmpty()) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Validation Error: Patient ID is required!");
            return false;
        }

        if (doctorChoice.getItemCount() == 0) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Validation Error: No doctor available in selected department.");
            return false;
        }

        String dept = deptChoice.getSelectedItem();
        String doctor = doctorChoice.getSelectedItem();
        String slot = slotChoice.getSelectedItem();

        statusLabel.setForeground(new Color(0, 128, 0)); // Green
        String msg = String.format("SUCCESS: Appt confirmed for %s with %s (%s) at %s", patientId, doctor, dept, slot);
        statusLabel.setText(msg);
        isBookingInProgress = false; // Reset in-progress flag
        return true;
    }

    public void resetForm() {
        patientIdField.setText("");
        if (deptChoice.getItemCount() > 0) {
            deptChoice.select(0);
            refreshDoctorList(deptChoice.getSelectedItem());
        }
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setText("Form cleared. Ready to book appointment.");
        isBookingInProgress = false;
    }

    public void markBookingInProgress() {
        isBookingInProgress = true;
    }

    public boolean handleWindowClosing() {
        if (isBookingInProgress && !patientIdField.getText().trim().isEmpty()) {
            statusLabel.setText("Warning: Booking in progress! Please confirm cancellation.");
            System.out.println("  [WindowListener] Window close intercepted: Active booking form in progress.");
        }
        dispose();
        return true;
    }

    // Getters for programmatic inspection
    public TextField getPatientIdField() { return patientIdField; }
    public Choice getDeptChoice() { return deptChoice; }
    public Choice getDoctorChoice() { return doctorChoice; }
    public Choice getSlotChoice() { return slotChoice; }
    public Label getStatusLabel() { return statusLabel; }
    public Button getBookButton() { return bookButton; }
    public boolean isBookingInProgress() { return isBookingInProgress; }
}
