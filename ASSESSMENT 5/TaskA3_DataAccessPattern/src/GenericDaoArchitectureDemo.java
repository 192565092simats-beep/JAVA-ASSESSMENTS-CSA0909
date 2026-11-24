import java.util.*;

/**
 * Task A3: Generic Data Access Object (DAO) Pattern Architecture Demo
 * Demonstrates:
 * 1. Generic interface Dao<T, K>
 * 2. Object-to-Table Relational Mapping
 * 3. Type-safe CRUD operations
 * 4. Decoupling of domain business logic from SQL storage mechanisms
 */
public class GenericDaoArchitectureDemo {

    // =========================================================================
    // 1. Generic DAO Interface
    // =========================================================================
    public interface Dao<T, K> {
        void insert(T entity);
        Optional<T> findById(K id);
        List<T> findAll();
        void update(T entity);
        boolean delete(K id);
    }

    // =========================================================================
    // 2. Student Domain Entity
    // =========================================================================
    public static class Student {
        private String registerNumber; // Primary Key -> student.reg_no
        private String fullName;       // -> student.full_name
        private String department;     // -> student.dept
        private double cgpa;           // -> student.cgpa

        public Student(String registerNumber, String fullName, String department, double cgpa) {
            this.registerNumber = registerNumber;
            this.fullName = fullName;
            this.department = department;
            this.cgpa = cgpa;
        }

        public String getRegisterNumber() { return registerNumber; }
        public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public double getCgpa() { return cgpa; }
        public void setCgpa(double cgpa) { this.cgpa = cgpa; }

        @Override
        public String toString() {
            return String.format("Student[Reg:%s | %s | Dept:%s | CGPA:%.2f]",
                    registerNumber, fullName, department, cgpa);
        }
    }

    // =========================================================================
    // 3. Concrete StudentDao Implementation
    // =========================================================================
    public static class StudentDaoImpl implements Dao<Student, String> {
        // Simulates underlying relational table: CREATE TABLE students (...)
        private final Map<String, Student> tableStorage = new LinkedHashMap<>();

        // SQL Template Constants (Centralized in exactly one place!)
        public static final String SQL_INSERT = "INSERT INTO students(reg_no, full_name, dept, cgpa) VALUES (?, ?, ?, ?)";
        public static final String SQL_SELECT_BY_ID = "SELECT reg_no, full_name, dept, cgpa FROM students WHERE reg_no = ?";
        public static final String SQL_SELECT_ALL = "SELECT reg_no, full_name, dept, cgpa FROM students";
        public static final String SQL_UPDATE = "UPDATE students SET full_name = ?, dept = ?, cgpa = ? WHERE reg_no = ?";
        public static final String SQL_DELETE = "DELETE FROM students WHERE reg_no = ?";

        @Override
        public void insert(Student student) {
            if (tableStorage.containsKey(student.getRegisterNumber())) {
                throw new IllegalStateException("Duplicate Primary Key: " + student.getRegisterNumber());
            }
            // Emulates PreparedStatement execution and row insertion
            tableStorage.put(student.getRegisterNumber(),
                    new Student(student.getRegisterNumber(), student.getFullName(),
                            student.getDepartment(), student.getCgpa()));
        }

        @Override
        public Optional<Student> findById(String id) {
            // Emulates SELECT ... WHERE reg_no = ? and ResultSet row mapping
            Student s = tableStorage.get(id);
            if (s == null) return Optional.empty();
            return Optional.of(new Student(s.getRegisterNumber(), s.getFullName(), s.getDepartment(), s.getCgpa()));
        }

        @Override
        public List<Student> findAll() {
            List<Student> list = new ArrayList<>();
            for (Student s : tableStorage.values()) {
                list.add(new Student(s.getRegisterNumber(), s.getFullName(), s.getDepartment(), s.getCgpa()));
            }
            return list;
        }

        @Override
        public void update(Student student) {
            if (!tableStorage.containsKey(student.getRegisterNumber())) {
                throw new NoSuchElementException("Student record not found for update: " + student.getRegisterNumber());
            }
            tableStorage.put(student.getRegisterNumber(),
                    new Student(student.getRegisterNumber(), student.getFullName(),
                            student.getDepartment(), student.getCgpa()));
        }

        @Override
        public boolean delete(String id) {
            return tableStorage.remove(id) != null;
        }
    }

    // =========================================================================
    // Test Harness
    // =========================================================================
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println(" TASK A3: Discover the Data-Access Design Pattern (DAO Pattern)   ");
        System.out.println("==================================================================");

        Dao<Student, String> studentDao = new StudentDaoImpl();

        // 1. Insert Operations
        System.out.println("\n[STEP 1] Executing DAO Insert Operations:");
        studentDao.insert(new Student("192565092", "Thejas Balaji", "Computer Science", 9.45));
        studentDao.insert(new Student("192565093", "Karthik Raja", "Information Tech", 8.85));
        studentDao.insert(new Student("192565094", "Ananya Sharma", "Electronics Eng", 9.12));
        System.out.println("  Inserted 3 students into simulated student table.");

        // 2. Find All
        System.out.println("\n[STEP 2] Executing DAO findAll():");
        List<Student> all = studentDao.findAll();
        for (Student s : all) {
            System.out.println("  " + s);
        }

        // 3. Find By ID
        System.out.println("\n[STEP 3] Executing DAO findById('192565092'):");
        Optional<Student> opt = studentDao.findById("192565092");
        System.out.println("  Lookup Result: " + (opt.isPresent() ? opt.get() : "NOT FOUND"));

        // 4. Update
        System.out.println("\n[STEP 4] Executing DAO update() (CGPA updated to 9.75):");
        if (opt.isPresent()) {
            Student s = opt.get();
            s.setCgpa(9.75);
            studentDao.update(s);
            System.out.println("  Updated Record: " + studentDao.findById("192565092").get());
        }

        // 5. Delete
        System.out.println("\n[STEP 5] Executing DAO delete('192565093'):");
        boolean deleted = studentDao.delete("192565093");
        System.out.println("  Deletion Status: " + (deleted ? "SUCCESS" : "FAILED"));
        System.out.println("  Remaining Record Count: " + studentDao.findAll().size());

        System.out.println("\n==================================================================");
        System.out.println(" Task A3 Generic DAO Pattern Verified Successfully!              ");
        System.out.println("==================================================================");
    }
}
