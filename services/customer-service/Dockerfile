FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG JAR_FILE=services/customer-service/target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]

