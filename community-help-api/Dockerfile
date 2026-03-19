# STAGE 1: Build con Maven
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia pom primero (cache de dependencias)
COPY pom.xml .

RUN mvn dependency:go-offline

# Copia el código fuente
COPY src ./src

# Compila la aplicación
RUN mvn clean package -DskipTests


# STAGE 2: Runtime
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copia el jar generado
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Variables JVM opcionales
ENV JAVA_OPTS=""

# Ejecuta la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]