# Task 1: GUI Design Review - Student Fee Payment System

**Course**: Programming in Java (CO4 - Event-Driven GUI Design)  
**Student Name**: Thejas Balaji  
**Register Number**: 192565092  
**Marks**: 6 Marks | **Bloom's Level**: K4 (Analyse)  

---

## 1.1 Diagnosis of Deficiencies in Given Code

Below is the forensic analysis of the deficiencies identified in the original `FeeWindow` implementation:

| # | Construct / Line | Defect Description | Runtime Symptom | Severity | Recommended Fix |
|---|------------------|--------------------|-----------------|----------|-----------------|
| **1** | `Thread.sleep(3000);` inside `actionPerformed()` | **Blocking the Event Dispatch Thread (EDT)**. Direct synchronous delays and database operations execute on the main AWT/Swing dispatch thread. | The entire application window freezes for 3 seconds. The OS marks the window as "Not Responding", clicks are dropped, and no repaint events occur. | **Critical** | Offload long-running operations, network calls, and database transactions to background threads using `SwingWorker` or `ExecutorService`, updating the UI via `EventQueue.invokeLater()`. |
| **2** | `cmd == "Pay"` | **Reference Identity vs Value Equality**. String comparison using `==` checks reference equality rather than lexical content equality. | Depending on JVM string interning and compiler optimizations, string action commands may evaluate to `false`, causing the "Pay" action to silently fall through to the `else` branch. | **Critical** | Use `"Pay".equals(cmd)` or `cmd.equals("Pay")` to evaluate string value equality safely. |
| **3** | `Double.parseDouble(t2.getText())` without `try-catch` | **Unchecked NumberFormatException**. If the user enters an invalid number, empty text, or non-numeric characters, parsing fails immediately. | The JVM throws unhandled `java.lang.NumberFormatException` on the EDT. The event dispatch chain is broken, and execution terminates abruptly without any notification to the user. | **Major** | Wrap input parsing in a `try-catch (NumberFormatException e)` block and display a user-friendly error message or status notification. |
| **4** | `t2.setText("")` in `else` block | **Faulty "Receipt" Button Handling**. The `if-else` chain checks `cmd == "Pay"` and falls into `else` for both "Clear" and "Receipt". | Clicking the "Receipt" button clears the fee amount field instead of displaying or printing a transaction receipt. | **Major** | Create distinct, dedicated event listeners for each button or use an explicit dispatch condition (`else if ("Receipt".equals(cmd))`). |
| **5** | Direct SQL concatenation: `INSERT INTO fees VALUES ('" + t1.getText() + "', ...)` | **SQL Injection Vulnerability & Tight Coupling**. Direct embedding of user input in raw SQL statements mixed directly inside GUI presentation classes. | Malicious input (e.g. `'; DROP TABLE fees; --`) can corrupt or erase the database. Furthermore, database logic cannot be unit tested without instantiating GUI components. | **Critical** | Decouple database logic into a Data Access Object (DAO) / Service Layer and use parameterized queries via `PreparedStatement`. |
| **6** | `System.out.println("Paid")` | **Absence of User Feedback in GUI**. Status notifications are printed only to standard output console instead of presenting feedback on the graphical window. | End users running the desktop app without a terminal window receive zero visual confirmation or error messages. | **Minor** | Update a GUI `Label` / `JLabel` or show an informative `JOptionPane.showMessageDialog`. |
| **7** | Missing `WindowListener` / `setDefaultCloseOperation` | **Unclosable Window & Resource Leak**. Window close button (`X`) is ignored because no `WindowListener` is registered. Database `Connection` and `Statement` are never closed. | Clicking the close button (`X`) has no effect. The process remains running in the background indefinitely, leaking JVM memory and system threads. | **Major** | Register a `WindowAdapter` overriding `windowClosing()` to clean up background executors, close open connections, and call `dispose()`. |

---

## 1.2 Repaired Design and Implementation

The repaired design decouples the user interface from the business logic:
1. **`FeeService.java`**: A headless, pure Java business service responsible for input validation, background worker thread dispatching via `ExecutorService`, simulated database updates, and callback notifications (`FeeCallback`).
2. **`RepairedFeeWindow.java`**: A clean AWT/Swing GUI that manages visual components, registers dedicated button listeners, provides instantaneous visual feedback via `statusLabel`, and handles window closing gracefully.

### Key Architectural Flow
```
[User Action: Click Pay]
          │
          ▼
[RepairedFeeWindow (EDT)]
          │ calls non-blocking
          ▼
   [FeeService] ──► Validates inputs synchronously
          │
          ├── (Valid) ──► Submits task to Background ExecutorService
          │                     │ (Simulates DB insert & latency)
          │                     ▼
          │               Invokes callback.onSuccess()
          │                     │
          │                     ▼
          └─────────────► Dispatches EventQueue.invokeLater()
                                │
                                ▼
                         Updates statusLabel on EDT
```

---

## 1.3 Design Defense (Testability & Maintainability)

> **Defense (74 words)**:  
> The original design tightly coupled GUI widgets with database queries and blocking delays, making automated testing impossible without launching display windows and active databases. The repaired architecture completely decouples domain validation and transaction processing into a standalone `FeeService`. This enables headless unit testing with mock frameworks (JUnit/Mockito) in milliseconds, achieves $100\%$ branch test coverage without the Event Dispatch Thread, and permits modifying UI styling or database drivers independently without breaking existing business logic.

---

## 1.4 Alternative Concept: Single Listener vs Dedicated Listener Objects

### Comparison Matrix

| Metric | Single Listener with `if-else` Dispatch | Dedicated Listener Objects / Lambdas |
|--------|-----------------------------------------|--------------------------------------|
| **Dispatch Time Complexity** | **$O(k)$ Linear Time**: Checks conditions sequentially using `cmd.equals()` across $k$ buttons. For large forms ($k > 20$), evaluation latency accumulates. | **$O(1)$ Constant Time**: Direct invocation of the specific method reference/closure registered with that unique component. |
| **Memory Overhead** | **$O(1)$ Listener Object Overhead**: Exactly 1 shared listener instance is instantiated across all $k$ controls. | **$O(k)$ Object Allocations**: Allocates $k$ distinct anonymous inner class or lambda instances in heap memory. |
| **Coupling & Cohesion** | **High Coupling / Low Cohesion**: Single class or handler must know all button labels, field references, and business actions. | **Low Coupling / High Cohesion**: Each handler is localized strictly to the component it controls. |
| **Maintainability & Extensibility** | **Fragile**: Adding, renaming, or translating button labels breaks `if-else` matching unless constant keys are meticulously maintained. | **Robust**: Renaming a button's visual label has zero impact on its registered listener lambda. |

### Conclusion
For modern desktop applications, **dedicated listeners (via lambdas or method references)** are overwhelmingly preferred. While single listeners save minimal memory ($O(1)$ vs $O(k)$ trivial bytes), dedicated listeners provide $O(1)$ dispatch time, compile-time type safety, eliminated `String` parsing errors, and localized maintainability.

---

## 1.5 Compilation and Execution

### Compilation Command
```bash
javac -d bin src/*.java
```

### Execution Command
```bash
java -cp bin FeeWindowDiagnosisDemo
```

### Execution Output
```text
==========================================================
 TASK 1: GUI Design Review - Diagnose and Repair          
==========================================================

[TEST 1] Validation with Empty Registration Plate:
  Handled Validation Error: Registration number cannot be empty.

[TEST 2] Validation with Non-numeric Amount ("FiveThousand"):
  Handled Validation Error: Invalid amount format. Please enter a valid number.

[TEST 3] Validation with Negative Amount (-1500):
  Handled Validation Error: Fee amount must be greater than zero.

[TEST 4] Valid Asynchronous Payment Execution (Background Thread):
  Payment Confirmed! Receipt: REC-86374 | Amount: Rs 12500.00

[VERIFICATION] Repaired architecture verified successfully.
==========================================================
```
