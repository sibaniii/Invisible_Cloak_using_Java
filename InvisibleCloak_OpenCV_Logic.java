package invisiblecloak;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;

public class InvisibleCloak {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws InterruptedException {
        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            System.out.println("Error: Cannot open camera");
            return;
        }

        Mat frame = new Mat();
        Mat background = new Mat();
        System.out.println("Capturing background, please stay out of frame for 3 seconds...");

        // Capture background (static)
        Thread.sleep(3000); // 3 seconds
        camera.read(background);
        Imgproc.cvtColor(background, background, Imgproc.COLOR_BGR2RGB); // convert to RGB

        System.out.println("Background captured. Start waving your cloak!");

        while (true) {
            if (!camera.read(frame)) {
                System.out.println("Cannot read frame");
                break;
            }

            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB); // convert to RGB

            // Define cloak color range (red example)
            Mat mask1 = new Mat();
            Mat mask2 = new Mat();
            Mat hsv = new Mat();
            Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_RGB2HSV);

            // Red color has two ranges in HSV
            Core.inRange(hsv, new Scalar(0, 120, 70), new Scalar(10, 255, 255), mask1);
            Core.inRange(hsv, new Scalar(170, 120, 70), new Scalar(180, 255, 255), mask2);

            Mat mask = new Mat();
            Core.addWeighted(mask1, 1.0, mask2, 1.0, 0.0, mask);

            // Morphological operations to remove noise
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3));
            Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);
            Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_DILATE, kernel);

            // Invert mask
            Mat maskInv = new Mat();
            Core.bitwise_not(mask, maskInv);

            // Extract cloak area from background
            Mat res1 = new Mat();
            Core.bitwise_and(background, background, res1, mask);

            // Extract non-cloak area from current frame
            Mat res2 = new Mat();
            Core.bitwise_and(frame, frame, res2, maskInv);

            // Combine both
            Mat finalFrame = new Mat();
            Core.addWeighted(res1, 1.0, res2, 1.0, 0.0, finalFrame);

            // Show result
            HighGui.imshow("Invisible Cloak", finalFrame);

            if (HighGui.waitKey(33) == 'q') {
                break;
            }
        }

        camera.release();
        HighGui.destroyAllWindows();
        System.out.println("Camera released. Program ended.");
    }
}
