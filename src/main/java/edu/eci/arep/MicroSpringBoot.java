package edu.eci.arep;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MicroSpringBoot {

    static Map<String, Method> controllerMethods = new HashMap<>();
    static Map<String, Object> controllerInstances = new HashMap<>();

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, InstantiationException {
        Logger.getLogger(MicroSpringBoot.class.getName()).info("Loading components ...");

        // Load specific controllers passed as arguments
        for (int i = 0; i < args.length; i++) {
            loadController(args[i]);
        }

        // Auto-discover controllers from classpath if no args provided
        if (args.length == 0) {
            autoDiscoverControllers();
        }

        Logger.getLogger(MicroSpringBoot.class.getName()).info("Starting web server...");
        
        // Start the web server with discovered routes
        startServer();
    }

    private static void loadController(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<?> c = Class.forName(className);
        if (c.isAnnotationPresent(RestController.class)) {
            registerController(c);
        }
    }

    private static void registerController(Class<?> controllerClass) throws InstantiationException, IllegalAccessException {
        Object instance;
        try {
            instance = controllerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            instance = null;
        }

        for (Method m : controllerClass.getDeclaredMethods()) {
            if (m.isAnnotationPresent(GetMapping.class)) {
                GetMapping mapping = m.getAnnotation(GetMapping.class);
                String path = mapping.value();
                controllerMethods.put(path, m);
                controllerInstances.put(path, instance);
                Logger.getLogger(MicroSpringBoot.class.getName()).info("Registered route: " + path);
            }
        }
    }

    private static void autoDiscoverControllers() {
        // Try to load common controller classes from same package
        String[] commonControllers = {
            "edu.eci.arep.HelloController",
            "edu.eci.arep.GreetingController"
        };

        for (String controllerName : commonControllers) {
            try {
                loadController(controllerName);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                // Controller not found, continue
            }
        }
    }

    private static void startServer() throws IOException {
        // Register routes with WebFramework
        for (Map.Entry<String, Method> entry : controllerMethods.entrySet()) {
            String path = entry.getKey();
            Method method = entry.getValue();
            Object instance = controllerInstances.get(path);

            WebFramework.get(path, (req, res) -> invokeMethod(method, instance, req, res));
        }

        WebFramework.staticfiles("webroot");
        WebFramework.port(8080);
        WebFramework.start();
    }

    private static String invokeMethod(Method method, Object instance, HttpRequest req, HttpResponse res) {
        try {
            Parameter[] parameters = method.getParameters();
            Object[] args = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                if (param.isAnnotationPresent(RequestParam.class)) {
                    RequestParam annotation = param.getAnnotation(RequestParam.class);
                    String paramName = annotation.value();
                    String paramValue = req.getValues(paramName);
                    
                    if (paramValue == null || paramValue.isEmpty()) {
                        paramValue = annotation.defaultValue();
                    }
                    
                    args[i] = paramValue;
                }
            }

            Object result = method.invoke(instance, args);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
