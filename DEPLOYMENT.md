# 🚀 LUCA Receipt Parser - Deployment Guide

## Overview
This guide covers deploying the Receipt Parser API with Google Vision OCR to Hugging Face Spaces.

---

## 📋 Prerequisites

1. **Google Cloud Account** with Vision API enabled
2. **Hugging Face Account** (free tier works)
3. **Git** installed locally

---

## 🔧 Step 1: Setup Google Cloud Vision API

### 1.1 Create a Google Cloud Project
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project (e.g., "luca-receipt-parser")
3. Enable billing (required for Vision API, but free tier gives 1000 requests/month)

### 1.2 Enable Vision API
```bash
# Go to APIs & Services > Library
# Search for "Cloud Vision API"
# Click "Enable"
```

### 1.3 Create Service Account
1. Go to **IAM & Admin > Service Accounts**
2. Click **Create Service Account**
3. Name: `receipt-parser-service`
4. Role: `Cloud Vision > Cloud Vision API User`
5. Click **Create Key** → JSON format
6. Save the JSON file (e.g., `service-account-key.json`)

---

## 🌐 Step 2: Deploy to Hugging Face Spaces

### 2.1 Create a New Space
1. Go to [Hugging Face Spaces](https://huggingface.co/spaces)
2. Click **Create new Space**
3. Settings:
   - **Name**: `luca-receipt-parser` (or your choice)
   - **SDK**: Docker
   - **Visibility**: Public or Private

### 2.2 Clone and Push Code
```bash
# Clone your HF Space repository
git clone https://huggingface.co/spaces/YOUR_USERNAME/luca-receipt-parser
cd luca-receipt-parser

# Copy backend files only
cp /path/to/luca-shared-expense/main.py .
cp /path/to/luca-shared-expense/requirements.txt .
cp /path/to/luca-shared-expense/Dockerfile .
cp /path/to/luca-shared-expense/README_HF.md README.md

# Commit and push
git add .
git commit -m "Initial deployment with Google Vision API"
git push
```

### 2.3 Add Google Credentials as Secret
1. Go to your Space **Settings**
2. Scroll to **Repository secrets**
3. Click **New secret**
4. Name: `GOOGLE_CREDENTIALS_JSON`
5. Value: Open your `service-account-key.json` and copy the **entire content** as a single line
   ```json
   {"type":"service_account","project_id":"your-project-id",...}
   ```
6. Click **Save**

### 2.4 Restart the Space
- After adding the secret, the Space will automatically rebuild
- Wait 2-3 minutes for deployment
- Check the logs for "✅ Google Vision Ready!"

---

## 📱 Step 3: Update Android App

### 3.1 Update API Base URL
Edit `app/src/main/java/com/example/luca/data/api/ScanApiClient.kt`:

```kotlin
private const val BASE_URL = "https://YOUR_USERNAME-luca-receipt-parser.hf.space/"
```

Replace `YOUR_USERNAME` with your Hugging Face username.

### 3.2 Rebuild Android App
```bash
cd /path/to/luca-shared-expense
./gradlew assembleDebug
```

### 3.3 Install APK
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🧪 Step 4: Test the API

### 4.1 Test with curl
```bash
curl https://YOUR_USERNAME-luca-receipt-parser.hf.space/
```

Expected response:
```json
{
  "status": "online",
  "message": "Receipt Parser API 🧾",
  "version": "3.1",
  "google_vision_status": "connected"
}
```

### 4.2 Test with a Receipt
```bash
curl -X POST \
  -F "file=@/path/to/receipt.jpg" \
  https://YOUR_USERNAME-luca-receipt-parser.hf.space/parse
```

### 4.3 Test from Android App
1. Open the app
2. Tap "Scan Receipt"
3. Take a photo
4. Wait for results (30-60 seconds first time)

---

## 🐛 Troubleshooting

### Issue: "Google Vision client not initialized"
**Solution**: Check that `GOOGLE_CREDENTIALS_JSON` is correctly set in HF Secrets.

### Issue: "HTTP 500 - Vision API error"
**Causes**:
- Vision API not enabled in Google Cloud
- Invalid service account JSON
- Insufficient permissions

**Solution**: 
1. Verify Vision API is enabled
2. Check service account has `Cloud Vision API User` role
3. Re-copy the JSON secret (ensure no extra spaces)

### Issue: "Timeout" from Android app
**Solution**: 
- First API call takes 30-60s (cold start)
- Increase OkHttp timeout in Android:
  ```kotlin
  .connectTimeout(120, TimeUnit.SECONDS)
  .readTimeout(120, TimeUnit.SECONDS)
  ```

### Issue: "No text detected"
**Causes**:
- Image too blurry
- Poor lighting
- Receipt text too small

**Solution**: 
- Ensure good lighting
- Hold phone steady
- Make sure text is clear and readable

---

## 💰 Cost Estimation

### Google Vision API Pricing (as of 2024)
- **Free tier**: 1,000 requests/month
- **After free tier**: $1.50 per 1,000 requests

### Hugging Face Spaces
- **Free tier**: 
  - 2 vCPU
  - 16 GB RAM
  - Always-on (with Community license)
- **Paid**: Upgrade for better performance if needed

### Estimated Monthly Cost
- **Development**: $0 (within free tiers)
- **Light usage** (< 1000 scans/month): $0
- **Heavy usage** (10,000 scans/month): ~$15 (Vision API only)

---

## 🔐 Security Best Practices

1. **Never commit** service account JSON to Git
2. **Use HF Secrets** for all credentials
3. **Set API rate limits** if needed
4. **Monitor usage** in Google Cloud Console
5. **Use Private Space** if handling sensitive data

---

## 📊 Monitoring

### Check API Health
```bash
curl https://YOUR_USERNAME-luca-receipt-parser.hf.space/health
```

### View HF Space Logs
1. Go to your Space
2. Click **Logs** tab
3. Monitor for errors or performance issues

### Check Google Cloud Usage
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Navigate to **APIs & Services > Dashboard**
3. View Vision API usage statistics

---

## 🚀 Performance Optimization

### 1. Image Compression (Android)
Already implemented in `ScanRepository.kt`:
- Resize to max 1920px
- Compress to JPEG quality 85
- Reduce to < 1MB

### 2. Caching (Future Enhancement)
- Cache OCR results in Android local database
- Avoid re-scanning same receipt

### 3. Batch Processing (Future)
- Process multiple receipts at once
- Use async processing for better UX

---

## 📝 Next Steps

1. ✅ Deploy to Hugging Face Spaces
2. ✅ Test API endpoints
3. ✅ Update Android app
4. 🔄 Add error handling improvements
5. 🔄 Implement receipt history in app
6. 🔄 Add user authentication (optional)
7. 🔄 Deploy bill splitting calculation API

---

## 📞 Support

- **Issues**: Open an issue on GitHub
- **Hugging Face**: Check [HF Spaces Docs](https://huggingface.co/docs/hub/spaces)
- **Google Vision**: See [Vision API Docs](https://cloud.google.com/vision/docs)

---

**Last Updated**: February 2026
**Version**: 3.1
**Accuracy**: 59.5% (22/37 test receipts balanced)

