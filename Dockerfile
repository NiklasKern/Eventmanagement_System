# Build-Stage: laeuft nativ auf der Build-Maschine. Tests laufen vorher im Workflow (Testcontainers braucht Docker).
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon > /dev/null
COPY src src
RUN ./gradlew bootJar --no-daemon

# Runtime-Stage (linux/arm64)
FROM eclipse-temurin:21-jre
LABEL org.opencontainers.image.source=https://github.com/NiklasKern/Eventmanagement_System
WORKDIR /app
RUN useradd --system --create-home spring
USER spring
COPY --from=build /app/build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]