# Guide de Démarrage Sans Maven

## ⚠️ Maven n'est pas installé

Pour démarrer le backend, vous avez **3 options** :

### Option 1 : Installer Maven (Recommandé)

**Avec Chocolatey (le plus simple) :**
```powershell
# Installer Chocolatey si ce n'est pas déjà fait
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Installer Maven
choco install maven
```

**Installation manuelle :**
1. Télécharger Maven : https://maven.apache.org/download.cgi
2. Extraire dans `C:\Program Files\Apache\maven`
3. Ajouter au PATH : `C:\Program Files\Apache\maven\bin`
4. Redémarrer le terminal
5. Vérifier : `mvn --version`

Puis lancez :
```powershell
cd backend
mvn spring-boot:run
```

### Option 2 : Utiliser Docker (Le plus facile)

Si vous avez Docker installé :

```powershell
docker-compose up --build
```

✅ Cela lance automatiquement le frontend ET le backend sans besoin de Maven !

**Accès :**
- Frontend : http://localhost:5173
- Backend : http://localhost:8080/api/meetings

### Option 3 : Compiler manuellement avec Java

Si Maven n'est pas disponible mais que vous avez Java :

```powershell
cd backend\src\main\java
javac -cp "..\..\..\..\..\.m2\repository\**\*.jar" com\zoom\*.java
java com.zoom.ZoomBackendApplication
```

⚠️ Cette option est complexe et n'est pas recommandée.

---

## 🚀 Démarrage Rapide avec Docker (RECOMMANDÉ)

La solution la plus simple si vous n'avez pas Maven :

1. **Installer Docker Desktop** : https://www.docker.com/products/docker-desktop/

2. **Lancer le projet** :
   ```powershell
   docker-compose up --build
   ```

3. **Accéder à l'application** :
   - Frontend : http://localhost:5173
   - Backend API : http://localhost:8080/api/meetings
   - Console H2 : http://localhost:8080/h2-console

4. **Arrêter** :
   ```powershell
   docker-compose down
   ```

---

## 🔧 Frontend uniquement (sans backend)

Si vous voulez juste tester le frontend :

```powershell
cd frontend
npm run dev
```

Le frontend utilisera des données mockées ou affichera des erreurs API (normal sans backend).

---

## 📚 Résumé

| Méthode | Prérequis | Complexité | Recommandation |
|---------|-----------|------------|----------------|
| Docker Compose | Docker Desktop | ⭐ Facile | ✅ **Recommandé** |
| Maven | Maven + Java | ⭐⭐ Moyen | ✅ Pour dev |
| Frontend seul | Node.js | ⭐ Facile | Pour tester UI |

**👉 Pour commencer rapidement : Utilisez Docker !**
