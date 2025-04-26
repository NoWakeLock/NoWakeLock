# σ₆: Protection Registry
*v1.0 | Created: 2025-04-25 | Updated: 2025-04-25*
*Π: 🏗️DEVELOPMENT | Ω: 🔍R*

## 🛡️ Protected Regions

### Critical Infrastructure
- **[CRITICAL]** `app/src/main/java/com/js/nowakelock/KoinDSL.kt` - 依赖注入配置
  - 保护理由: 核心依赖注入模块，影响整个应用的组件实例化
  - 最后修改: 2025-04-25 (添加SavedStateHandle注入)

### 导航系统
- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/navigation/NavRoutes.kt` - 路由定义
  - 保护理由: 定义应用导航结构，需谨慎修改
  - 最后修改: 2025-04-25 (添加类型路由类)

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/navigation/NavGraph.kt` - 导航图
  - 保护理由: 核心导航实现，影响所有页面跳转
  - 最后修改: 2025-04-25 (支持类型导航)

### UI组件
- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/components/TopAppBars.kt` - 顶部应用栏
  - 保护理由: 全局UI组件，出现在所有页面
  - 当前问题: 路由检测逻辑需更新以支持类型导航
  - 最后修改: 2025-04-25 (初步添加路由包含检测)

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/components/BottomNavBar.kt` - 底部导航栏
  - 保护理由: 全局UI组件，负责主要页面跳转
  - 最后修改: 2025-04-25 (添加混合导航支持)

### 模型和参数定义
- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/navigation/params/AppsScreenParams.kt` - Apps参数常量
  - 保护理由: 定义与Apps相关的所有参数名称，确保类型安全
  - 最后修改: 2025-04-25 (新创建)

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/navigation/params/DAsScreenParams.kt` - DAs参数常量
  - 保护理由: 定义与DAs相关的所有参数名称，确保类型安全
  - 最后修改: 2025-04-25 (新创建)

### 视图模型
- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/screens/apps/AppsViewModel.kt` - Apps视图模型
  - 保护理由: 包含关键业务逻辑，处理用户筛选、排序和应用选择
  - 最后修改: 2025-04-25 (改用SavedStateHandle)

- **[GUARDED]** `app/src/main/java/com/js/nowakelock/ui/screens/das/DAsViewModel.kt` - DAs视图模型
  - 保护理由: 处理唤醒锁、闹钟和服务的数据管理
  - 最后修改: 2025-04-25 (改用SavedStateHandle，添加应用筛选)

## 📜 Protection History
- 2025-04-25 ⟶ 添加新创建的参数常量类到保护列表
- 2025-04-25 ⟶ 更新导航相关组件的保护状态
- 2025-04-25 ⟶ 将TopAppBars.kt标记为需要进一步调整的组件

## ✅ Approvals
*尚无修改批准记录*

## ⚠️ Permission Violations
*尚无权限违规记录*