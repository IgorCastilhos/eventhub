# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw .
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B
# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd -g 1001 eventhub && useradd -u 1001 -g eventhub -m eventhub
COPY --from=build --chown=eventhub:eventhub /app/target/*.jar app.jar
USER eventhub
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
