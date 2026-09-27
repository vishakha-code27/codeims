FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/ims-0.0.1-SNAPSHOT.jar app.jar
ENV SERVER_PORT=8081
ENV SPRING_DATASOURCE_URL=jdbc:sqlite:/data/codeb_ims.db
EXPOSE 8081
VOLUME ["/data"]
ENTRYPOINT ["java", "-jar", "app.jar"]