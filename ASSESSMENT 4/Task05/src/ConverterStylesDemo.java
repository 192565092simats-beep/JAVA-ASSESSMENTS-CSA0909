import java.awt.*;
import java.awt.event.*;

/**
 * Task 5: Demonstration and Comparison of 4 Event Handling Styles
 * Style 1: Frame implements ActionListener directly
 * Style 2: Named Member Inner Class
 * Style 3: Anonymous Inner Class
 * Style 4: Lambda Expression / Method Reference
 */
public class ConverterStylesDemo {

    // =========================================================================
    // STYLE 1: Frame Implements ActionListener Directly
    // =========================================================================
    public static class ConverterFrameStyle extends Frame implements ActionListener {
        private final TextField amountField = new TextField("8350", 10);
        private final Button convertButton = new Button("Convert");
        private String outputText = "";

        public ConverterFrameStyle() {
            convertButton.addActionListener(this); // Registers this frame instance
            amountField.addActionListener(this);   // Enter key also handled by this frame
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Must inspect event source if multiple components share the listener
            if (e.getSource() == convertButton || e.getSource() == amountField) {
                outputText = executeConversion(amountField.getText());
            }
        }

        public String getOutputText() { return outputText; }
    }

    // =========================================================================
    // STYLE 2: Named Member Inner Class
    // =========================================================================
    public static class ConverterNamedInnerStyle extends Frame {
        private final TextField amountField = new TextField("16700", 10);
        private final Button convertButton = new Button("Convert");
        private String outputText = "";

        // Dedicated Named Inner Class
        private class ConvertActionHandler implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Direct access to outer class fields
                outputText = executeConversion(amountField.getText());
            }
        }

        public ConverterNamedInnerStyle() {
            ConvertActionHandler handler = new ConvertActionHandler();
            convertButton.addActionListener(handler);
            amountField.addActionListener(handler); // Reusable instance shared by both!
        }

        public String getOutputText() { return outputText; }
    }

    // =========================================================================
    // STYLE 3: Anonymous Inner Class
    // =========================================================================
    public static class ConverterAnonymousStyle extends Frame {
        private final TextField amountField = new TextField("25000", 10);
        private final Button convertButton = new Button("Convert");
        private String outputText = "";

        public ConverterAnonymousStyle() {
            ActionListener sharedHandler = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    outputText = executeConversion(amountField.getText());
                }
            };
            convertButton.addActionListener(sharedHandler);
            amountField.addActionListener(sharedHandler);
        }

        public String getOutputText() { return outputText; }
    }

    // =========================================================================
    // STYLE 4: Lambda Expression / Method Reference (Java 8+)
    // =========================================================================
    public static class ConverterLambdaStyle extends Frame {
        private final TextField amountField = new TextField("50000", 10);
        private final Button convertButton = new Button("Convert");
        private String outputText = "";

        public ConverterLambdaStyle() {
            // Method Reference or Lambda
            convertButton.addActionListener(e -> performConversion());
            amountField.addActionListener(this::onEnterOrClick);
        }

        private void onEnterOrClick(ActionEvent e) {
            performConversion();
        }

        public void performConversion() {
            outputText = executeConversion(amountField.getText());
        }

        public String getOutputText() { return outputText; }
    }

    // Shared calculation logic
    private static String executeConversion(String input) {
        try {
            double inr = Double.parseDouble(input.trim());
            double usd = inr * Converter.INR_TO_USD_RATE;
            return String.format("SUCCESS: ₹ %,.2f INR = $ %,.2f USD", inr, usd);
        } catch (NumberFormatException e) {
            return "ERROR: Invalid number format for input \"" + input + "\"";
        }
    }

    // =========================================================================
    // Main Test Harness
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println(" TASK 5: Currency Converter - 4 Event-Handling Styles     ");
        System.out.println("==========================================================");

        // Test Style 1
        System.out.println("\n[STYLE 1] Frame Implements ActionListener Directly:");
        ConverterFrameStyle s1 = new ConverterFrameStyle();
        s1.actionPerformed(new ActionEvent(s1.convertButton, ActionEvent.ACTION_PERFORMED, "Convert"));
        System.out.println("  Button Click Result: " + s1.getOutputText());

        // Test Style 2
        System.out.println("\n[STYLE 2] Named Member Inner Class (ConvertActionHandler):");
        ConverterNamedInnerStyle s2 = new ConverterNamedInnerStyle();
        s2.convertButton.getActionListeners()[0].actionPerformed(new ActionEvent(s2.amountField, ActionEvent.ACTION_PERFORMED, "Enter"));
        System.out.println("  Enter Key Result:    " + s2.getOutputText());

        // Test Style 3
        System.out.println("\n[STYLE 3] Anonymous Inner Class:");
        ConverterAnonymousStyle s3 = new ConverterAnonymousStyle();
        s3.convertButton.getActionListeners()[0].actionPerformed(new ActionEvent(s3.convertButton, ActionEvent.ACTION_PERFORMED, "Convert"));
        System.out.println("  Button Click Result: " + s3.getOutputText());

        // Test Style 4
        System.out.println("\n[STYLE 4] Lambda Expression & Method Reference (Modern Java):");
        ConverterLambdaStyle s4 = new ConverterLambdaStyle();
        s4.convertButton.getActionListeners()[0].actionPerformed(new ActionEvent(s4.convertButton, ActionEvent.ACTION_PERFORMED, "Convert"));
        System.out.println("  Lambda Result:       " + s4.getOutputText());

        // Test Completed Program Converter.java
        System.out.println("\n[VERIFICATION] Testing Completed Converter.java Program:");
        Converter mainApp = new Converter();
        mainApp.setShowDialogModal(false);
        mainApp.getAmountField().setText("100000");
        boolean success = mainApp.convert();
        System.out.println("  Conversion Status:   " + (success ? "SUCCESS" : "FAILED"));
        System.out.println("  Result Label:        " + mainApp.getResultLabel().getText());

        mainApp.getAmountField().setText("InvalidNumber");
        boolean failTest = mainApp.convert();
        System.out.println("  Invalid Test Status: " + (failTest ? "SUCCESS" : "HANDLED_ERROR"));
        System.out.println("  Result Label:        " + mainApp.getResultLabel().getText());

        System.out.println("\n==========================================================");
        System.out.println(" Task 5 All 4 Styles and Converter Completed Successfully!");
        System.out.println("==========================================================");
        mainApp.dispose();
    }
}
