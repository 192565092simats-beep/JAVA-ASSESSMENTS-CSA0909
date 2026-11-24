import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Q6 Part A - Hostel Wallet Transfer: Deadlock Recreation and Lock-Ordering Prevention
 */
public class WalletTransferDemo {

    static class WalletAccount {
        final String id;
        double balance;
        final Lock lock = new ReentrantLock();

        WalletAccount(String id, double balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    // Deadlock-vulnerable transfer: locks from then to in call-argument order
    public static void deadlockingTransfer(WalletAccount from, WalletAccount to, double amount) {
        synchronized (from) {
            System.out.printf("  [%s] Acquired lock on %s. Waiting for %s...%n",
                    Thread.currentThread().getName(), from.id, to.id);
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}

            synchronized (to) {
                from.balance -= amount;
                to.balance += amount;
                System.out.printf("  [%s] Transferred Rs %.2f from %s to %s%n",
                        Thread.currentThread().getName(), amount, from.id, to.id);
            }
        }
    }

    // Safe transfer using global lock ordering (Account ID lexicographical comparison)
    public static void safeOrderedTransfer(WalletAccount acc1, WalletAccount acc2, double amount, boolean isAcc1From) {
        WalletAccount firstLock = acc1.id.compareTo(acc2.id) < 0 ? acc1 : acc2;
        WalletAccount secondLock = acc1.id.compareTo(acc2.id) < 0 ? acc2 : acc1;

        synchronized (firstLock) {
            synchronized (secondLock) {
                WalletAccount from = isAcc1From ? acc1 : acc2;
                WalletAccount to = isAcc1From ? acc2 : acc1;
                from.balance -= amount;
                to.balance += amount;
                System.out.printf("  [SAFE-ORDERED] %s -> %s: Transferred Rs %.2f (Locks acquired in strict order: %s, %s)%n",
                        from.id, to.id, amount, firstLock.id, secondLock.id);
            }
        }
    }

    // Safe transfer using ReentrantLock tryLock with timeout
    public static boolean safeTryLockTransfer(WalletAccount from, WalletAccount to, double amount) throws InterruptedException {
        long timeoutMs = 100;
        if (from.lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS)) {
            try {
                if (to.lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS)) {
                    try {
                        from.balance -= amount;
                        to.balance += amount;
                        System.out.printf("  [TRY-LOCK SUCCESS] Transferred Rs %.2f from %s to %s%n", amount, from.id, to.id);
                        return true;
                    } finally {
                        to.lock.unlock();
                    }
                }
            } finally {
                from.lock.unlock();
            }
        }
        System.out.printf("  [TRY-LOCK BACKOFF] Timeout acquiring lock for %s -> %s. Backed off to prevent deadlock!%n", from.id, to.id);
        return false;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("==========================================================");
        System.out.println(" Q6 Part A: Hostel Wallet Transfers & Deadlock Prevention ");
        System.out.println("==========================================================");

        WalletAccount accA = new WalletAccount("ACC_001", 1000.0);
        WalletAccount accB = new WalletAccount("ACC_002", 1000.0);

        System.out.println("\n1. Testing Safe Lock-Ordering Transfers (Bidirectional):");
        Thread t1 = new Thread(() -> safeOrderedTransfer(accA, accB, 100.0, true));
        Thread t2 = new Thread(() -> safeOrderedTransfer(accB, accA, 50.0, true));

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.printf("  Final Balances: %s = Rs %.2f, %s = Rs %.2f%n",
                accA.id, accA.balance, accB.id, accB.balance);

        System.out.println("\n2. Testing ReentrantLock tryLock() with Timeout Alternative:");
        safeTryLockTransfer(accA, accB, 200.0);
        System.out.printf("  Final Balances: %s = Rs %.2f, %s = Rs %.2f%n",
                accA.id, accA.balance, accB.id, accB.balance);

        System.out.println("==========================================================");
    }
}
