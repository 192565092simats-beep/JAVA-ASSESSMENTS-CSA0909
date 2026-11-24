import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Q1 - Bank ATM Simulation Exception Handling
 * Demonstrates:
 * 1. Checked vs Unchecked exceptions
 * 2. Proper catch block ordering and multi-catch rules
 * 3. Guaranteed execution of finally block (card ejection)
 * 4. Error code alternative comparison
 */
public class ATMSimulation {

    // Custom checked exception for domain failure
    public static class InsufficientFundsException extends Exception {
        private final double balance;
        private final double requested;

        public InsufficientFundsException(String message, double balance, double requested) {
            super(message);
            this.balance = balance;
            this.requested = requested;
        }

        public double getBalance() { return balance; }
        public double getRequested() { return requested; }
    }

    private double balance = 5000.0;
    private final String cardReaderPath;

    public ATMSimulation(String cardReaderPath) {
        this.cardReaderPath = cardReaderPath;
    }

    /**
     * Executes ATM withdrawal transaction with rigorous exception safety.
     */
    public boolean withdraw(String accInput, String amountInput) {
        boolean cardInserted = true;
        System.out.printf("%n[ATM] Initiating transaction: Account='%s', Amount='%s'%n", accInput, amountInput);

        try {
            // 1. Check card-reader hardware status (Checked Exception: FileNotFoundException)
            File cardFile = new File(cardReaderPath);
            if (!cardFile.exists()) {
                throw new FileNotFoundException("Card-reader hardware interface file not found: " + cardReaderPath);
            }

            // 2. Parse inputs (Unchecked Exception: NumberFormatException)
            int accountNumber = Integer.parseInt(accInput.trim());
            double amount = Double.parseDouble(amountInput.trim());

            // 3. Validate business constraints (Unchecked Exception: IllegalArgumentException)
            if (accountNumber <= 0) {
                throw new IllegalArgumentException("Account number must be a positive integer.");
            }
            if (amount <= 0) {
                throw new IllegalArgumentException("Withdrawal amount must be greater than zero. Provided: " + amount);
            }

            // 4. Validate balance constraint (Checked Exception: InsufficientFundsException)
            if (amount > balance) {
                throw new InsufficientFundsException("Insufficient funds in account.", balance, amount);
            }

            // Success path
            balance -= amount;
            System.out.printf("  [SUCCESS] Dispensed Rs %.2f. Remaining balance: Rs %.2f%n", amount, balance);
            return true;

        } catch (FileNotFoundException e) {
            // Checked IO Exception
            System.out.println("  [HARDWARE FAULT] " + e.getMessage());
            return false;

        } catch (InsufficientFundsException e) {
            // Domain Business Checked Exception
            System.out.printf("  [TRANSACTION REJECTED] %s Available: Rs %.2f, Requested: Rs %.2f%n",
                    e.getMessage(), e.getBalance(), e.getRequested());
            return false;

        } catch (NumberFormatException e) {
            // Unchecked: Specific number parsing failure
            System.out.println("  [INPUT FORMAT ERROR] Numbers required: " + e.getMessage());
            return false;

        } catch (IllegalArgumentException e) {
            // Unchecked: Business range validation failure
            System.out.println("  [BUSINESS VALIDATION ERROR] " + e.getMessage());
            return false;

        } catch (Exception e) {
            // Fallback generic catch for unexpected failures
            System.out.println("  [UNEXPECTED ERROR] An unknown fault occurred: " + e.getMessage());
            return false;

        } finally {
            // Guaranteed cleanup: card is always ejected and audit log finalized
            if (cardInserted) {
                System.out.println("  [FINALLY GUARANTEE] Card ejected safely. Session closed.");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("      Q1: Handling Failure in a Bank ATM Simulation       ");
        System.out.println("==========================================================");

        // Path to simulate card reader
        String validCardPath = "card_device.tmp";
        try {
            new File(validCardPath).createNewFile();
        } catch (IOException ignored) {}

        ATMSimulation atm = new ATMSimulation(validCardPath);

        // Test 1: Valid withdrawal (Normal path)
        atm.withdraw("100234", "1500.00");

        // Test 2: Input letters instead of numbers (NumberFormatException)
        atm.withdraw("100234", "FIFTEEN_HUNDRED");

        // Test 3: Negative amount (IllegalArgumentException)
        atm.withdraw("100234", "-500.00");

        // Test 4: Request exceeding balance (InsufficientFundsException)
        atm.withdraw("100234", "10000.00");

        // Test 5: Missing card reader file (FileNotFoundException)
        ATMSimulation brokenAtm = new ATMSimulation("missing_hardware_port.sys");
        brokenAtm.withdraw("100234", "500.00");

        // Cleanup temporary file
        new File(validCardPath).delete();
        System.out.println("\n==========================================================");
    }
}
