# Script PowerShell pour vérifier la configuration DATABASE_URL
# Usage: .\verify-database-config.ps1 -BackendUrl "https://zoom-xxxx.railway.app"

param(
    [string]$BackendUrl = "http://localhost:8080",
    [string]$ApiEndpoint = "/api/health/database-config"
)

function Test-DatabaseConfig {
    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host "🔍 Vérification DATABASE_URL Configuration" -ForegroundColor Cyan
    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host ""

    # 1. Vérifier la connexion au backend
    Write-Host "1️⃣  Test de connexion au backend..." -ForegroundColor Yellow
    try {
        $fullUrl = $BackendUrl.TrimEnd('/') + $ApiEndpoint
        Write-Host "   URL: $fullUrl"

        $response = Invoke-RestMethod -Uri $fullUrl -Method Get -ErrorAction Stop
        Write-Host "✅ Connexion réussie" -ForegroundColor Green
        Write-Host ""

        # 2. Afficher la configuration
        Write-Host "2️⃣  Configuration DATABASE_URL:" -ForegroundColor Yellow
        Write-Host "   DATABASE_URL défini: $($response.database_url_set)" -ForegroundColor $(if ($response.database_url_set) { 'Green' } else { 'Red' })
        if ($response.database_url_masked) {
            Write-Host "   Format: $($response.database_url_format)" -ForegroundColor Green
            Write-Host "   Valeur: $($response.database_url_masked)" -ForegroundColor DarkGreen
        }
        Write-Host ""

        Write-Host "3️⃣  Configuration JDBC_DATABASE_URL (après conversion):" -ForegroundColor Yellow
        Write-Host "   JDBC_DATABASE_URL défini: $($response.jdbc_database_url_set)" -ForegroundColor $(if ($response.jdbc_database_url_set) { 'Green' } else { 'Red' })
        if ($response.jdbc_database_url_masked) {
            Write-Host "   Valeur: $($response.jdbc_database_url_masked)" -ForegroundColor DarkGreen
        }
        Write-Host ""

        # 3. Configuration Spring
        Write-Host "4️⃣  Configuration Spring:" -ForegroundColor Yellow
        Write-Host "   Active Profiles: $($response.active_profiles)" -ForegroundColor Green
        Write-Host "   DataSource URL configurée: $($response.datasource_url_configured)" -ForegroundColor Green
        Write-Host "   DataSource URL: $($response.datasource_url_masked)" -ForegroundColor DarkGreen
        Write-Host "   Driver: $($response.driver_class)" -ForegroundColor Green
        Write-Host "   Port: $($response.port)" -ForegroundColor Green
        Write-Host "   HikariCP Max Pool Size: $($response.hikari_max_pool_size)" -ForegroundColor Green
        Write-Host ""

        # 4. Résumé
        Write-Host "5️⃣  Résumé:" -ForegroundColor Yellow
        if ($response.database_url_set -and $response.jdbc_database_url_set -and $response.active_profiles -contains "railway") {
            Write-Host "✅ CONFIGURATION CORRECTE - DATABASE_URL est correctement configuré et converti" -ForegroundColor Green
        } else {
            Write-Host "⚠️  PROBLÈME DÉTECTÉ:" -ForegroundColor Red
            if (-not $response.database_url_set) {
                Write-Host "   - DATABASE_URL n'est pas défini dans Railway" -ForegroundColor Red
            }
            if (-not $response.jdbc_database_url_set) {
                Write-Host "   - JDBC_DATABASE_URL n'est pas défini (conversion a échoué)" -ForegroundColor Red
            }
            if ($response.active_profiles -notcontains "railway") {
                Write-Host "   - Profil 'railway' n'est pas actif" -ForegroundColor Red
            }
        }

    } catch {
        Write-Host "❌ Erreur de connexion: $_" -ForegroundColor Red
        Write-Host ""
        Write-Host "Vérifications locales:" -ForegroundColor Yellow
        Write-Host "   - Le backend est-il démarré?" -ForegroundColor Gray
        Write-Host "   - L'URL $BackendUrl est-elle correcte?" -ForegroundColor Gray
        Write-Host "   - Est-il en HTTPS?" -ForegroundColor Gray
        Write-Host ""
        Write-Host "Pour utiliser avec Railway:" -ForegroundColor Yellow
        Write-Host "   .\verify-database-config.ps1 -BackendUrl 'https://zoom-xxxx.railway.app'" -ForegroundColor Gray
    }

    Write-Host ""
    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host "Diagnostic terminé" -ForegroundColor Cyan
    Write-Host "============================================" -ForegroundColor Cyan
}

# Exécuter le test
Test-DatabaseConfig
