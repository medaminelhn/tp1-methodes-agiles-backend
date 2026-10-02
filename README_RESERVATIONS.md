# Developpement de la gestion des reservations

Ce document resume le travail realise pour ajouter la partie administration des reservations dans le projet de gestion des salles.

## Objectif fonctionnel

La fonctionnalite permet a une secretaire de consulter les demandes de reservation, filtrer les reservations, voir leurs details, puis accepter ou refuser une demande en respectant les contraintes de disponibilite des salles.

## Fonctionnalites realisees

- Affichage des reservations avec professeur, salle, date, horaire, duree, matiere, motif et statut.
- Recherche locale cote Angular sur professeur, salle, matiere, motif et statut.
- Filtres par statut et par salle.
- Panneau de details d'une reservation selectionnee.
- Actions disponibles selon le statut de la reservation.
- Acceptation d'une reservation en attente.
- Refus d'une reservation en attente.
- Proposition d'une alternative si une salle ou un creneau disponible est trouve.
- Modification de la salle attribuee a une reservation.
- Messages visibles en cas de succes ou d'erreur.
- Validation backend pour empecher les doubles reservations.
- Verrouillage pessimiste cote backend pendant les actions critiques afin de limiter les conflits lors de confirmations simultanees.

## Statuts utilises

Les statuts restent stockes dans le champ texte `statut` deja present dans le modele.

- `EN_ATTENTE` : demande creee, en attente de traitement par la secretaire.
- `CONFIRMEE` : reservation acceptee.
- `REFUSEE` : reservation refusee.
- `ANNULEE` : statut affiche cote interface si des donnees existantes l'utilisent.

La creation d'une reservation met maintenant le statut par defaut a `EN_ATTENTE`, afin que la secretaire puisse ensuite accepter ou refuser la demande.

## Backend Spring Boot

### Endpoints ajoutes ou completes

Dans `ReservationController` :

- `GET /api/reservations`
  - Retourne la liste des reservations.
  - Parametres optionnels :
    - `recherche`
    - `statut`
    - `salleId`
    - `utilisateurId`
    - `semestreId`

- `GET /api/reservations/{id}`
  - Retourne le detail d'une reservation.

- `POST /api/reservations`
  - Cree une ou plusieurs demandes de reservation.
  - Les nouvelles reservations sont creees avec le statut `EN_ATTENTE`.

- `PATCH /api/reservations/{id}/accepter`
  - Accepte une reservation en attente.
  - Verifie qu'aucune reservation confirmee ne chevauche le meme creneau dans la salle.
  - Assigne une salle alternative si necessaire et possible.
  - Passe la reservation en `CONFIRMEE`.
  - Met la salle a `disponible = false` selon le modele existant.

- `PATCH /api/reservations/{id}/refuser`
  - Refuse une reservation non confirmee.
  - Passe la reservation en `REFUSEE`.
  - Retourne une alternative disponible si les donnees permettent d'en trouver une.

- `PATCH /api/reservations/{id}/salle`
  - Modifie la salle attribuee a une reservation.
  - Verifie que la nouvelle salle est disponible.
  - Verifie qu'aucune reservation confirmee ne chevauche le meme creneau dans la nouvelle salle.
  - Met a jour l'etat des salles si la reservation est deja confirmee.

### DTO ajoutes

- `ReservationView`
  - Vue enrichie d'une reservation pour l'interface.
  - Contient notamment le nom de la salle, le nom du professeur et la duree calculee.

- `AlternativeReservation`
  - Represente une proposition alternative.
  - Contient la salle, le debut et la fin du creneau propose.

- `ReservationActionResponse`
  - Reponse commune aux actions accepter/refuser.
  - Contient la reservation mise a jour, une alternative eventuelle et un message.

### Regles metier implementees

- Une reservation ne peut etre acceptee que si elle est en `EN_ATTENTE`.
- Une reservation confirmee ne peut pas etre refusee directement.
- Le chevauchement est verifie avec la regle :
  - `dateDebut < finDemandee`
  - `dateFin > debutDemande`
- Seules les reservations `CONFIRMEE` bloquent un creneau.
- Le changement de salle attribuee applique les memes controles de conflit.
- Les creneaux restent fixes a 2 heures, conformement a la contrainte SQL existante.
- En cas de conflit ou d'indisponibilite, le service cherche :
  - d'abord une autre salle disponible au meme creneau ;
  - ensuite un creneau suivant disponible dans les prochaines plages de 2 heures.

### Concurrence

Des verrous pessimistes ont ete ajoutes dans les repositories :

- `ReservationRepository.findByIdForUpdate`
- `SalleRepository.findByIdForUpdate`

Ils sont utilises pendant l'acceptation et le refus afin de reduire les risques de double confirmation simultanee.

### Correction de configuration

Le projet contenait des classes dans le package `com.example.salles`, alors que l'application principale est dans `com.tp1methodesagiles.gestiondessalles`.

La classe `GestiondessallesApplication` a ete ajustee pour scanner :

- les composants Spring ;
- les entites JPA ;
- les repositories JPA.

La dependance `spring-boot-starter-data-jpa` a aussi ete ajoutee, car les repositories JPA sont utilises par le code.

## Frontend Angular

Le placeholder Angular initial a ete remplace par un ecran de gestion des reservations.

### Elements de l'ecran

- Titre `Gestion des reservations`.
- Zone de recherche.
- Filtre par statut.
- Filtre par salle.
- Tableau des reservations.
- Badge visuel de statut.
- Panneau de details.
- Boutons `Accepter` et `Refuser` uniquement si la reservation est en attente.
- Selecteur pour modifier la salle attribuee.
- Affichage d'une alternative si le backend en retourne une.
- Affichage clair des messages d'erreur et de succes.

### Appels HTTP utilises

Dans `app.component.ts` :

- `GET /api/salles`
- `GET /api/reservations`
- `PATCH /api/reservations/{id}/accepter`
- `PATCH /api/reservations/{id}/refuser`
- `PATCH /api/reservations/{id}/salle`

Le `HttpClient` Angular a ete active dans `app.config.ts` avec `provideHttpClient()`.

## Base de donnees

Le fichier `DB.sql` a ete mis a jour :

- ajout de la colonne `matiere` dans la table `reservations` ;
- changement du statut par defaut vers `EN_ATTENTE`.

Cette modification etait necessaire car l'entite Java `Reservation` utilisait deja le champ `matiere`, mais la colonne n'existait pas dans le schema SQL fourni.

Un fichier de migration idempotent a aussi ete ajoute :

- `migration_reservations_admin.sql`

Il contient :

```sql
ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS matiere VARCHAR(150);

ALTER TABLE reservations
    ALTER COLUMN statut SET DEFAULT 'EN_ATTENTE';
```

## Tests ajoutes

Un test unitaire cible a ete ajoute :

- `ReservationServiceTest`

Il couvre :

- acceptation d'une reservation sans conflit ;
- refus d'une reservation avec alternative disponible ;
- rejet d'une confirmation en cas de conflit sans alternative ;
- refus d'une reservation sans alternative disponible.
- modification de salle attribuee sans conflit ;
- rejet de modification de salle en cas de conflit.

## Fichiers principaux modifies

### Backend

- `pom.xml`
- `mvnw.cmd`
- `GestiondessallesApplication.java`
- `ReservationController.java`
- `ReservationService.java`
- `ReservationRepository.java`
- `SalleRepository.java`
- `ReservationView.java`
- `AlternativeReservation.java`
- `ReservationActionResponse.java`
- `SalleAffectationRequest.java`
- `ReservationServiceTest.java`

### Frontend

- `app.component.ts`
- `app.component.html`
- `app.component.scss`
- `app.component.spec.ts`
- `app.config.ts`

### SQL

- `DB.sql`
- `migration_reservations_admin.sql`

## Verification effectuee

Des commandes de verification ont ete tentees :

- `mvnw.cmd test`
- `npm run build`
- `npm install`

Les tests n'ont pas pu etre executes completement dans l'environnement au moment du developpement :

- Maven Wrapper devait telecharger Maven et la connexion a ete interrompue.
- Le frontend n'avait pas encore `node_modules`.
- `npm install` a echoue a cause d'un manque d'espace disque (`ENOSPC`).

Les dossiers temporaires ou partiels generes par ces essais ont ete nettoyes.

## Limites connues

- Le champ `salles.disponible` est un booleen global dans le modele existant. Lorsqu'une reservation est confirmee, la salle est mise a `disponible = false`, mais le schema ne prevoit pas encore de liberation automatique apres la fin du creneau.
- La proposition d'alternative reste simple : elle cherche une salle libre au meme creneau, puis quelques creneaux suivants de 2 heures.
- Les statuts restent sous forme de chaines de caracteres, car le projet ne possede pas encore d'enum ou de table de referentiel pour les statuts.

## Commandes utiles pour reprendre la verification

Backend :

```bash
cd "tp1-methodes-agiles-backend/backend/gestiondessalles"
mvnw.cmd test
```

Frontend :

```bash
cd "tp1-methodes-agiles-frontend/tp1-methodes-agiles-front"
npm install
npm run build
```
