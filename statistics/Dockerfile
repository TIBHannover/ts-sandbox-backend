FROM maven:3.9.4-eclipse-temurin-17

RUN mkdir -p /opt/sandbox/statistics && mkdir /opt/sandbox/history
COPY . /opt/sandbox/statistics
COPY ./lib/logmap-matcher-4.0.jar /root/.m2/repository/uk/ox/logmap/logmap-matcher/4.0/logmap-matcher-4.0.jar
COPY ./lib/google-api-translate-java-0.97.jar /root/.m2/repository/com/google/api/google-api-translate-java/0.97/google-api-translate-java-0.97.jar
RUN cd /opt/sandbox/statistics && mvn clean package -DskipTests

#EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Xmx5G -Xms1024m -cp '/opt/sandbox/statistics/target/ts-statistics-back.jar:/opt/sandbox/statistics/lib/*' org.springframework.boot.loader.JarLauncher"]

#FROM eclipse-temurin:17-jre-alpine
#COPY target/ts-statistics-back.jar ts-statistics-back.jar
#CMD ["java","-Xmx5G", "-Xms1024m","-jar","/ts-statistics-back.jar"]
