# syntax=docker/dockerfile:1.6

# Build with buildx for the right platform; default to amd64 if not provided
ARG BUILDPLATFORM=linux/amd64
ARG TARGETPLATFORM=linux/amd64

# Build stage
FROM --platform=${BUILDPLATFORM} eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew || true
RUN ./gradlew -PskipTests=true bootJar || ./gradle -PskipTests=true bootJar

# Run stage
FROM --platform=${TARGETPLATFORM} eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/anexya-api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
