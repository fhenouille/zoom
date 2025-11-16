# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["sh", "-c", "if [ -n \"$DATABASE_URL\" ]; then export JDBC_DATABASE_URL=$(echo $DATABASE_URL | sed 's|^postgres://|jdbc:postgresql://|; s|^postgresql://|jdbc:postgresql://|'); echo '✅ Converted DATABASE_URL to JDBC'; fi && java -Dserver.port=${PORT:-8080} -jar app.jar"]
