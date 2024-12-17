FROM eclipse-temurin:17-jre-alpine
RUN apk --no-cache add git

FROM maven:3.9.4-eclipse-temurin-17

COPY . /opt/sandbox/history
COPY ./lib/ContoDiff-1.0-SNAPSHOT-shaded.jar ./opt/sandbox/history/ContoDiff-1.0-SNAPSHOT-shaded.jar
COPY ./lib/rules/ChangeActions.xml ./opt/sandbox/history/rules/ChangeActions.xml
COPY ./lib/rules/Rule_OBO.xml ./opt/sandbox/history/rules/Rule_OBO.xml
WORKDIR /opt/sandbox/history
RUN <<EOF
  ls
  mvn install:install-file \
      -Dfile=ContoDiff-1.0-SNAPSHOT-shaded.jar \
      -DgroupId=org.dbs.leipzig \
      -DartifactId=conto \
      -Dversion=1.0 \
      -Dpackaging=jar \
      -DgeneratePom=true
  mvn clean package -DskipTests
EOF
ENTRYPOINT ["java","-Xmx5G", "-Xms1024m","-jar","/opt/sandbox/history/target/history-back.jar"]

#Can be used if maven is installed on the system - will reuse installed dependencies,
#so there's no need to install them each time.

#FROM eclipse-temurin:17-jre-alpine
#
#RUN apk add git
#
#COPY target/history-back.jar history-back.jar
#COPY src/main/java/eu/tib/ontologyhistory/contodiff/ContoDiff-1.0-SNAPSHOT-shaded.jar ContoDiff-1.0-SNAPSHOT-shaded.jar
#COPY src/main/java/eu/tib/ontologyhistory/contodiff/rules/ChangeActions.xml /rules/ChangeActions.xml
#COPY src/main/java/eu/tib/ontologyhistory/contodiff/rules/Rule_OBO.xml /rules/Rule_OBO.xml
#
#CMD ["java","-Xmx5G", "-Xms1024m","-jar","/history-back.jar"]

