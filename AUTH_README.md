# Authentification - Guide d'utilisation

## 🔐 Vue d'ensemble

L'application Zoom Meetings dispose maintenant d'un système d'authentification JWT complet.

## 📋 Fonctionnalités

- ✅ Authentification par username/password
- ✅ Token JWT avec expiration de 24 heures
- ✅ Protection des routes frontend
- ✅ Protection des endpoints backend
- ✅ Liaison avec Zoom User ID / Account ID
- ✅ Déconnexion automatique en cas de token expiré

## 🚀 Démarrage rapide

### 1. Identifiants par défaut

Au premier démarrage du backend, un utilisateur admin est créé automatiquement :

- **Username** : `admin`
- **Password** : `admin123`

⚠️ **Important** : Changez ce mot de passe en production !

### 2. Connexion

1. Accédez à `http://localhost:5173/login`
2. Saisissez vos identifiants
3. Vous serez redirigé vers la page d'accueil

### 3. Navigation

- **Page d'accueil** (`/`) : Accessible sans authentification
- **Page Réunions** (`/meetings`) : Nécessite une authentification

## 🔧 Configuration

### Backend

Le backend crée automatiquement un utilisateur lié à votre compte Zoom lors du démarrage.
Les informations proviennent du fichier `.env` :

```env
ZOOM_USER_ID=votre_zoom_user_id
ZOOM_ACCOUNT_ID=votre_zoom_account_id
```

### Sécurité JWT

Par défaut, les paramètres JWT sont :
- **Secret** : défini dans `application.properties` (`jwt.secret`)
- **Expiration** : 24 heures (`jwt.expiration=86400000`)

Pour modifier ces valeurs, éditez `backend/src/main/resources/application.properties` :

```properties
jwt.secret=VotreClefSecreteTresLongueEtSecurisee
jwt.expiration=86400000
```

## 💡 Utilisation avancée

### Créer un nouvel utilisateur

Vous pouvez créer des utilisateurs supplémentaires via l'API :

```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "nouveau_user",
  "password": "mot_de_passe_securise",
  "zoomUserId": "zoom_user_id_optionnel",
  "zoomAccountId": "zoom_account_id_optionnel"
}
```

### Vérification Zoom

Chaque utilisateur peut être lié à :
- Un **Zoom User ID** : pour accéder aux meetings d'un utilisateur spécifique
- Un **Zoom Account ID** : pour identifier le compte Zoom

Ces informations sont retournées lors de la connexion et peuvent être utilisées pour des vérifications supplémentaires.

## 🛡️ Sécurité

### Protection des routes

**Frontend** :
- Toutes les routes sensibles sont protégées par `ProtectedRoute`
- Le token est stocké dans `localStorage`
- Redirection automatique vers `/login` si non authentifié

**Backend** :
- Spring Security protège tous les endpoints sauf `/api/auth/**`
- Filtre JWT vérifie le token sur chaque requête
- Session stateless (pas de session côté serveur)

### Gestion des erreurs

- **401 Unauthorized** : Token invalide ou expiré → redirection vers login
- **Token expiré** : L'utilisateur doit se reconnecter

## 📝 Structure des fichiers

### Backend
```
backend/src/main/java/com/zoom/
├── entity/
│   └── User.java                    # Entité utilisateur
├── repository/
│   └── UserRepository.java          # Repository JPA
├── service/
│   └── AuthService.java             # Service d'authentification
├── controller/
│   └── AuthController.java          # Endpoints auth
├── security/
│   ├── JwtTokenProvider.java        # Gestion des JWT
│   ├── JwtAuthenticationFilter.java # Filtre de validation
│   └── SecurityConfig.java          # Configuration Spring Security
├── dto/
│   ├── LoginRequest.java
│   └── AuthResponse.java
└── init/
    └── DataInitializer.java         # Création user par défaut
```

### Frontend
```
frontend/src/
├── contexts/
│   └── AuthContext.tsx              # Context React pour auth
├── services/
│   ├── authService.ts               # Service API auth
│   └── api.ts                       # Intercepteur JWT
├── pages/
│   └── Login.tsx                    # Page de connexion
├── components/
│   ├── ProtectedRoute.tsx           # Protection des routes
│   └── Header.tsx                   # Menu avec déconnexion
└── App.tsx                          # Routing avec protection
```

## 🔍 Dépannage

### "401 Unauthorized" après login

- Vérifiez que le token est bien stocké dans `localStorage`
- Vérifiez la configuration CORS dans `SecurityConfig.java`
- Consultez les logs backend pour plus de détails

### Token expiré trop rapidement

Modifiez `jwt.expiration` dans `application.properties` :
```properties
# 7 jours au lieu de 24h
jwt.expiration=604800000
```

### Impossible de se connecter

- Vérifiez que le backend est démarré
- Vérifiez les identifiants (admin/admin123 par défaut)
- Consultez les logs backend pour voir les tentatives de connexion

## 🎯 Prochaines étapes

Pour aller plus loin, vous pourriez :

1. ✨ Ajouter l'enregistrement d'utilisateurs
2. 🔄 Implémenter le refresh token
3. 👥 Ajouter des rôles (admin, user, etc.)
4. 🔐 Authentification OAuth avec Zoom directement
5. 📧 Système de récupération de mot de passe

---

**Note** : Ce système d'authentification est fonctionnel mais basique. Pour une application en production, considérez :
- Stockage sécurisé des mots de passe (déjà fait avec BCrypt)
- HTTPS obligatoire
- Rate limiting sur les tentatives de connexion
- Audit des connexions
- 2FA (authentification à deux facteurs)
