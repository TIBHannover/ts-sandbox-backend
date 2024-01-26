FROM maven:3.9.4-eclipse-temurin-17

RUN mkdir -p /opt/sandbox/tiva
COPY . /opt/sandbox/tiva
RUN cd /opt/sandbox/tiva && ls && mvn clean package -DskipTests

#EXPOSE 8080
ENTRYPOINT ["java", "-Xmx5G", "-Xms1024m", "-jar", "/opt/sandbox/tiva/target/tiva-statistics-back.jar"]

#FROM eclipse-temurin:17-jre-alpine
#COPY target/tiva-statistics-back.jar tiva-statistics-back.jar
#CMD ["java","-Xmx5G", "-Xms1024m","-jar","/tiva-statistics-back.jar"]

