# Guide d'analyse des problèmes

Avant de configurer toute règle, vous devez d'abord confirmer que l'appareil a réellement des problèmes de batterie. Ce guide explique comment utiliser les outils pour analyser et identifier les problèmes.

!!! warning "Principe important"
    Ce n'est qu'après avoir confirmé par l'analyse des données qu'un WakeLock/Alarm/Service cause réellement des problèmes de consommation en veille que vous devriez envisager de le limiter. Ne configurez pas basé sur des suppositions ou des préjugés.

## Outils d'analyse des données

### Outils d'analyse recommandés

1. **BetterBatteryStats (BBS)** - Premier choix, fournit l'analyse la plus complète
2. **Statistiques intégrées NoWakeLock** - Alternative quand BBS n'est pas disponible
3. **Utilisation combinée** - Obtenir des résultats de diagnostic plus précis

## Utiliser BetterBatteryStats (BBS) pour le diagnostic

### Étapes de diagnostic

1. **Installer BBS** et accorder les permissions nécessaires
2. **Définir la période de surveillance** - Il est recommandé de surveiller un cycle de veille complet
3. **Analyser les données clés** :
   - Kernel Wakelocks (verrous de réveil au niveau système)
   - Partial Wakelocks (verrous de réveil au niveau application)
   - Fréquence et durée de réveil de chaque application

### Critères d'identification des problèmes

- Examiner l'activité de réveil anormale pendant la veille de l'appareil
- Identifier les WakeLocks avec une durée trop longue ou une fréquence trop élevée
- Confirmer que ces activités causent réellement des problèmes de consommation de batterie

## Utiliser l'analyse des données intégrées de NoWakeLock

**Quand BBS n'est pas disponible**, vous pouvez utiliser les données statistiques de NoWakeLock lui-même :

### Méthodes de consultation des données

1. **Liste des WakeLocks** - Consulter les statistiques d'activité WakeLock de chaque application
2. **Tri par durée** - Identifier les WakeLocks avec une durée anormalement longue
3. **Tri par fréquence** - Découvrir les WakeLocks avec une fréquence d'acquisition anormalement élevée
4. **État actif** - Surveiller les WakeLocks actuellement détenus

### Critères d'identification des anomalies

- WakeLocks qui affichent encore une activité pendant la veille de l'appareil
- Durée cumulée d'un seul WakeLock dépassant largement les autres applications similaires
- Acquisition fréquente de WakeLocks quand l'application n'est pas utilisée
- Activité WakeLock anormale pendant les heures nocturnes

## Processus de dépannage

### Première étape : Confirmer l'existence du problème

1. **Surveiller avec des outils d'analyse** pendant un cycle d'utilisation complet (au moins 24 heures)
   - Privilégier l'utilisation de BBS pour une surveillance complète
   - Utiliser les statistiques intégrées NoWakeLock quand BBS n'est pas disponible
2. **Comparer l'activité WakeLock** et la consommation de batterie pendant la veille
3. **Confirmer les anomalies** - Rechercher les WakeLocks qui restent actifs quand l'appareil n'est pas utilisé

### Deuxième étape : Localiser précisément la source du problème

1. **Identifier les applications et services spécifiques** causant des WakeLocks anormaux
2. **Analyser les modèles** - Vérifier s'il s'agit de problèmes dans des créneaux temporels ou conditions spécifiques
3. **Évaluer l'impact** - Confirmer l'impact réel de ce WakeLock sur l'autonomie de la batterie

### Troisième étape : Intervention minimale

1. **Privilégier les paramètres d'application** - Vérifier s'il existe des options pertinentes dans l'application
2. **Tester l'effet de limitation** - Utiliser le mode limitation plutôt que l'interception complète
3. **Surveiller les résultats** - Continuer à surveiller les effets après configuration

!!! tip "Points clés du diagnostic"
    - Le problème doit être réellement existant, pas théorique
    - L'intervention doit être ciblée, pas préventive
    - Après chaque configuration, vérifier que les fonctionnalités de l'application sont normales

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

## Étapes suivantes

Après l'analyse des problèmes terminée :

1. [Gestion des WakeLocks](../features/wakelocks.md) - Configurer des règles WakeLock spécifiques
2. [Démarrage rapide](quick-start.md) - Guide de configuration rapide en 5 minutes
3. [Questions fréquentes](../reference/faq.md) - Solutions en cas de problèmes