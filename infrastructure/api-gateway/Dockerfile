FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG JAR_FILE=infrastructure/api-gateway/target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]

