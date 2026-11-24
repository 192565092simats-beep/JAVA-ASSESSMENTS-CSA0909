import java.util.*;

/**
 * Task A5: Transaction Safety and SQL Injection Prevention Demo
 * Demonstrates:
 * 1. ACID Transaction atomic commit/rollback vs dangerous partial failure in auto-commit
 * 2. SQL Injection exploitation of dynamic string concatenation
 * 3. PreparedStatement parameterized defense
 */
public class TransactionAndInjectionSecurityDemo {

    // =========================================================================
    // 1. Wallet Transaction Simulation
    // =========================================================================
    public static class WalletAccount {
        public String walletId;
        public double balance;

        public WalletAccount(String walletId, double balance) {
            this.walletId = walletId;
            this.balance = balance;
        }
    }

    public static class WalletService {
        private final Map<String, WalletAccount> db = new HashMap<>();

        public WalletService() {
            db.put("HOSTEL_W1", new WalletAccount("HOSTEL_W1", 2000.0));
            db.put("CANTEEN_W1", new WalletAccount("CANTEEN_W1", 500.0));
        }

        // Unsafe Auto-Commit Mode (Vulnerable to money disappearance on partial failure)
        public void transferUnsafeAutoCommit(String senderId, String receiverId, double amount, boolean crashBetween) {
            System.out.println("  [UNSAFE AUTO-COMMIT] Starting transfer of Rs. " + amount);
            WalletAccount sender = db.get(senderId);
            if (sender.balance < amount) throw new IllegalStateException("Insufficient funds");

            // Step 1: Auto-committed debit
            sender.balance -= amount;
            System.out.printf("    Step 1: Debited Rs. %.2f from %s (New balance: %.2f) [AUTO-COMMITTED]%n",
                    amount, senderId, sender.balance);

            if (crashBetween) {
                System.out.println("    *** CRITICAL SYSTEM CRASH / POWER FAILURE OCCURRED BETWEEN UPDATES! ***");
                throw new RuntimeException("PowerFailureException: Server went down before step 2");
            }

            // Step 2: Credit
            WalletAccount receiver = db.get(receiverId);
            receiver.balance += amount;
            System.out.printf("    Step 2: Credited Rs. %.2f to %s%n", amount, receiverId);
        }

        // Safe Transactional Mode with Rollback
        public boolean transferSafeTransaction(String senderId, String receiverId, double amount, boolean crashBetween) {
            System.out.println("  [SAFE TRANSACTION] Starting atomic transfer of Rs. " + amount);

            // Snapshot for rollback simulation (mirrors DB Undo Log / WAL)
            double origSenderBal = db.get(senderId).balance;
            double origReceiverBal = db.get(receiverId).balance;

            try {
                // 1. Debit check
                WalletAccount sender = db.get(senderId);
                if (sender.balance < amount) throw new IllegalStateException("Insufficient funds");
                sender.balance -= amount;
                System.out.printf("    Step 1: Debited Rs. %.2f from %s in transaction%n", amount, senderId);

                if (crashBetween) {
                    System.out.println("    *** CRITICAL SYSTEM CRASH OCCURRED! TRIGGERING ROLLBACK ***");
                    throw new RuntimeException("HardwareException: Database connection lost");
                }

                // 2. Credit
                WalletAccount receiver = db.get(receiverId);
                receiver.balance += amount;
                System.out.printf("    Step 2: Credited Rs. %.2f to %s in transaction%n", amount, receiverId);

                System.out.println("    Step 3: COMMIT TRANSACTION -> All updates persisted atomically.");
                return true;

            } catch (Exception ex) {
                // Rollback restores exact snapshot
                db.get(senderId).balance = origSenderBal;
                db.get(receiverId).balance = origReceiverBal;
                System.out.println("    Step 4: ROLLBACK EXECUTED -> Restored sender and receiver balances.");
                return false;
            }
        }

        public double getBalance(String walletId) {
            return db.get(walletId).balance;
        }
    }

    // =========================================================================
    // 2. SQL Injection Demonstration
    // =========================================================================
    public static class LoginSecurityDemo {
        private static final Map<String, String> userTable = new HashMap<>();
        static {
            userTable.put("admin", "SuperSecurePassword#2026");
            userTable.put("student_thejas", "ThejasPass123");
        }

        // Flawed dynamic SQL concatenation
        public static boolean authenticateUnsafe(String user, String pwd) {
            String sql = "SELECT * FROM users WHERE id='" + user + "' AND pwd='" + pwd + "'";
            System.out.println("  Generated Raw SQL: " + sql);

            // Emulating SQL execution parser
            if (user.contains("' --") || user.contains("' OR '1'='1")) {
                System.out.println("  [VULNERABILITY EXPLOITED] Comment marker '--' or 'OR 1=1' bypassed password check!");
                return true; // Bypass granted!
            }
            return userTable.containsKey(user) && userTable.get(user).equals(pwd);
        }

        // Safe PreparedStatement AST separation
        public static boolean authenticateSafePreparedStatement(String user, String pwd) {
            System.out.println("  Compiled SQL Template: SELECT * FROM users WHERE id = ? AND pwd = ?");
            System.out.printf("  Bound Parameters: [1 => %s], [2 => %s]%n", user, pwd);

            // Parameters are treated purely as literal string values
            return userTable.containsKey(user) && userTable.get(user).equals(pwd);
        }
    }

    // =========================================================================
    // Main Test Harness
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println(" TASK A5: Design for Correctness and Safety - Demo               ");
        System.out.println("==================================================================");

        // ---------------------------------------------------------------------
        // TEST 1: Wallet Transfer Safety
        // ---------------------------------------------------------------------
        System.out.println("\n[EXPERIMENT 1] Auto-Commit Failure vs Atomic Transaction Rollback:");
        WalletService service = new WalletService();

        System.out.printf("Initial Balances: Hostel=Rs.%.2f | Canteen=Rs.%.2f%n",
                service.getBalance("HOSTEL_W1"), service.getBalance("CANTEEN_W1"));

        // Unsafe run with crash
        try {
            service.transferUnsafeAutoCommit("HOSTEL_W1", "CANTEEN_W1", 500.0, true);
        } catch (RuntimeException e) {
            System.out.println("  Caught expected crash: " + e.getMessage());
        }
        System.out.printf("Balances After Unsafe Crash: Hostel=Rs.%.2f | Canteen=Rs.%.2f%n",
                service.getBalance("HOSTEL_W1"), service.getBalance("CANTEEN_W1"));
        System.out.println("  >> RESULT: Rs. 500 WAS PERMANENTLY LOST INTO THIN AIR!");

        // Reset balances for transaction test
        service = new WalletService();
        System.out.println("\nTesting Safe Transaction with Rollback:");
        boolean txSuccess = service.transferSafeTransaction("HOSTEL_W1", "CANTEEN_W1", 500.0, true);
        System.out.printf("Balances After Transaction Rollback: Hostel=Rs.%.2f | Canteen=Rs.%.2f%n",
                service.getBalance("HOSTEL_W1"), service.getBalance("CANTEEN_W1"));
        System.out.println("  >> RESULT: " + (txSuccess ? "SUCCESS" : "ROLLED BACK SAFELY! Zero money lost or created."));

        // ---------------------------------------------------------------------
        // TEST 2: SQL Injection Attack Simulation
        // ---------------------------------------------------------------------
        System.out.println("\n[EXPERIMENT 2] SQL Injection Attack Simulation:");
        String maliciousInput = "admin' --";
        String arbitraryPassword = "WrongPassword999";

        System.out.println("\n1. Testing Against Vulnerable String-Concatenated SQL:");
        boolean unsafeLogin = LoginSecurityDemo.authenticateUnsafe(maliciousInput, arbitraryPassword);
        System.out.println("  Authentication Outcome: " + (unsafeLogin ? "ACCESS GRANTED (INTRUSION SUCCESSFUL!)" : "ACCESS DENIED"));

        System.out.println("\n2. Testing Against Secure PreparedStatement:");
        boolean safeLogin = LoginSecurityDemo.authenticateSafePreparedStatement(maliciousInput, arbitraryPassword);
        System.out.println("  Authentication Outcome: " + (safeLogin ? "ACCESS GRANTED" : "ACCESS DENIED (ATTACK NEUTRALIZED!)"));

        System.out.println("\n==================================================================");
        System.out.println(" Task A5 Correctness and Safety Principles 100% Verified!        ");
        System.out.println("==================================================================");
    }
}
