package origami;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static org.opencv.imgcodecs.Imgcodecs.imencode;

/**
 *
 */
public class HttpStreamServer implements Runnable {

    private BufferedImage img;
    private ServerSocket serverSocket;
    private Socket socket;
    private final String boundary = "stream";
    private OutputStream outputStream;
    public Mat imag;
    int port = 8080;


    public HttpStreamServer(Mat imagFr) {
        this.imag = imagFr;
    }

    public void startStreamingServer() throws IOException {
//        InetAddress address = InetAddress.getByAddress("0.0.0.0".getBytes());
//        serverSocket = new ServerSocket(8080, 10, address);
        serverSocket = new ServerSocket(port);
        socket = serverSocket.accept();
        writeHeader(socket.getOutputStream());
    }

    private void writeHeader(OutputStream stream) throws IOException {
        stream.write(headers().getBytes());
        stream.flush();
    }

    private String headers() {
        return "HTTP/1.0 200 OK\r\n" +
                "Connection: close\r\n" +
                "Max-Age: 0\r\n" +
                "Expires: 0\r\n" +
                "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" +
                "Pragma: no-cache\r\n" +
                "Content-Type: multipart/x-mixed-replace; " +
                "boundary=" + boundary + "\r\n" +
                "\r\n" +
                "--" + boundary + "\r\n";
    }

    public void pushImage(Mat frame) throws IOException {
        if (frame == null)
            return;
        try {
            outputStream = socket.getOutputStream();
            MatOfByte matOfByte = new MatOfByte();
            imencode(".jpg", frame, matOfByte);
            byte[] imageBytes = matOfByte.toArray();

            outputStream.write(("Content-type: image/jpeg\r\n" +
                    "Content-Length: " + imageBytes.length + "\r\n" +
                    "\r\n").getBytes());
            outputStream.write(imageBytes);
            outputStream.write(("\r\n--" + boundary + "\r\n").getBytes());
        } catch (Exception ex) {
            socket = serverSocket.accept();
            writeHeader(socket.getOutputStream());
        }
    }

    public void run() {
        try {
            System.out.print("go to  http://localhost:8080 with browser");
            startStreamingServer();

            while (true) {
                pushImage(imag);
            }
        } catch (IOException e) {
            return;
        }

    }

    public void stopStreamingServer() throws IOException {
        socket.close();
        serverSocket.close();
    }
}