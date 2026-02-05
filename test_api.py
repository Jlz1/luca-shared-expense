# ==============================================================================
# test_api.py - Local Testing Script for Receipt Parser API
# ==============================================================================
import requests
import sys
import os

def test_health_check(base_url):
    """Test the health endpoint"""
    print("\n" + "="*70)
    print("🏥 Testing Health Check Endpoint")
    print("="*70)

    try:
        response = requests.get(f"{base_url}/health", timeout=10)
        print(f"Status Code: {response.status_code}")
        print(f"Response: {response.json()}")
        return response.status_code == 200
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def test_home_endpoint(base_url):
    """Test the home endpoint"""
    print("\n" + "="*70)
    print("🏠 Testing Home Endpoint")
    print("="*70)

    try:
        response = requests.get(base_url, timeout=10)
        print(f"Status Code: {response.status_code}")
        data = response.json()
        print(f"Version: {data.get('version')}")
        print(f"Status: {data.get('status')}")
        print(f"Google Vision: {data.get('google_vision_status')}")
        print(f"Accuracy: {data.get('accuracy')}")
        return response.status_code == 200
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def test_parse_endpoint(base_url, image_path):
    """Test the parse endpoint with an image"""
    print("\n" + "="*70)
    print("📄 Testing Parse Endpoint")
    print("="*70)

    if not os.path.exists(image_path):
        print(f"❌ Image file not found: {image_path}")
        return False

    try:
        with open(image_path, 'rb') as f:
            files = {'file': f}
            print(f"Uploading: {image_path}")
            print("⏳ Processing... (this may take 30-60 seconds)")

            response = requests.post(
                f"{base_url}/parse",
                files=files,
                timeout=120
            )

            print(f"\nStatus Code: {response.status_code}")
            data = response.json()

            if response.status_code == 200 and data.get('status') == 'success':
                print("✅ Success!")

                receipt_data = data.get('data', {})
                items = receipt_data.get('items', [])
                summary = receipt_data.get('summary', {})
                debug = data.get('debug', {})

                print(f"\n📊 Results:")
                print(f"  Items Found: {len(items)}")
                print(f"  Words Detected: {debug.get('words_detected', 0)}")
                print(f"  Status: {receipt_data.get('status')}")

                print(f"\n🛒 Items:")
                for i, item in enumerate(items[:5], 1):  # Show first 5 items
                    print(f"  {i}. {item['name']}")
                    print(f"     Qty: {item['qty']} × Rp {item['unit_price']:,} = Rp {item['line_total']:,}")

                if len(items) > 5:
                    print(f"  ... and {len(items) - 5} more items")

                print(f"\n💰 Summary:")
                print(f"  Subtotal:  Rp {summary.get('subtotal', 0):,}")
                print(f"  Discount:  Rp {summary.get('total_discount', 0):,}")
                print(f"  Tax:       Rp {summary.get('tax', 0):,}")
                print(f"  Service:   Rp {summary.get('service', 0):,}")
                print(f"  Total:     Rp {summary.get('grand_total', 0):,}")
                print(f"  Diff:      Rp {summary.get('diff', 0):,}")

                return True
            else:
                print(f"❌ Error: {data.get('message', 'Unknown error')}")
                return False

    except requests.exceptions.Timeout:
        print("⏱️ Request timed out. The server might be cold starting.")
        return False
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def main():
    """Main test function"""
    print("="*70)
    print("🧪 LUCA Receipt Parser API - Test Suite")
    print("="*70)

    # Configuration
    if len(sys.argv) > 1:
        base_url = sys.argv[1]
    else:
        base_url = "http://localhost:7860"

    print(f"📡 Testing API at: {base_url}")

    # Run tests
    results = {
        "home": test_home_endpoint(base_url),
        "health": test_health_check(base_url)
    }

    # Test parse endpoint if image provided
    if len(sys.argv) > 2:
        image_path = sys.argv[2]
        results["parse"] = test_parse_endpoint(base_url, image_path)
    else:
        print("\n" + "="*70)
        print("ℹ️  To test parse endpoint, provide an image path:")
        print(f"   python test_api.py {base_url} /path/to/receipt.jpg")
        print("="*70)

    # Summary
    print("\n" + "="*70)
    print("📋 Test Summary")
    print("="*70)
    passed = sum(results.values())
    total = len(results)
    print(f"Passed: {passed}/{total}")

    for test_name, result in results.items():
        status = "✅" if result else "❌"
        print(f"{status} {test_name}")

    print("="*70)

    return passed == total

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)

