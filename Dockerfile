# ---- build stage ----
FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# ---- runtime stage ----
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
ENV JAVA_OPTS="-Xms256m -Xmx512m"
COPY --from=build /build/target/redis-app-0.0.1-SNAPSHOT.jar redis-app.jar
# exec в sh нужен, чтобы JAVA_OPTS подставлялся в команду запуска и при этом
# java оставался процессом PID 1 и корректно получал SIGTERM.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar redis-app.jar"]
