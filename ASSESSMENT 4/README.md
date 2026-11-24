# Java Assessment 4: CO4 – Event-Driven GUI Design

**Course**: Programming in Java (Course Outcome 4: Event-Driven GUI Design)  
**Student Name**: Thejas Balaji  
**Register Number**: 192565092  
**Institution**: SIMATS Engineering  
**JDK Version**: OpenJDK 25.0.1 LTS (Compatible with Java 17 / 21 / 25)  
**Total Marks**: 50 Marks | **Bloom's Taxonomy**: K4 (Analyse) & K5 (Evaluate)  
**Target Repository**: `https://github.com/192565092simats-beep/JAVA-ASSESSMENTS-CSA0909.git`  
**Subfolder**: `ASSESSMENT 4/`  

---

## 📋 Assessment Overview & Task Summary Matrix

This repository directory contains complete, production-grade solutions for all six tasks of **Assessment 4 (CO4: Event-Driven GUI Design)**. Each task is housed in its own dedicated subfolder featuring fully compiled Java source code, test drivers, captured execution logs (`output.txt`), and comprehensive documentation (`README.md`) answering all theoretical questions, architectural diagrams, SIMATS pseudocode, and alternative concept analyses.

| Task Folder | Marks | Bloom's Level | Focus Area & Title | Key Deliverables & Concepts |
|-------------|-------|---------------|-------------------|----------------------------|
| [**Task01**](Task01/) | 6 Marks | K4 (Analyse) | **GUI Design Review** (Fee Payment Window) | • Diagnosed 7 design & event-handling flaws (EDT freeze, `==` string bug, unhandled `NumberFormatException`, SQL injection, resource leaks).<br>• Decoupled into `FeeService.java` & `RepairedFeeWindow.java`.<br>• 74-word testability defense & $O(k)$ vs $O(1)$ listener trade-offs. |
| [**Task02**](Task02/) | 8 Marks | K4 (Analyse) | **From Requirement to Event-Driven Design** (Hospital Outpatient Desk) | • Event model table (Source, Event Object, Listener, Action).<br>• Component containment tree diagram.<br>• SIMATS pseudocode for cascading doctor dropdown & booking.<br>• Event sequence trace vs Applet lifecycle.<br>• Dynamic DB queries vs pre-loaded in-memory cache analysis. |
| [**Task03**](Task03/) | 9 Marks | K5 (Evaluate) | **From Sketch to Layout** (Campus Library Search Screen) | • Selected & justified nested layouts (`BorderLayout` + `FlowLayout`).<br>• Elastic Center expansion & fixed North/South headers.<br>• Responsive resizing evaluation across 3 viewport resolutions.<br>• Nested layouts vs `GridBagLayout` vs absolute `null` layout. |
| [**Task04**](Task04/) | 7 Marks | K4 (Analyse) | **Event-Handling Forensics** (Drawing Pad) | • Forensics on lowercase `mouseclicked` typo in `MouseAdapter`.<br>• Transient `getGraphics()` erasure root cause on minimize/restore.<br>• Completed `DrawPad.java` with persistent `List<Point>` & `paint(Graphics g)`.<br>• Role of `@Override` annotation.<br>• Memory/CPU analysis: Points list vs off-screen `BufferedImage`. |
| [**Task05**](Task05/) | 7 Marks | K4 (Analyse) | **Same Screen, Different Event-Handling Styles** (Currency Converter) | • Implemented 4 styles: Frame listener, Named inner class, Anonymous inner class, Lambda expression.<br>• Code changes for Enter key trigger & `JOptionPane` dialog.<br>• Detailed comparison matrix & enterprise production recommendation.<br>• Completed `Converter.java` & Swing `Action` abstraction. |
| [**Task06**](Task06/) | 13 Marks | K5 (Evaluate) | **Design Challenge: Campus Event Registration Desktop App** | • Full 5-stage design engineering lifecycle.<br>• Composite uniqueness: `UNIQUE(student_id, event_name)` in `schema.sql`.<br>• Threading architecture: EDT completely non-blocking via `ExecutorService`.<br>• Full Swing app (`JMenuBar`, `JTable`, `DefaultTableModel`, Form, Filter).<br>• Bulk `JTable` load vs Paginated `SwingWorker` for 100,000 records. |

---

## 🗂️ Directory Structure

```text
ASSESSMENT 4/
├── README.md                                  <-- Master Assessment Summary (This File)
├── Task01/                                    <-- Task 1: GUI Design Review (Fee Payment Window)
│   ├── README.md                              <-- Forensic analysis, design defense, complexity
│   ├── output.txt                             <-- Captured execution log
│   └── src/
│       ├── FeeService.java                    <-- Decoupled business logic & async executor
│       ├── RepairedFeeWindow.java             <-- Repaired GUI with dedicated listeners
│       └── FeeWindowDiagnosisDemo.java        <-- Automated test harness
├── Task02/                                    <-- Task 2: Hospital Outpatient Appointment Desk
│   ├── README.md                              <-- Event model, hierarchy, SIMATS pseudocode, lifecycle
│   ├── output.txt                             <-- Captured execution log
│   └── src/
│       ├── HospitalAppointmentDesk.java       <-- Cascading dropdowns & windowClosing confirmation
│       └── HospitalAppointmentDeskDemo.java   <-- Test driver for events and validation
├── Task03/                                    <-- Task 3: Campus Library Search Screen
│   ├── README.md                              <-- Layout selection, containment tree, resizing analysis
│   ├── output.txt                             <-- Captured execution log across 3 window sizes
│   └── src/
│       ├── LibrarySearchScreen.java           <-- Responsive nested BorderLayout + FlowLayout
│       └── LibrarySearchScreenDemo.java       <-- Resizing test driver
├── Task04/                                    <-- Task 4: Event-Handling Forensics (Drawing Pad)
│   ├── README.md                              <-- Forensic diagnoses, @Override role, BufferedImage analysis
│   ├── output.txt                             <-- Captured execution log
│   └── src/
│       ├── DrawPad.java                       <-- Completed code with TODO 1 & TODO 2 resolved
│       └── DrawPadForensicsDemo.java          <-- Test driver verifying persistent dot rendering
├── Task05/                                    <-- Task 5: Currency Converter Event-Handling Styles
│   ├── README.md                              <-- 4 styles analysis, comparison matrix, Swing Action
│   ├── output.txt                             <-- Captured execution log
│   └── src/
│       ├── Converter.java                     <-- Completed code with TODO 1 & TODO 2 (Lambda style)
│       └── ConverterStylesDemo.java           <-- Verification suite for all 4 styles
└── Task06/                                    <-- Task 6: Design Challenge (Event Registration Desktop App)
    ├── README.md                              <-- 5-stage design report, class diagram, pseudocode, analysis
    ├── schema.sql                             <-- SQL DDL/DML with composite unique constraints & seeds
    ├── output.txt                             <-- Captured execution log
    └── src/
        ├── Registration.java                  <-- Entity domain model
        ├── RegistrationDAO.java               <-- JDBC DAO interface & thread-safe implementation
        ├── RegistrationService.java           <-- Service layer with validation & async executor
        ├── CampusEventRegistrationApp.java    <-- Complete Swing GUI (JMenuBar, JTable, Form, Dialogs)
        └── CampusEventRegistrationDemo.java   <-- Automated test harness verifying all 5 stages
```

---

## ⚙️ Quick Build & Execution Instructions

All tasks are implemented using pure Java SE without external third-party dependencies, guaranteeing 100% build compatibility on any standard OpenJDK installation.

### 1. Compile All Tasks
```bash
# From the repository root directory
javac -d "ASSESSMENT 4/Task01/bin" "ASSESSMENT 4/Task01/src/"*.java
javac -d "ASSESSMENT 4/Task02/bin" "ASSESSMENT 4/Task02/src/"*.java
javac -d "ASSESSMENT 4/Task03/bin" "ASSESSMENT 4/Task03/src/"*.java
javac -d "ASSESSMENT 4/Task04/bin" "ASSESSMENT 4/Task04/src/"*.java
javac -d "ASSESSMENT 4/Task05/bin" "ASSESSMENT 4/Task05/src/"*.java
javac -d "ASSESSMENT 4/Task06/bin" "ASSESSMENT 4/Task06/src/"*.java
```

### 2. Run Test Drivers & Demos
```bash
# Task 1: Fee Window Diagnosis Demo
java -cp "ASSESSMENT 4/Task01/bin" FeeWindowDiagnosisDemo

# Task 2: Hospital Appointment Desk Demo
java -cp "ASSESSMENT 4/Task02/bin" HospitalAppointmentDeskDemo

# Task 3: Campus Library Search Screen Resizing Demo
java -cp "ASSESSMENT 4/Task03/bin" LibrarySearchScreenDemo

# Task 4: DrawPad Forensics Demo
java -cp "ASSESSMENT 4/Task04/bin" DrawPadForensicsDemo

# Task 5: Currency Converter 4-Styles Demo
java -cp "ASSESSMENT 4/Task05/bin" ConverterStylesDemo

# Task 6: Campus Event Registration Verification Suite
java -cp "ASSESSMENT 4/Task06/bin" CampusEventRegistrationDemo
```

---

## 🔒 Progressive Git Commit History

The implementation strictly followed progressive version control discipline, creating dedicated, verifiable commits for each task:

1. `db2482b` - `feat(Task01): diagnose and repair poor GUI event handling design`
2. `4adb0ac` - `feat(Task02): implement event-driven hospital appointment desk and event modeling`
3. `4c32154` - `feat(Task03): implement campus library search layout with responsive resizing`
4. `bbb69b5` - `feat(Task04): complete DrawPad code and forensic analysis of event handling defects`
5. `1be9446` - `feat(Task05): complete Converter code and compare 4 event handling styles`
6. `c4df867` - `feat(Task06): implement layered Swing-JDBC event registration desktop app and schema`
7. `docs: add comprehensive master README for ASSESSMENT 4`
