# Terminology Service Statistics

### System requirements
***
* Java Runtime Environment 8
* Maven
* Docker

### application versions and ports

* mongo version 3.4.24
* mongo running on port 27017
* application is running on port 9191
* spring boot version 2.4.0

### How to run
***
To start a project run following commands in a project directory:
1. `mvn clean install` to build project `mvn clean install -DskipTests` to build project skipping the tests
2. `docker-compose build` to build containers
3. `docker-compose up -d` create and start the containers in the background and leave them running

Terminology Service Statistics runs on port 9191: `http://localhost:9191`

API documentation: [swagger](http://localhost:9191/swagger-ui/)

### Questions to be answered by statistic
***

#### How many ontologies share this property?
`/api/ontology/similarity/property/{name}`

#### Which properties are shared within a set of ontologies?
`/api/ontology/similarity/property/internal`

#### Which namespaces are shared within a set of ontologies?
`/api/ontology/similarity/namespace/internal`

#### Which classes are shared within a set of ontologies?
`/api/ontology/similarity/class/internal`

#### Which imports are shared within a set of ontologies?
`/api/ontology/similarity/import/internal`

#### What is a commonly used ontology in the collection XYZ?
`/api/ontology/similarity/collection/{name}`

## How to develop
***

#### Backend Terminology Service Statistics (current service)

* this service provides with content for `Analytics` tab

#### (Optional) Backend service [ols-backend-2.0-poc](https://git.tib.eu/terminology/sandbox/ols-backend-2.0-poc)

* this service provides with content for `Ontologies` tab 