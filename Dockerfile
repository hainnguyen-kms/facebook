
FROM maven:3.6.3-jdk-11 AS MAVEN_TOOL_CHAIN
COPY pom.xml /tmp/
RUN mvn -B dependency:go-offline -f /tmp/pom.xml -s /usr/share/maven/ref/settings-docker.xml
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn -B -s /usr/share/maven/ref/settings-docker.xml package

FROM openjdk:11.0-jre

EXPOSE 8080

RUN mkdir /app
COPY --from=MAVEN_TOOL_CHAIN /tmp/target/*.jar /app/spring-boot-application.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Daws.accessKeyId=AKIAW5BA2BPHOWHP5Y5S","-Daws.secretAccessKey=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","-jar","/app/spring-boot-application.jar"]