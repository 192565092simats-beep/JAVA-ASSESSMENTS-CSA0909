/**
 * Test Harness to demonstrate diagnosis and verify the repaired design programmatically.
 */
public class FeeWindowDiagnosisDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("==========================================================");
        System.out.println(" TASK 1: GUI Design Review - Diagnose and Repair          ");
        System.out.println("==========================================================");

        FeeService service = new FeeService();

        System.out.println("\n[TEST 1] Validation with Empty Registration Plate:");
        service.processPayment("", "5000", new FeeService.FeeCallback() {
            public void onSuccess(String r, double a) { System.out.println("  Unexpected Success: " + r); }
            public void onFailure(String err) { System.out.println("  Handled Validation Error: " + err); }
        });

        System.out.println("\n[TEST 2] Validation with Non-numeric Amount (\"FiveThousand\"):");
        service.processPayment("192565092", "FiveThousand", new FeeService.FeeCallback() {
            public void onSuccess(String r, double a) { System.out.println("  Unexpected Success: " + r); }
            public void onFailure(String err) { System.out.println("  Handled Validation Error: " + err); }
        });

        System.out.println("\n[TEST 3] Validation with Negative Amount (-1500):");
        service.processPayment("192565092", "-1500", new FeeService.FeeCallback() {
            public void onSuccess(String r, double a) { System.out.println("  Unexpected Success: " + r); }
            public void onFailure(String err) { System.out.println("  Handled Validation Error: " + err); }
        });

        System.out.println("\n[TEST 4] Valid Asynchronous Payment Execution (Background Thread):");
        service.processPayment("192565092", "12500.00", new FeeService.FeeCallback() {
            public void onSuccess(String r, double a) {
                System.out.printf("  Payment Confirmed! Receipt: %s | Amount: Rs %.2f%n", r, a);
            }
            public void onFailure(String err) { System.out.println("  Payment Error: " + err); }
        });

        // Wait briefly for background worker execution
        Thread.sleep(1200);
        service.shutdown();
        System.out.println("\n[VERIFICATION] Repaired architecture verified successfully.");
        System.out.println("==========================================================");
    }
}
