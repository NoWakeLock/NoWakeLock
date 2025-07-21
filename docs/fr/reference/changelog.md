# Journal des modifications

Ce document enregistre l'historique des mises à jour de version et les changements importants de NoWakeLock.

## [v3.0.3 Build 80] - 2024-05-20

### 🎉 Mise à jour majeure
- **Première version officielle de la série 3.x**, refactorisation complète
- **Material Design 3** conception d'interface entièrement nouvelle
- Support de compatibilité **Android 7-15**
- **Support multi-utilisateur** implémentation complète
- **Système de suivi d'événements** mise à niveau complète

### ✨ Nouvelles fonctionnalités
- Intégration du framework UI **Jetpack Compose**
- Fonctionnalité **Statistiques d'application** améliorée
- Fonctionnalité **Recherche globale**
- **Détection de module** vérification d'état complète
- **Gestion utilisateur** support d'environnement multi-utilisateur

### ⚠️ Note de compatibilité
- **Problèmes connus avec les appareils Samsung** - Certaines fonctionnalités peuvent être limitées
- **Recommandation de mise à niveau** :
  1. Désinstaller la version v2.0.x
  2. Redémarrer l'appareil
  3. Installer la version v3.x
  4. Redémarrer l'appareil

### 🔄 Migration de configuration
- **Configuration v2.0 incompatible** - Reconfiguration nécessaire
- Il est recommandé de sauvegarder les configurations importantes avant la mise à niveau
- Fonctionnalité d'import/export de configuration fournie

---

## [v3.0.2 Build 77] - 2024-05-16

### ✨ Nouvelles fonctionnalités
- **Fonction de vérification de module** - Validation complète de l'état du module
- **Optimisation de l'interface de détection** - Affichage d'état plus clair
- **Stratégie de migration de base de données** - Traitement amélioré de mise à niveau de version

### 🐛 Corrections
- Correction des problèmes de détection de module sur certains appareils
- Amélioration du processus de mise à niveau de base de données
- Optimisation de la vitesse de réponse de l'interface utilisateur

---

## [v3.0.1 Build 75-76] - 2024-05-05

### 🔧 Améliorations
- **Stratégie Hook unifiée** - Optimisation du traitement Service, Alarm, WakeLock
- **Optimisation des performances** - Réduction de l'occupation des ressources système
- **Refactorisation du code** - Amélioration de la qualité et maintenabilité du code

### 🐛 Corrections
- Correction des problèmes de compatibilité sur certaines versions Android
- Amélioration de la stabilité Hook
- Optimisation de l'utilisation mémoire

---

## [v2.0.5 Build 62-63] - 2024-03

### ✨ Nouvelles fonctionnalités
- **Icône de démarrage thématisée** - Support de thème dynamique
- **Amélioration de gestion d'erreurs** - Traitement d'anomalies de données ContentProvider
- **Mise à jour Hook Service** - Support Android API 29-40

### 🔧 Améliorations
- Optimisation des performances de requête de données
- Amélioration de la réponse de l'interface utilisateur
- Amélioration de l'enregistrement des journaux d'erreur

---

## Série v2.x (versions historiques)

### Caractéristiques principales
- Gestion de base WakeLock/Alarm/Service
- Conception UI traditionnelle
- Support de base Android 7+
- Environnement mono-utilisateur

### Limitations connues
- Conception UI relativement simple
- Fonctionnalités relativement basiques
- Pas de support d'environnement multi-utilisateur
- Configuration incompatible avec v3.x

---

## Comparaison des versions

### Principales différences v3.x vs v2.x

| Caractéristique | v2.x | v3.x |
|-----------------|------|------|
| Framework UI | Vue traditionnelle | Jetpack Compose |
| Langage de conception | Material Design 2 | Material Design 3 |
| Support Android | 7-11 | 7-15 |
| Multi-utilisateur | ❌ | ✅ |
| Détection de module | Basique | Complète |
| Performance | Générale | Optimisée |
| Compatibilité config | - | Incompatible v2.x |

### Recommandations de mise à niveau

#### Mise à niveau depuis v2.x
1. **Sauvegarder la configuration** - Enregistrer les paramètres de règles actuels
2. **Désinstallation complète** - Désinstaller v2.x et redémarrer
3. **Installation nouvelle** - Installer v3.x et reconfigurer
4. **Configuration progressive** - Restaurer progressivement les paramètres selon la sauvegarde

#### Nouveaux utilisateurs
- Installer directement la dernière version v3.x
- Configurer selon le guide [Démarrage rapide](../getting-started/quick-start.md)

---

## Problèmes connus

### Problèmes connus v3.0.3
- **Appareils Samsung** - Certaines fonctionnalités peuvent être instables
- **Android 15** - Certaines nouvelles fonctionnalités peuvent ne pas être entièrement supportées
- **Utilisation mémoire** - Peut avoir un impact sur les performances sur les appareils à faible mémoire

### Solutions temporaires
- Les appareils Samsung sont recommandés d'utiliser une configuration conservatrice
- Les utilisateurs d'Android 15 sont priés de suivre les mises à jour ultérieures
- Les appareils à faible mémoire sont recommandés de réduire le nombre de règles

---

## Feuille de route de développement

### Plan à court terme (v3.1.x)
- Amélioration de la compatibilité des appareils Samsung
- Support complet d'Android 15
- Optimisation des performances
- Plus de modèles de configuration prédéfinis

### Plan à moyen terme (v3.2.x)
- Synchronisation de configuration cloud (optionnelle)
- Analyse statistique plus riche
- Système de plugins
- Ouverture d'API

### Plan à long terme (v4.x)
- Conception d'architecture entièrement nouvelle
- Support d'appareils plus large
- Configuration assistée par IA
- Fonctionnalités d'entreprise

---

## Obtenir les mises à jour

### Canaux officiels
- [GitHub Releases](https://github.com/NoWakeLock/NoWakeLock/releases) - Dernière version
- [IzzyOnDroid](https://apt.izzysoft.de/fdroid/index/apk/com.js.nowakelock) - Dépôt F-Droid

### Types de versions
- **Stable** - Version stable, recommandée pour les utilisateurs généraux
- **Beta** - Version test, aperçu anticipé des nouvelles fonctionnalités
- **Dev** - Version développement, uniquement pour tests de développement

### Notifications de mise à jour
- L'application vous alertera des nouvelles versions
- Suivez GitHub Releases pour les dernières informations
- Rejoignez les groupes communautaires pour des nouvelles de première main

!!! info "Stratégie de version"
    NoWakeLock suit le contrôle de version sémantique, les mises à jour majeures seront annoncées à l'avance, garantissant aux utilisateurs suffisamment de temps de préparation pour la mise à niveau.

!!! warning "Rappel de mise à niveau"
    Sauvegardez impérativement la configuration avant les mises à niveau de versions majeures, certaines versions peuvent présenter des incompatibilités.