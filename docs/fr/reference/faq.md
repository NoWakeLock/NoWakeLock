# Questions fréquentes

Collecte des questions les plus courantes rencontrées par les utilisateurs et leurs solutions.

## Liées à l'installation

### Q : Le module ne fonctionne pas après installation ?
**R** : Vérifiez les éléments suivants :
1. Confirmez que le framework Xposed fonctionne normalement
2. Cochez le module NoWakeLock dans le gestionnaire de framework
3. Redémarrez l'appareil puis vérifiez l'état du module
4. Confirmez que la portée inclut le framework système `android`

### Q : Quels frameworks Xposed sont supportés ?
**R** : Frameworks supportés :
- **LSPosed** - Recommandé, meilleure compatibilité
- **EdXposed** - Support partiel sur certains appareils
- **TaiChi** - Non recommandé, problèmes de compatibilité possibles

### Q : La vérification du module affiche des ❌ rouges ?
**R** : Traitez selon l'élément spécifique :
- **Framework non activé** → Vérifiez l'installation Xposed
- **Module non chargé** → Confirmez que le module est coché et redémarrez
- **Hook échoué** → Possible incompatibilité de version système

## Utilisation des fonctionnalités

### Q : Aucun effet après définition des règles ?
**R** : Causes possibles :
1. **Règles non effectives** - Vérifiez si les règles sont activées
2. **Redémarrage d'application** - Certaines règles nécessitent un redémarrage de l'application
3. **Cache système** - Attendez quelques minutes pour que le système applique les nouvelles règles
4. **Problèmes de permissions** - Confirmez que NoWakeLock a les permissions nécessaires

### Q : Une application ne peut pas recevoir de notifications push ?
**R** : Étapes de résolution :
1. Consultez la liste des WakeLocks de cette application
2. Trouvez les WakeLocks liés aux notifications push (contiennent généralement Push, GCM, FCM)
3. Changez ces WakeLocks en mode "Autoriser"
4. Redémarrez l'application pour tester les notifications push

### Q : L'application devient lente ou présente des fonctionnalités anormales ?
**R** : Traitement immédiat :
1. Désactivez temporairement toutes les règles de cette application
2. Activez les règles une par une pour identifier la règle problématique
3. Ajustez les paramètres de la règle problématique ou passez en mode limitation
4. Évitez d'intercepter les services système critiques

### Q : Comment déterminer si l'optimisation est efficace ?
**R** : Indicateurs à observer :
- **Court terme (24 heures)** : Vérifiez les statistiques d'interception, fonctionnement normal des applications
- **Moyen terme (une semaine)** : Amélioration évidente de l'autonomie de la batterie
- **Long terme** : Amélioration générale de la fluidité du système

## Problèmes de compatibilité

### Q : Compatibilité des versions Android ?
**R** : Situation de support :
- **Android 7.0-16** - AOSP Support complet

### Q : Problèmes avec des marques d'appareils spécifiques ?
**R** : Problèmes connus :
- **MIUI** - Nécessite de désactiver l'optimisation MIUI, autoriser l'exécution en arrière-plan
- **ColorOS** - Peut nécessiter d'autoriser le démarrage automatique dans la gestion des permissions
- **EMUI** - Il est recommandé d'ajouter NoWakeLock aux applications protégées
- **OneUI** - ⚠️ **Actuellement non supporté** - En raison des modifications du code source Android par OneUI, la position Hook a été tentée par diverses méthodes mais n'a jamais pu être effective

### Q : Dysfonctionnement après mise à jour système ?
**R** : Étapes de traitement :
1. Vérifiez si le framework Xposed fonctionne encore
2. Confirmez que le module NoWakeLock est toujours en état activé
3. Réinstallez le module si nécessaire
4. Reconfigurez les paramètres de portée

## Problèmes de performance

### Q : NoWakeLock consomme-t-il de la batterie ?
**R** : Dans des conditions normales, NoWakeLock consomme très peu :
- **Utilisation CPU** < 1%
- **Occupation mémoire** < 50MB
- **Activité en arrière-plan** minimisée

Si vous découvrez une consommation anormale, veuillez vérifier la configuration ou contacter le support.

### Q : L'appareil devient lent ou saccadé ?
**R** : Causes possibles :
1. **Trop de règles** - Réduisez les règles inutiles
2. **Expressions régulières complexes** - Simplifiez les expressions régulières
3. **Interceptions fréquentes** - Ajustez la stratégie d'interception
4. **Mémoire insuffisante** - Redémarrez l'appareil pour libérer la mémoire

### Q : Le démarrage des applications ralentit ?
**R** : Vérifiez si :
1. Vous avez intercepté des Services nécessaires au démarrage de l'application
2. Vous avez excessivement limité les WakeLocks liés à l'initialisation
3. Les règles affectent le processus de démarrage de l'application

## Problèmes de données

### Q : Les données statistiques ne sont pas précises ?
**R** : Situations possibles :
1. **Retard des données** - Les statistiques sont mises à jour toutes les 5 minutes
2. **Redémarrage de l'appareil** - Certaines données en temps réel sont réinitialisées après redémarrage
3. **Problème de fuseau horaire** - Vérifiez les paramètres de fuseau horaire de l'appareil
4. **Problème de stockage** - Nettoyez le cache de la base de données

### Q : Perte des données historiques ?
**R** : Politique de conservation des données :
- **Données en temps réel** - Effacées après redémarrage de l'appareil
- **Données statistiques** - Conservées 30 jours
- **Données de configuration** - Sauvegardées en permanence (sauf suppression manuelle)

### Q : Impossible d'importer la configuration exportée ?
**R** : Éléments à vérifier :
1. Le format de fichier est-il correct (JSON)
2. La version est-elle compatible
3. Le fichier est-il corrompu
4. Les permissions sont-elles suffisantes

## Problèmes multi-utilisateurs

### Q : Comment utiliser dans un environnement multi-utilisateur ?
**R** : Points à noter :
1. Chaque utilisateur doit être configuré séparément
2. Les applications système partagent les paramètres entre tous les utilisateurs
3. Les configurations n'interfèrent pas entre elles après changement d'utilisateur
4. Certaines fonctionnalités nécessitent les permissions d'utilisateur principal

### Q : Impossible d'utiliser dans le profil de travail ?
**R** : Limitations du profil de travail :
1. Nécessite d'installer le framework Xposed séparément dans le profil de travail
2. Certaines politiques d'entreprise peuvent interdire les modules Xposed
3. Contactez l'administrateur pour confirmer les politiques pertinentes

## Questions de sécurité

### Q : NoWakeLock est-il sûr ?
**R** : Mesures de sécurité :
- **Code open source** - Tout le code est public et auditable
- **Aucune permission réseau** - N'upload aucune donnée
- **Stockage local** - Toutes les données sont sauvegardées uniquement localement
- **Permissions minimales** - Ne demande que les permissions système nécessaires

### Q : Cela affecte-t-il la stabilité du système ?
**R** : Considérations de sécurité :
1. Utilise le mécanisme Hook standard du système
2. Impact minimal sur les appels système
3. Traitement de dégradation automatique en cas d'anomalie
4. Ne modifie pas les fichiers système centraux

### Q : Les données privées peuvent-elles fuiter ?
**R** : Protection de la vie privée :
- **Aucune collecte de données** - Ne collecte aucune information personnelle
- **Aucune communication réseau** - Ne communique pas avec des serveurs externes
- **Traitement local** - Toutes les analyses sont effectuées localement sur l'appareil
- **Contrôle utilisateur** - L'utilisateur contrôle complètement toutes les données

## Dépannage

### Q : Que faire en cas d'impossibilité complète d'utilisation ?
**R** : Étapes de réinitialisation :
1. Désactivez le module dans Xposed
2. Redémarrez l'appareil
3. Effacez les données de l'application NoWakeLock
4. Réactivez le module et configurez

### Q : Certaines applications plantent fréquemment ?
**R** : Traitement d'urgence :
1. Désactivez immédiatement toutes les règles de cette application
2. Vérifiez les informations d'erreur dans les journaux système
3. Restaurez progressivement les règles et observez
4. Ajoutez cette application à la liste blanche si nécessaire

### Q : Comment collecter des informations de débogage ?
**R** : Collecte d'informations :
```bash
# Informations de l'appareil
adb shell getprop ro.build.version.release
adb shell getprop ro.product.model

# Journaux de l'application
adb logcat | grep -i nowakelock

# Journaux Xposed
adb logcat | grep -i xposed
```

## Concepts de base

### Q : Que sont les WakeLock/Alarm/Service ? Comment les configurer pour obtenir les meilleurs effets ?

**R** : Explication des concepts centraux :

**WakeLock (verrou de réveil)** :
- Mécanisme empêchant l'appareil d'entrer en mode veille
- Types : PARTIAL (CPU en fonctionnement), SCREEN (écran allumé), etc.
- Documentation officielle : [Guide Android WakeLock](https://developer.android.com/training/scheduling/wakelock)

**Alarm (tâche programmée)** :
- Minuteur système déclenchant des tâches à des moments spécifiques
- Types : RTC, ELAPSED_REALTIME, etc.
- Documentation officielle : [Guide Android Alarms](https://developer.android.com/training/scheduling/alarms)

**Service (service)** :
- Composant d'application s'exécutant en arrière-plan
- Types : service de premier plan, service d'arrière-plan, service lié
- Documentation officielle : [Guide Android Services](https://developer.android.com/guide/components/services)

**Ressources d'apprentissage recommandées** :
- Guide XDA : ["Guide complet WakeLock pour débutants"](https://forum.xda-developers.com)
- [Amplify](https://forum.xda-developers.com/t/mod-xposed-amplify-battery-extender-control-alarms-services-and-wakelocks.2853874/) - Fournit une liste de référence d'informations WakeLock/Alarm/Service
- [WakeBlock](https://github.com/MrLast98/WakeBlock) - Fournit également des informations WakeLock

**Note** : Malheureusement, il n'existe pas de référence universelle parfaite, les différences entre appareils sont énormes. Il faut ajuster selon l'appareil et les applications spécifiques.

## Traitement des problèmes graves

### Q : Que faire si des opérations incorrectes empêchent l'appareil de démarrer ?

**R** : Étapes de récupération d'urgence :

**Situation 1 : Problèmes de démarrage causés par le module NoWakeLock** :

**Méthode 1 : Mode sécurisé par boutons physiques (recommandé)** :
```bash
1. Maintenez le bouton d'alimentation 10 secondes pour forcer le redémarrage
2. Dès que l'écran devient noir, appuyez immédiatement et de manière répétée sur n'importe quel bouton physique (volume ou alimentation)
3. Après avoir ressenti 2 courtes vibrations, continuez à appuyer rapidement 4 fois sur le même bouton
4. Après le 4ème appui, vous devriez ressentir une longue vibration, indiquant que Xposed est désactivé
5. Après un démarrage normal, désactivez le module NoWakeLock dans LSPosed
```

**Méthode 2 : Méthode du système de fichiers Recovery** :
```bash
1. Entrez en TWRP Recovery
2. Cliquez sur Advanced → File Manager
3. Naviguez vers /data/adb/lspd/
4. Supprimez le dossier config
5. Redémarrez vers le système
```

**Méthode 3 : Méthode spécifique à l'appareil (comme Pixel)** :
```bash
Au démarrage, après l'apparition du logo Google, appuyez frénétiquement sur le bouton volume bas
Jusqu'à ce que l'appareil vibre pour confirmer l'entrée en mode sécurisé
```

**Situation 2 : Interception erronée de composants système importants** :
```bash
1. Entrez en Recovery → Gestionnaire de fichiers
2. Naviguez vers /data/misc/xxx-xxx-xxx/prefs/com.js.nowakelock
   # xxx-xxx-xxx est une longue chaîne de caractères aléatoires, peut différer selon l'appareil
3. Supprimez l'intégralité du dossier
4. Redémarrez l'appareil
5. Après être entré dans le système, récupérez de l'erreur, si le problème spécifique n'est pas clair, effacez directement les données NoWakeLock
```

**En cas de cause incertaine** :
- Après être entré dans le système, effacez directement les données de l'application NoWakeLock
- Reconfigurez, évitez d'intercepter les composants système critiques

!!! danger "Avertissement important"
    Sauvegardez impérativement la configuration avant de modifier les composants système critiques. Il est recommandé de commencer par déboguer les applications tierces et d'ajuster progressivement les services système.

## Vie privée et sécurité

### Q : NoWakeLock collecte-t-il des données privées ?

**R** : Engagement de protection de la vie privée :
- **Complètement localisé** - Toutes les données sont stockées uniquement localement sur l'appareil
- **Zéro upload de données** - N'upload aucune donnée vers aucun serveur
- **Aucune collecte de vie privée** - Ne collecte ou stocke aucune information personnelle
- **Open source transparent** - Le code source est complètement public et auditable

**Fonctionnalités futures possibles** :
- Possible ajout d'une fonctionnalité optionnelle de synchronisation de configuration cloud
- L'utilisateur peut contrôler complètement s'il l'active
- Continuera à respecter les principes de protection de la vie privée

## Participation et contribution

### Q : Que faire en cas de besoin de nouvelles fonctionnalités ou de découverte de bugs ?

**R** : Moyens de participation :
- **GitHub Issues** - [Soumettre des problèmes ou des demandes de fonctionnalités](https://github.com/NoWakeLock/NoWakeLock/issues)
- **Description détaillée** - Fournir une description complète du problème et les étapes de reproduction
- **Retour actif** - Les développeurs s'efforceront de traiter tous les retours

### Q : Comment aider à mettre à jour les traductions ?

**R** : Contribution aux traductions :
- **Pull Request** - Soumettre directement des PR de traduction
- **Support multilingue** - Toutes contributions de traduction dans diverses langues sont bienvenues
- **Collaboration communautaire** - Possibilité de discuter des détails de traduction dans les groupes communautaires

## Obtenir de l'aide

### Q : Où signaler les problèmes ?
**R** : Canaux de support :
- **GitHub Issues** - Problèmes techniques détaillés et demandes de fonctionnalités
- **Groupe Telegram** - [@nowakelock](https://t.me/nowakelock) Questions rapides et discussions
- **Communauté Discord** - [NoWakelock](https://discord.gg/kewmG5AShQ) Échanges techniques approfondis

### Q : Comment fournir un rapport de problème efficace ?
**R** : Informations à inclure :
1. **Informations de l'appareil** - Marque, modèle, version Android
2. **Informations du framework** - Type et version du framework Xposed
3. **Description du problème** - Phénomènes spécifiques du problème
4. **Étapes de reproduction** - Comment reproduire le problème
5. **Informations de journal** - Journaux d'erreur pertinents
6. **Captures d'écran** - Captures d'écran de l'interface problématique

### Q : Dans combien de temps peut-on obtenir une réponse ?
**R** : Temps de réponse :
- **Groupes communautaires** - Généralement quelques heures
- **GitHub Issues** - 1-3 jours ouvrables
- **Problèmes urgents** - Traités en priorité

!!! tip "Conseils d'utilisation"
    En cas de problème, il est recommandé de consulter d'abord la documentation et les FAQ. Si le problème persiste, veuillez fournir des informations détaillées pour mieux aider à le résoudre.

!!! warning "Rappel important"
    Sauvegardez les données importantes avant utilisation. Une configuration incorrecte peut affecter le fonctionnement normal de l'appareil, veuillez procéder avec prudence.