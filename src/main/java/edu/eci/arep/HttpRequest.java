package edu.eci.arep;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an HTTP request, providing access to query parameters.
 */
public class HttpRequest {

    private final String path;
    private final Map<String, String> queryParams;

    /**
     * Constructs an HttpRequest by parsing the raw request URI.
     *
     * @param uri the full request URI (e.g. "/hello?name=Pedro")
     */
    public HttpRequest(String uri) {
        int queryIndex = uri.indexOf('?');
        if (queryIndex >= 0) {
            this.path = uri.substring(0, queryIndex);
            this.queryParams = parseQuery(uri.substring(queryIndex + 1));
        } else {
            this.path = uri;
            this.queryParams = Collections.emptyMap();
        }
    }

    /**
     * Returns the path portion of the request URI.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the value of the specified query parameter.
     *
     * @param name the query parameter name
     * @return the value, or an empty string if not present
     */
    public String getValues(String name) {
        return queryParams.getOrDefault(name, "");
    }

    /**
     * Returns an unmodifiable view of all query parameters.
     *
     * @return map of query parameter names to values
     */
    public Map<String, String> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        for (String pair : query.split("&")) {
            int eqIndex = pair.indexOf('=');
            if (eqIndex > 0) {
                String key = decode(pair.substring(0, eqIndex));
                String value = decode(pair.substring(eqIndex + 1));
                params.put(key, value);
            } else if (!pair.isEmpty()) {
                params.put(decode(pair), "");
            }
        }
        return params;
    }

    private static String decode(String value) {
        try {
            return java.net.URLDecoder.decode(value, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}
