import java.awt.*;
import java.awt.event.*;
import javax.swing.JOptionPane;

/**
 * Task 5: Currency Converter (INR to USD)
 * Complete the Code - Lambda Expression Style
 * Addresses TODO 1 (Listener registration) and TODO 2 (convert() method implementation).
 */
public class Converter extends Frame {

    // Conversion rate: 1 INR = 0.012 USD (approx 1 USD = 83.33 INR)
    public static final double INR_TO_USD_RATE = 0.012;

    private final TextField amountField = new TextField(12);
    private final Button convertButton = new Button("Convert to USD");
    private final Label resultLabel = new Label("Enter amount in INR and convert.");

    public Converter() {
        super("SIMATS Currency Converter (INR -> USD)");
        setupUI();
        registerEventHandlers();
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));

        Panel inputPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inputPanel.add(new Label("Amount in INR (₹):"));
        inputPanel.add(amountField);
        inputPanel.add(convertButton);
        add(inputPanel, BorderLayout.NORTH);

        resultLabel.setAlignment(Label.CENTER);
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        add(resultLabel, BorderLayout.CENTER);

        // Window Closing Handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setSize(420, 180);
    }

    private void registerEventHandlers() {
        /**
         * [TODO 1 Completed]:
         * Register event listeners on convertButton and amountField (Enter key)
         * using concise lambda expressions delegating to this.convert().
         */
        // 1. Convert button click listener
        convertButton.addActionListener(e -> convert());

        // 2. TextField Enter key listener (TextField fires ActionEvent on Enter)
        amountField.addActionListener(e -> convert());
    }

    /**
     * [TODO 2 Completed]:
     * Complete the convert() method:
     * 1. Read string from amountField and trim whitespace.
     * 2. Parse input into a double with proper exception handling.
     * 3. Validate that amount > 0.
     * 4. Calculate USD value = amount * INR_TO_USD_RATE.
     * 5. Update resultLabel with formatted currency output.
     * 6. Display a popup dialog box (JOptionPane.showMessageDialog).
     */
    public boolean convert() {
        String input = amountField.getText().trim();

        if (input.isEmpty()) {
            String errorMsg = "Please enter an amount in INR.";
            resultLabel.setText("Error: " + errorMsg);
            showPopupMessage("Input Error", errorMsg, JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            double inr = Double.parseDouble(input);
            if (inr < 0) {
                String errorMsg = "Amount cannot be negative.";
                resultLabel.setText("Error: " + errorMsg);
                showPopupMessage("Validation Error", errorMsg, JOptionPane.ERROR_MESSAGE);
                return false;
            }

            double usd = inr * INR_TO_USD_RATE;
            String formattedResult = String.format("₹ %,.2f INR = $ %,.2f USD (@ rate %.3f)", inr, usd, INR_TO_USD_RATE);

            // Update on-screen Label
            resultLabel.setText(formattedResult);

            // Display Popup Dialog Box
            showPopupMessage("Conversion Result", formattedResult, JOptionPane.INFORMATION_MESSAGE);
            return true;

        } catch (NumberFormatException ex) {
            String errorMsg = "Invalid input \"" + input + "\". Please enter digits only.";
            resultLabel.setText("Error: " + errorMsg);
            showPopupMessage("Number Format Error", errorMsg, JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean showDialogModal = true;

    public void setShowDialogModal(boolean showDialogModal) {
        this.showDialogModal = showDialogModal;
    }

    /**
     * Helper to show message dialog safely (works in GUI; gracefully handles headless).
     */
    protected void showPopupMessage(String title, String message, int messageType) {
        if (showDialogModal && !GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(this, message, title, messageType);
        } else {
            System.out.printf("  [POPUP DIALOG] [%s]: %s%n", title, message);
        }
    }

    // Getters for programmatic inspection
    public TextField getAmountField() { return amountField; }
    public Button getConvertButton() { return convertButton; }
    public Label getResultLabel() { return resultLabel; }
}
