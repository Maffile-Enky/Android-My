# 社区服务 — 阿里云 ECS 部署指南

## 架构概览

```
Android App ── HTTP/WS ──► ECS (CentOS/Ubuntu)
                              ├── Ktor Server (port 8080)
                              ├── H2 数据库 (./data/toolbox.mv.db)
                              └── systemd 守护进程 (自动重启)
```

## 1. ECS 实例准备

### 1.1 购买建议
- **地域**：选择离用户最近的区域
- **规格**：1 vCPU + 2 GiB 内存即可（社区服务很轻量）
- **系统**：CentOS 7.9 / Ubuntu 22.04 / Alibaba Cloud Linux 3
- **带宽**：按量付费，1-5 Mbps

### 1.2 安全组配置（关键）

登录 [阿里云控制台](https://ecs.console.aliyun.com/) → **网络与安全 → 安全组** → 添加规则：

| 方向 | 端口 | 协议 | 授权对象 | 说明 |
|------|------|------|----------|------|
| 入方向 | 8080 | TCP | 0.0.0.0/0 | 社区 API + WebSocket |
| 入方向 | 22 | TCP | 你的IP/32 | SSH 远程管理 |

> 生产环境建议将 8080 的授权对象限制为你的业务 IP 段，或用 Nginx 反代 + HTTPS。

### 1.3 SSH 连接

```bash
ssh root@<你的ECS公网IP>
```

---

## 2. 环境安装

### 2.1 安装 JDK 17

**CentOS / Alibaba Cloud Linux:**
```bash
sudo yum install -y java-17-openjdk-devel
java -version
```

**Ubuntu:**
```bash
sudo apt update
sudo apt install -y openjdk-17-jdk-headless
java -version
```

### 2.2 创建目录结构

```bash
mkdir -p /opt/toolbox-server/data
```

---

## 3. 构建并上传服务端

### 3.1 本地构建 fat JAR

在项目根目录执行（需本机安装 JDK 17 + Gradle）：

```bash
cd server
../gradlew shadowJar
```

构建产物：`server/build/libs/server.jar`（约 20-30MB，包含所有依赖）

### 3.2 上传到 ECS

```bash
scp server/build/libs/server.jar root@<ECS_IP>:/opt/toolbox-server/
```

### 3.3 首次试运行

```bash
ssh root@<ECS_IP>
cd /opt/toolbox-server
java -jar server.jar
```

看到 `Responding at http://0.0.0.0:8080` 说明启动成功。按 `Ctrl+C` 停止。

---

## 4. 配置 systemd 自启动

### 4.1 创建服务文件

```bash
sudo vi /etc/systemd/system/toolbox-server.service
```

写入以下内容：

```ini
[Unit]
Description=Toolbox Community Server
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/toolbox-server
ExecStart=/usr/bin/java -jar /opt/toolbox-server/server.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

# JVM 参数（小内存优化）
Environment="JAVA_OPTS=-Xmx256m -Xms128m"

[Install]
WantedBy=multi-user.target
```

### 4.2 启动服务

```bash
sudo systemctl daemon-reload
sudo systemctl enable toolbox-server
sudo systemctl start toolbox-server
sudo systemctl status toolbox-server
```

### 4.3 常用管理命令

```bash
sudo systemctl status toolbox-server   # 查看状态
sudo systemctl restart toolbox-server  # 重启
sudo systemctl stop toolbox-server     # 停止
sudo journalctl -u toolbox-server -f   # 实时日志
```

---

## 5. API 端点总览

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/posts?page=0&size=20` | 帖子列表（分页） |
| `GET` | `/api/posts/{id}` | 帖子详情 |
| `POST` | `/api/posts` | 发帖 `{author, title, content}` |
| `POST` | `/api/posts/{id}/like` | 点赞 |
| `DELETE` | `/api/posts/{id}?author=xxx` | 删帖（作者校验） |
| `GET` | `/api/chat/messages?user=A&target=B` | 聊天历史 |
| `POST` | `/api/chat/send` | 发送消息 `{fromUser, toUser, content}` |
| `WS` | `/ws` | 帖子实时事件（new_post/post_liked/post_deleted） |
| `WS` | `/ws/chat?user=xxx` | 聊天实时推送 |

## 6. 验证服务端 API

### 6.1 帖子功能

```bash
# 获取帖子列表（首次为空）
curl http://localhost:8080/api/posts
# 返回: {"posts":[],"total":0}

# 创建测试帖子
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"author":"测试用户","title":"你好社区","content":"ECS 部署成功！"}'

# 点赞
curl -X POST http://localhost:8080/api/posts/1/like
```

### 6.2 聊天功能

```bash
# 发送聊天消息
curl -X POST http://localhost:8080/api/chat/send \
  -H "Content-Type: application/json" \
  -d '{"fromUser":"小明","toUser":"测试用户","content":"你好！"}'

# 获取聊天历史
curl "http://localhost:8080/api/chat/messages?user=小明&target=测试用户"
# 返回: {"messages":[{...}]}

# 测试 WebSocket 聊天（用 wscat 或 websocat）
wscat -c "ws://localhost:8080/ws/chat?user=小明"
```

### 6.3 本地 PC 测试（用 ECS 公网 IP）

```bash
curl http://<ECS公网IP>:8080/api/posts
curl "http://<ECS公网IP>:8080/api/chat/messages?user=小明&target=测试用户"
```

> 如果本地 PC 无法访问，检查安全组是否已开放 8080 端口。

---

## 6. 更新 Android 客户端连接地址

编辑文件：[app/.../network/RetrofitClient.kt](app/src/main/java/com/example/jjjjjjppppp/network/RetrofitClient.kt)

```kotlin
object RetrofitClient {
    // 改为你的 ECS 公网 IP
    var BASE_URL = "http://<ECS公网IP>:8080/"

    // 开发时可选：从 SharedPreferences 读取，方便切换环境
    // var BASE_URL = prefs.getString("server_url", "http://10.0.2.2:8080/")!!
}
```

> 模拟器用 `10.0.2.2` 访问宿主机，真机必须用 ECS 公网 IP。

---

## 7. 数据库说明

- **引擎**：H2 嵌入式数据库（零配置，开箱即用）
- **文件位置**：`/opt/toolbox-server/data/toolbox.mv.db`
- **备份**：定期备份该文件即可

```bash
# 备份脚本示例（可加入 crontab）
cp /opt/toolbox-server/data/toolbox.mv.db /opt/toolbox-server/data/toolbox_$(date +%Y%m%d).mv.db
```

后期如需迁移到 MySQL：修改 [Database.kt](server/src/main/kotlin/com/toolbox/plugins/Database.kt) 的连接字符串，加 MySQL JDBC 驱动依赖即可。

---

## 8. 防火墙（可选）

如果 ECS 上启用了 firewalld：

```bash
sudo firewall-cmd --zone=public --add-port=8080/tcp --permanent
sudo firewall-cmd --reload
```

Ubuntu ufw：

```bash
sudo ufw allow 8080/tcp
```

---

## 9. 故障排查

| 现象 | 排查步骤 |
|------|----------|
| 服务启动后立即退出 | `journalctl -u toolbox-server -n 50` 查看错误日志 |
| 端口被占用 | `lsof -i :8080` 查看占用进程 |
| 客户端连不上 | 1. 检查安全组 2. `curl localhost:8080/api/posts` 确认本地可用 3. 检查公网 IP 是否正确 |
| WebSocket 断连 | 客户端 onPause 时会主动断开，检查 Fragment 生命周期 |
| 内存不足 | JVM 参数已限制 256m，如果不够可改为 `-Xmx512m` |

---

## 快速命令速查

```bash
# === 首次部署 ===
scp server.jar root@<IP>:/opt/toolbox-server/
scp toolbox-server.service root@<IP>:/etc/systemd/system/
ssh root@<IP>
sudo systemctl daemon-reload
sudo systemctl enable --now toolbox-server

# === 更新 JAR ===
# 本地执行
../gradlew :server:shadowJar
scp server/build/libs/server.jar root@<IP>:/opt/toolbox-server/
# 远程执行
ssh root@<IP> "sudo systemctl restart toolbox-server"

# === 查看日志 ===
ssh root@<IP> "sudo journalctl -u toolbox-server -f"
```
