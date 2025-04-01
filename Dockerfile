# Используем легковесный образ OpenJDK
FROM openjdk:17-slim

# Устанавливаем sshpass для передачи пароля SSH
RUN apt-get update && apt-get install -y sshpass && rm -rf /var/lib/apt/lists/*

# Создаем рабочую директорию
WORKDIR /app

# Копируем jar-файл приложения (убедитесь, что JAR файл находится в target/ после сборки)
COPY target/evo-1.0-SNAPSHOT.jar app.jar

# Копируем скрипт entrypoint.sh в контейнер и делаем его исполняемым
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Открываем порт приложения (например, 8080)
EXPOSE 8080

# Указываем команду запуска
ENTRYPOINT ["/app/entrypoint.sh"]
