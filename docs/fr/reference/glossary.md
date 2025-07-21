# Glossaire

Explication des termes techniques et concepts liés à NoWakeLock.

## Concepts centraux

### WakeLock (verrou de réveil)
Mécanisme empêchant les appareils Android d'entrer en mode veille. Les applications maintiennent le CPU en fonctionnement ou l'écran allumé en détenant un WakeLock.

**Types** :
- **PARTIAL_WAKE_LOCK** - Maintient le CPU en fonctionnement, l'écran peut s'éteindre
- **SCREEN_DIM_WAKE_LOCK** - Maintient l'écran allumé mais permet l'assombrissement
- **SCREEN_BRIGHT_WAKE_LOCK** - Maintient l'écran complètement allumé
- **FULL_WAKE_LOCK** - Maintient le CPU et l'écran en fonctionnement

### Alarm (tâche programmée)
Service de minuteur du système Android permettant aux applications d'exécuter des tâches à des moments spécifiques ou à intervalles.

**Types** :
- **RTC** - Minuteur basé sur l'heure réelle
- **RTC_WAKEUP** - Basé sur l'heure réelle et réveille l'appareil
- **ELAPSED_REALTIME** - Basé sur le temps de démarrage de l'appareil
- **ELAPSED_REALTIME_WAKEUP** - Basé sur le temps de démarrage et réveille l'appareil

### Service (service)
Composant d'application Android s'exécutant en arrière-plan, ne fournissant pas d'interface utilisateur.

**Types** :
- **Service de premier plan** - Exécute des tâches perceptibles par l'utilisateur, affiche une notification persistante
- **Service d'arrière-plan** - Exécute des tâches non directement perceptibles par l'utilisateur
- **Service lié** - Fournit une interface client-serveur

## Système Android

### Doze Mode (mode sommeil)
Mécanisme d'économie d'énergie introduit dans Android 6.0+, l'appareil entre en état de sommeil profond lorsqu'il est immobile.

### App Standby (veille d'application)
Restrictions d'économie d'énergie du système pour les applications inutilisées pendant longtemps.

### Background Execution Limits (limitations d'exécution en arrière-plan)
Limitations d'Android 8.0+ sur les services en arrière-plan et les récepteurs de diffusion.

### SELinux (Linux à sécurité renforcée)
Mécanisme de sécurité de contrôle d'accès obligatoire du système Android.

## Framework Xposed

### Xposed Framework
Framework permettant de modifier le comportement du système et des applications sans modifier les APK.

### Hook (crochet)
Technique d'interception et de modification des appels de fonction.

### LSPosed
Implémentation Xposed moderne basée sur Riru, supportant Android 8.1+.

### EdXposed
Implémentation Xposed basée sur YAHFA et SandHook.

### Zygote
Processus parent de tous les processus d'application dans le système Android.

## Terminologie NoWakeLock

### Modes d'interception
- **Autoriser** - Aucune restriction, fonctionnement normal
- **Limiter** - Définir des limitations de temps ou de fréquence
- **Intercepter** - Bloquer complètement l'opération

### Système de règles
Mécanisme de configuration basé sur la correspondance de motifs, supportant les expressions régulières.

### Composants
Terme collectif pour WakeLock, Alarm, Service.

### Portée
Plage d'applications où le module Xposed prend effet.

### DA
Abréviation de Detection/Action, faisant référence aux activités WakeLock, Alarm, Service détectées par NoWakeLock.

## Indicateurs de performance

### Nombre d'acquisitions
Nombre total de fois où un WakeLock a été acquis.

### Durée cumulée
Temps total pendant lequel un WakeLock a été détenu.

### Fréquence de déclenchement
Intervalle moyen de déclenchement des Alarmes.

### Nombre de démarrages
Nombre total de fois où un Service a été démarré.

### Taux d'interception
Pourcentage d'opérations interceptées par rapport au total des opérations.

## Termes techniques

### API Level
Numéro de niveau API correspondant à la version Android.

### Package Name
Identifiant unique de l'application, comme `com.example.app`.

### UID (identifiant utilisateur)
Identifiant numérique unique attribué par le système à chaque application.

### PID (identifiant de processus)
Identifiant numérique unique attribué par le système à chaque processus.

### ContentProvider
L'un des quatre composants Android majeurs, utilisé pour le partage de données entre applications.

### IPC (communication inter-processus)
Mécanisme d'échange de données entre différents processus.

### JNI (interface native Java)
Interface pour que le code Java appelle le code C/C++ natif.

## Terminologie de base de données

### Room
Framework d'abstraction SQLite officiel de Google.

### DAO (objet d'accès aux données)
Interface encapsulant les opérations de base de données.

### Entity (entité)
Mappage d'objet de table de base de données.

### Migration (migration)
Mécanisme de traitement de la mise à niveau des versions de base de données.

## Termes de développement

### Kotlin
Langage de programmation JVM moderne, langage préféré pour le développement Android.

### Jetpack Compose
Boîte à outils UI déclarative moderne d'Android.

### Coroutines (coroutines)
Mécanisme de programmation asynchrone de Kotlin.

### Flow
Framework de flux de données réactives de Kotlin.

### ViewModel
Composant d'architecture Android gérant les données liées à l'UI.

### LiveData
Classe de détention de données observable avec conscience du cycle de vie.

### Koin
Framework d'injection de dépendances léger.

## Expressions régulières

### Métacaractères
Caractères avec signification spéciale, comme `.`, `*`, `+`, `?`, etc.

### Classes de caractères
Ensemble de caractères entre crochets, comme `[abc]`.

### Quantificateurs
Symboles spécifiant le nombre de correspondances, comme `{n}`, `{n,m}`, etc.

### Groupes
Sous-expressions créées avec des parenthèses, comme `(abc)+`.

### Ancres
Symboles spécifiant la position de correspondance, comme `^` (début), `$` (fin).

## Terminologie de configuration

### Héritage
Mécanisme par lequel la configuration enfant obtient automatiquement les paramètres du parent.

### Priorité
Ordre d'exécution lorsque plusieurs règles sont en conflit.

### Modèle
Combinaison de configuration prédéfinie, réutilisable.

### Liste blanche
Liste d'applications ou composants non soumis aux restrictions de règles.

### Liste noire
Liste d'applications ou composants strictement limités ou interceptés.

## Services système

### PowerManagerService
Service système gérant l'état d'alimentation de l'appareil.

### AlarmManagerService
Service gérant les tâches programmées du système.

### ActivityManagerService
Service gérant le cycle de vie des applications.

### PackageManagerService
Service gérant l'installation et les permissions des applications.

### WindowManagerService
Service gérant l'affichage des fenêtres.

## Liées aux permissions

### QUERY_ALL_PACKAGES
Permission de requête de toutes les applications installées (Android 11+).

### WAKE_LOCK
Permission d'acquisition de WakeLock.

### RECEIVE_BOOT_COMPLETED
Permission de réception de diffusion de démarrage.

### WRITE_EXTERNAL_STORAGE
Permission d'écriture sur stockage externe.

## Termes de débogage

### ADB (pont de débogage Android)
Outil en ligne de commande connectant la machine de développement et l'appareil Android.

### Logcat
Outil de visualisation des journaux système Android.

### ANR (application ne répond pas)
Erreur quand le thread principal de l'application est bloqué plus de 5 secondes.

### Crash (plantage)
Erreur de terminaison anormale de l'application.

### Memory Leak (fuite mémoire)
Problème où la mémoire ne peut pas être libérée normalement pendant l'exécution du programme.

## Termes de performance

### Taux d'utilisation CPU
Pourcentage d'utilisation du processeur.

### Occupation mémoire
Taille de RAM utilisée par l'application.

### Consommation d'énergie
Quantité d'utilisation de batterie de l'application.

### Trafic réseau
Quantité de transfert de données de l'application.

### I/O stockage
Activité de lecture/écriture de stockage de l'application.

!!! info "Mise à jour de terminologie"
    Au fur et à mesure du développement du projet, de nouveaux termes seront continuellement ajoutés à cette liste. En cas de questions, veuillez consulter la documentation pertinente ou contacter la communauté.

!!! tip "Conseils d'apprentissage"
    Il est recommandé aux nouveaux utilisateurs de se familiariser d'abord avec les concepts centraux (WakeLock, Alarm, Service), puis de comprendre progressivement les détails techniques.