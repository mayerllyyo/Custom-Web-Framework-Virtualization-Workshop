package edu.eci.arep;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Core HTTP server that dispatches requests to registered routes or serves static files.
 */
public class WebServer {

    private static final int THREAD_POOL_SIZE = 10;

    private final int port;
    private final Map<String, Route> routes;
    private final String staticFilesLocation;

    /**
     * Constructs a WebServer with the given routes and static files location.
     *
     * @param routes              map of path to route handler
     * @param staticFilesLocation classpath location for static files
     * @param port                port to listen on
     */
    public WebServer(Map<String, Route> routes, String staticFilesLocation, int port) {
        this.routes = new HashMap<>(routes);
        this.staticFilesLocation = staticFilesLocation;
        this.port = port;
    }

    /**
     * Starts the server, accepting connections indefinitely.
     *
     * @throws IOException if the server socket cannot be created
     */
    public void start() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Logger.getLogger(WebServer.class.getName()).info("Server running on http://localhost:" + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleConnection(clientSocket));
            }
        }
    }

    private void handleConnection(Socket clientSocket) {
        try (clientSocket;
             InputStream in = clientSocket.getInputStream();
             OutputStream out = clientSocket.getOutputStream()) {

            // Read the request line
            StringBuilder requestBuilder = new StringBuilder();
            byte[] buffer = new byte[4096];
            int bytesRead = in.read(buffer);
            if (bytesRead <= 0) return;
            requestBuilder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));

            String requestText = requestBuilder.toString();
            String[] lines = requestText.split("\r\n");
            if (lines.length == 0) return;

            // Parse request line: METHOD PATH HTTP/VERSION
            String[] requestLine = lines[0].split(" ");
            if (requestLine.length < 2) return;

            String method = requestLine[0];
            String uri = requestLine[1];

            if (!"GET".equalsIgnoreCase(method)) {
                sendResponse(out, 405, "text/plain", "Method Not Allowed".getBytes(StandardCharsets.UTF_8));
                return;
            }

            // Separate path from query string
            String path = uri.contains("?") ? uri.substring(0, uri.indexOf('?')) : uri;

            // Check registered routes first
            if (routes.containsKey(path)) {
                HttpRequest req = new HttpRequest(uri);
                HttpResponse res = new HttpResponse();
                String body = routes.get(path).handle(req, res);
                byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
                sendResponse(out, res.getStatusCode(), res.getContentType(), bodyBytes);
                return;
            }

            // Try to serve a static file
            serveStaticFile(out, path);

        } catch (IOException e) {
            System.err.println("Error handling connection: " + e.getMessage());
        }
    }

    private void serveStaticFile(OutputStream out, String path) throws IOException {
        // Normalize: root path maps to index.html
        if ("/".equals(path)) {
            path = "/index.html";
        }

        String resourcePath = staticFilesLocation + path;
        // Remove leading slash for classpath lookup
        if (resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }

        InputStream fileStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (fileStream == null) {
            String notFound = "404 Not Found: " + path;
            sendResponse(out, 404, "text/plain", notFound.getBytes(StandardCharsets.UTF_8));
            return;
        }

        byte[] content = fileStream.readAllBytes();
        fileStream.close();
        String contentType = getContentType(path);
        sendResponse(out, 200, contentType, content);
    }

    private void sendResponse(OutputStream out, int statusCode, String contentType, byte[] body) throws IOException {
        String statusText = getStatusText(statusCode);
        String header = "HTTP/1.1 " + statusCode + " " + statusText + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n"
                + "\r\n";
        out.write(header.getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }

    private String getStatusText(int code) {
        switch (code) {
            case 200: return "OK";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 500: return "Internal Server Error";
            default:  return "Unknown";
        }
    }

    private String getContentType(String path) {
        if (path.endsWith(".html") || path.endsWith(".htm")) return "text/html";
        if (path.endsWith(".css"))  return "text/css";
        if (path.endsWith(".js"))   return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".png"))  return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif"))  return "image/gif";
        if (path.endsWith(".ico"))  return "image/x-icon";
        if (path.endsWith(".svg"))  return "image/svg+xml";
        return "text/plain";
    }
}
