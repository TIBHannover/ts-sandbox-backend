# Terminology Service Sandbox Backend

* Internally Hosted Sandbox Frontend: http://ols4ing01.develop.service.tib.eu/ts/sandbox
* Internally Hosted Sandbox Backend: http://ols4ing01.develop.service.tib.eu:9191/swagger-ui/index.html
* Public URL : not public

### System requirements
***
* Docker and Docker Compose

### application versions and ports

* mongo version 3.6.23
* mongo running on port 27017
* statistics-module is running on port 9191
* history-module is running on port 9292
* spring boot version 3.1.2

### How to run
***
To start a project run following commands in a project directory:

1. `docker-compose build` to build containers
2. `docker-compose up -d` create and start the containers in the background and leave them running

Terminology Service Statistics runs on port 9191: `http://localhost:9191`
Semantic Diff Service runs on port 9292: `http://localhost:9292`

API documentation for statistics module: [swagger-statistics](http://localhost:9191/swagger-ui/index.html)

API documentation for history module: [swagger-history](http://localhost:9292/swagger-ui/index.html)
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