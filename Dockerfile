FROM openjdk:17
LABEL authors="nrkim"



# keystore.p12 복사 추가
COPY src/main/resources/keystore.p12 /app/keystore.p12

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
