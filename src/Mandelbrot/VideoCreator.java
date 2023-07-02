package Mandelbrot;

import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;


public class VideoCreator {

    public static void createVideo(String videoName) {
        // the clock time of the next frame
        long nextFrameTime = 0;

        // video parameters
        final int videoStreamIndex = 0;
        final int videoStreamId = 0;

        final int framesPerSecond = 8;
        final long frameRate = 1000000 / framesPerSecond;

        final int width = 854;
        final int height = 480;


        try {
            final IMediaWriter writer = ToolFactory.makeWriter(Util.videoPath + "/" + videoName + ".mp4");

            writer.addListener(ToolFactory.makeViewer(
                    IMediaViewer.Mode.VIDEO_ONLY, true,
                    javax.swing.WindowConstants.EXIT_ON_CLOSE));

            writer.addVideoStream(videoStreamIndex, videoStreamId, width, height);

            File dir = new File(Util.imagePath.toString());
            File[] files = dir.listFiles((dir1, name) -> name.startsWith("image_") && name.endsWith(".jpeg"));

            // Sort the files by their number
            assert files != null;
            Arrays.sort(files, Comparator.comparingInt(f -> Integer.parseInt(f.getName().substring(6, f.getName().length() - 5))));

            // Iterate over the sorted files
            for (File file : files) {
                BufferedImage frame = ImageIO.read(file);
                System.out.println("[+] Adding frame " + file.getName());
                writer.encodeVideo(videoStreamIndex, frame, nextFrameTime, DEFAULT_TIME_UNIT);
                nextFrameTime += frameRate;
            }

            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sortFilesInDir(String directory) {
        File dir = new File(directory);
        File[] files = dir.listFiles((dir1, name) -> name.startsWith("image_") && name.endsWith(".jpeg"));

        // Sort the files by their number
        Arrays.sort(files, Comparator.comparingInt(f -> Integer.parseInt(f.getName().substring(6, f.getName().length() - 5))));

        // Iterate over the sorted files
        for (File file : files) {
            // Do something with the file
            System.out.println(file.getName());
        }
    }

    public static void main(String[] args) {
        createVideo("test");
    }

}