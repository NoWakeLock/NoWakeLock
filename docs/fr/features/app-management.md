# Gestion des applications

La fonctionnalité de gestion des applications vous permet de visualiser et configurer tous les WakeLocks, Alarmes et Services par application, offrant une vue de gestion unifiée au niveau de l'application.

## Aperçu des fonctionnalités

### Fonctions principales
- Affichage catégorisé de toutes les activités par application
- Informations statistiques au niveau application
- Configuration en lot de tous les composants d'une application
- Analyse des modèles de comportement des applications

### Avantages de gestion
- Perspective unifiée de l'application
- Facilite l'identification des applications problématiques
- Simplifie le processus de configuration en lot
- Analyse comparative intuitive

## Description de l'interface

### Liste des applications

【Capture d'écran nécessaire : page de liste des applications】

**Informations affichées** :
- **Icône et nom de l'application**
- **Nom de package** - Identifiant unique de l'application
- **Résumé statistique** - Nombre de WakeLocks/Alarmes/Services
- **État d'activité** - Indicateur d'activité récente
- **Utilisateur** - Identifiant d'utilisateur en environnement multi-utilisateur

### Cartes de statistiques

Chaque application affiche trois types de statistiques :

| Type | Indicateur | Description |
|------|------------|-------------|
| WakeLock | Compteur/Durée | Nombre d'acquisitions et temps de détention cumulé |
| Alarm | Compteur/Fréquence | Nombre de déclenchements et intervalle moyen |
| Service | Compteur/Durée | Nombre de démarrages et temps d'exécution |

### Filtrage et tri

**Options de filtrage** :
- Toutes les applications
- Applications système
- Applications utilisateur
- Applications avec activité
- Applications avec règles configurées

**Méthodes de tri** :
- Par nom d'application
- Par date d'installation
- Par fréquence d'activité
- Par consommation de ressources
- Par état de configuration

## Page de détails de l'application

### Accéder à la page de détails
1. Cliquez sur n'importe quelle application dans la liste
2. Entrez dans la page de détails de l'application

【Capture d'écran nécessaire : page de détails de l'application】

### Contenu de la page de détails

#### Informations de l'application
- Nom, version, nom de package de l'application
- Date d'installation, date de mise à jour
- Liste des permissions
- État d'exécution actuel

#### Statistiques d'activité
- **Graphique chronologique** - Tendance d'activité sur 12 heures
- **Statistiques catégorisées** - Données détaillées WakeLock/Alarm/Service
- **Consommation de ressources** - Utilisation CPU, mémoire, énergie

#### Liste des composants
Affichage catégorisé par onglets :
- **WakeLocks** - Tous les WakeLocks de cette application
- **Alarms** - Toutes les Alarmes de cette application
- **Services** - Tous les Services de cette application

## Fonctionnalités de configuration

### Configuration au niveau application

#### Paramètres globaux
Définir des règles uniformes pour l'application :
```
Mode Autoriser :
- Tous les composants fonctionnent normalement
- Adapté aux applications importantes

Mode Limiter :
- Limitations temporelles uniformes
- Adapté aux applications générales

Mode Intercepter :
- Blocage de toute activité en arrière-plan
- Adapté aux applications problématiques
```

#### Mécanisme d'héritage
- Les composants peuvent hériter des paramètres de l'application
- Support de configurations d'exception pour des composants individuels
- Priorité : Paramètres composant > Paramètres application > Défaut global

## Support multi-utilisateur

### Changement d'utilisateur
Sur les appareils multi-utilisateurs :
1. Sélecteur d'utilisateur en haut
2. Basculer pour voir les applications de différents utilisateurs
3. Configuration indépendante pour chaque utilisateur

【Capture d'écran nécessaire : interface de changement d'utilisateur】

### Isolation des utilisateurs
- Les configurations de chaque utilisateur n'interfèrent pas entre elles
- Les applications système sont partagées entre tous les utilisateurs
- Les applications utilisateur ne s'affichent que sous l'utilisateur correspondant

## Import/Export

### Sauvegarde de configuration

#### Exporter la configuration
```json
{
  "version": "3.0",
  "timestamp": "2024-01-01T00:00:00Z",
  "user_id": 0,
  "apps": [
    {
      "package_name": "com.example.app",
      "app_config": {
        "mode": "limit",
        "wakelock_timeout": 60000
      },
      "components": [
        {
          "type": "wakelock",
          "name": "ExampleWakeLock",
          "mode": "allow"
        }
      ]
    }
  ]
}
```

#### Importer la configuration
- Support d'import de configuration complète
- Import sélectif d'applications spécifiques
- Choix de stratégie de gestion des conflits

## Fonctionnalités associées

- [Gestion des WakeLocks](wakelocks.md) - Contrôle détaillé des WakeLocks
- [Gestion des Alarmes](alarms.md) - Contrôle détaillé des Alarmes
- [Gestion des Services](services.md) - Contrôle détaillé des Services
- [Système de règles](rules-regex.md) - Configuration en lot avec expressions régulières

!!! tip "Conseils d'utilisation"
    La gestion des applications est la fonctionnalité d'entrée de NoWakeLock. Il est recommandé aux nouveaux utilisateurs de commencer ici pour comprendre les modèles de comportement de chaque application, puis procéder à des optimisations ciblées.