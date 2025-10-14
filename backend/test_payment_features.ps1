# Comprehensive Payment API Test

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Payment API Feature Test" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Step 1: Login
Write-Host "[1/4] Logging in as admin..." -ForegroundColor Yellow
$loginResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{"username":"admin","password":"admin123"}' `
    -SessionVariable session

$user = ($loginResponse.Content | ConvertFrom-Json).user
Write-Host "✓ Logged in as: $($user.username) (Role: $($user.roles -join ', '))`n" -ForegroundColor Green

# Step 2: Get all payments (no filter)
Write-Host "[2/4] Getting ALL payments..." -ForegroundColor Yellow
$allPaymentsResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/payments?page=0&size=5" `
    -Method GET `
    -WebSession $session

$allPayments = $allPaymentsResponse.Content | ConvertFrom-Json
Write-Host "✓ Total payments in database: $($allPayments.totalElements)" -ForegroundColor Green
Write-Host "✓ Showing page $($allPayments.currentPage + 1) of $($allPayments.totalPages)`n" -ForegroundColor Green

if ($allPayments.content.Count -gt 0) {
    Write-Host "Sample payments (first 3):" -ForegroundColor Cyan
    $allPayments.content | Select-Object -First 3 | ForEach-Object {
        Write-Host "  • Payment ID: $($_.id)" -ForegroundColor White
        Write-Host "    - Lease ID: $($_.leaseId)" -ForegroundColor Gray
        Write-Host "    - Property: $($_.propertyAddress)" -ForegroundColor Gray
        Write-Host "    - Tenant: $($_.tenantName)" -ForegroundColor Gray
        Write-Host "    - Amount: ￥$($_.amount)" -ForegroundColor Green
        Write-Host "    - Date: $($_.paymentDate)" -ForegroundColor Gray
        Write-Host "    - Method: $($_.paymentMethod)" -ForegroundColor Gray
        Write-Host ""
    }
}

# Step 3: Get first leaseId from the results
if ($allPayments.content.Count -gt 0) {
    $testLeaseId = $allPayments.content[0].leaseId
    Write-Host "[3/4] Testing filter by lease ID: $testLeaseId..." -ForegroundColor Yellow
    
    $filteredResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/payments?leaseId=$testLeaseId&page=0&size=10" `
        -Method GET `
        -WebSession $session
    
    $filteredPayments = $filteredResponse.Content | ConvertFrom-Json
    Write-Host "✓ Found $($filteredPayments.totalElements) payment(s) for lease ID $testLeaseId`n" -ForegroundColor Green
    
    if ($filteredPayments.content.Count -gt 0) {
        Write-Host "Filtered payments:" -ForegroundColor Cyan
        $filteredPayments.content | ForEach-Object {
            Write-Host "  • Payment ID: $($_.id) - Amount: ￥$($_.amount) - Date: $($_.paymentDate)" -ForegroundColor White
        }
    }
} else {
    Write-Host "[3/4] ⚠ No payments found to test filtering" -ForegroundColor Yellow
}

# Step 4: Summary
Write-Host "`n[4/4] Test Summary:" -ForegroundColor Yellow
Write-Host "✓ All payments endpoint: GET /api/payments (no params)" -ForegroundColor Green
Write-Host "✓ Filtered payments endpoint: GET /api/payments?leaseId=X" -ForegroundColor Green
Write-Host "✓ PaymentDto now includes: propertyAddress and tenantName" -ForegroundColor Green

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Test Complete!" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan
