import requests
import sys

print("\n" + "="*70)
print("🔍 TESTING HUGGING FACE API")
print("="*70 + "\n")

url = "https://lucashared-luca-shared-expense.hf.space"

try:
    print("[1/3] Testing root endpoint (GET /)...")
    r = requests.get(url, timeout=30)

    if r.status_code == 200:
        data = r.json()
        print(f"  ✅ Status: {data.get('status')}")
        print(f"  ✅ Version: {data.get('version')}")

        vision_status = data.get('google_vision_status', 'unknown')

        if vision_status == 'connected':
            print(f"  ✅ Google Vision: {vision_status} 🎉")
        elif vision_status == 'not configured':
            print(f"  ❌ Google Vision: {vision_status}")
        else:
            print(f"  ⚠️  Google Vision: {vision_status}")
    else:
        print(f"  ❌ HTTP {r.status_code}")
        sys.exit(1)

except requests.exceptions.Timeout:
    print("  ⏱️  Timeout - Space mungkin sedang sleeping/restarting")
    print("  💡 Coba lagi dalam 1-2 menit atau buka URL di browser dulu")
    sys.exit(1)
except requests.exceptions.ConnectionError:
    print("  ❌ Connection error - Tidak bisa terhubung ke server")
    sys.exit(1)
except Exception as e:
    print(f"  ❌ Error: {e}")
    sys.exit(1)

print()

try:
    print("[2/3] Testing health endpoint (GET /health)...")
    r = requests.get(f"{url}/health", timeout=30)

    if r.status_code == 200:
        data = r.json()
        print(f"  ✅ Status: {data.get('status')}")
        print(f"  ✅ Google Vision: {data.get('google_vision')}")
    elif r.status_code == 500:
        data = r.json()
        print(f"  ⚠️  Status: {data.get('status')}")
        print(f"  ❌ Google Vision: {data.get('google_vision')}")
    else:
        print(f"  ⚠️  HTTP {r.status_code}")

except Exception as e:
    print(f"  ⚠️  Error: {e}")

print()

try:
    print("[3/3] Testing parse endpoint (POST /parse - no file)...")
    r = requests.post(f"{url}/parse", timeout=30)

    if r.status_code == 400:
        data = r.json()
        if "No file uploaded" in data.get('message', ''):
            print(f"  ✅ Endpoint working (correctly rejects empty request)")
        else:
            print(f"  ⚠️  Unexpected: {data.get('message')}")
    elif r.status_code == 500:
        data = r.json()
        if "not configured" in data.get('message', '').lower():
            print(f"  ❌ Google Vision not configured!")
        else:
            print(f"  ❌ Server error: {data.get('message')}")
    else:
        print(f"  ⚠️  HTTP {r.status_code}")

except Exception as e:
    print(f"  ⚠️  Error: {e}")

print("\n" + "="*70)

# Final verdict
print("\n🎯 KESIMPULAN:\n")

r = requests.get(url, timeout=30)
data = r.json()
vision = data.get('google_vision_status', 'unknown')

if vision == 'connected':
    print("✅✅✅ GOOGLE VISION CONNECTED! ✅✅✅")
    print("\n🎉 API SIAP DIGUNAKAN!")
    print("📱 Sekarang coba upload dari Android app!")
    print(f"\n📌 URL: {url}")
elif vision == 'not configured':
    print("❌❌❌ GOOGLE VISION BELUM DIKONFIGURASI! ❌❌❌")
    print("\n⚠️  Secret GOOGLE_CREDENTIALS_JSON belum di-set atau salah")
    print("\n📋 Cek:")
    print("  1. Secret name persis: GOOGLE_CREDENTIALS_JSON (case-sensitive)")
    print("  2. Value lengkap (dari { sampai })")
    print("  3. Space sudah restart setelah add secret")
    print(f"  4. Cek logs di: https://huggingface.co/spaces/lucaShared/luca-shared-expense")
else:
    print(f"⚠️  Status tidak dikenal: {vision}")
    print(f"   Cek logs di Hugging Face Space")

print("\n" + "="*70 + "\n")

