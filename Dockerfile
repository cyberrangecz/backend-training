FROM maven:3.6.2-jdk-11-slim AS build

## default environment variables for database settings
ARG USERNAME=postgres
ARG PASSWORD=postgres
ARG POSTGRES_DB=training
ARG PROJECT_ARTIFACT_ID=kypo2-rest-training

## default link to proprietary repository, e.g., Nexus repository
ARG PROPRIETARY_REPO_URL=YOUR-PATH-TO-PROPRIETARY_REPO

# install
RUN apt-get update && apt-get install -y supervisor postgresql rsyslog netcat

# configure supervisor
RUN mkdir -p /var/log/supervisor

# configure postgres
RUN /etc/init.d/postgresql start && \
    su postgres -c "createdb -O \"$USERNAME\" $POSTGRES_DB" && \
    su postgres -c "psql -c \"ALTER USER $USERNAME PASSWORD '$PASSWORD';\"" && \
    /etc/init.d/postgresql stop

# copy only essential parts
COPY /etc/training.properties /app/etc/training.properties
COPY supervisord.conf /app/supervisord.conf
COPY entrypoint.sh /app/entrypoint.sh
COPY pom.xml /app/pom.xml
COPY kypo2-api-training /app/kypo2-api-training
COPY kypo2-elasticsearch-training /app/kypo2-elasticsearch-training
COPY kypo2-persistence-training /app/kypo2-persistence-training
COPY kypo2-service-training /app/kypo2-service-training
COPY $PROJECT_ARTIFACT_ID /app/$PROJECT_ARTIFACT_ID

# build training
WORKDIR /app
RUN mvn clean install -DskipTests -Dproprietary-repo-url=$PROPRIETARY_REPO_URL && \
    cp /app/$PROJECT_ARTIFACT_ID/target/$PROJECT_ARTIFACT_ID-*.jar /app/kypo-rest-training.jar && \
    chmod a+x entrypoint.sh

EXPOSE 8083
ENTRYPOINT ["./entrypoint.sh"]