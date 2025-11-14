#!/bin/bash

# Script bash pour charger les variables d'environnement Zoom
# Usage: source ./set-env.sh  OU  . ./set-env.sh

# Vérifie si le fichier .env existe
if [ ! -f .env ]; then
    echo "❌ Le fichier .env n'existe pas!"
    echo "📝 Créez un fichier .env à partir de .env.example"
    echo "   cp .env.example .env"
    return 1 2>/dev/null || exit 1
fi

echo "🔧 Chargement des variables d'environnement Zoom..."

# Lit le fichier .env et définit les variables
while IFS='=' read -r key value; do
    # Ignore les lignes vides et les commentaires
    if [[ -z "$key" ]] || [[ "$key" =~ ^[[:space:]]*# ]]; then
        continue
    fi

    # Supprime les espaces autour de la clé et de la valeur
    key=$(echo "$key" | xargs)
    value=$(echo "$value" | xargs)

    # Supprime les guillemets si présents
    value="${value%\"}"
    value="${value#\"}"
    value="${value%\'}"
    value="${value#\'}"

    # Définit la variable d'environnement
    export "$key=$value"
    echo "  ✓ $key défini"
done < .env

echo "✅ Variables d'environnement chargées avec succès!"
echo ""
echo "📋 Variables définies:"
echo "  - ZOOM_ACCOUNT_ID: $ZOOM_ACCOUNT_ID"
echo "  - ZOOM_CLIENT_ID: $ZOOM_CLIENT_ID"
echo "  - ZOOM_CLIENT_SECRET: ****"
echo "  - ZOOM_USER_ID: $ZOOM_USER_ID"
