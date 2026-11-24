import java.io.*;
import java.util.List;

/**
 * Q3 - Robust Daily Sales Report Generator using try-with-resources.
 */
public class TryWithResourcesReport {

    // Custom AutoCloseable resource demonstrating closure order and suppressed exceptions
    static class AutoCloseStoreReader implements AutoCloseable {
        private final String storeName;
        private final boolean failOnClose;

        public AutoCloseStoreReader(String storeName, boolean failOnClose) {
            this.storeName = storeName;
            this.failOnClose = failOnClose;
            ManualCloseReport.openFileHandles++;
            System.out.printf("    [OPEN-TWR] %s opened. Active handles: %d%n", storeName, ManualCloseReport.openFileHandles);
        }

        public void readData(boolean failDuringRead) {
            if (failDuringRead) {
                throw new IllegalStateException("Corrupt checksum while reading " + storeName);
            }
        }

        @Override
        public void close() throws Exception {
            ManualCloseReport.openFileHandles--;
            System.out.printf("    [CLOSE-TWR] %s automatically closed. Active handles: %d%n",
                    storeName, ManualCloseReport.openFileHandles);
            if (failOnClose) {
                throw new IOException("I/O Flush exception during auto-close of " + storeName);
            }
        }
    }

    public static void generateReport(List<String> storeNames, boolean injectFailure, boolean failOnClose) {
        System.out.println("\n--- Running try-with-resources Report Generator ---");

        try (
            // Resources declared in parentheses are guaranteed to close in REVERSE order of declaration
            AutoCloseStoreReader store1 = new AutoCloseStoreReader(storeNames.get(0), false);
            AutoCloseStoreReader store2 = new AutoCloseStoreReader(storeNames.get(1), false);
            AutoCloseStoreReader store3 = new AutoCloseStoreReader(storeNames.get(2), failOnClose)
        ) {
            System.out.println("  Processing store records inside TWR block...");
            store1.readData(false);
            store2.readData(false);
            if (injectFailure) {
                store3.readData(true); // Triggers primary exception
            }
            System.out.println("  [SUCCESS] All store records parsed.");

        } catch (Exception e) {
            System.out.println("  [CAUGHT PRIMARY EXCEPTION] " + e.getClass().getSimpleName() + ": " + e.getMessage());

            // Inspecting suppressed exceptions
            Throwable[] suppressed = e.getSuppressed();
            if (suppressed.length > 0) {
                System.out.println("  [SUPPRESSED EXCEPTIONS DETECTED]:");
                for (Throwable s : suppressed) {
                    System.out.println("    - Suppressed during auto-close: " + s.getClass().getSimpleName() + ": " + s.getMessage());
                }
            }
        }

        System.out.printf("  [POST-TWR STATE] Open file handles remaining: %d (Guaranteed ZERO leaks!)%n",
                ManualCloseReport.openFileHandles);
    }

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println(" Q3: Resource Safety, Debugging and Robust Code           ");
        System.out.println("==========================================================");

        List<String> stores = List.of("Store_Chennai_01.dat", "Store_Bangalore_02.dat", "Store_Hyderabad_03.dat");

        // 1. Manual Version with failure injection: Demonstrates handle leak
        ManualCloseReport.openFileHandles = 0;
        ManualCloseReport.generateReport(stores, true);

        // 2. Try-with-resources with identical failure: Demonstrates zero leak
        ManualCloseReport.openFileHandles = 0;
        TryWithResourcesReport.generateReport(stores, true, true);

        System.out.println("==========================================================");
    }
}
