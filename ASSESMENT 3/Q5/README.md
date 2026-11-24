# Q5 – Race Conditions in a Cinema Seat-Booking System

**Bloom's Level: K4 – Analyse | Marks: 6**  
**CO3 Coverage**: Race conditions, critical sections, check-then-act vulnerabilities, intrinsic locks, synchronized methods vs blocks, and atomic CAS primitives.

---

## 1. Student A – Producing and Explaining the Race Condition

### A. The "Check-Then-Act" Race Condition Trace
In the unsynchronized version:
```java
if (!booked[seat]) {         // Step 1: Check state
    booked[seat] = true;     // Step 2: Act / Mutate state
    bookingsMade++;
}
```
Suppose Seat 4 is currently free (`booked[4] == false`). Two customers (User Thread A and User Thread B) click "Book Seat 4" concurrently:

| Time | Thread A (Customer 1) | Thread B (Customer 2) | Shared Memory State (`booked[4]`, `bookingsMade`) |
| :---: | :--- | :--- | :--- |
| **$t_1$** | Evaluates `if (!booked[4])` $\rightarrow$ **`true`** | Interleaved / Preempted | `booked[4] = false`, `bookingsMade = 0` |
| **$t_2$** | Context-switched out before updating flag | Evaluates `if (!booked[4])` $\rightarrow$ **`true`**! | `booked[4] = false`, `bookingsMade = 0` |
| **$t_3$** | Resumes: `booked[4] = true` | Waiting in time slice | `booked[4] = true`, `bookingsMade = 0` |
| **$t_4$** | `bookingsMade++` $\rightarrow$ $1$ | Resumes: `booked[4] = true` | `booked[4] = true`, `bookingsMade = 1` |
| **$t_5$** | Booking confirmation shown | `bookingsMade++` $\rightarrow$ **$2$ (DOUBLE BOOKED!)** | `booked[4] = true`, `bookingsMade = 2` |

Both customers are issued tickets for the exact same seat, and `bookingsMade` increments twice!

### B. The Critical Section
The critical section is the compound execution of:
1. Reading the seat status (`!booked[seat]`)
2. Writing the updated status (`booked[seat] = true`)
3. Incrementing total bookings counter (`bookingsMade++`)

This compound operation must be made **strictly atomic and mutually exclusive**.

---

## 2. Student B – Comparing Synchronization Strategies

### A. Synchronization Granularity Comparison

| Strategy | Intrinsic Lock Used | Scope of Lock | Throughput Under Contention |
| :--- | :--- | :--- | :--- |
| **Synchronized Method** | Instance lock on `screen` (`this`) | Locks the entire method. Threads booking Seat 1 block threads booking Seat 9! | Moderate (Serializes all bookings for that screen) |
| **Synchronized Block (Per-Seat)** | Array of lock objects `seatLocks[seat]` | Locks **only the specific seat** requested. Different seats process in parallel! | **High** (True parallel bookings across distinct seats) |
| **Static Synchronized** | Class object lock (`SeatBooking.class`) | Locks the entire class across ALL screens in the theater. | **Extremely Poor** (Global bottleneck) |

### B. What is Static Synchronization and Why Avoid It Here?
A static synchronized method (`public static synchronized void book(...)`) acquires the class-level monitor lock (`SeatBooking.class`). If applied to a cinema complex, a customer booking Screen 1 in Chennai would block a customer booking Screen 20 in Bangalore!

---

## 3. Joint Engineering Decision

### Recommended Architecture for a Cinema with 20 Screens
**Deploy Per-Screen Fine-Grained Locking or `AtomicBoolean[]` per screen.**
- **Rationale**:
  1. Each of the 20 screens represents an independent resource domain. Bookings for Screen 1 have zero overlap with Screen 2.
  2. Within each screen, an array of `AtomicBoolean` (one per seat) allows non-blocking, lock-free hardware CAS operations (`compareAndSet(false, true)`).
  3. This provides maximum concurrency: 20 screens $\times$ hundreds of seats can be booked simultaneously without any thread contention, eliminating thread parking/unparking context switches.

---

## 4. Complete the Code & Output Verification

### Completed `SeatBooking.java`
```java
public class SeatBooking {
    private final boolean[] booked;
    private int bookingsMade = 0;

    SeatBooking(int seats) { booked = new boolean[seats]; }

    // TODO 1: Make the check-then-book operation atomic so that a seat can never be sold twice
    public synchronized boolean book(int seat) {
        if (!booked[seat]) {
            booked[seat] = true;
            bookingsMade++;
            return true;
        }
        return false;
    }
    // ...
}
```

### Execution Output (Exact Match with Prompt)
```
Seats sold: 10
```

---

## 5. Alternative Concept & Complexity Analysis

### Synchronized Method vs. `AtomicBoolean[]` CAS vs. `ConcurrentHashMap`

| Approach | Time per Booking Under Heavy Contention | Extra Memory Space for $s$ Seats | Error-Proneness |
| :--- | :--- | :--- | :--- |
| **Synchronized Method** | High contention: Threads block/suspend, triggering OS thread context switches (~1-3 µs per switch). | Minimal: Primitive `boolean[s]` ($s\text{ bytes}$) + 1 object header. | Low (Easy to implement) |
| **`AtomicBoolean[]` (CAS)** | **Extremely Fast**: Zero thread blocking; uses CPU `LOCK CMPXCHG` instruction (~10-20 ns). | $s \times \text{AtomicBoolean objects}$ (~$24\text{ bytes}$ each) $\approx 24s\text{ bytes}$. | **Optimal for Production** |
| **`ConcurrentHashMap.putIfAbsent()`** | Fast: Bucket-level striped synchronization. | Node overhead + hash table buckets (~48 bytes per seat). | Slightly higher memory overhead |

---

## 6. Compilation and Execution

```bash
# Compile
javac "ASSESMENT 3/Q5/src/SeatBooking.java" "ASSESMENT 3/Q5/src/RaceConditionStudy.java" -d "ASSESMENT 3/Q5/bin"

# Run SeatBooking (Completed Code)
java -cp "ASSESMENT 3/Q5/bin" SeatBooking

# Run Race Condition Study Benchmark
java -cp "ASSESMENT 3/Q5/bin" RaceConditionStudy
```
