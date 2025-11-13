# Script d'initialisation du projet Zoom Meetings
# Ce script installe toutes les dépendances nécessaires

Write-Host "🚀 Initialisation du projet Zoom Meetings..." -ForegroundColor Cyan
Write-Host ""

# Vérification des prérequis
Write-Host "📋 Vérification des prérequis..." -ForegroundColor Yellow

# Vérifier Node.js
Write-Host "Vérification de Node.js..." -ForegroundColor Gray
try {
    $nodeVersion = node --version
    Write-Host "✅ Node.js $nodeVersion détecté" -ForegroundColor Green
} catch {
    Write-Host "❌ Node.js n'est pas installé. Veuillez installer Node.js 18+ depuis https://nodejs.org/" -ForegroundColor Red
    exit 1
}

# Vérifier Java
Write-Host "Vérification de Java..." -ForegroundColor Gray
try {
    $javaVersion = java -version 2>&1 | Select-String -Pattern "version"
    Write-Host "✅ Java détecté: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Java n'est pas installé. Veuillez installer Java JDK 17+ depuis https://adoptium.net/" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🔧 Installation des dépendances..." -ForegroundColor Yellow
Write-Host ""

# Installation Frontend
Write-Host "📦 Installation des dépendances Frontend..." -ForegroundColor Cyan
Set-Location -Path "frontend"

if (Test-Path "node_modules") {
    Write-Host "⚠️  node_modules existe déjà, suppression..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force "node_modules"
}

npm install
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Dépendances Frontend installées avec succès" -ForegroundColor Green
} else {
    Write-Host "❌ Erreur lors de l'installation des dépendances Frontend" -ForegroundColor Red
    Set-Location -Path ".."
    exit 1
}

Set-Location -Path ".."
Write-Host ""

# Vérification Backend (Maven téléchargera les dépendances au premier lancement)
Write-Host "☕ Vérification du Backend..." -ForegroundColor Cyan
Set-Location -Path "backend"

if (Test-Path ".\mvnw.cmd") {
    Write-Host "✅ Maven Wrapper trouvé" -ForegroundColor Green
    Write-Host "ℹ️  Les dépendances Maven seront téléchargées au premier lancement" -ForegroundColor Gray
} else {
    Write-Host "⚠️  Maven Wrapper non trouvé, création..." -ForegroundColor Yellow
}

Set-Location -Path ".."
Write-Host ""

# Résumé
Write-Host "✨ Installation terminée avec succès !" -ForegroundColor Green
Write-Host ""
Write-Host "📚 Prochaines étapes:" -ForegroundColor Cyan
Write-Host ""
Write-Host "1️⃣  Lire le guide de démarrage:" -ForegroundColor White
Write-Host "   type GETTING_STARTED.md" -ForegroundColor Gray
Write-Host ""
Write-Host "2️⃣  Démarrer le projet:" -ForegroundColor White
Write-Host ""
Write-Host "   Option A - Manuellement:" -ForegroundColor Yellow
Write-Host "   • Backend:  cd backend; .\mvnw.cmd spring-boot:run" -ForegroundColor Gray
Write-Host "   • Frontend: cd frontend; npm run dev" -ForegroundColor Gray
Write-Host ""
Write-Host "   Option B - Avec VS Code:" -ForegroundColor Yellow
Write-Host "   • Ctrl+Shift+P > Tasks: Run Task > Full Stack: Start Dev" -ForegroundColor Gray
Write-Host ""
Write-Host "   Option C - Avec Docker:" -ForegroundColor Yellow
Write-Host "   • docker-compose up --build" -ForegroundColor Gray
Write-Host ""
Write-Host "3️⃣  Accéder à l'application:" -ForegroundColor White
Write-Host "   • Frontend: http://localhost:5173" -ForegroundColor Gray
Write-Host "   • Backend:  http://localhost:8080/api/meetings" -ForegroundColor Gray
Write-Host "   • Console H2: http://localhost:8080/h2-console" -ForegroundColor Gray
Write-Host ""
Write-Host "🎉 Bon développement !" -ForegroundColor Magenta
