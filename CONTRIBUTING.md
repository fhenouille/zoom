# Guide de Contribution

Merci de votre intérêt pour contribuer à ce projet ! 🎉

## Code de Conduite

Ce projet adhère au [Code de Conduite](.github/CODE_OF_CONDUCT.md). En participant, vous vous engagez à respecter ce code.

## Comment contribuer

### Signaler un bug

1. Vérifiez que le bug n'a pas déjà été signalé dans les [Issues](../../issues)
2. Créez une nouvelle issue en utilisant le template de bug
3. Incluez autant de détails que possible :
   - Version utilisée
   - Étapes pour reproduire
   - Comportement attendu vs comportement actuel
   - Captures d'écran si pertinent

### Proposer une fonctionnalité

1. Vérifiez qu'elle n'a pas déjà été proposée
2. Créez une issue décrivant :
   - Le problème que cela résout
   - La solution proposée
   - Des alternatives considérées

### Soumettre des changements

1. **Fork** le repository
2. **Créez une branche** depuis `main` :
   ```bash
   git checkout -b feature/ma-super-feature
   ```
3. **Committez** vos changements :
   ```bash
   git commit -m "feat: ajoute une super feature"
   ```
4. **Poussez** vers votre fork :
   ```bash
   git push origin feature/ma-super-feature
   ```
5. **Ouvrez une Pull Request**

## Standards de Code

### Frontend (React/TypeScript)

- Utilisez TypeScript strict
- Suivez les conventions ESLint configurées
- Utilisez Prettier pour le formatage
- Nommage :
  - Composants : PascalCase (`MyComponent.tsx`)
  - Hooks : camelCase avec préfixe `use` (`useMyHook.ts`)
  - Fichiers utilitaires : camelCase

### Backend (Java/Spring Boot)

- Suivez les conventions Java standard
- Utilisez Lombok pour réduire le boilerplate
- Documentation JavaDoc pour les méthodes publiques
- Nommage :
  - Classes : PascalCase
  - Méthodes : camelCase
  - Constants : UPPER_SNAKE_CASE

### Commits

Utilisez [Conventional Commits](https://www.conventionalcommits.org/fr/) :

- `feat:` Nouvelle fonctionnalité
- `fix:` Correction de bug
- `docs:` Documentation
- `style:` Formatage, point-virgules manquants, etc.
- `refactor:` Refactoring de code
- `test:` Ajout de tests
- `chore:` Maintenance

Exemples :
```
feat: ajoute la pagination à la liste des réunions
fix: corrige le bug de date sur les réunions passées
docs: met à jour le README avec les nouvelles instructions
```

## Tests

### Frontend
```bash
cd frontend
npm test                # Tests unitaires
npm run test:coverage   # Avec couverture
```

### Backend
```bash
cd backend
.\mvnw.cmd test
```

**Tous les tests doivent passer avant de soumettre une PR.**

## Pull Request Checklist

- [ ] Mon code suit les standards de style du projet
- [ ] J'ai effectué une auto-revue de mon code
- [ ] J'ai commenté les parties complexes
- [ ] J'ai mis à jour la documentation si nécessaire
- [ ] Mes changements ne génèrent pas de nouveaux warnings
- [ ] J'ai ajouté des tests couvrant mes changements
- [ ] Tous les tests passent localement
- [ ] J'ai mis à jour le CHANGELOG.md

## Processus de Review

1. Un mainteneur reviewera votre PR
2. Des changements peuvent être demandés
3. Une fois approuvée, votre PR sera mergée
4. Votre contribution sera ajoutée à CONTRIBUTORS.md

## Questions ?

N'hésitez pas à :
- Ouvrir une issue pour toute question
- Rejoindre les discussions existantes
- Consulter la documentation

## Licence

En contribuant, vous acceptez que vos contributions soient sous la même licence que le projet.

---

**Merci pour vos contributions ! 🙏**
