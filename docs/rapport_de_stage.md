# Rapport de Stage d'Été 

**Sujet : Conception et Développement d'une Plateforme d'Assistance pour les Personnes Âgées**  
**Élaboré par :** Ranim Rtimi  
**Établissement :** Institut Supérieur des Sciences Appliquées et de Technologie de Sousse (ISSAT Sousse)  
**Organisme d'accueil :** Bee Coders (https://www.beecoders.tn/)  
**Année Universitaire :** 2024/2025  

---

## Remerciements
Je tiens à exprimer mes vifs remerciements au corps professoral et administratif de l’Institut Supérieur des Sciences Appliquées et de Technologie de Sousse (ISSAT Sousse) pour la qualité de l’enseignement dispensé. Je remercie également l’équipe de direction et l'équipe technique de Bee Coders pour leur encadrement, leurs précieux conseils et l'environnement professionnel exigeant et stimulant qu'ils m'ont offert durant ce stage d'été.

---

## Introduction Générale
Le vieillissement démographique constitue l'un des défis sociétaux majeurs de notre époque. Pour garantir la sécurité, le suivi médical, et préserver le lien social des seniors, la digitalisation des services d'assistance devient impérative. C'est dans ce contexte que s'inscrit ce projet de stage d'été au sein de Bee Coders. 

Ce projet visait à concevoir et développer de A à Z une plateforme Full-Stack : un système intelligent permettant une coordination en temps réel entre les personnes âgées, leurs familles et les professionnels de la santé.

---

## Chapitre 1 : Présentation de l'Organisme d'Accueil

### 1.1 Bee Coders et son Écosystème
**Bee Coders** est une agence technologique d’excellence spécialisée dans le développement web, le développement mobile et le consulting IT. La mission de l’entreprise est de concevoir des produits digitaux sur-mesure combinant robustesse technique et design intuitif.

Au-delà de la production logicielle, Bee Coders s’engage fortement dans le volet académique à travers leur plateforme en ligne **9antra.tn**, qui délivre des formations certifiantes visant l'insertion professionnelle des étudiants. Ce double rôle de créateur de logiciels et d'incubateur pédagogique a offert un contexte idéal pour la réalisation de ce stage, me permettant d'assimiler les rigueurs du monde professionnel.

---

## Chapitre 2 : Analyse et Spécifications des Besoins

Afin d'assurer la viabilité du projet, j'ai recensé l'ensemble des besoins nécessaires à la modélisation du système.

### 2.1 Besoins Fonctionnels
- **La Personne Âgée** : Doit bénéficier d'une interface simplifiée pour déclencher un "SOS" instantané et consulter la planification de ses rappels médicamenteux.
- **La Famille** : Bénéficie d'un suivi transparent pour vérifier la santé de leurs proches ou paramétrer des rendez-vous.
- **Le Soignant (Caregiver)** : Administre les profils médicaux et coordonne directement la prise en charge clinique.
- **L'Administrateur** : Dispose d'un tableau de bord de supervision et de la gestion centralisée des accès.

### 2.2 Besoins Non-Fonctionnels et Justifications Technologiques
La plateforme manipulant des données médicales sensibles (PHI), des contraintes strictes s'imposaient. Voici mes choix technologiques justifiés face au jury :

1. **Backend (Spring Boot 3 / Java 17)** : Contrairement à des frameworks comme Express.js, Spring Boot impose une rigueur structurelle, un solide typage fort garantissant la stabilité à long terme, et s'intègre nativement à l'écosystème de sécurité d'entreprise (Spring Security).
2. **Frontend (Angular 17)** : Plutôt que React (qui est une librairie nécessitant l'assemblage manuel d'outils), j'ai opté pour Angular, un framework structuré proposant nativement l'injection de dépendances, le routage protectif et une gestion stricte des états.
3. **Sécurité (JWT Stateless)** : J'ai refusé l'usage des Sessions traditionnelles, impossibles à scaler horizontalement. Le JWT crypte les claims de l'utilisateur, permettant au backend de valider de manière autonome une requête sans interroger la base de données systématiquement.
4. **Déploiement (Docker)** : L'utilisation de conteneurs Docker supprime les biais liés aux plateformes d'exploitation (le fameux "ça marche sur ma machine").

---

## Chapitre 3 : Conception et Architecture

*[NOTE POUR LE RAPPORT: Insérer ici une capture d'écran du Diagramme Entité-Association (ER Diagram) ou de Classe]*

### 3.1 L'Architecture "Clean" (Oignon/N-Tiers)
Pour que la plateforme soit maintenable par Bee Coders sur plusieurs années, j'ai implémenté une **Clean Architecture** côté back-end :
- **Couche Entité (JPA)** : Cartographie directe à la base MySQL. Le typage Java est protégé par des records immutables lors du retour d'API.
- **Data Transfer Objects (DTO)** : Pour éviter l'exposition d'informations sensibles de la base de données, l'API ne communique qu'avec des `RegisterRequest` ou `UserDTO`. Le mapping est effectué de façon explicite (via des méthodes `mapToDTO`) pour garantir un alignement précis avec le Frontend et contourner les erreurs de requêtes paresseuses (LazyInitializationException).
- **Global Exception Handler** : Intercepteur via `@RestControllerAdvice` qui intercepte toute erreur Java générant un crash (comme `MethodArgumentNotValidException`), afin de la transformer en un format JSON structuré (`ErrorResponse`). Cela bloque toute propagation de logs confidentiels côté client public.

---

## Chapitre 4 : Implémentation et Réalisation Fonctionnelle

### 4.1 La Sécurité Frontend-Backend
L'Angular 17 a été couplé à des concepts modernes pour sécuriser l'application :
- **L'AuthInterceptor** (Intercepteur Fonctionnel) : Il copie et modifie chaque requête HTTP sortante d'Angular pour lui injecter le `Token Bearer`. Si le JWT est expiré (réponse HTTP 401), il détruit le jeton côté client et redirige globalement sur le login. 
- **La protection des routes (AuthGuard)** : Implémentation bloquant l'accès visuel au Dashboard si le profil n'est pas identifié.

*[NOTE POUR LE RAPPORT: Insérer ici 2 captures d'écran : 1) La page de demande de connexion Angular Material. 2) Un message d'erreur rouge lorsque les identifiants sont erronés provenant du backend.]*

*[NOTE POUR LE RAPPORT: Insérer ici une capture d'écran de l'interface Swagger UI générée sur le port 8080]*

### 4.3 Les Innovations "HealthTech" (Fonctionnalités avancées)
Pour apporter une plus-value majeure au produit, j'ai implémenté 5 piliers technologiques d'assistance :
1. **Moteur Temps Réel (WebSockets STOMP)** : Remplacement des requêtes HTTP classiques par un canal WebSocket sécurisé. Lorsqu'un algorithme détecte un danger, une notification "Push" rouge apparaît instantanément (en quelques millisecondes) sur l'écran Angular du médecin, sans actualisation de page.
2. **Planificateur Médical CRON** : J'ai programmé un robot "Scheduler" qui s'exécute silencieusement toutes les 30 minutes via Spring Boot. Il vérifie l'observance médicamenteuse et lève automatiquement une Alerte si le patient a oublié sa pilule.
3. **Timeline Polymorphique de la Famille** : Création d'une API agrégeant 4 tables SQL différentes (Rendez-vous, Urgences, Tension artérielle, Médicaments) pour les aligner sur une seule ligne du temps chronologique (inspiré du feed d'un réseau social) afin de rassurer la Famille.
4. **Rapports Médicaux PDF** : Intégration de la librairie *OpenPDF*. En un clic, l'application génère et compile le passeport santé (données des 30 derniers jours) dans un fichier binaire A4 certifié, prêt pour une hospitalisation.
5. **Géofencing Anti-Errance (Mathématique)** : Programmation d'un module spatial de géolocalisation. L'algorithme calcule la distance de *Haversine* entre la "Safe-Zone" (la Maison) et les pings d'une Smartwatch simulée en Front-end. En cas de franchissement de périmètre, le hub déclenche une interception d'urgence.

---

## Chapitre 5 : DevOps et Gestion de Configuration

### 5.1 Docker et Variables d'Environnement (.env)
La sécurisation s'étend jusqu'aux couches de l'infrastructure. Aucune clé secrète ou mot de passe MySQL n'est codé en dur ; tout transite via un fichier paramétré `.env` lu par un fichier orchestre `docker-compose.yml`.

### 5.2 Optimisation (Multi-Stage Builds & Healthchecks)
La génération de l'image Back-end s'opère en deux temps (Multi-Stage) : le build Maven télécharge les dépendances, et seule l'archive finale `.jar` résultante est déposée sur un système très léger (Alpine). 
De plus, j'ai introduit des `healthchecks` Docker : notre API Spring Boot attend que le ping interne de la base de données MySQL retourne un succès avant de s'initialiser, contournant automatiquement les erreurs du type `Connection Refused` en démarrage à froid.

---

## Chapitre 6 : Difficultés Rencontrées

La conception d'une arborescence d'entreprise m'a obligée à surmonter divers obstacles :
1. **La gestion asynchrone des Observables Angular** : Le paradigme RxJS d'Angular nécessite d'abandonner les processus procéduraux. S'assurer que le stockage du token s'effectue avant l'initialisation du tableau de bord a exigé l'implémentation de résolveurs réactifs et l'usage de Signaux (v17+).
2. **L'implémentation stricte des DTO** : Gérer la conversion d'objets imbriqués sans affecter la performance (notamment les relations associatives de Hibernate) m'a forcé à concevoir des sous-projections (ex: `ElderlySummaryDTO`) et à consolider les mappings manuellement pour protéger la sérialisation JSON.
3. **Container Networking** : Connecter trois conteneurs distincts nécessitait de bien comprendre la résolution DNS interne des réseaux bridges Docker pour formuler l'URI JDBC vers MySQL.

---

## Conclusion et Perspectives

### Bilan Personnel et Technique
Ce stage chez Bee Coders m'a permis de réaliser mon immersion dans la méthode de travail "Industrie". En réalisant seul un projet Full-Stack du modèle de la base de données jusqu'à l'orchestration Docker CI/CD, sans omettre l'implémentation de mathématiques spatiales (Geofencing) et de sockets temps-réel, j'ai acquis le recul d'un ingénieur logiciel complet. J'ai compris que l'ingénierie logicielle ne se résume pas à écrire du code : concevoir l'architecture à l'aveugle est aisé, c'est concevoir pour l'évolution, le temps réel et la sécurité des données qui demande de la rigueur.

### Perspectives d'Amélioration
La "Elderly Assistance Platform", ayant franchi le stade du prototype MVP, présente un énorme potentiel. L'intégration de modules complémentaires s'impose :
- **Intégration CI/CD** : Concrétiser le workflow GitHub Actions pour un déploiement continu automatisé.
- **Internet of Medical Things (IoMT)** : Interfacer la plateforme avec des dispositifs de type "Smartwatches" avec sondes de santé, alimentant le backend Spring Boot via des Websockets pour l'analyse des cas de chute en temps réel.
- **Anomalie et ML** : Insérer des services d'Intelligence Artificielle prédictive sur l'évolution médicamenteuse de nos ainés et notifier indirectement les hôpitaux en cas de divergence grave.
