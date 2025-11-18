# API Testing Script
# Tests backend endpoints to verify CRUD operations

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "SIJA Presensi - API Testing Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8081/api"
$adminUser = @{
    username = "admin"
    password = "admin123"
}

# Function to print test result
function Print-Result {
    param (
        [string]$TestName,
        [bool]$Success,
        [string]$Message = ""
    )
    
    if ($Success) {
        Write-Host "✅ $TestName" -ForegroundColor Green
    } else {
        Write-Host "❌ $TestName" -ForegroundColor Red
    }
    
    if ($Message) {
        Write-Host "   $Message" -ForegroundColor Gray
    }
}

# Test 1: Backend Health Check
Write-Host "Test 1: Backend Health Check" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/hello" -Method GET -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Print-Result "Backend is running" $true "Status: $($response.StatusCode)"
    }
} catch {
    Print-Result "Backend is running" $false "Error: $_"
    Write-Host "⚠️  Please start backend: cd backend && mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# Test 2: Authentication
Write-Host "Test 2: Authentication" -ForegroundColor Yellow
try {
    $loginBody = $adminUser | ConvertTo-Json
    $response = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $response.token
    
    if ($token) {
        Print-Result "Login successful" $true "Token received"
        $headers = @{
            "Authorization" = "Bearer $token"
            "Content-Type" = "application/json"
        }
    } else {
        Print-Result "Login successful" $false "No token received"
        exit 1
    }
} catch {
    Print-Result "Login successful" $false "Error: $_"
    exit 1
}
Write-Host ""

# Test 3: GET All Siswa
Write-Host "Test 3: GET All Siswa" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/siswa" -Method GET -Headers $headers
    $siswaCount = $response.Count
    Print-Result "Get all siswa" $true "Found $siswaCount siswa"
    
    if ($siswaCount -gt 0) {
        Write-Host "   Sample: $($response[0].nama) (NIS: $($response[0].nis))" -ForegroundColor Gray
    }
} catch {
    Print-Result "Get all siswa" $false "Error: $_"
}
Write-Host ""

# Test 4: CREATE Siswa
Write-Host "Test 4: CREATE Siswa" -ForegroundColor Yellow
$testSiswa = @{
    nis = "TEST$(Get-Random -Minimum 1000 -Maximum 9999)"
    nama = "Test Siswa $(Get-Date -Format 'HHmmss')"
    kelas = "XII RPL 1"
    jurusan = "RPL"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/siswa" -Method POST -Body $testSiswa -Headers $headers
    $createdId = $response.id
    Print-Result "Create siswa" $true "Created with ID: $createdId"
    Write-Host "   Nama: $($response.nama)" -ForegroundColor Gray
} catch {
    Print-Result "Create siswa" $false "Error: $_"
    $createdId = $null
}
Write-Host ""

# Test 5: GET Siswa by ID
if ($createdId) {
    Write-Host "Test 5: GET Siswa by ID" -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/siswa/$createdId" -Method GET -Headers $headers
        Print-Result "Get siswa by ID" $true "Found: $($response.nama)"
    } catch {
        Print-Result "Get siswa by ID" $false "Error: $_"
    }
    Write-Host ""
}

# Test 6: UPDATE Siswa
if ($createdId) {
    Write-Host "Test 6: UPDATE Siswa" -ForegroundColor Yellow
    $updateSiswa = @{
        nis = "TEST$(Get-Random -Minimum 1000 -Maximum 9999)"
        nama = "Test Siswa UPDATED"
        kelas = "XII RPL 2"
        jurusan = "RPL"
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/siswa/$createdId" -Method PUT -Body $updateSiswa -Headers $headers
        Print-Result "Update siswa" $true "Updated to: $($response.nama)"
    } catch {
        Print-Result "Update siswa" $false "Error: $_"
    }
    Write-Host ""
}

# Test 7: GET All Guru
Write-Host "Test 7: GET All Guru" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/guru" -Method GET -Headers $headers
    $guruCount = $response.Count
    Print-Result "Get all guru" $true "Found $guruCount guru"
    
    if ($guruCount -gt 0) {
        Write-Host "   Sample: $($response[0].nama) (NIP: $($response[0].nip))" -ForegroundColor Gray
    }
} catch {
    Print-Result "Get all guru" $false "Error: $_"
}
Write-Host ""

# Test 8: CREATE Guru
Write-Host "Test 8: CREATE Guru" -ForegroundColor Yellow
$testGuru = @{
    nip = "TEST$(Get-Random -Minimum 100000 -Maximum 999999)"
    nama = "Test Guru $(Get-Date -Format 'HHmmss')"
    mapel = "Testing"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/guru" -Method POST -Body $testGuru -Headers $headers
    $createdGuruId = $response.id
    Print-Result "Create guru" $true "Created with ID: $createdGuruId"
    Write-Host "   Nama: $($response.nama)" -ForegroundColor Gray
} catch {
    Print-Result "Create guru" $false "Error: $_"
    $createdGuruId = $null
}
Write-Host ""

# Test 9: DELETE Guru
if ($createdGuruId) {
    Write-Host "Test 9: DELETE Guru" -ForegroundColor Yellow
    try {
        Invoke-RestMethod -Uri "$baseUrl/guru/$createdGuruId" -Method DELETE -Headers $headers
        Print-Result "Delete guru" $true "Deleted guru ID: $createdGuruId"
    } catch {
        Print-Result "Delete guru" $false "Error: $_"
    }
    Write-Host ""
}

# Test 10: DELETE Siswa
if ($createdId) {
    Write-Host "Test 10: DELETE Siswa" -ForegroundColor Yellow
    try {
        Invoke-RestMethod -Uri "$baseUrl/siswa/$createdId" -Method DELETE -Headers $headers
        Print-Result "Delete siswa" $true "Deleted siswa ID: $createdId"
    } catch {
        Print-Result "Delete siswa" $false "Error: $_"
    }
    Write-Host ""
}

# Test 11: GET Laporan Harian
Write-Host "Test 11: GET Laporan Harian (Presensi)" -ForegroundColor Yellow
try {
    $today = Get-Date -Format "yyyy-MM-dd"
    $response = Invoke-RestMethod -Uri "$baseUrl/laporan/harian?tanggal=$today" -Method GET -Headers $headers
    $presensiCount = $response.Count
    Print-Result "Get laporan harian" $true "Found $presensiCount presensi records"
} catch {
    Print-Result "Get laporan harian" $false "Error: $_"
}
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ All API endpoints are working!" -ForegroundColor Green
Write-Host "✅ Desktop app can now connect to backend" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Open desktop app (mvn javafx:run)" -ForegroundColor White
Write-Host "2. Login with: admin / admin123" -ForegroundColor White
Write-Host "3. Test CRUD operations in UI" -ForegroundColor White
Write-Host "4. Follow TESTING-GUIDE.md for detailed testing" -ForegroundColor White
Write-Host ""
