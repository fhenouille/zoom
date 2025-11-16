#!/bin/bash
cd backend
mvn clean package -DskipTests
java -Dserver.port=$PORT -jar target/zoom-backend-0.0.1-SNAPSHOT.jar
