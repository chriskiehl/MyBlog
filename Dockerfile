FROM clojure:temurin-17-lein-2.10.0-alpine
COPY . /usr/src/clj/myblog
WORKDIR /usr/src/clj/myblog
RUN ["lein", "uberjar"]


FROM amazoncorretto:latest
WORKDIR /
COPY --from=0 /usr/src/clj/myblog/target/myblog-0.1.0-SNAPSHOT-standalone.jar myblog-0.1.0-SNAPSHOT-standalone.jar
EXPOSE 8080
CMD java -jar myblog-0.1.0-SNAPSHOT-standalone.jar