# Gestion des WakeLocks

Le WakeLock (verrou de réveil) empêche l'appareil d'entrer en mode veille et constitue un mécanisme clé du système Android qui affecte l'autonomie de la batterie.

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

## Types de WakeLock

### Types principaux

| Type | Valeur constante | Description | Impact sur l'énergie |
|------|------------------|-------------|---------------------|
| PARTIAL_WAKE_LOCK | 1 | Maintient le CPU en fonctionnement | Élevé |
| SCREEN_DIM_WAKE_LOCK | 6 | Écran assombri mais pas éteint (obsolète) | Moyen |
| SCREEN_BRIGHT_WAKE_LOCK | 10 | Maintient la luminosité de l'écran (obsolète) | Élevé |
| FULL_WAKE_LOCK | 26 | CPU + écran complètement allumé (obsolète) | Très élevé |
| PROXIMITY_SCREEN_OFF_WAKE_LOCK | 32 | Contrôle par capteur de proximité | Faible |

!!! warning "Explication des types"
    À l'exception de PARTIAL_WAKE_LOCK et PROXIMITY_SCREEN_OFF_WAKE_LOCK, les autres types ont été dépréciés après l'API Android 17. Les applications modernes utilisent principalement PARTIAL_WAKE_LOCK.

### Identifiants spéciaux

**WakeLocks système** :
- `PowerManagerService.WakeLocks`
- `AlarmManager`
- `AudioMix`

**Liés au réseau** :
- `WifiManager`
- `ConnectivityService`

**Services de localisation** :
- `LocationManagerService`
- `GpsLocationProvider`

## Description de l'interface

### Liste des WakeLocks

【Capture d'écran nécessaire : page de liste des WakeLocks】

**Informations de la liste** :
- **Nom** - Identifiant du WakeLock
- **Application** - Nom de package source
- **Type** - Icône du type de WakeLock
- **État** - Indicateur d'état actuel
- **Statistiques** - Nombre d'acquisitions et durée cumulée

### Indicateurs d'état

| État | Icône | Description |
|------|-------|-------------|
| Autoriser | 🟢 | Fonctionnement normal, aucune restriction |
| Limiter | 🟡 | Temps de dépassement défini |
| Intercepter | 🔴 | Bloque complètement l'acquisition |
| Actif | ⚡ | Actuellement détenu |

### Filtrage et tri

**Options de filtrage** :
- Tous
- Autoriser
- Limiter  
- Intercepter
- Actuellement actif

**Méthodes de tri** :
- Par nom
- Par application
- Par nombre d'acquisitions
- Par durée cumulée
- Par heure de dernière activité

## Options de configuration

### Modes de traitement

#### Mode Autoriser
- Aucune restriction appliquée
- WakeLock acquis et libéré normalement
- Mode par défaut, adapté à la plupart des situations

#### Mode Limiter
- Définit un temps de détention maximum
- Libération forcée en cas de dépassement
- À utiliser uniquement après confirmation par BBS qu'un WakeLock dure trop longtemps

!!! warning "Principe de configuration du délai"
    Le délai doit être déterminé en fonction des données réelles analysées par BBS, et non sur des valeurs prédéfinies. Observez la durée normale de ce WakeLock, puis définissez un délai légèrement supérieur à la valeur normale.

#### Mode Intercepter
- Bloque complètement l'acquisition du WakeLock
- L'application ne peut pas détenir ce WakeLock
- À utiliser uniquement après confirmation que ce WakeLock est complètement inutile et affecte gravement la batterie

## Méthodes d'utilisation

### Processus d'opération de base

!!! warning "Important : lecture obligatoire avant configuration"
    1. **D'abord diagnostiquer, puis configurer** - Utiliser BBS pour confirmer le problème avant configuration
    2. **Traitement individuel** - Configurer individuellement pour des problèmes spécifiques, éviter les opérations en lot
    3. **Surveillance continue** - Continuer à utiliser BBS pour vérifier les effets après configuration

### Visualiser et analyser les WakeLocks

1. Cliquez sur l'onglet "Wakelocks" en bas
2. Parcourez la liste actuelle et les données statistiques
3. Analysez les éléments anormaux en combinant avec les données BBS

### Configuration ciblée

1. **Confirmer le problème** - Basé sur les résultats d'analyse BBS
2. **Cliquer sur le WakeLock cible** pour entrer dans la page de configuration
3. **Choisir l'intervention minimale** - Privilégier le mode limitation
4. **Définir les paramètres** - Basé sur les données réellement observées
5. **Vérifier les fonctionnalités** - Confirmer que les fonctions de l'application sont normales

!!! danger "Interdiction de configuration en lot"
    N'utilisez pas la fonction d'opération en lot pour des configurations prédéfinies. Chaque problème de WakeLock est spécifique et nécessite une analyse et un traitement individuels.

## Vérification des effets

### Surveillance après configuration

**Étapes nécessaires** :
1. **Continuer la surveillance des données** des effets après configuration
   - Privilégier l'utilisation de BBS pour une évaluation complète
   - Utiliser aussi les statistiques intégrées NoWakeLock comme référence
2. **Vérifier les fonctionnalités de l'application** - Confirmer que toutes les fonctions de l'application restent normales
3. **Évaluer l'amélioration de la batterie** - Comparer la consommation réelle de batterie avant et après configuration

**Préparation de retour en arrière** :
- Si les fonctionnalités de l'application sont affectées, annuler immédiatement la configuration
- Si l'amélioration de la batterie n'est pas évidente, réévaluer s'il est nécessaire de limiter

## Détails techniques

### Implémentation Hook

NoWakeLock intercepte les méthodes clés dans PowerManagerService :

```kotlin
// Méthodes Hook principales (paramètres varient selon la version Android)
acquireWakeLockInternal(...)
releaseWakeLockInternal(...)
```

**Traitement de compatibilité des versions** :
- Utilise un mécanisme de cache de position des paramètres
- Supporte différentes signatures de méthodes d'Android 7.0-15.0
- Détection et adaptation automatiques de la position des paramètres

### Traitement de compatibilité

**Adaptation de version** :
- Supporte Android 7.0-15.0
- Détection dynamique de la position des paramètres
- Stratégie de traitement de dégradation

**Optimisation des performances** :
- Coût d'appel Hook < 1ms
- Correspondance de règles utilisant le cache
- Traitement asynchrone des données statistiques

### Stockage de données

**Données en temps réel** :
- WakeLocks actuellement actifs
- Mise en cache en mémoire, effacée après redémarrage

**Statistiques de session** :
- Enregistrements d'activité WakeLock de la session actuelle
- Stockage temporaire en base de données, vidé après redémarrage de l'appareil

## Fonctionnalités associées

- [Gestion des applications](app-management.md) - Visualiser tous les WakeLocks par application
- [Système de règles](rules-regex.md) - Configuration en lot avec expressions régulières

!!! warning "Conseils d'utilisation"
    Modifier les WakeLocks des services système critiques peut affecter la stabilité de l'appareil. Il est recommandé de commencer par les applications tierces et d'ajuster progressivement les services système.