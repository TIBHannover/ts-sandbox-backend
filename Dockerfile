#FROM adoptopenjdk:11-jre-hotspot
#COPY target/ts-statistics-back.jar ts-statistics-back.jar
#CMD ["java","-Xmx5G", "-Xms1024m","-jar","/ts-statistics-back.jar"]


FROM maven:3.6.3-adoptopenjdk-14

RUN mkdir /opt/sandbox
COPY . /opt/sandbox/
RUN cd /opt/sandbox && ls && mvn clean package -DskipTests

EXPOSE 9191
ENTRYPOINT ["java", "-Xmx5G", "-Xms1024m", "-jar", "/opt/sandbox/target/ts-statistics-back.jar"]
