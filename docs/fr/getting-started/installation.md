# Guide d'installation

!!! danger "⚠️ Mode de récupération - Le plus important !"
    **Si l'appareil présente des anomalies de démarrage, se bloque ou redémarre en boucle après l'installation** :
    
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
    
    **Mesures préventives** : Configurez avec précaution lors de la première utilisation, testez progressivement l'effet des règles.

## Prérequis

### Configuration requise
- Android 7.0 (API 24) ou version supérieure

!!! error "Limitations de compatibilité des appareils"
    **Les appareils Samsung OneUI ne sont actuellement pas pris en charge**
    
    En raison des modifications apportées au code source Android par OneUI, la position du Hook a été tentée par diverses méthodes, mais n'a jamais pu être effective. Nous recherchons une solution, mais actuellement cela ne peut pas fonctionner normalement sur les appareils Samsung OneUI.
    
    Les autres appareils Android de fabricants tiers peuvent généralement être utilisés normalement.

### Framework Xposed
Installez l'un des frameworks suivants :

| Framework | Version applicable | Recommandation |
|-----------|-------------------|----------------|
| LSPosed | Android 8.1+ | ⭐⭐⭐⭐⭐ |
| EdXposed | Android 8.0-11 | ⭐⭐⭐ |

!!! info "Choix du framework"
    Il est recommandé d'utiliser LSPosed pour une meilleure compatibilité et stabilité.

## Télécharger NoWakeLock

### Canaux officiels

[![GitHub](https://img.shields.io/badge/GitHub-Releases-blue)](https://github.com/NoWakeLock/NoWakeLock/releases)
[![IzzyOnDroid](https://img.shields.io/badge/IzzyOnDroid-F-Droid-green)](https://apt.izzysoft.de/fdroid/index/apk/com.js.nowakelock)

**Méthodes de téléchargement** :
- **GitHub Releases** - Téléchargement direct du fichier APK
- **IzzyOnDroid** - Installation après ajout de la source IzzyOnDroid dans F-Droid
- **F-Droid officiel** - Prévu

!!! tip "Configuration de la source F-Droid"
    Pour installer via IzzyOnDroid :
    1. Ajoutez la source dans l'application F-Droid : `https://apt.izzysoft.de/fdroid/repo`
    2. Recherchez NoWakeLock pour l'installer

### Sélection de version

- **Version stable** - Téléchargement depuis GitHub Releases ou IzzyOnDroid
- **Version test** - Construction depuis la branche dev

!!! warning "Support uniquement pour les versions officielles"
    Seules les versions téléchargées depuis les canaux officiels sont supportées.

## Étapes d'installation

### 1. Télécharger l'APK
Téléchargez la dernière version du fichier APK depuis les canaux officiels.

### 2. Installer l'application
```bash
# Installation avec ADB (optionnel)
adb install nowakelock-v3.x.x.apk
```

Ou installez directement le fichier APK sur l'appareil.

【Capture d'écran nécessaire : interface d'installation】

### 3. Activer le module
1. Ouvrez le gestionnaire Xposed (LSPosed/EdXposed)
2. Accédez à la page "Modules"
3. Cochez NoWakeLock
4. Redémarrez l'appareil

【Capture d'écran nécessaire : liste des modules LSPosed】

### 4. Configurer la portée
Définissez la portée du module dans LSPosed :

**Portée requise** :
- `android` (framework système)

!!! tip "Explication de la portée"
    NoWakeLock nécessite uniquement la portée du framework système `android` pour fonctionner normalement.

【Capture d'écran nécessaire : configuration de la portée】

## Vérification de l'installation

### Vérifier l'état du module
1. Ouvrez l'application NoWakeLock
2. Accédez à la page "Vérification du module"
3. Confirmez que tous les éléments affichent un ✅ vert

【Capture d'écran nécessaire : page de vérification du module】

### Éléments de vérification

| Élément de vérification | Description |
|------------------------|-------------|
| Framework Xposed activé | Le framework fonctionne normalement |
| Module chargé | Le module NoWakeLock est reconnu |
| Hook fonctionne normalement | Interception des appels système réussie |
| Lecture de configuration réussie | L'application peut lire la configuration |

### Tester les fonctionnalités
1. Vérifiez si la page "Applications" affiche les applications installées
2. Vérifiez si la page "WakeLocks" contient des données
3. Essayez de définir une règle simple

## Problèmes courants

### Module non activé
**Symptômes** : La vérification du module affiche ❌  
**Solutions** :
1. Confirmez que le framework Xposed fonctionne normalement
2. Vérifiez si le module est coché
3. Redémarrez l'appareil puis vérifiez à nouveau

### Hook ne fonctionne pas
**Symptômes** : Pas de données WakeLock/Alarm  
**Solutions** :
1. Confirmez que la portée inclut `android`
2. Vérifiez la politique SELinux
3. Consultez les journaux Xposed

### Application plante
**Symptômes** : L'application plante immédiatement à l'ouverture  
**Solutions** :
1. Vérifiez la compatibilité de la version Android
2. Effacez les données de l'application
3. Réinstallez le module

## Désinstaller le module

### Étapes de désinstallation complète
1. Décochez le module dans le gestionnaire Xposed
2. Redémarrez l'appareil
3. Désinstallez l'application NoWakeLock

## Étapes suivantes

Après l'installation :

1. [Démarrage rapide](quick-start.md) - Configuration en 5 minutes
2. [Vérification du module](module-check.md) - Vérification détaillée de l'état du module
3. [Gestion des WakeLocks](../features/wakelocks.md) - Commencer à gérer les WakeLocks