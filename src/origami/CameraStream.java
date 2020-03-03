package origami;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CameraStream {

    public static Mat frame = null;
    private static HttpStreamServer httpStreamService;
    static VideoCapture videoCapture;
    static Timer tmrVideoProcess;

    public static void start() {
        videoCapture = new VideoCapture();
        videoCapture.open(0);
        if (!videoCapture.isOpened()) {
            return;
        }

        frame = new Mat();
        httpStreamService = new HttpStreamServer(frame);
        new Thread(httpStreamService).start();

        tmrVideoProcess = new Timer(100, e -> {
            if (!videoCapture.read(frame)) {
                tmrVideoProcess.stop();
            }
            httpStreamService.imag = frame;
        });
        tmrVideoProcess.start();
    }

    public static void main(String[] args) {
        Origami.init();
        start();
    }

}
