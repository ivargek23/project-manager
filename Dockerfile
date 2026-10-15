FROM eclipse-temurin:25-jdk AS build
WORKDIR /src

LABEL authors="ivonavargek"

# dependencies
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# source code
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

# runtime
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN useradd --system --uid 1001 app
USER app

COPY --from=build /src/target/*.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]