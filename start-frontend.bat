@echo off
echo ======================================
echo   Demarrage du Frontend React
echo ======================================
echo.

cd frontend

if not exist node_modules (
    echo [INFO] Installation des dependances...
    call npm install
)

echo [INFO] Demarrage du serveur de developpement Vite...
call npm run dev

cd ..
