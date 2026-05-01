# 管理后台 + 用户系统 + 版本更新 — 部署指南

## 架构概览

```
Android App ── HTTP/WS ──► Nginx (80/443) ──► Ktor Server (8080)
                                                    ├── H2 数据库
                                                    ├── /api/auth/*     用户认证 (JWT)
                                                    ├── /api/admin/*    管理员 API
                                                    ├── /api/version/*  版本检查
                                                    ├── /admin/         Web 管理后台
                                                    └── /files/         APK 下载目录
```

## 1. 服务端部署

### 1.1 构建 JAR

```bash
cd server
./gradlew shadowJar
# 产出: server/build/libs/server.jar
```

### 1.2 上传到 ECS

```bash
# 上传 JAR
scp build/libs/server.jar root@<ECS_IP>:/opt/toolbox-server/server.jar

# 上传管理后台静态文件
scp -r static/ root@<ECS_IP>:/opt/toolbox-server/static/
```

### 1.3 目录结构

在 ECS 上确保以下目录结构：

```
/opt/toolbox-server/
├── server.jar
├── data/                    # H2 数据库自动创建
│   └── toolbox.mv.db
└── static/
    ├── admin/
    │   └── index.html       # 管理后台页面
    └── files/
        └── app-v1.1.0.apk  # APK 文件（手动上传）
```

### 1.4 systemd 服务

```bash
cat > /etc/systemd/system/toolbox-server.service << 'EOF'
[Unit]
Description=Toolbox Server
After=network.target

[Service]
Type=simple
WorkingDirectory=/opt/toolbox-server
ExecStart=/usr/bin/java -Xmx256m -Xms128m -jar server.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable toolbox-server
systemctl start toolbox-server
```

## 2. Nginx 反向代理配置

### 2.1 安装 Nginx

```bash
# Ubuntu/Debian
apt update && apt install -y nginx

# CentOS
yum install -y nginx
```

### 2.2 配置反向代理

```bash
cat > /etc/nginx/sites-available/toolbox << 'EOF'
server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名或 IP

    client_max_body_size 100m;    # APK 上传大小限制

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket 支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 86400;
    }
}
EOF

# Ubuntu/Debian: 启用配置
ln -sf /etc/nginx/sites-available/toolbox /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default

# 测试并重启
nginx -t && systemctl restart nginx
```

### 2.3 HTTPS 配置（可选，推荐）

```bash
# 安装 certbot
apt install -y certbot python3-certbot-nginx

# 申请证书
certbot --nginx -d your-domain.com

# 自动续期
echo "0 3 * * * certbot renew --quiet" | crontab -
```

## 3. 防火墙配置

```bash
# 关闭 8080 端口的公网访问（只允许 Nginx 通过 localhost 访问）
# 阿里云安全组：移除 8080 端口的入站规则

# 只开放 80 和 443
# 阿里云安全组添加：
# | 入方向 | 80  | TCP | 0.0.0.0/0 | HTTP  |
# | 入方向 | 443 | TCP | 0.0.0.0/0 | HTTPS |
```

## 4. 管理后台使用

### 4.1 访问地址

```
http://your-domain.com/admin/
```

### 4.2 默认管理员账户

| 用户名 | 密码 | 角色 |
|--------|------|------|
| root   | root | admin |

> **安全提醒**：首次登录后请立即修改默认密码！

### 4.3 功能模块

| 模块 | 说明 |
|------|------|
| 仪表盘 | 显示用户数、帖子数、横幅数等统计信息 |
| 横幅管理 | 管理首页轮播横幅 |
| 快捷工具 | 管理首页快捷工具入口 |
| 精选卡片 | 管理首页精选推荐卡片 |
| 公告管理 | 发布和管理系统公告 |
| 帖子管理 | 查看和删除社区帖子（管理员可删除任意帖子） |
| 用户管理 | 查看用户列表、修改角色、删除用户 |
| 版本管理 | 发布新版本、设置更新日志和 APK 地址 |

## 5. 版本更新流程

### 5.1 服务端发布新版本

1. 在管理后台 → 版本管理 → 点击「发布新版本」
2. 填写版本号（如 `2`）、版本名（如 `1.1.0`）、更新日志
3. APK 地址填写 `/files/app-v1.1.0.apk`
4. 可选勾选「强制更新」

### 5.2 上传 APK 文件

```bash
# 将 APK 上传到服务器的 static/files/ 目录
scp app-v1.1.0.apk root@<ECS_IP>:/opt/toolbox-server/static/files/

# 重启服务使文件生效
systemctl restart toolbox-server
```

### 5.3 客户端更新流程

```
用户点击「检查更新」
    → 调用 GET /api/version/check?versionCode=1
    → 服务端比较版本号
    → 如有新版本，弹出更新对话框（显示版本名 + 更新日志）
    → 用户点击「立即更新」
    → 通过 DownloadManager 下载 APK
    → 自动触发安装
```

## 6. API 接口参考

### 6.1 认证接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/auth/register` | 用户注册 | 公开 |
| POST | `/api/auth/login` | 用户登录 | 公开 |
| GET | `/api/auth/me` | 获取当前用户信息 | 需要 token |

### 6.2 管理员接口

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/admin/users` | 用户列表 | 管理员 |
| DELETE | `/api/admin/users/{id}` | 删除用户 | 管理员 |
| PUT | `/api/admin/users/{id}/role` | 修改角色 | 管理员 |
| GET | `/api/admin/versions` | 版本列表 | 管理员 |
| POST | `/api/admin/versions` | 发布版本 | 管理员 |
| DELETE | `/api/admin/versions/{id}` | 删除版本 | 管理员 |

### 6.3 版本接口（公开）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/version/check?versionCode=X` | 检查更新 |
| GET | `/api/version/latest` | 获取最新版本 |

### 6.4 需要管理员权限的写操作

以下原有接口的写操作现在需要管理员 token：

- `POST /api/home/banners` — 保存横幅
- `DELETE /api/home/banners/{id}` — 删除横幅
- `POST /api/home/tools` — 保存工具
- `POST /api/home/featured` — 保存精选卡片
- `POST /api/home/notices` — 发布公告
- `DELETE /api/home/notices/{id}` — 删除公告
- `DELETE /api/posts/{id}` — 删除帖子（管理员可删任意帖子）

请求头格式：`Authorization: Bearer <jwt_token>`

## 7. 数据库说明

### 7.1 新增表

| 表名 | 说明 |
|------|------|
| `users` | 用户表（id, username, password_hash, role, created_at）|
| `app_versions` | 版本表（id, version_code, version_name, changelog, apk_url, force_update, created_at）|

### 7.2 默认数据

- **root 用户**：首次启动自动创建，密码使用 BCrypt 哈希存储
- **v1.0.0 版本**：首次启动自动创建初始版本记录

## 8. 常见问题

| 问题 | 解决方案 |
|------|----------|
| 管理后台 404 | 检查 `static/admin/` 目录是否存在，重启服务 |
| 登录提示"令牌无效" | 清除浏览器 localStorage，重新登录 |
| APK 下载失败 | 检查 `static/files/` 目录权限和文件是否存在 |
| Nginx 502 错误 | 检查 Ktor 服务是否运行：`systemctl status toolbox-server` |
| WebSocket 断连 | 确保 Nginx 配置了 WebSocket 升级头 |
| 无法删除 root 用户 | 这是预期行为，默认管理员不可删除 |
