# CyberRangeCZ Platform Training
This project represents back-end for managing trainings in CyberRangeCZ platform.

## Content

1.  Project Modules
2.  Build and Start the Project Using Docker

### 1. Project Modules
This project is divided into several modules:
* `training-rest`
  * Provides REST layer for communication with front-end.
  * Based on HTTP REST without HATEOAS.
  * Documented with Swagger.
* `training-api`
  * Contains API (DTO classes)
    * These are annotated with proprietary converters for DateTime processing.
    * Localized Bean validations are set (messages are localized).
    * Annotations for Swagger documentation are included.
  * Map Entities to DTO classes and vice versa with MapStruct framework.
  * Contains @Transactional annotations.
* `training-service`
    * Provides business logic of the application:
      * Calls persistence layer for database queries and combining the results as necessary.
      * Calls another microservices.
* `training-persistence`
  * Provides data layer of the application (Database queries).
  * Uses Spring Data JPA (Spring wrapper layer over JPA implemented with Hibernate framework).
  * Communicates with PostgreSQL database.
  * Uses QueryDSL for filtering the data.
* `training-opensearch`
  * Used for auditing and retrieving data from OpenSearch storage.
  * Contains Event classes describing particular events.

And the main project (parent maven project with packaging pom):
* `training`
  * Contains configurations for all modules as dependency versions, dependency for spring boot parent project etc.


### Build and Start the Project Using Docker

#### Prerequisites
Install the following technology:

Technology       | URL to Download
---------------- | ------------
Docker           | https://docs.docker.com/install/

#### 1. Preparation of Configuration Files
To build and run the project in docker it is necessary to prepare several configurations.

* Set the [OpenID Connect configuration](https://docs.platform.cyberrange.cz/installation-guide/setting-up-oidc-provider/) which is available on the provided hyperlink.

* Fill OIDC credentials gained from the previous step and set additional settings in the [training.properties](https://github.com/cyberrangecz/backend-training/blob/master/etc/training.properties) file and save it.

* By default, the provided configuration uses the in-memory H2 database. To use PostgreSQL instead, point the `spring.datasource.*` properties in `training.properties` to your PostgreSQL instance (run as its own, separate service/container).

* This service calls out to other CyberRangeCZ Platform microservices, configured via the following properties in `training.properties`:
  * `user-and-group-server.uri` &mdash; [backend-user-and-group](https://github.com/cyberrangecz/backend-user-and-group)
  * `sandbox-service.uri` &mdash; [backend-sandbox-service](https://github.com/cyberrangecz/backend-sandbox-service)
  * `answers-storage.uri` &mdash; [backend-answers-storage](https://github.com/cyberrangecz/backend-answers-storage)
  * `opensearch.host` / `opensearch.port` &mdash; OpenSearch instance used for storing and querying training events

#### 2. Build Docker Image
In the project root folder (folder with Dockerfile), run the following command:
```shell
$ sudo docker build \
  -t training-image \
  .
```

The Dockerfile accepts the following build arguments:
* PROJECT_ARTIFACT_ID=training - the name of the project artifact whose jar gets packaged and run.
* MAVEN_CLI_OPTS - extra options passed to the Maven build (e.g. `-s etc/ci_settings.xml`).

Those arguments can be overwritten during the build of the image, by adding the following option for each argument: 
```bash
--build-arg {name of argument}={value of argument} 
``` 

#### 3. Start the Project
Start the project by running docker container, but at first make sure that your ***OIDC Provider*** and the dependent services listed above (user-and-group, sandbox-service, answers-storage, OpenSearch, and PostgreSQL if configured) are running. To run a docker container, run the following command:
```shell
$  sudo docker run \
   --name training-container -it \
   --network host \
   -p 8083:8083 \
   training-image
```

Add the following option to use the custom property file:
```shell
-v {path to your config file}:/app/etc/training.properties
```

Add the following environment variable to wait for other services until they are up and running (space-separated `host:port` pairs):
```shell
-e SERVICE_PRECONDITION="localhost:8084 localhost:8080 localhost:8087"
```  
