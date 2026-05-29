# 💧 喝水助手 (Water Tracker)

一款简洁的 Android 喝水记录与提醒应用。

## ✨ 功能

- 📝 **记录喝水** - 快速按钮 (150/250/350/500ml) + 自定义输入
- ⏰ **定时提醒** - 可自定义间隔的喝水提醒通知
- 📊 **进度展示** - 环形进度条显示每日完成百分比
- 🏠 **桌面小组件** - 小米/Android 桌面一键记录 + 实时进度
- 🎨 **Material 3** - 支持动态主题（Android 12+）
- 💾 **本地存储** - Room 数据库，数据全在本机

## 📱 安装方式

### 方式一：GitHub Actions 自动打包

1. Fork 或上传代码到你的 GitHub 仓库
2. 推送到 `main` 分支后会自动触发构建
3. 进入 Actions → 最新构建 → 下载 `water-tracker-debug` artifact
4. 解压得到 APK，传到手机安装即可

### 方式二：本地编译

```bash
git clone <your-repo-url>
cd water-tracker
./gradlew assembleDebug
# APK 在 app/build/outputs/apk/debug/
```

## 🛠 技术栈

| 组件 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 数据库 | Room |
| 偏好存储 | DataStore |
| 后台任务 | WorkManager |
| 桌面小组件 | Glance AppWidget |
| 构建 | Gradle 8.5 + AGP 8.2 |

## 📂 项目结构

```
app/src/main/java/com/drink/watertracker/
├── data/          # 数据层 (Room DB, DataStore)
├── ui/            # 界面 (ViewModel, Theme, Screens)
├── widget/        # 桌面小组件 (Glance)
├── worker/        # 提醒后台任务 (WorkManager)
├── MainActivity.kt
└── WaterApp.kt
```

## 📄 License

MIT
