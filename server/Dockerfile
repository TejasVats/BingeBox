FROM gradle:9.4.1-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew
RUN ./gradlew :server:installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/server/build/install/server/ ./
EXPOSE 8080
ENTRYPOINT ["bin/server"]