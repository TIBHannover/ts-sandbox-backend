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

### Setup folder paths
* In each source project folders,  **.env** file is created for the and **terminology-service-statistics** project. 
* In this file, the variables HOME_PATH_LINUX and HOME_PATH_WINDOWS are defined.
* Additionally, the path to the folder where the MongoDB data will be stored are specified. For example **HOME_PATH_LINUX**
variable may be set to **/home/terminology/mongodb-data**.
* In the **docker-compose.yml** filed located in each project folder, the following lines should be added under the 
**mongodb** service if the project is being run on Linux or on Windows machine. In the **volumes** section, the following 
line should be added **${HOME_PATH_LINUX/WINDOWS}:/data/db**.

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
