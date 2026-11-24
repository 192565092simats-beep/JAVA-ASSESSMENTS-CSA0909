# Task 4: Event-Handling Forensics - Drawing Pad

**Course**: Programming in Java (CO4 - Event-Driven GUI Design)  
**Student Name**: Thejas Balaji  
**Register Number**: 192565092  
**Marks**: 7 Marks | **Bloom's Level**: K4 (Analyse)  

---

## 4.1 Forensic Defect 1: Method Capitalization Typo

### Defect Location
```java
// Original Faulty Line:
public void mouseclicked(MouseEvent e) { ... }
```

### Why the Runtime Event Failed
The `java.awt.event.MouseListener` interface defines:
```java
public void mouseClicked(java.awt.event.MouseEvent e); // Note camelCase uppercase 'C'
```
When extending `java.awt.event.MouseAdapter`, the adapter class provides default empty implementations for all methods of `MouseListener`, `MouseWheelListener`, and `MouseMotionListener`:
```java
public class MouseAdapter implements MouseListener, ... {
    public void mouseClicked(MouseEvent e) {} // default empty body
    public void mousePressed(MouseEvent e) {}
    ...
}
```

### Why the Java Compiler Did Not Catch It
In Java, method names are case-sensitive. The compiler saw `public void mouseclicked(MouseEvent e)` as a **completely new, valid helper method** declared on the anonymous class. Because `MouseAdapter` already supplied the required `mouseClicked(MouseEvent e)` method, the class satisfied the interface contract. 

At runtime, when the user clicked the mouse, the AWT Event Dispatcher invoked `mouseClicked(MouseEvent e)`. This invoked the adapter's empty default implementation. The developer's `mouseclicked(MouseEvent e)` was never called!

---

## 4.2 Forensic Defect 2: Transient `getGraphics()` vs AWT Painting Subsystem

### Defect Location
```java
// Original Faulty Line:
Graphics g = getGraphics();
g.fillOval(x, y, 6, 6);
```

### The Root Cause: AWT Painting Model
AWT uses a **callback-driven, retained-model painting architecture**:
1. When an AWT window is covered, minimized, restored, moved, or resized, the underlying Operating System windowing system invalidates the rectangular region (damage/expose event).
2. The OS sends a paint message to the JVM Event Dispatch Thread (EDT).
3. The EDT clears the dirty area with the background color and calls `paint(Graphics g)`.
4. Calling `getGraphics()` obtains a temporary, non-retained graphics context directly to the screen surface. Anything drawn via `getGraphics()` exists **only in video memory until the next repaint event**. The moment the window is minimized or covered, the OS wipes the pixels clean.
5. Because the original developer stored only the latest $(x, y)$ coordinate in two primitive variables and never overrode `paint(Graphics g)`, all previously drawn circles were permanently lost.

### Correct AWT Painting Approach
- Store drawing model state (e.g., `List<Point> points`) in persistent memory.
- In event listeners, simply append new shapes to the list and invoke `repaint()`.
- `repaint()` posts an asynchronous `PaintEvent` to the system event queue.
- Override `paint(Graphics g)` to iterate through the data model and redraw every shape onto the provided `Graphics` context.

---

## 4.3 Completed Code (`DrawPad.java`)

```java
// TODO 1 Completed in MouseAdapter:
@Override
public void mouseClicked(MouseEvent e) {
    points.add(new Point(e.getX(), e.getY()));
    status.setText("Last click: (" + e.getX() + ", " + e.getY() + ") | Total Dots: " + points.size());
    repaint(); // Invalidate window and request redraw from AWT subsystem
}

// TODO 2 Completed in DrawPad Frame:
@Override
public void paint(Graphics g) {
    super.paint(g);
    g.setColor(new Color(220, 20, 60)); // Crimson Red
    synchronized (points) {
        for (Point p : points) {
            // Centering a circle of diameter 6 at click point (p.x, p.y)
            g.fillOval(p.x - 3, p.y - 3, 6, 6);
        }
    }
}
```

---

## 4.4 Prevention via the `@Override` Annotation

If the developer had prefixed the listener method with `@Override`:
```java
addMouseListener(new MouseAdapter() {
    @Override
    public void mouseclicked(MouseEvent e) { ... } // Compiler checks supertype hierarchy!
});
```
The Java compiler would have immediately issued a **compile-time error**:
```text
DrawPad.java:13: error: method does not override or implement a method from a supertype
    @Override
    ^
1 error
```
The `@Override` annotation instructs the compiler to verify that a method signature strictly matches a declared method in a superclass or interface. This completely eliminates silent method name typos, incorrect parameter counts, or mismatched casing.

---

## 4.5 Alternative Concept: Points Collection vs Off-Screen `BufferedImage`

### Architectural Comparison

| Dimension | Option A: `List<Point>` + Redraw in `paint(g)` | Option B: Off-Screen `BufferedImage` Blitting |
|-----------|------------------------------------------------|-----------------------------------------------|
| **Repaint Time Complexity** | **$O(N)$ Linear Time**: On every expose/repaint event, iterates through all $N$ points and issues $N$ draw commands. | **$O(1)$ Constant Time**: The image is blitted in one single hardware call: `g.drawImage(canvasImage, 0, 0, this)`. |
| **Click Time Complexity** | **$O(1)$ Constant Time**: `points.add(new Point(x, y))` + `repaint()`. | **$O(1)$ Constant Time**: Draw circle onto image buffer `imgGraphics.fillOval(...)` + `repaint()`. |
| **Memory Footprint** | **Dynamic $O(N)$**: Scales linearly with number of points. Each `Point` object in HotSpot JVM costs 24 bytes + 8 bytes reference in list = 32 bytes/point. | **Fixed $O(W \times H)$**: Constant memory regardless of how many dots are drawn. For a $400 \times 400$ ARGB canvas: $400 \times 400 \times 4\text{ bytes} \approx 640\text{ KB}$. |
| **Undo / Scaling Flexibility** | **Very High**: Easy to undo last dot (`points.remove(points.size() - 1)`), change dot colors retroactively, or scale coordinates. | **Low**: Pixel data is baked/flattened into raster bitmap. Undo requires maintaining a history stack of full images or damage diffs. |

### Scalability Analysis Across Data Scales

| Number of Points ($N$) | Option A: `List<Point>` Memory & Performance | Option B: `BufferedImage` Memory & Performance | Superior Architecture |
|------------------------|----------------------------------------------|------------------------------------------------|-----------------------|
| **$N = 100$ Points** | Memory: $\approx 3.2\text{ KB}$.<br>Repaint: $< 0.1\text{ ms}$ (instantaneous). | Memory: $\approx 640\text{ KB}$.<br>Repaint: $< 0.1\text{ ms}$. | **Option A (`List<Point>`)**: Negligible memory footprint and trivially fast redraw. |
| **$N = 10,000$ Points** | Memory: $\approx 320\text{ KB}$.<br>Repaint: $\approx 5\text{ ms} - 10\text{ ms}$ (smooth 60 FPS). | Memory: $\approx 640\text{ KB}$ (unchanged).<br>Repaint: $< 0.5\text{ ms}$ bit-blit. | **Option B (`BufferedImage`)**: Blitting is faster, though Option A is still viable. |
| **$N = 1,000,000$ Points** | Memory: $\approx 32\text{ MB}$.<br>Repaint: $\approx 400\text{ ms} - 1000\text{ ms}$ (severe UI freeze and stuttering on every window move). | Memory: $\approx 640\text{ KB}$ (completely unchanged!).<br>Repaint: $< 1\text{ ms}$ constant bit-blit. | **Option B (`BufferedImage`) overwhelmingly superior**: Prevents UI frame drops and bounds memory usage to canvas dimensions. |

---

## 4.6 Compilation and Execution

### Compilation Command
```bash
javac -d bin src/*.java
```

### Execution Command
```bash
java -cp bin DrawPadForensicsDemo
```

### Execution Output
```text
==========================================================
 TASK 4: Event-Handling Forensics - Drawing Pad           
==========================================================

[FORENSIC DIAGNOSIS 1] Defect: Method Name Capitalization
  Original Code: public void mouseclicked(MouseEvent e)
  Interface Spec: public void mouseClicked(MouseEvent e) [in MouseListener]
  Compiler Consequence: MouseAdapter supplies an empty mouseClicked(MouseEvent e).
  Because 'mouseclicked' has a lowercase 'c', the compiler treated it as an ordinary
  new helper method. At runtime, the AWT Event Dispatcher invoked MouseAdapter's
  default empty mouseClicked(), completely ignoring the developer's method!

[FORENSIC DIAGNOSIS 2] Defect: Transient getGraphics() vs paint(Graphics g)
  Original Code: Graphics g = getGraphics(); g.fillOval(x, y, 6, 6);
  AWT Painting Principle: getGraphics() draws directly to the screen pixel surface.
  AWT does NOT retain or buffer pixels. When the OS minimizes, restores, resizes,
  or overlaps a window, the native windowing system invalidates the viewport and
  calls paint(Graphics g). If shapes are not stored in memory and drawn in paint(),
  all pixels are wiped completely clean on every repaint cycle.

[STEP 1] Testing Repaired DrawPad with Clicks Simulation:
  Simulated Click at (50, 80) -> Status: Last click: (50, 80) | Total Dots: 1
  Simulated Click at (120, 150) -> Status: Last click: (120, 150) | Total Dots: 2
  Simulated Click at (200, 220) -> Status: Last click: (200, 220) | Total Dots: 3
  Simulated Click at (310, 105) -> Status: Last click: (310, 105) | Total Dots: 4

[STEP 2] Verifying Model State Persistence:
  Total points stored in memory: 4
    Dot #1: Center=(50, 80), Render Bound=(47, 77, 6, 6)
    Dot #2: Center=(120, 150), Render Bound=(117, 147, 6, 6)
    Dot #3: Center=(200, 220), Render Bound=(197, 217, 6, 6)
    Dot #4: Center=(310, 105), Render Bound=(307, 102, 6, 6)

[STEP 3] Simulating Window Minimize / Restore Repaint Cycle:
  Repaint cycle executed successfully on off-screen graphics buffer.
  All 4 dots preserved and redrawn without loss!

==========================================================
 Task 4 Forensic Defects Repaired and Verified!           
==========================================================
```
