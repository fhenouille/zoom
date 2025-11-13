@echo off
echo.
echo ======================================
echo   Initialisation - Zoom Meetings
echo ======================================
echo.

echo Verification des prerequis...
echo.

REM Verifier Node.js
echo Verification de Node.js...
node --version >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Node.js n'est pas installe.
    echo Veuillez installer Node.js 18+ depuis https://nodejs.org/
    pause
    exit /b 1
) else (
    for /f "tokens=*" %%i in ('node --version') do set NODE_VERSION=%%i
    echo [OK] Node.js !NODE_VERSION! detecte
)

REM Verifier Java
echo Verification de Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Java n'est pas installe.
    echo Veuillez installer Java JDK 17+ depuis https://adoptium.net/
    pause
    exit /b 1
) else (
    echo [OK] Java detecte
)

echo.
echo ======================================
echo   Installation des dependances
echo ======================================
echo.

REM Installation Frontend
echo Installation des dependances Frontend...
cd frontend
if exist node_modules (
    echo [INFO] node_modules existe deja, suppression...
    rmdir /s /q node_modules
)
if exist package-lock.json (
    del package-lock.json
)

call npm install
if errorlevel 1 (
    echo [ERREUR] Erreur lors de l'installation des dependances Frontend
    cd ..
    pause
    exit /b 1
)
echo [OK] Dependances Frontend installees avec succes
cd ..

echo.
echo Verification du Backend...
cd backend
if exist mvnw.cmd (
    echo [OK] Maven Wrapper trouve
    echo [INFO] Les dependances Maven seront telechargees au premier lancement
) else (
    echo [ATTENTION] Maven Wrapper non trouve
)
cd ..

echo.
echo ======================================
echo   Installation terminee avec succes !
echo ======================================
echo.
echo Prochaines etapes:
echo.
echo [1] Lire le guide de demarrage:
echo     type INSTALL.md
echo.
echo [2] Demarrer le projet:
echo.
echo     Option A - Manuellement:
echo       Backend:  cd backend ^&^& mvnw.cmd spring-boot:run
echo       Frontend: cd frontend ^&^& npm run dev
echo.
echo     Option B - Avec VS Code:
echo       Ctrl+Shift+P ^> Tasks: Run Task ^> Full Stack: Start Dev
echo.
echo     Option C - Avec Docker:
echo       docker-compose up --build
echo.
echo [3] Acceder a l'application:
echo       Frontend:   http://localhost:5173
echo       Backend:    http://localhost:8080/api/meetings
echo       Console H2: http://localhost:8080/h2-console
echo.
echo ======================================
echo   Bon developpement !
echo ======================================
echo.
pause
