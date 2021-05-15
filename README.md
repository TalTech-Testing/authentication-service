### Arete backend

Backend API documentation is available at: (cs.ttu.ee)[https://cs.ttu.ee/services/arete/api/v2/swagger-ui/index.html?configUrl=/services/arete/api/v2/docs/swagger-config] 

Extra documentation at [gitlab pages](https://ained.pages.taltech.ee/it-doc/arete/index.html#)

# Running locally

Prerequisites:

- Java 11+
- Docker
- docker-compose

Running tests:

if Windows
````cmd
mvnw.cmd test
````
else
````cmd
./mvnw test
````

Running the application:

if Windows
````cmd
docker-compose up -d
mvnw.cmd package
java -jar target/authentication_service-1.0.jar
````
else
````cmd
docker-compose up -d
./mvnw package
java -jar target/authentication_service-1.0.jar
````
