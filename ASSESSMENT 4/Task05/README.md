# Task 5: Same Screen, Different Event-Handling Styles - Currency Converter

**Course**: Programming in Java (CO4 - Event-Driven GUI Design)  
**Student Name**: Thejas Balaji  
**Register Number**: 192565092  
**Marks**: 7 Marks | **Bloom's Level**: K4 (Analyse)  

---

## 5.1 The Four Event-Handling Implementations

Java provides multiple idiomatic ways to wire event sources to event listeners:

### (a) Frame Class Implements `ActionListener` Directly
The outer `Frame` subclass declares `implements ActionListener`.
```java
public class ConverterFrame extends Frame implements ActionListener {
    public ConverterFrame() {
        convertButton.addActionListener(this); // Passes 'this'
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == convertButton) {
            convert();
        }
    }
}
```

### (b) Named Member Inner Class
A separate, explicitly named class is defined inside the outer `Frame`.
```java
public class ConverterFrame extends Frame {
    private class ConvertActionHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            convert(); // Directly accesses outer class methods and fields
        }
    }
    public ConverterFrame() {
        ConvertActionHandler handler = new ConvertActionHandler();
        convertButton.addActionListener(handler);
    }
}
```

### (c) Anonymous Inner Class
An unnamed class implementing `ActionListener` is instantiated inline.
```java
convertButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        convert();
    }
});
```

### (d) Lambda Expression / Method Reference (Java 8+)
Since `ActionListener` is a **Functional Interface** containing exactly one abstract method (`void actionPerformed(ActionEvent e)`), it can be expressed as a lambda or method reference:
```java
// Lambda Expression:
convertButton.addActionListener(e -> convert());

// Method Reference:
convertButton.addActionListener(this::onConvertClicked);
```

---

## 5.2 Code Changes Required for Requirement Modifications

### Modifications Requested:
1. **Enter Key Support**: Pressing `ENTER` in `amountField` must trigger the same conversion action.
2. **Popup Dialog**: Display result in `JOptionPane.showMessageDialog` in addition to updating `resultLabel`.

### Implementation Comparison Across Styles

| Style | Change for Enter Key on `amountField` | Change for Popup Dialog (`JOptionPane`) | Boilerplate Assessment |
|-------|--------------------------------------|-----------------------------------------|------------------------|
| **(a) Frame Implements `ActionListener`** | Add `amountField.addActionListener(this);`. In `actionPerformed(ActionEvent e)`, modify dispatch check: `if (e.getSource() == convertButton \|\| e.getSource() == amountField)`. | Add `JOptionPane.showMessageDialog(this, ...)` inside `actionPerformed()`. | **Moderate**: Mixes event dispatch checks with UI logic. Must check event sources explicitly. |
| **(b) Named Inner Class** | Simply register the existing instantiated handler instance: `amountField.addActionListener(handler);`. Zero extra dispatch logic needed. | Add `JOptionPane.showMessageDialog(Converter.this, ...)` inside `ConvertActionHandler.actionPerformed()`. | **Low**: Highly reusable instance; clean delegation. |
| **(c) Anonymous Inner Class** | Cannot reuse inline anonymous listener directly unless extracted into a local reference: `ActionListener listener = new ActionListener() { ... };` then register on both. | Add `JOptionPane.showMessageDialog(Converter.this, ...)` inside inline method body. | **Moderate**: Duplicates code if anonymous block is copy-pasted; requires local variable refactoring. |
| **(d) Lambda Expression** *(Cleanest)* | Register the same method reference or lambda: `amountField.addActionListener(e -> convert());` or `amountField.addActionListener(this::onEnterOrClick);`. | Place `JOptionPane.showMessageDialog(this, ...)` directly inside the standalone `convert()` helper method. | **Minimal**: Exactly 1 extra line of declarative registration; $100\%$ zero duplication. |

---

## 5.3 Comparison Matrix & Production Recommendation

| Evaluation Metric | (a) Frame as Listener | (b) Named Inner Class | (c) Anonymous Inner Class | (d) Lambda Expression *(Recommended)* |
|-------------------|----------------------|-----------------------|---------------------------|---------------------------------------|
| **Readability & Verbosity** | **Poor**: High boilerplate; pollutes Frame class definition with `implements ActionListener`. | **Moderate**: Readable but verbose (6–10 lines for class definition). | **Fair**: Moderate boilerplate (5 lines of ceremony for 1 line of logic). | **Excellent**: Pure intent in 1 concise line (`e -> convert()`). |
| **Access to Enclosing Fields** | Direct access via `this` (since it IS the frame). | Direct access to outer frame fields via enclosing scope (`OuterClass.this`). | Direct access to outer frame fields; local variables must be `final` or effectively final. | Direct access to enclosing instance methods and fields. |
| **Handler Reusability** | Can be reused by other components in the same frame via `this`, but requires messy `e.getSource()` `if-else` branching. | **High**: One named handler instance can be assigned to multiple buttons, menu items, or shortcut keys. | **Low**: Local to single component unless assigned to reference variable. | **High**: The delegate business method (`convert()`) is completely decoupled and callable from anywhere. |
| **Generated Bytecode Overhead** | 0 extra `.class` files generated. | Generates `Outer$ConvertActionHandler.class`. | Generates `Outer$1.class` for every listener block. | Uses `invokedynamic` (Java 8+). Does not generate separate disk `.class` files, saving JAR size. |

### Preferred Style for Production Enterprise Applications (20+ Screens)
> **Recommendation**: **Style (d) Lambda Expressions combined with dedicated Business Methods (or Swing `Action` objects)** is overwhelmingly preferred.  
> **Justification**:
> 1. In enterprise applications with dozens of screens and hundreds of interactive widgets, implementing `ActionListener` on the Frame creates a massive, error-prone `if-else` dispatch spaghetti ("God Listener" anti-pattern).
> 2. Anonymous inner classes produce massive boilerplate and generate dozens of small `.class` files.
> 3. Lambdas delegate cleanly to focused domain methods (`convert()`), achieve compile-time verification, drastically reduce lines of code, and maximize readability and testability.

---

## 5.4 Completed Program (`Converter.java`)

The provided starter template was completed by resolving **TODO 1** and **TODO 2**:

```java
// [TODO 1 Completed]: Wire events concisely using Lambdas
convertButton.addActionListener(e -> convert());
amountField.addActionListener(e -> convert()); // Enter key triggers conversion

// [TODO 2 Completed]: Safe parsing, business math, UI label update, and modal dialog
public boolean convert() {
    String input = amountField.getText().trim();
    if (input.isEmpty()) {
        resultLabel.setText("Error: Please enter an amount in INR.");
        showPopupMessage("Input Error", "Please enter an amount in INR.", JOptionPane.WARNING_MESSAGE);
        return false;
    }
    try {
        double inr = Double.parseDouble(input);
        if (inr < 0) {
            resultLabel.setText("Error: Amount cannot be negative.");
            showPopupMessage("Validation Error", "Amount cannot be negative.", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        double usd = inr * INR_TO_USD_RATE;
        String formattedResult = String.format("₹ %,.2f INR = $ %,.2f USD (@ rate %.3f)", inr, usd, INR_TO_USD_RATE);
        resultLabel.setText(formattedResult);
        showPopupMessage("Conversion Result", formattedResult, JOptionPane.INFORMATION_MESSAGE);
        return true;
    } catch (NumberFormatException ex) {
        String err = "Invalid input \"" + input + "\". Please enter digits only.";
        resultLabel.setText("Error: " + err);
        showPopupMessage("Number Format Error", err, JOptionPane.ERROR_MESSAGE);
        return false;
    }
}
```

---

## 5.5 Alternative Concept: Method References & Swing `Action` (`AbstractAction`)

### 1. Method References (`this::convert`)
Instead of `e -> convert()`, Java enables method references when a method signature matches the functional interface:
```java
convertButton.addActionListener(this::onConvertAction);
```
This is even more concise than a lambda and avoids generating synthetic closure wrapper methods.

### 2. The Swing `Action` Architecture (`javax.swing.Action` & `AbstractAction`)
In large desktop software, an operation (e.g. "Convert Currency" or "Save File") can be invoked from:
1. A toolbar button (`JButton`)
2. A top menu item (`JMenuItem`)
3. A keyboard accelerator (e.g. `Ctrl + S` or Enter)

Instead of registering separate listeners and keeping labels/icons in sync, Swing provides `AbstractAction`:
```java
Action convertAction = new AbstractAction("Convert to USD") {
    @Override
    public void actionPerformed(ActionEvent e) {
        convert();
    }
};

// Bind to multiple controls effortlessly:
convertButton.setAction(convertAction);
menuItem.setAction(convertAction);
// Automatically shares label, icon, enabled state, tooltip, and execution logic!
```
When `convertAction.setEnabled(false)` is invoked, both the button and menu item disable synchronously. This represents the gold standard for GUI state synchronization.

---

## 5.6 Compilation and Execution

### Compilation Command
```bash
javac -d bin src/*.java
```

### Execution Command
```bash
java -cp bin ConverterStylesDemo
```

### Execution Output
```text
==========================================================
 TASK 5: Currency Converter - 4 Event-Handling Styles     
==========================================================

[STYLE 1] Frame Implements ActionListener Directly:
  Button Click Result: SUCCESS: ₹ 8,350.00 INR = $ 100.20 USD

[STYLE 2] Named Member Inner Class (ConvertActionHandler):
  Enter Key Result:    SUCCESS: ₹ 16,700.00 INR = $ 200.40 USD

[STYLE 3] Anonymous Inner Class:
  Button Click Result: SUCCESS: ₹ 25,000.00 INR = $ 300.00 USD

[STYLE 4] Lambda Expression & Method Reference (Modern Java):
  Lambda Result:       SUCCESS: ₹ 50,000.00 INR = $ 600.00 USD

[VERIFICATION] Testing Completed Converter.java Program:
  [POPUP DIALOG] [Conversion Result]: ₹ 100,000.00 INR = $ 1,200.00 USD (@ rate 0.012)
  Conversion Status:   SUCCESS
  Result Label:        ₹ 100,000.00 INR = $ 1,200.00 USD (@ rate 0.012)
  [POPUP DIALOG] [Number Format Error]: Invalid input "InvalidNumber". Please enter digits only.
  Invalid Test Status: HANDLED_ERROR
  Result Label:        Error: Invalid input "InvalidNumber". Please enter digits only.

==========================================================
 Task 5 All 4 Styles and Converter Completed Successfully!
==========================================================
```
