FROM eclipse-temurin:17-jre-alpine
RUN apk add git

FROM maven:3.9.4-eclipse-temurin-17

COPY . /opt/sandbox/history
COPY src/main/java/eu/tib/ontologyhistory/contodiff/ContoDiff-1.0-SNAPSHOT-shaded.jar ContoDiff-1.0-SNAPSHOT-shaded.jar
COPY src/main/java/eu/tib/ontologyhistory/contodiff/rules/ChangeActions.xml /rules/ChangeActions.xml
COPY src/main/java/eu/tib/ontologyhistory/contodiff/rules/Rule_OBO.xml /rules/Rule_OBO.xml
RUN cd /opt/sandbox/history && ls && mvn clean package -DskipTests
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

