package Mandelbrot;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Util {
    public static Path currentPath = Paths.get("");
    public static Path recordingPath = currentPath.resolve("src/Aufnahmen");
    public static Path videoPath = currentPath.resolve(recordingPath + "/Videos");
    public static Path imagePath = currentPath.resolve(recordingPath + "/Bilder");

    public static int RESOLUTION_WIDTH = (1280);
    public static int RESOLUTION_HEIGHT = (720);

    public static void createDirectory(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } catch (IOException e) {
            System.out.println("[-] Failed to create directory " + path + ".");
        }
    }

    public static void saveImage(BufferedImage image, int imageNumber) {
        try {
            ImageIO.write(image, "png", Util.imagePath.resolve("image_" + imageNumber + ".jpeg").toFile());
        } catch (IOException e) {
            System.out.println("[-] Failed to save image.");
        }
    }

    public static String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
    }

}
