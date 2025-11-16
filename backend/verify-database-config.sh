#!/bin/bash

# Script de vérification de la configuration DATABASE_URL sur Railway
# À exécuter dans le container Railway pour diagnostiquer les problèmes de connexion

echo "============================================"
echo "🔍 Diagnostic DATABASE_URL Configuration"
echo "============================================"
echo ""

# 1. Vérifier si DATABASE_URL existe
echo "1️⃣  Vérification de DATABASE_URL..."
if [ -z "$DATABASE_URL" ]; then
    echo "❌ DATABASE_URL n'est PAS défini"
else
    echo "✅ DATABASE_URL est défini"
    echo "   Format actuel: $DATABASE_URL"
    echo ""

    # 2. Analyser le format
    echo "2️⃣  Analyse du format DATABASE_URL..."
    if [[ $DATABASE_URL == postgres://* ]]; then
        echo "✅ Format correct: postgres://"

        # Extraire les composants
        TEMP="${DATABASE_URL#postgres://}"

        # Extraire user:password@host:port/db
        if [[ $TEMP == *"@"* ]]; then
            CREDENTIALS="${TEMP%@*}"
            HOST_PART="${TEMP#*@}"
            USER="${CREDENTIALS%:*}"
            PASSWORD="${CREDENTIALS#*:}"

            if [[ $HOST_PART == *":"* ]]; then
                HOST="${HOST_PART%:*}"
                TEMP2="${HOST_PART#*:}"
                PORT="${TEMP2%/*}"
                DATABASE="${TEMP2#*/}"
            fi

            echo "   Composants extraits:"
            echo "   - User: $USER"
            echo "   - Host: $HOST"
            echo "   - Port: $PORT"
            echo "   - Database: $DATABASE"
        fi
    elif [[ $DATABASE_URL == jdbc:postgresql://* ]]; then
        echo "✅ Déjà au format JDBC"
    else
        echo "⚠️  Format inattendu"
    fi
fi

echo ""

# 3. Vérifier JDBC_DATABASE_URL (après conversion)
echo "3️⃣  Vérification de JDBC_DATABASE_URL (après conversion)..."
if [ -z "$JDBC_DATABASE_URL" ]; then
    echo "⚠️  JDBC_DATABASE_URL n'est PAS défini"
else
    echo "✅ JDBC_DATABASE_URL est défini"
    echo "   Format: $JDBC_DATABASE_URL"
fi

echo ""

# 4. Vérifier les paramètres de connexion Spring
echo "4️⃣  Vérification du fichier application-railway.properties..."
PROPS_FILE="/app/application-railway.properties"
if [ -f "$PROPS_FILE" ]; then
    echo "✅ Fichier trouvé"
    echo "   Contenu pertinent:"
    grep "spring.datasource.url" "$PROPS_FILE" || echo "   ⚠️  spring.datasource.url non trouvé"
else
    echo "❌ Fichier non trouvé à $PROPS_FILE"
fi

echo ""

# 5. Vérifier les variables d'environnement essentielles
echo "5️⃣  Variables d'environnement:"
echo "   - SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-non défini}"
echo "   - PORT: ${PORT:-8080 (défaut)}"
echo "   - DATABASE_URL: ${DATABASE_URL:+défini}${DATABASE_URL:-❌ NON DÉFINI}"
echo "   - JDBC_DATABASE_URL: ${JDBC_DATABASE_URL:+défini}${JDBC_DATABASE_URL:-non défini}"

echo ""

# 6. Tester la connexion (si nc ou telnet disponible)
echo "6️⃣  Test de connectivité..."
if command -v nc &> /dev/null; then
    if [ ! -z "$HOST" ] && [ ! -z "$PORT" ]; then
        if nc -z -w 2 "$HOST" "$PORT" 2>/dev/null; then
            echo "✅ Connexion au host $HOST:$PORT réussie"
        else
            echo "❌ Impossible de se connecter à $HOST:$PORT"
        fi
    else
        echo "⚠️  Impossible d'extraire host/port de DATABASE_URL"
    fi
else
    echo "⚠️  'nc' (netcat) non disponible pour tester la connexion"
fi

echo ""
echo "============================================"
echo "Diagnostic terminé"
echo "============================================"
