FROM node:24-bookworm-slim AS ui
WORKDIR /build/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9.16-eclipse-temurin-17 AS backend
WORKDIR /build
COPY backend/ backend/
COPY --from=ui /build/frontend/dist/ frontend/dist/
RUN mvn -f backend/pom.xml -Prelease --no-transfer-progress verify

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd --system traceback && useradd --system --gid traceback traceback && mkdir /app/data && chown traceback:traceback /app/data
COPY --from=backend /build/backend/target/traceback-1.0.0.jar /app/traceback.jar
USER traceback
ENV SPRING_PROFILES_ACTIVE=production
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/traceback.jar"]
