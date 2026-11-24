import java.awt.*;
import java.awt.event.*;

/**
 * Task 1.2 - Repaired Fee Payment GUI Window
 * Demonstrates:
 * 1. Clean separation of UI and business logic
 * 2. Dedicated button listeners
 * 3. Proper window closing via WindowAdapter
 * 4. User feedback via visual Status Label
 */
public class RepairedFeeWindow extends Frame {
    private final TextField regNoField = new TextField(15);
    private final TextField amountField = new TextField(10);
    private final Button payButton = new Button("Pay");
    private final Button clearButton = new Button("Clear");
    private final Button receiptButton = new Button("Receipt");
    private final Label statusLabel = new Label("Ready for student fee submission.");
    private final FeeService feeService = new FeeService();
    private String lastReceipt = null;

    public RepairedFeeWindow() {
        super("Student Fee Payment System");
        setupUI();
        registerEventHandlers();
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));

        // Form panel
        Panel formPanel = new Panel(new GridLayout(2, 2, 5, 5));
        formPanel.add(new Label("Register Number:"));
        formPanel.add(regNoField);
        formPanel.add(new Label("Fee Amount (INR):"));
        formPanel.add(amountField);
        add(formPanel, BorderLayout.NORTH);

        // Status Label in Center
        statusLabel.setAlignment(Label.CENTER);
        add(statusLabel, BorderLayout.CENTER);

        // Button panel
        Panel buttonPanel = new Panel(new FlowLayout());
        buttonPanel.add(payButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(receiptButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(400, 200);
    }

    private void registerEventHandlers() {
        // Dedicated listener 1: Pay
        payButton.addActionListener(e -> {
            statusLabel.setText("Processing payment... Please wait.");
            payButton.setEnabled(false);

            feeService.processPayment(regNoField.getText(), amountField.getText(), new FeeService.FeeCallback() {
                @Override
                public void onSuccess(String receiptNumber, double amount) {
                    EventQueue.invokeLater(() -> {
                        lastReceipt = receiptNumber;
                        statusLabel.setText("Payment SUCCESS! Receipt: " + receiptNumber);
                        payButton.setEnabled(true);
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    EventQueue.invokeLater(() -> {
                        statusLabel.setText("Payment FAILED: " + errorMessage);
                        payButton.setEnabled(true);
                    });
                }
            });
        });

        // Dedicated listener 2: Clear
        clearButton.addActionListener(e -> {
            regNoField.setText("");
            amountField.setText("");
            statusLabel.setText("Inputs cleared.");
        });

        // Dedicated listener 3: Receipt
        receiptButton.addActionListener(e -> {
            if (lastReceipt != null) {
                statusLabel.setText("Last Receipt: " + lastReceipt);
            } else {
                statusLabel.setText("No previous transaction found.");
            }
        });

        // Window Closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                feeService.shutdown();
                dispose();
            }
        });
    }

    public TextField getRegNoField() { return regNoField; }
    public TextField getAmountField() { return amountField; }
    public Button getPayButton() { return payButton; }
    public Button getClearButton() { return clearButton; }
    public Button getReceiptButton() { return receiptButton; }
    public Label getStatusLabel() { return statusLabel; }
    public FeeService getFeeService() { return feeService; }
}
