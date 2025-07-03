# 养老院AI智能食谱推荐系统 Docker 部署指南

## 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Vue Frontend  │    │ Spring Boot API │    │     MySQL       │
│   (Nginx:80)    │───▶│   (Java:8080)   │───▶│   (Port:3306)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │      Redis      │
                       │   (Port:6379)   │
                       └─────────────────┘
```

## 快速开始

### 前置要求

- Windows 10/11 或 Windows Server
- Docker Desktop for Windows
- PowerShell 5.0+
- 至少 4GB 可用内存
- 至少 10GB 可用磁盘空间

### 1. 检查 Docker 环境

```powershell
# 检查 Docker 版本
docker --version
docker-compose --version

# 确保 Docker 服务正在运行
docker info
```

### 2. 部署应用

#### 方式一：使用部署脚本（推荐）

```powershell
# 进入项目目录
cd e:\nurse_new-master\nursing-center-system

# 启动所有服务（首次需要构建镜像）
.\deploy.ps1 up -Build

# 查看服务状态
.\deploy.ps1 logs
```

#### 方式二：直接使用 Docker Compose

```powershell
# 启动所有服务
docker-compose up --build -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 3. 访问应用

- **前端应用**: http://localhost
- **后端API**: http://localhost:8080/api
- **MySQL**: localhost:3306 (用户名: , 密码: )
- **Redis**: localhost:6379

## 部署脚本说明

### deploy.ps1 命令

```powershell
# 启动服务
.\deploy.ps1 up

# 启动服务并重新构建镜像
.\deploy.ps1 up -Build

# 停止服务
.\deploy.ps1 down

# 重启服务
.\deploy.ps1 restart

# 查看日志
.\deploy.ps1 logs

# 重新构建镜像
.\deploy.ps1 build

# 清理所有资源
.\deploy.ps1 clean
```

## 开发环境

如果只需要数据库和Redis用于本地开发：

```powershell
# 启动开发环境（仅数据库和Redis）
docker-compose -f docker-compose.dev.yml up -d

# 停止开发环境
docker-compose -f docker-compose.dev.yml down
```

## 环境配置

### 环境变量

编辑 `.env` 文件来自定义配置：

```env
# 数据库配置
MYSQL_ROOT_PASSWORD=nursing123
MYSQL_DATABASE=nursing_center
MYSQL_USER=nursing
MYSQL_PASSWORD=nursing123

# Redis配置
REDIS_PASSWORD=

# 端口配置
FRONTEND_PORT=80
BACKEND_PORT=8080
MYSQL_PORT=3306
REDIS_PORT=6379
```

### 数据库初始化

确保 `nursing_center.sql` 文件在项目根目录，Docker 会自动执行初始化脚本。

## 故障排除

### 常见问题

1. **端口冲突**
   ```powershell
   # 检查端口占用
   netstat -ano | findstr :80
   netstat -ano | findstr :8080
   netstat -ano | findstr :3306
   ```

2. **内存不足**
   ```powershell
   # 检查 Docker 资源使用
   docker system df
   docker stats
   ```

3. **网络问题**
   ```powershell
   # 检查 Docker 网络
   docker network ls
   docker network inspect nursing-center-system_nursing-network
   ```

### 日志查看

```powershell
# 查看所有服务日志
docker-compose logs

# 查看特定服务日志
docker-compose logs frontend
docker-compose logs backend
docker-compose logs mysql
docker-compose logs redis

# 实时查看日志
docker-compose logs -f backend
```

### 重启单个服务

```powershell
# 重启后端服务
docker-compose restart backend

# 重启前端服务
docker-compose restart frontend
```

## 生产环境部署建议

### 安全配置

1. **更改默认密码**
   - 修改 `.env` 文件中的数据库密码
   - 设置 Redis 密码

2. **使用 HTTPS**
   - 配置 SSL 证书
   - 修改 nginx.conf 添加 HTTPS 支持

3. **限制端口访问**
   - 只暴露必要的端口（80, 443）
   - 数据库和Redis端口不对外暴露

### 性能优化

1. **调整JVM参数**
   ```dockerfile
   ENV JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC"
   ```

2. **数据库配置优化**
   - 调整 MySQL 配置参数
   - 设置适当的连接池大小

3. **Redis配置**
   - 启用持久化
   - 设置内存策略

## 数据备份

### 数据库备份

```powershell
# 创建数据库备份
docker exec nursing-mysql mysqldump -uroot -pnursing123 nursing_center > backup.sql

# 恢复数据库
docker exec -i nursing-mysql mysql -uroot -pnursing123 nursing_center < backup.sql
```

### 数据卷备份

```powershell
# 备份数据卷
docker run --rm -v nursing-center-system_mysql_data:/data -v ${PWD}:/backup alpine tar czf /backup/mysql_backup.tar.gz -C /data .

# 恢复数据卷
docker run --rm -v nursing-center-system_mysql_data:/data -v ${PWD}:/backup alpine tar xzf /backup/mysql_backup.tar.gz -C /data
```

## 更新部署

```powershell
# 更新代码后重新部署
git pull
.\deploy.ps1 down
.\deploy.ps1 up -Build
```

## 联系支持

如有问题，请检查：
1. Docker 日志：`docker-compose logs`
2. 系统资源：`docker stats`
3. 网络连接：`docker network ls`
4. 服务状态：`docker-compose ps`
