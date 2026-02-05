# ==============================================================================
# run_local.ps1 - Setup and Run Receipt Parser API Locally
# ==============================================================================

Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "  Receipt Parser API - Local Setup & Run" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check if virtual environment exists
if (-Not (Test-Path "venv")) {
    Write-Host "[1/4] Creating virtual environment..." -ForegroundColor Yellow
    python -m venv venv
    Write-Host "✅ Virtual environment created!" -ForegroundColor Green
} else {
    Write-Host "[1/4] Virtual environment already exists" -ForegroundColor Green
}

# Step 2: Activate virtual environment
Write-Host "[2/4] Activating virtual environment..." -ForegroundColor Yellow
& .\venv\Scripts\Activate.ps1

# Step 3: Install dependencies
Write-Host "[3/4] Installing dependencies..." -ForegroundColor Yellow
pip install -r requirements.txt

# Step 4: Check if Google credentials are set
if (-Not $env:GOOGLE_CREDENTIALS_JSON) {
    Write-Host ""
    Write-Host "⚠️  WARNING: GOOGLE_CREDENTIALS_JSON not set!" -ForegroundColor Red
    Write-Host ""
    Write-Host "To set it, run:" -ForegroundColor Yellow
    Write-Host '  $env:GOOGLE_CREDENTIALS_JSON = Get-Content path\to\service-account-key.json -Raw' -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Or set GOOGLE_APPLICATION_CREDENTIALS:" -ForegroundColor Yellow
    Write-Host '  $env:GOOGLE_APPLICATION_CREDENTIALS = "path\to\service-account-key.json"' -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Press Enter to continue anyway (API will fail without credentials)..." -ForegroundColor Yellow
    Read-Host
} else {
    Write-Host "✅ Google credentials found!" -ForegroundColor Green
}

# Step 5: Run the Flask app
Write-Host "[4/4] Starting Flask API..." -ForegroundColor Yellow
Write-Host ""
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "  API will be available at: http://localhost:7860" -ForegroundColor Green
Write-Host "  Press Ctrl+C to stop the server" -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

python main.py

