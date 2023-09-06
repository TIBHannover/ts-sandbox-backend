# Terminology Service Sandbox Backend

* Internally Hosted Sandbox Frontend: http://ols4ing01.develop.service.tib.eu/ts/sandbox
* Internally Hosted Sandbox Backend: http://ols4ing01.develop.service.tib.eu:9191/swagger-ui/index.html
* Public URL : not public

### System requirements
***
* Docker and Docker Compose

### application versions and ports

* mongo version 3.4.24
* mongo running on port 27017
* application is running on port 9191
* spring boot version 2.4.0

### How to run
***
To start a project run following commands in a project directory:

1. `docker-compose build` to build containers
2. `docker-compose up -d` create and start the containers in the background and leave them running

Terminology Service Statistics runs on port 9191: `http://localhost:9191`

API documentation: [swagger](http://localhost:9191/swagger-ui/index.html)

### Questions to be answered by Terminology Service Statistics for Pairwise Similarity
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

### Frontend Code Base using this Repository as Backend

* Frontend Repository: [code base](https://git.tib.eu/terminology/sandbox/tib-terminology-service-2-0-central-frontend)

### Backend Terminology Service Statistics (current service)

* this service provides with content for `Sandbox` tab in Terminology Service Frontend