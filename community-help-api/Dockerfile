# STAGE 1: Build con Maven
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia pom primero (para cachear dependencias)
COPY pom.xml .

RUN mvn dependency:go-offline

# Copia el código fuente
COPY src ./src

# Compila la aplicación
RUN mvn clean package -DskipTests

# STAGE 2: Runtime
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Instala netcat para que wait-for-db.sh funcione
RUN apt-get update && apt-get install -y netcat && rm -rf /var/lib/apt/lists/*

# Copia el jar generado desde el build
COPY --from=build /app/target/*.jar app.jar

# Copia el script wait-for-db
COPY wait-for-db.sh /wait-for-db.sh
RUN chmod +x /wait-for-db.sh

EXPOSE 8080

# Variables JVM opcionales
ENV JAVA_OPTS=""

# Ejecuta la aplicación después de que PostgreSQL esté listo
ENTRYPOINT ["/wait-for-db.sh"]