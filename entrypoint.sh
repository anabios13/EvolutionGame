#!/bin/bash
set -e

echo "Запуск SSH туннеля..."
# Используем sshpass для передачи пароля в неинтерактивном режиме
sshpass -p 'LPsX}5896' ssh -o StrictHostKeyChecking=no -p 2222 -L 5433:localhost:5432 s336781@helios.cs.ifmo.ru -N &

# Даём время установить туннель (при необходимости можно увеличить задержку)
sleep 5

echo "Запуск Spring Boot приложения..."
exec java -jar /app/app.jar
