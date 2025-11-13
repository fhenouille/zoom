# Projet Full-Stack React + Spring Boot

## 📋 Description

Projet full-stack moderne avec :
- **Frontend** : React 18 + TypeScript + Vite + TailwindCSS + React Router
- **Backend** : Spring Boot 3.x + JPA + H2 Database
- **Containerisation** : Docker + Docker Compose

## 🏗️ Structure du Projet

```
zoom/
├── frontend/           # Application React
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.ts
├── backend/            # API Spring Boot
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   ├── pom.xml
│   └── mvnw
├── .github/
│   ├── copilot-instructions.md
│   └── prompts/
├── .vscode/            # Configuration VS Code
│   ├── settings.json
│   ├── extensions.json
│   ├── launch.json
│   └── tasks.json
├── docker-compose.yml
└── README.md
```

## Configuration GitHub Copilot

Ce projet est configuré avec GitHub Copilot pour améliorer la productivité du développement. Les instructions personnalisées pour Copilot se trouvent dans `.github/copilot-instructions.md`.

### Fichiers de configuration

- `.github/copilot-instructions.md` - Instructions personnalisées pour GitHub Copilot
- `.vscode/settings.json` - Configuration VS Code avec paramètres Copilot
- `.vscode/extensions.json` - Extensions recommandées
- `.editorconfig` - Configuration de l'éditeur pour la cohérence du code
- `.github/PULL_REQUEST_TEMPLATE.md` - Template pour les pull requests

## 🚀 Démarrage Rapide

### Prérequis

- **Node.js** 18+ et npm
- **Java** 17+
- **Maven** (ou utiliser le Maven Wrapper inclus)
- **Docker** et Docker Compose (optionnel)

### Option 1 : Développement Local

#### Backend (Spring Boot)

```powershell
cd backend

# Compiler et lancer le backend
.\mvnw.cmd spring-boot:run

# Ou utiliser la tâche VS Code : Terminal > Run Task > Backend: Run Spring Boot
```

Le backend sera accessible sur : `http://localhost:8080`
Console H2 : `http://localhost:8080/h2-console`

#### Frontend (React)

```powershell
cd frontend

# Installer les dépendances
npm install

# Copier le fichier d'environnement
copy .env.example .env

# Lancer le serveur de développement
npm run dev

# Ou utiliser la tâche VS Code : Terminal > Run Task > Frontend: Dev Server
```

Le frontend sera accessible sur : `http://localhost:5173`

#### Lancer les deux en même temps

**Avec VS Code Tasks** :
- Ouvrir la palette de commandes : `Ctrl+Shift+P`
- Taper : `Tasks: Run Task`
- Sélectionner : `Full Stack: Start Dev`

**Avec VS Code Debugger** :
- Aller dans l'onglet Run & Debug (`Ctrl+Shift+D`)
- Sélectionner : `Full Stack (Frontend + Backend)`
- Cliquer sur le bouton play ▶️

### Option 2 : Docker Compose

```powershell
# Construire et lancer tous les services
docker-compose up -d

# Voir les logs
docker-compose logs -f

# Arrêter tous les services
docker-compose down
```

## 🧪 Tests

### Frontend

```powershell
cd frontend
npm test              # Tests unitaires
npm run test:coverage # Avec couverture
```

### Backend

```powershell
cd backend
.\mvnw.cmd test       # Tests JUnit
```

## 📡 API Endpoints

### Users API

- **GET** `/api/users` - Liste tous les utilisateurs
- **POST** `/api/users` - Créer un nouvel utilisateur
  ```json
  {
    "name": "John Doe",
    "email": "john@example.com"
  }
  ```

## 🛠️ Configuration

### Variables d'Environnement

**Frontend** (`frontend/.env`)
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=Zoom App
```

**Backend** (`backend/src/main/resources/application.properties`)
```properties
server.port=8080
spring.datasource.url=jdbc:h2:mem:testdb
app.cors.allowed-origins=http://localhost:5173
```

## 📦 Build de Production

### Frontend

```powershell
cd frontend
npm run build
# Les fichiers sont dans frontend/dist/
```

### Backend

```powershell
cd backend
.\mvnw.cmd package -DskipTests
# Le JAR est dans backend/target/
```

## 🔧 Tâches VS Code Disponibles

- `Frontend: Install Dependencies` - Installer les dépendances npm
- `Frontend: Dev Server` - Lancer le serveur de développement Vite
- `Frontend: Build` - Build de production
- `Frontend: Test` - Lancer les tests
- `Backend: Clean` - Nettoyer le projet Maven
- `Backend: Compile` - Compiler le projet
- `Backend: Test` - Lancer les tests JUnit
- `Backend: Run Spring Boot` - Lancer l'application
- `Backend: Package` - Créer le JAR
- `Docker: Build All` - Construire les images Docker
- `Docker: Start All` - Démarrer avec Docker Compose
- `Docker: Stop All` - Arrêter les conteneurs
- `Full Stack: Start Dev` - Lancer frontend + backend

## 🔌 Extensions VS Code Recommandées

Le projet recommande automatiquement les extensions suivantes :
- GitHub Copilot & Copilot Chat
- ESLint & Prettier
- TailwindCSS IntelliSense
- Java Extension Pack
- Spring Boot Extension Pack
- Docker
- REST Client

## 📝 Scripts npm Disponibles

```powershell
npm run dev          # Serveur de développement
npm run build        # Build de production
npm run preview      # Prévisualiser le build
npm run test         # Tests
npm run lint         # Linter le code
npm run format       # Formatter avec Prettier
```

## 🤝 Contribution

1. Créer une branche : `git checkout -b feature/ma-feature`
2. Commiter : `git commit -m 'Ajout de ma feature'`
3. Pusher : `git push origin feature/ma-feature`
4. Créer une Pull Request

## 📄 License

[Votre License]

## 🆘 Support

Pour toute question ou problème, créez une issue sur GitHub.

---

**Bon développement ! 🚀**
