package Mandelbrot;

import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class VideoCreator {

    public static void createVideo(String videoName) {
        // the clock time of the next frame
        long nextFrameTime = 0;

        // video parameters
        final int videoStreamIndex = 0;
        final int videoStreamId = 0;
        final long frameRate = DEFAULT_TIME_UNIT.convert(500, MILLISECONDS);
        final int width = 320;
        final int height = 200;


        try {
            final IMediaWriter writer = ToolFactory.makeWriter(Util.videoPath + "/" + videoName + ".mp4");

            writer.addListener(ToolFactory.makeViewer(
                    IMediaViewer.Mode.VIDEO_ONLY, true,
                    javax.swing.WindowConstants.EXIT_ON_CLOSE));

            writer.addVideoStream(videoStreamIndex, videoStreamId, width, height);

            File dir = new File(Util.imagePath.toString());
            for (File f : Objects.requireNonNull(dir.listFiles())) {
                BufferedImage frame = ImageIO.read(f);
                writer.encodeVideo(videoStreamIndex, frame, nextFrameTime, DEFAULT_TIME_UNIT);
                nextFrameTime += frameRate;
            }
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}