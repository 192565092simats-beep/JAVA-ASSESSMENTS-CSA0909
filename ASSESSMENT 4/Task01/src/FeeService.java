import java.util.concurrent.*;

/**
 * Task 1.2 - Decoupled Service Layer for Student Fee Processing
 * Encapsulates validation, asynchronous transaction processing, and audit logging.
 */
public class FeeService {

    public interface FeeCallback {
        void onSuccess(String receiptNumber, double amount);
        void onFailure(String errorMessage);
    }

    private final ExecutorService workerPool = Executors.newSingleThreadExecutor();

    public void processPayment(String regNo, String amountStr, FeeCallback callback) {
        // 1. Validation (Synchronous on caller thread before dispatching)
        if (regNo == null || regNo.trim().isEmpty()) {
            callback.onFailure("Registration number cannot be empty.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr.trim());
            if (amount <= 0) {
                callback.onFailure("Fee amount must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            callback.onFailure("Invalid amount format. Please enter a valid number.");
            return;
        }

        // 2. Offload Database / Gateway work to background worker thread (keeps EDT responsive!)
        workerPool.submit(() -> {
            try {
                // Simulate network latency / bank confirmation
                Thread.sleep(1000);

                // Simulated PreparedStatement: INSERT INTO fees (reg_no, amount) VALUES (?, ?)
                String receiptNo = "REC-" + System.currentTimeMillis() % 100000;
                callback.onSuccess(receiptNo, amount);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                callback.onFailure("Transaction interrupted by system.");
            } catch (Exception e) {
                callback.onFailure("Database error: " + e.getMessage());
            }
        });
    }

    public void shutdown() {
        workerPool.shutdown();
    }
}
