# Task A4: What Should Be Serialized? – Object Persistence & Schema Evolution

**Course**: Programming in Java (CO5: Data Persistence Engineering)  
**Student Name**: Thejas Balaji  
**Register Number**: 192565092  
**Marks**: 4 Marks | **Bloom's Level**: K4 (Analyse)  

---

## A4.1 Decide What to Persist

A hostel management application persists its domain hierarchy consisting of a root `Hostel` entity containing a `List<Room>`, where each `Room` contains a `List<Resident>`. Each `Resident` holds:
- `name` (`String`)
- `regNo` (`String`)
- `password` (`String`)
- `dbConnection` (`java.sql.Connection`)

### 1. Transient Field Decisions & Justifications

| Field Name | Type | Persistence Decision | Technical Rationale |
|---|---|:---:|---|
| `dbConnection` | `java.sql.Connection` | **MUST BE `transient`** | **Non-Serializable System Resource**: A database connection is not a static data structure; it represents an active TCP socket, an operating system file descriptor, and an ephemeral authentication state on a remote server. The interface `java.sql.Connection` does NOT implement `java.io.Serializable`. If this field is not marked `transient`, calling `oos.writeObject(hostel)` immediately throws `java.io.NotSerializableException: com.mysql.cj.jdbc.ConnectionImpl` and completely aborts serialization. |
| `password` | `java.lang.String` | **MUST BE `transient`** | **Security & Credential Protection**: Passwords represent sensitive authentication secrets. Persisting raw or hashed passwords directly into serialized binary files exposes them to unauthorized hex editing, file-system leakage, or disk theft. Authentication credentials must be verified dynamically at login, never baked into state dumps. |
| `name`, `regNo` | `java.lang.String` | **Persisted** | Core state attributes defining resident identity. `String` natively implements `Serializable`. |

### 2. Object Graph Serialization Mechanism
When `ObjectOutputStream.writeObject(hostel)` is invoked:
1. **Recursive Graph Traversal**: Java uses a Depth-First Search (DFS) algorithm to navigate the object reference graph:
   $$\text{Hostel} \longrightarrow \text{List}\langle\text{Room}\rangle \longrightarrow \text{Room} \longrightarrow \text{List}\langle\text{Resident}\rangle \longrightarrow \text{Resident}$$
2. **Metadata & Header Serialization**: For each encountered class, the stream writes a class descriptor (`ObjectStreamClass`), including class name, serial version UID, and list of persistent field types.
3. **Handle Table & Cycle Prevention**: To resolve circular references (e.g. if a `Resident` maintains a back-reference `getRoom()`), the stream assigns a unique 32-bit integer handle to each serialized object. If an object is referenced multiple times, subsequent references write only the 4-byte handle rather than re-serializing the object, guaranteeing reference identity (`==`) and preventing infinite recursive loops.
4. **Transient Field Omission**: The stream skips fields marked `transient`. Upon deserialization (`ois.readObject()`), JVM memory initializes transient reference fields to `null` and primitives to `0` or `false`.

---

## A4.2 Plan for Change – Schema Evolution and `serialVersionUID`

Suppose a new field `private String phoneNumber;` is added to the `Resident` class after thousands of existing student records have already been serialized and archived to disk.

### 1. Scenario A: `serialVersionUID` is NOT Explicitly Declared
- If the developer does not explicitly declare `private static final long serialVersionUID`, the Java compiler automatically synthesizes a 64-bit cryptographic hash (SHA-1) computed from class name, implemented interfaces, fields, methods, and access modifiers.
- When `phoneNumber` is added, the class structure changes, altering the synthesized hash.
- **Runtime Consequence**: When reading an old archive file, `ObjectInputStream` discovers that the UID in the stream (`UID_old`) does not match the newly compiled runtime class (`UID_new`). Deserialization crashes immediately with:
  ```text
  java.io.InvalidClassException: Resident; local class incompatible:
    stream classdesc serialVersionUID = 7293849102938401923,
    local class serialVersionUID = 8491029481029384912
  ```
  All historical hostel archive files become completely unreadable!

### 2. Scenario B: `serialVersionUID` IS Explicitly Declared
```java
public class Resident implements Serializable {
    private static final long serialVersionUID = 3001L; // Explicit constant!
    private final String name;
    private final String regNo;
    private String phoneNumber; // Newly added field
}
```
- Because both versions share the exact same `serialVersionUID = 3001L`, the JVM treats them as compatible versions of the same class.
- **Deserialization Behavior**:
  1. The stream loads existing fields (`name`, `regNo`).
  2. The newly introduced field `phoneNumber` (which does not exist in the old stream) is automatically initialized to its type-default value (`null`).
  3. No exceptions are thrown, and legacy archives load seamlessly.

---

## Alternative Concept & Complexity Analysis: Java Binary Serialization vs JSON / CSV

### Comparison Matrix

| Evaluation Dimension | Java Binary Serialization (`.ser`) | JSON Format (`.json` via Jackson/Gson) | CSV Format (`.csv`) |
|---|---|---|---|
| **Interoperability & Portability** | **Extremely Poor**: Proprietary Java binary protocol. Cannot be natively read or parsed by Python, Node.js, Go, or web browsers. | **Universal ($100\%$)**: Industry-standard text format natively supported by every modern programming language and database. | **High**: Universal, easily imported into Excel, R, Python, and SQL databases. |
| **Human Readability & Debugging** | **Zero**: Opaque binary byte stream. Cannot be inspected or edited with a text editor. | **Excellent**: Clear key-value structure (`{"name": "Thejas", "room": 101}`). | **High**: Plain comma-delimited tabular rows. |
| **File Storage Footprint** | Moderate: Includes verbose Java class descriptor metadata and signatures (~510 bytes for 2 residents). | Slightly larger for small records due to repeated key strings (`"name"`, `"regNo"`), but compresses well with GZIP. | **Smallest ($< 100\text{ bytes}$)**: Omits keys, storing raw delimited values only. |
| **Schema Evolution Flexibility** | **Fragile**: Requires strict management of `serialVersionUID`. Renaming fields or changing inheritance hierarchies easily breaks deserialization. | **Highly Resilient**: Parsers naturally ignore unknown keys or supply defaults for missing fields without runtime crashes. | Moderate: Adding columns at the end is backward compatible; adding columns in the middle breaks index order. |
| **Security Risk** | **Severe (Gadget Chain Vulnerability)**: Deserializing untrusted binary streams allows Remote Code Execution (RCE) via malicious object graphs. | **Safe**: Text data parser constructs plain objects without invoking arbitrary native deserialization gadgets. | **Safe**: Pure tabular string parsing. |

### Architectural Recommendation
Java binary serialization should be **deprecated for long-term data persistence**. For enterprise applications, **JSON (with Jackson) or relational databases (via JDBC)** should be used for structured multi-entity storage.

---

## A4.3 Execution Output Verification

```text
==================================================================
 TASK A4: Object Graph Serialization and Transient Design Demo    
==================================================================

[STEP 1] State Before Serialization (In-Memory Objects):
  Hostel: SIMATS Tagore International Hostel
  Resident 1: Resident[Name=Thejas Balaji, Reg=192565092, Password=SecretPass#2026, Connection=ActiveSocket[jdbc:mysql://10.0.0.12:3306/hostel_db, open=true], Phone=+91-9840123456]
  Resident 2: Resident[Name=Karthik Raja, Reg=192565093, Password=MyPassword@123, Connection=ActiveSocket[jdbc:mysql://10.0.0.12:3306/hostel_db, open=true], Phone=Unassigned]
  Active Live Connection Present: true

[STEP 2] Serializing Object Graph to File: hostel_state13301130523427129305.ser
  Serialization Succeeded! File size: 510 bytes
  (Live socket and password were safely bypassed without NotSerializableException)

[STEP 3] Deserializing Object Graph from Disk into Fresh JVM Memory:
  Restored Hostel Name: SIMATS Tagore International Hostel
  Total Rooms Reconstructed: 2
  Room #101 Residents:
    -> Resident[Name=Thejas Balaji, Reg=192565092, Password=<null/omitted>, Connection=<null/disconnected>, Phone=+91-9840123456]
  Room #102 Residents:
    -> Resident[Name=Karthik Raja, Reg=192565093, Password=<null/omitted>, Connection=<null/disconnected>, Phone=Unassigned]

[STEP 4] Verifying Security and Transient Field Protection:
  Password restored as:   null (Must be null)
  Connection restored as: null (Must be null)
  Phone number preserved: +91-9840123456

==================================================================
 Task A4 Object Graph Serialization Verified Successfully!        
==================================================================
```
