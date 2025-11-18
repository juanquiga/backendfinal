# =============================
# STAGE 1: Build del proyecto
# =============================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar pom y MVN wrapper
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Copiar el código
COPY src ./src

# Construir la app y empacar el jar ejecutable
RUN mvn -DskipTests clean package

# =============================
# STAGE 2: Imagen final
# =============================
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiar el JAR generado (spring boot lo repackea)
COPY --from=build /app/target/*.jar /app/app.jar

# Puerto (opcional)
EXPOSE 8080

# Ejecutar backend
ENTRYPOINT ["java", "-jar", "/app/app.jar"]