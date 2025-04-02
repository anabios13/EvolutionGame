#!/bin/bash
set -e

echo "Запуск SSH туннеля..."
# Используем sshpass для передачи пароля в неинтерактивном режиме
sshpass -p '' ssh -o StrictHostKeyChecking=no -p 2222 -L  -N &

# Даём время установить туннель (при необходимости можно увеличить задержку)
sleep 5

echo "Запуск Spring Boot приложения..."
exec java -jar /app/app.jar
