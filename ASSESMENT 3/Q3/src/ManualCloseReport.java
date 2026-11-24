import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Q3 - Daily Sales Report Generator: Manual close() with deliberate resource leak demonstration.
 */
public class ManualCloseReport {

    // Simulates tracking OS open file handles
    public static int openFileHandles = 0;

    static class TrackedFileReader extends BufferedReader {
        private final String fileName;
        private boolean closed = false;

        public TrackedFileReader(String fileName, Reader in) {
            super(in);
            this.fileName = fileName;
            openFileHandles++;
            System.out.printf("    [OPEN] %s opened. Total active file handles: %d%n", fileName, openFileHandles);
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                closed = true;
                openFileHandles--;
                System.out.printf("    [CLOSE] %s closed. Total active file handles: %d%n", fileName, openFileHandles);
            }
            super.close();
        }
    }

    public static void generateReport(List<String> storeNames, boolean injectFailure) {
        System.out.println("\n--- Running Manual try-catch-finally Report Generator ---");
        List<TrackedFileReader> readers = new ArrayList<>();
        BufferedWriter writer = null;

        try {
            // Simulate opening summary report output writer
            writer = new BufferedWriter(new StringWriter());
            openFileHandles++;
            System.out.println("  [WRITER OPEN] summary_report.txt opened. Active handles: " + openFileHandles);

            for (int i = 0; i < storeNames.size(); i++) {
                String store = storeNames.get(i);
                TrackedFileReader reader = new TrackedFileReader(store, new StringReader("Sales data for " + store));
                readers.add(reader);

                // Simulate data processing failure midway on store 3
                if (injectFailure && i == 2) {
                    System.err.println("  [ERROR INJECTED] Corrupted sales data format in: " + store);
                    throw new RuntimeException("NumberFormatException: Bad sales ledger entry in " + store);
                }
            }
            System.out.println("  [SUCCESS] All store files processed successfully.");

        } catch (Exception e) {
            System.err.println("  [REPORT CRASH] Aborting: " + e.getMessage());

        } finally {
            // Manual closing vulnerability: If any close() throws or if collection is incomplete, leaks occur!
            System.out.println("  [FINALLY BLOCK] Attempting manual cleanup...");
            // Flawed manual close: developer closes writer, but fails to close all open file handles stored in list
            try {
                if (writer != null) {
                    writer.close();
                    openFileHandles--;
                    System.out.println("    [WRITER CLOSED] summary_report.txt closed.");
                }
            } catch (IOException e) {
                System.err.println("Error closing writer: " + e.getMessage());
            }
            // Notice: The store file readers were NOT closed upon error!
            System.out.printf("  [LEAK REPORT] Open file descriptors remaining unclosed: %d (LEAK!)%n", openFileHandles);
        }
    }
}
