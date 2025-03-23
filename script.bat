@echo off
setlocal enabledelayedexpansion

REM Каталог, в котором находится скрипт
set "TARGET_DIR=%~dp0"

REM Итоговый файл для записи
set "OUTPUT_FILE=project_code_dump.txt"

REM Если итоговый файл уже существует – удаляем его
if exist "%OUTPUT_FILE%" del "%OUTPUT_FILE%"

echo === Извлечение из каталога %TARGET_DIR% === >> "%OUTPUT_FILE%"
echo Начинается сканирование каталога: %TARGET_DIR%

REM Список расширений файлов для анализа. При необходимости можно добавить другие.
set "EXTENSIONS=java xml properties yml yaml json md txt gradle"

REM Флаг для проверки найденных файлов
set "foundFiles="

for %%ext in (%EXTENSIONS%) do (
    echo Обработка файлов с расширением .%%ext...
    for /R "%TARGET_DIR%" %%f in (*."%%ext") do (
        set "foundFiles=true"
        echo Найден файл: %%f
        echo // Файл: %%f >> "%OUTPUT_FILE%"
        type "%%f" >> "%OUTPUT_FILE%"
        echo. >> "%OUTPUT_FILE%"
        echo ------------------------------------------------------ >> "%OUTPUT_FILE%"
        echo. >> "%OUTPUT_FILE%"
    )
)

if not defined foundFiles (
    echo Не найдено файлов с указанными расширениями в %TARGET_DIR%.
    echo Не найдено файлов с указанными расширениями в %TARGET_DIR%. >> "%OUTPUT_FILE%"
)

echo Извлечение завершено. Результаты сохранены в "%OUTPUT_FILE%".
pause
