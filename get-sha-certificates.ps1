# Script untuk mendapatkan SHA-1 dan SHA-256 Certificate Fingerprints
# Untuk konfigurasi Google OAuth di Firebase

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SHA Certificate Generator for Firebase" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Cari Java keytool
$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
    Write-Host "⚠️  JAVA_HOME tidak ditemukan. Mencari Java di lokasi default..." -ForegroundColor Yellow

    # Cari di Program Files
    $possiblePaths = @(
        "C:\Program Files\Java\*\bin\keytool.exe",
        "C:\Program Files (x86)\Java\*\bin\keytool.exe",
        "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe"
    )

    $keytool = $null
    foreach ($path in $possiblePaths) {
        $found = Get-ChildItem -Path $path -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) {
            $keytool = $found.FullName
            break
        }
    }

    if (-not $keytool) {
        Write-Host "❌ Keytool tidak ditemukan!" -ForegroundColor Red
        Write-Host ""
        Write-Host "Solusi:" -ForegroundColor Yellow
        Write-Host "1. Install JDK jika belum ada" -ForegroundColor White
        Write-Host "2. Set JAVA_HOME environment variable" -ForegroundColor White
        Write-Host "   Atau gunakan: Android Studio > File > Settings > Build > Build Tools > Gradle > Gradle JDK" -ForegroundColor White
        Write-Host ""
        Write-Host "Alternatif: Gunakan Android Studio" -ForegroundColor Yellow
        Write-Host "1. Buka project di Android Studio" -ForegroundColor White
        Write-Host "2. Klik Gradle tab (kanan)" -ForegroundColor White
        Write-Host "3. app > Tasks > android > signingReport" -ForegroundColor White
        exit 1
    }
} else {
    $keytool = Join-Path $javaHome "bin\keytool.exe"
    if (-not (Test-Path $keytool)) {
        Write-Host "❌ Keytool tidak ditemukan di JAVA_HOME: $keytool" -ForegroundColor Red
        exit 1
    }
}

Write-Host "✅ Keytool ditemukan: $keytool" -ForegroundColor Green
Write-Host ""

# Path debug keystore
$keystorePath = Join-Path $env:USERPROFILE ".android\debug.keystore"

if (-not (Test-Path $keystorePath)) {
    Write-Host "❌ Debug keystore tidak ditemukan di: $keystorePath" -ForegroundColor Red
    Write-Host ""
    Write-Host "Solusi:" -ForegroundColor Yellow
    Write-Host "1. Build project terlebih dahulu dengan: .\gradlew assembleDebug" -ForegroundColor White
    Write-Host "2. Debug keystore akan otomatis dibuat" -ForegroundColor White
    exit 1
}

Write-Host "✅ Debug keystore ditemukan: $keystorePath" -ForegroundColor Green
Write-Host ""
Write-Host "Mengambil certificate fingerprints..." -ForegroundColor Cyan
Write-Host ""

# Jalankan keytool
& $keytool -list -v -keystore $keystorePath -alias androiddebugkey -storepass android -keypass android 2>&1 | ForEach-Object {
    if ($_ -match "SHA1:") {
        Write-Host $_ -ForegroundColor Yellow
    } elseif ($_ -match "SHA256:") {
        Write-Host $_ -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  LANGKAH SELANJUTNYA" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Copy SHA1 dan SHA256 di atas" -ForegroundColor White
Write-Host "2. Buka Firebase Console: https://console.firebase.google.com/" -ForegroundColor White
Write-Host "3. Pilih project: luca-a8984" -ForegroundColor White
Write-Host "4. Masuk ke Project Settings > Your apps > com.noir.luca" -ForegroundColor White
Write-Host "5. Scroll ke 'SHA certificate fingerprints'" -ForegroundColor White
Write-Host "6. Klik 'Add fingerprint' dan paste SHA1" -ForegroundColor White
Write-Host "7. Klik 'Add fingerprint' lagi dan paste SHA256" -ForegroundColor White
Write-Host "8. Klik 'Save'" -ForegroundColor White
Write-Host ""
Write-Host "Lihat file GOOGLE_OAUTH_SETUP_GUIDE.md untuk panduan lengkap" -ForegroundColor Cyan
Write-Host ""
