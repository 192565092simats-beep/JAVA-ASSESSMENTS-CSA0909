import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * Service Layer for Campus Event Registration System.
 * Enforces business rules, validation constraints, and offloads database queries
 * to a dedicated background executor thread to keep the Swing EDT responsive.
 */
public class RegistrationService {

    public interface ServiceCallback<T> {
        void onSuccess(T result);
        void onFailure(String errorMessage);
    }

    private static final int MAX_EVENT_CAPACITY = 50;
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^[A-Za-z0-9]{6,15}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    private final RegistrationDAO dao;
    private final ExecutorService backgroundPool = Executors.newFixedThreadPool(2);

    public RegistrationService(RegistrationDAO dao) {
        this.dao = dao;
    }

    public RegistrationService() {
        this(new RegistrationDAO.InMemoryDAO());
    }

    /**
     * Registers a student for an event with comprehensive validation and duplicate prevention.
     */
    public void registerStudent(String studentId, String name, String dept, String email, String eventName,
                                ServiceCallback<Registration> callback) {

        // 1. Synchronous Input Validation
        if (studentId == null || !STUDENT_ID_PATTERN.matcher(studentId.trim()).matches()) {
            callback.onFailure("Invalid Student ID! Must be 6-15 alphanumeric characters (e.g., 192565092).");
            return;
        }

        if (name == null || name.trim().length() < 2) {
            callback.onFailure("Student Name must be at least 2 characters long.");
            return;
        }

        if (email == null || !EMAIL_PATTERN.matcher(email.trim().toLowerCase()).matches()) {
            callback.onFailure("Invalid Email address format (e.g., student@simats.edu.in).");
            return;
        }

        if (eventName == null || eventName.trim().isEmpty() || eventName.equalsIgnoreCase("All Events")) {
            callback.onFailure("Please select a specific campus event.");
            return;
        }

        final String cleanId = studentId.trim();
        final String cleanName = name.trim();
        final String cleanDept = dept != null ? dept.trim() : "General";
        final String cleanEmail = email.trim().toLowerCase();
        final String cleanEvent = eventName.trim();

        // 2. Offload Database checks & insertion to background thread (EDT-safe)
        backgroundPool.submit(() -> {
            try {
                // Simulate network/database roundtrip latency (e.g. 50ms)
                Thread.sleep(50);

                // Check capacity constraint
                int currentCount = dao.countByEvent(cleanEvent);
                if (currentCount >= MAX_EVENT_CAPACITY) {
                    callback.onFailure("Event Capacity Full! Maximum limit of " + MAX_EVENT_CAPACITY + " reached for " + cleanEvent);
                    return;
                }

                // Check duplicate registration constraint
                if (dao.existsByStudentAndEvent(cleanId, cleanEvent)) {
                    callback.onFailure("Duplicate Registration Rejected! Student (" + cleanId + ") is already registered for \"" + cleanEvent + "\".");
                    return;
                }

                // Create and persist record
                Registration reg = new Registration(cleanId, cleanName, cleanDept, cleanEmail, cleanEvent);
                dao.save(reg);
                callback.onSuccess(reg);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                callback.onFailure("Registration operation interrupted.");
            } catch (SQLException e) {
                callback.onFailure("Database constraint error: " + e.getMessage());
            } catch (Exception e) {
                callback.onFailure("Unexpected system error: " + e.getMessage());
            }
        });
    }

    /**
     * Asynchronously fetches registrations filtered by event.
     */
    public void fetchRegistrations(String eventFilter, ServiceCallback<List<Registration>> callback) {
        backgroundPool.submit(() -> {
            try {
                Thread.sleep(30); // simulated query latency
                List<Registration> list = dao.findByEvent(eventFilter);
                callback.onSuccess(list);
            } catch (Exception e) {
                callback.onFailure("Failed to retrieve registrations: " + e.getMessage());
            }
        });
    }

    /**
     * Asynchronously deletes a registration by ID.
     */
    public void deleteRegistration(int id, ServiceCallback<Boolean> callback) {
        backgroundPool.submit(() -> {
            try {
                boolean deleted = dao.delete(id);
                if (deleted) {
                    callback.onSuccess(true);
                } else {
                    callback.onFailure("Record ID #" + id + " not found.");
                }
            } catch (Exception e) {
                callback.onFailure("Database error during deletion: " + e.getMessage());
            }
        });
    }

    public RegistrationDAO getDao() {
        return dao;
    }

    public void shutdown() {
        backgroundPool.shutdown();
    }
}
