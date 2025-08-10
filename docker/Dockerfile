FROM gradle:8.14.3-jdk24-ubi AS backend

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

RUN gradle bootJar

FROM eclipse-temurin:24-jre AS runtime

WORKDIR /app

COPY --from=backend /app/build/libs/model-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
