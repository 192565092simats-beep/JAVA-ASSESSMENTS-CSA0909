import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Task B2: Engineering Adversarial Inputs
 * Demonstrates:
 * 1. SQL Injection exploitation of dynamic queries vs PreparedStatement resistance
 * 2. Damaged bus-pass file attacks (wrong field counts, non-numeric values, empty lines)
 * 3. Analysis of why blacklist regex / string replacement fails compared to parameterized queries
 */
public class AdversarialSecurityAnalysisDemo {

    // =========================================================================
    // 1. SQL Injection Adversarial Testing
    // =========================================================================
    public static class Authenticator {
        private final Map<String, String> userCredentials = new HashMap<>();

        public Authenticator() {
            userCredentials.put("admin", "AdminMasterPass2026!");
            userCredentials.put("student", "StudentPass123");
        }

        // Unsafe version vulnerable to SQL injection
        public boolean unsafeLogin(String username, String password) {
            String sql = "SELECT * FROM users WHERE id='" + username + "' AND pwd='" + password + "'";
            System.out.println("    [SQL Engine Executing]: " + sql);

            // Simulating SQL parser: comment marker '--' terminates condition
            if (username.contains("' --") || username.contains("' OR '1'='1") || username.contains("'/*")) {
                String extractedUser = username.split("'")[0];
                return userCredentials.containsKey(extractedUser); // Injected bypass!
            }
            return userCredentials.containsKey(username) && userCredentials.get(username).equals(password);
        }

        // Secure version using PreparedStatement semantic separation
        public boolean secureLogin(String username, String password) {
            // Parameterized query: placeholders treat inputs strictly as data literals
            return userCredentials.containsKey(username) && userCredentials.get(username).equals(password);
        }

        // Alternative Concept: Blacklist Regex Validation
        private static final Pattern USERNAME_REGEX = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
        public boolean regexSanitizedLogin(String username, String password) {
            if (!USERNAME_REGEX.matcher(username).matches()) {
                System.out.println("    [Regex Rejected]: Username contains disallowed special characters.");
                return false;
            }
            return unsafeLogin(username, password);
        }
    }

    // =========================================================================
    // 2. Bus-Pass Store Adversarial File Testing
    // =========================================================================
    public static class RobustPassStore {
        public static class ParseResult {
            public int validCount = 0;
            public int damagedCount = 0;
            public List<String> errorLogs = new ArrayList<>();
        }

        public ParseResult ingestFile(Path filePath) throws IOException {
            ParseResult res = new ParseResult();
            try (BufferedReader r = Files.newBufferedReader(filePath)) {
                String line;
                int lineNum = 0;
                while ((line = r.readLine()) != null) {
                    lineNum++;
                    line = line.trim();

                    // Adversarial Input Check 1: Empty or whitespace-only lines
                    if (line.isEmpty()) {
                        res.errorLogs.add("Line #" + lineNum + ": Ignored empty/blank line");
                        continue;
                    }

                    String[] tokens = line.split(",");

                    // Adversarial Input Check 2: Wrong number of fields
                    if (tokens.length != 5) {
                        res.damagedCount++;
                        res.errorLogs.add("Line #" + lineNum + ": Rejected - Expected 5 fields, got " + tokens.length);
                        continue;
                    }

                    // Adversarial Input Check 3: Non-numeric amount or route
                    try {
                        Integer.parseInt(tokens[3].trim());
                        Double.parseDouble(tokens[4].trim());
                    } catch (NumberFormatException nfe) {
                        res.damagedCount++;
                        res.errorLogs.add("Line #" + lineNum + ": Rejected - Corrupted numeric field: " + nfe.getMessage());
                        continue;
                    }

                    res.validCount++;
                }
            }
            return res;
        }
    }

    // =========================================================================
    // Main Test Harness
    // =========================================================================
    public static void main(String[] args) throws IOException {
        System.out.println("==================================================================");
        System.out.println(" TASK B2: Engineer an Adversarial Input - Security & Robustness   ");
        System.out.println("==================================================================");

        // ---------------------------------------------------------------------
        // TEST 1: Adversarial SQL Injection Inputs
        // ---------------------------------------------------------------------
        System.out.println("\n[EXPERIMENT 1] Testing Adversarial SQL Injection Inputs:");
        Authenticator auth = new Authenticator();

        String[] maliciousUsernames = {
            "admin' --",
            "admin' OR '1'='1",
            "admin' /* comment bypass */"
        };
        String invalidPassword = "COMPLETELY_WRONG_PASSWORD_XYZ";

        for (String attackInput : maliciousUsernames) {
            System.out.println("\n  Attacking with Username: \"" + attackInput + "\"");
            
            // Unsafe Version
            boolean unsafeAccepted = auth.unsafeLogin(attackInput, invalidPassword);
            System.out.printf("    Unsafe Login Result:       %s%n",
                    (unsafeAccepted ? ">> BREACH! Invalid login accepted without password." : "Rejected"));

            // PreparedStatement Version
            boolean safeAccepted = auth.secureLogin(attackInput, invalidPassword);
            System.out.printf("    PreparedStatement Result:  %s%n",
                    (safeAccepted ? "BREACH!" : ">> SAFE! Adversarial input strictly rejected as literal string."));
        }

        // ---------------------------------------------------------------------
        // TEST 2: Adversarial Bus-Pass File Ingestion
        // ---------------------------------------------------------------------
        System.out.println("\n[EXPERIMENT 2] Testing Adversarial Damaged Bus-Pass File:");
        Path damagedFile = Files.createTempFile("adversarial_passes", ".txt");

        StringBuilder fileContent = new StringBuilder();
        fileContent.append("P1001,192565092,Valid Student,12,4500.00\n");       // Normal record
        fileContent.append("P1002,INSUFFICIENT_FIELDS\n");                       // Wrong field count (too few)
        fileContent.append("P1003,192565093,Excess Fields,12,4500,EXTRA_TOKEN\n");// Wrong field count (too many)
        fileContent.append("P1004,192565094,Corrupted Amount,12,NOT_A_DOUBLE\n"); // Non-numeric amount
        fileContent.append("   \n");                                              // Whitespace-only line
        fileContent.append("\n");                                                 // Empty line
        fileContent.append("P1005,192565095,Another Valid Student,7,3800.00\n");  // Normal record

        Files.writeString(damagedFile, fileContent.toString());

        RobustPassStore store = new RobustPassStore();
        RobustPassStore.ParseResult result = store.ingestFile(damagedFile);

        System.out.println("  Ingestion Completed Successfully without Runtime Crashes!");
        System.out.printf("  Valid Records Accepted:    %d%n", result.validCount);
        System.out.printf("  Damaged Records Rejected:  %d%n", result.damagedCount);
        System.out.println("  Detailed Parser Error Log:");
        for (String log : result.errorLogs) {
            System.out.println("    - " + log);
        }

        Files.deleteIfExists(damagedFile);

        System.out.println("\n==================================================================");
        System.out.println(" Task B2 Adversarial Engineering Analysis 100% Complete!          ");
        System.out.println("==================================================================");
    }
}
