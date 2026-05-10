package edunova.connexion.tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utility class to generate background images for the login interface
 */
public class ImageGenerator {

    /**
     * Generates a professional education-themed background image for the login sidebar
     * Creates a dark blue background with educational icons and particles
     * Dimensions: 450x660 (sidebar only)
     */
    public static void generateLoginSidebarBackground() {
        try {
            // Create image with sidebar dimensions (450x660)
            BufferedImage image = new BufferedImage(450, 660, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // Enable anti-aliasing for smooth rendering
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Create gradient from dark blue to lighter blue (like the image provided)
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(15, 30, 60),        // Dark blue (#0f1e3c)
                450, 660, new Color(30, 80, 140)    // Lighter blue (#1e508c)
            );
            
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, 450, 660);
            
            // Add subtle pattern/texture with particles
            addParticles(g2d, 450, 660);
            
            // Add educational icons
            addEducationIcons(g2d, 450, 660);
            
            g2d.dispose();
            
            // Save the image
            File outputDir = new File("src/main/resources/images");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            File outputFile = new File(outputDir, "login_sidebar_bg.jpg");
            ImageIO.write(image, "jpg", outputFile);
            
            System.out.println("✅ Background image generated: " + outputFile.getAbsolutePath());
            
        } catch (Exception e) {
            System.err.println("❌ Error generating background image: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Adds subtle particles/bokeh effect to the background
     */
    private static void addParticles(Graphics2D g2d, int width, int height) {
        // Add glowing particles
        g2d.setColor(new Color(100, 150, 255, 30)); // Semi-transparent light blue
        
        // Random particles
        java.util.Random rand = new java.util.Random(42); // Fixed seed for consistency
        for (int i = 0; i < 40; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            int size = rand.nextInt(15) + 5;
            g2d.fillOval(x, y, size, size);
        }
        
        // Add some larger glowing circles
        g2d.setColor(new Color(100, 180, 255, 20));
        for (int i = 0; i < 15; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            int size = rand.nextInt(40) + 30;
            g2d.fillOval(x - size/2, y - size/2, size, size);
        }
    }
    
    /**
     * Adds subtle education-related icons to the background
     */
    private static void addEducationIcons(Graphics2D g2d, int width, int height) {
        g2d.setColor(new Color(150, 200, 255, 40)); // Semi-transparent light blue
        g2d.setStroke(new BasicStroke(1.5f));
        
        // Draw some simple geometric shapes representing education
        // Book icon
        g2d.drawRect(50, 150, 30, 40);
        g2d.drawLine(65, 150, 65, 190);
        
        // Graduation cap
        g2d.drawLine(200, 100, 220, 120);
        g2d.drawLine(220, 120, 240, 100);
        g2d.drawLine(220, 120, 220, 140);
        
        // Light bulb (idea)
        g2d.drawOval(100, 400, 20, 25);
        g2d.drawRect(105, 425, 10, 15);
        g2d.drawLine(100, 440, 120, 440);
    }
    
    /**
     * Main method to generate all background images
     */
    public static void main(String[] args) {
        generateLoginSidebarBackground();
    }
}
