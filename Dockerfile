FROM eclipse-temurin:21-jdk-jammy

WORKDIR /usrapp/bin

# Port the app listens on (can be overridden at runtime)
ENV PORT=8080

# Copy compiled classes and all dependencies
COPY target/classes        ./classes

# Expose the application port
EXPOSE 8080

# Start the framework
CMD ["java", "-cp", "./classes", "edu.eci.arep.MicroSpringBoot"]
