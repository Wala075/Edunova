package edunova.connexion.tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utility class to resize images
 */
public class ImageResizer {

    /**
     * Resize an image to specified dimensions
     */
    public static void resizeImage(String inputPath, String outputPath, int targetWidth, int targetHeight) {
        try {
            System.out.println("📦 Opening image: " + inputPath);
            
            // Read the image
            BufferedImage originalImage = ImageIO.read(new File(inputPath));
            
            if (originalImage == null) {
                System.err.println("❌ Could not read image: " + inputPath);
                return;
            }
            
            System.out.println("📐 Original size: " + originalImage.getWidth() + "x" + originalImage.getHeight());
            System.out.println("🔄 Resizing to: " + targetWidth + "x" + targetHeight);
            
            // Create resized image
            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            
            // Enable high-quality rendering
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Draw scaled image
            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();
            
            // Ensure output directory exists
            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();
            
            // Write the resized image
            ImageIO.write(resizedImage, "jpg", outputFile);
            
            System.out.println("✅ Image resized and saved: " + outputPath);
            System.out.println("   New size: " + targetWidth + "x" + targetHeight);
            
        } catch (Exception e) {
            System.err.println("❌ Error resizing image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Main method to resize the child image
     */
    public static void main(String[] args) {
        String inputPath = System.getProperty("user.home") + "/Downloads/child.jpg";
        String outputPath = "src/main/resources/images/login_sidebar_bg.jpg";
        
        System.out.println("=" + "=".repeat(58) + "=");
        System.out.println("🖼️  Resize Child Image (450x660)");
        System.out.println("=" + "=".repeat(58) + "=");
        System.out.println();
        
        resizeImage(inputPath, outputPath, 450, 660);
    }
}
