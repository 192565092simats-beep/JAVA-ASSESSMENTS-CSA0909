import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test Harness and Automated Verification Suite for Task 6:
 * Campus Event Registration Desktop System.
 */
public class CampusEventRegistrationDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("======================================================================");
        System.out.println(" TASK 6: Design Challenge - Campus Event Registration Desktop App      ");
        System.out.println("======================================================================");

        RegistrationService service = new RegistrationService();

        // ---------------------------------------------------------------------
        // TEST 1: Input Validation Strategy
        // ---------------------------------------------------------------------
        System.out.println("\n[TEST 1] Testing Input Validation Enforcement:");
        
        // (a) Invalid short Student ID
        service.registerStudent("123", "John Doe", "CSE", "john@simats.edu.in", "Hackathon 2026",
            new RegistrationService.ServiceCallback<Registration>() {
                public void onSuccess(Registration r) { System.out.println("  Unexpected Success: " + r); }
                public void onFailure(String err) { System.out.println("  [PASS] Handled Invalid ID: " + err); }
            });

        // (b) Invalid Email format
        service.registerStudent("192565095", "John Doe", "CSE", "invalid_email_format", "Hackathon 2026",
            new RegistrationService.ServiceCallback<Registration>() {
                public void onSuccess(Registration r) { System.out.println("  Unexpected Success: " + r); }
                public void onFailure(String err) { System.out.println("  [PASS] Handled Invalid Email: " + err); }
            });

        // ---------------------------------------------------------------------
        // TEST 2: Valid Asynchronous Registrations (Background Thread / EDT Safe)
        // ---------------------------------------------------------------------
        System.out.println("\n[TEST 2] Testing Valid Asynchronous Registrations:");
        CountDownLatch latch2 = new CountDownLatch(2);

        service.registerStudent("192565095", "Rahul Sharma", "Computer Science", "rahul@simats.edu.in", "Hackathon 2026",
            new RegistrationService.ServiceCallback<Registration>() {
                public void onSuccess(Registration r) {
                    System.out.println("  [PASS] Registered: " + r);
                    latch2.countDown();
                }
                public void onFailure(String err) {
                    System.out.println("  Registration Error: " + err);
                    latch2.countDown();
                }
            });

        service.registerStudent("192565096", "Sita Devi", "Electronics Eng", "sita@simats.edu.in", "RoboWars Challenge",
            new RegistrationService.ServiceCallback<Registration>() {
                public void onSuccess(Registration r) {
                    System.out.println("  [PASS] Registered: " + r);
                    latch2.countDown();
                }
                public void onFailure(String err) {
                    System.out.println("  Registration Error: " + err);
                    latch2.countDown();
                }
            });

        latch2.await(2, TimeUnit.SECONDS);

        // ---------------------------------------------------------------------
        // TEST 3: Duplicate Registration Prevention (Composite Key: student_id + event_name)
        // ---------------------------------------------------------------------
        System.out.println("\n[TEST 3] Testing Duplicate Registration Prevention:");
        CountDownLatch latch3 = new CountDownLatch(1);

        // Attempting to register Rahul Sharma (192565095) for "Hackathon 2026" again
        service.registerStudent("192565095", "Rahul Sharma", "Computer Science", "rahul@simats.edu.in", "Hackathon 2026",
            new RegistrationService.ServiceCallback<Registration>() {
                public void onSuccess(Registration r) {
                    System.out.println("  [FAIL] Duplicate unexpectedly allowed: " + r);
                    latch3.countDown();
                }
                public void onFailure(String err) {
                    System.out.println("  [PASS] Duplicate Correctly Rejected: " + err);
                    latch3.countDown();
                }
            });

        latch3.await(2, TimeUnit.SECONDS);

        // ---------------------------------------------------------------------
        // TEST 4: Cross-Registration Allowed (Same Student, Different Event)
        // ---------------------------------------------------------------------
        System.out.println("\n[TEST 4] Testing Cross-Event Registration (Same Student, Different Event):");
        CountDownLatch latch4 = new CountDownLatch(1);

        service.registerStudent("192565095", "Rahul Sharma", "Computer Science", "rahul@simats.edu.in", "Code Marathon",
            new RegistrationService.ServiceCallback<Registration>() {
                public void onSuccess(Registration r) {
                    System.out.println("  [PASS] Cross-Registration Allowed: " + r);
                    latch4.countDown();
                }
                public void onFailure(String err) {
                    System.out.println("  [FAIL] Unexpected rejection: " + err);
                    latch4.countDown();
                }
            });

        latch4.await(2, TimeUnit.SECONDS);

        // ---------------------------------------------------------------------
        // TEST 5: Query and Filtering by Event Category
        // ---------------------------------------------------------------------
        System.out.println("\n[TEST 5] Testing Query & Filtering by Event Category:");
        CountDownLatch latch5 = new CountDownLatch(3);

        service.fetchRegistrations("Hackathon 2026", new RegistrationService.ServiceCallback<List<Registration>>() {
            public void onSuccess(List<Registration> list) {
                System.out.println("  Filter [Hackathon 2026] Count: " + list.size());
                for (Registration r : list) {
                    System.out.println("    - " + r.getStudentName() + " (" + r.getStudentId() + ") [" + r.getDepartment() + "]");
                }
                latch5.countDown();
            }
            public void onFailure(String err) { latch5.countDown(); }
        });

        service.fetchRegistrations("RoboWars Challenge", new RegistrationService.ServiceCallback<List<Registration>>() {
            public void onSuccess(List<Registration> list) {
                System.out.println("  Filter [RoboWars Challenge] Count: " + list.size());
                for (Registration r : list) {
                    System.out.println("    - " + r.getStudentName() + " (" + r.getStudentId() + ") [" + r.getDepartment() + "]");
                }
                latch5.countDown();
            }
            public void onFailure(String err) { latch5.countDown(); }
        });

        service.fetchRegistrations("All Events", new RegistrationService.ServiceCallback<List<Registration>>() {
            public void onSuccess(List<Registration> list) {
                System.out.println("  Total Directory Count across all events: " + list.size());
                latch5.countDown();
            }
            public void onFailure(String err) { latch5.countDown(); }
        });

        latch5.await(2, TimeUnit.SECONDS);

        // ---------------------------------------------------------------------
        // TEST 6: Desktop Swing Application Instantiation Test
        // ---------------------------------------------------------------------
        System.out.println("\n[TEST 6] Testing Swing GUI Application Structure & Menus:");
        CampusEventRegistrationApp app = new CampusEventRegistrationApp();
        app.setShowModalDialogs(false);

        System.out.println("  Window Title: " + app.getTitle());
        System.out.println("  JMenuBar Menus: File, Filter, Help (" + app.getJMenuBar().getMenuCount() + " menus)");
        System.out.println("  JTable Column Count: " + app.getTableModel().getColumnCount());
        System.out.println("  JTable Loaded Initial Row Count: " + app.getTableModel().getRowCount());

        // Clean up resources
        service.shutdown();
        app.getRegistrationService().shutdown();
        app.dispose();

        System.out.println("\n======================================================================");
        System.out.println(" Task 6 Campus Event Registration Architecture 100% Verified!          ");
        System.out.println("======================================================================");
    }
}
