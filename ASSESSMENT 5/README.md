# Java Assessment 5: CO5 – Data Persistence Engineering (Files and Databases)

**Course**: Programming in Java (Course Outcome 5: Data Persistence Engineering)  
**Student Name**: Thejas Balaji  
**Register Number**: 192565092  
**Institution**: SIMATS Engineering  
**JDK Version**: OpenJDK 25.0.1 LTS (Compatible with Java 17 / 21 / 25)  
**Total Marks**: 50 Marks | **Bloom's Taxonomy**: K4 (Analyse) & K5 (Evaluate)  
**Target Repository**: `https://github.com/192565092simats-beep/JAVA-ASSESSMENTS-CSA0909.git`  
**Subfolder**: `ASSESSMENT 5/`  

---

## 📋 Assessment Overview & Task Summary Matrix

This repository directory contains complete, production-grade solutions for all nine tasks of **Assessment 5 (CO5: Data Persistence Engineering – Files and Databases)**. The assignment investigates the core engineering question:
> **"How should a Java engineer decide where and how data is stored, and how can the program be made correct, safe, and efficient as the data grows?"**

Following the required workflow (**Model $\rightarrow$ Choose a Store $\rightarrow$ Design $\rightarrow$ Protect $\rightarrow$ Implement $\rightarrow$ Measure $\rightarrow$ Compare $\rightarrow$ Decide**), each task is organized in its own dedicated directory with clean Java source code, test drivers, captured execution logs (`output.txt`), and comprehensive documentation (`README.md`).

| Task Folder | Marks | Bloom's Level | Focus Area & Title | Key Deliverables & Technical Highlights |
|:---|:---:|:---:|:---|:---|
| [**TaskA1**](TaskA1_FileFormatDesign/) | 7 M | K4 (Analyse) | **Design a File Format** (Bus-Pass Records) | • Delimited CSV (RFC 4180 quoting) vs Binary `DataOutputStream` record layouts.<br>• SIMATS pseudocode for Add, Find, and Route List operations with justified stream selection.<br>• Defense against damaged lines, missing files, and power failure atomic rename pattern.<br>• `RandomAccessFile` fixed-length record seek ($O(1)$) vs $50.4\%$ space wasted by padding. |
| [**TaskA2**](TaskA2_BufferingDesign/) | 5 M | K4 (Analyse) | **Don't Read What You Don't Need** (Buffering) | • Cost prediction: 52.4 million syscalls in 50 MB unbuffered read causing 78s context-switch stalls.<br>• Redesign using `BufferedInputStream` (8 KB buffer) slashing syscalls by $99.988\%$.<br>• Situations where buffering provides no benefit (small files, random single-byte seeks, mmap).<br>• Complexity analysis: Streaming $O(1)$ memory vs `Files.readAllLines()` 200 MB heap bloat. |
| [**TaskA3**](TaskA3_DataAccessPattern/) | 5 M | K4 (Analyse) | **Discover the Data-Access Design Pattern** (DAO) | • Common lifecycle boilerplate vs differing SQL/mapping parts across DB classes.<br>• Generic `Dao<T, K>` interface with complete CRUD methods & `StudentDao` implementation.<br>• Object-to-Table relational schema mapping for `Student` entity.<br>• Blast radius analysis: Column rename impacts 1 DAO file vs dozens of UI classes. |
| [**TaskA4**](TaskA4_SerializationDesign/) | 4 M | K4 (Analyse) | **What Should Be Serialized?** (Hostel Management) | • Justified `transient` fields: `Connection dbConnection` (OS socket descriptor) & `String password` (credential security).<br>• Object graph recursive traversal & handle table cycle resolution.<br>• Schema evolution analysis: Adding `phoneNumber` with vs without `serialVersionUID`.<br>• Java binary serialization vs JSON / CSV trade-off matrix. |
| [**TaskA5**](TaskA5_TransactionSafety/) | 4 M | K4 (Analyse) | **Design for Correctness & Safety** (Transactions & SQLi) | • Wallet transfer ₹500 SIMATS pseudocode with atomic `commit` & `rollback`.<br>• SQL Injection AST manipulation analysis (`admin' --`) vs `PreparedStatement` defense.<br>• Auto-commit crash analysis (money vanishes) vs single atomic transaction ($1 \times \text{fsync}$). |
| [**TaskB1**](TaskB1_InstrumentedFileStore/) | 7 M | K4 (Analyse) | **Implement & Instrument the File Store** | • **Completed Code**: `PassStore.java` with `find()` method matching expected output.<br>• Validated add, find, and list with normal case, non-existent pass, and damaged line bypass.<br>• Measured buffering across 1 MB, 10 MB, 50 MB (39x–85x speedup, 99.988% syscall reduction).<br>• Measured growth from 1,000 to 100,000 records ($O(n)$ linear scan trend).<br>• Linear file scan vs $O(n + q)$ in-memory `HashMap` index trade-off. |
| [**TaskB2**](TaskB2_AdversarialInputs/) | 5 M | K4 (Analyse) | **Engineer an Adversarial Input** (Security & Robustness) | • Executed adversarial SQL injection attacks (`admin' --`, `' OR '1'='1`) bypassing unsafe dynamic query while `PreparedStatement` strictly rejects.<br>• Ingested adversarial damaged file (wrong token count, non-numeric amount, blank lines) with complete error containment.<br>• Regex blacklist sanitization flaws vs parameterized query compiler proof. |
| [**TaskB3**](TaskB3_JdbcBatchPerformance/) | 8 M | K4 (Analyse) | **Statement vs PreparedStatement vs Batch** | • **Completed Code**: `PassLoader.java` with batching, commit, and rollback.<br>• B3.1: Small data correctness (10 records) verified via `ResultSetMetaData`.<br>• B3.2: Scaled performance across 1k, 10k, 100k records (Batching achieved 30x speedup: 1.5s vs 45s).<br>• B3.3: Mid-way failure verified: auto-commit left 500 dirty rows vs transaction clean rollback to 0 rows.<br>• Engineering trade-off: Nightly bulk import vs counter issuance. |
| [**TaskB4**](TaskB4_FinalEngineeringDecision/) | 5 M | K5 (Evaluate) | **Final Engineering Decision** (12 Campuses, 100x Scale) | • B4.1: Production migration ETL pipeline with transactional batch loading & failure audit log.<br>• B4.2: `DatabaseMetaData` inspection of product, driver, and tables.<br>• B4.3: Architectural decision: Centralized RDBMS (MySQL/PostgreSQL) with row-level locking & B-Trees vs flat files vs serialization.<br>• Distributed per-campus files vs centralized database trade-off. |

---

## 🗂️ Directory Structure

```text
ASSESSMENT 5/
├── README.md                                       <-- Master Assessment Summary (This File)
├── TaskA1_FileFormatDesign/                        <-- Task A1: Bus-Pass Record File Format Design
│   ├── README.md                                   <-- Record layouts, pseudocode, damaged data design
│   ├── output.txt                                  <-- Execution log
│   └── src/
│       └── BusPassFileStoreDemo.java               <-- Text & binary file store with atomic write
├── TaskA2_BufferingDesign/                         <-- Task A2: Buffering Cost & Syscall Reduction
│   ├── README.md                                   <-- Cost prediction, syscall math, memory comparison
│   ├── output.txt                                  <-- Empirical benchmark log (72.8x speedup)
│   └── src/
│       └── BufferingCostBenchmarkDemo.java         <-- Unbuffered vs Buffered benchmark runner
├── TaskA3_DataAccessPattern/                       <-- Task A3: Generic DAO Pattern & Blast Radius
│   ├── README.md                                   <-- Pattern breakdown, generic interface, ORM mapping
│   ├── output.txt                                  <-- CRUD verification log
│   └── src/
│       └── GenericDaoArchitectureDemo.java         <-- Generic Dao<T,K> and StudentDao implementation
├── TaskA4_SerializationDesign/                     <-- Task A4: Transient Design & Schema Evolution
│   ├── README.md                                   <-- Transient justification, serialVersionUID, JSON vs Ser
│   ├── output.txt                                  <-- Object graph verification log
│   └── src/
│       └── HostelSerializationDemo.java            <-- Hostel/Room/Resident serialization demo
├── TaskA5_TransactionSafety/                       <-- Task A5: ACID Transactions & SQL Injection
│   ├── README.md                                   <-- Wallet transfer pseudocode, injection AST analysis
│   ├── output.txt                                  <-- Rollback & injection verification log
│   └── src/
│       └── TransactionAndInjectionSecurityDemo.java<-- Transaction rollback & AST injection test
├── TaskB1_InstrumentedFileStore/                   <-- Task B1: Instrumented File Store & Growth
│   ├── README.md                                   <-- Complete the Code explanation, 1k-100k growth trend
│   ├── output.txt                                  <-- Empirical measurement log
│   └── src/
│       ├── PassStore.java                          <-- Completed Code (TODO find() resolved)
│       └── FileStoreInstrumentationBenchmark.java  <-- Validation & growth benchmark runner
├── TaskB2_AdversarialInputs/                       <-- Task B2: Adversarial Security & Corrupt File Testing
│   ├── README.md                                   <-- Adversarial input analysis & blacklist flaws
│   ├── output.txt                                  <-- Security test log
│   └── src/
│       └── AdversarialSecurityAnalysisDemo.java    <-- SQLi payloads & damaged file ingestion test
├── TaskB3_JdbcBatchPerformance/                    <-- Task B3: JDBC Statement vs PreparedStatement vs Batch
│   ├── README.md                                   <-- Complete the Code, 100k benchmark, trade-off
│   ├── schema.sql                                  <-- Relational bus_pass table schema
│   ├── output.txt                                  <-- Execution log (30x batch speedup & rollback)
│   └── src/
│       ├── PassLoader.java                         <-- Completed Code (TODO 1, 2, 3 resolved)
│       └── JdbcBatchPerformanceBenchmark.java      <-- Batch scaling benchmark & metadata inspector
└── TaskB4_FinalEngineeringDecision/                <-- Task B4: Multi-Campus Migration & Scale Decision
    ├── README.md                                   <-- Migration report, DatabaseMetaData, final decision
    ├── output.txt                                  <-- Migration execution log & metadata dump
    └── src/
        └── CampusPassMigrationApp.java             <-- Batch migration ETL engine & metadata inspector
```

---

## ⚙️ Quick Build & Execution Instructions

All tasks are implemented in pure standard Java without requiring external JAR downloads or database setup.

### 1. Compile All Tasks
```bash
# From repository root
export PATH="/Users/thejasbalaji/Library/Application Support/minecraft/runtime/java-runtime-epsilon/osx-arm64/java-runtime-epsilon/jre.bundle/Contents/Home/bin:$PATH"

javac -d "ASSESSMENT 5/TaskA1_FileFormatDesign/bin" "ASSESSMENT 5/TaskA1_FileFormatDesign/src/"*.java
javac -d "ASSESSMENT 5/TaskA2_BufferingDesign/bin" "ASSESSMENT 5/TaskA2_BufferingDesign/src/"*.java
javac -d "ASSESSMENT 5/TaskA3_DataAccessPattern/bin" "ASSESSMENT 5/TaskA3_DataAccessPattern/src/"*.java
javac -d "ASSESSMENT 5/TaskA4_SerializationDesign/bin" "ASSESSMENT 5/TaskA4_SerializationDesign/src/"*.java
javac -d "ASSESSMENT 5/TaskA5_TransactionSafety/bin" "ASSESSMENT 5/TaskA5_TransactionSafety/src/"*.java
javac -d "ASSESSMENT 5/TaskB1_InstrumentedFileStore/bin" "ASSESSMENT 5/TaskB1_InstrumentedFileStore/src/"*.java
javac -d "ASSESSMENT 5/TaskB2_AdversarialInputs/bin" "ASSESSMENT 5/TaskB2_AdversarialInputs/src/"*.java
javac -d "ASSESSMENT 5/TaskB3_JdbcBatchPerformance/bin" "ASSESSMENT 5/TaskB3_JdbcBatchPerformance/src/"*.java
javac -cp "ASSESSMENT 5/TaskB3_JdbcBatchPerformance/bin" -d "ASSESSMENT 5/TaskB4_FinalEngineeringDecision/bin" "ASSESSMENT 5/TaskB4_FinalEngineeringDecision/src/"*.java
```

### 2. Run Test Demos & Benchmarks
```bash
# Task A1: File Store Design Demo
java -cp "ASSESSMENT 5/TaskA1_FileFormatDesign/bin" BusPassFileStoreDemo

# Task A2: Buffering Cost Benchmark
java -cp "ASSESSMENT 5/TaskA2_BufferingDesign/bin" BufferingCostBenchmarkDemo

# Task A3: Generic DAO Architecture Demo
java -cp "ASSESSMENT 5/TaskA3_DataAccessPattern/bin" GenericDaoArchitectureDemo

# Task A4: Object Serialization Demo
java -cp "ASSESSMENT 5/TaskA4_SerializationDesign/bin" HostelSerializationDemo

# Task A5: Transaction Safety & SQLi Demo
java -cp "ASSESSMENT 5/TaskA5_TransactionSafety/bin" TransactionAndInjectionSecurityDemo

# Task B1: PassStore & File Store Growth Benchmark
java -cp "ASSESSMENT 5/TaskB1_InstrumentedFileStore/bin" PassStore
java -cp "ASSESSMENT 5/TaskB1_InstrumentedFileStore/bin" FileStoreInstrumentationBenchmark

# Task B2: Adversarial Security Analysis Demo
java -cp "ASSESSMENT 5/TaskB2_AdversarialInputs/bin" AdversarialSecurityAnalysisDemo

# Task B3: JDBC Batch Performance & PassLoader Demo
java -cp "ASSESSMENT 5/TaskB3_JdbcBatchPerformance/bin" JdbcBatchPerformanceBenchmark

# Task B4: Campus Pass Migration & Metadata Inspection
java -cp "ASSESSMENT 5/TaskB4_FinalEngineeringDecision/bin:ASSESSMENT 5/TaskB3_JdbcBatchPerformance/bin" CampusPassMigrationApp
```

---

## 🔒 Progressive Git Commit History

Progressive version control was strictly maintained with dedicated semantic commits for every task:

1. `bb22593` - `feat(TaskA1): design bus-pass file format, operations pseudocode, and error containment`
2. `e7e72b6` - `feat(TaskA2): analyze unbuffered vs buffered I/O cost, syscall reduction, and memory complexity`
3. `01ccad4` - `feat(TaskA3): design generic DAO interface, object-relational mapping, and blast radius analysis`
4. `2daeda8` - `feat(TaskA4): analyze transient fields, object graph serialization, and serialVersionUID schema evolution`
5. `eae045a` - `feat(TaskA5): design ACID wallet transaction and demonstrate SQL injection AST manipulation vs PreparedStatement`
6. `8eaee1e` - `feat(TaskB1): complete PassStore find method, validate damaged data bypass, and benchmark buffering and file growth`
7. `3294f51` - `feat(TaskB2): engineer adversarial SQL injection payloads and damaged file inputs with defensive mitigations`
8. `257f689` - `feat(TaskB3): complete PassLoader batch loader, benchmark Statement vs PreparedStatement vs Batch, and verify rollback`
9. `cd71b1c` - `feat(TaskB4): implement ETL migration engine, inspect DatabaseMetaData, and justify centralized RDBMS decision`
10. `docs: add comprehensive master README for ASSESSMENT 5`
