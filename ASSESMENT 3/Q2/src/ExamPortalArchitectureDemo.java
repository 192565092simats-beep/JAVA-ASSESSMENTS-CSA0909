import java.io.IOException;

/**
 * 3-Layer Architecture Demonstration: Controller -> Service -> Storage
 */
public class ExamPortalArchitectureDemo {

    // Domain Exception Hierarchy
    static class BaseExamException extends Exception {
        BaseExamException(String message) { super(message); }
        BaseExamException(String message, Throwable cause) { super(message, cause); }
    }

    static class ExamWindowClosedException extends BaseExamException {
        ExamWindowClosedException(String message) { super(message); }
    }

    static class StudentNotRegisteredException extends BaseExamException {
        StudentNotRegisteredException(String message) { super(message); }
    }

    static class StorageFailureException extends BaseExamException {
        StorageFailureException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    // Layer 3: Storage Layer
    static class StorageLayer {
        void persistFile(String studentId, String fileName, long sizeKb) throws StorageFailureException {
            try {
                if (sizeKb > 500) {
                    throw new IOException("Storage disk block allocation failed: Quota 500KB exceeded.");
                }
                System.out.printf("    [StorageLayer] File '%s' (Size: %d KB) written to disk.%n", fileName, sizeKb);
            } catch (IOException e) {
                // Exception Chaining: Preserves low-level root cause
                throw new StorageFailureException("Failed to persist answer script: " + fileName, e);
            }
        }
    }

    // Layer 2: Service Layer
    static class ExamService {
        private final StorageLayer storage = new StorageLayer();

        void processSubmission(String studentId, String fileName, long sizeKb, int submitHour) throws BaseExamException {
            System.out.printf("  [ServiceLayer] Validating submission for student %s...%n", studentId);

            // Business Rule 1: Student registration check
            if ("REG_INVALID".equalsIgnoreCase(studentId)) {
                throw new StudentNotRegisteredException("Student ID " + studentId + " is not enrolled in course CSA0909.");
            }

            // Business Rule 2: Exam window check (10:00 to 13:00)
            if (submitHour >= 13) {
                throw new ExamWindowClosedException("Submission portal closed at 13:00. Attempted at " + submitHour + ":00.");
            }

            // Delegate to storage layer
            storage.persistFile(studentId, fileName, sizeKb);
        }
    }

    // Layer 1: Controller Layer (Presentation / UI boundary)
    static class ExamController {
        private final ExamService service = new ExamService();

        void handleStudentSubmit(String studentId, String fileName, long sizeKb, int submitHour) {
            System.out.printf("%n[Controller] Incoming submission: Student=%s, File=%s, Size=%dKB, Time=%d:00%n",
                    studentId, fileName, sizeKb, submitHour);
            try {
                service.processSubmission(studentId, fileName, sizeKb, submitHour);
                System.out.println("  [UI Response 200 OK] Submission successful! Confirmation receipt issued.");
            } catch (ExamWindowClosedException e) {
                System.out.println("  [UI Response 403 Forbidden] " + e.getMessage());
            } catch (StudentNotRegisteredException e) {
                System.out.println("  [UI Response 401 Unauthorized] " + e.getMessage());
            } catch (StorageFailureException e) {
                System.out.println("  [UI Response 500 Internal Error] " + e.getMessage());
                System.out.println("    [Server Diagnostic Log] Root Cause: " + e.getCause().getMessage());
            } catch (BaseExamException e) {
                System.out.println("  [UI Response 400 Bad Request] " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println(" Q2: 3-Layer Exam Portal Exception Propagation & Chaining ");
        System.out.println("==========================================================");

        ExamController controller = new ExamController();

        // Scenario 1: Success
        controller.handleStudentSubmit("192565092", "thejas_answer.pdf", 250, 11);

        // Scenario 2: Unregistered Student
        controller.handleStudentSubmit("REG_INVALID", "draft.pdf", 200, 11);

        // Scenario 3: Exam Window Closed
        controller.handleStudentSubmit("192565092", "late_submission.pdf", 300, 14);

        // Scenario 4: Storage Quota Exceeded (Exception Chaining)
        controller.handleStudentSubmit("192565092", "huge_project.pdf", 1200, 11);

        System.out.println("\n==========================================================");
    }
}
