FROM clojure:temurin-17-lein AS build
WORKDIR /usr/src/clj/myblog

ENV JAVA_TOOL_OPTIONS="-Djava.net.preferIPv4Stack=true \
-Dmaven.wagon.http.retryHandler.count=10 \
-Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
-Dmaven.wagon.rto=120000"

COPY project.clj .
RUN lein deps
COPY . .
RUN lein uberjar

FROM amazoncorretto:17
WORKDIR /
COPY --from=build /usr/src/clj/myblog/target/myblog-0.1.0-SNAPSHOT-standalone.jar myblog-0.1.0-SNAPSHOT-standalone.jar
EXPOSE 8080
CMD ["java", "-jar", "myblog-0.1.0-SNAPSHOT-standalone.jar"]
