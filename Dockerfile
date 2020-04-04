FROM maven:3.6.2-jdk-11-slim AS build

## default environment variables for database settings
ARG USERNAME=postgres
ARG PASSWORD=postgres
ARG POSTGRES_DB=training
ARG PROJECT_ARTIFACT_ID=kypo2-rest-training

## default link to proprietary repository, e.g., Nexus repository
ARG PROPRIETARY_REPO_URL=YOUR-PATH-TO-PROPRIETARY_REPO

COPY ./ /app
WORKDIR /app
RUN echo $PROPRIETARY_REPO_URL && mvn clean install -DskipTests -Dproprietary-repo-url=$PROPRIETARY_REPO_URL
RUN apt-get update && apt-get install -y supervisor postgresql rsyslog

WORKDIR /app/kypo2-persistence-training
RUN /etc/init.d/postgresql start &&\
    su postgres -c "createdb -O \"$USERNAME\" $POSTGRES_DB" &&\
    su postgres -c "psql -c \"ALTER USER $USERNAME PASSWORD '$PASSWORD';\""

RUN /etc/init.d/postgresql start &&\
    mvn flyway:migrate -Djdbc.url="jdbc:postgresql://localhost:5432/$POSTGRES_DB" -Djdbc.username="$USERNAME" -Djdbc.password="$PASSWORD" &&\
    mkdir -p /var/log/supervisor &&\
    cp /app/supervisord.conf /etc/supervisor/supervisord.conf &&\
    cp /app/$PROJECT_ARTIFACT_ID/target/$PROJECT_ARTIFACT_ID-*.jar /app/kypo-rest-training.jar

WORKDIR /app
EXPOSE 8083
ENTRYPOINT ["/usr/bin/supervisord", "-c", "/etc/supervisor/supervisord.conf"]
