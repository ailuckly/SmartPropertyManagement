# AI配置诊断脚本

Write-Host "=== AI配置诊断 ===" -ForegroundColor Cyan

# 1. 检查后端是否运行
Write-Host "`n1. 检查后端服务..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -ErrorAction SilentlyContinue
    Write-Host "   ✓ 后端正在运行" -ForegroundColor Green
} catch {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/properties" -ErrorAction SilentlyContinue
    if ($response) {
        Write-Host "   ✓ 后端正在运行" -ForegroundColor Green
    } else {
        Write-Host "   ✗ 后端未运行，请先启动后端" -ForegroundColor Red
        exit
    }
}

# 2. 检查AI端点是否存在
Write-Host "`n2. 检查AI聊天端点..." -ForegroundColor Yellow
try {
    # 不带token测试（应该返回401或其他错误，但不是404）
    Invoke-RestMethod -Uri "http://localhost:8080/api/ai/chat" -Method Post -Headers @{"Content-Type"="application/json"} -Body '{"message":"test"}' -ErrorAction Stop
    Write-Host "   ✓ AI端点存在" -ForegroundColor Green
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 404) {
        Write-Host "   ✗ AI端点不存在（404）" -ForegroundColor Red
        Write-Host "   原因：AIChatController未创建，可能是 app.ai.enabled=false 或Bean初始化失败" -ForegroundColor Yellow
    } elseif ($statusCode -eq 401 -or $statusCode -eq 403) {
        Write-Host "   ✓ AI端点存在（需要认证）" -ForegroundColor Green
    } elseif ($statusCode -eq 500) {
        Write-Host "   ? AI端点存在但出现500错误" -ForegroundColor Yellow
    } else {
        Write-Host "   ? 未知状态码：$statusCode" -ForegroundColor Yellow
    }
}

# 3. 读取配置文件
Write-Host "`n3. 检查配置文件..." -ForegroundColor Yellow
$propsFile = "src/main/resources/application.properties"
if (Test-Path $propsFile) {
    $content = Get-Content $propsFile -Raw
    
    # 检查AI是否启用
    if ($content -match 'app\.ai\.enabled=\$\{AI_ENABLED:(\w+)\}') {
        $defaultEnabled = $matches[1]
        Write-Host "   app.ai.enabled 默认值: $defaultEnabled" -ForegroundColor Cyan
    }
    
    # 检查API Key
    if ($content -match 'app\.ai\.api-key=\$\{OPENAI_API_KEY:([^}]+)\}') {
        $apiKey = $matches[1]
        $maskedKey = $apiKey.Substring(0, [Math]::Min(10, $apiKey.Length)) + "..."
        Write-Host "   API Key: $maskedKey" -ForegroundColor Cyan
    }
    
    # 检查Base URL
    if ($content -match 'app\.ai\.base-url=\$\{OPENAI_BASE_URL:([^}]+)\}') {
        $baseUrl = $matches[1]
        Write-Host "   Base URL: $baseUrl" -ForegroundColor Cyan
    }
    
    # 检查Model
    if ($content -match 'app\.ai\.model=\$\{OPENAI_MODEL:([^}]+)\}') {
        $model = $matches[1]
        Write-Host "   Model: $model" -ForegroundColor Cyan
    }
} else {
    Write-Host "   ✗ 配置文件不存在" -ForegroundColor Red
}

# 4. 检查环境变量
Write-Host "`n4. 检查环境变量..." -ForegroundColor Yellow
$envVars = @("AI_ENABLED", "OPENAI_API_KEY", "OPENAI_BASE_URL", "OPENAI_MODEL")
foreach ($var in $envVars) {
    $value = [Environment]::GetEnvironmentVariable($var)
    if ($value) {
        if ($var -eq "OPENAI_API_KEY") {
            $value = $value.Substring(0, [Math]::Min(10, $value.Length)) + "..."
        }
        Write-Host "   $var = $value" -ForegroundColor Cyan
    } else {
        Write-Host "   $var = (未设置)" -ForegroundColor Gray
    }
}

Write-Host "`n=== 诊断完成 ===" -ForegroundColor Cyan
Write-Host "`n建议：" -ForegroundColor Yellow
Write-Host "1. 如果AI端点返回404，说明Controller未创建" -ForegroundColor White
Write-Host "2. 检查后端启动日志中是否有 '初始化OpenAI服务' 的日志" -ForegroundColor White
Write-Host "3. 检查是否有Bean创建失败的错误" -ForegroundColor White
Write-Host "4. 确认 app.ai.enabled=true" -ForegroundColor White
