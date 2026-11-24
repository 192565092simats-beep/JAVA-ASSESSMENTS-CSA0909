/**
 * Data Model for Student Score Record
 */
public class StudentScore {
    public enum Status { SUCCESS, MISSING_FILE, CORRUPT_DATA, SYSTEM_ERROR }

    private final String studentId;
    private final int marks;
    private final Status status;
    private final String errorMessage;

    public StudentScore(String studentId, int marks, Status status, String errorMessage) {
        this.studentId = studentId;
        this.marks = marks;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public static StudentScore success(String studentId, int marks) {
        return new StudentScore(studentId, marks, Status.SUCCESS, null);
    }

    public static StudentScore failure(String studentId, Status status, String errorMessage) {
        return new StudentScore(studentId, -1, status, errorMessage);
    }

    public String getStudentId() { return studentId; }
    public int getMarks() { return marks; }
    public Status getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }

    @Override
    public String toString() {
        return String.format("[%s | Status: %s | Marks: %d | Error: %s]",
                studentId, status, marks, errorMessage != null ? errorMessage : "None");
    }
}
