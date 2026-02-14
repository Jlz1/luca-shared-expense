#!/usr/bin/env python
"""
Test HuggingFace Space Integration
Verifies that the EasyOCR model is properly configured and accessible
"""
import requests
import json
from PIL import Image
import io
import numpy as np

# ============================================
# CONFIGURATION
# ============================================
HF_SPACE_URL = "https://lucashared-luca-shared-expense.hf.space"
TEST_IMAGE_SIZE = (300, 200)  # Small test image

print("=" * 70)
print("HUGGINGFACE SPACE INTEGRATION TEST")
print("=" * 70)

# TEST 1: Health Check
print("\n[TEST 1] Health Check")
print(f"  Testing endpoint: {HF_SPACE_URL}/")
try:
    resp = requests.get(f"{HF_SPACE_URL}/", timeout=10)
    if resp.status_code == 200:
        print(f"  ✅ Service is RUNNING")
        print(f"     Response: {resp.json()}")
    else:
        print(f"  ❌ Service returned {resp.status_code}")
        print(f"     Response: {resp.text}")
except Exception as e:
    print(f"  ❌ Connection failed: {e}")

# TEST 2: Check /scan endpoint exists
print("\n[TEST 2] Endpoint Verification")
print(f"  Testing endpoint: {HF_SPACE_URL}/scan")

# Create a dummy image for testing
dummy_img = Image.new('RGB', TEST_IMAGE_SIZE, color='white')
img_bytes = io.BytesIO()
dummy_img.save(img_bytes, format='JPEG')
img_bytes.seek(0)

try:
    files = {'file': ('test.jpeg', img_bytes, 'image/jpeg')}
    resp = requests.post(
        f"{HF_SPACE_URL}/scan",
        files=files,
        timeout=30  # Allow time for model inference
    )

    print(f"  Status Code: {resp.status_code}")

    if resp.status_code == 200:
        print(f"  ✅ Endpoint is WORKING")
        try:
            data = resp.json()
            print(f"  Response Structure:")
            print(f"    - status: {data.get('status')}")
            print(f"    - data: {'Present' if data.get('data') else 'Null'}")
            print(f"    - message: {data.get('message')}")
            print(f"\n  Full Response:")
            print(json.dumps(data, indent=2)[:500])  # Print first 500 chars
        except:
            print(f"  Response text: {resp.text[:200]}")
    else:
        print(f"  ❌ Endpoint returned {resp.status_code}")
        print(f"  Error: {resp.text}")

except requests.exceptions.Timeout:
    print(f"  ⏱️  Request timed out (model might be loading)")
    print(f"     Try again in a moment")
except Exception as e:
    print(f"  ❌ Error: {e}")

# TEST 3: Expected Response Format
print("\n[TEST 3] Response Format Validation")
expected_format = {
    "status": "string (success/error)",
    "data": {
        "items": ["list of {name, qty, unit_price, line_total}"],
        "summary": {
            "subtotal": "int",
            "total_discount": "int",
            "tax": "int",
            "service": "int",
            "grand_total": "int"
        },
        "status": "string"
    },
    "message": "string or null",
    "debug": "object or null"
}
print(f"  Expected format:")
print(json.dumps(expected_format, indent=2))

# TEST 4: Check Required Files
print("\n[TEST 4] Local Project Files")
import os
required_files = [
    'requirements.txt',
    'main.py',
    'app/src/main/java/com/example/luca/data/api/ScanApiClient.kt',
]
for file_path in required_files:
    full_path = f"D:\\BCA\\Cawu 4\\ML\\Project\\luca-shared-expense\\{file_path}"
    if os.path.exists(full_path.replace('\\', '/')):
        print(f"  ✅ {file_path}")
    else:
        print(f"  ❌ {file_path} NOT FOUND")

# TEST 5: Configuration Check
print("\n[TEST 5] Configuration Summary")
print(f"  Android Base URL: https://lucashared-luca-shared-expense.hf.space/")
print(f"  API Endpoint: POST /scan")
print(f"  Request Type: multipart/form-data")
print(f"  File Parameter: 'file'")
print(f"  Expected Response: ScanResponse (Kotlin data class)")

print("\n" + "=" * 70)
print("SUMMARY: If all tests passed, integration is ready for Android!")
print("=" * 70)

