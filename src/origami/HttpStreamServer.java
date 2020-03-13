package origami;

import org.opencv.core.Mat;
import origami.utils.FileWatcher;

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

/**
 *
 */
public class HttpStreamServer implements Runnable {

    final private static String boundary = "stream";
    final private static String headers() {
        return headers(boundary);
    }
    final private static String headers(String boundary) {
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

    private Mat frame;
    private ServerSocket serverSocket;
    private OutputStream outputStream;
    private Mat imag;
    int port = 8180;
    String hostname = "0.0.0.0";

    public HttpStreamServer(Mat imagFr) {
        this.imag = imagFr;
    }

    public void startStreamingServer() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(hostname, port));
        while (true) {
            Socket socket = serverSocket.accept();
            new Handler(socket).start();
        }

    }

    class Handler extends Thread {
        private Socket socket;
        private MyWatcher myWatcher;

        public Handler(Socket _sock) {
            this.socket = _sock;
        }

        OutputStream outputStream;
        Filter streamVideoFilter;

        public void run() {
            try {
                outputStream = socket.getOutputStream();
                streamVideoFilter = parseFilter();
                writeHeader(outputStream);
                while (true) {
                    byte[] imageBytes = Origami.matToBytes(streamVideoFilter.apply(frame));
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
//                try {
//                    outputStream = socket.getOutputStream();
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
            } finally {
                try {
                    socket.close();
                    System.out.println("Thread finishing ...");
                } catch (IOException e) {
                    // e.printStackTrace();
                }
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
                    myWatcher = new MyWatcher(f);
                    myWatcher.start();
                    return _f;
                } else {
                    return Origami.StringToFilter(result);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Filter f = mat -> mat;
                return f;
            }
        }

        class MyWatcher extends FileWatcher {

            public MyWatcher(File file) {
                super(file);
            }

            @Override
            public void doOnChange() {
                streamVideoFilter = Origami.StringToFilter(this.file);
            }
        }


    }

    private void writeHeader(OutputStream stream) throws IOException {
        stream.write(headers().getBytes());
        stream.flush();
    }


    public void pushImage(Mat frame) {
        this.frame = frame;
    }

    public void run() {
        try {
            System.out.print("go to  http://" + hostname + ":" + port + " with browser");
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