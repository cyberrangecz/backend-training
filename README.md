# KYPO Training
This project represents back-end for managing users, groups and roles in KYPO platform.

## Content

1.  Requirements
2.  Project modules

### 1. Requirements
The requirements for manual installation of the project, building and running the project in docker container and so on are available on the following [WIKI](https://gitlab.ics.muni.cz/kypo2/services-and-portlets/kypo2-training/wikis/home) page.

### 2. Project Modules
This project is containes several modules. 
It is divided into several modules as:
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
  
