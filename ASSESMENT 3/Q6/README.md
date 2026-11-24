# Q6 – Deadlock and Inter-Thread Communication

**Bloom's Level: K4 – Analyse | Marks: 7**  
**CO3 Coverage**: Inter-thread communication (`wait()`, `notify()`, `notifyAll()`), spurious wakeups, 4 Coffman deadlock conditions, lock-ordering prevention, and `ArrayBlockingQueue` alternatives.

---

## 1. Student A – Producer–Consumer Pattern (`TokenCounter`)

### A. Why `wait()` MUST Be Called Inside a `while` Loop
1. **Spurious Wakeups**: The POSIX and JVM specifications allow threads to wake up from `wait()` without any explicit `notify()` or interrupt call.
2. **State Invalidation Between Wakeup and Lock Reacquisition**:
   - Thread $A$ calls `notifyAll()`.
   - Consumer 1 and Consumer 2 both wake up and compete to re-acquire the synchronized lock.
   - Consumer 1 wins the lock, consumes the only token, and releases the lock.
   - Consumer 2 acquires the lock next. If an `if` statement was used, Consumer 2 proceeds assuming the buffer is non-empty, reading stale data or throwing an `ArrayIndexOutOfBoundsException`!
   - A `while` loop forces the thread to re-verify the predicate before proceeding.

### B. Why `wait()` Must Be Called Inside a `synchronized` Block
- `wait()` atomically places the current thread into the object's **Wait Set** and **releases the intrinsic monitor lock**.
- If called outside a synchronized block, the JVM throws `java.lang.IllegalMonitorStateException` because the thread does not hold the monitor.

### C. `notify()` vs. `notifyAll()` Hazard
- **`notify()`**: Wakes up an arbitrary single thread from the wait set. If a consumer consumes a token and calls `notify()`, the JVM might wake another *consumer* instead of a *producer*. The second consumer finds the buffer empty and goes back to sleep. Now all threads are waiting, causing **Deadlock / Lost Wakeup**.
- **`notifyAll()`**: Wakes all waiting threads, allowing both producers and consumers to re-evaluate their respective conditions.

### D. `sleep()` vs. `wait()` Comparison

| Feature | `Thread.sleep(millis)` | `Object.wait()` |
| :--- | :--- | :--- |
| **Lock Handling** | **Retains all monitor locks** while sleeping. Other threads remain blocked. | **Atomically releases the monitor lock**, allowing competing threads to enter. |
| **Class Origin** | Static method of `java.lang.Thread`. | Non-static instance method of `java.lang.Object`. |
| **Wakeup Mechanism** | Resumes automatically after timeout expires. | Resumes only when `notify()`/`notifyAll()` is called (or timeout/interrupt). |
| **Context** | Callable from anywhere. | Callable **only** within a synchronized block. |

---

## 2. Student B – Deadlock Analysis and Prevention

### A. The 4 Coffman Conditions for Deadlock
1. **Mutual Exclusion**: An account wallet lock can be held by only one thread at a time.
2. **Hold and Wait**: Thread 1 holds Account A's lock while waiting to acquire Account B's lock.
3. **No Preemption**: A lock cannot be forcibly taken away from a thread; it must be voluntarily released.
4. **Circular Wait**: Thread 1 holds $A$ and waits for $B$; Thread 2 holds $B$ and waits for $A$.

### B. Deadlock Prevention via Global Lock Ordering
- **Rule**: Never acquire locks in the order given by method parameters `(from, to)`.
- **Solution**: Compare unique immutable account identifiers (e.g. `acc1.id.compareTo(acc2.id)`):
  - Always acquire the lock on the lexicographically smaller account ID first, followed by the larger account ID.
- **Why It Works**: Breaks the **Circular Wait** condition completely. If Thread 1 transfers $A \rightarrow B$ and Thread 2 transfers $B \rightarrow A$, both threads will attempt to acquire lock $A$ first! The second thread simply waits for $A$ before touching $B$, eliminating cycles.

---

## 3. Joint Conclusion

### Commonality Between Deadlock and Producer-Consumer
Both paradigms deal with **resource contention and thread synchronization hazards**.
- **Deadlock**: Caused by uncoordinated, circular dependencies on multiple shared resources.
- **Producer-Consumer**: Manages flow control and coordination between threads running at different speeds over a bounded shared buffer.
- **Progress Guarantee**: Lock-ordering guarantees progress in multi-resource locking, while `wait()` / `notifyAll()` in `while` loops guarantees progress in conditional producer-consumer signaling.

---

## 4. Complete the Code & Output Verification

### Completed `TokenCounter.java`
```java
public synchronized void put(int token) throws InterruptedException {
    while (count == buffer.length) {
        wait();
    }
    buffer[in] = token;
    in = (in + 1) % buffer.length;
    count++;
    notifyAll();
}

public synchronized int take() throws InterruptedException {
    while (count == 0) {
        wait();
    }
    int token = buffer[out];
    out = (out + 1) % buffer.length;
    count--;
    notifyAll();
    return token;
}
```

### Execution Output (Exact Match with Prompt)
```
Sum of tokens collected: 210 (expected 210)
```

### Follow-up Question Answer:
> *Change `while` to `if` in both methods, run it several times, and explain any difference.*

When changed to `if`, after a thread is awakened, it proceeds to insert or remove a token **without re-checking `count`**. Under high concurrency or spurious wakeups, two consumers both proceed when `count == 1`: the first decrements `count` to 0, and the second decrements to -1, corrupting circular pointers and throwing `ArrayIndexOutOfBoundsException` or corrupting tokens.

---

## 5. Alternative Concept & Complexity Analysis

### `wait()`/`notifyAll()` vs. `ArrayBlockingQueue` vs. `tryLock()` with Timeout

| Metric | Intrinsic `wait()`/`notifyAll()` | `ArrayBlockingQueue<Integer>(5)` | `ReentrantLock.tryLock()` with Timeout |
| :--- | :--- | :--- | :--- |
| **Implementation Complexity** | High (Prone to lost wakeups, spurious wakeups, deadlocks) | **Minimal (Zero boilerplate)**: `queue.put()`, `queue.take()` | Moderate (Requires manual lock sequencing & backoff loops) |
| **Time per Operation** | ~100–300 ns | ~80–150 ns (Uses dual `Condition` queues for separate notFull/notEmpty) | ~200–500 ns (Includes timeout checks) |
| **Memory Footprint** | Minimal: Primitive circular array ($20\text{ bytes}$) + lock overhead | ~128 bytes (Node references, ReentrantLock, 2 Conditions) | Moderate |
| **Deadlock Avoidance** | Requires strict lock ordering | Built-in thread-safe queue | **Prevents deadlock** via timeout and backoff |

---

## 6. Compilation and Execution

```bash
# Compile
javac "ASSESMENT 3/Q6/src/TokenCounter.java" "ASSESMENT 3/Q6/src/WalletTransferDemo.java" -d "ASSESMENT 3/Q6/bin"

# Run Completed TokenCounter
java -cp "ASSESMENT 3/Q6/bin" TokenCounter

# Run Wallet Transfer Deadlock Prevention Demo
java -cp "ASSESMENT 3/Q6/bin" WalletTransferDemo
```
