# 新旧 Xposed 兼容：方案可行性与实现完成度审计

日期：2026-09-06。目标：保留当前主线用户的旧框架体验，同一 APK 在指定新稳定框架使用 Modern 机制，不承诺所有过渡 API。本文核对实际工作区、历史产物与一手框架源码；不将“源码已写”“测试通过”“设备可用”混为一谈。

## 判断

**已有大量可复用实现，但当前方案并非只差设备验证。** 两套 hook 及配置适配的总体方向仍有依据；“保留两套入口文件就能让旧框架自动走原路径”这一前提不成立于 LSPosed1.9.2。即使增加构造桥，该框架也不会自动恢复 New XSharedPreferences 环境。需要先修正兼容边界和补齐适配，再做设备验收。

这不等于单 APK 已被证伪。应区分：当前 APK 不满足指定旧框架；增加生命周期及配置适配后的候选有源码依据；候选最终可用性尚需二进制/ART/设备验证。详见[旧环境深挖](Xposed_Legacy_Environment_Feasibility_2026-09-06.zh-CN.md)和[入口调查](Xposed_Entry_Dispatch_Research_2026-09-05.zh-CN.md)。

旧侧暂以 LSPosed1.9.2 固定提交为审计基线，等待与用户实际正常工作的 fork/版本对齐；它不是替用户确定最终支持范围。新侧固定 Vector2.2/API102，其他 fork 应列出具体版本，不能只凭 API 数字宣称覆盖。[新稳定发布说明](https://github.com/JingMatrix/Vector/releases/tag/v2.2)

## 1. 当前工作实际上在哪里

- 分支 `feature/dual-xposed-config-backend`，HEAD `7d2d417`；相对本地 `dev` 只有一个已提交的调查文档提交。
- 大部分业务兼容实现处于未提交工作区，包括配置发布、Modern入口及三类hook、SettingsProvider路由、健康检查、测试和OpenSpec。不能把分支提交数当作工作量。
- [现有 tasks](../openspec/changes/add-dual-xposed-config-backend/tasks.md) 多数已勾选，设备smoke 5.7未勾选；但清单中的其他完成项也需要对照实际证据重审。
- [设计](../openspec/changes/add-dual-xposed-config-backend/design.md) 保留 legacy，同时明确删除API100构造。排除所有过渡版本与保住一个仍会选择旧Modern loader的指定稳定框架，是不同问题；当前设计没有化解这层冲突。

## 2. 已经实现的主体及证据边界

| 层次 | 当前实现 | 当前可以下的结论 |
|---|---|---|
| 同包入口 | assets旧入口 + META-INF新入口；Modern仅无参构造 | 打包结构存在；不证明旧框架会挑旧入口 |
| Modern系统hook | Wakelock/Alarm/Service各有独立实现，使用hook().intercept() | 已经迁移实际业务路径，不只是新入口空壳；语义等价未完整验证 |
| Modern上下文 | 从receiver/参数、mContext、嵌套对象取得Context | 已替换AndroidAppHelper依赖；首次调用时机及OEM字段仍需验证 |
| 规则发布 | Room→ConfigPublisher→legacy/remote；启动、绑定、单条/regex、恢复时发布 | 主路径存在；重连、并发快照与持久化边界仍有缺口 |
| hook读配置 | LegacyHookConfigReader和SharedPreferencesHookConfigReader；Modern不回退旧值 | 代际隔离已有实现；remote对象存在不等于系统进程已读到最新配置 |
| 事件统计 | XpRecord→ContentResolver→ModernSettingsProviderHook→XProvider/InfoDatabase | 路由代码存在；需要单事件端到端验证 |
| 健康检查 | 后端状态取代目录扫描，保留激活与事件数据检查 | 改进方向合理；没有逐进程配置确认与逐方法安装结果 |

源码：[Modern入口](../app/src/main/java/com/js/nowakelock/xposedhook/ModernXposedModule.kt)、[Modern实现目录](../app/src/main/java/com/js/nowakelock/xposedhook/modern/)、[发布器](../app/src/main/java/com/js/nowakelock/data/config/ConfigPublisher.kt)、[读配置](../app/src/main/java/com/js/nowakelock/xposedhook/model/HookConfigReader.kt)、[事件发送](../app/src/main/java/com/js/nowakelock/xposedhook/model/XpRecord.kt)、[事件接收](../app/src/main/java/com/js/nowakelock/data/provider/XProvider.kt)。

API102未废弃本地使用的hook chain、进程回调和Remote Preferences，因此不需要因API升级推倒Modern主体重写。102新增诊断和热重载不自动解决入口、规则或统计闭环；能力逐项依据见[稳定API调查](Xposed_Stable_API_Capabilities_2026-09-05.zh-CN.md)。

## 3. 设备之前已经能确定的缺口

### A. 指定旧框架的加载与配置环境尚未适配

LSPosed1.9.2先看非空Modern list，随后查找两参构造；当前入口仅无参。因此当前源码已经缺少它实际要求的构造，不能归类为“写完待测试”。若采用桥，还需旧system_server回调转发，当前代码没有。[框架与ABI证据](Xposed_Legacy_Environment_Feasibility_2026-09-06.zh-CN.md)

进一步，Modern分类对应loadedModules中的空值；New XSharedPreferences读端不进入共享路径识别，App端不安装允许MODE_WORLD_READABLE及共享目录的hook。现有SPTools恰好依赖这些条件。添加构造器不能恢复它们。[本地SPTools](../app/src/main/java/com/js/nowakelock/base/SPTools.kt)、[旧框架读写证据](Xposed_Legacy_Environment_Feasibility_2026-09-06.zh-CN.md)

### B. 当前“Modern存在”检测不是成功接管确认

旧入口只要Class.forName找到XposedInterface就return。它没有证明新入口已实例化、API版本满足、目标hook已安装或配置可读。这是代码事实；具体哪些fork让此分支触发并丢失旧hook仍需实验。应以真实加载路径决定适配器，不能以类存在代替成功接管。[旧入口](../app/src/main/java/com/js/nowakelock/xposedhook/XposedModule.kt)、[检测器](../app/src/main/java/com/js/nowakelock/xposedhook/ModernXposedRuntimeDetector.kt)

### C. 安装guard与文档承诺不一致

当前实际调用带entry/source的markSharedInstalled，legacy与modern生成不同key。既有测试明确要求两者各自首次返回true；因此它只防同一路重复，不提供设计所说的跨入口互斥。它还在安装完成前占位；失败后相同key重试会被挡住。[guard](../app/src/main/java/com/js/nowakelock/xposedhook/XposedHookInstallGuard.kt)、[测试](../app/src/test/java/com/js/nowakelock/xposedhook/XposedHookInstallGuardTest.kt)

这不证明当前Vector会加载两遍；框架通常先选一条路径。但若继续以“两个入口都来时有保护”作为保证，需补实现或修正文档，并明确安装失败后的重试语义。

### D. 成功日志和健康状态尚不足以做验收

Modern installer内部捕获各类hook失败，返回Unit；外层仍记录installed system hooks。SettingsProvider查类失败也可能提前return，外层仍记录hooked。reader则取得对象就固定isReadable=true。以上不能作为hook和配置成功证据。[入口](../app/src/main/java/com/js/nowakelock/xposedhook/ModernXposedModule.kt)、[installer](../app/src/main/java/com/js/nowakelock/xposedhook/modern/ModernXposedSystemHookInstaller.kt)、[provider hook](../app/src/main/java/com/js/nowakelock/xposedhook/modern/ModernSettingsProviderHook.kt)、[reader](../app/src/main/java/com/js/nowakelock/xposedhook/model/HookConfigReader.kt)

设备探针应分别记录入口构造、进程/API、每个方法的发现/安装结果、配置revision、真实回调与事件回传；SettingsProvider读配置成功不能替system_server作证。

### E. 配置实验仍有明确遗漏和待复现边界

- 全量writeAll只写St/AppSt，未发布debug；UI重建debug仍读SPTools。若旧后端不可用，remote-only写入的debug不会由这条UI初始化路径读回；service稍后绑定的全量发布也不含debug。不能把“所有配置可重发”记作已完成。[writer](../app/src/main/java/com/js/nowakelock/data/config/ConfigSnapshotWriter.kt)、[SettingsViewModel](../app/src/main/java/com/js/nowakelock/ui/screens/settings/SettingsViewModel.kt)
- 全量发布与单条发布可在多个IO协程并行，每个key独立commit，没有revision/串行化。旧快照是否覆盖刚更新规则需要受控并发实验；无需等设备才能验证算法。
- writeAll没有删除已不存在的key。这是发布接口能力边界；当前BackupRepo是insert合并语义，并不清空未出现在备份中的Room规则，所以不能直接宣称“恢复较小备份必然产生幽灵规则”。应先确定删除/替换语义，再做对应实验。[BackupRepo](../app/src/main/java/com/js/nowakelock/data/repository/backup/BackupRepo.kt)
- BackupManager忽略publishAll返回的false，仍返回恢复成功；本地恢复成功和hook配置发布成功需要分开表达。[BackupManager](../app/src/main/java/com/js/nowakelock/data/repository/backup/BackupManager.kt)

## 4. 历史验证并不能覆盖当前工作区

本轮检查到的旧发布APK：`app/build/outputs/apk/release/NoWakeLock-3.0.10.apk`，检查时修改时间2026-06-25，SHA256 `5818da24e03f01e6f6217b3342b025ebf6a8e03c20de500bb21f3a914d2a6ad0`。ZIP检查确认两套入口与min101/target101；其scope只有system/android，当前源码还包含com.android.providers.settings。因此旧APK不是当前工作区产物的完整证据。

本轮重跑前检查到7份JUnit XML，均为6月25日，共16个测试、0失败；它们未包含当前ModernXposedModuleCompatibilityTest与XposedMetadataTest的结果，不能视作当前相关测试全通过。随后重跑已更新测试输出，以下保留检查时的历史结论。

两份当前测试甚至直接矛盾：ModernMetadataTest要求scope包含SettingsProvider，XposedMetadataTest要求scope恰好是system/android。本轮读取实际scope并比较，前一断言条件成立、后一条件不成立。这是本地确定证据，不需设备。[测试1](../app/src/test/java/com/js/nowakelock/xposedhook/ModernMetadataTest.kt)、[测试2](../app/src/test/java/com/js/nowakelock/xposedhook/XposedMetadataTest.kt)

其他既有测试的证明范围也有限：构造测试是在API101测试依赖下反射查无参；隔离测试只是扫描选定源码关键词；FakeBackend测试验证key和值。它们不执行旧loader、ART验证、真实Remote Preferences Binder或系统hook链，也不证明完整可达DEX没有legacy调用。

本轮构建与测试执行结果在下节单独记录，不覆盖或改写上述历史证据。

## 5. 本轮执行记录

执行针对xposedhook、data.config及ModuleCheckResult的现有测试，并请求assembleRelease，固定`-PuseLocalMavenBootstrap=true --offline`。首次沙盒内解析API101依赖失败；随后使用现有bootstrap脚本补齐官方Maven依赖及module元数据，在沙盒外重跑。

- Debug/Release Kotlin编译成功。
- **31项测试执行，30通过、1失败**：`XposedMetadataTest.modern scope uses system server target for SettingsProvider callbacks`，第34行的精确scope断言。没有修改断言来掩盖失败。
- 同次命令还出现Gradle problems-report目标文件已存在的报告输出错误；这与测试断言失败分开记录。测试XML仍生成，可见[本轮测试报告](../app/build/reports/tests/testDebugUnitTest/index.html)。
- 同次构建因失败没有完成发布打包；另行执行`.\gradlew.bat :app:assembleRelease -PuseLocalMavenBootstrap=true --offline --no-problems-report`成功，包含R8处理后的发布产物。
- 新APK SHA256：`c9bbab5821f2e7d6160b7b5dbc0da1df0f2a97d589646361e40f2cc9aa9ff238`。重新检查ZIP及DEX：两套入口存在，min101/target101，scope包含三项；Modern入口构造为`()V`，DEX没有定义`io.github.libxposed.api.*`类。这证明当前打包形状，不证明旧loader或ART装载成功。

针对性测试命令：`.\gradlew.bat :app:testDebugUnitTest --tests 'com.js.nowakelock.xposedhook.*' --tests 'com.js.nowakelock.data.config.*' --tests 'com.js.nowakelock.data.model.ModuleCheckResultTest' :app:assembleRelease -PuseLocalMavenBootstrap=true --offline`。没有执行全部项目测试或设备smoke。

## 6. 接下来最有价值的实验

| 顺序 | 要回答的单一问题 | 成功证据 | 可以在哪里完成 |
|---|---|---|---|
| 1 | 指定旧版与新版能否装载同一最小入口 | 同一APK哈希，两边构造/系统回调日志正确，无缺类或重复接管 | 主机先查ABI/R8；ART在合适模拟器或设备验证 |
| 2 | 旧APK被标Modern后如何继续取得规则 | App写revision，旧system_server读同revision，App退出仍可读 | 先核旧协议/适配；再设备闭环 |
| 3 | 新侧规则是否稳定到达两个目标进程 | system_server及SettingsProvider独立报告revision；重启/重连/更新一致 | 主机测发布状态机，设备测IPC与生命周期 |
| 4 | 每类hook是否保留业务语义 | 允许、全拦、灭屏规则、间隔/regex适用项；原方法执行次数及结果正确 | 受控逻辑/chain实验，再目标ROM |
| 5 | 统计是否完整往返 | 人工制造一个已知事件，hook→provider→DB→UI逐步对应 | 设备；同一ROM比较旧主线与候选 |

第1、2步应构成一个独立最小探针，避免让三类复杂系统hook掩盖入口/配置问题。可以并行做新侧API102探针；不必先迁移全部业务才能检验核心可行性。

指定LSPosed1.9.2已有Remote Preferences能力，故候选可以是“旧loader生命周期桥 + 原有hook业务 + 指定旧版配置适配”，新侧继续纯Modern。**这会改变原先‘旧配置完全原样不动’的实现假设**，但保留用户的旧版功能目标；旧service协议与101wrapper不能预设兼容。[旧Remote Preferences依据](Xposed_Legacy_Environment_Feasibility_2026-09-06.zh-CN.md)

完成第1、2步后才适合决定最终单APK架构；现在不应以任务完成百分比或“最后上手机测一下”描述剩余工作。本轮没有改业务代码、metadata、既有测试或OpenSpec完成状态，也没有安装APK、切换框架或将#54/#55标为已解决。
