# Vérification du module

La page de vérification du module vous aide à valider que NoWakeLock est correctement installé et en cours d'exécution.

## Éléments de vérification

### Vérifications de base

| Élément de vérification | Description | Raison d'échec |
|-------------------------|-------------|----------------|
| **Framework Xposed activé** | Détecte l'état d'exécution du framework Xposed | Framework non installé ou non activé |
| **Module chargé** | Confirme que NoWakeLock est reconnu par le framework | Module non coché ou installation échouée |
| **Module activé** | Vérifie que le module s'exécute dans le processus cible | Configuration de portée incorrecte |

### Vérifications fonctionnelles

| Élément de vérification | Description | Raison d'échec |
|-------------------------|-------------|----------------|
| **Hook fonctionne normalement** | Vérifie la fonction d'interception des appels système | Version système incompatible |
| **Lecture de configuration réussie** | Confirme la possibilité de lire les paramètres | Problème de permissions ou anomalie de stockage |
| **Base de données normale** | Vérifie les fonctions de lecture/écriture de la base de données | Espace de stockage insuffisant ou problème de permissions |

【Capture d'écran nécessaire : page de vérification du module - tout réussi】

## Description des états

### ✅ État normal
Tous les éléments de vérification affichent une icône verte, indiquant que le module fonctionne normalement.

### ❌ État anormal
L'icône rouge indique la présence de problèmes qui nécessitent un traitement.

### ⚠️ État d'avertissement
L'icône jaune indique que les fonctionnalités sont partiellement limitées, mais généralement utilisables.

## Dépannage

### Problèmes du framework Xposed

**Symptômes** : "Framework Xposed activé" affiche ❌

**Étapes de résolution** :
1. Confirmez que LSPosed ou EdXposed est installé
2. Vérifiez si le gestionnaire de framework affiche "Activé"
3. Redémarrez l'appareil
4. Vérifiez la compatibilité de la version du framework

### Problèmes de chargement du module

**Symptômes** : "Module chargé" affiche ❌

**Étapes de résolution** :
1. Ouvrez le gestionnaire Xposed
2. Accédez à la page "Modules"
3. Confirmez que NoWakeLock est coché
4. Redémarrez l'appareil
5. Vérifiez à nouveau

### Problèmes de configuration de portée

**Symptômes** : "Module activé" affiche ❌

**Étapes de résolution** :
1. Ouvrez le gestionnaire LSPosed
2. Cliquez sur le module NoWakeLock
3. Accédez aux paramètres de "Portée"
4. Confirmez que sont sélectionnés :
   - `android` (framework système)
   - `com.js.nowakelock` (l'application elle-même)
5. Redémarrez l'appareil

【Capture d'écran nécessaire : configuration de portée LSPosed】

### Problèmes de fonctionnalité Hook

**Symptômes** : "Hook fonctionne normalement" affiche ❌

**Causes possibles** :
- Version Android non supportée
- Modification d'interface due à la personnalisation du système
- Restrictions de politique SELinux

**Solutions** :
1. Vérifiez la compatibilité de l'appareil
2. Consultez les journaux Xposed :
   ```bash
   adb logcat | grep -i nowakelock
   ```
3. Essayez de réinstaller le module

### Problèmes de lecture de configuration

**Symptômes** : "Lecture de configuration réussie" affiche ❌

**Étapes de résolution** :
1. Vérifiez les permissions de stockage
2. Effacez les données de l'application :
   ```bash
   # Attention : ceci supprimera toute la configuration
   adb shell pm clear com.js.nowakelock
   ```
3. Rouvrez l'application

### Problèmes de base de données

**Symptômes** : "Base de données normale" affiche ❌

**Étapes de résolution** :
1. Vérifiez l'espace de stockage
2. Vérifiez les permissions de l'application
3. Réinitialisez la base de données :
   - Paramètres → Effacer les données
   - Reconfigurer

## Vérification avancée

### Consulter les journaux système
```bash
# Consulter les journaux liés à NoWakeLock
adb logcat | grep -i nowakelock

# Consulter les journaux Xposed
adb logcat | grep -i xposed
```

### Vérifier l'effet Hook
1. Ouvrez n'importe quelle application
2. Passez à la page WakeLock
3. Vérifiez s'il y a de nouveaux enregistrements WakeLock

### Tester la fonctionnalité des règles
1. Définissez une règle de test
2. Déclenchez le comportement système correspondant
3. Vérifiez si les données statistiques sont mises à jour

## Surveillance des performances

### Utilisation des ressources
La page de vérification du module affiche également :
- Taux d'utilisation CPU
- Occupation mémoire
- Utilisation de l'espace de stockage

### Indicateurs de performance

| Indicateur | Plage normale | Description |
|------------|---------------|-------------|
| Utilisation CPU | < 5% | Coût de traitement Hook |
| Occupation mémoire | < 50MB | Cache et occupation des données |
| Utilisation stockage | < 100MB | Taille de la base de données et des journaux |

## Vérification périodique

### Fréquence recommandée
- **Première installation** : Vérification quotidienne
- **Fonctionnement stable** : Vérification hebdomadaire
- **Après mise à jour système** : Vérification immédiate

### Vérification automatique
L'application effectue automatiquement des vérifications de base au démarrage, affichant une notification en cas d'anomalie.

### Enregistrement des vérifications
L'application conserve les enregistrements de vérification des 30 derniers jours, consultables dans les paramètres.

## Contacter le support

Si les vérifications échouent de manière persistante :

1. **Collecter les informations** :
   - Modèle d'appareil et version Android
   - Type et version du framework Xposed
   - Capture d'écran de la page de vérification

2. **Obtenir les journaux** :
   ```bash
   adb logcat -v time > nowakelock_log.txt
   ```

3. **Demander de l'aide** :
   - [GitHub Issues](https://github.com/NoWakeLock/NoWakeLock/issues)
   - [Groupe Telegram](https://t.me/nowakelock)
   - [Communauté Discord](https://discord.gg/kewmG5AShQ)

!!! warning "Rappel important"
    Lorsque la vérification du module échoue, les fonctionnalités de NoWakeLock peuvent ne pas fonctionner normalement. Veuillez résoudre tous les problèmes avant de procéder à la configuration.