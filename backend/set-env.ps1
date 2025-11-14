# Script PowerShell pour charger les variables d'environnement Zoom
# Usage: . .\set-env.ps1

# Vérifie si le fichier .env existe
if (-Not (Test-Path ".env")) {
    Write-Host "❌ Le fichier .env n'existe pas!" -ForegroundColor Red
    Write-Host "📝 Créez un fichier .env à partir de .env.example" -ForegroundColor Yellow
    Write-Host "   Copy-Item .env.example .env" -ForegroundColor Cyan
    exit 1
}

Write-Host "🔧 Chargement des variables d'environnement Zoom..." -ForegroundColor Green

# Lit le fichier .env et définit les variables
Get-Content .env | ForEach-Object {
    # Ignore les lignes vides et les commentaires
    if ($_ -match '^\s*$' -or $_ -match '^\s*#') {
        return
    }

    # Parse la ligne KEY=VALUE
    if ($_ -match '^([^=]+)=(.*)$') {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()

        # Supprime les guillemets si présents
        $value = $value -replace '^["'']|["'']$', ''

        # Définit la variable d'environnement
        Set-Item -Path "env:$key" -Value $value
        Write-Host "  ✓ $key défini" -ForegroundColor Gray
    }
}

Write-Host "✅ Variables d'environnement chargées avec succès!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Variables définies:" -ForegroundColor Cyan
Write-Host "  - ZOOM_ACCOUNT_ID: $env:ZOOM_ACCOUNT_ID" -ForegroundColor Gray
Write-Host "  - ZOOM_CLIENT_ID: $env:ZOOM_CLIENT_ID" -ForegroundColor Gray
Write-Host "  - ZOOM_CLIENT_SECRET: ****" -ForegroundColor Gray
Write-Host "  - ZOOM_USER_ID: $env:ZOOM_USER_ID" -ForegroundColor Gray
