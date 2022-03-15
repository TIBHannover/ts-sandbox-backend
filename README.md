# Terminology Service Statistics

### System requirements
***
* Java Runtime Environment 11
* Maven
* Docker

### How to run
***
To start a project run following commands in a project directory:
1. `mvn clean install` to build project
2. `docker-compose build` to build containers
3. `docker-compose up -d` create and start the containers in the background and leave them running

Terminology Service Statistics runs on port 8081

API documentation: [swagger](http://localhost:8083/swagger-ui/)

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

### How to develop
***

#### Common flow for the implementation of new features or bug fixes.

* create a new issue, tagging it accordingly and assigning it
* create Merge Request (MR) from that issue
* use the MR created branch to create a local branch
* make your commits to that branch and push to remote branch
* (if any) trigger the CI/CD pipeline to test new developments on the test server
* if the pipeline runs successfully the MR can be marked as ready
* delete source branch should be selected, so that merged branches are not left in the remote
* merge to the master branch

#### Frontend service [ts frontend 2.0 nfdi4chem](https://git.tib.eu/terminology/sandbox/nfdi4chem-ts)

- create new tab
    - create new component in a new folder
    - add new rout to `src/App.js`
    - add new link to `src/components/common/navbar/Navbar.jsx`

#### Backend Terminology Service Statistics (current service)

* this service provides with content for `Analytics` tab

#### Backend service [ols-backend-2.0-poc](https://git.tib.eu/terminology/sandbox/ols-backend-2.0-poc)

* this service provides with content for `Ontologies` tab 





