# Multi-stage build для оптимизации размера образа

# Stage 1: Build
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Копируем файлы для сборки
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src

# Собираем приложение
RUN gradle clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Создаем пользователя для безопасности
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Копируем собранный JAR из stage build
COPY --from=build /app/build/libs/*.war app.war

# Открываем порт
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.war"]

