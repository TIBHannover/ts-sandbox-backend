FROM adoptopenjdk:11-jre-hotspot
COPY target/ts-statistics-back.jar ts-statistics-back.jar
CMD ["java","-Xmx5G", "-Xms1024m","-jar","/ts-statistics-back.jar"]