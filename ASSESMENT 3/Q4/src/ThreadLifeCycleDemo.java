import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Q4 - Comprehensive Thread Life Cycle, Scheduling & join() Demonstration
 */
public class ThreadLifeCycleDemo {

    public static void demonstrateRunVsStart() {
        System.out.println("\n--- 1. Proof: Calling run() vs start() ---");
        ScannerStationThread directCall = new ScannerStationThread("Station-Direct", 5);

        System.out.println("Invoking directCall.run():");
        directCall.run(); // Executes synchronously on main thread!

        System.out.println("\nInvoking directCall.start():");
        ScannerStationThread threadCall = new ScannerStationThread("Station-Threaded", 5);
        threadCall.start(); // Spawns new OS thread
        try {
            threadCall.join();
        } catch (InterruptedException ignored) {}
    }

    public static void runMultiStationBatch() throws InterruptedException {
        System.out.println("\n--- 2. Parallel Scanning with 4 Stations using start() & join() ---");
        ScannerStationThread[] stations = new ScannerStationThread[4];
        for (int i = 0; i < 4; i++) {
            stations[i] = new ScannerStationThread("Scanner-" + (i + 1), 25);
        }

        // Track states
        System.out.println("State before start(): " + stations[0].getState() + " (NEW)");

        for (ScannerStationThread s : stations) {
            s.start();
        }

        System.out.println("State immediately after start(): " + stations[0].getState() + " (RUNNABLE / TIMED_WAITING)");

        // Wait for all 4 stations to complete using join()
        int totalScripts = 0;
        for (ScannerStationThread s : stations) {
            s.join(); // Main thread waits until station s terminates
            totalScripts += s.getScriptsCompleted();
        }

        System.out.println("State after join(): " + stations[0].getState() + " (TERMINATED)");
        System.out.println("All stations finished! Verified Total Answer Scripts: " + totalScripts + " (Expected: 100)");
    }

    public static void demonstrateDaemonThread() throws InterruptedException {
        System.out.println("\n--- 3. Daemon Thread Experiment ---");
        Thread backgroundCleaner = new Thread(() -> {
            try {
                while (true) {
                    System.out.println("  [Daemon-Cleaner] Background hardware telemetry running...");
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                System.out.println("  [Daemon-Cleaner] Interrupted.");
            }
        });

        backgroundCleaner.setDaemon(true); // Daemon thread abruptly terminates when user threads finish
        backgroundCleaner.start();
        Thread.sleep(120);
        System.out.println("  [Main] Main thread exiting. Daemon cleaner will be terminated by JVM automatically.");
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("==========================================================");
        System.out.println(" Q4: Thread Creation and the Thread Life Cycle            ");
        System.out.println("==========================================================");

        demonstrateRunVsStart();
        runMultiStationBatch();
        demonstrateDaemonThread();

        System.out.println("==========================================================");
    }
}
