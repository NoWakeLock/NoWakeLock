# 3.0.11 测试版发布准备

准备日期：2026-09-12。`versionName=3.0.11`，`versionCode=89`；由维护者手动运行 CI，发布状态为 **prerelease**。

## 版本与说明

- `app/build.gradle` 和 `META-INF/xposed/module.prop` 保持版本一致。
- GitHub Release 使用 [中英完整说明](../fastlane/release-notes/3.0.11.md)，按实际 versionName 读取；没有专用说明的其他版本继续使用提交记录生成日志。
- 文档站各语言 changelog 和 fastlane 各语言 `89.txt` 提供摘要；补记此前缺失的 3.0.10 排序修复。
- CI 的 Hook 构建身份为 `versionName:versionCode-源码提交前12位`，便于区分实际运行代码，固定输入可重复生成相同身份。重复构建同一提交不会伪造新版本；本地修改后交付仍须显式传入不同的 `-PhookBuildId`。
- 对照 CI 做可重复构建时，必须传入同一 `hookBuildId`（除其他固定构建输入外）；直接使用 Gradle 默认身份的产物不会与 CI 字节相同。本次测试发布不宣称已通过 F-Droid 的独立可重复构建核验。

## 手动运行 CI

本次按维护者要求将准备好的提交合入并推送到 `master`，然后在 GitHub Actions → **Release Upload** → **Run workflow** 选择 `master`。版本仍通过 prerelease 渠道试用；合入分支不等于发布稳定版。

| 参数 | 本次取值 | 含义 |
|---|---|---|
| release_type | `none` | 使用已提交的 3.0.11 / 89；不要再次选择 patch |
| build | `true` | 构建主应用的混淆 release APK |
| github | `true` | 上传为 GitHub Release；若只取 CI artifact，选 false |
| release_status | `prerelease` | 测试版，不选择 release |

预期 GitHub 标题 `v3.0.11 (Test release)`，tag `v3.0.11`，附件 `NoWakeLock-3.0.11.apk`。此处沿用仓库数字版本/tag 约定，测试渠道由 GitHub prerelease 标记和标题区分；APK 内版本名仍为 3.0.11。

不要用手动推送 `v3.0.11` tag 的方式替代本次流程：现有 tag push 路径按稳定版处理。手动运行默认使用 `none / build=true / prerelease`，使用原始已提交代码并跳过版本提交和分支推送。Release tag 明确指向所构建的提交；通过 GITHUB_TOKEN 产生的 tag push 不会递归触发另一轮发布。

CI 使用仓库 Gradle Wrapper 和项目需要的 Android SDK；只构建 `:app:assembleRelease`，不把受控测试应用当作发布 APK。CI artifact 同时保存 R8 mapping，保留 7 天。签名继续使用仓库现有 secrets。本地检查不使用发布密钥，也不替代 GitHub runner、远程依赖和签名 secrets 的实际执行验证。

发布后核对 prerelease 标志、tag 提交、APK 版本/签名、双入口及 module.prop，并保留本次 CI 日志与 mapping。重新上传相同 tag/同名附件可能被 GitHub 拒绝；需要新一轮用户测试时应递增 versionCode 并准备对应更新说明，不强制覆盖已发布 tag。

## 测试边界

前序 OP13 候选是 `3.0.10 / 88`，其设备证据见 [NWL-22](NWL-22_API102_Hot_Reload_2026-09-12.zh-CN.md)。本次为版本与发布准备，不将先前候选的 APK 哈希或真机结论改写成 3.0.11 已安装验收，也不关闭 NWL-22 尚待验证的边界。升级提示、同签名覆盖/异签名备份、首次重启要求和待测范围均随完整说明发布。

## 本地检查结果

- `:app:testDebugUnitTest :app:assembleRelease -PuseLocalMavenBootstrap=true --offline --no-problems-report -PhookBuildId=3.0.11:89-release-prep-20260912` 通过：120 项测试，0 失败/错误/跳过，混淆及 release lint vital 通过。
- APK 确认为 `com.js.nowakelock / 3.0.11 / 89`，旧/现代双入口、module.prop 和构建身份正确；SHA-256 为 `1707a10db351527acd3581b7467388a4fdffa788faf91e9bfa0603be86129603`。这是本地调试签名的构建检查产物，不是 CI 发布签名产物，未安装到设备。
- Actionlint 1.7.12 检查通过（本地未启用 shellcheck/pyflakes）；Ruby 3.3.7 语法检查通过。隔离执行 Fastfile，外部动作全部使用替身，验证完整说明、prerelease 标志、精确提交与主 APK、只构建不发布、版本提交先推送再发布、构建身份及缺失 APK 时拒绝发布。没有实际调用 GitHub 发布接口。
- 工作流内的版本同步脚本分别通过 none、patch、code、匹配 tag 和拒绝不匹配 tag 五种本地场景；元数据五语言摘要均不超过 500 字符，各文档版本一致。
- 证据在忽略目录 `.tmp/release-3.0.11/`：`build.txt`、`verification.json`、`check_fastlane.rb` 及 CI 脚本场景目录。实际 GitHub runner 的远程依赖、签名与上传留给维护者本次手动运行。

## 发布工具依据

本会话没有可用 Context7 工具，核对了官方文档：[fastlane set_github_release 的 commitish / is_prerelease](https://docs.fastlane.tools/actions/set_github_release/)、[GitHub Actions 的 GITHUB_TOKEN 触发行为](https://docs.github.com/en/actions/how-tos/write-workflows/choose-when-workflows-run/trigger-a-workflow)。根据 [Google SDK 包清单](https://dl.google.com/android/repository/repository2-3.xml) 固定命令行工具 `16111833`、平台 `platforms;android-37.0` 和构建工具 `36.0.0`；旧工作流的 2023 年工具与 `android-37` 包名不适用于这次构建。
