import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/**
 * Task B1: Complete the Code - PassStore.java
 * Fulfills TODO in find() method using try-with-resources and BufferedReader.
 */
public class PassStore {
    private final Path file;

    PassStore(Path file) {
        this.file = file;
    }

    public void add(String passId, String regNo, String name, int routeNo, double amount) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(String.join(",", passId, regNo, name, String.valueOf(routeNo), String.valueOf(amount)));
            w.newLine();
        }
    }

    /**
     * [TODO Completed]:
     * Returns the five fields of the pass with the given passId, or null if not found.
     * Uses a BufferedReader inside try-with-resources. Reads each line, splits by comma,
     * validates field count, and matches passId. Corrupted lines are gracefully skipped.
     */
    String[] find(String passId) throws IOException {
        if (!Files.exists(file)) return null;

        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] fields = line.split(",");
                // Defensive check: Only examine lines with exactly 5 tokens
                if (fields.length == 5 && fields[0].equals(passId)) {
                    return fields;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        Path f = Files.createTempFile("passes", ".txt");
        PassStore store = new PassStore(f);

        store.add("P1001", "192311001", "Arun", 12, 4500);
        Files.writeString(f, "P1002,DAMAGED LINE\n", StandardOpenOption.APPEND);
        store.add("P1003", "192311003", "Divya", 7, 3800);

        System.out.println(Arrays.toString(store.find("P1003")));
        System.out.println(Arrays.toString(store.find("P9999")));

        Files.delete(f);
    }
}
