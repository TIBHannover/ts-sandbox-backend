FROM maven:3.9.4-eclipse-temurin-17

RUN mkdir -p /opt/sandbox/qualityassessment
COPY . /opt/sandbox/qualityassessment
RUN cd /opt/sandbox/qualityassessment && ls && mvn clean package -DskipTests

#EXPOSE 8080
ENTRYPOINT ["java", "-Xmx5G", "-Xms1024m", "-jar", "/opt/sandbox/qualityassessment/target/qualityassessment-0.0.1-SNAPSHOT.jar"]