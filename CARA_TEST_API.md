# 🧪 CARA TEST HUGGING FACE API SETELAH ADD SECRET

## ✅ OPSI 1: Test dengan Browser (PALING MUDAH)

### Step 1: Buka URL ini di browser
```
https://lucashared-luca-shared-expense.hf.space/
```

**Yang harus kamu lihat:**
```json
{
  "status": "online",
  "version": "3.1",
  "google_vision_status": "connected"  ← CEK INI!
}
```

### Step 2: Buka health endpoint
```
https://lucashared-luca-shared-expense.hf.space/health
```

**Kalau BERHASIL (Secret sudah benar):**
```json
{
  "status": "healthy",
  "google_vision": "connected"  ✅
}
```

**Kalau GAGAL (Secret belum di-set atau salah):**
```json
{
  "status": "unhealthy",
  "google_vision": "not configured"  ❌
}
```

---

## ✅ OPSI 2: Test dengan Python Script

### Buka terminal/PowerShell:
```powershell
cd "D:\BCA\Cawu 4\ML\Project\luca-shared-expense"
python test_hf_status.py
```

**Output yang diharapkan (jika berhasil):**
```
======================================================================
🔍 TESTING HUGGING FACE API
======================================================================

[1/3] Testing root endpoint (GET /)...
  ✅ Status: online
  ✅ Version: 3.1
  ✅ Google Vision: connected 🎉

[2/3] Testing health endpoint (GET /health)...
  ✅ Status: healthy
  ✅ Google Vision: connected

[3/3] Testing parse endpoint (POST /parse - no file)...
  ✅ Endpoint working (correctly rejects empty request)

======================================================================

🎯 KESIMPULAN:

✅✅✅ GOOGLE VISION CONNECTED! ✅✅✅

🎉 API SIAP DIGUNAKAN!
📱 Sekarang coba upload dari Android app!
```

---

## ✅ OPSI 3: Test dengan curl (PowerShell)

```powershell
# Test root endpoint
curl.exe https://lucashared-luca-shared-expense.hf.space/

# Test health endpoint
curl.exe https://lucashared-luca-shared-expense.hf.space/health
```

Cari di output:
- `"google_vision_status": "connected"` ← KALAU ADA INI = SUCCESS ✅
- `"google_vision_status": "not configured"` ← KALAU ADA INI = GAGAL ❌

---

## ⚠️ JIKA SPACE TIDAK MERESPONS

Space Hugging Face kadang "sleep" kalau tidak ada request dalam beberapa menit.

**Solusi:**
1. Buka URL di browser (tunggu 10-20 detik sampai Space "wake up")
2. Refresh beberapa kali kalau perlu
3. Setelah Space "bangun", test lagi

**Atau:**
- Pergi ke: https://huggingface.co/spaces/lucaShared/luca-shared-expense
- Klik tab "Logs" untuk lihat apa yang terjadi
- Kalau stuck, klik "Factory reboot" di Settings

---

## 📊 CARA CEK LOGS DI HUGGING FACE

1. Buka: https://huggingface.co/spaces/lucaShared/luca-shared-expense
2. Klik tab **"Logs"** (di atas)
3. Cari baris ini:

**Kalau BERHASIL:**
```
🚀 Initializing Google Vision API...
✅ Using GOOGLE_CREDENTIALS_JSON from secrets
✅ Google Vision Ready!
```

**Kalau GAGAL:**
```
🚀 Initializing Google Vision API...
⚠️ No credentials found, trying default...
❌ Google Vision initialization failed!
```

---

## 🐛 TROUBLESHOOTING

### Problem 1: "google_vision_status": "not configured"

**Penyebab**: Secret belum di-set atau nama salah

**Solusi:**
1. Cek di HF Space Settings > Repository secrets
2. Pastikan ada secret dengan nama persis: `GOOGLE_CREDENTIALS_JSON`
3. Kalau belum ada, add sekarang
4. Kalau sudah ada, coba delete lalu add lagi (mungkin JSON-nya corrupt)

---

### Problem 2: Space tidak merespons / timeout

**Penyebab**: Space sleeping atau restarting

**Solusi:**
1. Buka URL di browser, tunggu 20-30 detik
2. Refresh beberapa kali
3. Kalau masih tidak work, pergi ke Space Settings > Factory reboot

---

### Problem 3: "Invalid JSON" di logs

**Penyebab**: JSON credentials tidak lengkap atau corrupt

**Solusi:**
1. Delete secret yang ada
2. Buka file `google-credentials.json` lokal
3. Copy **SELURUH ISI** (pastikan dari `{` sampai `}`)
4. Add secret lagi dengan value yang baru
5. Tunggu Space restart

---

## ✅ AFTER SUCCESS

Setelah kamu konfirmasi `"google_vision_status": "connected"`:

1. **Build Android app** (Rebuild project)
2. **Jalankan app** di device/emulator
3. **Pilih/ambil foto receipt**
4. **Klik upload**
5. **Harusnya BERHASIL!** 🎉

---

## 📞 KASIH TAHU AKU HASILNYA

Setelah test, kasih tahu aku:

✅ **Kalau berhasil**: 
   "Google Vision status: connected"
   
❌ **Kalau gagal**: 
   "Google Vision status: not configured"
   + Screenshot logs dari HF Space (kalau bisa)

---

**File ini tersimpan di**: `D:\BCA\Cawu 4\ML\Project\luca-shared-expense\CARA_TEST_API.md`

