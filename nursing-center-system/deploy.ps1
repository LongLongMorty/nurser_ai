# 养老院管理系统 Docker 部署脚本
# PowerShell 脚本，适用于 Windows 环境

param(
    [string]$Action = "up",
    [switch]$Build = $false,
    [switch]$Logs = $false
)

Write-Host "=== 养老院AI智能食谱推荐系统 Docker 部署脚本 ===" -ForegroundColor Green

# 检查 Docker 是否安装
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "错误: Docker 未安装或未添加到 PATH" -ForegroundColor Red
    exit 1
}

# 检查 Docker Compose 是否可用
if (-not (Get-Command docker-compose -ErrorAction SilentlyContinue)) {
    Write-Host "错误: Docker Compose 未安装" -ForegroundColor Red
    exit 1
}

switch ($Action) {
    "up" {
        Write-Host "启动服务..." -ForegroundColor Yellow
        if ($Build) {
            docker-compose up --build -d
        } else {
            docker-compose up -d
        }
        
        Write-Host "等待服务启动..." -ForegroundColor Yellow
        Start-Sleep -Seconds 10
        
        Write-Host "检查服务状态..." -ForegroundColor Yellow
        docker-compose ps
        
        Write-Host ""
        Write-Host "=== 服务访问地址 ===" -ForegroundColor Green
        Write-Host "前端应用: http://localhost" -ForegroundColor Cyan
        Write-Host "后端API: http://localhost:8080/api" -ForegroundColor Cyan
        Write-Host "MySQL:   localhost:3306" -ForegroundColor Cyan
        Write-Host "Redis:   localhost:6379" -ForegroundColor Cyan
    }
    
    "down" {
        Write-Host "停止服务..." -ForegroundColor Yellow
        docker-compose down
    }
    
    "restart" {
        Write-Host "重启服务..." -ForegroundColor Yellow
        docker-compose restart
    }
    
    "logs" {
        docker-compose logs -f
    }
    
    "build" {
        Write-Host "重新构建镜像..." -ForegroundColor Yellow
        docker-compose build --no-cache
    }
    
    "clean" {
        Write-Host "清理 Docker 资源..." -ForegroundColor Yellow
        docker-compose down -v --remove-orphans
        docker system prune -f
    }
    
    default {
        Write-Host "用法: .\deploy.ps1 [up|down|restart|logs|build|clean] [-Build] [-Logs]" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "命令说明:" -ForegroundColor Green
        Write-Host "  up      - 启动所有服务"
        Write-Host "  down    - 停止所有服务"
        Write-Host "  restart - 重启所有服务"
        Write-Host "  logs    - 查看日志"
        Write-Host "  build   - 重新构建镜像"
        Write-Host "  clean   - 清理所有 Docker 资源"
        Write-Host ""
        Write-Host "选项:" -ForegroundColor Green
        Write-Host "  -Build  - 在启动时重新构建镜像"
        Write-Host "  -Logs   - 启动后显示日志"
    }
}

if ($Logs -and $Action -eq "up") {
    Write-Host ""
    Write-Host "显示服务日志..." -ForegroundColor Yellow
    docker-compose logs -f
}
