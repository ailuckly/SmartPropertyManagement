# 测试AI聊天接口
$token = "YOUR_JWT_TOKEN_HERE"

$body = @{
    message = "你好，系统有哪些功能？"
    history = @()
} | ConvertTo-Json -Depth 10

Write-Host "发送AI聊天请求..." -ForegroundColor Yellow
Write-Host "请求体: $body" -ForegroundColor Cyan

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/ai/chat" `
        -Method Post `
        -Headers @{
            "Authorization" = "Bearer $token"
            "Content-Type" = "application/json"
        } `
        -Body $body `
        -ErrorAction Stop
    
    Write-Host "`n✓ 请求成功!" -ForegroundColor Green
    Write-Host "AI回复: $($response.reply)" -ForegroundColor White
} catch {
    Write-Host "`n✗ 请求失败!" -ForegroundColor Red
    Write-Host "状态码: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    Write-Host "错误信息: $($_.Exception.Message)" -ForegroundColor Red
    
    # 尝试读取响应体
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应体: $responseBody" -ForegroundColor Yellow
    }
}
