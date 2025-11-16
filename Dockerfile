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

# Convert DATABASE_URL and start app
CMD ["sh", "-c", "if [ -n \"$DATABASE_URL\" ]; then export JDBC_DATABASE_URL=$(echo $DATABASE_URL | sed 's|^postgres://|jdbc:postgresql://|'); echo '✅ Converted DATABASE_URL to JDBC'; fi && java -Dserver.port=${PORT:-8080} -jar app.jar"]
