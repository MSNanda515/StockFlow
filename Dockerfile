FROM gradle:7-jdk18 as builder
USER root
WORKDIR /builder
ADD . /builder
RUN gradle build --stacktrace

FROM openjdk:18.0.1.1-oraclelinux7
WORKDIR /app
EXPOSE 8080
COPY --from=builder /builder/build/libs/*.jar .
CMD ["java", "-jar", "stockflow-0.0.1-SNAPSHOT.jar"]