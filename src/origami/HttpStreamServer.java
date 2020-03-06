package origami;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.opencv.imgcodecs.Imgcodecs.imencode;

/**
 *
 */
public class HttpStreamServer implements Runnable {

    private BufferedImage img;
    private ServerSocket serverSocket;


    private OutputStream outputStream;
    public Mat imag;
    int port = 8180;


    public HttpStreamServer(Mat imagFr) {
        this.imag = imagFr;
    }

    public void startStreamingServer() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress("0.0.0.0", port));
        while (true) {
            Socket socket = serverSocket.accept();
            new Handler(socket).start();
        }

    }

    class Handler extends Thread {
        private Socket socket;

        public Handler(Socket _sock) {
            this.socket = _sock;
        }

        OutputStream outputStream;

        public void run() {
            try {
                outputStream = socket.getOutputStream();
                Filter f = parseFilter();
//                System.out.println(f.get)
                writeHeader(outputStream);
                while (true) {
                    byte[] imageBytes = matToBytes(f.apply(frame));
                    if (imageBytes != null) {
                        outputStream.write(("Content-type: image/jpeg\r\n" +
                                "Content-Length: " + imageBytes.length + "\r\n" +
                                "\r\n").getBytes());
                        outputStream.write(imageBytes);
                        outputStream.write(("\r\n--" + boundary + "\r\n").getBytes());
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
                try {
                    outputStream = socket.getOutputStream();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } finally {
//                try {
//                    socket.close();
//                } catch (IOException e) {
//                    // e.printStackTrace();
//                }
            }

        }

        private Filter parseFilter() {
            byte[] b = new byte[4096];
            try {
                socket.getInputStream().read(b);
                List<String> headers = Arrays.asList(new String(b).split("\n"));
                String get = headers.stream().filter(e -> e.contains("GET")).collect(Collectors.toList()).get(0);
                String query = get.split(" ")[1];
                String filter = query.split("=")[1];
                String result = java.net.URLDecoder.decode(filter, StandardCharsets.UTF_8.name());
                System.out.println(result);
                File f = new File(result);
                if (f.exists()) {
                    Filter _f = Origami.StringToFilter(f);
                    return _f;
                } else {
                    Filter _f = Origami.StringToFilter(result);
                    return _f;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Filter f = mat -> mat;
                return f;
            }
        }

    }

    private void writeHeader(OutputStream stream) throws IOException {
        stream.write(headers().getBytes());
        stream.flush();
    }

    static String boundary = "stream";

    static String headers() {
        return headers(boundary);
    }

    static String headers(String boundary) {
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

    //    private byte[] imageBytes = null;
    private Mat frame;

    public void pushImage(Mat frame) {
        this.frame = frame;
    }

    private byte[] matToBytes(Mat frame) {
        MatOfByte matOfByte = new MatOfByte();
        imencode(".jpg", frame, matOfByte);
        return matOfByte.toArray();
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
        serverSocket.close();
    }
}