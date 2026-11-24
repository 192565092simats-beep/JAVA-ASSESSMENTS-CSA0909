/**
 * Approach 2: Creating thread task by implementing java.lang.Runnable
 */
public class ScannerStationRunnable implements Runnable {
    private final String stationName;
    private final int scriptsToScan;
    private int scriptsCompleted = 0;

    public ScannerStationRunnable(String stationName, int scriptsToScan) {
        this.stationName = stationName;
        this.scriptsToScan = scriptsToScan;
    }

    @Override
    public void run() {
        System.out.printf("  [Runnable-%s] Running on: %s%n",
                stationName, Thread.currentThread().getName());
        try {
            for (int i = 1; i <= scriptsToScan; i++) {
                Thread.sleep(15);
                scriptsCompleted++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.printf("  [Runnable-%s] Complete (%d scripts).%n", stationName, scriptsCompleted);
    }

    public int getScriptsCompleted() { return scriptsCompleted; }
}
