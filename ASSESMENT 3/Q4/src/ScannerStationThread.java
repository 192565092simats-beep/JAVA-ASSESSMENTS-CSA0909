/**
 * Approach 1: Creating thread by extending java.lang.Thread
 */
public class ScannerStationThread extends Thread {
    private final String stationName;
    private final int scriptsToScan;
    private int scriptsCompleted = 0;

    public ScannerStationThread(String stationName, int scriptsToScan) {
        super(stationName); // Set thread name
        this.stationName = stationName;
        this.scriptsToScan = scriptsToScan;
    }

    @Override
    public void run() {
        System.out.printf("  [%s] Started scanning on thread: %s (Priority: %d, Daemon: %b)%n",
                stationName, Thread.currentThread().getName(),
                Thread.currentThread().getPriority(), Thread.currentThread().isDaemon());
        try {
            for (int i = 1; i <= scriptsToScan; i++) {
                Thread.sleep(15); // Simulate scanning delay (TIMED_WAITING state)
                scriptsCompleted++;
            }
        } catch (InterruptedException e) {
            System.err.printf("  [%s] Scanning interrupted!%n", stationName);
            Thread.currentThread().interrupt();
        }
        System.out.printf("  [%s] Finished scanning %d scripts.%n", stationName, scriptsCompleted);
    }

    public int getScriptsCompleted() { return scriptsCompleted; }
}
