FROM openjdk:8u181-alpine3.8

WORKDIR /

COPY target/myblog-0.1.0-SNAPSHOT-standalone.jar myblog-0.1.0-SNAPSHOT-standalone.jar
EXPOSE 8080

CMD java -jar myblog-0.1.0-SNAPSHOT-standalone.jar