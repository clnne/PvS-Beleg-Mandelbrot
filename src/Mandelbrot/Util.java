package Mandelbrot;

import io.github.cdimascio.dotenv.Dotenv;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Util {
    public static Path currentPath = Paths.get("");
    public static Path recordingPath = currentPath.resolve("src/Aufnahmen");
    public static Path videoPath = currentPath.resolve(recordingPath + "/Videos");
    public static Path imagePath = currentPath.resolve(recordingPath + "/Bilder");

    public static int RESOLUTION_WIDTH = (1920);
    public static int RESOLUTION_HEIGHT = (1080) - 80; // - 80px für die Taskbar

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

}
