#!/bin/bash

# Convert PostgreSQL connection URL format for Spring Boot
if [ -n "$DATABASE_URL" ]; then
    # Convert postgres:// to jdbc:postgresql://
    export JDBC_DATABASE_URL=$(echo $DATABASE_URL | sed 's/^postgres:/jdbc:postgresql:/')
    echo "✅ Converted DATABASE_URL to JDBC format"
    echo "📍 JDBC URL: ${JDBC_DATABASE_URL//:[^:/@]*@/:****@}"
fi

# Start the application
java -Dserver.port=${PORT:-8080} -jar app.jar

