# Wakelock 信息管理系统方案总结

基于我们的讨论，以下是 Wakelock 信息管理系统的最终方案整理：

## 一、数据存储与组织

### 基本架构

1. **单一 JSON 文件**
   - 所有 wakelock/alarm/service 信息存储在一个完整的 JSON 文件中
   - 包含版本号、更新日期和所有条目数据
   - 支持多语言描述和建议

2. **文件位置**
   - 内置版本：`assets/wakelock_info.json`（随应用打包）
   - 更新版本：`context.filesDir/wakelock_info.json`（用户存储空间）

3. **版本控制**
   - JSON 文件包含明确的版本号（如 `"version": "1.2.0"`）
   - 可选包含更新说明（如 `"release_notes": {"en": "Added 20 new wakelocks", "zh": "添加了20个新的唤醒锁"}`）

### JSON 结构设计

```json
{
  "version": "1.0.0",
  "update_date": "2023-07-15",
  "release_notes": {
    "en": "Initial version with 150 wakelocks",
    "zh": "初始版本，包含150个唤醒锁"
  },
  "items": [
    {
      "id": "nlp_wakelock",
      "name": "NlpWakeLock",
      "type": "wakelock",
      "package": "com.google.android.gms",
      "safe_to_block": "safe",
      "description": {
        "en": "Used by Google Play Services to determine your location...",
        "zh": "由 Google Play 服务使用，用于确定您的位置..."
      },
      "recommendation": {
        "en": "Safe to limit. Allow each 420 seconds.",
        "zh": "安全限制。建议每420秒允许一次。"
      }
    },
    // 更多条目...
  ]
}
```

## 二、应用内实现

### 1. 数据加载与缓存

- **懒加载机制**：首次请求时才加载和解析 JSON
- **内存缓存**：解析后的数据保存在内存中，避免重复解析
- **查询优化**：构建内存索引（如基于名称和包名的 Map）加速查询
- **加载优先级**：
  1. 首先尝试从内部存储加载更新版本
  2. 如果不存在，回退到应用内置版本

### 2. 数据管理器

- **单例设计**：创建 `WakelockInfoManager` 单例类管理所有数据操作
- **提供接口**：
  - 获取特定 wakelock 信息
  - 获取支持的语言列表
  - 获取当前数据版本
  - 手动检查和下载更新

### 3. 更新机制

- **手动更新**：在应用设置中提供"检查信息库更新"选项
- **更新流程**：
  1. 从 GitHub 获取最新 JSON 文件的版本信息
  2. 比较本地版本，如有更新则下载
  3. 下载到临时文件，验证有效后替换现有文件
  4. 重新加载数据并通知 UI

## 三、GitHub 仓库与社区贡献

### 1. 仓库结构

```
wakelock-info-repo/
├── wakelock_info.json    # 生成的最终 JSON 文件（应用直接使用）
├── source/               # 源数据文件（供贡献者编辑）
│   ├── wakelocks.yaml    # 可能使用更人类友好的 YAML 格式
│   ├── alarms.yaml       # 分类存储源数据
│   └── services.yaml     # 分类存储源数据
├── scripts/              # 自动化脚本
│   └── build.js          # 将源文件合并为单一 JSON
├── README.md             # 贡献指南
└── CHANGELOG.md          # 变更记录
```

### 2. 贡献流程

- **用户贡献方式**：
  - 通过 GitHub Issues 提交新信息（使用标准模板）
  - 通过 Pull Request 修改源 YAML 文件
  - 可选使用 GitHub Discussions 讨论特定 wakelock

- **自动化辅助**：
  - 使用 GitHub Actions 自动验证提交的更改
  - 使用 Bot 协助格式化和验证贡献者提交
  - 自动构建脚本将源文件合并为最终 JSON

- **版本管理**：
  - 维护者审核通过后更新版本号
  - 自动生成变更记录
  - 定期发布新版本

## 四、界面集成

### 1. 详情页集成

- 在 `DADetailViewModel` 中添加获取 wakelock 信息的方法
- 页面加载时查询当前 wakelock 的描述和建议
- 根据安全等级调整 UI 样式（颜色、图标等）

### 2. 设置页面集成

- 添加"检查唤醒锁信息更新"选项
- 显示当前数据库版本和条目数量
- 提供更新后的变更通知

## 五、实现步骤

### 短期实现计划：

1. **创建基础 JSON 文件**：
   - 基于现有 INFO.md 整理内容
   - 设计合理的 JSON 结构
   - 添加版本信息和基本元数据

2. **实现数据管理器**：
   - 创建 `WakelockInfoManager` 单例
   - 实现 JSON 加载和解析
   - 添加简单的缓存和查询机制

3. **集成到详情页**：
   - 在 ViewModel 中添加数据获取方法
   - 在 UI 中展示描述和建议信息
   - 根据安全等级调整显示风格

4. **添加手动更新功能**：
   - 在设置中添加更新检查选项
   - 实现基本的下载和替换逻辑
   - 添加版本信息显示

### 长期发展方向：

1. **建立 GitHub 仓库**：
   - 设置公开仓库结构
   - 创建初始数据文件
   - 编写贡献指南

2. **改进匹配机制**：
   - 添加模式匹配支持
   - 实现更精确的相关性排序
   - 支持模糊搜索

3. **增强社区功能**：
   - 设计标准化贡献模板
   - 添加自动验证机制
   - 实现 Bot 辅助贡献

## 结论

这个方案在保持简单实现的同时，为 wakelock 信息管理提供了完整的解决方案。核心设计原则是：

1. **简单有效**：单一 JSON 文件，懒加载机制，内存缓存
2. **用户友好**：集成到详情页，提供丰富的描述和建议
3. **可扩展**：支持社区贡献，版本控制，多语言支持
4. **可维护**：清晰的结构，分离内置和更新数据，手动更新机制

这种设计既满足了当前应用的需求，又为未来的扩展和社区协作提供了基础。
