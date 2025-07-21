# Aperçu du système

NoWakeLock adopte une architecture moderne de développement Android, combinant les capacités de Hook au niveau système du framework Xposed avec la conception d'interface utilisateur déclarative de Jetpack Compose.

## Architecture globale

### Diagramme d'architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Système Android                       │
├─────────────────────────────────────────────────────────┤
│  PowerManagerService │ AlarmManagerService │ ActiveServices │
│         ↓ Hook             ↓ Hook               ↓ Hook     │
├─────────────────────────────────────────────────────────┤
│                 Couche module Xposed                     │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        │
│  │WakelockHook │ │ AlarmHook   │ │ ServiceHook │        │
│  └─────────────┘ └─────────────┘ └─────────────┘        │
├─────────────────────────────────────────────────────────┤
│                   Couche application                     │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        │
│  │ Presentation│ │  Domain     │ │    Data     │        │
│  │   (UI)      │ │ (Business)  │ │ (Storage)   │        │
│  └─────────────┘ └─────────────┘ └─────────────┘        │
└─────────────────────────────────────────────────────────┘
```

## Pile technologique

### Technologies principales
- **Kotlin** 1.9.25 - Langage de programmation principal
- **Jetpack Compose** 2025.04.01 - Framework d'interface utilisateur déclarative
- **Room** 2.7.1 - Framework ORM de base de données
- **Coroutines** - Programmation asynchrone
- **Flow** - Flux de données réactives

### Intégration Xposed
- **Xposed API** 82 - Framework de Hook au niveau système
- **LSPosed** - Framework cible principal
- **EdXposed** - Support de compatibilité ascendante
- **Mécanisme de réflexion** - Adaptation d'API inter-versions

### Injection de dépendances
- **Koin** 4.0.4 - Framework DI léger
- **Configuration modulaire** - Injection groupée par fonctionnalité
- **Gestion ViewModel** - Liaison automatique du cycle de vie

## Stratification de l'architecture

### 1. Couche module Xposed
```kotlin
// Point d'entrée
class XposedModule : IXposedHookZygoteInit, IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        when (lpparam.packageName) {
            "android" -> {
                WakelockHook.hook(lpparam)
                AlarmHook.hook(lpparam)
                ServiceHook.hook(lpparam)
            }
        }
    }
}
```

**Responsabilités** :
- Interception Hook des services système
- Communication de données inter-processus
- Gestion de la compatibilité des versions

### 2. Couche de données (Data Layer)

#### Architecture double base de données
```kotlin
// Base de données métier principale
@Database(
    entities = [AppInfo::class, WakelockRule::class],
    version = 13
)
abstract class AppDatabase : RoomDatabase()

// Base de données d'enregistrement d'événements  
@Database(
    entities = [InfoEvent::class],
    version = 12
)
abstract class InfoDatabase : RoomDatabase()
```

**Responsabilités** :
- Gestion des informations d'application
- Enregistrement des données d'événements
- Stockage de configuration des règles
- Paramètres de préférences utilisateur

#### Modèle Repository
```kotlin
interface DARepository {
    fun getApps(userId: Int): Flow<List<AppDas>>
    suspend fun updateRule(rule: Rule)
}

class DARepositoryImpl(
    private val appInfoDao: AppInfoDao,
    private val xProvider: XProvider
) : DARepository {
    // Implémentation de la logique d'accès aux données
}
```

**Caractéristiques** :
- Interface d'accès aux données unifiée
- Base de données locale + source de données Xposed
- Flux de données réactives

### 3. Couche métier (Domain Layer)

#### Architecture ViewModel
```kotlin
class DAsViewModel(
    private val repository: DARepository,
    private val userRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DAsUiState())
    val uiState: StateFlow<DAsUiState> = _uiState.asStateFlow()
    
    fun loadData() {
        viewModelScope.launch {
            repository.getApps()
                .distinctUntilChanged()
                .collect { apps ->
                    _uiState.update { it.copy(apps = apps) }
                }
        }
    }
}
```

**Caractéristiques** :
- Modèle d'architecture MVVM
- Gestion d'état StateFlow
- Liaison de données réactives

### 4. Couche présentation (Presentation Layer)

#### Architecture d'interface utilisateur Compose
```kotlin
@Composable
fun NoWakeLockApp() {
    val navController = rememberNavController()
    
    NoWakeLockTheme {
        NavHost(navController = navController) {
            composable<Apps> { AppsScreen() }
            composable<Wakelocks> { WakelocksScreen() }
            composable<Services> { ServicesScreen() }
        }
    }
}
```

**Caractéristiques** :
- Composants d'interface utilisateur déclaratifs
- Navigation type-safe
- Material Design 3

## Fonctionnalités principales

### 1. Support multi-utilisateurs
```kotlin
class UserManager {
    fun getCurrentUsers(): List<User> {
        return UserManagerService.getUsers()
    }
    
    fun switchUser(userId: Int) {
        // Changer de contexte utilisateur
    }
}
```

### 2. Compatibilité des versions
```kotlin
object VersionCompat {
    fun getParameterIndices(method: Method): IntArray {
        return when (Build.VERSION.SDK_INT) {
            in 24..28 -> intArrayOf(0, 1, 2)
            in 29..30 -> intArrayOf(1, 2, 3)
            else -> intArrayOf(2, 3, 4)
        }
    }
}
```

### 3. Optimisation des performances
- **Cache de position des paramètres** - Réduction de la surcharge de réflexion
- **Flow distinctUntilChanged** - Éviter les mises à jour répétitives
- **Index de base de données** - Optimisation des performances de requête
- **Chargement paresseux** - Chargement de données à la demande

## Conception du flux de données

### Processus de traitement d'événements
```
Appel système → Interception Hook → Correspondance de règles → Exécution d'action → Enregistrement d'événement → Mise à jour UI
    ↓         ↓         ↓         ↓         ↓        ↓
PowerManager → WakelockHook → RuleEngine → Block/Allow → InfoEvent → Mise à jour Flow
```

### Gestion d'état
```kotlin
data class DAsUiState(
    val apps: List<AppDas> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filterOption: FilterOption = FilterOption.ALL,
    val sortOption: SortOption = SortOption.NAME
)
```

## Mécanisme de communication

### 1. Communication Xposed ↔ Application
- **XProvider** - Communication inter-processus ContentProvider
- **SharedPreferences** - Partage de données de configuration
- **Système de fichiers** - Échange de données temporaires

### 2. Communication inter-composants
- **Modèle Repository** - Abstraction de la couche de données
- **StateFlow** - Partage d'état réactif
- **Navigation** - Transmission de paramètres entre pages

## Points d'extension

### 1. Ajout de nouveaux types de Hook
```kotlin
// Créer une nouvelle classe Hook
object NewFeatureHook {
    fun hook(lpparam: LoadPackageParam) {
        // Implémentation Hook
    }
}

// Enregistrer dans XposedModule
NewFeatureHook.hook(lpparam)
```

### 2. Ajout de nouvelles pages UI
```kotlin
// Ajouter une nouvelle route
@Serializable
data class NewFeature(val param: String = "")

// Ajouter navigation et page
composable<NewFeature> { NewFeatureScreen() }
```

### 3. Intégration de nouvelles sources de données
```kotlin
// Étendre l'interface Repository
interface ExtendedRepository : DARepository {
    fun getNewData(): Flow<List<NewData>>
}
```

## Principes de conception

### 1. Responsabilité unique
Chaque classe et module a des limites de responsabilité claires, facilitant la maintenance et les tests.

### 2. Inversion de dépendances
Les modules de haut niveau ne dépendent pas des modules de bas niveau, tous dépendent d'interfaces abstraites.

### 3. Principe ouvert-fermé
Ouvert à l'extension, fermé à la modification, facilitant l'ajout de nouvelles fonctionnalités.

### 4. Conception réactive
Utilise Flow et StateFlow pour implémenter un flux de données réactif, assurant la synchronisation entre l'interface utilisateur et les données.

!!! info "Avantages de l'architecture"
    Cette conception d'architecture en couches permet à NoWakeLock de gérer à la fois des opérations complexes au niveau système tout en maintenant une bonne organisation du code et une maintenabilité.

!!! warning "Points d'attention"
    Les modifications de Hook au niveau système nécessitent une gestion prudente de la compatibilité des versions, il est recommandé de tester suffisamment sur plusieurs versions Android.