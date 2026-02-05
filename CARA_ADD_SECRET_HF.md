# 🎬 Step-by-Step: Add Google Credentials Secret ke Hugging Face

## 📍 Langkah 1: Buka Space Settings

1. Buka browser, pergi ke:
   ```
   https://huggingface.co/spaces/lucashared/luca-shared-expense
   ```

2. Klik tab **"Settings"** (icon ⚙️ di bagian atas)

---

## 📍 Langkah 2: Scroll ke "Repository secrets"

Scroll ke bawah sampai kamu lihat section:
```
┌──────────────────────────────┐
│ Repository secrets           │
│                              │
│ [Add a secret]               │
└──────────────────────────────┘
```

---

## 📍 Langkah 3: Klik "Add a secret"

Akan muncul form dengan 2 field:
```
┌─────────────────────────────────────┐
│ Name: [____________]                │
│                                     │
│ Value: [_________________________] │
│        [_________________________] │
│        [_________________________] │
│                                     │
│  [Cancel]  [Add secret]            │
└─────────────────────────────────────┘
```

---

## 📍 Langkah 4: Isi Form

### Field 1: Name
Isi persis seperti ini (case-sensitive!):
```
GOOGLE_CREDENTIALS_JSON
```

⚠️ **HARUS PERSIS!** Jangan pakai spasi atau huruf kecil!

### Field 2: Value
Copy-paste **SELURUH ISI** file `google-credentials.json`:

```json
{
  "type": "service_account",
  "project_id": "project-dd012e0e-84bf-4897-a17",
  "private_key_id": "7e5fa645141a722aa35519e2d558e78349e08130",
  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDZB3JHnkq8Feyi\nZaL9g1oLdLk+k8tllSAoYq8BBpV/pTefpU2JJDVP7Mbdc1tKak4UKEUvHpJqvn8t\neGTYnO8bPTcW4s0rJZFKH5mtWcQHMon7+Btz0zh0G5sCpdR2lcQi5rguFF7c/hw8\nUsdVXHrwoMjQ8TQhNMhYrm7YfuKOWsh02zHrpx/BXg0uI28KdxRblRsaZv9e+udP\nUqiDpGoaow5BAM/vnHhyUrdH8kOgavq0TCVN04krj1y8AUNc6eXYIIdPIjcKIOXE\nDZbaxYVyKkR3vGGGSILTlhOkBIuLMeIdc5edCCIekUpiBfFEgDiPKOI0k+MfZwpr\nXFr95yRTAgMBAAECggEAAK/h7BKhKwA9UfskFvijtpGdEbRDDO/kJtRoS2tzmM/U\nFXv0wexSNURqeRbTRU7CrI8Q7ZkRKDAzrxQUxKAkQmbF9BBcH74532nWPRxa2aNo\niEodp4lXJuZvC+RlsQsCE1P/GU9ELYch2wbyWzxgHc+eBjFDwJ7nPZbtOLwlaiQI\nMYG1WXuAClahMw7qlVKTTcv5edLqPCi3AGKEGajwoPTmPjFl20pvNzET5vxD3bIw\nca3oALONLJ1eN9kTADTkeidHcdllxvyv81+hBNQ/kVpgf9BPd1DPW1QabsA42DxG\nyI/GF7EIqxgq/7xaNkseldflW+hih1GmWbxb+6qEGQKBgQD2F81WoUjkHCAhm5Mk\nnMAmZRRhwRuCZ9NOgtHionpXeyWgOxFXDmNpj7VOPkmpF3YJlFwUHZXCgEd6tmg2\nfkV+FCqz1KHn8k2E65pbggbpooLA3sCQCvmmkmgyQc51rf3bbMePZIae/jFa2uJD\nKLfaQODLY/Wn/BIZcA9iI2OtBQKBgQDhxB271uEyoS+UD2NyA1/96YgATH/ljDTX\nNlLBaMtBdLoyeIehTQdxo/PufHg3th+xnyasYsCRZ75PQnXzr+k2dPLUhyPboI8y\nCe6WHjyVRF4o3aK8SKOXbiKzxvPtIaDggcch8+gPvZmoSgHZ1oKT/O5rVOKiheeH\npgTijyCLdwKBgQDPn21CdXiF26TQNe2Cqi0DN6xmfQG0l9wTFRP23ZXSULeB49PR\nWvZMjU4t4SdMXdrcYir9XaiIHKoxwctSjl4a7PDKH88pXahBSHVGVwF2BAHErypP\nXLMb8dGu9Q43AsB2a+RB+lIJufPx71GNz8CthqchcghD3ct3Yq/X4Hur8QKBgHe9\nc2O38RLQSJLHop/KyHO8E4TNDhxqQ3BNgJDSCtN5nHO+V3kmiKcuJOc9Hum1b5Pe\nbD2L1sSH+HjDMBoCF7fpSQ2Na2hF/Qy2FdOKz+j/LM2R14jzIcjkAgXFpIQFjPTl\n//6zBUar8b4/GkI2MmVZBf7pM5atXIImfHrJHKK7AoGAC+SPRCQsvmez6KVsy5P8\nDClcAWhtdm5Loq6ATHUJJlntwV3zM6NL1A4f34ZRYfDZyi7sxd/cSKk4+hddJ+F3\nTRXbca72Ei8oWUmDT4Pp/rWUl1dx0nL16CshdG3bSAucQcvGDY/2G/no3mTbDgBQ\nnJh+POBLjNGYT5415YglRro=\n-----END PRIVATE KEY-----\n",
  "client_email": "luca-shared-expense@project-dd012e0e-84bf-4897-a17.iam.gserviceaccount.com",
  "client_id": "118297563139446120946",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/luca-shared-expense%40project-dd012e0e-84bf-4897-a17.iam.gserviceaccount.com",
  "universe_domain": "googleapis.com"
}
```

⚠️ **Penting**: 
- Pastikan copy SELURUH JSON (dari `{` sampai `}`)
- Jangan edit apapun
- Harus include semua baris termasuk `private_key`

---

## 📍 Langkah 5: Klik "Add secret"

Klik tombol **"Add secret"** di kanan bawah form.

---

## 📍 Langkah 6: Tunggu Space Restart

Setelah add secret, Space akan **otomatis restart**.

Kamu akan lihat:
```
🔄 Building...
```

Tunggu 2-3 menit sampai status berubah jadi:
```
✅ Running
```

---

## 📍 Langkah 7: Verify Berhasil

### Test 1: Cek di Browser
Buka:
```
https://lucashared-luca-shared-expense.hf.space/health
```

**Harusnya return:**
```json
{
  "status": "healthy",
  "google_vision": "connected"  ✅
}
```

### Test 2: Cek Logs
Di Space UI, klik tab **"Logs"**.

**Harusnya ada:**
```
🚀 Initializing Google Vision API...
✅ Using GOOGLE_CREDENTIALS_JSON from secrets
✅ Google Vision Ready!
```

---

## 📍 Langkah 8: Test dari Android

1. Buka Android app
2. Pilih gambar receipt atau ambil foto
3. Klik upload
4. **Harusnya berhasil!** 🎉

---

## 🐛 Troubleshooting

### Error: "Invalid JSON"
**Penyebab**: JSON tidak lengkap atau ada karakter terhapus

**Solusi**: 
1. Delete secret yang salah
2. Copy ulang dari file `google-credentials.json` ASLI
3. Add secret lagi

---

### Error: "Permission denied"
**Penyebab**: Service account tidak punya akses ke Google Vision API

**Solusi**:
1. Buka Google Cloud Console
2. Pergi ke "IAM & Admin" > "Service Accounts"
3. Pilih service account `luca-shared-expense@...`
4. Tambahkan role "Cloud Vision API User"

---

### Space Tidak Restart
**Solusi**: 
1. Pergi ke Settings
2. Klik "Factory reboot" (restart manual)
3. Tunggu 2-3 menit

---

## ✅ Checklist

- [ ] Buka Space Settings
- [ ] Scroll ke "Repository secrets"
- [ ] Klik "Add a secret"
- [ ] Name: `GOOGLE_CREDENTIALS_JSON` (persis!)
- [ ] Value: Copy seluruh `google-credentials.json`
- [ ] Klik "Add secret"
- [ ] Tunggu Space restart (2-3 menit)
- [ ] Test `/health` endpoint
- [ ] Test upload dari Android

---

## 🎯 Setelah Selesai

Upload dari Android app akan langsung work! 🚀

Tidak perlu:
- ❌ Edit code
- ❌ Push ke Git
- ❌ Rebuild app
- ❌ Restart handphone

Cukup add secret, **DONE!** ✅

