FROM maven:3.9.4-eclipse-temurin-17

RUN mkdir -p /opt/sandbox/statistics && mkdir /opt/sandbox/history
COPY . /opt/sandbox/statistics
COPY ./lib/logmap-matcher-4.0.jar /root/.m2/repository/uk/ox/logmap/logmap-matcher/4.0/
COPY ./lib/google-api-translate-java-0.97.jar /root/.m2/repository/com/google/api/google-api-translate-java/0.97/
RUN cd /opt/sandbox/statistics && ls && mvn clean package -DskipTests
#RUN cd /opt/sandbox/statistics

#EXPOSE 8080
ENTRYPOINT ["java", "-Xmx5G", "-Xms1024m", "-jar", "/opt/sandbox/statistics/target/ts-statistics-back.jar"]

#FROM eclipse-temurin:17-jre-alpine
#COPY target/ts-statistics-back.jar ts-statistics-back.jar
#CMD ["java","-Xmx5G", "-Xms1024m","-jar","/ts-statistics-back.jar"]

