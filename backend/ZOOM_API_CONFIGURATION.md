# Configuration de l'API Zoom

## Prérequis

Pour utiliser l'intégration avec l'API Zoom, vous devez créer une application Server-to-Server OAuth sur le [Zoom Marketplace](https://marketplace.zoom.us/).

## Étapes de configuration

### 1. Créer une application Server-to-Server OAuth

1. Allez sur https://marketplace.zoom.us/
2. Connectez-vous avec votre compte Zoom
3. Cliquez sur "Develop" > "Build App"
4. Sélectionnez "Server-to-Server OAuth"
5. Donnez un nom à votre application et créez-la

### 2. Récupérer les credentials

Dans la page de configuration de votre app, vous trouverez :
- **Account ID** : Dans l'onglet "App Credentials"
- **Client ID** : Dans l'onglet "App Credentials"
- **Client Secret** : Dans l'onglet "App Credentials"

### 3. Configurer les scopes (permissions)

Dans l'onglet "Scopes", ajoutez les permissions suivantes :
- `meeting:read:admin` - Pour lire les informations des meetings
- `user:read:admin` - Pour lire les informations utilisateur
- Ou au minimum : `meeting:read:past_meetings:admin`

### 4. Activer l'application

Une fois les scopes configurés, activez votre application.

### 5. Récupérer l'User ID

L'User ID est l'email de l'utilisateur Zoom dont vous souhaitez récupérer les meetings, ou son ID utilisateur unique.

Vous pouvez le récupérer via :
- L'API : `GET https://api.zoom.us/v2/users`
- Ou directement l'email de l'utilisateur (ex: `user@example.com`)

## Configuration de l'application

### Méthode 1 : Utiliser un fichier .env (RECOMMANDÉ)

1. **Créez votre fichier .env** à partir du template :
```bash
cd backend
copy .env.example .env
```

2. **Éditez le fichier .env** avec vos vrais identifiants Zoom :
```
ZOOM_ACCOUNT_ID=votre_account_id_reel
ZOOM_CLIENT_ID=votre_client_id_reel
ZOOM_CLIENT_SECRET=votre_client_secret_reel
ZOOM_USER_ID=votre.email@example.com
```

3. **Chargez les variables avant de lancer l'application** :

**PowerShell :**
```powershell
cd backend
. .\set-env.ps1
```

**Command Prompt (CMD) :**
```cmd
cd backend
call set-env.bat
```

4. **Lancez ensuite votre application** dans le même terminal :
```powershell
.\mvnw.cmd spring-boot:run
```

> ⚠️ **Le fichier .env est dans .gitignore** - vos identifiants ne seront jamais commités !

### Méthode 2 : Variables d'environnement manuelles

Définissez les variables d'environnement directement (moins pratique) :

```bash
export ZOOM_CLIENT_ID=votre_client_id
export ZOOM_CLIENT_SECRET=votre_client_secret
export ZOOM_ACCOUNT_ID=votre_account_id
export ZOOM_USER_ID=email@example.com
```

Sous Windows PowerShell :
```powershell
$env:ZOOM_CLIENT_ID="votre_client_id"
$env:ZOOM_CLIENT_SECRET="votre_client_secret"
$env:ZOOM_ACCOUNT_ID="votre_account_id"
$env:ZOOM_USER_ID="email@example.com"
```

### Méthode 3 : Modifier application.properties (NON RECOMMANDÉ)

Vous pouvez également modifier directement le fichier `backend/src/main/resources/application.properties` :

```properties
zoom.api.client-id=votre_client_id
zoom.api.client-secret=votre_client_secret
zoom.api.account-id=votre_account_id
zoom.api.user-id=email@example.com
```

**⚠️ ATTENTION : Ne commitez JAMAIS vos credentials dans Git !**

## Fonctionnement

Lorsque le frontend envoie une requête `GET /api/meetings`, le backend :

1. **Interroge l'API Zoom** pour récupérer les meetings passés des 5 derniers jours
2. **Authentifie** automatiquement avec OAuth Server-to-Server
3. **Synchronise** les nouveaux meetings dans la base de données locale
4. **Retourne** toutes les données (synchronisées + existantes) au frontend

### Endpoints API

- `GET /api/meetings` : Récupère tous les meetings (avec synchronisation Zoom automatique)
- `GET /api/meetings/{id}` : Récupère un meeting spécifique
- `GET /api/meetings/upcoming` : Récupère les meetings à venir
- `POST /api/meetings` : Crée un nouveau meeting
- `PUT /api/meetings/{id}` : Met à jour un meeting
- `DELETE /api/meetings/{id}` : Supprime un meeting

### Synchronisation automatique

La synchronisation avec Zoom est effectuée automatiquement à chaque appel de `GET /api/meetings`.

Le système :
- Vérifie si les meetings existent déjà en base (via `zoom_meeting_id`)
- N'ajoute que les nouveaux meetings
- Enregistre les informations : sujet, durée, hôte, dates, etc.
- Gère la pagination pour récupérer tous les meetings
- Continue de fonctionner même en cas d'erreur avec l'API Zoom (retourne les données en base)

## Dépannage

### Erreur d'authentification

Si vous obtenez une erreur d'authentification :
1. Vérifiez que vos credentials sont corrects
2. Assurez-vous que l'application est activée sur le Zoom Marketplace
3. Vérifiez que les scopes sont correctement configurés

### Pas de meetings retournés

Si aucun meeting n'est retourné :
1. Vérifiez que l'User ID est correct
2. Assurez-vous qu'il y a bien des meetings passés dans les 5 derniers jours
3. Vérifiez les logs de l'application pour plus de détails

### Logs

Les logs sont configurés au niveau DEBUG pour le package `com.zoom`.
Vérifiez la console pour voir les détails de la synchronisation.
