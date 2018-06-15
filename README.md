# KYPO2 Trainings
This is parent project representing back-end for Trainings. It is divided into several modules as [REST] (https://gitlab.ics.muni.cz/kypo2/services-and-portlets/kypo2-training/tree/master/kypo2-rest-training), [facade] (https://gitlab.ics.muni.cz/kypo2/services-and-portlets/kypo2-training/tree/master/kypo2-facade-training), [service] (https://gitlab.ics.muni.cz/kypo2/services-and-portlets/kypo2-training/tree/master/kypo2-service-training), [persistence] (https://gitlab.ics.muni.cz/kypo2/services-and-portlets/kypo2-training/tree/master/kypo2-persistence-training), [Elasticsearch] (https://gitlab.ics.muni.cz/kypo2/services-and-portlets/kypo2-training/tree/master/kypo2-elasticsearch-training). 
The main project is [REST module] (https://gitlab.ics.muni.cz/kypo2/services-and-portlets/kypo2-training/tree/master/kypo2-rest-training).

## Authors

Role         | UCO          | Name 
------------ | ------------ | -------------
Developer    |   441048     | Šeda Pavel


## Starting up Project (NOT implemented yet)
You don't have to install Java, Maven, Tomcat, Elasticsearch, etc. to your local development environment. All you need is [Vagrant installed] (http://docs.ansible.com/ansible/latest/guide_vagrant.html).

```
vagrant up
```

Now check that documentation on the following section is reachable. 
NOTE: This command will also insert some testing data to data storages.

## Documentation 
Documentation is done in Swagger framework. It is possible to reach it on the following page:

```
~/kypo2-rest-training/swagger-ui.html
```

e.g. on localhost it should be:

```
http://localhost:8080/kypo2-rest-training/swagger-ui.html
```

NOTE: please note that client for that REST API could be generated using [Swagger codegen] (https://swagger.io/tools/swagger-codegen/). It is crucial to annotate each RestController method properly!
# Following steps are necessary for manual starting up project

## Database migration
Prerequisities running PostgreSQL and created database named 'training' with schema 'public'.
To migrate database data it is necessary to run these two scripts:

```
$ mvn flyway:migrate -Djdbc.url=jdbc:postgresql://{url to DB}/training -Djdbc.username={username in DB} -Djdbc.password={password to DB}
```
e.g.:
```
$ mvn flyway:migrate -Djdbc.url=jdbc:postgresql://localhost:5432/training -Djdbc.username=postgres -Djdbc.password=postgre

```

NOTE: This script must be run in [kypo2-training-persistence] (https://gitlab.ics.muni.cz/kypo2/services-and-portlets/kypo2-training/tree/master/kypo2-persistence-training) module.

## Application properties
```
<!-- For description of each field check: https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html -->

<!-- DATASOURCE (DataSourceAutoConfiguration & DataSourceProperties) -->
spring.datasource.url=jdbc:postgresql://localhost:5432/training 
spring.datasource.username=postgres
spring.datasource.password=postgre
spring.datasource.driver-class-name=org.postgresql.Driver

<!-- JPA (JpaBaseConfiguration, HibernateJpaAutoConfiguration) -->
spring.data.jpa.repositories.enabled=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL9Dialect
spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true 
spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults = false

<!-- Elasticsearch -->
elasticsearch.ipaddress=localhost
elasticsearch.protocol=http
elasticsearch.port1=9200
elasticsearch.port2=9201

<!-- context path -->
server.servlet.context-path=/kypo2-rest-training/api/v1
server.port=8080

<!-- Jackson (e.g. converting Java 8 dates to ISO format -->
spring.jackson.serialization.write_dates_as_timestamps=false 


spring.jmx.enabled = false
```

## Installing project
Installing by maven:

```
mvn clean install
```

## Used Technologies
The project was built and tested with these technologies, so if you have any unexpected troubles let us know.

```
Maven         : 3.3.9
Java          : 1.8.0_144, vendor: Oracle Corporation
Spring boot   : 2.0.2.RELEASE
Swagger       : 2.7.0
Hibernate     : 5.2.8.Final
Jackson       : 2.9.0
Tomcat        : 8
PostgreSQL    : 9.5
Elasticsearch : 5.5.0
```