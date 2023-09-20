#FROM maven:3.9.4-eclipse-temurin-17
#
#RUN mkdir -p /opt/sandbox/statistics && mkdir /opt/sandbox/history
#COPY . /opt/sandbox/statistics
#RUN cd /opt/sandbox/statistics && ls && mvn clean package -DskipTests
#
#EXPOSE 9191
#ENTRYPOINT ["java", "-Xmx5G", "-Xms1024m", "-jar", "/opt/sandbox/statistics/target/ts-statistics-back.jar"]

FROM eclipse-temurin:17-jre-alpine
COPY target/ts-statistics-back.jar ts-statistics-back.jar
CMD ["java","-Xmx5G", "-Xms1024m","-jar","/ts-statistics-back.jar"]

