import requests

url = "https://lucashared-luca-shared-expense.hf.space"

print("\n" + "="*60)
print("🧪 TESTING API")
print("="*60 + "\n")

try:
    r = requests.get(url, timeout=30)
    data = r.json()

    print(f"Status: {data.get('status')}")
    print(f"Version: {data.get('version')}")
    print(f"Google Vision: {data.get('google_vision_status')}")

    if data.get('google_vision_status') == 'connected':
        print("\n" + "="*60)
        print("✅ SUCCESS! GOOGLE VISION CONNECTED!")
        print("="*60)
        print("\n🎉 API READY! Upload dari Android sekarang!")
    else:
        print("\n" + "="*60)
        print("❌ GAGAL! Vision masih: " + data.get('google_vision_status'))
        print("="*60)

except Exception as e:
    print(f"❌ Error: {e}")

print()

