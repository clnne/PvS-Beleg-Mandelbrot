package Mandelbrot;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;


public class VideoCreator {

    public static void createVideo(String videoName, int width, int height) {
        // the clock time of the next frame
        long nextFrameTime = 0;

        // video parameters
        final int videoStreamIndex = 0;
        final int videoStreamId = 0;

        final int framesPerSecond = 20;
        final long frameRate = 1000000 / framesPerSecond;

        System.out.println("[+] Creating video " + videoName + ".mp4 ...");

        try {
            final IMediaWriter writer = ToolFactory.makeWriter(Util.videoPath + "/" + videoName + ".mp4");


            //writer.addListener(ToolFactory.makeViewer(
            //        IMediaViewer.Mode.VIDEO_ONLY, true,
            //        javax.swing.WindowConstants.EXIT_ON_CLOSE));

            writer.addVideoStream(videoStreamIndex, videoStreamId, width, height);

            File dir = new File(Util.imagePath.toString());
            File[] files = dir.listFiles((dir1, name) -> name.startsWith("image_") && name.endsWith(".jpeg"));

            // Sort the files by their number
            assert files != null;
            Arrays.sort(files, Comparator.comparingInt(f -> Integer.parseInt(f.getName().substring(6, f.getName().length() - 5))));

            // Iterate over the sorted files
            for (File file : files) {
                BufferedImage frame = ImageIO.read(file);
                //System.out.println("[/] Adding frame " + file.getName());
                writer.encodeVideo(videoStreamIndex, frame, nextFrameTime, DEFAULT_TIME_UNIT);
                nextFrameTime += frameRate;
            }

            writer.close();

            System.out.println("[+] Video " + videoName + ".mp4 has been created with " + files.length + " frames.");
            System.out.println("[?] The video can be found in " + Util.videoPath + "/");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sortFilesInDir(String directory) {
        File dir = new File(directory);
        File[] files = dir.listFiles((dir1, name) -> name.startsWith("image_") && name.endsWith(".jpeg"));

        // Sort the files by their number
        Arrays.sort(files, Comparator.comparingInt(f -> Integer.parseInt(f.getName().substring(6, f.getName().length() - 5))));
        for (File file : files) {
            System.out.println(file.getName());
        }
    }

    public static void main(String[] args) {
        createVideo(Util.getTimestamp(), 1920, 1080);
    }

}