# Task A5: Design for Correctness and Safety – Transactions & SQL Injection

**Course**: Programming in Java (CO5: Data Persistence Engineering)  
**Student Name**: Thejas Balaji  
**Register Number**: 192565092  
**Marks**: 4 Marks | **Bloom's Level**: K4 (Analyse)  

---

## A5.1 Transaction Design (Hostel to Canteen Wallet Transfer)

A student transfers **₹500** from a hostel wallet (`HOSTEL_W1`) to a canteen wallet (`CANTEEN_W1`). In an enterprise system, this multi-step operation must be governed by an **ACID Transaction Boundary**:

### SIMATS Algorithmic Pseudocode
```text
ALGORITHM TransferWalletFunds(con, senderWalletId, receiverWalletId, transferAmount)
BEGIN
    oldAutoCommit <- con.getAutoCommit()
    con.setAutoCommit(FALSE) // STEP 1: Disable Auto-Commit (Start Transaction)

    TRY
        // STEP 2: Atomic Debit with Invariant Balance Check
        sqlDebit <- "UPDATE wallets SET balance = balance - ? WHERE wallet_id = ? AND balance >= ?"
        TRY WITH (stmtDebit <- con.prepareStatement(sqlDebit))
            stmtDebit.setDouble(1, transferAmount)
            stmtDebit.setString(2, senderWalletId)
            stmtDebit.setDouble(3, transferAmount)
            rowsUpdated <- stmtDebit.executeUpdate()

            IF rowsUpdated == 0 THEN
                THROW InsufficientFundsException("Insufficient balance or sender wallet not found")
            END IF
        END TRY

        // STEP 3: Atomic Credit to Receiver
        sqlCredit <- "UPDATE wallets SET balance = balance + ? WHERE wallet_id = ?"
        TRY WITH (stmtCredit <- con.prepareStatement(sqlCredit))
            stmtCredit.setDouble(1, transferAmount)
            stmtCredit.setString(2, receiverWalletId)
            rowsCredited <- stmtCredit.executeUpdate()

            IF rowsCredited == 0 THEN
                THROW WalletNotFoundException("Receiver wallet does not exist")
            END IF
        END TRY

        // STEP 4: Commit Atomic Transaction
        con.commit() // Writes changes permanently to Write-Ahead Log (WAL)
        RETURN TRUE

    CATCH Exception ex
        // STEP 5: Rollback on Any Failure
        con.rollback() // Reverts both debit and credit to pre-transaction state
        LOG_CRITICAL("Transaction failed and rolled back safely: " + ex.getMessage())
        THROW ex

    FINALLY
        // STEP 6: Restore Previous Connection State
        con.setAutoCommit(oldAutoCommit)
    END TRY
END
```

### Guarantees Provided by the Design
- **Atomicity (All-or-Nothing)**: Either both the debit and credit succeed together, or neither takes effect.
- **Consistency**: The system-wide invariant is preserved:
  $$\text{Balance}_{\text{hostel}} + \text{Balance}_{\text{canteen}} = \text{Constant}$$
  Money is mathematically prohibited from being created or destroyed.

---

## A5.2 Injection Analysis and Secure Redesign

### The Vulnerable Query
```java
String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
```

### How the Vulnerability is Exploited
Suppose an adversary submits:
- **Username (`id`)**: `admin' --`
- **Password (`pwd`)**: `ArbitraryWrongPassword123`

When concatenated into the raw query string:
```sql
SELECT * FROM users WHERE id='admin' --' AND pwd='ArbitraryWrongPassword123'
```

#### What Happens in the Database Engine:
1. The quote character (`'`) immediately closes the string literal for the `id` column.
2. The sequence `--` represents the SQL single-line comment delimiter.
3. The database SQL lexer and parser **completely discard everything after the comment marker**, including the password verification clause (`AND pwd='...'`).
4. The database executes:
   ```sql
   SELECT * FROM users WHERE id = 'admin'
   ```
5. The query matches the administrator account and returns a valid `ResultSet`. The application logs the adversary in as administrator without any valid password!

### Secure Redesign Using `PreparedStatement`

```java
public boolean authenticateUser(Connection con, String id, String pwd) throws SQLException {
    String sql = "SELECT user_id, role, password_hash FROM users WHERE id = ? AND password_hash = ?";
    
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, id.trim());
        ps.setString(2, hashPassword(pwd)); // Compare against salted cryptographic hash
        
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next(); // True only if BOTH exact credentials match
        }
    }
}
```

### Why `PreparedStatement` Makes SQL Injection Mathematically Impossible
1. **Compilation Precedes Data Binding**: The database compiler parses and compiles the SQL statement into an **Abstract Syntax Tree (AST)** and query execution plan **before** any user inputs are received.
2. **Context Separation**: The placeholders (`?`) represent pure literal data slots. Even if user input contains `' --` or `OR '1'='1'`, the database SQL engine treats those characters as **literal text strings within the data cell**, never as executable SQL control operators. The query grammar structure remains immutable.

---

## Alternative Concept & Complexity Analysis: Auto-Commit vs Single Transaction

### Analytical Comparison

| Metric / Scenario | Two Auto-Commit Statements | Single ACID Transaction (`commit` / `rollback`) |
|---|---|---|
| **Behavior During Mid-Way Failure** (Power cut or network loss after debit) | **Catastrophic Inconsistency**: Debit was already committed to disk. Credit was never reached. **₹500 has permanently vanished from the system**. Requires manual forensic accounting and customer service intervention. | **Guaranteed Consistency**: The database transaction manager detects connection severance or abort, invokes rollback from Undo logs, and restores the sender's ₹500 immediately. |
| **Database Disk I/O Latency** | **$2 \times \text{fsync}$ latency**: Every auto-commit statement must independently flush the database Write-Ahead Log (WAL) to physical disk platter ($\approx 2 \times 5\text{ ms} = \mathbf{10\text{ ms}}$). | **$1 \times \text{fsync}$ latency**: Both updates are staged in database memory buffers; only a **single `fsync`** occurs at `con.commit()` ($\approx \mathbf{5\text{ ms}}$). |
| **Lock Holding Duration** | Row locks are acquired and released twice independently, opening race condition windows for interleaved concurrent updates. | Row locks are held atomically throughout the transaction, preventing dirty reads and non-repeatable reads. |

**Conclusion**: Executing multi-table financial operations under auto-commit is a critical engineering anti-pattern. Atomic transactions provide **superior disk performance** while eliminating financial data corruption.

---

## A5.3 Execution Output Verification

```text
==================================================================
 TASK A5: Design for Correctness and Safety - Demo               
==================================================================

[EXPERIMENT 1] Auto-Commit Failure vs Atomic Transaction Rollback:
Initial Balances: Hostel=Rs.2000.00 | Canteen=Rs.500.00
  [UNSAFE AUTO-COMMIT] Starting transfer of Rs. 500.0
    Step 1: Debited Rs. 500.00 from HOSTEL_W1 (New balance: 1500.00) [AUTO-COMMITTED]
    *** CRITICAL SYSTEM CRASH / POWER FAILURE OCCURRED BETWEEN UPDATES! ***
  Caught expected crash: PowerFailureException: Server went down before step 2
Balances After Unsafe Crash: Hostel=Rs.1500.00 | Canteen=Rs.500.00
  >> RESULT: Rs. 500 WAS PERMANENTLY LOST INTO THIN AIR!

Testing Safe Transaction with Rollback:
  [SAFE TRANSACTION] Starting atomic transfer of Rs. 500.0
    Step 1: Debited Rs. 500.00 from HOSTEL_W1 in transaction
    *** CRITICAL SYSTEM CRASH OCCURRED! TRIGGERING ROLLBACK ***
    Step 4: ROLLBACK EXECUTED -> Restored sender and receiver balances.
Balances After Transaction Rollback: Hostel=Rs.2000.00 | Canteen=Rs.500.00
  >> RESULT: ROLLED BACK SAFELY! Zero money lost or created.

[EXPERIMENT 2] SQL Injection Attack Simulation:

1. Testing Against Vulnerable String-Concatenated SQL:
  Generated Raw SQL: SELECT * FROM users WHERE id='admin' --' AND pwd='WrongPassword999'
  [VULNERABILITY EXPLOITED] Comment marker '--' or 'OR 1=1' bypassed password check!
  Authentication Outcome: ACCESS GRANTED (INTRUSION SUCCESSFUL!)

2. Testing Against Secure PreparedStatement:
  Compiled SQL Template: SELECT * FROM users WHERE id = ? AND pwd = ?
  Bound Parameters: [1 => admin' --], [2 => WrongPassword999]
  Authentication Outcome: ACCESS DENIED (ATTACK NEUTRALIZED!)

==================================================================
 Task A5 Correctness and Safety Principles 100% Verified!        
==================================================================
```
