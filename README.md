# KYPO Training
This project represents back-end for managing trainings in KYPO platform.

## Content

1.  Project Modules
2.  Build and Start the Project Using Docker

### 1. Project Modules
This project is divided into several modules:
* `kypo2-rest`
  * Provides REST layer for communication with front-end.
  * Based on HTTP REST without HATEOAS.
  * Documented with Swagger.
* `kypo2-api`
  * Contains API (DTO classes)
    * These are annotated with proprietary converters for DateTime processing.
    * Localized Bean validations are set (messages are localized).
    * Annotations for Swagger documentation are included.
  * Map Entities to DTO classes and vice versa with MapStruct framework.
  * Contains @Transactional annotations.
* `kypo2-service`
    * Provides business logic of the application:
      * Calls persistence layer for database queries and combining the results as necessary.
      * Calls another microservices.
* `kypo2-persistence`
  * Provides data layer of the application (Database queries).
  * Uses Spring Data JPA (Spring wrapper layer over JPA implemented with Hibernate framework).
  * Communicates with PostgreSQL database.
  * Uses QueryDSL for filtering the data.
* `kypo2-elasticsearch`
  * Used for auditing and retrieving data from Elasticsearch storage.
  * Contains Event classes describing particular events.

And the main project (parent maven project with packaging pom):
* `kypo2-training`
  * Contains configurations for all modules as dependency versions, dependency for spring boot parent project etc.
  


### Build and Start the Project Using Docker

#### Prerequisities
Install the following technology:

Technology       | URL to Download
---------------- | ------------
Docker           | https://docs.docker.com/install/

#### 1. Preparation of Configuration Files
To build and run the project in docker it is necessary to prepare several configurations.

* Set the [OpenID Connect configuration](https://docs.crp.kypo.muni.cz/installation-guide/setting-up-oidc-provider/) which is available on the provided hyperlink.

* Fill OIDC credentials gained from the previous step and set additional settings in the [training.properties](https://gitlab.ics.muni.cz/muni-kypo-crp/backend-java/kypo2-training/-/blob/master/etc/training.properties) file and save it.

#### 2. Build Docker Image
In the project root folder (folder with Dockerfile), run the following command:
```
sudo docker build -t {docker image name} .
```

e.g.:
```
sudo docker build -t training-image .
```

Dockefile contains several default arguments:
* USERNAME=postgres - the name of the user to connect to the database. 
* PASSWORD=postgres - user password.
* POSRGRES_DB=training - the name of the created database.
* PROJECT_ARTIFACT_ID=kypo2-rest-training - the name of the project artifact.
* PROPRIETARY_REPO_URL=YOUR-PATH-TO-PROPRIETARY_REPO.

Those arguments can be overwritten during the build of the image, by adding the following option for each argument: 
```bash
--build-arg {name of argument}={value of argument} 
``` 

e.g.:
```bash
sudo docker build --build-arg PROPRIETARY_REPO_URL=https://nexus.csirt.muni.cz/repository/kypo-maven-group/ -t training-image .
```

#### 3. Start the Project
Start the project by running docker container, but at first make sure that your ***OIDC Provider*** and [kypo2-user-and-group](https://gitlab.ics.muni.cz/muni-kypo-crp/backend-java/kypo2-user-and-group) service is running. Instead of usage of the PostgreSQL database, you can use the in-memory database H2. It just depends on the provided configuration. To run a docker container, run the following command: 

```
sudo docker run --name {container name} --network host -it -p {port in host}:{port in container} {docker image name}
```
e.g. with this command:
```
sudo docker run --name training-container --network host -it -p 8083:8083 training-image 
```

To create a backup for your database add the following docker option:
```
-v db_data_uag:/var/lib/postgresql/11/main/
