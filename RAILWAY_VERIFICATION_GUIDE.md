# 🔍 Vérification de la Configuration DATABASE_URL sur Railway

## 📋 Méthodes de Vérification

### 1️⃣ Vérifier via le Dashboard Railway

1. **Aller dans Railway.app Dashboard**
   - Ouvrir https://railway.app
   - Sélectionner votre projet "zoom"
   - Aller à l'onglet **Variables**

2. **Vérifier les variables d'environnement**
   - `DATABASE_URL` doit être présent et visible
   - Format attendu: `postgres://user:password@host.railway.app:5432/railway`
   - L'addon PostgreSQL crée automatiquement cette variable

3. **Logs du déploiement**
   - Aller à l'onglet **Logs**
   - Chercher le message de démarrage:
     ```
     ✅ Converted DATABASE_URL to JDBC
     ```
   - Si absent: La conversion n'a pas fonctionné
   - Chercher les erreurs de connexion:
     ```
     Connection to localhost:5432 refused
     ```
   - Cela signifie que `DATABASE_URL` n'était pas utilisé

### 2️⃣ Vérifier via l'Endpoint de Diagnostic

Une fois le backend déployé, vous pouvez appeler l'endpoint de diagnostic:

```bash
# Via curl
curl https://zoom-xxxx.railway.app/api/health/database-config

# Via PowerShell
Invoke-RestMethod -Uri "https://zoom-xxxx.railway.app/api/health/database-config"
```

**Réponse attendue:**
```json
{
  "database_url_set": true,
  "database_url_format": "postgres://",
  "database_url_masked": "postgres://user:****@host.railway.app:5432/railway",
  "jdbc_database_url_set": true,
  "jdbc_database_url_masked": "jdbc:postgresql://user:****@host.railway.app:5432/railway",
  "active_profiles": "railway",
  "datasource_url_configured": true,
  "datasource_url_masked": "jdbc:postgresql://user:****@host.railway.app:5432/railway",
  "driver_class": "org.postgresql.Driver",
  "port": "assigned_port",
  "hikari_max_pool_size": "10"
}
```

**Problèmes courants et solutions:**
- ❌ `database_url_set: false` → DATABASE_URL n'existe pas dans Railway
  - Solution: Vérifier que l'addon PostgreSQL est lié au service backend
  - Aller à Variables et créer manuellement si nécessaire

- ❌ `database_url_format: "unknown"` → Format non reconnu
  - Solution: Vérifier le format exact dans Railway dashboard

- ❌ `jdbc_database_url_set: false` → Conversion échouée
  - Solution: Vérifier que le Dockerfile contient la conversion
  - Forcer un redéploiement

### 3️⃣ Vérifier via les Logs de Démarrage

Les logs doivent afficher (dans cet ordre):

```
Starting ZoomBackendApplication v0.0.1-SNAPSHOT...
The following 1 profile is active: "railway"
✅ Converted DATABASE_URL to JDBC
DataSource configuration:
  spring.datasource.url: jdbc:postgresql://...
  driver-class-name: org.postgresql.Driver
```

**Chercher ces erreurs:**
```
❌ "Connection to localhost:5432 refused"
   → DATABASE_URL n'est pas converti correctement

❌ "/app/start.sh: No such file or directory"
   → Le script externe manque (bug résolu dans Dockerfile)

❌ "HikariPool-1 - Connection is not available"
   → DATABASE_URL est mal configuré ou inaccessible
```

### 4️⃣ Vérifier via le Script de Diagnostic (en local ou SSH)

**Sur votre machine locale:**
```bash
cd backend
bash verify-database-config.sh
```

**En SSH dans le container Railway (si activé):**
```bash
# Dans le terminal Railway
/bin/sh -c 'source /app/verify-database-config.sh'
```

### 5️⃣ Checklist de Vérification

- [ ] PostgreSQL addon est créé dans Railway
- [ ] Backend service est lié à l'addon PostgreSQL (vérifier dans Railway dashboard)
- [ ] Aller à Variables du service backend
- [ ] `DATABASE_URL` existe avec format `postgres://...`
- [ ] Dockerfile contient la conversion sed:
  ```dockerfile
  CMD ["sh", "-c", "if [ -n \"$DATABASE_URL\" ]; then export JDBC_DATABASE_URL=$(echo $DATABASE_URL | sed 's|^postgres://|jdbc:postgresql://|'); echo '✅ Converted DATABASE_URL to JDBC'; fi && java -Dserver.port=${PORT:-8080} -jar app.jar"]
  ```
- [ ] `application-railway.properties` contient:
  ```properties
  spring.datasource.url=${JDBC_DATABASE_URL:${DATABASE_URL:...}}
  spring.jpa.hibernate.ddl-auto=update
  ```
- [ ] Logs montrent "✅ Converted DATABASE_URL to JDBC"
- [ ] Endpoint `/api/health/database-config` retourne un JSON valide

## 🚀 Actions si Quelque Chose ne Fonctionne Pas

### Problème: DATABASE_URL n'existe pas
**Solution 1: Ajouter manuellement dans Railway**
1. Aller à Settings → Variables
2. Cliquer sur "New Variable"
3. Clé: `DATABASE_URL`
4. Valeur: Copier-coller depuis l'addon PostgreSQL
5. Sauvegarder et redéployer

**Solution 2: Vérifier l'addon PostgreSQL**
1. Aller à l'addon PostgreSQL dans Railway
2. Vérifier que "Connected Services" inclut le backend
3. Si absent, cliquer "Connect" pour lier l'addon

### Problème: Dockerfile n'a pas le CMD correct
1. Vérifier le contenu local:
   ```bash
   cat backend/Dockerfile
   ```
2. S'assurer que `CMD` (pas `ENTRYPOINT`) contient la conversion
3. Forcer le push et redéploiement:
   ```bash
   git push --force
   ```

### Problème: Logs montrent "Connection to localhost"
- APPLICATION N'UTILISE PAS DATABASE_URL
- Solutions:
  1. Redéployer après fix du Dockerfile
  2. Vérifier que `spring.profiles.active=railway` est défini
  3. Vérifier que `application-railway.properties` existe

## 📊 Sequence Complète de Démarrage Attendue

```
1. [inf] Starting Container
2. [inf] Starting ZoomBackendApplication v0.0.1-SNAPSHOT
3. [inf] The following 1 profile is active: "railway"
4. [inf] 🔍 DATABASE_URL Configuration Diagnostic
5. [inf] ✅ DATABASE_URL is SET
6. [inf] Original format: postgres://user:****@host:5432/railway
7. [inf] Dockerfile will convert to jdbc:postgresql://
8. [inf] After conversion: jdbc:postgresql://user:****@host:5432/railway
9. [inf] Active Spring Profiles: railway
10. [inf] DataSource Configuration:
11. [inf]    spring.datasource.url: jdbc:postgresql://...
12. [inf]    hikari.max-pool-size: 10
13. [inf] HibernateJpaConfiguration attempting connection...
14. [inf] ✅ Connection successful!
15. [inf] Tomcat started on port 12345 (HTTPS)
```

Si vous voyez à la place:
```
[inf] Connection to localhost:5432 refused
```
= DATABASE_URL n'est pas correctement configuré

## 🔐 Sécurité

- L'endpoint `/api/health/database-config` masque les mots de passe
- En production, vous devriez ajouter une authentification:
  ```java
  // Dans SecurityConfig.java
  .antMatchers("/api/health/database-config").hasRole("ADMIN")
  ```
- Ou le désactiver entièrement en production
