# Script untuk build dan install Luca App
# Otomatis set JAVA_HOME dan build project

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  LUCA APP - BUILD & INSTALL SCRIPT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Cari JAVA_HOME
Write-Host "1. Checking JAVA_HOME..." -ForegroundColor Yellow

$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
    Write-Host "   ⚠️  JAVA_HOME not set. Searching for Java..." -ForegroundColor Yellow

    # Cari di Android Studio
    $possiblePaths = @(
        "C:\Program Files\Android\Android Studio\jbr",
        "C:\Program Files (x86)\Android\Android Studio\jbr",
        "$env:LOCALAPPDATA\Android\Sdk\jre"
    )

    foreach ($path in $possiblePaths) {
        if (Test-Path "$path\bin\java.exe") {
            $javaHome = $path
            $env:JAVA_HOME = $javaHome
            $env:PATH = "$javaHome\bin;$env:PATH"
            Write-Host "   ✅ Found Java at: $javaHome" -ForegroundColor Green
            break
        }
    }

    if (-not $javaHome) {
        Write-Host "   ❌ Java not found!" -ForegroundColor Red
        Write-Host ""
        Write-Host "   Solusi:" -ForegroundColor Yellow
        Write-Host "   1. Install Android Studio" -ForegroundColor White
        Write-Host "   2. Atau set JAVA_HOME manual:" -ForegroundColor White
        Write-Host "      `$env:JAVA_HOME = 'C:\Path\To\JDK'" -ForegroundColor Cyan
        Write-Host ""
        exit 1
    }
} else {
    Write-Host "   ✅ JAVA_HOME: $javaHome" -ForegroundColor Green
}

# 2. Check Gradle
Write-Host ""
Write-Host "2. Checking Gradle..." -ForegroundColor Yellow
if (Test-Path ".\gradlew.bat") {
    Write-Host "   ✅ Gradle wrapper found" -ForegroundColor Green
} else {
    Write-Host "   ❌ gradlew.bat not found!" -ForegroundColor Red
    exit 1
}

# 3. Clean Project
Write-Host ""
Write-Host "3. Cleaning project..." -ForegroundColor Yellow
Write-Host "   (This may take a moment...)" -ForegroundColor Gray
$cleanResult = & .\gradlew.bat clean 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Clean successful" -ForegroundColor Green
} else {
    Write-Host "   ❌ Clean failed!" -ForegroundColor Red
    Write-Host $cleanResult
    exit 1
}

# 4. Build Debug APK
Write-Host ""
Write-Host "4. Building debug APK..." -ForegroundColor Yellow
Write-Host "   (This will take 1-3 minutes...)" -ForegroundColor Gray
Write-Host ""

$buildResult = & .\gradlew.bat assembleDebug 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "   ✅ Build successful!" -ForegroundColor Green
    Write-Host ""

    # Tampilkan lokasi APK
    $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
    if (Test-Path $apkPath) {
        $apkSize = [math]::Round((Get-Item $apkPath).Length / 1MB, 2)
        Write-Host "   📦 APK Location:" -ForegroundColor Cyan
        Write-Host "      $((Get-Item $apkPath).FullName)" -ForegroundColor White
        Write-Host "   📊 APK Size: $apkSize MB" -ForegroundColor Cyan
    }
} else {
    Write-Host ""
    Write-Host "   ❌ Build failed!" -ForegroundColor Red
    Write-Host ""
    Write-Host "   Error output:" -ForegroundColor Yellow
    $buildResult | Select-String -Pattern "error|Error|ERROR|FAILURE" -Context 3,1
    exit 1
}

# 5. Check Connected Devices
Write-Host ""
Write-Host "5. Checking connected devices..." -ForegroundColor Yellow
$devices = & adb devices 2>&1 | Select-String -Pattern "device$" -NotMatch "List"

if ($devices) {
    Write-Host "   ✅ Device(s) found:" -ForegroundColor Green
    $devices | ForEach-Object { Write-Host "      - $_" -ForegroundColor White }

    # Tanya user mau install atau tidak
    Write-Host ""
    $response = Read-Host "   Install to device? (Y/N)"

    if ($response -eq 'Y' -or $response -eq 'y') {
        Write-Host ""
        Write-Host "6. Installing APK..." -ForegroundColor Yellow

        $installResult = & .\gradlew.bat installDebug 2>&1

        if ($LASTEXITCODE -eq 0) {
            Write-Host "   ✅ Install successful!" -ForegroundColor Green
            Write-Host ""
            Write-Host "   🚀 App installed! You can now open Luca on your device." -ForegroundColor Cyan
        } else {
            Write-Host "   ❌ Install failed!" -ForegroundColor Red
            Write-Host $installResult
        }
    } else {
        Write-Host "   ℹ️  Skipped installation" -ForegroundColor Gray
        Write-Host ""
        Write-Host "   Manual install:" -ForegroundColor Yellow
        Write-Host "   adb install app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor White
    }
} else {
    Write-Host "   ⚠️  No device connected" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "   To install manually:" -ForegroundColor Yellow
    Write-Host "   1. Connect your device with USB" -ForegroundColor White
    Write-Host "   2. Enable USB Debugging" -ForegroundColor White
    Write-Host "   3. Run: adb install app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor White
    Write-Host ""
    Write-Host "   Or copy APK to device and install manually" -ForegroundColor White
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  ✅ BUILD COMPLETE!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
