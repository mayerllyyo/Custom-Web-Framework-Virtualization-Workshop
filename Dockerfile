# Runtime stage with pre-built JAR
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /usrapp/bin

# Default port configuration (can be overridden at runtime)
ENV PORT=8080

# Copy pre-compiled classes (build locally with mvn clean package)
COPY target/classes ./classes

# Expose the application port
EXPOSE 8080

# Health check for container orchestration
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:${PORT}/ || exit 1

# Start the framework with exec form to ensure proper signal handling
# This enables graceful shutdown via SIGTERM
ENTRYPOINT ["java", "-cp", "./classes"]
CMD ["edu.eci.arep.MicroSpringBoot"]
