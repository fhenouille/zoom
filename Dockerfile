# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY backend/pom.xml ./pom.xml
COPY backend/src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=railway

# Convert DATABASE_URL from postgres:// to jdbc:postgresql://
CMD ["sh", "-c", "if [ -z \"$DATABASE_URL\" ]; then SPRING_DATASOURCE_URL=\"jdbc:postgresql://localhost:5432/zoomdb\"; else SPRING_DATASOURCE_URL=$(echo $DATABASE_URL | sed 's/^postgres:/jdbc:postgresql:/'); fi && java -Dserver.port=${PORT:-8080} -Dspring.datasource.url=$SPRING_DATASOURCE_URL -jar app.jar"]
