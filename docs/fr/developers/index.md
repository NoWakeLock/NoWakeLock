# Documentation développeur

Comprendre en profondeur l'implémentation technique de NoWakeLock pour contribuer au code du projet.

## Navigation de la documentation

### 🏗️ Conception d'architecture
- [Vue d'ensemble du système](architecture/overview.md) - Architecture globale et philosophie de conception
- [Xposed Hooks](architecture/xposed-hooks.md) - Implémentation du système Hook
- [Conception du flux de données](architecture/data-flow.md) - Circulation des données dans le système
- [Conception de base de données](architecture/database.md) - Architecture de stockage des données

### ⚙️ Détails d'implémentation
- [Détail des Hooks](implementation/hook-details.md) - Implémentation Hook spécifique
- [Système de compteurs](implementation/counter-system.md) - Mécanisme de calcul statistique
- [Communication inter-processus](implementation/ipc.md) - Communication entre module et application

### 📚 Référence API
- [ContentProvider](api/content-provider.md) - Interface d'accès aux données
- [API interne](api/internal-api.md) - Interface interne du module

### 🤝 Guide de contribution
- [Environnement de développement](contributing/setup.md) - Configuration de l'environnement de développement
- [Normes de codage](contributing/guidelines.md) - Style de code et conventions
- [Guide de test](contributing/testing.md) - Framework et méthodes de test

## Démarrage rapide

### Exigences d'environnement
- **Android Studio** - Arctic Fox ou version plus récente
- **JDK** - 17 ou version plus récente
- **Android SDK** - API 24-35
- **Git** - Outil de contrôle de version

### Obtenir le code source
```bash
git clone https://github.com/NoWakeLock/NoWakeLock.git
cd NoWakeLock
git checkout dev
```

### Construire le projet
```bash
# Installer les dépendances
./gradlew clean

# Construire la version Debug
./gradlew assembleDebug

# Exécuter les tests
./gradlew test
```

## Stack technologique

### Technologies centrales
- **Kotlin** - Langage de programmation principal
- **Jetpack Compose** - Framework UI moderne
- **Room** - Couche d'abstraction de base de données
- **Coroutines** - Programmation asynchrone
- **Flow** - Flux de données réactives

### Intégration Xposed
- **LSPosed API** - Framework Hook principal
- **Compatibilité EdXposed** - Support de compatibilité rétroactive
- **Mécanisme de réflexion** - Adaptation API cross-version

### Injection de dépendances
- **Koin** - Framework DI léger
- **ViewModel** - Gestion d'état UI
- **Modèle Repository** - Abstraction d'accès aux données

## Modules centraux

### Module XposedHook
```
xposedhook/
├── XposedModule.kt      # Point d'entrée du module
├── hook/               # Implémentation Hook
│   ├── WakelockHook.kt
│   ├── AlarmHook.kt
│   └── ServiceHook.kt
└── model/              # Modèles de données
    └── XpNSP.kt
```

### Couche de données
```
data/
├── db/                 # Base de données
│   ├── AppDatabase.kt
│   ├── InfoDatabase.kt
│   └── entity/
├── repository/         # Dépôt de données
└── counter/           # Système de compteurs
```

### Couche UI
```
ui/
├── screens/           # Composants de page
├── components/        # Composants génériques
├── theme/            # Styles de thème
└── navigation/       # Logique de navigation
```

## Processus de développement

### Développement de fonctionnalités
1. **Analyse des besoins** - Clarifier les exigences fonctionnelles et la solution technique
2. **Création de branche** - Créer une branche de fonctionnalité depuis la branche `dev`
3. **Implémentation du code** - Implémenter les fonctionnalités en suivant les normes de codage
4. **Tests unitaires** - Écrire et exécuter les cas de test
5. **Tests d'intégration** - Tester sur des appareils réels
6. **Revue de code** - Soumettre une Pull Request
7. **Fusion et publication** - Fusionner dans la branche `dev`

### Correction de bugs
1. **Reproduction du problème** - Confirmer les étapes de reproduction du bug
2. **Analyse de cause racine** - Analyser la cause fondamentale du problème
3. **Implémentation de la correction** - Écrire le code de correction minimal
4. **Tests de régression** - S'assurer que la correction n'affecte pas les autres fonctionnalités
5. **Déploiement** - Choisir le moment de publication selon la gravité

## Architecture du code

### Architecture MVVM
```kotlin
// ViewModel
class WakelocksViewModel(
    private val repository: WakelockRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WakelocksUiState())
    val uiState: StateFlow<WakelocksUiState> = _uiState.asStateFlow()
    
    fun loadWakelocks() {
        viewModelScope.launch {
            repository.getWakelocks()
                .collect { wakelocks ->
                    _uiState.update { it.copy(wakelocks = wakelocks) }
                }
        }
    }
}

// Compose UI
@Composable
fun WakelocksScreen(viewModel: WakelocksViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn {
        items(uiState.wakelocks) { wakelock ->
            WakelockItem(wakelock = wakelock)
        }
    }
}
```

### Modèle Repository
```kotlin
interface WakelockRepository {
    fun getWakelocks(): Flow<List<WakelockInfo>>
    suspend fun updateWakelock(wakelock: WakelockInfo)
}

class WakelockRepositoryImpl(
    private val dao: WakelockDao,
    private val xpProvider: XProvider
) : WakelockRepository {
    override fun getWakelocks(): Flow<List<WakelockInfo>> {
        return dao.getAllWakelocks()
            .map { entities -> entities.map { it.toDomain() } }
    }
}
```

### Modèle d'implémentation Hook
```kotlin
object WakelockHook {
    fun hookWakeLocks(lpparam: LoadPackageParam) {
        findAndHookMethod(
            PowerManagerService::class.java,
            "acquireWakeLockInternal",
            *parameterTypes
        ) { param ->
            val result = processWakeLockAcquire(param.args)
            if (result.shouldBlock) {
                param.result = null // Intercepter l'appel
                return@findAndHookMethod
            }
            // Continuer l'appel original
        }
    }
}
```

## Stratégie de test

### Tests unitaires
- **Tests ViewModel** - Tests de logique métier
- **Tests Repository** - Tests de couche de données
- **Tests de classes utilitaires** - Tests de fonctions auxiliaires

### Tests d'intégration
- **Tests de base de données** - Opérations de base de données Room
- **Tests Hook** - Fonctionnalités Xposed Hook
- **Tests UI** - Tests d'interface Compose

### Tests d'appareil
- **Tests de compatibilité** - Appareils Android multi-versions
- **Tests de performance** - Consommation mémoire, CPU, batterie
- **Tests de stabilité** - Tests d'exécution prolongée

## Processus de publication

### Gestion des versions
- **Version majeure** - Mises à jour de fonctionnalités majeures
- **Version mineure** - Ajout de nouvelles fonctionnalités
- **Version de révision** - Corrections de bugs

### Stratégie de branches
- **master** - Version stable
- **dev** - Version de développement
- **feature/*** - Branches de fonctionnalités
- **hotfix/*** - Corrections d'urgence

### Processus CI/CD
```yaml
# Workflow GitHub Actions
name: Build and Test
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew test
      - name: Build APK
        run: ./gradlew assembleDebug
```

## Astuces de débogage

### Débogage Xposed
```kotlin
// Utiliser XposedBridge.log pour la sortie d'informations de débogage
XposedBridge.log("NoWakeLock: Hook executed")

// Utiliser la compilation conditionnelle pour contrôler le code de débogage
if (BuildConfig.DEBUG) {
    XposedBridge.log("Debug info: ${param.args}")
}
```

### Analyse des journaux
```bash
# Filtrer les journaux NoWakeLock
adb logcat | grep -i nowakelock

# Surveiller les métriques de performance
adb shell dumpsys meminfo com.js.nowakelock
adb shell top | grep nowakelock
```

## Contribution communautaire

### Moyens de participation
- **Contribution de code** - Développement de fonctionnalités et correction de bugs
- **Contribution de documentation** - Amélioration de la documentation et des tutoriels
- **Contribution de tests** - Tests de compatibilité d'appareils
- **Contribution de traduction** - Support multilingue

### Canaux de communication
- **GitHub Issues** - Rapports de problèmes et demandes de fonctionnalités
- **GitHub Discussions** - Discussions techniques et échange d'idées
- **Telegram** - Communication en temps réel et support rapide
- **Discord** - Discussions techniques approfondies

### Revue de code
Toutes les contributions doivent passer par une revue de code :
- Vérification de qualité du code
- Évaluation de sécurité
- Analyse d'impact sur les performances
- Vérification de compatibilité

!!! info "Accord développeur"
    Contribuer au code signifie accepter la licence open source du projet (GPL v3.0) et l'accord de contributeur.

!!! tip "Accueil aux débutants"
    Le projet accueille les contributeurs débutants, nous avons des tâches simples marquées `good-first-issue` pour les débutants.