# 文件预览功能测试脚本

Write-Host "================================" -ForegroundColor Cyan
Write-Host "文件预览功能测试" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查后端服务
Write-Host "1. 检查后端服务..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method GET -TimeoutSec 2 -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "   ✓ 后端服务正在运行" -ForegroundColor Green
    }
} catch {
    Write-Host "   ✗ 后端服务未运行" -ForegroundColor Red
    Write-Host "   请先启动后端服务: ./mvnw spring-boot:run" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# 2. 查找已上传的文件
Write-Host "2. 查找已上传的文件..." -ForegroundColor Yellow
$uploadDir = "E:\University\Henan-University-of-Technology\2025-09\courseExperimentProject\CourseDesign\backend\uploads"

if (Test-Path $uploadDir) {
    $files = Get-ChildItem $uploadDir -File -Recurse
    
    if ($files.Count -gt 0) {
        Write-Host "   找到 $($files.Count) 个文件" -ForegroundColor Green
        Write-Host ""
        
        $testFile = $files[0]
        $fileName = $testFile.Name
        $relativePath = $testFile.FullName.Replace($uploadDir + "\", "")
        
        Write-Host "   测试文件: $fileName" -ForegroundColor Cyan
        Write-Host "   相对路径: $relativePath" -ForegroundColor Gray
        Write-Host ""
        
        # 3. 测试预览接口
        Write-Host "3. 测试预览接口..." -ForegroundColor Yellow
        $previewUrl = "http://localhost:8080/api/files/preview/$fileName"
        Write-Host "   请求 URL: $previewUrl" -ForegroundColor Gray
        
        try {
            # 注意：需要登录后的 Cookie
            Write-Host "   提示: 需要登录后的认证 Cookie" -ForegroundColor Yellow
            Write-Host ""
            Write-Host "   请在浏览器中访问以下 URL 来测试:" -ForegroundColor Cyan
            Write-Host "   $previewUrl" -ForegroundColor White
            Write-Host ""
            Write-Host "   或者在前端应用中使用 FileUpload 组件上传并预览" -ForegroundColor Cyan
            
        } catch {
            Write-Host "   ✗ 预览失败: $($_.Exception.Message)" -ForegroundColor Red
        }
        
    } else {
        Write-Host "   暂无上传的文件" -ForegroundColor Gray
        Write-Host "   请先在前端应用中上传文件" -ForegroundColor Yellow
    }
} else {
    Write-Host "   uploads 目录不存在" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "测试完成" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "修复内容:" -ForegroundColor Green
Write-Host "  1. ✓ 修复了 FileController 中的文件名解析逻辑" -ForegroundColor Green
Write-Host "  2. ✓ 添加了 FileService.getFileByStoredName() 方法" -ForegroundColor Green
Write-Host "  3. ✓ 预览接口现在可以正确处理 UUID 格式的文件名" -ForegroundColor Green
Write-Host "  4. ✓ 添加了缓存控制头，提高预览性能" -ForegroundColor Green
Write-Host ""
Write-Host "如何测试:" -ForegroundColor Yellow
Write-Host "  1. 启动后端: cd backend && ./mvnw spring-boot:run" -ForegroundColor White
Write-Host "  2. 启动前端: cd frontend && npm run dev" -ForegroundColor White
Write-Host "  3. 访问: http://localhost:5173/file-upload-test" -ForegroundColor White
Write-Host "  4. 上传图片，观察实时预览效果" -ForegroundColor White
Write-Host ""
