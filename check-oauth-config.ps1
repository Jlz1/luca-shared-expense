# Script untuk verifikasi konfigurasi Google OAuth
# Memeriksa apakah semua setup sudah benar

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Google OAuth Configuration Checker" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$hasError = $false

# 1. Check Package Name di build.gradle.kts
Write-Host "1. Checking Package Name..." -ForegroundColor Yellow
$buildGradle = Get-Content "app\build.gradle.kts" -Raw
if ($buildGradle -match 'applicationId\s*=\s*"com\.noir\.luca"') {
    Write-Host "   ✅ Package name: com.noir.luca (BENAR)" -ForegroundColor Green
} else {
    Write-Host "   ❌ Package name SALAH! Harus: com.noir.luca" -ForegroundColor Red
    $hasError = $true
}

# 2. Check google-services.json exists
Write-Host ""
Write-Host "2. Checking google-services.json..." -ForegroundColor Yellow
$googleServicesPath = "app\google-services.json"
if (Test-Path $googleServicesPath) {
    Write-Host "   ✅ File ditemukan" -ForegroundColor Green

    try {
        # Parse JSON
        $googleServices = Get-Content $googleServicesPath -Raw | ConvertFrom-Json

        # Check package name
        $packageName = $googleServices.client[0].client_info.android_client_info.package_name
        Write-Host "   📦 Package name di Firebase: $packageName" -ForegroundColor White

        if ($packageName -eq "com.noir.luca") {
            Write-Host "   ✅ Package name MATCH!" -ForegroundColor Green
        } else {
            Write-Host "   ❌ Package name TIDAK MATCH!" -ForegroundColor Red
            $hasError = $true
        }

        # Check oauth_client
        $oauthClients = $googleServices.client[0].oauth_client
        if ($null -ne $oauthClients -and $oauthClients.Count -gt 0) {
            Write-Host "   ✅ OAuth clients: $($oauthClients.Count) client(s)" -ForegroundColor Green

            # Check Web Client
            $webClient = $null
            foreach ($client in $oauthClients) {
                if ($client.client_type -eq 3) {
                    $webClient = $client
                    break
                }
            }

            if ($null -ne $webClient) {
                $webClientId = $webClient.client_id
                Write-Host "   🌐 Web Client ID: $webClientId" -ForegroundColor Cyan
            } else {
                Write-Host "   ❌ Web Client ID tidak ditemukan!" -ForegroundColor Red
                $hasError = $true
            }

            # Check Android Client
            $androidClient = $null
            foreach ($client in $oauthClients) {
                if ($client.client_type -eq 1) {
                    $androidClient = $client
                    break
                }
            }

            if ($null -ne $androidClient) {
                Write-Host "   ✅ Android OAuth Client ditemukan" -ForegroundColor Green
            } else {
                Write-Host "   ❌ Android OAuth Client tidak ditemukan!" -ForegroundColor Red
                $hasError = $true
            }
        } else {
            Write-Host "   ❌ oauth_client KOSONG!" -ForegroundColor Red
            Write-Host ""
            Write-Host "   SOLUSI:" -ForegroundColor Yellow
            Write-Host "   1. Tambahkan SHA certificates ke Firebase Console" -ForegroundColor White
            Write-Host "   2. Enable Google Sign-In di Authentication" -ForegroundColor White
            Write-Host "   3. Download ULANG google-services.json" -ForegroundColor White
            $hasError = $true
        }
    } catch {
        Write-Host "   ❌ Error parsing JSON: $_" -ForegroundColor Red
        $hasError = $true
    }
} else {
    Write-Host "   ❌ File tidak ditemukan!" -ForegroundColor Red
    $hasError = $true
}

# 3. Check dependencies
Write-Host ""
Write-Host "3. Checking Dependencies..." -ForegroundColor Yellow
if ($buildGradle -match "com\.google\.firebase:firebase-auth") {
    Write-Host "   ✅ Firebase Auth" -ForegroundColor Green
} else {
    Write-Host "   ❌ Firebase Auth tidak ditemukan!" -ForegroundColor Red
    $hasError = $true
}

if ($buildGradle -match "com\.google\.android\.gms:play-services-auth") {
    Write-Host "   ✅ Google Play Services Auth" -ForegroundColor Green
} else {
    Write-Host "   ❌ Google Play Services Auth tidak ditemukan!" -ForegroundColor Red
    $hasError = $true
}

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
if ($hasError) {
    Write-Host "  ❌ KONFIGURASI BELUM LENGKAP" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "📖 Lihat: GOOGLE_OAUTH_QUICK_START.md" -ForegroundColor Yellow
} else {
    Write-Host "  ✅ KONFIGURASI SUDAH BENAR!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "🚀 Siap untuk build:" -ForegroundColor Green
    Write-Host "   .\gradlew clean" -ForegroundColor White
    Write-Host "   .\gradlew assembleDebug" -ForegroundColor White
}
Write-Host ""
