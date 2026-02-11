package backend;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class InvisibleCloakEngine implements Runnable {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private volatile boolean running = false;
    private JLabel videoLabel;

    public InvisibleCloakEngine(JLabel videoLabel) {
        this.videoLabel = videoLabel;
    }

    public void startCamera() {
        running = true;
        new Thread(this).start();
    }

    public void stopCamera() {
        running = false;
    }

    @Override
    public void run() {

        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) return;

        Mat frame = new Mat();
        Mat background = new Mat();

        // ---------- CAPTURE CLEAN BACKGROUND ----------
        try { Thread.sleep(3000); } catch (Exception ignored) {}

        for (int i = 0; i < 30; i++) {
            camera.read(background);
            try { Thread.sleep(30); } catch (Exception ignored) {}
        }

        while (running) {
            camera.read(frame);
            if (frame.empty()) continue;

            // --------- HSV CONVERSION (BGR → HSV) ---------
            Mat hsv = new Mat();
            Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);

            // --------- RED COLOR DETECTION (2 RANGES) ---------
            Mat mask1 = new Mat();
            Mat mask2 = new Mat();
            Mat mask = new Mat();

            Core.inRange(hsv,
                    new Scalar(0, 120, 70),
                    new Scalar(10, 255, 255),
                    mask1);

            Core.inRange(hsv,
                    new Scalar(170, 120, 70),
                    new Scalar(180, 255, 255),
                    mask2);

            Core.add(mask1, mask2, mask);

            // --------- NOISE REMOVAL + SMOOTHING ---------
            Mat kernel = Imgproc.getStructuringElement(
                    Imgproc.MORPH_ELLIPSE, new Size(5, 5));

            Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);
            Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_DILATE, kernel);
            Imgproc.GaussianBlur(mask, mask, new Size(7, 7), 0);

            // --------- INVERT MASK ---------
            Mat maskInv = new Mat();
            Core.bitwise_not(mask, maskInv);

            // --------- CLOAK FROM BACKGROUND ---------
            Mat cloakArea = new Mat();
            Core.bitwise_and(background, background, cloakArea, mask);

            // --------- VISIBLE AREA ---------
            Mat visibleArea = new Mat();
            Core.bitwise_and(frame, frame, visibleArea, maskInv);

            // --------- FINAL FRAME (BGR) ---------
            Mat finalFrame = new Mat();
            Core.add(cloakArea, visibleArea, finalFrame);

            // BGR → RGB BEFORE DISPLAY
            Imgproc.cvtColor(finalFrame, finalFrame, Imgproc.COLOR_BGR2RGB);

            // Show in Swing
            ImageIcon icon = new ImageIcon(matToBufferedImage(finalFrame));
            videoLabel.setIcon(icon);
        }

        camera.release();
    }

    // ---------- MAT → BUFFEREDIMAGE ----------
    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        byte[] data = new byte[mat.rows() * mat.cols() * mat.channels()];
        mat.get(0, 0, data);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);
        return image;
    }
}
