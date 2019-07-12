# KYPO2 Trainings
This is parent project representing back-end for Training. 

It is divided into several modules as:
* `kypo2-rest`
  * Provides REST layer for communication with front-end.
  * Based on HTTP REST without HATEOAS.
  * Documented with Swagger.
* `kypo2-facade`
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
  

## Authors

Role         | UCO          | Name 
------------ | ------------ | -------------
Lead Developer    |   441048     | Šeda Pavel
Developer    |   445537     | Dominik Pilár
Developer    |   445343     | Boris Jaduš


## Starting up Project (NOT implemented yet)
You don't have to install Java, Maven, Tomcat, Elasticsearch, etc. to your local development environment. All you need is [Vagrant installed] (http://docs.ansible.com/ansible/latest/guide_vagrant.html).

```
vagrant up
```

Now check that documentation on the following section is reachable. 
NOTE: This command will also insert some testing data to data storages.

## Documentation 
Documentation is done in the Swagger framework. It is possible to reach it on the following page:

```
~/kypo2-rest-training/api/v1/swagger-ui.html
```

e.g. on localhost it should be:

```
https://localhost:8080/kypo2-rest-training/api/v1/swagger-ui.html
```

NOTE: please note that client for that REST API could be generated using [Swagger codegen] (https://swagger.io/tools/swagger-codegen/). It is crucial to annotate each RestController method properly!

# Following steps are necessary for manual starting up project

## 1. Getting Masaryk University OpenID Connect credentials 

1. Go to `https://oidc.muni.cz/oidc/` and log in.
2. Click on "**Self-service Client Registration**" -> "**New Client**".
3. Set Client name.
4. Add at least one custom Redirect URI and `https://localhost:8080/{context path from external properties file}/webjars/springfox-swagger-ui/oauth2-redirect.html` (IMPORTANT for Swagger UI).
5. In tab "**Access**":
    1. choose which information about the user you will be getting, so-called `scopes`.
    2. select just *implicit* in **Grand Types**
    3. select *token* and *code id_toke* in **Responses Types**
6. Hit **Save** button.
7. Then got to tab "**JSON**", copy the JSON file and save it to file. **IMPORTANT STEP**
8. Now create new Resource in "**Self-service Protected Resource Registration**".
9. Again insert client Name and save JSON to an external file in "**JSON**" tab.
10. In tab "**Access**" again choose which information about the user you will be getting, so-called `scopes`.
11. Hit **Save** button.

## 2. Generate CA for project 
Use 'keytool' to generate KeyStore for client:

```
keytool -genkeypair -alias {alias of KeyStore} -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore {filename of KeyStore}.p12 -validity 3650
```
It will create file with private key, public key and certificate for client. During generating the KeyStore, use for 'first name and last name' domain name of server where your application is running, e.g., localhost.
  
Then export certificate from KeyStore to create *.crt file:
```
keytool -export -keystore {filename of created KeyStore}.p12 -alias {alias of certificate} -file {filename of certificate}.crt
```

After that import exported certificate into TrustStore:
```
keytool -importcert -trustcacerts -file {path to exported certificate} -alias {alias of exported certificate} -keystore {path to your TrustStore}
```

To remove certificate from TrustStore:
```
keytool -delete -alias {alias of certificate} -keystore {path to TrustStore}
```

To show all certificates in TrustStore: 
```
keytool -list -v -keystore {path to TrustStore}
```

For more information about 'How to enable communication over https between 2 spring boot applications using self signed certificate' visit http://www.littlebigextra.com/how-to-enable-communication-over-https-between-2-spring-boot-applications-using-self-signed-certificate


## 3. Create a property file with necessary configuration details
After the previous steps you have to create properties file according to format shown in [kypo2 training property file](kypo2-training.properties) and save it. 

## 4. Installing project and database migration
Installing by maven:

```
mvn clean install
```
NOTE: Before installing this project you must download (git clone) and install (mvn clean install) [security-commons project] (https://gitlab.ics.muni.cz/kypo2/services-and-portlets/kypo2-security-commons)
NOTE: To skip integration tests use -DskipITs 
### Database migration
Prerequisites running PostgreSQL and created the database named 'training' with schema 'public'.
To migrate database data it is necessary to run these two scripts:

```
$ mvn flyway:migrate -Djdbc.url=jdbc:postgresql://{url to DB}/training -Djdbc.username={username in DB} -Djdbc.password={password to DB}
```
e.g.:
```
$ mvn flyway:migrate -Djdbc.url=jdbc:postgresql://localhost:5432/training -Djdbc.username=postgres -Djdbc.password=postgres

```

NOTE: This script must be run in [kypo2-training-persistence] (https://gitlab.ics.muni.cz/kypo2/services-and-portlets/kypo2-training/tree/master/kypo2-persistence-training) module.

### 5. Configuration of HTTPS

#### Generating a Self-Signed Certificate
Here, we use the following certificate format:
1. PKCS12: Public Key Cryptographic Standards is a password protected format that can contain multiple certificates and keys; it’s an industry-wide used format

We use keytool to generate the certificates from the command line. Keytool is shipped with Java Runtime Environment.

##### Generating a Keystore
Now we’ll create a set of cryptographic keys and store it in a keystore.
We can use the following command to generate our PKCS12 keystore format:
```
$ keytool -genkeypair -alias seda -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore seda.p12 -validity 3650

```

##### Configuring SSL Properties
Now, we’ll configure the SSL related properties:

```
# The format used for the keystore
server.ssl.key-store-type=PKCS12
# The path to the keystore containing the certificate
server.ssl.key-store=classpath:keystore/seda.p12
# The password used to generate the certificate
server.ssl.key-store-password=password
# The alias mapped to the certificate
server.ssl.key-alias=seda

security.require-ssl=true
```

##### Invoking an HTTPS URL
Now that we have enabled HTTPS in our application, let’s move on into the client and let’s explore how to invoke an HTTPS endpoint with the self-signed certificate.
```
#trust store location
trust.store=classpath:keystore/seda.p12
#trust store password
trust.store.password=password
```

### Run project
In Intellij Idea:
1. Click on "**Run**" -> "**Edit configurations**".
2. Choose "**WebConfigRestTraining**" configuration.
3. Add into "**Program arguments**" --path.to.config.file="{path to your config properties}".
4. Run WebConfigRestTraining

In command line:
You have to go to module `kypo2-rest-training` and start it:
```
cd kypo2-rest-training/
mvn spring-boot:run -Dpath-to-config-file={path to properties file from step 2}
```

## Clover
Should be used for testing purposes only, before every merge perform:
```
$ mvn clean install
```
To instrument code and create clover snapshot use:
```
$ mvn clover:setup clover:snapshot
```  
To run optimized build use:
```
$ mvn clover:optimize clean install -Dansi.color=true
```
-Dansi.color does not work on all OS and is not necessary, it only prints info about time saving in color.


# Following part is for informative purposes (documentation, technologies, and installing particular technologies)

## ER Diagram
ER diagram generated from model in persistence module
![alt text](doc-files/kypo2-training-ERD.png)

## Used Technologies
The project was built and tested with these technologies, so if you have any unexpected troubles let us know.

```
Maven         : 3.3.9
Java          : OpenJDK 11
Spring Boot   : 2.1.1.RELEASE
Swagger       : 2.9.2
Hibernate     : 5.3.7.Final
Jackson       : 2.9.7
Tomcat        : 9
PostgreSQL    : 11
Elasticsearch : 5.5.0
```

## Installing technologies on Debian based system
### Installing Java
Follow the steps in the following link: https://dzone.com/articles/installing-openjdk-11-on-ubuntu-1804-for-real

Verify the installation:
```
$ java -version
```

Do not forget to set OpenJDK in IntelliJ IDEA in all the places. The tutorial for setting OpenJDK in IntelliJ IDEA in all the places is shown in the following link: [https://stackoverflow.com/a/26009627/2892314].
### Installing Maven
```
$ apt-cache search maven
$ sudo apt-get install maven
```
Verify the installation:
```
$ mvn -v
```

### PostgreSQL installation 
Follow the steps in the following link: [https://www.digitalocean.com/community/tutorials/how-to-install-and-use-postgresql-on-ubuntu-16-04]
