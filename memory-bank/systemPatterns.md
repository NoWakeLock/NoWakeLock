# σ₂: System Patterns
*v1.3 | Created: 2025-04-09 | Updated: 2025-04-13*
*Π: INITIALIZING | Ω: REVIEW*

## 🏛️ Architecture Overview
NoWakeLock 遵循清晰的架构方法，使用 MVVM（Model-View-ViewModel）模式和 Jetpack Compose 构建 UI 层。应用由两个主要组件组成：与 Android 系统交互的 Xposed 模块钩子，以及用于配置和监控的用户界面应用。UI 组件采用纯内容组件设计模式，避免嵌套 Scaffold 结构，保持组件职责单一和清晰，实现了更一致的用户体验。

```mermaid
flowchart TD
    UI[UI - Compose Screens] --> ViewModels[ViewModels]
    ViewModels --> Repos[Repositories]
    Repos --> DB[(Room Database)]
    Repos --> XH[Xposed Hooks]
    XH --> AS[Android System]
    
    MainScaffold[Global Scaffold] --> TopBar[Global TopBar]
    MainScaffold --> Content[Content Area]
    Content --> ListItems[List Items]
    Content --> DetailScreen[Pure Content Detail]
    ListItems -->|Navigation| DetailScreen
```

## 🧩 Key Components
- [K₁] UI Layer: Material 3 Compose 屏幕用于用户交互
- [K₂] ViewModel Layer: 管理 UI 状态和业务逻辑
- [K₃] Repository Layer: 数据访问和操作
- [K₄] Database Layer: Room 持久化存储
- [K₅] Xposed Hooks: 系统级拦截和控制
- [K₆] DI Framework: Koin 依赖注入
- [K₇] Navigation: Jetpack Navigation Compose 控制应用导航
- [K₈] Pure Content Pattern: 纯内容组件设计模式

## 🧪 Design Patterns
- [P₁] MVVM: 分离 UI、业务逻辑和数据
- [P₂] Repository: 数据源抽象
- [P₃] Dependency Injection: Koin 构造函数注入
- [P₄] Observer Pattern: LiveData 和 StateFlow 用于 UI 更新
- [P₅] Factory Pattern: 用于创建数据对象
- [P₆] Navigation Pattern: 基于路由的导航模式
- [P₇] Pure Content Pattern: 纯内容组件设计，避免嵌套 Scaffold
- [P₈] Global TopBar Pattern: 统一导航栏的标题和返回控制

## 🔄 Data Flow
应用使用单向数据流模式，UI 事件触发 ViewModel 操作，ViewModel 通过 StateFlow 更新 UI 状态。导航事件和 UI 结构变化采用全局状态管理。
```
flowchart LR
    User[User] --> UI[Compose UI]
    UI --> Events[UI Events]
    Events --> ViewModel[ViewModel]
    Events --> NavEvents[Navigation Events]
    NavEvents --> NavController[Navigation Controller]
    NavController --> TopBarState[TopBar State]
    TopBarState --> TopBar[Global TopBar]
    ViewModel --> Repos[Repositories]
    Repos --> DB[(Room Database)]
    Repos --> XP[Xposed Module]
    ViewModel --> State[UI State]
    State --> UI
    NavController --> ContentScreen[Content Screen]
```

## 🔍 Technical Decisions
- [D₁] Jetpack Compose: 现代 UI 工具包，提供更好的用户体验
- [D₂] Material 3: 来自 Google 的最新设计系统
- [D₃] Koin DI: Dagger/Hilt 的轻量级替代方案
- [D₄] Room DB: 类型安全的数据库访问，支持 SQL
- [D₅] Kotlin Coroutines: 用于异步操作和并发
- [D₆] Navigation Compose: 用于应用内导航的现代 API
- [D₇] Pure Content Pattern: 避免嵌套 Scaffold，保持组件职责单一

## 🔗 Component Relationships
应用按功能区域（闹钟、唤醒锁、服务、应用）组织成逻辑模块。每个模块包含自己的屏幕、ViewModels 和 repositories，共享一个通用核心架构。详情页采用纯内容组件设计，只负责内容展示，而导航和 UI 结构（如 TopBar）由全局组件处理，确保整个应用的一致体验。

---
σ₂ captures system architecture and design patterns