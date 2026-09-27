# 提醒记事（Android）

一个记录「提醒 / 人生轨迹 / 纪念日」的安卓 App，Kotlin + Jetpack Compose 单 Activity 实现。

## 四个模块（底部悬浮毛玻璃导航切换）

| 模块       | 功能                                                                                            |
| -------- | --------------------------------------------------------------------------------------------- |
| **别忘了**  | 记录长间隔提醒。顶部显示今天是年月日星期；右下角毛玻璃「+」弹出输入界面（事项 / 日期 / 重复 / 设置提醒 / 图标）；清单支持拖动排序、点击缩放 90% 后弹出「修改 / 删除」 |
| **人生轨迹** | 时间线（最新在上、最早在下）。首次点「+」先设置生日，之后每条自动显示当时几岁                                                       |
| **纪念日**  | 卡片列表，按剩余天数排序；日期过后每年重新计算，阴历项标注（阴历）                                                             |
| **设置**   | 生日设置、数据备份与恢复（JSON 导出 / 导入）、关于                                                                 |

## 需求对照

- 底部导航栏半透明毛玻璃、悬浮在底部（`frostGlass` 统一实现，含阴影 + 半透明渐变 + 高光描边）
- 右下角圆形毛玻璃「+」按钮
- 输入界面五行：事项 → 日期（半屏日历）→ 重复（每 N 天/周/月/年）→ 设置提醒（写入系统日历）→ 图标（内置 40 个矢量图标）
- 半屏日历：可直接点选日期，月份右侧「今日」快捷，支持阳历 / 阴历切换（1900–2100）
- 清单每项头部两竖线 + 图标，按住拖动即可调整上下顺序
- 每项：标题（大）、日期 + 星期 + 重复 + 剩余天数（灵活显示年月日）、「距今已 X 天」（数字放大占两行高度）
- 人生轨迹按生日计算「几岁」，每条淡色背景卡片
- 纪念日阴历项在时间后显示（阴历），过期自动按年重算
- 设置：生日、备份恢复、关于

## 目录结构

```
app/src/main/kotlin/com/noteremind/app/
├── MainActivity.kt
├── data/
│   ├── Models.kt           提醒 / 轨迹 / 纪念日 / 全量数据模型（kotlinx.serialization）
│   └── AppRepo.kt          JSON 文件持久化 + StateFlow，备份导出导入
├── ui/
│   ├── AppRoot.kt          页面切换、毛玻璃底部导航、毛玻璃 FAB、各弹出层编排
│   ├── theme/Theme.kt      浅色配色与主题
│   ├── components/
│   │   ├── Glass.kt        frostGlass 毛玻璃 Modifier
│   │   ├── IconSet.kt      内置 40 个简约矢量图标
│   │   ├── CalendarSheet.kt 半屏日历（阴阳历切换、今日）
│   │   ├── EditorSheet.kt  提醒 / 纪念日输入界面 + 图标选择器
│   │   └── LifeEventSheet.kt 人生轨迹输入界面
│   └── screens/
│       ├── DontForgetScreen.kt  别忘了（拖动排序、缩放弹菜单）
│       ├── LifeTrackScreen.kt   人生轨迹时间线
│       ├── AnniversaryScreen.kt 纪念日
│       └── SettingsScreen.kt    设置
└── util/
    ├── LunarCalendar.kt    1900–2100 阴阳历互转（含闰月）
    ├── DateUtils.kt        日期格式化、重复 / 下次到期计算、灵活时长、年龄
    └── SystemCalendarHelper.kt 跳转系统日历建立日程（含 RRULE 重复规则）
```

## 编译

**Android Studio**：直接 `File → Open` 选择本目录，等待 Gradle 同步后 Run 即可（建议 JDK 17、compileSdk 35）。

**命令行**：

```bash
cd ReminderApp
# 1) 确认 local.properties 指向本机 Android SDK（已填好一份，按需改成自己的路径）
#    Windows 示例：sdk.dir=D:\\Android\\Sdk
# 2) 构建（需要 JDK 17 + 网络下载 Gradle 与依赖）
./gradlew assembleDebug      # 出包：app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug       # 直接安装到已连接的设备
```

> 网络提示：Android 依赖默认从 Google / Maven Central 拉取。国内若下载缓慢，可在  
> `~/.gradle/init.d/` 放一个镜像脚本（阿里云 / 腾讯云 maven 镜像）即可加速，工程源码无需改动。

数据保存在 App 私有目录 `reminder_data.json`，备份导出的是同一份结构的 JSON，可直接恢复。
