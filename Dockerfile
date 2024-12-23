FROM amazoncorretto:23.0.1-alpine
VOLUME /tmp
EXPOSE 8080

ARG JAR_FILE=target/customer-management-api-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
