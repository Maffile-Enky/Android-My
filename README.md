---

### 新增 5 个工具（共 10 个）
工具	功能
1-5	计数器、计算器、秒表、计时器、待办事项	 原有
6	随机数	指定范围随机数 + 掷骰子
7	单位换算	长度/重量/温度实时换算
8	便签	标题+内容，支持保存/加载/清空
9	BMI计算	身高体重输入，BMI值+健康评估
10	字数统计	实时统计字符数/中文字数/单词数
个人中心改造
可编辑字段：昵称、性别（单选）、生日、手机号、邮箱、地址、个性签名
点击「保存信息」按钮持久化到 SharedPreferences，下次打开自动加载
UI 优化
计数器 — 圆形卡片展示数字，大圆按钮 - +
秒表/计时器 — 卡片式时间显示，monospace 字体
待办事项 — 统一圆角输入框，紫色添加按钮
首页 — 新增紫色顶栏标题栏，设置按钮移至顶栏，欢迎卡片化
工具卡片 — 圆角14dp，阴影2dp，紧凑间距
所有按钮 — 统一 cornerRadius="8dp"，白字配色

登录页面 (fragment_login.xml)
重新设计：圆形头像 + 卡片式表单，图标装饰的输入框
登录状态时显示 "已登录，欢迎回来，xxx"，隐藏登录表单
退出登录按钮替代注册按钮
注册功能 (LoginFragment.kt)
点击「注册新账号」弹出注册对话框（dialog_register.xml）
表单：用户名（≥3字符）、密码（≥4字符）、确认密码
验证逻辑：密码一致性检查、用户名唯一性检查
注册成功后自动登录
登录流程
校验用户名是否存在 → 校验密码 → 登录成功，状态持久化
退出登录：清除当前用户状态，恢复登录表单
数据通过 SharedPreferences 存储（user_accounts），跨 App 重启保持登录状态


版本更新 (0.2.0)：

app/build.gradle.kts:32 — versionName = "0.2.0"
SettingsFragment.kt — Toast 文本改为 v0.2.0
fragment_settings.xml — 检查更新和底部版本号改为 v0.2.0
英语选项：

ThemeManager.kt — 新增语言管理（LANG_ZH/LANG_EN），wrapContext() 通过 attachBaseContext 注入 locale
SettingsFragment.kt — 语言行点击弹出 "简体中文 / English" 选择框，切换后 activity.recreate() 即时生效
MainActivity.kt — 新增 attachBaseContext() 在启动时恢复已保存的语言
values-en/strings.xml — 英文版字符串资源（底部导航、工具名等）

方案已写好，覆盖了服务端（Ktor REST + WebSocket）和客户端（Retrofit + OkHttp WebSocket）的完整架构。

主要设计决策：

Ktor 作为后端框架（你选的）
REST API 处理帖子 CRUD + WebSocket 推送实时通知（新帖、点赞）
H2 数据库在 MVP 阶段开箱即用，后期可迁移到 MySQL
客户端网络优先 + 本地缓存兜底，保证离线可用
ECS 部署用 systemd 管理进程自动重启

# 构建成功。以下是已修复内容的总结：

## 代码文件修改
**CommunityFragment.kt**
将 `viewModel.uiState.observe(viewLifecycleOwner)` 和 `viewModel.posts.observe(viewLifecycleOwner)` 改为使用 `viewLifecycleOwner.lifecycleScope.launch { viewModel.uiState.collect { ... } }`，原因是 StateFlow 不支持 `.observe()` 方法（该方法属于 LiveData API）。新增了 `lifecycleScope` 和 `kotlinx.coroutines.launch` 的导入语句。

**fragment_community.xml**
将4处硬编码的中文文本替换为字符串资源引用：
- "暂无帖子..." → `@string/community_empty`
- "网络连接失败" → `@string/community_error`
- "重试" → `@string/community_retry`
- 内容描述`contentDescription="发布新帖子"` → `@string/community_new_post_fab`

**drawer_settings.xml**
修复硬编码的版本号 "v1.0"，替换为 `@string/version_short`（对应值为 "v0.2.0"）

**strings.xml（中文+英文）**
为社区页面的空状态/错误状态/重试/悬浮按钮状态新增了4个字符串资源。

## 构建产物与测试说明
APK 文件路径：`app/build/outputs/apk/debug/app-debug.apk`
位于 `server/` 目录下的ECS服务器代码已编写完成——如需完成端到端测试，请将服务器JAR包部署至你的ECS实例，并将 `RetrofitClient.BASE_URL` 更新为ECS实例的IP地址。

### 术语注释（便于理解）
1. **StateFlow/LiveData**：安卓开发中用于数据监听的组件
2. **lifecycleScope**：安卓协程作用域，用于安全执行异步任务
3. **硬编码**：直接将文本/数值写在代码/布局中，不利于多语言、版本维护
4. **ECS**：阿里云云服务器
5. **端到端测试**：从前端APP到后端服务器的完整流程测试
6. **Retrofit**：安卓网络请求框架
7. **BASE_URL**：网络请求的基础服务器地址

========================================

已创建 DOCS_ECS.md，涵盖完整的部署流程：

ECS 准备 — 安全组开放 8080 端口（API + WebSocket）
环境安装 — JDK 17 安装、目录创建
构建上传 — 本地 gradlew shadowJar 打包 → scp 上传
systemd 服务 — 自启动、崩溃自动重启、日志管理
API 验证 — curl 测试 REST 端点和 WebSocket
客户端配置 — 修改 RetrofitClient.BASE_URL 指向 ECS 公网 IP
数据库备份 — H2 文件备份 + MySQL 迁移路径
故障排查表 — 常见问题速查
最后附了快速命令速查卡，日常更新只需两行命令：本地打包 + scp 上传重启