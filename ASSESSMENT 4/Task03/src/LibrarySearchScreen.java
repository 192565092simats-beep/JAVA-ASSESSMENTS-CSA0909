import java.awt.*;
import java.awt.event.*;

/**
 * Task 3: Campus Library Search Screen
 * Demonstrates:
 * 1. Nested layout management (BorderLayout + FlowLayout)
 * 2. Responsive component behavior upon window resizing
 * 3. Fixed-height North and South panels with an expanding Center results area
 */
public class LibrarySearchScreen extends Frame {

    // North Components
    private final Label titleLabel = new Label("SIMATS Campus Library - Catalog & OPAC Search", Label.CENTER);
    private final TextField searchField = new TextField(22);
    private final Choice categoryChoice = new Choice();
    private final Button searchButton = new Button("Search Catalog");

    // Center Components
    private final TextArea resultsArea = new TextArea(10, 50);

    // South Components
    private final Button prevButton = new Button("<< Previous");
    private final Button nextButton = new Button("Next >>");
    private final Button reserveButton = new Button("Reserve Selected Book");
    private final Label statusBar = new Label("Ready. Enter keyword and click Search.");

    // Container Panels
    private final Panel northPanel = new Panel(new BorderLayout(5, 5));
    private final Panel searchRowPanel = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    private final Panel southPanel = new Panel(new BorderLayout(5, 5));
    private final Panel buttonRowPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 15, 5));

    public LibrarySearchScreen() {
        super("Campus Library Search - Responsive Layout");
        setupLayout();
        registerListeners();
    }

    private void setupLayout() {
        // Frame uses BorderLayout as the top-level orchestrator
        setLayout(new BorderLayout(10, 10));

        // 1. NORTH REGION: Header Title and Search Criteria Row
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setBackground(new Color(230, 235, 245));
        northPanel.add(titleLabel, BorderLayout.NORTH);

        categoryChoice.add("All Fields");
        categoryChoice.add("Title");
        categoryChoice.add("Author");
        categoryChoice.add("ISBN");
        categoryChoice.add("Subject / Topic");

        searchRowPanel.add(new Label("Keyword:"));
        searchRowPanel.add(searchField);
        searchRowPanel.add(new Label("Category:"));
        searchRowPanel.add(categoryChoice);
        searchRowPanel.add(searchButton);

        northPanel.add(searchRowPanel, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        // 2. CENTER REGION: Results Area (Expands horizontally and vertically on resize)
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(resultsArea, BorderLayout.CENTER);

        // 3. SOUTH REGION: Navigation, Actions, and Status Bar
        buttonRowPanel.add(prevButton);
        buttonRowPanel.add(nextButton);
        buttonRowPanel.add(reserveButton);
        southPanel.add(buttonRowPanel, BorderLayout.CENTER);

        statusBar.setBackground(new Color(240, 240, 240));
        southPanel.add(statusBar, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        setSize(700, 420);
    }

    private void registerListeners() {
        // Search Action
        searchButton.addActionListener(e -> executeSearch());
        searchField.addActionListener(e -> executeSearch()); // Enter key trigger

        // Navigation Actions
        prevButton.addActionListener(e -> statusBar.setText("Page 1 of 1 (No prior pages)."));
        nextButton.addActionListener(e -> statusBar.setText("Page 1 of 1 (No additional pages)."));
        reserveButton.addActionListener(e -> {
            String selected = resultsArea.getSelectedText();
            if (selected != null && !selected.trim().isEmpty()) {
                statusBar.setText("Reserved: " + selected.trim());
            } else {
                statusBar.setText("Please highlight / select a book entry above to reserve.");
            }
        });

        // Window Closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    public void executeSearch() {
        String query = searchField.getText().trim();
        String cat = categoryChoice.getSelectedItem();

        if (query.isEmpty()) {
            statusBar.setText("Error: Search query cannot be empty.");
            return;
        }

        statusBar.setText("Searching for \"" + query + "\" in category [" + cat + "]...");

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s | %-32s | %-20s | %-12s%n", "ACCESSION", "TITLE", "AUTHOR", "STATUS"));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("%-10s | %-32s | %-20s | %-12s%n", "CS-10901", "Core Java Volume I: Fundamentals", "Cay S. Horstmann", "AVAILABLE"));
        sb.append(String.format("%-10s | %-32s | %-20s | %-12s%n", "CS-10902", "Effective Java (3rd Edition)", "Joshua Bloch", "AVAILABLE"));
        sb.append(String.format("%-10s | %-32s | %-20s | %-12s%n", "CS-10903", "Java: The Complete Reference", "Herbert Schildt", "CHECKED OUT"));
        sb.append(String.format("%-10s | %-32s | %-20s | %-12s%n", "CS-10904", "Design Patterns: Gang of Four", "Erich Gamma et al.", "RESERVED"));

        resultsArea.setText(sb.toString());
        statusBar.setText("Search Completed: 4 records found for \"" + query + "\" (" + cat + ").");
    }

    // Getters for programmatic inspection
    public TextField getSearchField() { return searchField; }
    public Choice getCategoryChoice() { return categoryChoice; }
    public Button getSearchButton() { return searchButton; }
    public TextArea getResultsArea() { return resultsArea; }
    public Label getStatusBar() { return statusBar; }
    public Panel getNorthPanel() { return northPanel; }
    public Panel getSouthPanel() { return southPanel; }
}
