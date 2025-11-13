@echo off
echo ======================================
echo   Demarrage du Backend Spring Boot
echo ======================================
echo.

cd backend

REM Verifier si Maven est installe
where mvn >nul 2>&1
if %ERRORLEVEL% == 0 (
    echo [INFO] Utilisation de Maven installe sur le systeme
    mvn spring-boot:run
) else (
    echo [ERREUR] Maven n'est pas installe.
    echo.
    echo Options pour installer Maven:
    echo   1. Telecharger depuis https://maven.apache.org/download.cgi
    echo   2. Ou installer via Chocolatey: choco install maven
    echo   3. Ou utiliser Docker: docker-compose up
    echo.
    echo Alternative: Utilisez Docker Compose pour lancer le projet:
    echo   docker-compose up --build
    echo.
    pause
    exit /b 1
)

cd ..
