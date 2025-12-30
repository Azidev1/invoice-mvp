# ---- Build stage ----
FROM docker.io/maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1) Copie des fichiers Maven pour maximiser le cache
COPY pom.xml /app/pom.xml

# (optionnel mais recommandé) télécharge les deps en cache
RUN mvn -q -e -DskipTests dependency:go-offline

# 2) Copie du code
COPY src /app/src

# 3) Build
RUN mvn -q -DskipTests clean package

# ---- Runtime stage ----
FROM docker.io/eclipse-temurin:21-jre
WORKDIR /app

# Adapte le nom du jar si besoin (sinon wildcard OK)
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
