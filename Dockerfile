FROM gradle:jdk18 as builder
USER root
WORKDIR /builder
ADD . /builder
RUN gradle build --stacktrace

FROM openjdk:18.0.1.1-oraclelinux7
WORKDIR /app
EXPOSE $PORT 27017
COPY --from=builder /builder/build/libs/*.jar ./
CMD java -Dserver.port=$PORT  -jar stockflow-0.0.1-SNAPSHOT.jar