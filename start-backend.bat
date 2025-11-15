@echo off
echo ======================================
echo   Demarrage du Backend Spring Boot
echo ======================================
echo.

cd backend

echo 🔧 Chargement des variables d'environnement Zoom...

for /f "usebackq tokens=1,2 delims==" %%a in (".env") do (
    REM Ignore les lignes vides et les commentaires
    echo %%a | findstr /r "^#" >nul
    if errorlevel 1 (
        set %%a=%%b
        echo   ✓ %%a défini
    )
)

echo ✅ Variables d'environnement chargées avec succès!
echo.
echo 📋 Variables définies:
echo   - ZOOM_ACCOUNT_ID: %ZOOM_ACCOUNT_ID%
echo   - ZOOM_CLIENT_ID: %ZOOM_CLIENT_ID%
echo   - ZOOM_CLIENT_SECRET: ****
echo   - ZOOM_USER_ID: %ZOOM_USER_ID%
echo   - MEETING_ID: %MEETING_ID%

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
