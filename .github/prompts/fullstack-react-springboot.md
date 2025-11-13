# Projet Full-Stack React + Spring Boot

Crée un projet complet avec :

## 1. Front-end React :
- Utilise React 18 avec Vite pour le bundling.
- Configure TypeScript.
- Ajoute un système de routing avec React Router v6.
- Intègre le state management jotai couplé avec la librairie jotai-tanstack-query pour la gestion des requêtes.
- Prépare une structure de composants réutilisables moderne.
- Ajoute un thème avec ant design.
- Configure un appel API vers le backend (base URL configurable via .env).
- Ajoute un exemple de page : `Home` et `Meetings` (liste des réunions récupérée via API REST).

## 2. Back-end Java Spring Boot :
- Crée un projet Spring Boot (version 3.x) avec Maven.
- Configure les dépendances : `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, et `spring-boot-devtools`.
- Ajoute une base H2 en mémoire pour les tests.
- Crée une entité `Meeting` avec les champs : `id`, `start`, `end`.
- Crée un repository JPA pour `Meeting`.
- Crée un service pour la logique métier.
- Crée un contrôleur REST exposant l'endpoint :
    - `GET /api/meetings` : retourne la liste des réunions.
- Active CORS pour permettre les appels depuis le front.
- Ajoute un fichier `application.properties` avec la configuration H2 et le port (par défaut 8080).

## 3. Instructions supplémentaires :
- Génère un script `docker-compose.yml` pour lancer le front et le back ensemble.
- Ajoute un README.md expliquant comment démarrer le projet (commandes pour le front et le back).
- Prépare une structure de dossiers claire :
    - `/frontend` pour React
    - `/backend` pour Spring Boot
- Ajoute des tests unitaires simples pour le back (JUnit) et pour le front (React Testing Library).
