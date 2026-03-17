package edu.eci.arep;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Web framework entry point providing static methods to register REST routes
 * and configure static file serving.
 *
 */
public class WebFramework {

    private static final Map<String, Route> routes = new HashMap<>();
    private static String staticFilesLocation = "webroot";
    private static int port = 8080;

    private WebFramework() {
        // utility class
    }

    /**
     * Registers a GET route for the given path with the provided handler.
     *
     * @param path    the URL path (e.g. "/hello")
     * @param handler a lambda that processes the request and returns a response body
     */
    public static void get(String path, Route handler) {
        routes.put(path, handler);
    }

    /**
     * Specifies the classpath folder from which static files are served.
     * The folder is resolved relative to the classpath root.
     *
     * @param location the folder path (e.g. "/webroot" or "webroot/public")
     */
    public static void staticfiles(String location) {
        // Normalize: ensure no trailing slash, keep leading slash stripped later in WebServer
        staticFilesLocation = location.startsWith("/") ? location.substring(1) : location;
    }

    /**
     * Sets the port on which the server listens. Defaults to 8080.
     *
     * @param serverPort the port number
     */
    public static void port(int serverPort) {
        port = serverPort;
    }

    /**
     * Starts the web server.
     *
     * @throws IOException if the server cannot be started
     */
    public static void start() throws IOException {
        WebServer server = new WebServer(routes, staticFilesLocation, port);
        server.start();
    }

    /**
     * Resets the framework state (useful for testing).
     */
    static void reset() {
        routes.clear();
        staticFilesLocation = "webroot";
        port = 8080;
    }

    /**
     * Returns a copy of all registered routes (for testing).
     *
     * @return map of path to route handler
     */
    static Map<String, Route> getRoutes() {
        return new HashMap<>(routes);
    }

    /**
     * Returns the configured static files location (for testing).
     *
     * @return the static files location
     */
    static String getStaticFilesLocation() {
        return staticFilesLocation;
    }
}
