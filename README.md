# Terminology Service Sandbox Backend

* DEV Server: ols4ing01.develop.service.tib.eu
* Prod Server: ols4ing21.service.tib.eu
* Public URLs : https://service.tib.eu/sandbox/history/swagger-ui/index.html#/ and https://service.tib.eu/sandbox/mappings/swagger-ui/index.html#/

### System requirements
***
* Docker and Docker Compose

### application versions and ports

* mongo version 3.6.23
* mongo running on port 27017
* statistics-module is running on port 9191
* history-module is running on port 9090
* spring boot version 3.1.2

### How to run
***
To start a project run following commands in a project directory:

1. `mvn clean install` to build modules as jars
2. `docker-compose up --force-recreate --build -d` to build, create and start the containers in the background and leave them running

Terminology Service Statistics runs on port 9191: `http://localhost:9191`  

Semantic Diff Service runs on port 9090: `http://localhost:9090`

API documentation for statistics module: [swagger-statistics](http://localhost:9191/swagger-ui/index.html)

API documentation for history module: [swagger-history](http://localhost:9090/swagger-ui/index.html)


## How to develop
***

#### Backend Terminology Service Statistics (current service)

* this service provides with content for `Analytics` tab

#### (Optional) Backend service [ols-backend-2.0-poc](https://git.tib.eu/terminology/sandbox/ols-backend-2.0-poc)

* this service provides with content for `Ontologies` tab 
