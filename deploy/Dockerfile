# ============================================================================
# Image du backend Urgence Santé — construction multi-étapes.
#
# Étape 1 : compile le jar avec Maven (le VPS n'a pas besoin de JDK/Maven).
# Étape 2 : image d'exécution minimale (JRE seul).
#
# Contexte de build = RACINE du dépôt (le pom copie docs/api/openapi.yaml et
# infrastructure/demo/R__seed_demo.sql en ressources — voir bootstrap/pom.xml).
# infrastructure/directory/abidjan-starter.json (annuaire RÉEL, issue #123)
# n'est PAS une ressource du pom : FacilityImportRunner le lit du système de
# fichiers (facility.import.file), donc il est copié tel quel dans l'image
# d'exécution ci-dessous, pas embarqué au classpath du jar.
# Build : docker compose -f deploy/docker-compose.yml build
# ============================================================================

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY backend/ backend/
COPY docs/api/ docs/api/
COPY infrastructure/demo/ infrastructure/demo/
COPY infrastructure/directory/ infrastructure/directory/
RUN cd backend && mvn -B -q -pl bootstrap -am package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /build/backend/bootstrap/target/*-exec.jar app.jar
COPY --from=build /build/infrastructure/directory/ infrastructure/directory/
# Profil requis explicitement par docker-compose.yml (SPRING_PROFILES_ACTIVE) :
# AUCUN défaut ici (issue #123 — un défaut silencieux vers le profil local a
# chargé le seed DEMO et désactivé les garde-fous production). Le port et la
# base sont fournis par docker-compose (DB_HOST=db, etc.).
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
