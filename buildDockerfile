FROM openjdk:18.0.1.1-oraclelinux7
WORKDIR /app
EXPOSE 8080
COPY ./build/libs/*.jar .
CMD ["java", "-jar", "stockflow-0.0.1-SNAPSHOT.jar"]