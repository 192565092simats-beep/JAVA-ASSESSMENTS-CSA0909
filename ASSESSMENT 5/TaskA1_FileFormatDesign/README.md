# Task A1: Design a File Format – Bus-Pass Records

**Course**: Programming in Java (CO5: Data Persistence Engineering)  
**Student Name**: Thejas Balaji  
**Register Number**: 192565092  
**Marks**: 7 Marks | **Bloom's Level**: K4 (Analyse)  

---

## A1.1 Represent the Record

The transport office issues ~8,000 bus passes annually. Each record comprises seven attributes:
1. `pass_id`: Unique identifier (e.g. `P1001`)
2. `reg_no`: Student registration number (e.g. `192565092`)
3. `name`: Student full name (e.g. `Balaji, Thejas`)
4. `route_no`: Integer bus route number (e.g. `12`)
5. `boarding_stop`: Pickup location (e.g. `Tambaram Sanatorium`)
6. `validity_date`: ISO date string (e.g. `2026-12-31`)
7. `amount_paid`: Decimal fee amount (e.g. `4500.00`)

### (a) Delimited Text File Layout (RFC 4180 CSV Standard)
- **Field Order**: `pass_id,reg_no,name,route_no,boarding_stop,validity_date,amount_paid`
- **Field Delimiter**: Comma (`,`)
- **Record Delimiter**: Standard newline (`\n` or `\r\n`)
- **Handling Delimiter in Fields**: If the student's name contains a comma (e.g. `"Balaji, Thejas"`), the token is encapsulated within double quotes (`"`). Any inner quote is escaped as `""`.
- **Sample Line**:
  ```csv
  P1001,192565092,"Balaji, Thejas",12,Tambaram Sanatorium,2026-12-31,4500.00
  P1002,192565093,Karthik Raja,12,Guindy Metro,2026-12-31,4500.00
  ```

### (b) Binary File Layout (`DataOutputStream` / `DataInputStream`)
In a binary stream, primitive types are written in big-endian network byte order, and strings are encoded using modified UTF-8 prefixed with a 2-byte unsigned length header:

| Field Order | Field Name | Java Data Type | `DataOutputStream` Method | Exact Byte Size |
|:---:|:---|:---|:---|:---|
| 1 | `pass_id` | `String` | `dos.writeUTF(passId)` | 2 bytes length + UTF-8 bytes (~7 bytes) |
| 2 | `reg_no` | `String` | `dos.writeUTF(regNo)` | 2 bytes length + 9 ASCII bytes = 11 bytes |
| 3 | `name` | `String` | `dos.writeUTF(name)` | 2 bytes length + variable bytes (e.g. 14 bytes) |
| 4 | `route_no` | `int` | `dos.writeInt(routeNo)` | **4 bytes fixed** |
| 5 | `boarding_stop` | `String` | `dos.writeUTF(boardingStop)` | 2 bytes length + variable bytes (~15 bytes) |
| 6 | `validity_date` | `String` | `dos.writeUTF(validityDate)`| 2 bytes length + 10 bytes = 12 bytes |
| 7 | `amount_paid` | `double` | `dos.writeDouble(amountPaid)`| **8 bytes fixed IEEE 754** |

**Binary Advantage**: Comma escaping is completely unnecessary because string boundaries are explicitly tracked by their 2-byte length prefixes.

---

## A1.2 Design the Operations (SIMATS Algorithmic Pseudocode)

### Stream Class Selection & Justification
- **Chosen Stream Classes**: `BufferedReader` and `BufferedWriter` wrapping character streams (`Files.newBufferedReader` / `Files.newBufferedWriter`) with explicit `StandardCharsets.UTF_8`.
- **Justification**:
  1. `FileReader` / `FileWriter` use platform-default charset (causes cross-platform encoding bugs between Windows ANSI and Linux UTF-8) and lack buffering.
  2. `BufferedReader` provides `readLine()`, which buffers chunks (default 8 KB) in user space, slashing kernel context switches by $99.9\%$.
  3. `BufferedWriter` provides `newLine()`, writing OS-appropriate line terminators safely.

### 1. Add a New Pass
```text
ALGORITHM AddBusPass(passRecord, targetFilePath)
BEGIN
    formattedLine <- FormatRecordToCsv(passRecord) // Handles comma quoting
    
    TRY WITH (writer <- OPEN_BUFFERED_WRITER(targetFilePath, UTF_8, APPEND_MODE))
        writer.write(formattedLine)
        writer.newLine()
    CATCH IOException ex
        LOG_ERROR("Failed to append bus pass: " + ex.getMessage())
        THROW ex
    END TRY
END
```

### 2. Find a Pass by Pass ID
```text
ALGORITHM FindPassById(targetPassId, targetFilePath)
BEGIN
    IF NOT FILE_EXISTS(targetFilePath) THEN
        RETURN NULL
    END IF

    targetPassId <- TRIM(targetPassId)

    TRY WITH (reader <- OPEN_BUFFERED_READER(targetFilePath, UTF_8))
        WHILE (line <- reader.readLine()) != NULL DO
            line <- TRIM(line)
            IF line IS EMPTY OR STARTS_WITH(line, "#") THEN
                CONTINUE
            END IF

            TRY
                pass <- ParseCsvLine(line)
                IF pass.passId EQUALS_IGNORE_CASE targetPassId THEN
                    RETURN pass // Immediate return on match
                END IF
            CATCH DamagedLineException ex
                LOG_WARNING("Skipping damaged line: " + line)
                CONTINUE // Resilient error containment
            END TRY
        END WHILE
    CATCH IOException ex
        LOG_ERROR("I/O error during pass search: " + ex.getMessage())
    END TRY

    RETURN NULL // Not found after scanning entire file
END
```

### 3. List All Passes on a Given Route
```text
ALGORITHM ListPassesByRoute(targetRouteNo, targetFilePath)
BEGIN
    matchedList <- EMPTY_LIST()

    IF NOT FILE_EXISTS(targetFilePath) THEN
        RETURN matchedList
    END IF

    TRY WITH (reader <- OPEN_BUFFERED_READER(targetFilePath, UTF_8))
        WHILE (line <- reader.readLine()) != NULL DO
            TRY
                pass <- ParseCsvLine(TRIM(line))
                IF pass.routeNo == targetRouteNo THEN
                    matchedList.ADD(pass)
                END IF
            CATCH DamagedLineException ex
                LOG_WARNING("Skipping invalid record during route scan")
                CONTINUE
            END TRY
        END WHILE
    CATCH IOException ex
        LOG_ERROR("Error reading route records: " + ex.getMessage())
    END TRY

    RETURN matchedList
END
```

---

## A1.3 Design for Damaged Data

| Failure Scenario | Threat / Failure Symptom | Defensive Engineering Design |
|---|---|---|
| **Damaged Line** | Corrupted line with missing delimiters, non-numeric amount (`"Rs.4500"` instead of `4500.0`), or truncated fields. | **Defensive Tokenization & Exception Isolation**: `parseRecord()` checks `tokens.size() == 7` and wraps numeric parsing in `try-catch (NumberFormatException e)`. A damaged line logs a warning with line number and `CONTINUE`s the loop, preventing the entire batch from crashing. |
| **Missing File** | Application attempts to read when the data file does not exist yet (`NoSuchFileException`). | **Defensive Guardrail**: Check `Files.exists(filePath)`. In read operations, return `Optional.empty()` or empty list. In write operations, use `StandardOpenOption.CREATE` to automatically create parent directories and file. |
| **Incomplete Write (Power Failure / Crash)** | System powers off mid-write, leaving a half-written broken line at the end of the file. | **Atomic Replace Pattern**: Write new data to temporary file `passes.csv.tmp`. Flush buffers and invoke `FileDescriptor.sync()` to force write to physical storage platter. Atomically rename temporary file over target using `Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)`. If power cuts, either old valid file remains or new file lands cleanly. |

---

## Alternative Concept & Complexity Analysis: `RandomAccessFile` with Fixed-Length Records

### Implementation Model
In a fixed-length record architecture, every record occupies an invariant number of bytes $R$ (e.g. $R = 128$ bytes).
Record index $i$ is located at exact byte offset:
$$\text{Offset}(i) = i \times \text{recordSize}$$

```java
try (RandomAccessFile raf = new RandomAccessFile("passes.bin", "rw")) {
    raf.seek(i * RECORD_SIZE);
    // Read or write fixed-size fields
}
```

### Search Time Complexity Comparison
- **Delimited Text File**: To find record $i$, the engine must sequentially scan from byte 0, counting newline characters. Search time is **$O(n)$ Linear Scan Time**.
- **Fixed-Length `RandomAccessFile`**:
  - If searched by sequential record index: **$O(1)$ Direct Seek Time** via `raf.seek(index * RECORD_SIZE)`.
  - If searched by `passId` on an unsorted file: Still $O(n)$ scan, but seeking is faster because no parsing is needed.
  - If kept sorted by `passId`: Supports **$O(\log n)$ Binary Search** directly on disk without loading into RAM!

### Space Wastage Analysis (Padding Penalty)
In variable-length delimited text:
- Average record length: $5 + 9 + 15 + 2 + 15 + 10 + 6 + 6\text{ delimiters} \approx \mathbf{68\text{ bytes}}$.
- For 8,000 passes: Total file size $\approx 8,000 \times 68 = \mathbf{544\text{ KB}}$.

In fixed-length record layout:
- `pass_id`: 10 bytes
- `reg_no`: 15 bytes
- `name`: 50 bytes (padded with spaces/nulls to accommodate longest possible name)
- `route_no`: 4 bytes
- `boarding_stop`: 40 bytes (padded)
- `validity_date`: 10 bytes
- `amount_paid`: 8 bytes
- **Total Fixed Record Size**: **137 bytes**.
- For 8,000 passes: Total file size $\approx 8,000 \times 137 = \mathbf{1,096\text{ KB}}$ (1.09 MB).

$$\text{Wasted Storage} = 1,096\text{ KB} - 544\text{ KB} = \mathbf{552\text{ KB}}\quad (\mathbf{50.4\%}\text{ wasted on padding!})$$

**Engineering Trade-off**: Fixed-length records trade away $50\%$ extra disk space in exchange for $O(1)$ random seek and in-place updates. Delimited text files minimize disk space and remain human-readable, but require full sequential scanning.

---

## A1.4 Execution Output Verification

```text
==================================================================
 TASK A1: Bus-Pass File Format Design and Error Containment Demo   
==================================================================

[STEP 1] Writing Valid Passes (Handling Comma in Name):
  Passes written successfully to text and binary stores.

[STEP 2] Injecting Damaged Line into Text File:
  Appended corrupted record: "P1099,CORRUPTED_ENTRY_MISSING_FIELDS"

[STEP 3] Testing Find Pass by ID:
  Search for 'P1001': [P1001] Reg:192565092 | Balaji, Thejas | Route 12 (Tambaram) | Valid:2026-12-31 | Rs.4500.00
  Search for 'P9999': NOT FOUND

[STEP 4] Testing List Passes on Route 12:
  Found 2 passes on Route 12:
    - [P1001] Reg:192565092 | Balaji, Thejas | Route 12 (Tambaram) | Valid:2026-12-31 | Rs.4500.00
    - [P1002] Reg:192565093 | Karthik Raja | Route 12 (Guindy) | Valid:2026-12-31 | Rs.4500.00

[STEP 5] Reading from Binary File Store (DataInputStream):
  Total Binary Passes Recovered: 3
    - [P1001] Reg:192565092 | Balaji, Thejas | Route 12 (Tambaram) | Valid:2026-12-31 | Rs.4500.00
    - [P1002] Reg:192565093 | Karthik Raja | Route 12 (Guindy) | Valid:2026-12-31 | Rs.4500.00
    - [P1003] Reg:192565094 | Ananya Sharma | Route 7 (Central Station) | Valid:2026-12-31 | Rs.3800.00

==================================================================
 Task A1 Design & Implementation Verified Successfully!           
==================================================================
```
