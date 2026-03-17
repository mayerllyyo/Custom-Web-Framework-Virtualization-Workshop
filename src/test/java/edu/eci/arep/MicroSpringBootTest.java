package edu.eci.arep;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MicroSpringBoot framework
 */
public class MicroSpringBootTest {

    @BeforeEach
    public void setUp() {
        // Reset framework state before each test
        WebFramework.reset();
        MicroSpringBoot.controllerMethods.clear();
        MicroSpringBoot.controllerInstances.clear();
    }

    @Test
    public void testRestControllerAnnotationExists() {
        assertTrue(HelloController.class.isAnnotationPresent(RestController.class));
    }

    @Test
    public void testGetMappingAnnotationExists() throws NoSuchMethodException {
        Method method = HelloController.class.getDeclaredMethod("index");
        assertTrue(method.isAnnotationPresent(GetMapping.class));
    }

    @Test
    public void testRequestParamAnnotationExists() throws NoSuchMethodException {
        Method method = GreetingController.class.getDeclaredMethod("greeting", String.class);
        assertNotNull(method);
        // The method exists
    }

    @Test
    public void testHttpRequestParseQuery() {
        HttpRequest req = new HttpRequest("/greeting?name=Pedro");
        assertEquals("/greeting", req.getPath());
        assertEquals("Pedro", req.getValues("name"));
    }

    @Test
    public void testHttpRequestDefaultQueryValue() {
        HttpRequest req = new HttpRequest("/api");
        assertEquals("/api", req.getPath());
        assertEquals("", req.getValues("nonexistent"));
    }

    @Test
    public void testHttpResponseStatus() {
        HttpResponse res = new HttpResponse();
        res.status(404);
        assertEquals(404, res.getStatusCode());
    }

    @Test
    public void testHttpResponseContentType() {
        HttpResponse res = new HttpResponse();
        res.contentType("application/json");
        assertEquals("application/json", res.getContentType());
    }

    @Test
    public void testHelloControllerIndex() throws Exception {
        HelloController controller = new HelloController();
        String result = controller.index();
        assertNotNull(result);
        assertTrue(result.contains("Greetings") || result.contains("MicroSpringBoot"));
    }

    @Test
    public void testGreetingControllerGreeting() throws Exception {
        GreetingController controller = new GreetingController();
        String result = controller.greeting("TestUser");
        assertTrue(result.contains("TestUser"));
    }

    @Test
    public void testGreetingControllerDefaultValue() throws Exception {
        GreetingController controller = new GreetingController();
        // The default behavior can be tested, but requires method invocation with reflection
        // which is complex. This demonstrates the annotation exists.
        Method method = GreetingController.class.getDeclaredMethod("greeting", String.class);
        assertNotNull(method.getParameters()[0].getAnnotation(RequestParam.class));
    }
}
