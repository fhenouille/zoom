# Projet Full-Stack React + Spring Boot

## 📋 Description

Projet full-stack avec :
- **Frontend** : React 18 + TypeScript
- **Backend** : Spring Boot 3.x + JPA + H2 Database

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

## 🔒 Gestion des données personnelles (RGPD)

Cette application utilise l’API Zoom pour récupérer les informations de participation aux réunions (nom original du participant, durée de connexion, heure, etc.) afin de calculer des statistiques d’audience.
Les données nominatives sont **conservées pour une durée maximale de 90 jours**, puis **supprimées automatiquement**. Seul le nombre total de participants est conservé à des fins statistiques.
Aucune donnée personnelle n’est partagée avec des tiers. Les informations sont stockées de manière sécurisée et utilisées exclusivement dans le cadre de suivi interne des réunions.

> La collecte initiale des données et la base légale sont gérées par Zoom conformément à ses conditions d’utilisation et sa politique de confidentialité.
