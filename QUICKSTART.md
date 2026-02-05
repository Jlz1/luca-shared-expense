# 🚀 Quick Start Guide

Get your Receipt Parser API running in 5 minutes!

---

## 📋 Prerequisites

- [ ] Python 3.9+
- [ ] Google Cloud account
- [ ] Hugging Face account (optional, for deployment)

---

## ⚡ Option 1: Local Development (5 minutes)

### Step 1: Get Google Cloud Credentials
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create/select a project
3. Enable **Cloud Vision API**
4. Create a service account with "Cloud Vision API User" role
5. Download the JSON key file

### Step 2: Setup and Run
```powershell
# Clone or navigate to project
cd luca-shared-expense

# Set credentials (Windows PowerShell)
$env:GOOGLE_CREDENTIALS_JSON = Get-Content path\to\service-account-key.json -Raw

# Run setup script (creates venv, installs deps, starts server)
.\run_local.ps1
```

### Step 3: Test
```powershell
# In another terminal
python test_api.py http://localhost:7860 path\to\receipt.jpg
```

**Done!** 🎉 Your API is running at `http://localhost:7860`

---

## ☁️ Option 2: Deploy to Hugging Face (10 minutes)

### Step 1: Create Hugging Face Space
1. Go to https://huggingface.co/spaces
2. Click "Create new Space"
3. Name: `luca-receipt-parser`
4. SDK: **Docker**
5. Visibility: Public or Private

### Step 2: Push Code
```bash
# Clone HF Space repo
git clone https://huggingface.co/spaces/YOUR_USERNAME/luca-receipt-parser
cd luca-receipt-parser

# Copy backend files
cp ../luca-shared-expense/main.py .
cp ../luca-shared-expense/requirements.txt .
cp ../luca-shared-expense/Dockerfile .
cp ../luca-shared-expense/README_HF.md README.md

# Commit and push
git add .
git commit -m "Deploy Google Vision Receipt Parser"
git push
```

### Step 3: Add Credentials
1. Go to your Space **Settings**
2. Scroll to **Repository secrets**
3. Add new secret:
   - **Name**: `GOOGLE_CREDENTIALS_JSON`
   - **Value**: Copy entire content of your service-account-key.json
4. Save

### Step 4: Wait for Build
- The Space will automatically build (2-3 minutes)
- Check logs for "✅ Google Vision Ready!"

### Step 5: Test
```bash
curl https://YOUR_USERNAME-luca-receipt-parser.hf.space/health
```

**Done!** 🎉 Your API is deployed!

---

## 📱 Option 3: Update Android App

### Step 1: Update API URL
Edit `app/src/main/java/com/example/luca/data/api/ScanApiClient.kt`:

```kotlin
// For local testing:
private const val BASE_URL = "http://10.0.2.2:7860/"  // Android emulator

// For production:
private const val BASE_URL = "https://YOUR_USERNAME-luca-receipt-parser.hf.space/"
```

### Step 2: Build APK
```bash
cd luca-shared-expense
.\gradlew assembleDebug
```

### Step 3: Install
```bash
adb install app\build\outputs\apk\debug\app-debug.apk
```

**Done!** 🎉 Open the app and scan a receipt!

---

## 🧪 Testing

### Test Health
```bash
curl http://localhost:7860/health
```

### Test Parse
```bash
curl -X POST -F "file=@receipt.jpg" http://localhost:7860/parse
```

### Test with Python Script
```bash
python test_api.py http://localhost:7860 receipt.jpg
```

---

## 🐛 Troubleshooting

### "Google Vision client not initialized"
**Fix**: Check that `GOOGLE_CREDENTIALS_JSON` environment variable is set correctly.

```powershell
# Verify it's set
echo $env:GOOGLE_CREDENTIALS_JSON

# Re-set if needed
$env:GOOGLE_CREDENTIALS_JSON = Get-Content service-account-key.json -Raw
```

### "No text detected"
**Causes**:
- Blurry image
- Poor lighting
- Receipt text too small

**Fix**: Retake photo with better lighting and focus.

### "Timeout" from Android
**Fix**: Increase timeout in `ScanApiClient.kt`:
```kotlin
.connectTimeout(120, TimeUnit.SECONDS)
.readTimeout(120, TimeUnit.SECONDS)
```

### "Connection refused" from Android
**Fix for Emulator**: Use `http://10.0.2.2:7860/` instead of `localhost`

**Fix for Device**: 
1. Connect device and computer to same WiFi
2. Use computer's IP: `http://192.168.x.x:7860/`

---

## 📚 Next Steps

1. ✅ API is running
2. ⏳ Read [DEPLOYMENT.md](DEPLOYMENT.md) for detailed setup
3. ⏳ Read [FILE_CHANGES.md](FILE_CHANGES.md) to understand changes
4. ⏳ Test with multiple receipts
5. ⏳ Integrate bill splitting calculation
6. ⏳ Add user authentication (if needed)

---

## 💰 Free Tier Limits

- **Google Vision API**: 1,000 requests/month free
- **Hugging Face Spaces**: Free hosting (Community license)
- **Total Cost**: $0 for development and light usage!

---

## 📞 Need Help?

- 📖 Read [DEPLOYMENT.md](DEPLOYMENT.md) for detailed guide
- 📝 Check [FILE_CHANGES.md](FILE_CHANGES.md) for what changed
- 🐛 Check logs: `python main.py` will show errors
- 🔍 Google Cloud Console → API Dashboard for Vision API errors

---

**Happy Coding!** 🎉

