# Déploiement Heroku - Guide

## Prérequis
1. Créer un compte Heroku (gratuit): https://www.heroku.com/
2. Installer Heroku CLI: https://devcenter.heroku.com/articles/heroku-cli

## Étapes de déploiement

### 1. Se connecter à Heroku
```bash
heroku login
```

### 2. Créer une nouvelle application Heroku
```bash
cd backend
heroku create zoom-backend-fhenouille
```
(Remplacez `zoom-backend-fhenouille` par un nom unique)

### 3. Ajouter PostgreSQL
```bash
heroku addons:create heroku-postgresql:hobby-dev
```

### 4. Configurer l'application
```bash
heroku config:set SPRING_PROFILES_ACTIVE=heroku
```

### 5. Déployer
```bash
git push heroku main
```

Ou si vous n'êtes pas sur la branche `main`:
```bash
git push heroku YOUR_BRANCH:main
```

### 6. Voir les logs
```bash
heroku logs --tail
```

### 7. Vérifier le déploiement
L'URL de votre API sera: `https://zoom-backend-fhenouille.herokuapp.com/api`

Vous pouvez tester avec:
```bash
curl https://zoom-backend-fhenouille.herokuapp.com/h2-console
```

### 8. Mettre à jour le frontend
Une fois déployé, mettez à jour `frontend/.env.production`:
```
VITE_API_BASE_URL=https://zoom-backend-fhenouille.herokuapp.com/api
```

Puis committez et poussez pour redéployer le frontend sur GitHub Pages.

## Commandes utiles
```bash
# Voir les variables d'environnement
heroku config

# Voir les logs en temps réel
heroku logs --tail

# Ouvrir l'app dans le navigateur
heroku open

# Supprimer l'app
heroku apps:destroy zoom-backend-fhenouille
```

## Troubleshooting

### Les données ne persistent pas
C'est normal avec Heroku - la base de données est réinitialisée à chaque déploiement.
Pour persister les données, utilisez une base de données PostgreSQL externe.

### Port dynamique
Heroku assigne le port dynamiquement via la variable `$PORT`.
C'est géré dans `application-heroku.properties` avec `server.port=${PORT:8080}`

### CORS
La configuration CORS accepte déjà `https://fhenouille.github.io`, donc le frontend déployé fonctionnera.
