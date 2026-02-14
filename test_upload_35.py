#!/usr/bin/env python
"""Test upload 35.jpeg to HuggingFace Space"""
import requests
import json

url = 'https://lucashared-luca-shared-expense.hf.space/scan'
image_path = '35.jpeg'

print("=" * 80)
print("TESTING UPLOAD: 35.jpeg to HuggingFace Space")
print("=" * 80)
print()

try:
    with open(image_path, 'rb') as f:
        files = {'file': f}
        response = requests.post(url, files=files, timeout=120)

    print(f"Status Code: {response.status_code}")
    print()

    if response.status_code == 200:
        data = response.json()

        print("✅ SUCCESS!")
        print()
        print(f"Status: {data.get('status')}")
        print()

        if data.get('data'):
            print("ITEMS:")
            for item in data['data'].get('items', []):
                name = item.get('name', 'N/A')
                price = item.get('price', 'N/A')
                qty = item.get('qty', 'N/A')
                print(f"  - {name:40} x{qty:3} = Rp {price}")

            print()
            print("SUMMARY:")
            summary = data['data'].get('summary', {})
            print(f"  Subtotal:   Rp {summary.get('subtotal', 'N/A')}")
            print(f"  Tax:        Rp {summary.get('tax', 'N/A')}")
            print(f"  Service:    Rp {summary.get('service_charge', 'N/A')}")
            print(f"  Total:      Rp {summary.get('total', 'N/A')}")

        print()
        print("FULL JSON:")
        print(json.dumps(data, indent=2))
    else:
        print(f"❌ ERROR {response.status_code}")
        print(f"Response: {response.text}")

except Exception as e:
    print(f"❌ Exception: {e}")
    import traceback
    traceback.print_exc()

print()
print("=" * 80)

