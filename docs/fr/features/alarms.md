# Gestion des Alarmes

Les Alarmes (tâches programmées) constituent le mécanisme de minuteur du système Android, utilisé pour déclencher des opérations à des moments spécifiques. Des alarmes fréquentes affectent l'autonomie de l'appareil.

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

### Rôle des Alarmes
- Exécution de tâches programmées
- Déclenchement d'opérations périodiques
- Minuteur au niveau système
- Mécanisme de maintien en vie des applications

### Objectifs de gestion
- Surveiller la configuration et le déclenchement des alarmes
- Identifier les tâches programmées excessivement fréquentes
- Contrôler la fréquence de déclenchement des alarmes
- Réduire les réveils inutiles

## Description de l'interface

### Liste des Alarmes

【Capture d'écran nécessaire : page de liste des Alarmes】

**Informations de la liste** :
- **Étiquette** - Identifiant de l'alarme
- **Application** - Nom de package source
- **Type** - Icône du type d'alarme
- **État** - État d'interception
- **Statistiques** - Nombre de déclenchements et informations temporelles

### Affichage des états

| État | Icône | Description |
|------|-------|-------------|
| Autoriser | 🟢 | Déclenchement normal |
| Limiter | 🟡 | Réduction de la fréquence de déclenchement |
| Intercepter | 🔴 | Empêcher le déclenchement |
| En attente | ⏰ | Configuré en attente de déclenchement |

## Types d'Alarmes

### Classification par condition de déclenchement

| Type | Description | Usage typique |
|------|-------------|---------------|
| RTC | Déclenchement à heure absolue | Réveils, rappels |
| RTC_WAKEUP | Réveil de l'appareil à heure absolue | Notifications importantes |
| ELAPSED_REALTIME | Déclenchement à temps relatif | Vérifications périodiques |
| ELAPSED_REALTIME_WAKEUP | Réveil à temps relatif | Tâches en arrière-plan |

### Classification par mode de répétition

**Alarmes uniques** :
- Annulation automatique après une exécution
- Utilisées pour des tâches à moments spécifiques

**Alarmes répétitives** :
- Déclenchement répétitif à intervalles fixes
- Courantes pour les tâches de synchronisation et mise à jour

**Alarmes exactes** :
- Déclenchement précis dans le temps
- Consommation de ressources système plus élevée

## Options de configuration

### Modes de traitement

#### Mode Autoriser
- Configuration et déclenchement normaux des alarmes
- Aucune intervention
- Adapté aux fonctions système importantes

#### Mode Limiter
- Réduction de la fréquence de déclenchement
- Fusion des temps de déclenchement proches
- Retard des alarmes non urgentes

#### Mode Intercepter
- Blocage complet de la configuration d'alarmes
- L'application ne peut pas créer ce type d'alarme
- Peut sérieusement affecter les fonctionnalités de l'application

### Options avancées

**Fusion intelligente** :
- Fusion des alarmes avec des temps proches
- Réduction du nombre de réveils de l'appareil

**Mode traitement par lots** :
- Retard des alarmes non urgentes
- Exécution avec d'autres tâches

## Méthodes d'utilisation

### Visualiser la liste des Alarmes

1. Cliquez sur l'onglet "Alarms" en bas
2. Consultez les alarmes actuellement actives
3. Utilisez les filtres pour voir des états spécifiques

### Configurer les règles d'Alarmes

1. Cliquez sur l'élément d'alarme cible
2. Sélectionnez le mode de traitement
3. Définissez les paramètres spécifiques :
   - Temps d'intervalle minimum
   - Temps de retard
   - Options de traitement par lots

【Capture d'écran nécessaire : page de configuration des Alarmes】

### Gestion en lot

**Configuration en lot par application** :
1. Filtrez les alarmes d'une application spécifique
2. Sélectionnez l'opération en lot
3. Appliquez des règles uniformes

**Configuration en lot par type** :
- Limitation de tous les types WAKEUP
- Réduction de fréquence de toutes les alarmes répétitives
- Traitement prudent des alarmes système

## Application pratique

### Identification des problèmes

#### Caractéristiques des alarmes anormales

**Déclenchement haute fréquence** :
- Alarmes répétitives avec intervalle inférieur à 1 minute
- Déclenchement fréquent pendant les heures nocturnes
- Fonctionnement continu même quand l'appareil est immobile

## Implémentation technique

### Mécanisme Hook

Interception des méthodes clés d'AlarmManagerService :
```kotlin
// Appel de configuration d'alarme système
setImpl(
    int type,
    long triggerAtTime,
    long windowLength,
    long interval,
    PendingIntent operation,
    IAlarmListener directReceiver,
    String listenerTag,
    WorkSource workSource,
    AlarmManager.AlarmClockInfo alarmClock,
    int callingUid,
    String callingPackage
)

// Traitement du déclenchement d'alarme
triggerAlarmsLocked(ArrayList<Alarm> triggerList)
```

### Traitement des données

**Traitement en temps réel** :
- Vérification des règles lors de la configuration d'alarmes
- Contrôle de fréquence lors du déclenchement
- Ajustement dynamique des temps de déclenchement

**Historique** :
- Stockage de l'historique de déclenchement en base de données
- Analyse statistique et calcul de tendances
- Nettoyage automatique des données expirées

### Compatibilité

**Support de versions** :
- Support complet d'Android 7.0+
- Adaptation des API de différentes versions
- Stratégie de compatibilité dégradée

**Optimisation des performances** :
- Minimisation du coût des Hooks
- Algorithme de correspondance de règles efficace
- Traitement asynchrone des données statistiques

## Fonctionnalités associées

- [Gestion des applications](app-management.md) - Visualiser toutes les alarmes d'une application
- [Gestion des WakeLocks](wakelocks.md) - Optimisation en combinaison avec les WakeLocks
- [Système de règles](rules-regex.md) - Configuration en lot avec expressions régulières

!!! tip "Conseils d'optimisation"
    L'optimisation des alarmes a des effets évidents, mais nécessite un équilibre avec les fonctionnalités. Il est recommandé de commencer par les applications non critiques et d'ajuster progressivement les paramètres des applications importantes.

!!! warning "Points d'attention"
    Une limitation excessive des alarmes système peut affecter les fonctions normales de l'appareil, comme la synchronisation automatique de l'heure, la vérification des mises à jour système, etc.