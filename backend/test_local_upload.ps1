# 本地文件上传系统测试脚本
# 验证本地文件存储是否正常工作

Write-Host "================================" -ForegroundColor Cyan
Write-Host "本地文件上传系统检查" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查上传目录
Write-Host "1. 检查上传目录..." -ForegroundColor Yellow
$uploadDir = "E:\University\Henan-University-of-Technology\2025-09\courseExperimentProject\CourseDesign\backend\uploads"

if (Test-Path $uploadDir) {
    Write-Host "   ✓ uploads 目录存在" -ForegroundColor Green
    
    # 检查子目录
    $subDirs = @("properties", "contracts", "maintenance", "avatars", "others")
    foreach ($dir in $subDirs) {
        $fullPath = Join-Path $uploadDir $dir
        if (Test-Path $fullPath) {
            $fileCount = (Get-ChildItem $fullPath -File -ErrorAction SilentlyContinue).Count
            Write-Host "   ✓ $dir/ 存在 ($fileCount 个文件)" -ForegroundColor Green
        } else {
            Write-Host "   ○ $dir/ 不存在（首次上传时会自动创建）" -ForegroundColor Gray
        }
    }
} else {
    Write-Host "   ✗ uploads 目录不存在（首次上传时会自动创建）" -ForegroundColor Yellow
}

Write-Host ""

# 2. 检查配置文件
Write-Host "2. 检查配置文件..." -ForegroundColor Yellow
$configFile = "E:\University\Henan-University-of-Technology\2025-09\courseExperimentProject\CourseDesign\backend\src\main\resources\application.properties"

if (Test-Path $configFile) {
    Write-Host "   ✓ application.properties 存在" -ForegroundColor Green
    
    $config = Get-Content $configFile
    $uploadDirConfig = $config | Where-Object { $_ -match "^file.storage.upload-dir=" }
    $maxImageSize = $config | Where-Object { $_ -match "^file.storage.max-image-size=" }
    $maxDocSize = $config | Where-Object { $_ -match "^file.storage.max-document-size=" }
    
    if ($uploadDirConfig) {
        Write-Host "   ✓ $uploadDirConfig" -ForegroundColor Green
    }
    if ($maxImageSize) {
        $sizeMB = [int]($maxImageSize -replace '.*=', '') / 1MB
        Write-Host "   ✓ 图片最大大小: $sizeMB MB" -ForegroundColor Green
    }
    if ($maxDocSize) {
        $sizeMB = [int]($maxDocSize -replace '.*=', '') / 1MB
        Write-Host "   ✓ 文档最大大小: $sizeMB MB" -ForegroundColor Green
    }
}

Write-Host ""

# 3. 列出已上传的文件
Write-Host "3. 已上传的文件列表..." -ForegroundColor Yellow
if (Test-Path $uploadDir) {
    $allFiles = Get-ChildItem $uploadDir -File -Recurse
    if ($allFiles.Count -gt 0) {
        Write-Host "   找到 $($allFiles.Count) 个文件：" -ForegroundColor Green
        foreach ($file in $allFiles) {
            $sizeMB = [math]::Round($file.Length / 1MB, 2)
            $relativePath = $file.FullName.Replace($uploadDir + "\", "")
            Write-Host "   - $relativePath ($sizeMB MB)" -ForegroundColor Cyan
        }
    } else {
        Write-Host "   暂无上传的文件" -ForegroundColor Gray
    }
}

Write-Host ""

# 4. 检查后端是否运行
Write-Host "4. 检查后端服务..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method GET -TimeoutSec 2 -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "   ✓ 后端服务正在运行" -ForegroundColor Green
    }
} catch {
    Write-Host "   ✗ 后端服务未运行（端口 8080）" -ForegroundColor Red
    Write-Host "   提示: 请先启动后端服务" -ForegroundColor Yellow
}

Write-Host ""

# 5. 系统总结
Write-Host "================================" -ForegroundColor Cyan
Write-Host "系统状态总结" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "存储方式: 本地文件系统" -ForegroundColor Green
Write-Host "存储路径: $uploadDir" -ForegroundColor Green
Write-Host "访问方式: http://localhost:8080/api/files/preview/{fileName}" -ForegroundColor Green
Write-Host ""
Write-Host "这是一个完整的本地降级方案，无需云存储服务！" -ForegroundColor Green
Write-Host ""
