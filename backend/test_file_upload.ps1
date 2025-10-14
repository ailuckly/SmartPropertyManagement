# 文件上传功能快速测试脚本
# 测试文件上传API是否正常工作

Write-Host "=== 文件上传功能测试 ===" -ForegroundColor Green
Write-Host ""

# 1. 检查后端是否运行
Write-Host "1. 检查后端服务..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/me" -Method GET -ErrorAction Stop
    Write-Host "   ✓ 后端服务正常运行" -ForegroundColor Green
} catch {
    Write-Host "   ✗ 后端服务未运行或无法访问" -ForegroundColor Red
    Write-Host "   请确保后端已启动: ./mvnw spring-boot:run" -ForegroundColor Yellow
    exit
}

# 2. 检查uploads目录
Write-Host ""
Write-Host "2. 检查uploads目录..." -ForegroundColor Yellow
if (Test-Path "./uploads") {
    Write-Host "   ✓ uploads目录存在" -ForegroundColor Green
} else {
    Write-Host "   ✗ uploads目录不存在，正在创建..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path "./uploads" -Force | Out-Null
    Write-Host "   ✓ uploads目录已创建" -ForegroundColor Green
}

# 3. 测试登录
Write-Host ""
Write-Host "3. 测试登录..." -ForegroundColor Yellow
$loginBody = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -SessionVariable session `
        -ErrorAction Stop
    
    Write-Host "   ✓ 登录成功" -ForegroundColor Green
    
    # 提取Cookie
    $cookies = $loginResponse.Headers['Set-Cookie']
    Write-Host "   认证Cookie已获取" -ForegroundColor Gray
    
} catch {
    Write-Host "   ✗ 登录失败" -ForegroundColor Red
    Write-Host "   错误: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# 4. 创建测试图片文件
Write-Host ""
Write-Host "4. 创建测试图片..." -ForegroundColor Yellow
$testImagePath = "./test-image.txt"
"This is a test file for upload testing" | Out-File -FilePath $testImagePath -Encoding UTF8
Write-Host "   ✓ 测试文件已创建: $testImagePath" -ForegroundColor Green

# 5. 测试文件上传
Write-Host ""
Write-Host "5. 测试文件上传API..." -ForegroundColor Yellow
try {
    # 构建multipart/form-data请求
    $boundary = [System.Guid]::NewGuid().ToString()
    $fileBinary = [System.IO.File]::ReadAllBytes($testImagePath)
    
    $bodyLines = @(
        "--$boundary",
        "Content-Disposition: form-data; name=`"file`"; filename=`"test.txt`"",
        "Content-Type: text/plain",
        "",
        [System.Text.Encoding]::UTF8.GetString($fileBinary),
        "--$boundary",
        "Content-Disposition: form-data; name=`"category`"",
        "",
        "OTHER",
        "--$boundary--"
    )
    
    $body = $bodyLines -join "`r`n"
    
    $headers = @{
        "Content-Type" = "multipart/form-data; boundary=$boundary"
    }
    
    # 从session中获取cookie
    if ($session.Cookies) {
        $cookieContainer = $session.Cookies.GetCookies("http://localhost:8080")
        $cookieHeader = ($cookieContainer | ForEach-Object { "$($_.Name)=$($_.Value)" }) -join "; "
        $headers["Cookie"] = $cookieHeader
    }
    
    $uploadResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/files/upload" `
        -Method POST `
        -Headers $headers `
        -Body $body `
        -WebSession $session `
        -ErrorAction Stop
    
    Write-Host "   ✓ 文件上传成功!" -ForegroundColor Green
    Write-Host "   响应: $($uploadResponse.Content)" -ForegroundColor Gray
    
} catch {
    Write-Host "   ✗ 文件上传失败" -ForegroundColor Red
    Write-Host "   状态码: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "   错误: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.ErrorDetails.Message) {
        Write-Host "   详细信息: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

# 6. 清理
Write-Host ""
Write-Host "6. 清理测试文件..." -ForegroundColor Yellow
if (Test-Path $testImagePath) {
    Remove-Item $testImagePath -Force
    Write-Host "   ✓ 测试文件已删除" -ForegroundColor Green
}

Write-Host ""
Write-Host "=== 测试完成 ===" -ForegroundColor Green
Write-Host ""
Write-Host "如果上传成功，现在可以在前端测试文件上传功能了！" -ForegroundColor Cyan
Write-Host "访问: http://localhost:5173/file-upload-test" -ForegroundColor Cyan
