# Complete test: Login + AI Chat
$baseUrl = "http://localhost:8080/api"

Write-Host "=== Testing Complete Flow ===" -ForegroundColor Cyan

# Step 1: Login
Write-Host "`n1. Attempting login..." -ForegroundColor Yellow
$loginUrl = "$baseUrl/auth/login"
$loginBody = @{
    username = "admin"
    password = "123456"
} | ConvertTo-Json

try {
    $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $loginResponse = Invoke-WebRequest -Uri $loginUrl -Method POST -Body $loginBody -ContentType "application/json" -WebSession $session -UseBasicParsing
    Write-Host "✓ Login successful" -ForegroundColor Green
    Write-Host "Cookies: $($session.Cookies.GetCookies($loginUrl).Count)" -ForegroundColor Gray
    
    # Step 2: Test AI Chat
    Write-Host "`n2. Testing AI Chat..." -ForegroundColor Yellow
    $chatUrl = "$baseUrl/ai/chat"
    $chatBody = @{
        message = "你好"
        history = @()
    } | ConvertTo-Json
    
    try {
        $chatResponse = Invoke-WebRequest -Uri $chatUrl -Method POST -Body $chatBody -ContentType "application/json" -WebSession $session -UseBasicParsing
        Write-Host "✓ AI Chat successful!" -ForegroundColor Green
        $responseData = $chatResponse.Content | ConvertFrom-Json
        Write-Host "`nAI Reply:" -ForegroundColor Cyan
        Write-Host $responseData.reply -ForegroundColor White
        
    } catch {
        Write-Host "✗ AI Chat failed" -ForegroundColor Red
        Write-Host "Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        
        # Try to get response body
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorBody = $reader.ReadToEnd()
            Write-Host "`nError Response:" -ForegroundColor Red
            Write-Host $errorBody -ForegroundColor Red
        } catch {
            Write-Host "Could not read error response" -ForegroundColor Gray
        }
    }
    
} catch {
    Write-Host "✗ Login failed" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    
    # Try common credentials
    Write-Host "`nTrying alternative credentials..." -ForegroundColor Yellow
    
    $credentials = @(
        @{username="admin"; password="admin123"},
        @{username="admin"; password="123456"},
        @{username="root"; password="root"}
    )
    
    foreach ($cred in $credentials) {
        Write-Host "Trying: $($cred.username)/$($cred.password)" -ForegroundColor Gray
        $testBody = $cred | ConvertTo-Json
        try {
            $testResponse = Invoke-WebRequest -Uri $loginUrl -Method POST -Body $testBody -ContentType "application/json" -UseBasicParsing -ErrorAction Stop
            Write-Host "✓ Success with $($cred.username)/$($cred.password)" -ForegroundColor Green
            break
        } catch {
            Write-Host "✗ Failed" -ForegroundColor Red
        }
    }
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
