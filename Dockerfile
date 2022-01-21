############ BUILD STAGE ############
FROM maven:3.6.2-jdk-11-slim AS build
WORKDIR /app
ARG PROJECT_ARTIFACT_ID=kypo-rest-training
# Default link to proprietary repository, e.g., GitLab repository
ARG PROPRIETARY_REPO_URL=YOUR-PATH-TO-PROPRIETARY_REPO
COPY pom.xml /app/pom.xml
COPY kypo-api-training /app/kypo-api-training
COPY kypo-elasticsearch-training /app/kypo-elasticsearch-training
COPY kypo-persistence-training /app/kypo-persistence-training
COPY kypo-service-training /app/kypo-service-training
COPY $PROJECT_ARTIFACT_ID /app/$PROJECT_ARTIFACT_ID
# Build JAR file
RUN mvn clean install -DskipTests -Dproprietary-repo-url=$PROPRIETARY_REPO_URL && \
    cp /app/$PROJECT_ARTIFACT_ID/target/$PROJECT_ARTIFACT_ID-*.jar /app/kypo-rest-training.jar

############ RUNNABLE STAGE ############
FROM openjdk:11-jre-slim AS runnable
WORKDIR /app
COPY /etc/training.properties /app/etc/training.properties
COPY entrypoint.sh /app/entrypoint.sh
COPY --from=build /app/kypo-rest-training.jar ./
RUN apt-get update && \
    # Required to use nc command in the wait for it function, see entrypoint.sh
    apt-get install -y netcat && \
    # Make a file executable
    chmod a+x entrypoint.sh
EXPOSE 8083
CMD ["./entrypoint.sh"]
