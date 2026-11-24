import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Task 4: Event-Handling Forensics - Drawing Pad
 * Repaired and completed version addressing:
 * 1. mouseClicked capitalization fix with @Override
 * 2. Persistent painting subsystem using ArrayList<Point> and paint(Graphics g)
 */
public class DrawPad extends Frame {

    private final Label status = new Label("Click anywhere to draw");
    private final List<Point> points = new ArrayList<>();

    public DrawPad() {
        super("Forensics Repaired - Persistent Drawing Pad");
        setSize(400, 400);
        setLayout(new BorderLayout());
        add(status, BorderLayout.SOUTH);

        // Event Listener Registration with @Override to prevent typo defects
        addMouseListener(new MouseAdapter() {
            /**
             * [TODO 1 Completed]:
             * Correctly override mouseClicked(MouseEvent e) in camelCase.
             * 1. Capture click coordinates and append to persistent points list.
             * 2. Update status label with coordinate and count feedback.
             * 3. Request framework repaint via repaint() rather than transient getGraphics().
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                points.add(new Point(e.getX(), e.getY()));
                status.setText("Last click: (" + e.getX() + ", " + e.getY() + ") | Total Dots: " + points.size());
                repaint();
            }
        });

        // Window closing adapter
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    /**
     * [TODO 2 Completed]:
     * Override paint(Graphics g) to render all recorded dots on every system-initiated
     * or programmatic repaint cycle (including minimize, restore, resize, and uncover).
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // Render each recorded point as a 6x6 filled circle centered on (x, y)
        g.setColor(new Color(220, 20, 60)); // Crimson Red
        synchronized (points) {
            for (Point p : points) {
                // Centering circle of diameter 6 at (p.x, p.y): top-left is (p.x - 3, p.y - 3)
                g.fillOval(p.x - 3, p.y - 3, 6, 6);
            }
        }
    }

    /**
     * Programmatic click simulator for headless test harness.
     */
    public void simulateClick(int x, int y) {
        MouseEvent me = new MouseEvent(
                this, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
                0, x, y, 1, false, MouseEvent.BUTTON1
        );
        for (MouseListener ml : getMouseListeners()) {
            ml.mouseClicked(me);
        }
    }

    public List<Point> getPoints() {
        return points;
    }

    public Label getStatus() {
        return status;
    }
}
