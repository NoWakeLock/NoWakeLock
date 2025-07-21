# Gestion des Services

Les Services (services en arrière-plan) sont des composants d'applications Android qui exécutent des tâches de longue durée en arrière-plan. Un excès de services en arrière-plan consomme des ressources système et de l'énergie.

!!! danger "⚠️ Mode de récupération - Rappel important !"
    **Si une configuration incorrecte des WakeLocks empêche l'appareil de démarrer** :
    
    **Situation 1 : Problème du framework LSPosed (problème rencontré après installation sans configuration)** :
    1. Maintenez le bouton d'alimentation pendant 10 secondes pour forcer le redémarrage
    2. Dès que l'écran devient noir, appuyez immédiatement et de manière répétée sur n'importe quel bouton physique
    3. Après avoir ressenti 2 courtes vibrations, continuez à appuyer rapidement 4 fois sur le même bouton
    4. Après le 4ème appui, vous devriez ressentir une longue vibration, indiquant que LSPosed est désactivé
    5. Après un démarrage normal, désactivez le module NoWakeLock dans LSPosed
    
    **Situation 2 : Problème de configuration par erreur (accès au Recovery possible)** :
    1. Entrez en Recovery → Gestionnaire de fichiers
    2. Naviguez vers /data/misc/xxx-xxx-xxx/prefs/com.js.nowakelock
       (xxx-xxx-xxx est une longue chaîne de caractères aléatoires, peut différer selon l'appareil)
    3. Supprimez l'intégralité du dossier
    4. Redémarrez l'appareil
    
    **En cas de cause incertaine** : Effacez directement les données de l'application NoWakeLock, évitez d'intercepter les composants système critiques lors de la reconfiguration.

## Aperçu des fonctionnalités

### Rôle des Services
- Traitement de données en arrière-plan
- Communication réseau et téléchargements
- Services multimédia comme la lecture musicale
- Surveillance et maintenance système

### Objectifs de gestion
- Surveiller le comportement de démarrage et de liaison des services
- Contrôler les services en arrière-plan inutiles
- Optimiser la fréquence de démarrage des services
- Réduire l'occupation des ressources

## Description de l'interface

### Liste des Services

【Capture d'écran nécessaire : page de liste des Services】

**Informations de la liste** :
- **Nom du service** - Nom de classe du Service
- **Application** - Nom de package source
- **Type** - Icône du type de service
- **État** - État d'exécution et paramètres d'interception
- **Statistiques** - Nombre de démarrages et durée d'exécution

### Indication d'état

| État | Icône | Description |
|------|-------|-------------|
| Autoriser | 🟢 | Démarrage et exécution normaux |
| Limiter | 🟡 | Limitation de la fréquence de démarrage |
| Intercepter | 🔴 | Empêcher le démarrage |
| En cours | ▶️ | Actuellement en cours d'exécution |
| Arrêté | ⏹️ | Service arrêté |

### Types de services

**Services de premier plan** :
- Affichent une notification persistante
- Services perceptibles par l'utilisateur
- Comme la lecture musicale, la navigation

**Services d'arrière-plan** :
- Sans interface utilisateur
- Exécution silencieuse de tâches
- Comme la synchronisation de données, le nettoyage

**Services liés** :
- Liés à d'autres composants
- Fournissent des interfaces d'appel
- Cycle de vie associé au lieur

## Options de configuration

### Modes de traitement

#### Mode Autoriser
- Démarrage et exécution normaux des services
- Aucune restriction appliquée
- Adapté aux services de fonctions importantes

#### Mode Limiter
- Contrôle de la fréquence de démarrage
- Limitation du nombre d'exécutions simultanées
- Arrêt automatique des services fonctionnant longtemps

#### Mode Intercepter
- Blocage complet du démarrage des services
- Inclut les démarrages explicites et implicites
- Peut affecter les fonctions centrales de l'application

### Options avancées

**Planification intelligente** :
- Retard du démarrage des services non urgents
- Fusion des services de fonctions similaires
- Ajustement selon la charge système

**Limitations de ressources** :
- Limitation du taux d'utilisation CPU
- Contrôle de l'occupation mémoire
- Limitation du trafic réseau

## Méthodes d'utilisation

### Visualiser la liste des Services

1. Cliquez sur l'onglet "Services" en bas
2. Consultez tous les services détectés
3. Utilisez les filtres pour voir des états ou applications spécifiques

### Configurer les règles de services

1. Cliquez sur l'élément de service cible
2. Sélectionnez le mode de traitement
3. Définissez des paramètres de limitation spécifiques :
   - Temps d'intervalle de démarrage
   - Durée maximale d'exécution
   - Limitations d'utilisation des ressources

【Capture d'écran nécessaire : page de configuration des Services】

### Gestion en lot

**Filtrage par application** :
- Visualiser tous les services d'une application spécifique
- Configuration en lot de règles au niveau application

**Filtrage par type** :
- Gestion séparée des services de premier plan
- Limitation unifiée des services d'arrière-plan

## Application pratique

### Identification des problèmes

#### Caractéristiques des services anormaux

**Démarrage fréquent** :
- Intervalle de démarrage inférieur à 10 secondes
- Redémarrage répétitif du même service en peu de temps
- Démarrage de services même quand l'application n'est pas utilisée

**Consommation de ressources** :
- Exécution longue (dépassant 30 minutes)
- Taux d'utilisation CPU élevé (> 5%)
- Occupation importante de mémoire (> 100MB)

**Services invalides** :
- Arrêt immédiat après démarrage
- Services vides sans fonction réelle
- Services uniquement utilisés pour maintenir en vie

## Implémentation technique

### Mécanisme Hook

Interception des méthodes de gestion de services d'ActivityManagerService :
```kotlin
// Interception du démarrage de service
startServiceLocked(
    IApplicationThread caller,
    Intent service,
    String resolvedType,
    int callingPid,
    int callingUid,
    boolean fgRequired,
    String callingPackage,
    int userId
)

// Interception de liaison de service  
bindServiceLocked(
    IApplicationThread caller,
    IBinder token,
    Intent service,
    String resolvedType,
    IServiceConnection connection,
    int flags,
    String callingPackage,
    int userId
)
```

### Suivi des données

**Surveillance en temps réel** :
- Enregistrement d'informations lors du démarrage de services
- Suivi de l'état d'exécution des services
- Calcul de l'utilisation des ressources

**Statistiques historiques** :
- Stockage de l'historique des services en base de données
- Analyse des modèles et tendances de démarrage
- Génération de recommandations d'optimisation

### Traitement de compatibilité

**Adaptation de versions** :
- Adaptation aux limitations des services d'arrière-plan d'Android 8.0+
- Traitement des différences d'API entre versions
- Traitement spécial des services de premier plan

**Optimisation des performances** :
- Minimisation du coût des appels Hook
- Correspondance de règles efficace
- Traitement asynchrone des données statistiques

## Fonctionnalités associées

- [Gestion des applications](app-management.md) - Visualiser tous les services par application
- [Gestion des WakeLocks](wakelocks.md) - WakeLocks liés aux services
- [Système de règles](rules-regex.md) - Configuration en lot avec expressions régulières

!!! info "Changements Android 8.0+"
    À partir d'Android 8.0, les services d'arrière-plan sont limités, le système arrête automatiquement la plupart des services d'arrière-plan. La gestion des services de NoWakeLock se concentre principalement sur les services de premier plan et les services liés.

!!! warning "Traitement prudent"
    L'interception de services critiques peut causer des anomalies de fonctionnement des applications. Il est recommandé d'utiliser d'abord le mode limitation, de confirmer l'absence d'impact avant d'envisager l'interception.