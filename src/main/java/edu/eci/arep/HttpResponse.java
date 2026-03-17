package edu.eci.arep;

/**
 * Represents an HTTP response. Provides helper methods to set response metadata.
 */
public class HttpResponse {

    private int statusCode = 200;
    private String contentType = "text/plain";

    /**
     * Sets the HTTP status code.
     *
     * @param statusCode the status code
     * @return this response (for chaining)
     */
    public HttpResponse status(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * Sets the Content-Type header.
     *
     * @param contentType the MIME type
     * @return this response (for chaining)
     */
    public HttpResponse contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Returns the HTTP status code.
     *
     * @return status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the Content-Type.
     *
     * @return content type string
     */
    public String getContentType() {
        return contentType;
    }
}
