#!/bin/bash

echo "Compilando microservicios con Maven..."

for service in category-service products-service qa-service reviews-service search-service seller-service; do
  echo "Compilando $service ..."
  cd "$service"
  mvn clean install -DskipTests
  if [ $? -ne 0 ]; then
    echo "Error compilando $service. Abortando."
    exit 1
  fi
  cd ..
done

echo "Compilación completada."

echo "🐳 Levantando contenedores con Docker Compose..."
docker-compose up --build
