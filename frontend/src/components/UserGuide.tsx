import React, { useState } from 'react';
import './UserGuide.css';

interface GuideSection {
  id: string;
  title: string;
  content: React.ReactNode;
}

const UserGuide: React.FC = () => {
  const [expandedSections, setExpandedSections] = useState<Set<string>>(new Set(['introduction']));

  const toggleSection = (id: string) => {
    const newExpanded = new Set(expandedSections);
    if (newExpanded.has(id)) {
      newExpanded.delete(id);
    } else {
      newExpanded.add(id);
    }
    setExpandedSections(newExpanded);
  };

  /**
   * Calculates the initial attendance value for a participant based on their total duration and poll response.
   *
   * @remarks
   * This function implements the attendance rules defined by the application to determine
   * whether a participant should be counted as present, partially present, or absent.
   *
   * The calculation follows these rules:
   *
   * **Duration-based rules:**
   * - **Minimum duration:** 5 minutes required to be counted
   * - **Partial presence:** 5-30 minutes = 0.5 attendance value
   * - **Full presence:** > 30 minutes = 1.0 attendance value
   * - **Multiple connections:** Durations are automatically cumulated
   *
   * **Poll validation:**
   * - If a participant answered the poll, they get at minimum 0.5 attendance
   * - This ensures that engaged participants (who answered polls) are counted even with short connection times
   *
   * @param totalDuration - The cumulated duration in minutes of all participant connections
   * @param hasAnsweredPoll - Boolean indicating if the participant answered any poll during the meeting
   *
   * @returns The calculated attendance value:
   * - `0` if duration < 5 minutes and no poll response
   * - `0.5` if duration between 5-30 minutes OR if poll was answered
   * - `1` if duration > 30 minutes
   *
   * @example
   * ```typescript
   * // Participant connected for 3 minutes, no poll response
   * calculateInitialAssistance(3, false); // Returns 0
   *
   * // Participant connected for 15 minutes
   * calculateInitialAssistance(15, false); // Returns 0.5
   *
   * // Participant connected for 45 minutes
   * calculateInitialAssistance(45, false); // Returns 1
   *
   * // Participant connected for 3 minutes but answered poll
   * calculateInitialAssistance(3, true); // Returns 0.5
   * ```
   *
   * @see {@link ATTENDANCE_RULES} for the constant values used in calculation
   */
  const sections: GuideSection[] = [
    {
      id: 'introduction',
      title: "📖 Guide d'Utilisation",
      content: (
        <div className="guide-introduction">
          <p>
            Bienvenue dans le guide d'utilisation de l'application de gestion de l'assistance aux
            réunions Zoom. Cette application vous aide à suivre l'assistance des participants,
            visualiser les sondages et générer des rapports d'assistance.
          </p>
          <p>Cliquez sur chaque section ci-dessous pour voir les instructions détaillées.</p>
        </div>
      ),
    },
    {
      id: 'meetings-list',
      title: '📋 Affichage des Réunions',
      content: (
        <div className="guide-section-content">
          <div className="tip">
            💡 Une réunion apparaît dans la liste quand elle a été <strong>quittée pour tous</strong>
          </div>
          <ol>
            <li>
              <strong>Chargement de la liste</strong>
              <ul>
                <li>
                  L'application affiche automatiquement les <strong>7 derniers jours</strong> de
                  réunions
                </li>
                <li>
                  Les données sont connectées à votre compte Zoom pour récupérer les informations
                </li>
                <li>
                  Les filtres "Date de début" et "Date de fin" permettent de visualiser les réunions
                  sur une période spécifique.
                </li>
              </ul>
            </li>
            <li>
              <strong>Les sondages deviennent disponibles après quelques secondes</strong>
              <ul>
                <li>Le bouton "Sondages" reste grisé au départ</li>
                <li>Une fois les données de Zoom chargées, le bouton se dégrise</li>
                <li>
                  <strong>Important :</strong> Le bouton "Sondages" n'est actif que si un sondage a
                  été lancé pendant la réunion
                </li>
              </ul>
            </li>
          </ol>
          <div className="tip">
            💡 <strong>Conseil :</strong> Si le bouton "Sondages" reste grisé, c'est qu'aucun
            sondage n'a été lancé pendant cette réunion.
          </div>
        </div>
      ),
    },
    {
      id: 'participants-view',
      title: '👥 Vue Participants',
      content: (
        <div className="guide-section-content">
          <h4>Étape 1️⃣ : Accéder aux participants</h4>
          <ol>
            <li>
              <strong>Cliquez sur le bouton "Participants"</strong>
              <ul>
                <li>Chaque réunion dispose d'un bouton "Participants"</li>
                <li>Un chargement peut prendre quelques secondes</li>
              </ul>
            </li>
            <li>
              <strong>Affichage initial des données</strong>
              <ul>
                <li>Les participants s'affichent avec un regroupement automatique par nom</li>
                <li>
                  Si une personne s'est connectée plusieurs fois, les durées de connexion sont
                  cumulées
                </li>
                <li>Exemple : Jean a été présent 15 min, déconnecté, puis 10 min = total 25 min</li>
              </ul>
            </li>
          </ol>

          <h4>Étape 2️⃣ : Tri et organisation</h4>
          <ol>
            <li>
              <strong>Possibilités de tri</strong>
              <ul>
                <li>
                  Triez par <strong>nom</strong> (ordre alphabétique)
                </li>
                <li>
                  Triez par <strong>durée de présence</strong> (croissant/décroissant)
                </li>
              </ul>
            </li>
            <li>
              <strong>Utilisation</strong>
              <ul>
                <li>Utilisez les en-têtes de colonne ou un sélecteur de tri</li>
                <li>Cela facilite la navigation dans la liste des participants</li>
              </ul>
            </li>
          </ol>

          <div className="tip">
            💡 <strong>Conseil :</strong> Le regroupement automatique évite les doublons et vous
            donne la durée réelle de présence.
          </div>
        </div>
      ),
    },
    {
      id: 'assistance-management',
      title: "✅ Gestion de l'Assistance",
      content: (
        <div className="guide-section-content">
          <h4>Rôle du Préposé à l'Accueil</h4>
          <p>
            Le préposé à l'accueil reçoit une proposition calculée par l'application pour chaque
            participant.
          </p>

          <h4>Étape 1️⃣ : Valider les propositions</h4>
          <ol>
            <li>
              <strong>Valeurs par défaut par ordre de priorité</strong>
              <ul>
                <li>
                  <strong>0</strong> - participants "Mons Assemblee", "tablette pupitre",
                  "*accueil*"
                </li>
                <li>
                  <strong>x</strong> - x étant la réponse au sondage "Combien de personnes sont
                  présentes avec vous (y compris vous) ?", si elle a été donnée
                </li>
                <li>
                  <strong>y</strong> - y étant un chiffre correspondant au suffixe du nom du
                  participant. Exemple: Famille Dupont (3)
                </li>
                <li>
                  <strong>1</strong> - pour les autres participants
                </li>
              </ul>
            </li>
            <li>
              <strong>Accepter la proposition</strong>
              <ul>
                <li>Si le chiffre proposé vous convient, ne touchez à rien</li>
                <li>Il sera enregistré comme assistance confirmée</li>
              </ul>
            </li>
            <li>
              <strong>Modifier cas par cas</strong>
              <ul>
                <li>Vous pouvez modifier chaque valeur d'assistance individuellement</li>
                <li>
                  Exemple : Un membre de la famille était présent physiquement mais pas en visio:
                  diminuer l'assistance visio pour cette famille
                </li>
              </ul>
            </li>
          </ol>

          <h4>Étape 2️⃣ : Saisir le comptage présentiel</h4>
          <ol>
            <li>
              <strong>Champ "En présentiel"</strong>
              <ul>
                <li>Vous devez saisir le nombre de personnes présentes physiquement</li>
                <li>Cette information est utilisée pour les statistiques</li>
              </ul>
            </li>
            <li>
              <strong>Calcul automatique de l'assistance en visioconférence</strong>
              <ul>
                <li>Le total visio est calculé automatiquement</li>
                <li>Somme de toutes les valeurs de la colonne "Assistance"</li>
              </ul>
            </li>
          </ol>

          <h4>Étape 3️⃣ : Sauvegarde</h4>
          <ol>
            <li>
              <strong>Bouton "Sauvegarder"</strong>
              <ul>
                <li>Un bouton "Sauvegarder" est disponible en haut à droite</li>
                <li>Cliquez pour sauvegarder toutes vos modifications</li>
              </ul>
            </li>
            <li>
              <strong>Confirmation de sauvegarde</strong>
              <ul>
                <li>Un message confirme la sauvegarde réussie</li>
                <li>Toutes les données sont conservées dans l'application</li>
              </ul>
            </li>
          </ol>

          <h4>Étape 4️⃣ : Reprendre les données saisies</h4>
          <ol>
            <li>
              <strong>Prochaines visites</strong>
              <ul>
                <li>Lorsque vous cliquez à nouveau sur "Participants" pour cette réunion</li>
                <li>Les données que vous aviez saisies s'affichent automatiquement</li>
                <li>Pas besoin de ressaisir les informations</li>
              </ul>
            </li>
          </ol>

          <div className="tip">
            💡 <strong>Conseil :</strong> La sauvegarde est persistante. Vos modifications restent
            enregistrées.
          </div>
        </div>
      ),
    },
    {
      id: 'refresh-data',
      title: '🔄 Actualiser depuis Zoom',
      content: (
        <div className="guide-section-content">
          <h4>Réinitialiser aux valeurs proposées par défaut</h4>
          <ol>
            <li>
              <strong>Bouton "Actualiser depuis Zoom"</strong>
              <ul>
                <li>Localisé en bas de la vue des participants</li>
                <li>Permet de revenir aux valeurs initiales de Zoom</li>
              </ul>
            </li>
            <li>
              <strong>Quand l'utiliser</strong>
              <ul>
                <li>Vous avez fait des erreurs et voulez recommencer</li>
                <li>Les données Zoom ont été mises à jour</li>
                <li>Vous voulez comparer avec les données actuelles de Zoom</li>
              </ul>
            </li>
            <li>
              <strong>Attention</strong>
              <ul>
                <li>Cette action réinitialise toutes vos modifications locales</li>
                <li>Les données enregistrées précédemment peuvent être perdues</li>
              </ul>
            </li>
          </ol>

          <div className="tip">
            ⚠️ <strong>Important :</strong> Assurez-vous d'avoir sauvegardé avant d'actualiser si
            vous voulez conserver vos modifications.
          </div>
        </div>
      ),
    },
    {
      id: 'polls',
      title: '📊 Affichage des Sondages',
      content: (
        <div className="guide-section-content">
          <ol>
            <li>
              <strong>Accéder aux sondages</strong>
              <ul>
                <li>Le bouton "Sondages" n'apparaît que si un sondage a été lancé</li>
                <li>Il se déverrouille automatiquement après le chargement des données</li>
              </ul>
            </li>
            <li>
              <strong>Informations disponibles</strong>
              <ul>
                <li>Visualisez les questions du sondage</li>
                <li>Voyez les réponses des participants</li>
                <li>Consultez les statistiques des réponses</li>
              </ul>
            </li>
            <li>
              <strong>Lien avec l'assistance</strong>
              <ul>
                <li>Dans la vue Participants, la colonne "Sondage" indique qui a répondu</li>
                <li>Cela permet de croiser assistance et engagement (réponse au sondage)</li>
              </ul>
            </li>
          </ol>

          <div className="tip">
            💡 <strong>Conseil :</strong> Les sondages aident à mesurer l'engagement des
            participants.
          </div>
        </div>
      ),
    },
    {
      id: 'workflow-summary',
      title: '🎯 Scénario principal',
      content: (
        <div className="guide-section-content">
          <p>
            <strong>Processus type d'une session :</strong>
          </p>
          <ol>
            <li>✅ Quitter la réunion pour tous (voir avec la sonorisation si vous n'avez pas les droits)</li>
            <li>✅ Se connecter à l'application</li>
            <li>✅ Cliquer "Voir les réunions"</li>
            <li>✅ Attendre quelques secondes (les sondages se déverrouillent)</li>
            <li>✅ Sélectionner une réunion → Clic "Participants"</li>
            <li>✅ Valider/modifier chaque assistance proposée</li>
            <li>✅ Saisir comptage présentiel</li>
            <li>✅ Cliquer "Sauvegarder"</li>
          </ol>

          <div className="tip">
            ✨ <strong>Résultat :</strong> Une traçabilité complète de l'assistance avec historique
            enregistré.
          </div>
        </div>
      ),
    },
    {
      id: 'tips',
      title: '⚡ Conseils et Bonnes Pratiques',
      content: (
        <div className="guide-section-content">
          <ul>
            <li>
              <strong>Attendre :</strong> La réunion doit avoir été quittée pour tous par l'hôte Zoom
            </li>
            <li>
              <strong>Sauvegarde régulière :</strong> Cliquez sur "Sauvegarder" après chaque
              modification importante
            </li>
            <li>
              <strong>Modification au cas par cas :</strong> Vous n'êtes jamais obligé d'accepter la
              proposition, adaptez au contexte réel
            </li>
            <li>
              <strong>Croiser les données :</strong> Utilisez la colonne "Sondage" pour consolider
              les informations d'assistance
            </li>
            <li>
              <strong>Tri intelligent :</strong> Triez par durée croissante pour ne pas
              comptabiliser les participants avec quelques minutes de présence
            </li>
            <li>
              <strong>Comptage présentiel :</strong> Utile pour les statistiques hybrides (visio +
              présentiel)
            </li>
          </ul>
        </div>
      ),
    },
    {
      id: 'support',
      title: '❓ Support et Aide',
      content: (
        <div className="guide-section-content">
          <p>En cas de problème ou de question :</p>
          <ul>
            <li>Consultez cette documentation (vous êtes dessus !)</li>
            <li>Essayez de rafraîchir la page (Ctrl+F5)</li>
            <li>Vérifiez votre connexion Internet</li>
            <li>En cas de données incorrectes, vérifiez que la réunion a été quittée pour tous par l'hôte Zoom</li>
            <li>
              Contactez <a href="mailto:fhenouille@gmail.com">fhenouille@gmail.com</a> pour les
              problèmes techniques
            </li>
          </ul>

          <div className="tip">
            💡 <strong>Pro Tip :</strong> Gardez cette page de guide ouverte dans un onglet pour
            référence rapide.
          </div>
        </div>
      ),
    },
  ];

  return (
    <div className="user-guide-container">
      <div className="guide-header">
        <h1>📚 Guide Utilisateur</h1>
        <p>Application de gestion de l'assistance aux réunions Zoom</p>
      </div>

      <div className="guide-sections">
        {sections.map((section) => (
          <div
            key={section.id}
            className={`guide-section ${
              expandedSections.has(section.id) ? 'expanded' : 'collapsed'
            }`}
          >
            <button
              className="guide-section-header"
              onClick={() => toggleSection(section.id)}
              type="button"
            >
              <span className="guide-section-title">{section.title}</span>
              <span className="guide-section-toggle">
                {expandedSections.has(section.id) ? '▼' : '▶'}
              </span>
            </button>

            {expandedSections.has(section.id) && (
              <div className="guide-section-body">{section.content}</div>
            )}
          </div>
        ))}
      </div>

      <div className="guide-footer">
        <p>Version 2.0 | © 2026 Zoom Meeting Manager</p>
      </div>
    </div>
  );
};

export default UserGuide;
