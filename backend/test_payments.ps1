# Test Payment Endpoints

Write-Host "=== Testing Payment Endpoints ===" -ForegroundColor Cyan

# Step 1: Login to get cookies
Write-Host "`n1. Logging in as admin..." -ForegroundColor Yellow
$loginResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"username":"admin","password":"admin123"}' `
    -SessionVariable session

Write-Host "Login successful!" -ForegroundColor Green
$user = ($loginResponse.Content | ConvertFrom-Json).user
Write-Host "Logged in as: $($user.username) (Role: $($user.roles -join ', '))" -ForegroundColor Green

# Step 2: Get all payments
Write-Host "`n2. Getting all payments..." -ForegroundColor Yellow
$paymentsResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/payments?page=0&size=5" `
    -Method GET `
    -WebSession $session

$payments = $paymentsResponse.Content | ConvertFrom-Json
Write-Host "Total payments: $($payments.totalElements)" -ForegroundColor Green
Write-Host "Current page: $($payments.currentPage + 1) of $($payments.totalPages)" -ForegroundColor Green

if ($payments.content.Count -gt 0) {
    Write-Host "`nFirst few payments:" -ForegroundColor Cyan
    $payments.content | Select-Object -First 3 | ForEach-Object {
        Write-Host "  - ID: $($_.id), Amount: ￥$($_.amount), Date: $($_.paymentDate), Method: $($_.paymentMethod)" -ForegroundColor White
    }
} else {
    Write-Host "No payments found in database." -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
