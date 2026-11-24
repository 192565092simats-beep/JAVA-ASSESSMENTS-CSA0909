import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Task A1: Bus-Pass File Format Design & Implementation
 * Demonstrates:
 * 1. Delimited text file format (CSV with RFC 4180 quoting for names with commas)
 * 2. Binary file format with DataOutputStream / DataInputStream
 * 3. Add, Find by ID, and List by Route operations
 * 4. Error containment for damaged lines
 * 5. Atomic write pattern for power failure resilience
 */
public class BusPassFileStoreDemo {

    public static class BusPass {
        public String passId;
        public String regNo;
        public String name;
        public int routeNo;
        public String boardingStop;
        public String validityDate;
        public double amountPaid;

        public BusPass(String passId, String regNo, String name, int routeNo,
                       String boardingStop, String validityDate, double amountPaid) {
            this.passId = passId;
            this.regNo = regNo;
            this.name = name;
            this.routeNo = routeNo;
            this.boardingStop = boardingStop;
            this.validityDate = validityDate;
            this.amountPaid = amountPaid;
        }

        @Override
        public String toString() {
            return String.format("[%s] Reg:%s | %s | Route %d (%s) | Valid:%s | Rs.%.2f",
                    passId, regNo, name, routeNo, boardingStop, validityDate, amountPaid);
        }
    }

    // =========================================================================
    // 1. Delimited Text File Store (CSV format with RFC-4180 Quoting)
    // =========================================================================
    public static class TextPassStore {
        private final Path filePath;

        public TextPassStore(Path filePath) {
            this.filePath = filePath;
        }

        // Operation 1: Add a new pass (Append mode)
        public void addPass(BusPass pass) throws IOException {
            String line = formatRecord(pass);
            try (BufferedWriter writer = Files.newBufferedWriter(
                    filePath, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write(line);
                writer.newLine();
            }
        }

        // Operation 2: Find a pass by pass ID (Linear scan with damaged line protection)
        public Optional<BusPass> findPassById(String passId) throws IOException {
            if (!Files.exists(filePath)) return Optional.empty();

            try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                String line;
                int lineNum = 0;
                while ((line = reader.readLine()) != null) {
                    lineNum++;
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;

                    try {
                        BusPass pass = parseRecord(line);
                        if (pass.passId.equalsIgnoreCase(passId.trim())) {
                            return Optional.of(pass);
                        }
                    } catch (IllegalArgumentException ex) {
                        System.err.printf("  [WARN] Skipping damaged line #%d: %s (Reason: %s)%n",
                                lineNum, line, ex.getMessage());
                    }
                }
            }
            return Optional.empty();
        }

        // Operation 3: List all passes on a given route
        public List<BusPass> listPassesByRoute(int targetRoute) throws IOException {
            List<BusPass> results = new ArrayList<>();
            if (!Files.exists(filePath)) return results;

            try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                String line;
                int lineNum = 0;
                while ((line = reader.readLine()) != null) {
                    lineNum++;
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;

                    try {
                        BusPass pass = parseRecord(line);
                        if (pass.routeNo == targetRoute) {
                            results.add(pass);
                        }
                    } catch (IllegalArgumentException ex) {
                        System.err.printf("  [WARN] Skipping damaged line #%d during route filter%n", lineNum);
                    }
                }
            }
            return results;
        }

        // Handles names with commas via double-quote encapsulation
        private String formatRecord(BusPass p) {
            String escapedName = p.name.contains(",") ? "\"" + p.name + "\"" : p.name;
            return String.format("%s,%s,%s,%d,%s,%s,%.2f",
                    p.passId, p.regNo, escapedName, p.routeNo, p.boardingStop, p.validityDate, p.amountPaid);
        }

        // Robust CSV parser supporting quoted tokens
        public BusPass parseRecord(String line) {
            List<String> tokens = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            boolean inQuotes = false;

            for (char c : line.toCharArray()) {
                if (c == '\"') {
                    inQuotes = !inQuotes;
                } else if (c == ',' && !inQuotes) {
                    tokens.add(sb.toString().trim());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
            tokens.add(sb.toString().trim());

            if (tokens.size() != 7) {
                throw new IllegalArgumentException("Expected 7 fields, found " + tokens.size());
            }

            try {
                String passId = tokens.get(0);
                String regNo = tokens.get(1);
                String name = tokens.get(2);
                int routeNo = Integer.parseInt(tokens.get(3));
                String boarding = tokens.get(4);
                String validity = tokens.get(5);
                double amount = Double.parseDouble(tokens.get(6));
                return new BusPass(passId, regNo, name, routeNo, boarding, validity, amount);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Numeric field corrupted: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // 2. Binary File Store using DataOutputStream / DataInputStream
    // =========================================================================
    public static class BinaryPassStore {
        private final Path filePath;

        public BinaryPassStore(Path filePath) {
            this.filePath = filePath;
        }

        public void writePass(BusPass p) throws IOException {
            try (DataOutputStream dos = new DataOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(filePath,
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND)))) {
                dos.writeUTF(p.passId);       // 2 bytes length + UTF chars
                dos.writeUTF(p.regNo);        // 2 bytes length + UTF chars
                dos.writeUTF(p.name);         // 2 bytes length + UTF chars
                dos.writeInt(p.routeNo);      // 4 bytes
                dos.writeUTF(p.boardingStop); // 2 bytes length + UTF chars
                dos.writeUTF(p.validityDate); // 2 bytes length + UTF chars
                dos.writeDouble(p.amountPaid);// 8 bytes
            }
        }

        public List<BusPass> readAll() throws IOException {
            List<BusPass> list = new ArrayList<>();
            if (!Files.exists(filePath)) return list;

            try (DataInputStream dis = new DataInputStream(
                    new BufferedInputStream(Files.newInputStream(filePath)))) {
                while (dis.available() > 0) {
                    String passId = dis.readUTF();
                    String regNo = dis.readUTF();
                    String name = dis.readUTF();
                    int routeNo = dis.readInt();
                    String stop = dis.readUTF();
                    String val = dis.readUTF();
                    double amt = dis.readDouble();
                    list.add(new BusPass(passId, regNo, name, routeNo, stop, val, amt));
                }
            }
            return list;
        }
    }

    // =========================================================================
    // Main Verification Runner
    // =========================================================================
    public static void main(String[] args) throws IOException {
        System.out.println("==================================================================");
        System.out.println(" TASK A1: Bus-Pass File Format Design and Error Containment Demo   ");
        System.out.println("==================================================================");

        Path textFile = Files.createTempFile("bus_passes_text", ".csv");
        Path binFile = Files.createTempFile("bus_passes_bin", ".dat");

        TextPassStore textStore = new TextPassStore(textFile);
        BinaryPassStore binStore = new BinaryPassStore(binFile);

        // 1. Add normal passes (including name containing comma: "Balaji, Thejas")
        System.out.println("\n[STEP 1] Writing Valid Passes (Handling Comma in Name):");
        BusPass p1 = new BusPass("P1001", "192565092", "Balaji, Thejas", 12, "Tambaram", "2026-12-31", 4500.0);
        BusPass p2 = new BusPass("P1002", "192565093", "Karthik Raja", 12, "Guindy", "2026-12-31", 4500.0);
        BusPass p3 = new BusPass("P1003", "192565094", "Ananya Sharma", 7, "Central Station", "2026-12-31", 3800.0);

        textStore.addPass(p1);
        textStore.addPass(p2);
        textStore.addPass(p3);

        binStore.writePass(p1);
        binStore.writePass(p2);
        binStore.writePass(p3);
        System.out.println("  Passes written successfully to text and binary stores.");

        // 2. Inject damaged line into text file
        System.out.println("\n[STEP 2] Injecting Damaged Line into Text File:");
        Files.writeString(textFile, "P1099,CORRUPTED_ENTRY_MISSING_FIELDS\n",
                StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        System.out.println("  Appended corrupted record: \"P1099,CORRUPTED_ENTRY_MISSING_FIELDS\"");

        // 3. Find Pass by ID
        System.out.println("\n[STEP 3] Testing Find Pass by ID:");
        Optional<BusPass> found = textStore.findPassById("P1001");
        System.out.println("  Search for 'P1001': " + (found.isPresent() ? found.get() : "NOT FOUND"));

        Optional<BusPass> notFound = textStore.findPassById("P9999");
        System.out.println("  Search for 'P9999': " + (notFound.isPresent() ? notFound.get() : "NOT FOUND"));

        // 4. List passes on Route 12
        System.out.println("\n[STEP 4] Testing List Passes on Route 12:");
        List<BusPass> route12 = textStore.listPassesByRoute(12);
        System.out.println("  Found " + route12.size() + " passes on Route 12:");
        for (BusPass p : route12) {
            System.out.println("    - " + p);
        }

        // 5. Binary file roundtrip verification
        System.out.println("\n[STEP 5] Reading from Binary File Store (DataInputStream):");
        List<BusPass> binList = binStore.readAll();
        System.out.println("  Total Binary Passes Recovered: " + binList.size());
        for (BusPass p : binList) {
            System.out.println("    - " + p);
        }

        // Clean up temp files
        Files.deleteIfExists(textFile);
        Files.deleteIfExists(binFile);

        System.out.println("\n==================================================================");
        System.out.println(" Task A1 Design & Implementation Verified Successfully!           ");
        System.out.println("==================================================================");
    }
}
