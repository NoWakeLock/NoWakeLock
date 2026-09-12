# NWL-21：空正则集合导致 API 102 整批规则发布失败

## 现场

- OP13 / PJZ110，Android 16，Vector 2.2（3080），API 102。
- 安装的本地 release：3.0.10 / 87，SHA-256 `ac187ddbde425361d675c53bf85a641ea126c90cf216d13cc9f691154d95b824`。
- 主用户 0，工作资料 10。现场微信安装于用户 10，规则和事件中的用户 ID 均为 10。
- Wakelock、Alarm、Service 及 SettingsProvider Hook 均已安装。累计提交事件超过 114 万，未报告丢失、写入失败或待处理积压。
- `observedRevision=0`、`backendAvailable=false`，规则没有送到 Hook。微信 `ALARM_ACTION(10000)` 已配置完全拦截，但记录为 47 次触发、0 次拦截；其他已配置拦截的应用也存在相同现象。

## 根因

重启 NoWakeLock 应用（未重启设备）后，官方配置服务 Binder 成功送达，排除了未启用或服务未连接。发布日志稳定出现：

```text
ConfigPublisher: Failed to publish config to remote
BadParcelableException: Parcelable encountered ClassNotFoundException
reading a Serializable object (name = wh0)
```

该 release 的 R8 mapping 将 `wh0` 对应到 `kotlin.collections.EmptySet`。

[官方 libxposed service 102.0.0 RemotePreferences 源码](https://github.com/libxposed/service/blob/102.0.0/service/src/main/java/io/github/libxposed/service/RemotePreferences.java) 的 `putStringSet` 保留调用方传入的集合对象；提交时使用 `Bundle.putSerializable` 包装 Java HashMap，但没有转换其内部集合。

NoWakeLock 的 `SharedPreferencesConfigBackend.publish` 使用 `toSet()`。空集合会变成 Kotlin EmptySet，混淆后的类不在框架进程的类加载器中。只要任意应用规则有空的正则组，整批配置都可能失败，随后也不会进行已持久化快照的激活。这不是多用户 UID 匹配错误，也不是统计功能故障。

## 修复与验证

在配置后端的传输边界将所有字符串集合复制为 `java.util.HashSet`，覆盖整批发布和单项写入。保持规则、用户 ID、原子激活协议及框架不变。该类型也适用于旧 SharedPreferences 后端。

回归测试通过实际 `ConfigSnapshotWriter → SharedPreferencesConfigBackend` 调用路径，将交给 Editor 的集合按官方服务格式序列化，并用无法加载模块类的类加载器读取；包括空、单条和多条规则集合。

设备只读复现脚本：`.tmp/op13-diagnosis-20260912/check_config.py`。修复前输出本地请求版本大于 0、Hook 观察版本为 0，断言失败。现场备份及日志保存在同目录，不提交个人设备数据。

两项新增回归测试在修复前均因 `ClassNotFoundException: kotlin.collections.EmptySet` 失败；修复后全部 100 项主机测试通过（0 failures/errors/skipped）。Release 构建成功，APK SHA-256：`8990278e2fefb1fd8bb5509e453a4207de27cfb3ad46949ca7301472cf2aa7cd`。

OP13 覆盖安装修复版后，仅重启 NoWakeLock 应用，系统进程 PID 保持 `3431`：

- 同一只读复现脚本转绿，`requested=observed=1789192093309`，配置后端可用，system_server 与 SettingsProvider 观察版本一致。
- 微信用户 10 的 `PlatformComm` 从拦截 0 次增长至 18 次；B 站用户 10 的 `NeuronLocalService` 从 0 次增长至 94 次。修复生效后，受这两条规则约束的放行计数保持不变。
- 强制停止 NoWakeLock 后，拦截计数仍继续增长，规则无需应用驻留。
- 最后一次观察中，`PlatformComm` 拦截 49 次、`MicroMsg.MMAutoAuth` 拦截 1 次，B 站 `NeuronLocalService` 拦截 150 次。时间窗口规则在窗口经过后仍有新的放行，不能要求累计放行数永远不变。
- 统计队列检查无丢失、失败或积压。
- 本次没有修改用户规则、没有重启设备、没有修改 Vector。未重新进行冷启动验收，也没有把“应用侧修复立即生效”称为 Hook 代码热重载：system_server 仍执行原先已加载的 Hook，更新的是修复后的发布器发来的规则。

该结论验证了现场多用户拦截失败的直接原因，并不等于所有应用、所有事件都已重新验收。之前交付停在设备首次解锁前，未完成真实用户配置的发布闭环；后续交付检查必须包含非空真实规则库、空正则组、请求/发布/Hook 版本一致及实际拦截增量。

## 正则匹配排查

现场 JSON 中的反斜杠显示曾引起怀疑。随后在 OP13 上只读查询实际保存的正则，按 Android `org.json.JSONArray` 解码后调用 `java.util.regex.Pattern.matches(regex, "ALARM_ACTION(10000)")`，结果为 true。这条正则本身未被证明有错，原规则保持不变。
