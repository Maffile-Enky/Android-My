# Android 签名与构建文档

## 签名配置

### Keystore 文件

| 项目 | 值 |
|------|-----|
| 文件名 | `my-release-key.jks` |
| 位置 | 项目根目录 |
| Store Password | `android123` |
| Key Alias | `my-key-alias` |
| Key Password | `android123` |

### 配置文件

签名信息存储在 `keystore.properties` 文件中，`app/build.gradle.kts` 读取该文件进行签名配置：

```kotlin
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}
```

## 版本管理

在 `app/build.gradle.kts` 中修改版本号：

```kotlin
defaultConfig {
    versionCode = 2      // 递增数字版本号，每次发布 +1
    versionName = "1.1.0" // 语义化版本号
}
```

### 版本历史

| 版本 | versionCode | 说明 |
|------|-------------|------|
| 1.0.0 | 1 | 初始版本 |
| 1.1.0 | 2 | 用户权限系统 + 管理后台 + 检查更新 + 头像上传 |

## 构建命令

### Debug 版本

```bash
./gradlew :app:assembleDebug
```

输出路径：`app/build/outputs/apk/debug/app-debug.apk`

### Release 版本（已签名）

```bash
./gradlew :app:assembleRelease
```

输出路径：`app/build/outputs/apk/release/app-release.apk`

### 清理后重新构建

```bash
./gradlew clean :app:assembleRelease
```

## 生成 Keystore

如果需要生成新的 keystore 文件：

```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

## 完整发布流程

1. 修改 `app/build.gradle.kts` 中的 `versionCode` 和 `versionName`
2. 更新服务端版本管理（管理后台 → 版本管理 → 发布新版本）
3. 构建签名 APK：`./gradlew :app:assembleRelease`
4. 将 APK 上传到服务器供客户端下载更新
5. 确保服务端 `apkUrl` 指向正确的下载地址

## 注意事项

- `keystore.properties` 包含敏感信息，不要提交到 Git
- `my-release-key.jks` 文件丢失将无法更新已发布的应用
- 每次发布必须递增 `versionCode`，否则无法覆盖安装
