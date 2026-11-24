import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Q5 - In-depth Race Condition, Synchronization Strategies & Benchmark
 */
public class RaceConditionStudy {

    // 1. Unsynchronized version (reproduces double-booking race condition)
    static class UnsynchronizedBooking {
        private final boolean[] booked;
        private int bookingsMade = 0;

        UnsynchronizedBooking(int seats) { booked = new boolean[seats]; }

        public boolean book(int seat) {
            if (!booked[seat]) {
                Thread.yield(); // Encourages thread context switch between check and act!
                booked[seat] = true;
                bookingsMade++;
                return true;
            }
            return false;
        }

        public int getBookingsMade() { return bookingsMade; }
    }

    // 2. Synchronized block version (fine-grained locking on seat object)
    static class SynchronizedBlockBooking {
        private final boolean[] booked;
        private final Object[] seatLocks;
        private final AtomicInteger bookingsMade = new AtomicInteger(0);

        SynchronizedBlockBooking(int seats) {
            booked = new boolean[seats];
            seatLocks = new Object[seats];
            for (int i = 0; i < seats; i++) seatLocks[i] = new Object();
        }

        public boolean book(int seat) {
            synchronized (seatLocks[seat]) { // Locks ONLY the individual seat!
                if (!booked[seat]) {
                    booked[seat] = true;
                    bookingsMade.incrementAndGet();
                    return true;
                }
            }
            return false;
        }

        public int getBookingsMade() { return bookingsMade.get(); }
    }

    // 3. Lock-free AtomicBoolean CAS version
    static class AtomicBooking {
        private final AtomicBoolean[] booked;
        private final AtomicInteger bookingsMade = new AtomicInteger(0);

        AtomicBooking(int seats) {
            booked = new AtomicBoolean[seats];
            for (int i = 0; i < seats; i++) booked[i] = new AtomicBoolean(false);
        }

        public boolean book(int seat) {
            // Hardware-level atomic Compare-And-Swap (CAS)
            if (booked[seat].compareAndSet(false, true)) {
                bookingsMade.incrementAndGet();
                return true;
            }
            return false;
        }

        public int getBookingsMade() { return bookingsMade.get(); }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("==========================================================");
        System.out.println(" Q5: Race Conditions in a Cinema Seat-Booking System      ");
        System.out.println("==========================================================");

        // Experiment 1: Unsynchronized Race Condition Test over 5 runs with simultaneous start latch
        System.out.println("\n[EXPERIMENT 1] Unsynchronized Booking (Target: 10 unique seats):");
        for (int run = 1; run <= 5; run++) {
            UnsynchronizedBooking unsync = new UnsynchronizedBooking(10);
            CountDownLatch latch = new CountDownLatch(1);
            Thread[] threads = new Thread[100];
            for (int i = 0; i < 100; i++) {
                final int s = i % 10;
                threads[i] = new Thread(() -> {
                    try {
                        latch.await(); // Release all 100 threads simultaneously
                        unsync.book(s);
                    } catch (InterruptedException ignored) {}
                });
                threads[i].start();
            }
            latch.countDown(); // Release threads
            for (Thread t : threads) t.join();
            System.out.printf("  Run %d: Seats sold = %d %s%n", run, unsync.getBookingsMade(),
                    unsync.getBookingsMade() > 10 ? "(RACE CONDITION DETECTED! Overbooked!)" : "(Passed by luck)");
        }

        // Experiment 2: Benchmark 10,000 bookings under high contention
        System.out.println("\n[EXPERIMENT 2] High-Contention Benchmark (10,000 Bookings across 50 seats):");
        int NUM_THREADS = 10000;
        int NUM_SEATS = 50;

        // Synchronized Block Benchmark
        SynchronizedBlockBooking blockBooking = new SynchronizedBlockBooking(NUM_SEATS);
        Thread[] blockThreads = new Thread[NUM_THREADS];
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_THREADS; i++) {
            final int s = i % NUM_SEATS;
            blockThreads[i] = new Thread(() -> blockBooking.book(s));
            blockThreads[i].start();
        }
        for (Thread t : blockThreads) t.join();
        long blockTime = System.currentTimeMillis() - start;
        System.out.printf("  Synchronized Block: Time = %d ms, Seats sold = %d (Guaranteed exact %d)%n",
                blockTime, blockBooking.getBookingsMade(), NUM_SEATS);

        // Atomic CAS Benchmark
        AtomicBooking atomicBooking = new AtomicBooking(NUM_SEATS);
        Thread[] atomicThreads = new Thread[NUM_THREADS];
        start = System.currentTimeMillis();
        for (int i = 0; i < NUM_THREADS; i++) {
            final int s = i % NUM_SEATS;
            atomicThreads[i] = new Thread(() -> atomicBooking.book(s));
            atomicThreads[i].start();
        }
        for (Thread t : atomicThreads) t.join();
        long atomicTime = System.currentTimeMillis() - start;
        System.out.printf("  AtomicBoolean CAS:  Time = %d ms, Seats sold = %d (Guaranteed exact %d)%n",
                atomicTime, atomicBooking.getBookingsMade(), NUM_SEATS);

        System.out.println("==========================================================");
    }
}
