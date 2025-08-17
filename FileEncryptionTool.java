import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileEncryptionTool {
    private static final int PORT = 8080;
    private static final String WEB_FOLDER = "web";
    private static final List<String> history = new LinkedList<>();

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server running on http://localhost:" + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(() -> handleClient(socket)).start();
        }
    }

    private static void handleClient(Socket socket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream out = socket.getOutputStream()
        ) {
            String requestLine = in.readLine();
            if (requestLine == null) return;

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;

            String method = parts[0];
            String path = parts[1];

            if (path.equals("/encrypt") || path.equals("/decrypt")) {
                handleEncryptionRequest(in, out, path.equals("/encrypt"));
            } else if (path.equals("/history")) {
                sendHistory(out);
            } else {
                serveStaticFile(out, path.equals("/") ? "/index.html" : path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleEncryptionRequest(BufferedReader in, OutputStream out, boolean encrypt) throws Exception {
        // Skip headers until we reach content length
        String line;
        int contentLength = 0;
        while (!(line = in.readLine()).isEmpty()) {
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        }

        char[] bodyChars = new char[contentLength];
        in.read(bodyChars);
        String body = new String(bodyChars);

        // Parse body
        Map<String, String> params = new HashMap<>();
        for (String param : body.split("&")) {
            String[] kv = param.split("=");
            if (kv.length == 2) {
                params.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8"));
            }
        }

        String filename = params.get("filename");
        String text = params.get("text");
        String password = params.get("password");

        if (!filename.endsWith(".txt")) {
            sendResponse(out, "Only .txt files are allowed!", "text/plain");
            return;
        }

        String result = encrypt ? simpleEncrypt(text, password) : simpleDecrypt(text, password);

        // Add to history (keep last 3)
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        history.add((encrypt ? "Encrypted" : "Decrypted") + " " + filename + " at " + time);
        if (history.size() > 3) history.remove(0);

        sendResponse(out, result, "text/plain");
    }

    private static void sendHistory(OutputStream out) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String entry : history) {
            sb.append(entry).append("\n");
        }
        sendResponse(out, sb.toString(), "text/plain");
    }

    private static void serveStaticFile(OutputStream out, String path) throws Exception {
        File file = new File(WEB_FOLDER + path);
        if (!file.exists()) {
            sendResponse(out, "404 Not Found", "text/plain");
            return;
        }
        String contentType = path.endsWith(".css") ? "text/css" :
                             path.endsWith(".js") ? "application/javascript" :
                             "text/html";
        sendResponse(out, new String(Files.readAllBytes(file.toPath())), contentType);
    }

    private static void sendResponse(OutputStream out, String content, String contentType) throws Exception {
        PrintWriter pw = new PrintWriter(out);
        pw.print("HTTP/1.1 200 OK\r\n");
        pw.print("Content-Type: " + contentType + "\r\n");
        pw.print("Content-Length: " + content.length() + "\r\n");
        pw.print("\r\n");
        pw.print(content);
        pw.flush();
    }

    private static String simpleEncrypt(String text, String password) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            sb.append((char)(text.charAt(i) + password.length()));
        }
        return sb.toString();
    }

    private static String simpleDecrypt(String text, String password) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            sb.append((char)(text.charAt(i) - password.length()));
        }
        return sb.toString();
    }
}
