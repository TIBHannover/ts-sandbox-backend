FROM maven:3.9.4-eclipse-temurin-17

COPY . /opt/sandbox/tiva
RUN cd /opt/sandbox/tiva && ls && mvn clean package -DskipTests

#EXPOSE 9090
ENTRYPOINT ["java","-Xmx5G", "-Xms1024m","-jar","/opt/sandbox/history/target/history-back.jar"]

#FROM eclipse-temurin:17-jre-alpine
#COPY target/history-back.jar history-back.jar
#CMD ["java","-Xmx5G", "-Xms1024m","-jar","/history-back.jar"]

