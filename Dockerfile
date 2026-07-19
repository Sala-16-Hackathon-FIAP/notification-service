FROM maven:3.9-eclipse-temurin-21 AS build
ARG GITHUB_TOKEN
ARG GITHUB_ACTOR=github-actions
WORKDIR /app
COPY pom.xml settings.xml ./
RUN GITHUB_TOKEN=${GITHUB_TOKEN} GITHUB_ACTOR=${GITHUB_ACTOR} mvn dependency:go-offline -B -s settings.xml -q
COPY src ./src
RUN GITHUB_TOKEN=${GITHUB_TOKEN} GITHUB_ACTOR=${GITHUB_ACTOR} mvn clean package -DskipTests -B -s settings.xml -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/target/newrelic/newrelic.jar newrelic/newrelic.jar
COPY newrelic.yml newrelic/newrelic.yml
EXPOSE 8085

ENTRYPOINT ["java", \
  "-javaagent:/app/newrelic/newrelic.jar", \
  "-Dnewrelic.config.file=/app/newrelic/newrelic.yml", \
  "-jar", "/app/app.jar"]
