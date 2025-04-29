FROM openjdk:23-jdk-slim
RUN apt-get update && apt-get install -y curl
ARG JAR_FILE=./registry-server-runner/target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 9095
ENTRYPOINT ["java", "-jar", "/app.jar"]
