# Déploiement Frontend sur GitHub Pages

## 🚀 Configuration pour GitHub Pages

Votre frontend Zoom est maintenant configuré pour être déployé sur GitHub Pages.

### Configuration effectuée

1. **`homepage` dans `package.json`** : `https://fhenouille.github.io/zoom`
2. **`base` dans `vite.config.ts`** : `/zoom/` (chemin de base pour GitHub Pages)
3. **Scripts npm** :
   - `npm run deploy` : Construction et déploiement
   - `npm run predeploy` : Lance la build avant le déploiement
4. **Dépendance `gh-pages`** : Ajoutée au `package.json`
5. **GitHub Actions** : Workflow automatique pour déployer à chaque push

## 📦 Installation des dépendances

Avant de déployer, installez les dépendances :

```bash
cd frontend
npm install
```

## 🎯 Déploiement manuel (local)

### Méthode 1 : Avec gh-pages CLI (recommended)

```bash
cd frontend
npm install              # Si pas déjà fait
npm run deploy          # Compile et déploie automatiquement
```

**Prérequis** : Avoir `gh-pages` installé localement (déjà dans `package.json`)

### Méthode 2 : Déploiement automatique avec GitHub Actions

Simplement push sur la branche `main` :

```bash
git add .
git commit -m "Configuration GitHub Pages"
git push origin main
```

Le workflow `.github/workflows/deploy-frontend.yml` se déclenchera automatiquement et déploiera le site.

## ⚙️ Configuration GitHub

### 1. Activer GitHub Pages

1. Allez dans **Settings** de votre repo
2. Aller à **Pages** (dans la sidebar)
3. Source : Sélectionner `Deploy from a branch`
4. Branch : Sélectionner `gh-pages` et folder `root`
5. Cliquer **Save**

### 2. Configurer un domaine personnalisé (optionnel)

Si vous avez un domaine personnalisé :

1. Dans **Settings > Pages**
2. Ajouter votre domaine dans **Custom domain**
3. Configurer les DNS records chez votre registraire

## 🔗 URL d'accès

Après le déploiement, votre site sera accessible à :

- **Default** : `https://fhenouille.github.io/zoom`
- **Custom domain** : `https://zoom.fhenouille.com` (si configuré)

## 🔧 Variables d'environnement

Si votre frontend doit communiquer avec le backend :

### Développement local
- Backend à `http://localhost:8080`
- La requête proxy est configurée dans `vite.config.ts`

### En production (GitHub Pages)
- Vous devez pointer vers votre backend en production
- Créer un fichier `.env.production` :

```bash
VITE_API_BASE_URL=https://votre-backend.com/api
```

Puis dans votre code :

```typescript
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
```

## 📝 Points importants

### React Router et GitHub Pages

Puisque le site est dans un sous-dossier (`/zoom/`), assurez-vous que :

1. **`base` est configuré** dans `vite.config.ts` ✅
2. **Routes relatives** sont utilisées
3. **Assets** utilisent des chemins relatifs

### Mode Hash Router (optionnel)

Si vous avez des problèmes avec le routing, utilisez HashRouter au lieu de BrowserRouter :

```typescript
// frontend/src/App.tsx
import { HashRouter } from 'react-router-dom';

<HashRouter>
  <Routes>
    {/* ... */}
  </Routes>
</HashRouter>
```

Cela changera les URLs en `#/meetings` au lieu de `/meetings`.

## 🚨 Dépannage

### "404 Not Found" après déploiement

**Cause** : GitHub Pages ne comprend pas les routes React

**Solutions** :
1. Ajouter un fichier `public/404.html` qui redirige vers `index.html`
2. Ou utiliser `HashRouter` au lieu de `BrowserRouter`

### Le site ne se met pas à jour

1. Vider le cache du navigateur (Ctrl+Shift+Delete)
2. Attendre ~2-5 minutes après le push
3. Vérifier que le workflow GitHub Actions s'est exécuté

### CORS errors en production

Si vous avez des erreurs CORS :

1. Vérifier que `VITE_API_BASE_URL` pointe vers votre backend
2. Configurer CORS sur votre backend pour accepter `https://fhenouille.github.io`

## 📚 Ressources

- [GitHub Pages Documentation](https://docs.github.com/en/pages)
- [Vite - Deploying a static site](https://vitejs.dev/guide/static-deploy.html)
- [gh-pages npm package](https://www.npmjs.com/package/gh-pages)

## ✅ Checklist de déploiement

- [ ] Installer les dépendances : `npm install`
- [ ] Tester localement : `npm run dev`
- [ ] Build fonctionne : `npm run build`
- [ ] GitHub Pages activé dans Settings
- [ ] Secrets GitHub configurés (si besoin)
- [ ] Premier déploiement : `npm run deploy` ou push sur main
- [ ] Vérifier le site à `https://fhenouille.github.io/zoom`
- [ ] Configurer domaine custom (optionnel)

---

**Note** : Après le premier déploiement, chaque push sur `main` avec des changements dans le dossier `frontend/` déclenchera automatiquement un nouveau déploiement !
