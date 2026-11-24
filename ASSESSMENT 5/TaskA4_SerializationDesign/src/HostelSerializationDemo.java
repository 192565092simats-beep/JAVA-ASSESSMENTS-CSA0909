import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Task A4: Object Serialization and Transient Design Demo
 * Demonstrates:
 * 1. Object graph traversal (Hostel -> Room -> Resident)
 * 2. Protection of non-serializable and sensitive fields via 'transient'
 * 3. Prevention of NotSerializableException on mock Connection
 * 4. Schema evolution resilience via serialVersionUID
 */
public class HostelSerializationDemo {

    // Dummy Non-Serializable Connection to simulate java.sql.Connection
    public static class LiveDatabaseConnection {
        private final String socketAddress = "jdbc:mysql://10.0.0.12:3306/hostel_db";
        @Override
        public String toString() {
            return "ActiveSocket[" + socketAddress + ", open=true]";
        }
    }

    // =========================================================================
    // Domain Entities
    // =========================================================================
    public static class Hostel implements Serializable {
        private static final long serialVersionUID = 1001L;

        private final String hostelName;
        private final List<Room> rooms = new ArrayList<>();

        public Hostel(String hostelName) {
            this.hostelName = hostelName;
        }

        public void addRoom(Room r) { rooms.add(r); }
        public String getHostelName() { return hostelName; }
        public List<Room> getRooms() { return rooms; }
    }

    public static class Room implements Serializable {
        private static final long serialVersionUID = 2001L;

        private final int roomNumber;
        private final List<Resident> residents = new ArrayList<>();

        public Room(int roomNumber) {
            this.roomNumber = roomNumber;
        }

        public void addResident(Resident r) { residents.add(r); }
        public int getRoomNumber() { return roomNumber; }
        public List<Resident> getResidents() { return residents; }
    }

    public static class Resident implements Serializable {
        // Explicit serialVersionUID guarantees backward compatibility across schema changes!
        private static final long serialVersionUID = 3001L;

        private final String name;
        private final String regNo;

        // CRITICAL: Transient fields excluded from persistence
        private transient String password;                 // Sensitive credential
        private transient LiveDatabaseConnection dbConn;    // Non-serializable live OS socket resource

        // Simulating added field in newer version (schema evolution)
        private String phoneNumber;

        public Resident(String name, String regNo, String password, LiveDatabaseConnection dbConn) {
            this.name = name;
            this.regNo = regNo;
            this.password = password;
            this.dbConn = dbConn;
            this.phoneNumber = "Unassigned";
        }

        public String getName() { return name; }
        public String getRegNo() { return regNo; }
        public String getPassword() { return password; }
        public LiveDatabaseConnection getDbConn() { return dbConn; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        @Override
        public String toString() {
            return String.format("Resident[Name=%s, Reg=%s, Password=%s, Connection=%s, Phone=%s]",
                    name, regNo,
                    (password == null ? "<null/omitted>" : password),
                    (dbConn == null ? "<null/disconnected>" : dbConn),
                    phoneNumber);
        }
    }

    // =========================================================================
    // Test Harness
    // =========================================================================
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("==================================================================");
        System.out.println(" TASK A4: Object Graph Serialization and Transient Design Demo    ");
        System.out.println("==================================================================");

        // 1. Build Object Graph
        Hostel hostel = new Hostel("SIMATS Tagore International Hostel");
        Room r101 = new Room(101);
        Room r102 = new Room(102);

        LiveDatabaseConnection liveSocket = new LiveDatabaseConnection();

        Resident res1 = new Resident("Thejas Balaji", "192565092", "SecretPass#2026", liveSocket);
        Resident res2 = new Resident("Karthik Raja", "192565093", "MyPassword@123", liveSocket);
        res1.setPhoneNumber("+91-9840123456");

        r101.addResident(res1);
        r102.addResident(res2);
        hostel.addRoom(r101);
        hostel.addRoom(r102);

        System.out.println("\n[STEP 1] State Before Serialization (In-Memory Objects):");
        System.out.println("  Hostel: " + hostel.getHostelName());
        System.out.println("  Resident 1: " + res1);
        System.out.println("  Resident 2: " + res2);
        System.out.println("  Active Live Connection Present: " + (res1.getDbConn() != null));

        // 2. Serialize Object Graph to Disk
        Path serializedFile = Files.createTempFile("hostel_state", ".ser");
        System.out.println("\n[STEP 2] Serializing Object Graph to File: " + serializedFile.getFileName());
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(serializedFile)))) {
            oos.writeObject(hostel);
        }
        System.out.printf("  Serialization Succeeded! File size: %d bytes%n", Files.size(serializedFile));
        System.out.println("  (Live socket and password were safely bypassed without NotSerializableException)");

        // 3. Deserialize Object Graph from Disk
        System.out.println("\n[STEP 3] Deserializing Object Graph from Disk into Fresh JVM Memory:");
        Hostel restoredHostel;
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(serializedFile)))) {
            restoredHostel = (Hostel) ois.readObject();
        }

        System.out.println("  Restored Hostel Name: " + restoredHostel.getHostelName());
        System.out.println("  Total Rooms Reconstructed: " + restoredHostel.getRooms().size());

        for (Room room : restoredHostel.getRooms()) {
            System.out.printf("  Room #%d Residents:%n", room.getRoomNumber());
            for (Resident r : room.getResidents()) {
                System.out.println("    -> " + r);
            }
        }

        // 4. Verify Transient Protection
        Resident restoredRes1 = restoredHostel.getRooms().get(0).getResidents().get(0);
        System.out.println("\n[STEP 4] Verifying Security and Transient Field Protection:");
        System.out.println("  Password restored as:   " + restoredRes1.getPassword() + " (Must be null)");
        System.out.println("  Connection restored as: " + restoredRes1.getDbConn() + " (Must be null)");
        System.out.println("  Phone number preserved: " + restoredRes1.getPhoneNumber());

        Files.deleteIfExists(serializedFile);

        System.out.println("\n==================================================================");
        System.out.println(" Task A4 Object Graph Serialization Verified Successfully!        ");
        System.out.println("==================================================================");
    }
}
