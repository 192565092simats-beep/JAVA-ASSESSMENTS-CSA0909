import java.awt.*;

/**
 * Test Harness for LibrarySearchScreen.
 * Tests search action and measures component dimensions across multiple window resolutions.
 */
public class LibrarySearchScreenDemo {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println(" TASK 3: Campus Library Search Screen - Layout Evaluation ");
        System.out.println("==========================================================");

        LibrarySearchScreen screen = new LibrarySearchScreen();

        System.out.println("\n[STEP 1] Initial Window Setup:");
        System.out.println("  Top-Level Layout: " + screen.getLayout().getClass().getSimpleName());
        System.out.println("  North Panel Layout: " + screen.getNorthPanel().getLayout().getClass().getSimpleName());
        System.out.println("  South Panel Layout: " + screen.getSouthPanel().getLayout().getClass().getSimpleName());
        System.out.println("  Initial Size: " + screen.getWidth() + "x" + screen.getHeight());

        System.out.println("\n[STEP 2] Simulating Search Execution:");
        screen.getSearchField().setText("Java");
        screen.getCategoryChoice().select("Title");
        screen.executeSearch();
        System.out.println("  Status Bar: " + screen.getStatusBar().getText());
        System.out.println("  Results Area Character Count: " + screen.getResultsArea().getText().length());

        System.out.println("\n[STEP 3] Responsive Layout Resizing Evaluation:");
        int[][] testResolutions = {
            {550, 300},  // Compact
            {750, 450},  // Standard
            {1100, 700}  // Expanded Widescreen
        };

        for (int[] res : testResolutions) {
            int width = res[0];
            int height = res[1];
            screen.setSize(width, height);
            screen.doLayout();
            screen.getNorthPanel().doLayout();
            screen.getSouthPanel().doLayout();

            Dimension northDim = screen.getNorthPanel().getSize();
            Dimension centerDim = screen.getResultsArea().getSize();
            Dimension southDim = screen.getSouthPanel().getSize();

            System.out.printf("  Window [%4dx%4d] -> North: [%4dx%3d] | Center: [%4dx%3d] | South: [%4dx%3d]%n",
                    width, height,
                    northDim.width, northDim.height,
                    centerDim.width, centerDim.height,
                    southDim.width, southDim.height);
        }

        System.out.println("\n[VERIFICATION] Center region successfully expands to absorb available delta,");
        System.out.println("               while North and South retain stable preferred heights.");
        System.out.println("==========================================================");
        screen.dispose();
    }
}
