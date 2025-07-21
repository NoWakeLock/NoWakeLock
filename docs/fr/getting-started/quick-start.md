# Démarrage rapide

Configuration rapide de NoWakeLock en 5 minutes pour commencer à optimiser l'autonomie de la batterie de votre appareil.

## Étape 1 : Vérifier l'installation

Ouvrez NoWakeLock, accédez à la page "Vérification du module" :

- ✅ Tous les éléments affichent du vert = installation réussie
- ❌ Tout élément rouge = consultez le [Guide d'installation](installation.md)

【Capture d'écran nécessaire : interface de vérification du module réussie】

## Étape 2 : Visualiser la liste des applications

1. Cliquez sur l'onglet "Applications" en bas
2. Parcourez la liste des applications installées
3. Notez les statistiques numériques à droite

【Capture d'écran nécessaire : interface de liste des applications】

**Informations de la liste** :
- Nom et icône de l'application
- Statistiques WakeLock/Alarm/Service
- Heure de dernière activité

## Étape 3 : Analyser les problèmes de consommation

⚠️ **Important** : Avant de configurer toute règle, vous devez d'abord confirmer que l'appareil a réellement des problèmes de batterie.

### Utiliser les outils d'analyse de problèmes
- Consultez le [Guide d'analyse des problèmes](problem-analysis.md) pour le processus d'analyse complet
- Utilisez BetterBatteryStats ou les statistiques intégrées de NoWakeLock pour confirmer les problèmes
- Configurez uniquement les WakeLocks confirmés comme problématiques

### Identifier rapidement les applications problématiques
1. Cliquez sur l'onglet "Wakelocks"
2. Portez attention aux éléments avec des "compteurs" et "durées" élevés
3. Ce sont les principales sources de consommation d'énergie

【Capture d'écran nécessaire : liste des WakeLocks】

## Étape 4 : Définir des règles de base

### Configurer les WakeLocks problématiques
1. Cliquez sur l'élément WakeLock anormal
2. Sélectionnez la méthode de traitement :
   - **Limiter** - Libération en cas de dépassement de délai (recommandé pour débutants)
   - **Intercepter** - Bloquer complètement (à utiliser avec précaution)

【Capture d'écran nécessaire : interface de définition de règles】

## Observer les effets

### Vérifier après 24 heures
1. **Interface Applications** - Vérifier l'effet d'interception
2. **Fonctionnalité de l'application** - Confirmer que les fonctions importantes sont normales
3. **Utilisation de la batterie** - Statistiques de batterie du système

### Indicateurs clés
- Augmentation du nombre d'interceptions ✅
- Notifications push des applications normales ✅  
- Amélioration de l'autonomie de la batterie ✅

【Capture d'écran nécessaire : graphiques de statistiques】

## Problèmes courants

### Une application ne peut pas recevoir de notifications
**Solution** :
1. Trouvez le WakeLock lié aux notifications push de cette application
2. Changez en mode "Autoriser"
3. Observez pendant quelques heures

### Aucun effet après configuration
**Éléments à vérifier** :
1. Le module fonctionne-t-il normalement
2. Les règles sont-elles correctement appliquées
3. L'application a-t-elle redémarré

### L'appareil devient lent ou saccadé
**Traitement immédiat** :
1. Désactivez temporairement toutes les règles
2. Restaurez progressivement les applications importantes
3. Évitez d'intercepter les services système critiques

## Opérations suivantes

### Configuration approfondie
- [Gestion des WakeLocks](../features/wakelocks.md) - Contrôle détaillé des WakeLocks
- [Système de règles](../features/rules-regex.md) - Correspondance par expressions régulières

### Fonctionnalités avancées
- [Gestion des Alarmes](../features/alarms.md) - Contrôle des tâches programmées
- [Gestion des Services](../features/services.md) - Gestion des services en arrière-plan
- [Gestion des applications](../features/app-management.md) - Configuration par application

### Obtenir de l'aide
- [Questions fréquentes](../reference/faq.md) - Questions les plus courantes
- [Dépannage](../reference/troubleshooting.md) - Guide de diagnostic des problèmes

!!! tip "Conseils d'utilisation"
    Il est recommandé de commencer avec des paramètres conservateurs et d'ajuster progressivement. Assurez-vous que les fonctions importantes des applications sont normales avant de procéder à des optimisations plus agressives.