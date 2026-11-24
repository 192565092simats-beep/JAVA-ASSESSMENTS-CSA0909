import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Task A2: Buffering Cost Prediction and Empirical Benchmark
 * Demonstrates:
 * 1. Unbuffered single-byte FileInputStream read cost
 * 2. BufferedInputStream chunked user-space buffering
 * 3. Exact system call comparison and throughput measurement
 */
public class BufferingCostBenchmarkDemo {

    public static void main(String[] args) throws IOException {
        System.out.println("==================================================================");
        System.out.println(" TASK A2: Don't Read What You Don't Need - Buffering Analysis    ");
        System.out.println("==================================================================");

        // Create a 2 MB test file with 40,000 lines for controlled benchmark
        Path tempFile = Files.createTempFile("attendance_benchmark", ".log");
        int targetLines = 40000;
        try (BufferedWriter bw = Files.newBufferedWriter(tempFile)) {
            for (int i = 1; i <= targetLines; i++) {
                bw.write("2026-11-24 08:30:00,STUDENT_" + i + ",PRESENT,BIOMETRIC_GATE_04\n");
            }
        }
        long fileSizeBytes = Files.size(tempFile);
        System.out.printf("[BENCHMARK DATASET] Generated %d lines (%d bytes / %.2f MB)%n",
                targetLines, fileSizeBytes, fileSizeBytes / (1024.0 * 1024.0));

        // ---------------------------------------------------------------------
        // 1. Unbuffered Read: 1 Byte at a Time (FileInputStream.read())
        // ---------------------------------------------------------------------
        System.out.println("\n[EXPERIMENT 1] Unbuffered Line Count (FileInputStream.read() 1 byte/call):");
        long startUnbuf = System.currentTimeMillis();
        long unbufCount = 0;
        long unbufReadCalls = 0;
        try (FileInputStream fis = new FileInputStream(tempFile.toFile())) {
            int b;
            while ((b = fis.read()) != -1) {
                unbufReadCalls++;
                if (b == '\n') {
                    unbufCount++;
                }
            }
            unbufReadCalls++; // EOF check
        }
        long elapsedUnbuf = System.currentTimeMillis() - startUnbuf;
        System.out.printf("  Lines Counted:    %d%n", unbufCount);
        System.out.printf("  Total read() calls: %d (1 syscall per byte)%n", unbufReadCalls);
        System.out.printf("  Time Elapsed:     %d ms%n", elapsedUnbuf);
        System.out.printf("  Throughput:       %.2f KB/s%n",
                (fileSizeBytes / 1024.0) / (Math.max(1, elapsedUnbuf) / 1000.0));

        // ---------------------------------------------------------------------
        // 2. Buffered Read: BufferedInputStream (8 KB Buffer)
        // ---------------------------------------------------------------------
        System.out.println("\n[EXPERIMENT 2] Buffered Line Count (BufferedInputStream 8192-byte buffer):");
        long startBuf = System.currentTimeMillis();
        long bufCount = 0;
        long estimatedSyscalls = (long) Math.ceil((double) fileSizeBytes / 8192.0);
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(tempFile.toFile()))) {
            int b;
            while ((b = bis.read()) != -1) {
                if (b == '\n') {
                    bufCount++;
                }
            }
        }
        long elapsedBuf = System.currentTimeMillis() - startBuf;
        System.out.printf("  Lines Counted:    %d%n", bufCount);
        System.out.printf("  Estimated Syscalls to OS: %d (down from %d)%n", estimatedSyscalls, unbufReadCalls);
        System.out.printf("  Time Elapsed:     %d ms%n", elapsedBuf);
        System.out.printf("  Throughput:       %.2f MB/s%n",
                (fileSizeBytes / (1024.0 * 1024.0)) / (Math.max(1, elapsedBuf) / 1000.0));

        // Speedup calculation
        double speedup = (double) Math.max(1, elapsedUnbuf) / Math.max(1, elapsedBuf);
        System.out.printf("%n[ANALYSIS] Buffering provided a %.1fx speedup and reduced OS system calls by %.3f%%%n",
                speedup, (1.0 - (double) estimatedSyscalls / unbufReadCalls) * 100.0);

        Files.deleteIfExists(tempFile);
        System.out.println("==================================================================");
        System.out.println(" Task A2 Buffering Benchmark Completed Successfully!             ");
        System.out.println("==================================================================");
    }
}
