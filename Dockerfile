FROM maven:3.6.2-jdk-11-slim AS build

COPY ./etc/settings.xml /root/.m2/settings.xml
COPY ./ /app
WORKDIR /app
RUN mvn clean package

FROM openjdk:11-jdk AS jdk
COPY --from=build /app/kypo2-rest-training/target/kypo2-rest-training-*.jar /app/kypo-training.jar
COPY --from=build /app/etc/training.properties /app/etc/training.properties

WORKDIR /app
EXPOSE 8083
ENTRYPOINT ["java", "-Dpath.to.config.file=/app/etc/training.properties", "-jar", "/app/kypo-training.jar"]
