# Préparation à la Soutenance (Jury Defense Q&A)

Ce document est votre "antisèche" pour affronter les questions difficiles du jury. Les membres du jury (professeurs et encadrants professionnels) aiment tester la profondeur de votre compréhension. Ne récitez pas, répondez avec l'état d'esprit d'un ingénieur logiciel.

---

## 🔒 Thème 1 : Sécurité et Authentification (JWT)

**Question du Jury :** *"Pourquoi avez-vous choisi d'utiliser JWT au lieu des Sessions traditionnelles HTTP ?"*
**Votre Réponse (Senior) :** 
> "J'ai opté pour une approche **Stateless** (sans état). Avec les sessions HTTP, le framework côté Backend (Tomcat/Spring) doit stocker l'état de chaque utilisateur en mémoire. Si l'application scale sur plusieurs serveurs, ça pose un énorme problème de synchronisation (le serveur B ne reconnait pas la session du serveur A). 
Avec JWT, toute l'intelligence cryptographique est dans le jeton. Le Backend vérifie simplement la signature cryptographique. Cela garantit une évolutivité parfaite et sécurise l'environnement puisque l'API est purement REST."

**Question du Jury :** *"Où stockez-vous ce JWT côté Frontend et n'est-ce pas dangereux ?"*
**Votre Réponse (Senior) :** 
> "Actuellement, il est stocké dans le `localStorage` pour des raisons pratiques inhérentes au prototype. Néanmoins, pour parer aux attaques XSS de haut niveau, l'amélioration professionnelle serait d'utiliser des **Cookies sécurisés HttpOnly** que le Javascript ne peut pas lire. Le JWT garantit cependant qu'aucune de ses Claims ne peut être modifiée ; toute altération le rend invalide côté Spring Boot."

---

## 🏗️ Thème 2 : Architecture Backend (DTO & MapStruct)

**Question du Jury :** *"Pourquoi avez-vous créé des objets DTO (AuthRequest/Response) au lieu d'utiliser directement votre classe `User` dans les requêtes API ?"*
**Votre Réponse (Senior) :** 
> "Exposer les classes `@Entity` pose plusieurs risques critiques. D'abord, un attaquant pourrait envoyer des données supplémentaires en JSON (Mass Assignment) et écraser des données système. Deuxièmement, envoyer un objet `User` renverrait également le hash de son mot de passe ou sa relation hiérarchique en BD. La création de **DTOs sous forme logique de `records`** Java 17 encapsule les données, valide les Inputs (via `@NotBlank`) dès l'entrée de mon URL, et empêche mon architecture interne de s'échapper vers Internet."

**Question du Jury :** *"Comment gérez-vous la conversion entre votre Entity et ce DTO ?"*
**Votre Réponse (Senior) :** 
> "Je n'expose jamais les `@Entity` directement au JSON : j'utilise des **DTO immutables** (souvent des `record` Java) pour contrôler le contrat API. Le projet inclut **MapStruct** pour les zones où un mapper généré à la compilation apporte le plus de valeur (par exemple autour des utilisateurs). Pour les agrégats simples, je construis aussi des **DTO imbriqués** (`ElderlySummaryDTO`, `CaregiverSummaryDTO`) dans le contrôleur ou un petit service : c'est explicite, facile à relire en soutenance, et évite les 'fuites' de modèle relationnel vers le front."

---

## 🌐 Thème 3 : Frontend (Angular 17)

**Question du Jury :** *"Comment empêchez-vous un utilisateur non connecté d'écrire /dashboard dans sa barre d'URL ?"*
**Votre Réponse (Senior) :** 
> "J'ai mis en place une logique défensive à deux niveaux. Côté Frontend, j'utilise un **AuthGuard** configuré sur mon routeur Angular. Avant même que le composant ne soit chargé en RAM, le Guard intercepte l'appel, vérifie l'état local du token et, si vide, bloque la navigation en orientant l'utilisateur sur login. Même s'il parvenait à le contourner en forçant le cache de son navigateur, le second niveau de défense (l'**AuthInterceptor**) s'activerait. L'intercepteur tente alors un appel API, voit que le Backend renvoie un 401 Unauthorized à cause de la faille, écoute ce HTTP code globalement et déconnecte l'intrus sur le champ."

---

## 🐳 Thème 4 : DevOps (Docker et CI/CD)

**Question du Jury :** *"Qu'apporte Docker à ce stage et comment garantissez-vous que votre application démarre dans l'ordre ?"*
**Votre Réponse (Senior) :** 
> "Docker me permet de rendre l'environnement purement prédictible. Plutôt que de configurer un serveur Ubuntu avec MySQL en forçant ma machine, j'isole les 3 espaces.
Pour pallier le problème de synchronisation (le Backend plantant parce que la DB met du temps à se lancer), je n'utilise pas un simple `sleep 10s`. Mon fichier d'orchestration (docker-compose) utilise des **Healthchecks**. Il "ping" la couche native MySQL. Mon Spring ne bootera QUE quand MySQL répond "Ok", protégeant toute l'infrastructure de dysfonctionnements de processus croisés."

**Question du Jury :** *"Pouvez-vous m'expliquer ce fichier `.github/workflows/ci.yml` ?"*
**Votre Réponse (Senior) :** 
> "C'est l'essence même du développement moderne. Il s'agit d'un pipeline d'Intégration Continue (CI). Concrètement, aucun collègue ou stagiaire futur ne peut casser notre application par erreur : GitHub intercepte chaque mise à jour, lance un serveur Linux jetable, compile notre Angular, compile notre Java et lance nos JUnit Tests. Si un test crash, la fusion de code est bloquée."
