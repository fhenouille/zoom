# Déploiement Railway.app - Guide

Railway.app est une alternative moderne à Heroku. C'est gratuit, simple et sans carte de crédit requise initialement!

## Prérequis
1. Un compte GitHub (vous l'avez déjà)
2. Un compte Railway (gratuit): https://railway.app

## Étapes de déploiement

### 1. Créer un compte Railway
- Allez sur https://railway.app
- Cliquez "Start Now"
- Connectez-vous avec GitHub
- Autorisez Railway à accéder à vos repos

### 2. Créer un nouveau projet
- Dans le dashboard Railway, cliquez "New Project"
- Sélectionnez "Deploy from GitHub"
- Cherchez le repo `zoom`
- Cliquez "Deploy"

Railway détecte automatiquement:
- Le backend Spring Boot dans le dossier `backend/`
- Le framework et la version Java
- Les dépendances

### 3. Ajouter PostgreSQL
- Dans votre projet Railway, cliquez "+ New"
- Sélectionnez "Database"
- Choisissez "PostgreSQL"
- Railway crée la BD automatiquement et injecte `DATABASE_URL`

### 4. Configurer les variables d'environnement
Railway injecte automatiquement les variables nécessaires:
- `DATABASE_URL` - Connexion PostgreSQL (auto)
- `PORT` - Port dynamique (auto)

Vous devez ajouter:
1. Cliquez sur le service "zoom" (votre backend)
2. Allez dans l'onglet "Variables"
3. Cliquez "+ Add Variable"
4. Ajoutez: `SPRING_PROFILES_ACTIVE=railway`

### 5. Déploiement automatique
Railway redéploie automatiquement à chaque push sur `main`:
- Modifiez votre code
- Committez et poussez
- Railway détecte le changement et redéploie

## Voir votre app

1. Dans Railway, cliquez sur le service "zoom"
2. Allez dans l'onglet "Settings"
3. Cherchez "Domains" - vous verrez une URL comme:
   `https://zoom-xxxx.railway.app`

Votre API backend sera accessible à:
`https://zoom-xxxx.railway.app/api`

## Mettre à jour le frontend

Une fois votre backend déployé:

1. Modifiez `frontend/.env.production`:
```
VITE_API_BASE_URL=https://zoom-xxxx.railway.app/api
```
(Remplacez `zoom-xxxx` par votre URL Railway)

2. Committez et poussez:
```bash
git add frontend/.env.production
git commit -m "feat: update API URL for Railway backend"
git push
```

GitHub Pages redéploiera automatiquement le frontend avec la nouvelle URL.

## Commandes utiles

### Voir les logs
- Dans Railway, cliquez sur "zoom" → "Logs"
- Voir les logs en temps réel de votre app

### Redéployer manuellement
- Cliquez sur "Deploy" dans Railway
- Ou: `git push` (redéploie automatiquement)

### Voir les variables
- Service "zoom" → "Variables"

### Supprimer le projet
- Project Settings → "Delete Project"

## Limites gratuites Railway

- **$5 de crédit gratuit par mois**
- Parfait pour une petite app comme la vôtre
- PostgreSQL inclus
- Pas de limitation de temps (contrairement à Heroku qui déploie après 30 min d'inactivité)

## Troubleshooting

### L'app se déploie mais affiche une erreur
1. Cliquez sur "zoom" → "Logs"
2. Cherchez l'erreur dans les logs
3. Vérifiez que `SPRING_PROFILES_ACTIVE=railway` est défini

### La base de données ne se connecte pas
- Railway injecte automatiquement `DATABASE_URL`
- Vérifiez dans "Variables" que PostgreSQL est connecté
- Les logs doivent montrer la connexion BD

### Port erreur
- Railway assigne le port dynamiquement via `PORT`
- C'est automatiquement géré par `application-railway.properties`

## Besoin d'aide?
- Docs Railway: https://docs.railway.app
- Support: https://railway.app/support
