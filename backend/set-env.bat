@echo off
REM Script batch pour charger les variables d'environnement Zoom
REM Usage: call set-env.bat

if not exist ".env" (
    echo ❌ Le fichier .env n'existe pas!
    echo 📝 Créez un fichier .env à partir de .env.example
    echo    copy .env.example .env
    exit /b 1
)

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
