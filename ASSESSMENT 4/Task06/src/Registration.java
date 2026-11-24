import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Domain Entity representing a Student Registration record.
 */
public class Registration {
    private int id;
    private String studentId;
    private String studentName;
    private String department;
    private String email;
    private String eventName;
    private LocalDateTime registrationTime;
    private String status;

    public Registration(int id, String studentId, String studentName, String department,
                        String email, String eventName, LocalDateTime registrationTime, String status) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.department = department;
        this.email = email;
        this.eventName = eventName;
        this.registrationTime = registrationTime != null ? registrationTime : LocalDateTime.now();
        this.status = status != null ? status : "REGISTERED";
    }

    public Registration(String studentId, String studentName, String department, String email, String eventName) {
        this(0, studentId, studentName, department, email, eventName, LocalDateTime.now(), "REGISTERED");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getDepartment() { return department; }
    public String getEmail() { return email; }
    public String getEventName() { return eventName; }
    public LocalDateTime getRegistrationTime() { return registrationTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFormattedTimestamp() {
        return registrationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public Object[] toTableRow() {
        return new Object[] {
            id, studentId, studentName, department, email, eventName, getFormattedTimestamp(), status
        };
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) -> %s [%s]", id, studentName, studentId, eventName, status);
    }
}
