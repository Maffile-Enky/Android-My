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