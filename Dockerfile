FROM adoptopenjdk:11-jre-hotspot
RUN mkdir /app

WORKDIR /app

EXPOSE 8080

ENV APP_PROP=""

COPY target/ts-statistics-back.jar /app/ts-statistics-back.jar

CMD ["/app/ts-statistics-back.jar"]