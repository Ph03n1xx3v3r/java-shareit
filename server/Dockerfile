FROM eclipse-temurin:21-jre
WORKDIR /app
COPY shareIt-server/target/shareIt-server-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
