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

==================================

修改总结
默认 UI → 清新绿 (#4CAF50)
colors.xml — 将默认主题色从紫色 #6200EE 改为绿色 #4CAF50，splash 背景色同步更新
6 套内置主题色系
主题	主色
清新绿	#4CAF50
天空蓝	#2196F3
活力橙	#FF9800
优雅紫	#9C27B0
浪漫粉	#E91E63
深邃青	#009688
每套主题均有对应的暗色模式变体（在 values-night/themes.xml 中定义）。

核心架构改造
ThemeManager.kt — 新增 applyColorTheme() / getColorThemeStyle() / getColorThemeName() 等方法，管理主题色系的选择与样式资源映射

BaseActivity.kt（新建）— 所有 Activity 的统一基类，在 onCreate 前自动应用选中的主题色系和日夜间模式，同时处理语言环境包装

所有 11 个 Activity — 改为继承 BaseActivity，移除重复的 attachBaseContext 和 applyTheme 代码

全局布局/矢量图 — 将所有 @color/primary、@color/accent、@color/bottom_nav_item_color 引用替换为 ?attr/colorPrimary / ?attr/colorAccent，确保跟随主题动态变化

设置页面改造
SettingsFragment.kt 中的"主题设置"拆分为两个入口：

主题色系 — 弹出圆点色板对话框，6 种配色一目了然，点击即时切换
深色模式 — 原来的浅色/深色/跟随系统选择
注意事项
切换主题色系会触发 activity.recreate() 重建当前页面以应用新主题
SplashActivity 保持独立主题（不参与动态切换，仅展示 3 秒）
旧版紫色 UI 已完全替换为清新绿默认配色

=========================================
所有修改已完成。以下是完整的总结和使用说明。

修改总结
主界面布局重新设计
原来的主界面只有 banner 轮播 + 欢迎卡片 + 两个按钮，现在改为服务端驱动的四大板块：


┌──────────────────────────────────┐
│          工具箱          [⚙️]    │  ← Top Bar
├──────────────────────────────────┤
│   Banner 轮播（来自服务器）       │  ← 板块1：公告/Banner
│         ○ ○ ● ○                 │
├──────────────────────────────────┤
│  常用工具                        │  ← 板块2：快捷工具网格
│  ┌────┐┌────┐┌────┐┌────┐     │
│  │计数││BMI ││秒表││便签│     │     服务器控制显示哪些工具
│  └────┘└────┘└────┘└────┘     │
├──────────────────────────────────┤
│  精选推荐                        │  ← 板块3：精选推荐卡片
│  ┌ 加入社区讨论 ─────────┐      │     可点击跳转到社区/工具页
│  │ 分享你的工具使用心得    │      │
│  └───────────────────────┘      │
├──────────────────────────────────┤
│  最新通知                        │  ← 板块4：通知列表
│  ● 工具箱 v0.3.0 已上线          │
│  ● 欢迎使用工具箱                │
├──────────────────────────────────┤
│        工具箱 v0.3.0             │
└──────────────────────────────────┘
旧的 "新增公告" / "删除公告" 按钮已移除，所有内容改由服务器后台控制。

新增/修改的文件
服务端 (4 个新文件 + 2 个修改):

文件	说明
server/.../models/HomeConfig.kt	数据模型：HomeConfig, BannerItem, QuickToolItem, FeaturedCardItem, NoticeItem
server/.../repository/HomeRepository.kt	数据库层：4 张新表 + 默认种子数据
server/.../services/HomeService.kt	业务逻辑层
server/.../routes/HomeRoutes.kt	REST API 端点
server/.../plugins/Database.kt	✅ 添加了 HomeRepository.initTable()
server/.../Application.kt	✅ 注册了 HomeService 和 homeRoutes
客户端 (3 个新文件 + 4 个修改):

文件	说明
network/dto/HomeConfigDto.kt	Android 端 DTO
viewmodel/HomeViewModel.kt	主页 ViewModel
res/layout/fragment_home.xml	✅ 全新布局
fragments/HomeFragment.kt	✅ 重写为服务端驱动
fragments/BannerAdapter.kt	✅ 适配新数据模型
network/ApiService.kt	✅ 添加 getHomeConfig()
服务器后台使用说明
1. 启动服务器

cd server
./gradlew run
# 服务器运行在 http://localhost:8080
首次启动会自动创建 4 张数据库表并填充默认数据。

2. API 端点一览
获取完整主页配置（客户端调用）

curl http://localhost:8080/api/home/config
响应示例：


{
  "banners": [
    {"id":1, "title":"欢迎使用工具箱", "content":"实用工具，简单生活", "sortOrder":0},
    {"id":2, "title":"社区功能已上线", "content":"加入社区，分享你的使用心得", "sortOrder":1}
  ],
  "quickTools": [
    {"id":1, "toolId":"counter", "toolName":"计数器", "iconName":"ic_counter", "sortOrder":0, "enabled":true},
    {"id":2, "toolId":"bmi", "toolName":"BMI计算", "iconName":"ic_bmi", "sortOrder":1, "enabled":true},
    ...
  ],
  "featuredCards": [...],
  "notices": [...]
}
Banner 管理

# 查看所有 banner
curl http://localhost:8080/api/home/banners

# 批量替换 banner（会删除旧的全部替换）
curl -X POST http://localhost:8080/api/home/banners \
  -H "Content-Type: application/json" \
  -d '{
    "banners": [
      {"title": "新活动上线", "content": "立即体验全新功能", "sortOrder": 0},
      {"title": "使用技巧", "content": "长按+号可以快速增加计数", "sortOrder": 1},
      {"title": "社区热帖", "content": "看看大家都在讨论什么", "sortOrder": 2}
    ]
  }'

# 删除单个 banner
curl -X DELETE http://localhost:8080/api/home/banners/1
快捷工具管理

# 查看当前工具
curl http://localhost:8080/api/home/tools

# 批量设置工具（会删除旧的，全部替换）
curl -X POST http://localhost:8080/api/home/tools \
  -H "Content-Type: application/json" \
  -d '{
    "tools": [
      {"toolId": "counter",    "toolName": "计数器",   "iconName": "ic_counter",    "sortOrder": 0, "enabled": true},
      {"toolId": "calculator", "toolName": "计算器",   "iconName": "ic_calculator", "sortOrder": 1, "enabled": true},
      {"toolId": "stopwatch",  "toolName": "秒表",     "iconName": "ic_stopwatch",  "sortOrder": 2, "enabled": true},
      {"toolId": "bmi",        "toolName": "BMI计算",  "iconName": "ic_bmi",        "sortOrder": 3, "enabled": true},
      {"toolId": "todo",       "toolName": "待办事项", "iconName": "ic_todo",       "sortOrder": 4, "enabled": true},
      {"toolId": "notes",      "toolName": "便签",     "iconName": "ic_note",       "sortOrder": 5, "enabled": true}
    ]
  }'
可用的 toolId 值： counter, calculator, stopwatch, timer, todo, random, converter, notes, bmi, wordcount

可用的 iconName 值： ic_counter, ic_calculator, ic_stopwatch, ic_timer, ic_todo, ic_random, ic_convert, ic_note, ic_bmi, ic_word_count

精选推荐管理

# 查看
curl http://localhost:8080/api/home/featured

# 设置（批量替换）
curl -X POST http://localhost:8080/api/home/featured \
  -H "Content-Type: application/json" \
  -d '{
    "cards": [
      {"title": "加入社区讨论", "description": "分享你的使用心得", "actionType": "community", "actionValue": "", "sortOrder": 0},
      {"title": "更多工具", "description": "探索全部实用工具", "actionType": "tools", "actionValue": "", "sortOrder": 1}
    ]
  }'
actionType 可选值： "community"（跳转社区）、"tools"（跳转工具页）、""（无跳转）

通知管理

# 查看通知列表
curl http://localhost:8080/api/home/notices

# 添加通知
curl -X POST http://localhost:8080/api/home/notices \
  -H "Content-Type: application/json" \
  -d '{"title": "系统维护通知", "content": "今晚 22:00-24:00 系统维护"}'

# 删除通知
curl -X DELETE http://localhost:8080/api/home/notices/1
3. 扩展性
添加更多板块：在 HomeConfig.kt 中新增数据类 → 在 HomeRepository.kt 中新建表 → 在 HomeRoutes.kt 中添加端点 → 在 HomeFragment.kt 的 applyConfig() 中添加渲染逻辑
修改默认内容：编辑 HomeRepository.kt 中的 seedDefaultData() 方法
工具图标和跳转映射：在 HomeFragment.kt 中的 toolIconMap 和 toolActivityMap 中添加新条目

======================================

个人主页已完成重新设计。以下是所做工作的概要：

全新布局（fragment_mine.xml）
登录后视图现在是一个专业的个人中心，分为几个卡片式区域：

个人资料头部卡片 — 头像、用户名、会员徽章
数据统计面板 — 收藏/关注/粉丝/动态 四个统计项
账户信息 — 7 个可点击行（昵称、性别、生日、电话、邮箱、地址、个性签名），带图标 + 标签 + 值 + 箭头，点击后弹出编辑对话框
应用设置 — 主题色系（含颜色预览圆点）、深色模式、语言设置——均直接在标签页内通过对话框操作，无需跳转
数据管理 — 清除缓存
关于 — 检查更新、关于我们、意见反馈、隐私政策
退出登录按钮
登录流程保持不变（基于 SharedPreferences 的登录/注册，dialog_register.xml）。

新增资源
11 个矢量图标（ic_gender.xml、ic_birthday.xml、ic_phone.xml、ic_email.xml、ic_address.xml、ic_bio.xml、ic_logout.xml、ic_stat_favorites.xml、ic_stat_followers.xml、ic_stat_following.xml、ic_stat_posts.xml）
中英文各约 20 个新字符串

=====================================
版本号：app/build.gradle.kts 中 versionName → "1.0.0"
应用名称：中文和英文字符串中的 app_name、app_title 以及所有文案引用（欢迎标题、关于信息、版本文字等）均改为 雨宫莲

======================================

服务端
数据库 — users 表添加 avatar 列，home_notices 表添加 image_url 列
API — 新增 4 个上传接口：
POST /api/upload/avatar — 用户上传自己的头像（需认证）
POST /api/upload/notice-image — 管理员上传公告图片
POST /api/admin/users/{id}/avatar — 管理员修改任意用户头像
PUT /api/admin/users/{id}/info — 管理员修改用户信息
静态文件 — /uploads/ 目录存放上传的图片
管理后台
用户列表显示头像缩略图，点击「编辑」可上传头像、修改角色
公告发布支持配图上传，列表显示图片缩略图
Android 客户端
MineFragment 头像区域可点击选择图片上传
使用 Coil 库加载圆形头像
头像 URL 从服务器同步，登录后自动显示
部署到 ECS 时需要：


# 上传新的 server.jar 和 index.html
scp server/build/libs/server.jar root@47.99.34.251:/opt/toolbox-server/server.jar
scp server/static/admin/index.html root@47.99.34.251:/opt/toolbox-server/static/admin/index.html

# 在 ECS 上创建 uploads 目录
ssh root@47.99.34.251 "mkdir -p /opt/toolbox-server/static/uploads && systemctl restart toolbox-server"