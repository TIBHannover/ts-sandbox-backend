# Terminology Service Statistics

### How to run
***
To start a project run following commands in a project directory:
1. `mvn clean install` to build project
2. `docker-compose up -d` to build, create and start the containers in the background and leave them running


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


