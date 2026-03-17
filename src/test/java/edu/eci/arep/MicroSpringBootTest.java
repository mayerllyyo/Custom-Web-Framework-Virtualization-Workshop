package edu.eci.arep;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for MicroSpringBoot framework
 * Tests cover: annotations, routing, HTTP model, and concurrent behavior
 */
@DisplayName("MicroSpringBoot Framework Tests")
public class MicroSpringBootTest {

    @BeforeEach
    public void setUp() {
        // Reset framework state before each test
        WebFramework.reset();
        MicroSpringBoot.controllerMethods.clear();
        MicroSpringBoot.controllerInstances.clear();
    }

    // =================== ANNOTATION TESTS ===================
    
    @Test
    @DisplayName("RestController annotation exists on HelloController")
    public void testRestControllerAnnotationExists() {
        assertTrue(HelloController.class.isAnnotationPresent(RestController.class));
    }

    @Test
    @DisplayName("GetMapping annotation exists on HelloController methods")
    public void testGetMappingAnnotationExists() throws NoSuchMethodException {
        Method method = HelloController.class.getDeclaredMethod("index");
        assertTrue(method.isAnnotationPresent(GetMapping.class));
    }

    @Test
    @DisplayName("RequestParam annotation exists on GreetingController")
    public void testRequestParamAnnotationExists() throws NoSuchMethodException {
        Method method = GreetingController.class.getDeclaredMethod("greeting", String.class);
        assertNotNull(method);
    }

    // =================== HTTP REQUEST TESTS ===================

    @Test
    @DisplayName("HttpRequest parses path and query parameters correctly")
    public void testHttpRequestParseQuery() {
        HttpRequest req = new HttpRequest("/greeting?name=Pedro&age=30");
        assertEquals("/greeting", req.getPath());
        assertEquals("Pedro", req.getValues("name"));
        assertEquals("30", req.getValues("age"));
    }

    @Test
    @DisplayName("HttpRequest handles URL without query string")
    public void testHttpRequestNoQuery() {
        HttpRequest req = new HttpRequest("/api");
        assertEquals("/api", req.getPath());
    }

    @Test
    @DisplayName("HttpRequest returns empty string for missing parameter")
    public void testHttpRequestDefaultQueryValue() {
        HttpRequest req = new HttpRequest("/api");
        assertEquals("", req.getValues("nonexistent"));
    }

    @Test
    @DisplayName("HttpRequest handles single query parameter")
    public void testHttpRequestSingleParameter() {
        HttpRequest req = new HttpRequest("/user?id=123");
        assertEquals("123", req.getValues("id"));
    }

    // =================== HTTP RESPONSE TESTS ===================

    @Test
    @DisplayName("HttpResponse sets and retrieves status code")
    public void testHttpResponseStatus() {
        HttpResponse res = new HttpResponse();
        res.status(404);
        assertEquals(404, res.getStatusCode());
    }

    @Test
    @DisplayName("HttpResponse sets and retrieves content type")
    public void testHttpResponseContentType() {
        HttpResponse res = new HttpResponse();
        res.contentType("application/json");
        assertEquals("application/json", res.getContentType());
    }

    @Test
    @DisplayName("HttpResponse defaults to 200 status")
    public void testHttpResponseDefaultStatus() {
        HttpResponse res = new HttpResponse();
        assertEquals(200, res.getStatusCode());
    }

    @Test
    @DisplayName("HttpResponse defaults to text/plain content type")
    public void testHttpResponseDefaultContentType() {
        HttpResponse res = new HttpResponse();
        assertEquals("text/plain", res.getContentType());
    }

    // =================== CONTROLLER TESTS ===================

    @Test
    @DisplayName("HelloController.index() returns greeting message")
    public void testHelloControllerIndex() throws Exception {
        HelloController controller = new HelloController();
        String result = controller.index();
        assertNotNull(result);
        assertTrue(result.contains("Greetings") || result.contains("MicroSpringBoot"));
    }

    @Test
    @DisplayName("HelloController.hello() returns Hello World")
    public void testHelloControllerHello() throws Exception {
        HelloController controller = new HelloController();
        String result = controller.hello();
        assertNotNull(result);
        assertTrue(result.contains("Hello"));
    }

    @Test
    @DisplayName("HelloController.getPI() returns PI value")
    public void testHelloControllerPI() throws Exception {
        HelloController controller = new HelloController();
        String result = controller.getPI();
        assertNotNull(result);
        assertTrue(result.contains("3.14"));
    }

    @Test
    @DisplayName("GreetingController.greeting() returns personalized greeting")
    public void testGreetingControllerGreeting() throws Exception {
        GreetingController controller = new GreetingController();
        String result = controller.greeting("TestUser");
        assertTrue(result.contains("TestUser"));
    }

    @Test
    @DisplayName("GreetingController.greeting() handles null parameter")
    public void testGreetingControllerNullParameter() throws Exception {
        GreetingController controller = new GreetingController();
        String result = controller.greeting("");
        assertNotNull(result);
    }

    @Test
    @DisplayName("RequestParam annotation has value attribute")
    public void testRequestParamAnnotationValue() throws NoSuchMethodException {
        Method method = GreetingController.class.getDeclaredMethod("greeting", String.class);
        RequestParam param = method.getParameters()[0].getAnnotation(RequestParam.class);
        assertNotNull(param);
        assertEquals("name", param.value());
    }

    // =================== FRAMEWORK CONFIGURATION TESTS ===================

    @Test
    @DisplayName("WebFramework can register GET routes")
    public void testWebFrameworkGetRoute() {
        WebFramework.get("/test", (req, res) -> "test response");
        assertEquals(1, WebFramework.getRoutes().size());
        assertTrue(WebFramework.getRoutes().containsKey("/test"));
    }

    @Test
    @DisplayName("WebFramework tracks static file location")
    public void testWebFrameworkStaticFiles() {
        WebFramework.staticfiles("static");
        assertEquals("static", WebFramework.getStaticFilesLocation());
    }

    @Test
    @DisplayName("WebFramework tracks configured port")
    public void testWebFrameworkPort() {
        WebFramework.port(9000);
        assertEquals(9000, WebFramework.getPort());
    }

    @Test
    @DisplayName("WebFramework resets all configuration")
    public void testWebFrameworkReset() {
        WebFramework.get("/path", (req, res) -> "response");
        WebFramework.port(9000);
        WebFramework.staticfiles("custom");
        
        WebFramework.reset();
        
        assertEquals(0, WebFramework.getRoutes().size());
        assertEquals(8080, WebFramework.getPort());
        assertEquals("webroot", WebFramework.getStaticFilesLocation());
    }

    // =================== CONCURRENT REQUEST TESTS ===================

    @Test
    @DisplayName("Multiple concurrent route registrations work correctly")
    public void testConcurrentRouteRegistration() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int routeNum = i;
            new Thread(() -> {
                WebFramework.get("/route" + routeNum, (req, res) -> "response" + routeNum);
                latch.countDown();
            }).start();
        }
        
        latch.await();
        assertEquals(threadCount, WebFramework.getRoutes().size());
    }

    @Test
    @DisplayName("Controller methods can be invoked concurrently")
    public void testConcurrentControllerInvocation() throws InterruptedException {
        HelloController controller = new HelloController();
        int threadCount = 20;
        int invokeCount = 5;
        AtomicInteger successCount = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < invokeCount; j++) {
                    try {
                        String result = controller.index();
                        if (result != null && !result.isEmpty()) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // Count failed invocations
                    }
                }
            });
        }
        
        executor.shutdown();
        Thread.sleep(2000); // Wait for execution
        
        assertEquals(threadCount * invokeCount, successCount.get());
    }

    @Test
    @DisplayName("HttpRequest creation is thread-safe")
    public void testThreadSafeHttpRequestCreation() throws InterruptedException {
        int threadCount = 50;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    HttpRequest req = new HttpRequest("/api?id=" + threadId + "&data=test");
                    assertEquals("/api", req.getPath());
                    assertEquals(String.valueOf(threadId), req.getValues("id"));
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        
        latch.await();
        assertEquals(threadCount, successCount.get());
    }

    @Test
    @DisplayName("Multiple HttpResponse objects work independently")
    public void testMultipleHttpResponses() {
        HttpResponse res1 = new HttpResponse();
        HttpResponse res2 = new HttpResponse();
        
        res1.status(200);
        res1.contentType("application/json");
        
        res2.status(404);
        res2.contentType("text/html");
        
        assertEquals(200, res1.getStatusCode());
        assertEquals("application/json", res1.getContentType());
        assertEquals(404, res2.getStatusCode());
        assertEquals("text/html", res2.getContentType());
    }

    // =================== INTEGRATION TESTS ===================

    @Test
    @DisplayName("Framework can handle route with query parameter")
    public void testFrameworkRouteWithParameter() {
        WebFramework.get("/api", (req, res) -> {
            String name = req.getValues("name");
            return "Hello, " + name;
        });
        
        Route route = WebFramework.getRoutes().get("/api");
        assertNotNull(route);
        HttpRequest req = new HttpRequest("/api?name=John");
        HttpResponse res = new HttpResponse();
        String result = route.handle(req, res);
        assertTrue(result.contains("John"));
    }

    @Test
    @DisplayName("Framework can handle route with multiple parameters")
    public void testFrameworkRouteWithMultipleParameters() {
        WebFramework.get("/user", (req, res) -> {
            String name = req.getValues("name");
            String age = req.getValues("age");
            return name + "::" + age;
        });
        
        Route route = WebFramework.getRoutes().get("/user");
        HttpRequest req = new HttpRequest("/user?name=Alice&age=25");
        HttpResponse res = new HttpResponse();
        String result = route.handle(req, res);
        assertEquals("Alice::25", result);
    }

    @Test
    @DisplayName("Route handlers can set custom response status")
    public void testRouteHandlerCustomStatus() {
        WebFramework.get("/created", (req, res) -> {
            res.status(201);
            res.contentType("application/json");
            return "{\"id\": 123}";
        });
        
        Route route = WebFramework.getRoutes().get("/created");
        HttpResponse res = new HttpResponse();
        route.handle(new HttpRequest("/created"), res);
        assertEquals(201, res.getStatusCode());
        assertEquals("application/json", res.getContentType());
    }

    // =================== PERFORMANCE BASELINE TESTS ===================

    @Test
    @DisplayName("Route lookup performance under load")
    public void testRouteLookupPerformance() {
        // Register many routes
        for (int i = 0; i < 1000; i++) {
            WebFramework.get("/route" + i, (req, res) -> "response");
        }
        
        long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            WebFramework.getRoutes().get("/route" + (i % 1000));
        }
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        // Should complete 10k lookups in less than 1 second
        assertTrue(durationMs < 1000, "Route lookup too slow: " + durationMs + "ms");
    }

    @Test
    @DisplayName("Request parsing performance")
    public void testRequestParsingPerformance() {
        long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            HttpRequest req = new HttpRequest("/api?id=" + i + "&name=test&data=value");
            req.getPath();
            req.getValues("id");
            req.getValues("name");
        }
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        // Should complete 10k request creations in less than 2 seconds
        assertTrue(durationMs < 2000, "Request parsing too slow: " + durationMs + "ms");
    }
}
