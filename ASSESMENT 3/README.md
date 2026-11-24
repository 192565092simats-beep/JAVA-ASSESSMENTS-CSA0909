# Java Assignment 3 – CO3: Exception Handling & Multithreading

**Course**: Programming in Java (CO3 - CSA0909)  
**Assignment**: Assignment 3 – Exception Handling and Multithreading (Sessions 27–38)  
**Mode**: Pair Work – 2 Students per Group  
**Student A**: Thejas Balaji (Register Number: 192565092)  
**Student B**: Pair Partner / Collaborator  
**Institution**: SIMATS Engineering (Saveetha Institute of Medical and Technical Sciences)  
**Java Version**: OpenJDK 25.0.1 LTS (Java 17 / 21 / 25 compatible)  
**GitHub Repository Link**: [https://github.com/192565092simats-beep/JAVA-ASSESSMENTS-CSA0909/tree/main/ASSESMENT%203](https://github.com/192565092simats-beep/JAVA-ASSESSMENTS-CSA0909/tree/main/ASSESMENT%203)

---

## Individual Contribution Record

| Question | Assessed Area | Bloom's Level | Marks | Student A Responsibility | Student B Responsibility | Joint Responsibility |
| :---: | :--- | :---: | :---: | :--- | :--- | :--- |
| **Q1** | Bank ATM Failure Simulation | K4 – Analyse | 6 | Exception Hierarchy & Catch Block Ordering | 5-Path Test Matrix & Stack Trace Analysis | Single-Catch Defense & Error Code Comparison |
| **Q2** | Online Examination Portal | K4 – Analyse | 7 | Exception Hierarchy & 3-Layer Propagation | Failure Scenarios & Chaining Evidence (`getCause()`) | Storage Exception Abstraction & Code Completion |
| **Q3** | Resource Safety & Debugging | K4 – Analyse | 7 | Step-through Debugging & File Leak Isolation | Design Correctness & AutoCloseable Semantics | Engineering Decision on TWR Robustness |
| **Q4** | Thread Creation & Lifecycle | K4 – Analyse | 7 | Thread vs Runnable & Lifecycle State Diagram | 10-Run Scheduler Trace & Daemon Experiments | Non-Deterministic Ordering Defense |
| **Q5** | Cinema Seat Booking Race | K4 – Analyse | 6 | Race Condition Interleaving & Critical Section | Synchronization Strategies & 10,000 Contention Benchmark | 20-Screen Locking Architecture & Code Completion |
| **Q6** | Deadlock & Inter-Thread Comm | K4 – Analyse | 7 | Producer-Consumer Design (`wait`/`notifyAll`) | Deadlock Analysis & Global Lock Ordering | Commonality Analysis & Code Completion |
| **Q7** | Fault-Tolerant Result Processor| K5 – Evaluate | 10 | Stage 2: Concurrent Design (`Callable`/`Future`) | Stage 3: Implementation, Injection & Measurements | Stage 1 (Problem Interpretation) & Stage 4 (Defense) |
| **TOTAL**| | | **50** | | | |

---

## Summary of Questions and Verified Outputs

| Task | Core Problem | Key Completed Programs | Result Summary |
| :--- | :--- | :--- | :--- |
| [**Q1**](Q1/) | Bank ATM Simulation | `ATMSimulation.java` | Verified 5 distinct failure paths: valid, letters format, negative amount, insufficient balance, missing card file. Guaranteed `finally` card ejection. |
| [**Q2**](Q2/) | Online Exam Portal | `StorageDemo.java`<br>`ExamPortalArchitectureDemo.java` | **Completed Code**: Exact output match (`Saved answer01.pdf`, `Error: Could not store answer02.pdf`, `Cause: Disk quota exceeded`). 3-layer propagation verified. |
| [**Q3**](Q3/) | Daily Sales Report Generator | `ManualCloseReport.java`<br>`TryWithResourcesReport.java` | Demonstrated 3 open handles leaked upon failure in manual version; 0 handles leaked with guaranteed auto-close and suppressed exception capture in TWR. |
| [**Q4**](Q4/) | Answer-Script Scanner | `ScannerStationThread.java`<br>`ScannerStationRunnable.java`<br>`ThreadLifeCycleDemo.java` | Demonstrated `run()` vs `start()`, `join()` synchronization, thread state transitions, daemon termination, and 10-run scheduler non-determinism. |
| [**Q5**](Q5/) | Cinema Seat Booking | `SeatBooking.java`<br>`RaceConditionStudy.java` | **Completed Code**: Atomic booking exact output match (`Seats sold: 10`). Unsynchronized race condition reproduced; 10,000 bookings benchmarked. |
| [**Q6**](Q6/) | Hostel Wallet & Token Counter | `TokenCounter.java`<br>`WalletTransferDemo.java` | **Completed Code**: Exact output match (`Sum of tokens collected: 210 (expected 210)`). Prevented wallet deadlock via global ID lock ordering. |
| [**Q7**](Q7/) | Semester Result Processor | `ResultProcessor.java`<br>`BenchmarkProcessor.java` | Benchmarked 10,000 files across 1, 2, 4, 8 threads with 100% exact match against hand-checked totals (9,000 success, 500 missing, 500 corrupt). |

---

## Steps to Compile and Execute Each Task

Ensure Java 17+ is on your PATH.

### Q1: ATM Simulation
```bash
javac "ASSESMENT 3/Q1/src/ATMSimulation.java" -d "ASSESMENT 3/Q1/bin"
java -cp "ASSESMENT 3/Q1/bin" ATMSimulation
```

### Q2: Exam Portal & Storage Chaining
```bash
javac "ASSESMENT 3/Q2/src/StorageDemo.java" -d "ASSESMENT 3/Q2/bin"
javac "ASSESMENT 3/Q2/src/ExamPortalArchitectureDemo.java" -d "ASSESMENT 3/Q2/bin"
java -cp "ASSESMENT 3/Q2/bin" StorageDemo
java -cp "ASSESMENT 3/Q2/bin" ExamPortalArchitectureDemo
```

### Q3: Resource Safety & Try-with-Resources
```bash
javac "ASSESMENT 3/Q3/src/ManualCloseReport.java" "ASSESMENT 3/Q3/src/TryWithResourcesReport.java" -d "ASSESMENT 3/Q3/bin"
java -cp "ASSESMENT 3/Q3/bin" TryWithResourcesReport
```

### Q4: Parallel Answer-Script Scanning
```bash
javac "ASSESMENT 3/Q4/src/ScannerStationThread.java" "ASSESMENT 3/Q4/src/ScannerStationRunnable.java" "ASSESMENT 3/Q4/src/ThreadLifeCycleDemo.java" -d "ASSESMENT 3/Q4/bin"
java -cp "ASSESMENT 3/Q4/bin" ThreadLifeCycleDemo
```

### Q5: Cinema Seat Booking Race Conditions
```bash
javac "ASSESMENT 3/Q5/src/SeatBooking.java" "ASSESMENT 3/Q5/src/RaceConditionStudy.java" -d "ASSESMENT 3/Q5/bin"
java -cp "ASSESMENT 3/Q5/bin" SeatBooking
java -cp "ASSESMENT 3/Q5/bin" RaceConditionStudy
```

### Q6: Deadlock & Token Counter
```bash
javac "ASSESMENT 3/Q6/src/TokenCounter.java" "ASSESMENT 3/Q6/src/WalletTransferDemo.java" -d "ASSESMENT 3/Q6/bin"
java -cp "ASSESMENT 3/Q6/bin" TokenCounter
java -cp "ASSESMENT 3/Q6/bin" WalletTransferDemo
```

### Q7: Concurrent Fault-Tolerant Result Processor
```bash
javac "ASSESMENT 3/Q7/src/StudentScore.java" "ASSESMENT 3/Q7/src/ResultProcessor.java" "ASSESMENT 3/Q7/src/BenchmarkProcessor.java" -d "ASSESMENT 3/Q7/bin"
java -cp "ASSESMENT 3/Q7/bin" BenchmarkProcessor
```

---

## Progressive Git Commit History

The tasks have been progressively committed to Git as required:
1. `chore: setup .gitignore and prepare ASSESMENT 3 directory`
2. `feat(Q1): implement bank ATM exception handling simulation, hierarchy, and test matrix`
3. `feat(Q2): complete StorageDemo, implement 3-tier exception propagation and chaining demo`
4. `feat(Q3): implement resource leak debugging, try-with-resources, and suppressed exceptions analysis`
5. `feat(Q4): implement thread creation, lifecycle states, join synchronization, and daemon experiments`
6. `feat(Q5): complete SeatBooking code, race condition analysis, and synchronization strategies benchmark`
7. `feat(Q6): complete TokenCounter code, inter-thread communication, and deadlock prevention demo`
8. `feat(Q7): implement concurrent fault-tolerant semester result processor, benchmark, and design defense`
9. `docs: add comprehensive master README for ASSESMENT 3 with contribution records and build instructions`
