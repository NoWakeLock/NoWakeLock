# Dépannage

Guide systématique de diagnostic et de résolution des problèmes.

## Processus de diagnostic

### Première étape : Vérifications de base

#### Vérification de l'état du module
1. Ouvrez NoWakeLock → "Vérification du module"
2. Confirmez que tous les éléments affichent ✅
3. S'il y a des éléments ❌, traitez selon les instructions

【Capture d'écran nécessaire : exemple d'échec de vérification du module】

#### Vérification du framework Xposed
```bash
# Vérifier l'état de LSPosed
adb shell am start -n org.lsposed.manager/.ui.activity.MainActivity

# Voir la liste des modules
adb shell pm list packages | grep nowakelock
```

#### Vérification des permissions de base
- Permissions de stockage
- Permission de requête de toutes les applications (Android 11+)
- Permissions de service d'accessibilité (si nécessaire)

### Deuxième étape : Tests fonctionnels

#### Test WakeLock
1. Définissez une règle de limitation WakeLock simple
2. Ouvrez l'application correspondante pour déclencher le WakeLock
3. Vérifiez si la page de statistiques a des enregistrements d'interception

#### Test d'efficacité des règles
1. Créez une règle de test : intercepter un WakeLock spécifique
2. Observez les changements de comportement de l'application cible
3. Consultez le journal des événements pour confirmer l'exécution des règles

## Classification des problèmes courants

### Problèmes d'installation

#### Le module ne peut pas se charger
**Symptômes** : La vérification du module affiche "Module non chargé"

**Étapes de diagnostic** :
1. Confirmez que le framework Xposed fonctionne normalement
2. Vérifiez si le module est coché dans le gestionnaire
3. Vérifiez si la signature de l'application est correcte

**Solutions** :
```bash
# Réinstaller le module
adb uninstall com.js.nowakelock
adb install nowakelock.apk

# Effacer le cache du framework
# Dans LSPosed : "Paramètres" → "Effacer le cache"
```

#### Dysfonctionnement de la fonction Hook
**Symptômes** : Module chargé mais Hook ne fonctionne pas

**Causes possibles** :
- Version système incompatible
- Configuration de portée incorrecte
- Restrictions de politique SELinux

**Solutions** :
1. Confirmez que la portée inclut `android`
2. Vérifiez l'état SELinux :
   ```bash
   adb shell getenforce
   # Si c'est Enforcing, cela peut affecter la fonction Hook
   ```
3. Consultez les journaux Xposed :
   ```bash
   adb logcat | grep -E "(Xposed|nowakelock)"
   ```

### Problèmes fonctionnels

#### Les règles ne prennent pas effet
**Symptômes** : Aucun effet d'interception après définition des règles

**Liste de vérification** :
- [ ] Les règles sont-elles activées
- [ ] Les conditions de correspondance sont-elles correctes
- [ ] L'application cible a-t-elle redémarré
- [ ] Y a-t-il des règles conflictuelles

**Méthodes de débogage** :
1. Utilisez une correspondance exacte simple pour tester
2. Vérifiez la priorité des règles
3. Consultez les journaux de correspondance

#### Fonctionnalités d'application anormales
**Symptômes** : L'application ne peut pas fonctionner normalement après définition des règles

**Traitement immédiat** :
1. Désactivez les règles associées
2. Redémarrez l'application problématique
3. Restaurez progressivement les règles

**Solution fondamentale** :
1. Analysez les composants critiques dont dépend l'application
2. Ajustez la portée ou les paramètres des règles
3. Utilisez "Limiter" au lieu d'"Intercepter"

#### Données statistiques anormales
**Symptômes** : Les données statistiques affichent des anomalies ou ne se mettent pas à jour

**Éléments à vérifier** :
1. État de la base de données
   ```bash
   adb shell ls -la /data/data/com.js.nowakelock/databases/
   ```
2. Espace de stockage
   ```bash
   adb shell df /data
   ```
3. Permissions de l'application

**Méthodes de réparation** :
```bash
# Effacer la base de données (attention : perte des données historiques)
adb shell pm clear com.js.nowakelock
```

### Problèmes de performance

#### Ralentissement du système
**Symptômes** : Le système répond plus lentement après installation de NoWakeLock

**Analyse de performance** :
```bash
# Taux d'utilisation CPU
adb shell top | grep nowakelock

# Utilisation mémoire
adb shell dumpsys meminfo com.js.nowakelock
```

**Solutions d'optimisation** :
1. Réduisez le nombre de règles
2. Simplifiez les expressions régulières
3. Ajustez la fréquence des statistiques

#### Augmentation de la consommation d'énergie
**Symptômes** : Le module lui-même consomme de l'énergie

**Méthodes de diagnostic** :
1. Vérifiez l'activité en arrière-plan
   ```bash
   adb shell dumpsys battery
   ```
2. Analysez l'utilisation des WakeLocks
   ```bash
   adb shell dumpsys power | grep nowakelock
   ```

**Solutions** :
- Vérifiez s'il y a des tâches cycliques anormales
- Optimisez la fréquence des requêtes de base de données
- Confirmez l'absence de fuites mémoire

### Problèmes de compatibilité

#### Conflits avec des applications spécifiques
**Symptômes** : Certaines applications entrent en conflit avec NoWakeLock

**Méthodes d'identification** :
1. Analyse des journaux système
2. Rapports de plantage d'applications
3. Journaux ANR (Application Not Responding)

**Stratégies de traitement** :
```yaml
Solution temporaire:
  - Ajouter l'application à la liste blanche
  - Désactiver les règles associées

Solution à long terme:
  - Analyser les causes du conflit
  - Ajuster la stratégie Hook
  - Mettre à jour le code de compatibilité
```

#### Compatibilité des versions système
**Symptômes** : Fonctionnalités anormales sur les nouvelles versions Android

**Vérification d'adaptation** :
1. Analyse des changements d'API
2. Changements du modèle de permissions
3. Mises à jour des politiques de sécurité

**Solutions de dégradation** :
- Désactiver les fonctionnalités incompatibles
- Utiliser des implémentations alternatives
- Attendre les mises à jour de version

## Analyse des journaux

### Collecte des journaux

#### Journaux système
```bash
# Journal complet
adb logcat -v time > full_log.txt

# Relatif à NoWakeLock
adb logcat | grep -i nowakelock > nowakelock_log.txt

# Relatif à Xposed
adb logcat | grep -i xposed > xposed_log.txt
```

#### Journaux d'application
```bash
# Journal de processus spécifique
adb logcat --pid=$(adb shell pidof com.js.nowakelock)

# Journal de plantage
adb logcat | grep -E "(FATAL|AndroidRuntime)"
```

### Analyse des journaux

#### Identification des erreurs critiques
```
E/Xposed: Hook failed
E/NoWakeLock: Database error
W/ActivityManager: Unable to start service
```

#### Identification des problèmes de performance
```
W/Choreographer: Skipped frames
I/Timeline: Timeline: Activity_idle
W/InputDispatcher: Application is not responding
```

### Nettoyage des journaux
```bash
# Effacer les journaux
adb logcat -c

# Définir le niveau de journal
adb shell setprop log.tag.NoWakeLock VERBOSE
```

## Récupération de données

### Sauvegarde de configuration
```bash
# Sauvegarder la configuration
adb backup -f backup.ab com.js.nowakelock

# Extraire la base de données
adb shell cp /data/data/com.js.nowakelock/databases/app_database /sdcard/
adb pull /sdcard/app_database ./
```

### Restauration de configuration
```bash
# Restaurer la sauvegarde
adb restore backup.ab

# Restaurer manuellement la base de données
adb push ./app_database /sdcard/
adb shell cp /sdcard/app_database /data/data/com.js.nowakelock/databases/
```

### Options de réinitialisation

#### Réinitialisation douce (conserver la configuration)
1. Paramètres de l'application → Effacer le cache
2. Redémarrer l'application

#### Réinitialisation dure (effacer toutes les données)
```bash
adb shell pm clear com.js.nowakelock
```

#### Réinitialisation complète (réinstallation)
```bash
adb uninstall com.js.nowakelock
# Réinstaller et configurer
```

## Débogage avancé

### Débogage Hook

#### Activer les journaux détaillés
Activez le "Mode débogage" dans les paramètres de l'application, cela affichera des informations Hook détaillées.

#### Outils de test Hook
```kotlin
// Tester un point Hook spécifique
fun testWakeLockHook() {
    // Déclencher manuellement l'acquisition de WakeLock
    // Observer si Hook est appelé
}
```

### Analyse de performance

#### Analyse CPU
```bash
# Surveillance de performance
adb shell am start -n com.android.shell/.BugreportStorageProvider

# Analyse des threads
adb shell ps -T | grep nowakelock
```

#### Analyse mémoire
```bash
# Détails mémoire
adb shell dumpsys meminfo com.js.nowakelock

# Détection de fuites mémoire
adb shell am dumpheap com.js.nowakelock /sdcard/heap.hprof
```

### Débogage de base de données

#### Vérification de base de données
```sql
-- Connexion à la base de données
sqlite3 app_database

-- Vérifier la structure des tables
.schema

-- Voir les données
SELECT * FROM app_info LIMIT 10;
SELECT * FROM wakelock_info LIMIT 10;
```

#### Vérification de cohérence des données
```sql
-- Vérifier les enregistrements orphelins
SELECT * FROM events WHERE app_id NOT IN (SELECT id FROM apps);

-- Validation des données statistiques
SELECT package_name, COUNT(*) FROM events GROUP BY package_name;
```

## Mesures préventives

### Maintenance régulière

#### Vérification hebdomadaire
- Vérification de l'état du module
- Évaluation de l'efficacité des règles
- Surveillance des indicateurs de performance

#### Maintenance mensuelle
- Nettoyage des données historiques
- Mise à jour de la configuration des règles
- Sauvegarde des paramètres importants

### Paramètres de surveillance

#### Surveillance de performance
Définir des seuils de performance, alerte automatique en cas de dépassement :
- Taux d'utilisation CPU > 5%
- Utilisation mémoire > 100MB
- Taille de base de données > 500MB

#### Surveillance fonctionnelle
Tester régulièrement les fonctions critiques :
- Précision de correspondance des règles
- Intégrité des données statistiques
- Normalité des fonctions d'application

## Support professionnel

### Support communautaire
- **Telegram** : [@nowakelock](https://t.me/nowakelock)
- **Discord** : [Communauté NoWakelock](https://discord.gg/kewmG5AShQ)
- **GitHub** : [Issues](https://github.com/NoWakeLock/NoWakeLock/issues)

### Modèle de rapport de problème
```markdown
## Informations d'environnement
- Appareil : [Marque Modèle]
- Version Android : [Numéro de version]
- Framework Xposed : [Version LSPosed/EdXposed]
- Version NoWakeLock : [Numéro de version]

## Description du problème
[Description détaillée des phénomènes du problème]

## Étapes de reproduction
1. [Étape une]
2. [Étape deux]
3. [Problème apparaît]

## Résultat attendu
[Comportement normal espéré]

## Résultat réel
[Comportement anormal réellement survenu]

## Journaux associés
```log
[Coller les journaux pertinents]
```

## Autres informations
[Toute autre information pertinente]
```

!!! warning "Sécurité des données"
    Lors du dépannage, sauvegardez impérativement les configurations importantes. Certaines opérations peuvent entraîner une perte de données.

!!! tip "Conseils de débogage"
    Pour les problèmes complexes, il est recommandé de procéder étape par étape, en commençant par la configuration la plus simple et en augmentant progressivement la complexité, pour faciliter la localisation de la cause du problème.