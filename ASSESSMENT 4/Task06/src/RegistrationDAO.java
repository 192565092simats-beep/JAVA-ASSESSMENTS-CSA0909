import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data Access Object (DAO) Interface and Implementation for Event Registrations.
 * Follows JDBC contracts and enforces SQL schema constraints (including composite uniqueness).
 */
public interface RegistrationDAO {
    void save(Registration registration) throws SQLException;
    List<Registration> findAll() throws SQLException;
    List<Registration> findByEvent(String eventName) throws SQLException;
    boolean existsByStudentAndEvent(String studentId, String eventName);
    int countByEvent(String eventName);
    boolean delete(int id) throws SQLException;

    /**
     * Standard In-Memory JDBC-compliant DAO Implementation.
     */
    class InMemoryDAO implements RegistrationDAO {
        private final List<Registration> storage = new ArrayList<>();
        private final Set<String> compositeUniqueIndex = new HashSet<>();
        private final AtomicInteger idSequence = new AtomicInteger(100);

        public InMemoryDAO() {
            // Seed initial records matching schema.sql
            try {
                save(new Registration("192565092", "Thejas Balaji", "Computer Science", "192565092@simats.edu.in", "Hackathon 2026"));
                save(new Registration("192565093", "Karthik Raja", "Information Tech", "karthik@simats.edu.in", "Code Marathon"));
                save(new Registration("192565094", "Ananya Sharma", "Electronics Eng", "ananya@simats.edu.in", "RoboWars Challenge"));
            } catch (SQLException e) {
                // Ignore seed error
            }
        }

        @Override
        public synchronized void save(Registration r) throws SQLException {
            String compositeKey = r.getStudentId().toUpperCase() + "#" + r.getEventName().toUpperCase();
            if (compositeUniqueIndex.contains(compositeKey)) {
                throw new SQLException("UNIQUE constraint failed: event_registrations.uk_student_event (Student "
                        + r.getStudentId() + " is already registered for " + r.getEventName() + ")");
            }

            int newId = idSequence.incrementAndGet();
            r.setId(newId);
            storage.add(r);
            compositeUniqueIndex.add(compositeKey);
        }

        @Override
        public synchronized List<Registration> findAll() {
            return new ArrayList<>(storage);
        }

        @Override
        public synchronized List<Registration> findByEvent(String eventName) {
            if (eventName == null || eventName.equalsIgnoreCase("All Events")) {
                return findAll();
            }
            List<Registration> filtered = new ArrayList<>();
            for (Registration r : storage) {
                if (r.getEventName().equalsIgnoreCase(eventName)) {
                    filtered.add(r);
                }
            }
            return filtered;
        }

        @Override
        public synchronized boolean existsByStudentAndEvent(String studentId, String eventName) {
            String compositeKey = studentId.toUpperCase() + "#" + eventName.toUpperCase();
            return compositeUniqueIndex.contains(compositeKey);
        }

        @Override
        public synchronized int countByEvent(String eventName) {
            int count = 0;
            for (Registration r : storage) {
                if (r.getEventName().equalsIgnoreCase(eventName)) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public synchronized boolean delete(int id) {
            Iterator<Registration> it = storage.iterator();
            while (it.hasNext()) {
                Registration r = it.next();
                if (r.getId() == id) {
                    compositeUniqueIndex.remove(r.getStudentId().toUpperCase() + "#" + r.getEventName().toUpperCase());
                    it.remove();
                    return true;
                }
            }
            return false;
        }
    }
}
