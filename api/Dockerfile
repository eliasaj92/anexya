# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew || true
RUN ./gradlew -PskipTests=true bootJar || ./gradle -PskipTests=true bootJar

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/anexya-api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
