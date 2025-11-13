# 🚀 Guide de Démarrage - Projet Full-Stack Zoom Meetings

## 📋 Prérequis

Avant de commencer, assurez-vous d'avoir installé :

- **Node.js** 18+ et npm
- **Java JDK** 17+
- **Maven** (ou utilisez le Maven Wrapper inclus)
- **Docker** et Docker Compose (optionnel)
- **Git**

## 🏗️ Installation du Projet

### 1. Cloner le repository (si nécessaire)

```powershell
git clone <url-du-repo>
cd zoom
```

### 2. Installation du Frontend

```powershell
cd frontend
npm install
```

### 3. Vérification du Backend

Le backend utilise Maven et toutes les dépendances seront téléchargées automatiquement au premier lancement.

## ▶️ Démarrage du Projet

### Option 1 : Démarrage Manuel

#### Démarrer le Backend

```powershell
# Depuis le dossier racine
cd backend

# Avec Maven Wrapper (recommandé)
.\mvnw.cmd spring-boot:run

# OU avec Maven installé
mvn spring-boot:run
```

Le backend démarre sur **http://localhost:8080**

Endpoints disponibles :
- API : http://localhost:8080/api/meetings
- Console H2 : http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:zoomdb`
  - Username: `sa`
  - Password: (laisser vide)

#### Démarrer le Frontend

Dans un **nouveau terminal** :

```powershell
# Depuis le dossier racine
cd frontend
npm run dev
```

Le frontend démarre sur **http://localhost:5173**

### Option 2 : Avec VS Code Tasks

1. Ouvrir VS Code
2. Appuyer sur `Ctrl+Shift+P`
3. Taper "Tasks: Run Task"
4. Sélectionner **"Full Stack: Start Dev"**

Cela lance automatiquement le backend et le frontend !

### Option 3 : Avec VS Code Debugger

1. Aller dans l'onglet **Run & Debug** (`Ctrl+Shift+D`)
2. Sélectionner **"Full Stack (Frontend + Backend)"**
3. Cliquer sur le bouton **▶️ Start Debugging**

### Option 4 : Avec Docker Compose

```powershell
# Construire et lancer
docker-compose up --build

# En arrière-plan
docker-compose up -d

# Voir les logs
docker-compose logs -f

# Arrêter
docker-compose down
```

## 🧪 Exécution des Tests

### Tests Frontend

```powershell
cd frontend

# Lancer les tests
npm test

# Avec interface UI
npm run test:ui

# Avec couverture
npm run test:coverage
```

### Tests Backend

```powershell
cd backend

# Avec Maven Wrapper
.\mvnw.cmd test

# OU avec Maven
mvn test
```

## 📦 Build de Production

### Frontend

```powershell
cd frontend
npm run build
```

Les fichiers optimisés seront dans `frontend/dist/`

### Backend

```powershell
cd backend
.\mvnw.cmd package -DskipTests
```

Le fichier JAR sera dans `backend/target/zoom-backend-0.0.1-SNAPSHOT.jar`

Pour lancer le JAR :
```powershell
java -jar backend\target\zoom-backend-0.0.1-SNAPSHOT.jar
```

## 🔍 Vérification du Bon Fonctionnement

### 1. Vérifier le Backend

Ouvrez http://localhost:8080/api/meetings dans votre navigateur.
Vous devriez voir un JSON avec la liste des réunions.

### 2. Vérifier le Frontend

1. Ouvrez http://localhost:5173
2. Vous devriez voir la page d'accueil
3. Cliquez sur "Voir les réunions" ou le menu "Réunions"
4. Vous devriez voir la liste des réunions récupérée depuis le backend

## 🛠️ Commandes Utiles

### Frontend

```powershell
npm run dev          # Serveur de développement
npm run build        # Build de production
npm run preview      # Prévisualiser le build
npm test             # Tests
npm run lint         # Vérifier le code
```

### Backend

```powershell
.\mvnw.cmd clean              # Nettoyer le projet
.\mvnw.cmd compile            # Compiler
.\mvnw.cmd test               # Tests
.\mvnw.cmd spring-boot:run    # Lancer l'application
.\mvnw.cmd package            # Créer le JAR
```

## 🐛 Résolution de Problèmes

### Le frontend ne peut pas contacter le backend

1. Vérifiez que le backend tourne sur le port 8080
2. Vérifiez le fichier `frontend/.env` :
   ```
   VITE_API_BASE_URL=http://localhost:8080/api
   ```
3. Redémarrez le frontend après modification du .env

### Erreur de compilation Java

1. Vérifiez votre version Java : `java -version` (doit être 17+)
2. Nettoyez le projet : `.\mvnw.cmd clean`
3. Relancez : `.\mvnw.cmd spring-boot:run`

### Erreurs npm

1. Supprimez `node_modules` et `package-lock.json`
2. Réinstallez : `npm install`

### Port déjà utilisé

**Backend (8080)** :
- Modifiez `backend/src/main/resources/application.properties`
- Changez `server.port=8080` vers un autre port

**Frontend (5173)** :
- Modifiez `frontend/vite.config.ts`
- Changez le port dans la section `server`

## 📚 Documentation

- **Structure du projet** : voir `README.md`
- **API Endpoints** : voir la console H2 et les contrôleurs Java
- **Composants React** : voir `frontend/src/components/`

## 🎯 Prochaines Étapes

1. ✅ Vérifier que tout fonctionne
2. 📝 Personnaliser les composants selon vos besoins
3. 🎨 Modifier le thème Ant Design
4. 🔐 Ajouter l'authentification
5. 📊 Ajouter plus de fonctionnalités (création, modification, suppression de réunions)

## 💡 Conseils

- Utilisez les **VS Code Tasks** pour un démarrage rapide
- Installez les **extensions recommandées** pour une meilleure expérience
- Consultez les **logs** en cas d'erreur (terminal backend et frontend)
- Utilisez la **console H2** pour vérifier les données en base

---

**Bon développement ! 🚀**

Si vous rencontrez des problèmes, vérifiez :
1. Les logs du terminal backend
2. Les logs du terminal frontend
3. La console du navigateur (F12)
