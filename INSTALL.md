# 🚀 Guide d'Installation Rapide - Zoom Meetings

## ⚠️ Problème d'Exécution de Script PowerShell

Si vous rencontrez l'erreur "l'exécution de scripts est désactivée", vous avez plusieurs options :

### Option 1 : Autoriser l'exécution temporaire (Recommandé)

Exécutez cette commande dans PowerShell **en tant qu'administrateur** :

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

Puis relancez :
```powershell
.\init-project.ps1
```

### Option 2 : Exécution ponctuelle (Sans changer la politique)

```powershell
powershell -ExecutionPolicy Bypass -File .\init-project.ps1
```

### Option 3 : Installation manuelle (Si les scripts ne fonctionnent pas)

Suivez ces étapes :

#### 1️⃣ Installer les dépendances Frontend

```powershell
cd frontend
npm install
cd ..
```

#### 2️⃣ Vérifier Java et Maven

```powershell
# Vérifier Java (doit être 17+)
java -version

# Vérifier que Maven Wrapper existe
cd backend
dir mvnw.cmd
cd ..
```

C'est tout ! Les dépendances Maven seront téléchargées automatiquement au premier lancement du backend.

---

## 🚀 Démarrer l'Application

### Méthode 1 : Avec VS Code Tasks (Le plus simple)

1. Ouvrez VS Code dans ce dossier
2. Appuyez sur `Ctrl+Shift+P`
3. Tapez "Tasks: Run Task"
4. Sélectionnez **"Full Stack: Start Dev"**

✅ Le frontend et le backend démarreront automatiquement !

### Méthode 2 : Manuellement

**Terminal 1 - Backend :**
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

**Terminal 2 - Frontend :**
```powershell
cd frontend
npm run dev
```

### Méthode 3 : Avec Docker

```powershell
docker-compose up --build
```

---

## 🌐 Accéder à l'Application

Une fois démarré :

- **Frontend** : http://localhost:5173
- **API Backend** : http://localhost:8080/api/meetings
- **Console H2** : http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:zoomdb`
  - Username: `sa`
  - Password: (laisser vide)

---

## 📋 Prérequis

Vérifiez que vous avez :

```powershell
# Node.js 18+
node --version

# Java 17+
java -version

# npm
npm --version
```

Si quelque chose manque :
- **Node.js** : https://nodejs.org/
- **Java JDK 17+** : https://adoptium.net/

---

## 🆘 Dépannage Rapide

### Le frontend ne démarre pas

```powershell
cd frontend
Remove-Item -Recurse -Force node_modules
Remove-Item package-lock.json
npm install
npm run dev
```

### Le backend ne démarre pas

```powershell
cd backend
.\mvnw.cmd clean
.\mvnw.cmd spring-boot:run
```

### Port déjà utilisé

Changez les ports dans :
- Backend : `backend/src/main/resources/application.properties` (ligne `server.port=8080`)
- Frontend : `frontend/vite.config.ts` (section `server.port`)

---

## 📚 Plus d'Informations

- Guide complet : `GETTING_STARTED.md`
- Vue d'ensemble : `PROJECT_SUMMARY.md`
- Documentation : `README.md`

---

**Bon développement ! 🎉**
