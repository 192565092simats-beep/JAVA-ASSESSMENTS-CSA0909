import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Task 6: Campus Event Registration Desktop Application
 * Full-featured Swing application with:
 * 1. Layered architecture (UI -> Service -> DAO)
 * 2. Asynchronous background execution (EDT responsiveness)
 * 3. MenuBar (JMenuBar) with File, Filter, and Help menus
 * 4. JTable with DefaultTableModel and dynamic filtering
 * 5. Input validation and duplicate registration prevention
 */
public class CampusEventRegistrationApp extends JFrame {

    // Service Layer
    private final RegistrationService registrationService = new RegistrationService();

    // Form Controls
    private final JTextField studentIdField = new JTextField(10);
    private final JTextField nameField = new JTextField(15);
    private final JComboBox<String> deptCombo = new JComboBox<>(new String[]{
        "Computer Science", "Information Tech", "Electronics Eng", "Mechanical Eng", "Biomedical Eng"
    });
    private final JTextField emailField = new JTextField(15);
    private final JComboBox<String> eventCombo = new JComboBox<>(new String[]{
        "Hackathon 2026", "Code Marathon", "RoboWars Challenge", "Web Dev Blitz", "Paper Presentation"
    });

    private final JButton registerButton = new JButton("Register Student");
    private final JButton clearButton = new JButton("Clear Form");
    private final JButton deleteButton = new JButton("Delete Record");

    // Table View Controls
    private final String[] columnNames = {
        "ID", "Student ID", "Name", "Department", "Email", "Event Name", "Registered Time", "Status"
    };
    private final DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // read-only table view
        }
    };
    private final JTable registrationTable = new JTable(tableModel);
    private final JComboBox<String> filterCombo = new JComboBox<>(new String[]{
        "All Events", "Hackathon 2026", "Code Marathon", "RoboWars Challenge", "Web Dev Blitz", "Paper Presentation"
    });
    private final JButton refreshButton = new JButton("Refresh");

    // Status and Progress
    private final JLabel statusLabel = new JLabel("System Ready. Loaded registrations.");
    private final JProgressBar progressBar = new JProgressBar();

    private boolean showModalDialogs = true;

    public CampusEventRegistrationApp() {
        super("SIMATS Campus Event Registration Portal - Desktop Application");
        setupUI();
        setupMenuBar();
        registerEventHandlers();
        reloadTableData("All Events");
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 1. File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New Registration");
        newItem.addActionListener(e -> clearForm());
        JMenuItem exportItem = new JMenuItem("Export Summary");
        exportItem.addActionListener(e -> exportSummaryReport());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> handleAppExit());
        fileMenu.add(newItem);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // 2. Filter Menu
        JMenu filterMenu = new JMenu("Filter");
        for (int i = 0; i < filterCombo.getItemCount(); i++) {
            final String item = filterCombo.getItemAt(i);
            JMenuItem mi = new JMenuItem(item);
            mi.addActionListener(e -> {
                filterCombo.setSelectedItem(item);
                reloadTableData(item);
            });
            filterMenu.add(mi);
        }

        // 3. Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About System");
        aboutItem.addActionListener(e -> showMessageDialog("About System",
                "SIMATS Campus Event Registration Desktop Portal\nStudent: Thejas Balaji (192565092)\nCourse: Programming in Java (CO4)",
                JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(filterMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));

        // West Panel: Registration Form
        JPanel formContainer = new JPanel(new BorderLayout(5, 5));
        formContainer.setBorder(BorderFactory.createTitledBorder("Student Event Registration"));

        JPanel formGrid = new JPanel(new GridLayout(5, 2, 8, 8));
        formGrid.add(new JLabel("Student ID:"));
        formGrid.add(studentIdField);
        formGrid.add(new JLabel("Full Name:"));
        formGrid.add(nameField);
        formGrid.add(new JLabel("Department:"));
        formGrid.add(deptCombo);
        formGrid.add(new JLabel("University Email:"));
        formGrid.add(emailField);
        formGrid.add(new JLabel("Select Event:"));
        formGrid.add(eventCombo);
        formContainer.add(formGrid, BorderLayout.CENTER);

        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        formButtons.add(registerButton);
        formButtons.add(clearButton);
        formContainer.add(formButtons, BorderLayout.SOUTH);

        add(formContainer, BorderLayout.WEST);

        // Center Panel: Filter and Table
        JPanel tableContainer = new JPanel(new BorderLayout(5, 5));
        tableContainer.setBorder(BorderFactory.createTitledBorder("Registered Participants Directory"));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.add(new JLabel("Filter by Event:"));
        filterPanel.add(filterCombo);
        filterPanel.add(refreshButton);
        filterPanel.add(deleteButton);
        tableContainer.add(filterPanel, BorderLayout.NORTH);

        registrationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(registrationTable);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);

        // South Panel: Status and Progress Bar
        JPanel statusPanel = new JPanel(new BorderLayout(10, 5));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        statusPanel.add(statusLabel, BorderLayout.CENTER);

        progressBar.setPreferredSize(new Dimension(120, 16));
        progressBar.setVisible(false);
        statusPanel.add(progressBar, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);

        setSize(950, 480);
        setLocationRelativeTo(null);
    }

    private void registerEventHandlers() {
        // Register button
        registerButton.addActionListener(e -> executeRegistration());

        // Clear button
        clearButton.addActionListener(e -> clearForm());

        // Filter combo change
        filterCombo.addActionListener(e -> {
            String selected = (String) filterCombo.getSelectedItem();
            reloadTableData(selected);
        });

        // Refresh button
        refreshButton.addActionListener(e -> reloadTableData((String) filterCombo.getSelectedItem()));

        // Delete button
        deleteButton.addActionListener(e -> executeDelete());

        // Window Closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleAppExit();
            }
        });
    }

    public void executeRegistration() {
        String sId = studentIdField.getText();
        String name = nameField.getText();
        String dept = (String) deptCombo.getSelectedItem();
        String email = emailField.getText();
        String event = (String) eventCombo.getSelectedItem();

        setBusy(true, "Registering student for event...");

        registrationService.registerStudent(sId, name, dept, email, event, new RegistrationService.ServiceCallback<Registration>() {
            @Override
            public void onSuccess(Registration result) {
                SwingUtilities.invokeLater(() -> {
                    setBusy(false, "Registration SUCCESS: ID #" + result.getId() + " - " + result.getStudentName());
                    tableModel.addRow(result.toTableRow());
                    showMessageDialog("Registration Confirmed",
                            "Successfully registered " + result.getStudentName() + " for " + result.getEventName() + "!\nRegistration ID: #" + result.getId(),
                            JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                SwingUtilities.invokeLater(() -> {
                    setBusy(false, "Registration FAILED: " + errorMessage);
                    showMessageDialog("Registration Error", errorMessage, JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    public void reloadTableData(String eventFilter) {
        setBusy(true, "Loading records for " + eventFilter + "...");
        registrationService.fetchRegistrations(eventFilter, new RegistrationService.ServiceCallback<List<Registration>>() {
            @Override
            public void onSuccess(List<Registration> list) {
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    for (Registration r : list) {
                        tableModel.addRow(r.toTableRow());
                    }
                    setBusy(false, "Showing " + list.size() + " registrations for [" + eventFilter + "].");
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                SwingUtilities.invokeLater(() -> setBusy(false, "Query Error: " + errorMessage));
            }
        });
    }

    public void executeDelete() {
        int selectedRow = registrationTable.getSelectedRow();
        if (selectedRow == -1) {
            showMessageDialog("Selection Required", "Please select a registration row in the table to delete.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int regId = (int) tableModel.getValueAt(selectedRow, 0);
        String sName = (String) tableModel.getValueAt(selectedRow, 2);

        setBusy(true, "Deleting record ID #" + regId + "...");
        registrationService.deleteRegistration(regId, new RegistrationService.ServiceCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                SwingUtilities.invokeLater(() -> {
                    tableModel.removeRow(selectedRow);
                    setBusy(false, "Deleted registration #" + regId + " for " + sName);
                    showMessageDialog("Record Removed", "Successfully deleted record ID #" + regId, JOptionPane.INFORMATION_MESSAGE);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                SwingUtilities.invokeLater(() -> setBusy(false, "Delete Failed: " + errorMessage));
            }
        });
    }

    public void exportSummaryReport() {
        int rowCount = tableModel.getRowCount();
        StringBuilder sb = new StringBuilder();
        sb.append("=== SIMATS CAMPUS EVENT REGISTRATION EXPORT ===\n");
        sb.append(String.format("Total Exported Records: %d%n%n", rowCount));
        for (int i = 0; i < rowCount; i++) {
            sb.append(String.format("#%s | %s | %s (%s) | %s | %s%n",
                    tableModel.getValueAt(i, 0),
                    tableModel.getValueAt(i, 1),
                    tableModel.getValueAt(i, 2),
                    tableModel.getValueAt(i, 3),
                    tableModel.getValueAt(i, 5),
                    tableModel.getValueAt(i, 6)));
        }
        showMessageDialog("Export Report Summary", sb.toString(), JOptionPane.INFORMATION_MESSAGE);
    }

    public void clearForm() {
        studentIdField.setText("");
        nameField.setText("");
        emailField.setText("");
        if (deptCombo.getItemCount() > 0) deptCombo.setSelectedIndex(0);
        if (eventCombo.getItemCount() > 0) eventCombo.setSelectedIndex(0);
    }

    private void setBusy(boolean busy, String message) {
        statusLabel.setText(message);
        progressBar.setVisible(busy);
        progressBar.setIndeterminate(busy);
        registerButton.setEnabled(!busy);
        refreshButton.setEnabled(!busy);
        deleteButton.setEnabled(!busy);
    }

    private void handleAppExit() {
        registrationService.shutdown();
        dispose();
    }

    protected void showMessageDialog(String title, String message, int type) {
        if (showModalDialogs && !GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(this, message, title, type);
        } else {
            System.out.printf("  [POPUP DIALOG] [%s]: %s%n", title, message.replace("\n", " | "));
        }
    }

    public void setShowModalDialogs(boolean show) {
        this.showModalDialogs = show;
    }

    // Getters for testing
    public JTextField getStudentIdField() { return studentIdField; }
    public JTextField getNameField() { return nameField; }
    public JTextField getEmailField() { return emailField; }
    public JComboBox<String> getDeptCombo() { return deptCombo; }
    public JComboBox<String> getEventCombo() { return eventCombo; }
    public JComboBox<String> getFilterCombo() { return filterCombo; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JLabel getStatusLabel() { return statusLabel; }
    public RegistrationService getRegistrationService() { return registrationService; }
}
