# ═══════════════════════════════════════════════════════════════
# Dockerfile multi-etapa — Users Management API (Spring Boot)
# ═══════════════════════════════════════════════════════════════

# ───────────────────────────────────────────────────────────────
# ETAPA 1: Compilación (build)
# Imagen con Maven y JDK 17. Solo se usa para compilar;
# no forma parte de la imagen final.
# ───────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 1) Copiar solo el pom.xml y descargar las dependencias.
#    Docker guarda esta capa en caché: si el pom.xml no cambia,
#    las dependencias no se vuelven a descargar.
COPY pom.xml .
RUN mvn -B dependency:go-offline

# 2) Copiar el código fuente y empaquetar.
#    Las pruebas se ejecutan en local antes de cada commit;
#    aquí se omiten para acelerar la construcción.
COPY src ./src
RUN mvn -B package -DskipTests

# ───────────────────────────────────────────────────────────────
# ETAPA 2: Ejecución (runtime)
# Solo el JRE 17: sin Maven, sin JDK y sin código fuente.
# ───────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre
WORKDIR /app

# Usuario sin privilegios: la aplicación no corre como root.
RUN useradd --system --no-create-home appuser

# Copiar únicamente el .jar generado en la etapa de compilación.
COPY --from=build /app/target/*.jar app.jar

USER appuser
EXPOSE 8080

# La JVM usa como máximo el 75% de la memoria del contenedor
# (el plan gratuito de Render ofrece 512 MB).
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]