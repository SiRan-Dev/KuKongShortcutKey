# 酷控快捷磁贴 (KuKongShortcutKey)

一个无界面的 Android 快捷设置磁贴（Quick Settings Tile）：在下拉控制中心点一下，即可打开「酷控智能遥控」，省去翻应用列表找 App 的麻烦。

## 功能特性

- 点击磁贴秒开酷控智能遥控，磁贴短暂点亮后自动熄灭
- 无任何可见界面、无桌面图标、不申请任何敏感权限
- 磁贴图标复刻小米万能遥控磁贴样式（外环 + 中心圆 + 四向圆点）
- 兼容 MIUI / HyperOS 的后台启动限制

## 下载安装

前往 [Releases](../../releases) 页面下载最新 APK，安装后即可使用。应用安装后没有桌面图标，属于正常现象。

## 使用方法

1. 下拉通知栏，进入快捷设置（控制中心）的编辑页面
2. 找到「酷控遥控」磁贴，拖动到可用区域
3. 点击磁贴即可打开酷控智能遥控

> 注意：覆盖安装新版本后，如果磁贴图标没有变化，是系统图标缓存导致——把磁贴移除后重新添加即可。

## 实现原理

MIUI / HyperOS 会拦截后台服务直接拉起 Activity（即使授予「后台弹出界面」权限也无效），因此本应用采用中转方案：

```
点击磁贴 → KuKongTileService → TransparentLauncherActivity（全透明，随应用进入前台）→ 拉起酷控 → 自动销毁
```

磁贴通过 `startActivityAndCollapse()` 启动透明中转页，此时应用已处于前台状态，再由中转页调用 `startActivity()` 即可绕过系统的后台启动限制。中转页无 UI 且 `excludeFromRecents`，用户无感知。

目标应用按以下顺序查找，覆盖包名变体：

1. 标准包名 `com.kookong.app`
2. 包名包含 `kookong` 的应用
3. 应用名包含「酷控」或 `kookong` 的应用

## 兼容性

- 最低支持 Android 8.0（API 26），目标版本 Android 14（API 34）
- 已在 HyperOS 上验证；原生 Android 及其他 ROM 同样可用

## 自行构建

环境要求：JDK 17+，Android SDK 37（Gradle Wrapper 会自动拉取对应版本的 Gradle）。

```bash
# Debug 构建（无需签名配置）
./gradlew assembleDebug

# Release 构建：先在项目根目录创建 keystore.properties
./gradlew assembleRelease
```

`keystore.properties` 格式（该文件及 `keystore/` 目录已被 gitignore，不会提交）：

```properties
storeFile=keystore/your-keystore.jks
storePassword=你的库密码
keyAlias=你的别名
keyPassword=你的密钥密码
```

## 项目结构

```
app/src/main/
├── AndroidManifest.xml                              # 声明磁贴服务与透明中转页
├── java/com/sirandev/kukongshortcutkey/
│   ├── KuKongTileService.kt                         # 磁贴服务：点亮/熄灭、拉起中转页
│   └── TransparentLauncherActivity.kt               # 透明中转页：查找并启动酷控
└── res/
    ├── drawable/ic_kookong_tile.xml                 # 磁贴图标（矢量图，纯白+透明底）
    └── values/strings.xml                           # 应用名与磁贴名称
```
