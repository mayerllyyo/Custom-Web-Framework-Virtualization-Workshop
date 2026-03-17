package edu.eci.arep;

/**
 * Functional interface representing a route handler.
 * Implementations handle an HTTP request and produce a response string.
 */
@FunctionalInterface
public interface Route {
    /**
     * Handles an HTTP request.
     *
     * @param req the HTTP request
     * @param res the HTTP response
     * @return the response body as a String
     */
    String handle(HttpRequest req, HttpResponse res);
}
