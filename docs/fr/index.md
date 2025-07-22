# NoWakeLock

NoWakeLock est un module Android Xposed conçu pour gérer le comportement des WakeLocks, Alarms et Services de l'appareil, aidant à optimiser l'autonomie de la batterie.

!!! warning "Avertissement important et recommandations d'utilisation"
    **L'utilisation se fait à vos risques et périls, les développeurs ne sont pas responsables des dommages à l'appareil.**
    
    **Important** : Si votre appareil n'a pas de problèmes d'autonomie, l'utilisation de ce logiciel n'est pas recommandée. La gestion en arrière-plan d'Android 11+ a été grandement optimisée, et son utilisation n'est conseillée qu'après avoir confirmé des problèmes de consommation anormale avec des outils comme BetterBatteryStats.
    
    NoWakeLock est un outil de résolution de problèmes spécifiques, pas un logiciel d'optimisation générale.

!!! danger "⚠️ Important : Mode de récupération"
    **Si l'appareil se bloque au démarrage, redémarre en boucle ou présente des anomalies système** :
    
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

## Fonctionnalités principales

- **Gestion des WakeLocks** - Surveillance et contrôle des verrous de réveil des applications
- **Contrôle des Alarms** - Gestion des tâches programmées du système
- **Gestion des Services** - Contrôle du démarrage des services en arrière-plan
- **Gestion des applications** - Visualisation et configuration par application de tous les composants
- **Système de règles** - Configuration flexible prenant en charge les expressions régulières

## Démarrage rapide

1. [Guide d'installation](getting-started/installation.md) - Installer le module NoWakeLock
2. [Analyse des problèmes](getting-started/problem-analysis.md) - Analyser les problèmes de consommation (lecture obligatoire avant utilisation)
3. [Prise en main rapide](getting-started/quick-start.md) - Configuration de base en 5 minutes
4. [Vérification du module](getting-started/module-check.md) - Vérifier l'état du module

## Fonctionnalités principales

### 📱 Gestion des applications
- [Gestion des applications](features/app-management.md) - Visualisation et configuration par application

### ⚡ Contrôle système
- [Gestion des WakeLocks](features/wakelocks.md) - Mécanisme de verrouillage empêchant la mise en veille de l'appareil
- [Gestion des Alarmes](features/alarms.md) - Contrôle des tâches programmées du système
- [Gestion des Services](features/services.md) - Contrôle des services en arrière-plan

### 🔧 Outils de configuration
- [Règles et expressions régulières](features/rules-regex.md) - Règles de correspondance flexibles
- [Gestion des applications](features/app-management.md) - Gestion unifiée par application

## Guide d'utilisation

Opérations via les cinq onglets de l'interface principale de l'application :
- **Apps** - Liste des applications et gestion globale
- **Wakelocks** - Surveillance et contrôle des WakeLocks
- **Alarms** - Gestion des tâches programmées
- **Services** - Contrôle des services en arrière-plan
- **Settings** - Paramètres globaux et configuration

## Obtenir de l'aide

- [Questions fréquentes](reference/faq.md) - Réponses aux questions les plus courantes
- [Dépannage](reference/troubleshooting.md) - Diagnostic et résolution des problèmes
- [Glossaire](reference/glossary.md) - Explication des termes techniques

## Compatibilité

- **Version Android** : 7.0 (API 24) à 16.0 (API 35)
- **Framework Xposed** : LSPosed (recommandé), EdXposed
- **Support d'architecture** : ARM64, ARM32
- **Version actuelle** : 3.0.3 (version finale)

!!! error "Limitations de compatibilité des appareils"
    **Les appareils Samsung OneUI ne sont actuellement pas pris en charge**
    
    En raison des modifications apportées au code source Android par OneUI, la position du Hook a été tentée par diverses méthodes, mais n'a jamais pu être effective. Les autres appareils Android de fabricants tiers peuvent généralement être utilisés normalement.

## Communauté et support

- **Telegram** : [@nowakelock](https://t.me/nowakelock)
- **Discord** : [NoWakelock](https://discord.gg/kewmG5AShQ)
- **GitHub** : [NoWakeLock/NoWakeLock](https://github.com/NoWakeLock/NoWakeLock)

## Développeurs

Intéressé par l'implémentation technique ou la contribution au code ?

- [Documentation développeur](developers/) - Architecture technique et détails d'implémentation
- [Environnement de développement](developers/) - Comment participer au développement

---

!!! warning "Rappel d'utilisation"
    NoWakeLock nécessite le framework Xposed, veuillez sauvegarder vos données importantes avant utilisation. Les développeurs ne sont pas responsables des problèmes d'appareil.

!!! info "Licence"
    Ce projet est open source sous licence [GNU General Public License v3.0](https://github.com/NoWakeLock/NoWakeLock/blob/master/LICENSE).