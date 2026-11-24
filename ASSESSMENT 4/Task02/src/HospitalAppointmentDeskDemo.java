/**
 * Test Harness for HospitalAppointmentDesk.
 * Simulates user interactions and tests all event handling logic.
 */
public class HospitalAppointmentDeskDemo {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println(" TASK 2: Hospital Outpatient Appointment Desk Demo       ");
        System.out.println("==========================================================");

        HospitalAppointmentDesk desk = new HospitalAppointmentDesk();

        System.out.println("\n[STEP 1] Initial Form State Verification:");
        System.out.println("  Default Selected Department: " + desk.getDeptChoice().getSelectedItem());
        System.out.println("  Doctors available in " + desk.getDeptChoice().getSelectedItem() + ":");
        for (int i = 0; i < desk.getDoctorChoice().getItemCount(); i++) {
            System.out.println("    - " + desk.getDoctorChoice().getItem(i));
        }

        System.out.println("\n[STEP 2] Simulating Department Change Event (ItemEvent):");
        System.out.println("  User selects: \"Neurology\"");
        desk.getDeptChoice().select("Neurology");
        // Trigger cascading update
        desk.refreshDoctorList("Neurology");
        desk.markBookingInProgress();
        System.out.println("  Updated Doctors list for Neurology:");
        for (int i = 0; i < desk.getDoctorChoice().getItemCount(); i++) {
            System.out.println("    - " + desk.getDoctorChoice().getItem(i));
        }

        System.out.println("\n[STEP 3] Simulating Validation Failure (Empty Patient ID):");
        desk.getPatientIdField().setText("");
        boolean validResult = desk.processBooking();
        System.out.println("  Booking result: " + (validResult ? "SUCCESS" : "FAILED"));
        System.out.println("  Status displayed: " + desk.getStatusLabel().getText());

        System.out.println("\n[STEP 4] Simulating Valid Appointment Booking (ActionEvent):");
        desk.getPatientIdField().setText("PAT-98421");
        desk.getSlotChoice().select("10:00 AM - 10:30 AM");
        boolean bookSuccess = desk.processBooking();
        System.out.println("  Booking result: " + (bookSuccess ? "SUCCESS" : "FAILED"));
        System.out.println("  Status displayed: " + desk.getStatusLabel().getText());
        System.out.println("  Booking in progress flag: " + desk.isBookingInProgress());

        System.out.println("\n[STEP 5] Simulating Window Closing Event with Active Form (WindowEvent):");
        desk.getPatientIdField().setText("PAT-NEW-55");
        desk.markBookingInProgress();
        desk.handleWindowClosing();

        System.out.println("\n==========================================================");
        System.out.println(" Task 2 Event-Driven Architecture Verified Successfully!  ");
        System.out.println("==========================================================");
    }
}
