FROM openjdk:17-alpine

RUN mkdir /app
ADD target/opscanner-0.0.1-SNAPSHOT.jar /app

ENTRYPOINT [ "java", "-jar", "/app/opscanner-0.0.1-SNAPSHOT.jar" ]