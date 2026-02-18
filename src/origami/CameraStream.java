package origami;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import javax.swing.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class CameraStream {

    public static Mat frame = null;
    private static HttpStreamServer httpStreamService;
    static VideoCapture videoCapture;
    static Timer tmrVideoProcess;

    public static void start(String host, String port, String camFile) {
        videoCapture = Origami.CaptureDevice(camFile);
        if (!videoCapture.isOpened()) {
            return;
        }
        frame = new Mat();
        httpStreamService = new HttpStreamServer(host, port, frame);
        new Thread(httpStreamService).start();

        tmrVideoProcess = new Timer(10, e -> {
            if (!videoCapture.read(frame)) {
                tmrVideoProcess.stop();
            }
            httpStreamService.pushImage(frame);
        });
        tmrVideoProcess.start();
    }

    public static void main(String[] args) throws Exception {
        Origami.init();
        String hostname = args.length>0 ? args[0] : "0.0.0.0";
        String port = args.length>1 ? args[1] : "8090";
        String camFile = args.length >2?args[2]: "0";

        // String text = new String(Files.readAllBytes(Paths.get(camFile)), StandardCharsets.UTF_8);
        System.out.println(String.format("Using:\n > Bind: %s\n > Port: %s\n > Camera: %s", hostname, port, camFile));

        start(hostname, port, camFile);
        Thread.currentThread().join();
    }

}
