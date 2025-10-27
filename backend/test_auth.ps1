# Test Auth Status
$authUrl = "http://localhost:8080/api/auth/me"
$loginUrl = "http://localhost:8080/api/auth/login"

Write-Host "Testing auth status..." -ForegroundColor Cyan

# Test if user is logged in
try {
    $response = Invoke-WebRequest -Uri $authUrl -UseBasicParsing -SessionVariable session
    Write-Host "User is logged in" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Green
} catch {
    Write-Host "User is NOT logged in (401)" -ForegroundColor Yellow
    Write-Host "`nTrying to login with test credentials..." -ForegroundColor Cyan
    
    # Try to login
    $loginBody = @{
        username = "admin"
        password = "admin123"
    } | ConvertTo-Json
    
    try {
        $loginResponse = Invoke-WebRequest -Uri $loginUrl -Method POST -Body $loginBody -ContentType "application/json" -SessionVariable session -UseBasicParsing
        Write-Host "Login successful!" -ForegroundColor Green
        
        # Now test AI chat with session
        Write-Host "`nTesting AI chat with logged-in session..." -ForegroundColor Cyan
        $chatUrl = "http://localhost:8080/api/ai/chat"
        $chatBody = @{
            message = "你好"
            history = @()
        } | ConvertTo-Json
        
        $chatResponse = Invoke-WebRequest -Uri $chatUrl -Method POST -Body $chatBody -ContentType "application/json" -WebSession $session -UseBasicParsing
        Write-Host "AI Chat Success!" -ForegroundColor Green
        Write-Host "Response: $($chatResponse.Content)" -ForegroundColor Green
        
    } catch {
        Write-Host "Login failed!" -ForegroundColor Red
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}
