#FROM maven:3.9.4-eclipse-temurin-17
#
#COPY . /opt/sandbox/history
#RUN cd /opt/sandbox/history && ls && mvn clean package -DskipTests
#
#EXPOSE 9292
#ENTRYPOINT ["java","-Xmx5G", "-Xms1024m","-jar","/opt/sandbox/history/target/history-back.jar"]

FROM eclipse-temurin:17-jre-alpine
COPY target/history-back.jar history-back.jar
CMD ["java","-Xmx5G", "-Xms1024m","-jar","/history-back.jar"]

