FROM adoptopenjdk:11-jre-hotspot
COPY target/ts-statistics-back.jar ts-statistics-back.jar
ENTRYPOINT ["java","-jar","/ts-statistics-back.jar"]