# 📚 Bibliothèque de Prompts

Ce dossier contient des prompts réutilisables pour GitHub Copilot.

## 🚀 Utilisation

Pour utiliser un prompt avec GitHub Copilot Chat :

1. **Méthode 1 - Référence de fichier** :
   ```
   @workspace /promptfullstack-react-springboot
   ```

2. **Méthode 2 - Copier-coller** :
   - Ouvrez le fichier du prompt souhaité
   - Copiez son contenu
   - Collez-le dans GitHub Copilot Chat

3. **Méthode 3 - Utilisation directe** :
   - Tapez `#file:` dans Copilot Chat
   - Sélectionnez le fichier de prompt
   - Le contenu sera utilisé comme contexte

## 📋 Liste des Prompts Disponibles

### `fullstack-react-springboot.md`
**Description** : Génère un projet complet avec front-end React (Vite, TypeScript, React Router, TailwindCSS) et back-end Spring Boot (REST API, JPA, H2).

**Utilisation** :
```
@workspace Exécute le prompt dans #file:.github/prompts/fullstack-react-springboot.md
```

---

## ➕ Ajouter un nouveau prompt

1. Créez un nouveau fichier `.md` dans ce dossier avec un nom descriptif (kebab-case)
2. Écrivez votre prompt avec des instructions claires et structurées
3. Ajoutez une entrée dans ce README avec :
   - Le nom du fichier
   - Une description courte
   - Un exemple d'utilisation

### Template pour nouveau prompt :

```markdown
# [Titre du Prompt]

[Description de ce que fait le prompt]

## Instructions :
- [Instruction 1]
- [Instruction 2]
- ...

## Résultat attendu :
[Description du résultat]
```

## 💡 Bonnes Pratiques

- ✅ Utilisez des noms de fichiers descriptifs en kebab-case
- ✅ Structurez vos prompts avec des titres et sous-sections
- ✅ Soyez précis et détaillé dans les instructions
- ✅ Incluez des exemples quand c'est pertinent
- ✅ Documentez les dépendances ou prérequis
- ✅ Mettez à jour ce README à chaque nouveau prompt

## 🔖 Catégories Suggérées

Organisez vos prompts par catégorie en utilisant des préfixes :

- `fullstack-*` : Projets full-stack
- `frontend-*` : Projets frontend uniquement
- `backend-*` : Projets backend uniquement
- `api-*` : APIs et services
- `test-*` : Tests et qualité
- `deploy-*` : Déploiement et DevOps
- `refactor-*` : Refactoring et optimisation
- `doc-*` : Documentation
