# ============================================================================
# Image du backend Urgence Santé — construction multi-étapes.
#
# Étape 1 : compile le jar avec Maven (le VPS n'a pas besoin de JDK/Maven).
# Étape 2 : image d'exécution minimale (JRE seul).
#
# Contexte de build = RACINE du dépôt (le pom copie docs/api/openapi.yaml et
# infrastructure/demo/R__seed_demo.sql en ressources — voir bootstrap/pom.xml).
# Build : docker compose -f deploy/docker-compose.yml build
# ============================================================================

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY backend/ backend/
COPY docs/api/ docs/api/
COPY infrastructure/demo/ infrastructure/demo/
RUN cd backend && mvn -B -q -pl bootstrap -am package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /build/backend/bootstrap/target/*-exec.jar app.jar
# Profil par défaut : local (seed automatique des 15 établissements DEMO).
# Le port et la base sont fournis par docker-compose (DB_HOST=db, etc.).
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
