# ==============================================================================
# main.py - Receipt Parser API v3.1 (Google Vision OCR + Rule-based)
# ==============================================================================
import os
import json
import re
from flask import Flask, request, jsonify
from google.cloud import vision
from google.oauth2 import service_account
import io
from PIL import Image

# New imports for Mindee
try:
    from mindee_parser import init_mindee_model, parse_with_mindee
except Exception:
    # mindee_parser may not be present in some branches; handle gracefully
    init_mindee_model = None
    parse_with_mindee = None

# ==============================================================================
# FLASK APP
# ==============================================================================
app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16MB max

ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp'}

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

# ==============================================================================
# INITIALIZE GOOGLE VISION
# ==============================================================================
def init_vision_client():
    """Initialize Google Vision with credentials from HF Secrets"""
    try:
        # Try environment variable first (HF Secret: GOOGLE_CREDENTIALS_JSON)
        google_creds_json = os.environ.get('GOOGLE_CREDENTIALS_JSON')

        if google_creds_json:
            print("✅ Using GOOGLE_CREDENTIALS_JSON from secrets")
            creds_dict = json.loads(google_creds_json)
            credentials = service_account.Credentials.from_service_account_info(creds_dict)
            return vision.ImageAnnotatorClient(credentials=credentials)

        # Fallback: try file
        creds_path = os.environ.get('GOOGLE_APPLICATION_CREDENTIALS')
        if creds_path and os.path.exists(creds_path):
            print(f"✅ Using credentials file: {creds_path}")
            return vision.ImageAnnotatorClient()

        print("⚠️ No credentials found, trying default...")
        return vision.ImageAnnotatorClient()

    except Exception as e:
        print(f"❌ Vision API init failed: {e}")
        import traceback
        traceback.print_exc()
        return None

print("🚀 Initializing Google Vision API...")
vision_client = init_vision_client()

# Initialize Mindee (if available)
mindee_model = None
if init_mindee_model:
    try:
        init_mindee_model()
    except Exception as e:
        print(f"[WARN] Mindee init raised: {e}")

# ==============================================================================
# GOOGLE VISION OCR
# ==============================================================================
def get_ocr_google(image_bytes):
    """Extract text using Google Vision API"""
    if not vision_client:
        return [], [], None, "Google Vision client not initialized"

    try:
        # Validate image
        try:
            img = Image.open(io.BytesIO(image_bytes))
            img.verify()
            img = Image.open(io.BytesIO(image_bytes))
        except Exception as e:
            return [], [], None, f"Invalid image format: {str(e)}"

        image = vision.Image(content=image_bytes)
        response = vision_client.document_text_detection(image=image)

        if response.error.message:
            return [], [], None, f"Google Vision API error: {response.error.message}"

        words = []
        boxes = []

        for page in response.full_text_annotation.pages:
            for block in page.blocks:
                for paragraph in block.paragraphs:
                    for word in paragraph.words:
                        word_text = ''.join([symbol.text for symbol in word.symbols])
                        words.append(word_text)

                        vertices = word.bounding_box.vertices
                        box = [
                            vertices[0].x,
                            vertices[0].y,
                            vertices[2].x,
                            vertices[2].y
                        ]
                        boxes.append(box)

        return words, boxes, img, None

    except Exception as e:
        import traceback
        traceback.print_exc()
        return [], [], None, f"OCR error: {str(e)}"

# ==============================================================================
# TEXT PROCESSING (FROM NOTEBOOK)
# ==============================================================================
def group_lines_by_height_overlap(words, boxes, overlap_threshold=0.5):
    """Group words into lines based on vertical overlap"""
    if not words or not boxes:
        return ""

    results = []
    for word, box in zip(words, boxes):
        bbox = [
            [box[0], box[1]],
            [box[2], box[1]],
            [box[2], box[3]],
            [box[0], box[3]]
        ]
        results.append((bbox, word))

    results = sorted(results, key=lambda r: r[0][0][1])
    lines = []

    while results:
        current_item = results.pop(0)
        box_curr = current_item[0]

        y_min_curr = min(box_curr[0][1], box_curr[1][1])
        y_max_curr = max(box_curr[2][1], box_curr[3][1])
        height_curr = y_max_curr - y_min_curr

        current_line = [current_item]
        remaining_results = []

        for other_item in results:
            box_other = other_item[0]
            y_min_other = min(box_other[0][1], box_other[1][1])
            y_max_other = max(box_other[2][1], box_other[3][1])

            overlap_min = max(y_min_curr, y_min_other)
            overlap_max = min(y_max_curr, y_max_other)
            overlap_h = max(0, overlap_max - overlap_min)

            height_other = y_max_other - y_min_other
            min_height = min(height_curr, height_other)

            if min_height > 0 and (overlap_h / min_height) > overlap_threshold:
                current_line.append(other_item)
            else:
                remaining_results.append(other_item)

        results = remaining_results
        current_line.sort(key=lambda item: item[0][0][0])
        lines.append(current_line)

    final_text_lines = []
    for line_items in lines:
        line_str = ""
        for i, item in enumerate(line_items):
            text = item[1]
            if i > 0:
                prev_box = line_items[i-1][0]
                curr_box = item[0]
                prev_x_end = max(prev_box[1][0], prev_box[2][0])
                curr_x_start = min(curr_box[0][0], curr_box[3][0])
                distance = curr_x_start - prev_x_end

                if distance > 20:
                    line_str += " \t " + text
                else:
                    line_str += " " + text
            else:
                line_str += text

        final_text_lines.append(line_str)

    return "\n".join(final_text_lines)

def smart_filter_receipt(text):
    """Filter receipt text"""
    if not text or text.strip() == "":
        return ""

    lines = text.split('\n')

    blacklist_patterns = [
        r'No\.|Trans|Date|Time|Tanggal|Waktu|Kasir|Table|Bill|Receipt|Welcome|Thank',
        r'Phone|Fax|Email|Website|WWW|Telp',
        r'MANDIRI|BCA|BNI|BRI|CIMB|DANAMON|PERMATA|MAYBANK',
        r'QRIS|GOPAY|OVO|DANA|LINKAJA|SHOPEEPAY',
        r'DEBIT|CREDIT|CARD|KARTU|EDC|FLASH|E-MONEY',
        r'TUNAI|PAID|LUNAS|BAYAR',
        r'\d{2}[-./]\d{2}[-./]\d{2,4}',
        r'[A-Z0-9]{12,}'
    ]

    blacklist_regex = re.compile('|'.join(blacklist_patterns), re.IGNORECASE)
    money_regex = re.compile(r'(?:Rp\.?\s*|@\s*|x\s*)?(\d{1,3}(?:[.,]\d{3})+(?:[.,]\d{0,2})?)', re.IGNORECASE)
    keyword_regex = re.compile(r'TOTAL|SUBTOTAL|TAX|PAJAK|PPN|HARGA|QTY|ITEM|DISKON|SC|SERVICE', re.IGNORECASE)

    final_lines = []

    for i, line in enumerate(lines):
        line_clean = line.strip()
        if not line_clean or blacklist_regex.search(line_clean):
            continue

        is_service_change = "SERVICE" in line_clean.upper() and "CHANGE" in line_clean.upper()

        if not is_service_change and re.search(r'CHANGE|KEMBALI|CASH', line_clean, re.IGNORECASE):
            continue

        has_money = money_regex.search(line_clean)
        has_keyword = keyword_regex.search(line_clean)

        if has_money or has_keyword:
            if has_money and not has_keyword and i > 0:
                prev_line = lines[i-1].strip()
                if (not blacklist_regex.search(prev_line) and
                    not money_regex.search(prev_line) and
                    not keyword_regex.search(prev_line) and
                    (len(final_lines) == 0 or final_lines[-1] != prev_line)):
                    final_lines.append(prev_line)
            final_lines.append(line_clean)

    return '\n'.join(final_lines)

def parse_receipt_to_json(clean_text):
    """Parse receipt to JSON"""
    if not clean_text:
        return {}

    lines = clean_text.split('\n')

    item_pattern = re.compile(r'^(.*?)\s*((?:Rp\.?\s*)?[\d,.]+)$', re.IGNORECASE)
    qty_specific_pattern = re.compile(r'(?:^|\s)[x@]\s*(\d+)|(\d+)\s*[x@]', re.IGNORECASE)
    separator_pattern = re.compile(r'^[\-=_]{3,}$')
    metadata_pattern = re.compile(r'^(OPERATOR|CASHIER|KASIR|SERVER|TABLE|TBL|POS|SHIFT|WAKTU|DATE|TIME|NO\.|ORDER|PICKUP|QUEUE|ANTRIAN)', re.IGNORECASE)

    def parse_price(price_str):
        digits = re.sub(r'\D', '', price_str)
        return int(digits) if digits else 0

    items = []
    grand_total = 0
    tax = 0
    service = 0
    total_discount = 0
    pending_name = None

    forbidden_items = re.compile(r'TOTAL|SUBTOTAL|BELANJA|JUAL|HEMAT|PAYMENT|NPWP|DPP|PURCHASE|VAT|HASE|CHANGE|KEMBALI|CASH|TUNAI|DEBIT|CREDIT|ITEM|ITEMS|QTY|MENU|EDC|MANUAL', re.IGNORECASE)
    discount_keywords = re.compile(r'DISKON|DISC|VOUCHER|POTONGAN|PROMO|HEMAT', re.IGNORECASE)

    for line in lines:
        line = line.strip()
        if not line:
            continue

        if separator_pattern.match(line) or metadata_pattern.search(line):
            pending_name = None
            continue

        if re.search(r'^(Grand\s*Total|Total\s*Bayar|Amount|Tagihan|Net|Total\s+[A-Z]+|Total\b)', line, re.IGNORECASE):
            if not re.search(r'(Sub|Disc|Hemat|Saving|Item)', line, re.IGNORECASE):
                parts = item_pattern.search(line)
                if parts:
                    found_total = parse_price(parts.group(2))
                    if found_total > 0:
                        grand_total = found_total
            continue

        if re.search(r'(Tax|Pajak|PB1|PPN|VAT)', line, re.IGNORECASE):
            parts = item_pattern.search(line)
            if parts:
                tax = parse_price(parts.group(2))
            continue

        if re.search(r'(Service|SC[:\s]|Charge)', line, re.IGNORECASE):
            parts = item_pattern.search(line)
            if parts:
                service = parse_price(parts.group(2))
            continue

        qty_match = qty_specific_pattern.search(line)
        price_match = item_pattern.search(line)

        if pending_name and price_match:
            current_line_name = price_match.group(1).strip()
            if forbidden_items.search(current_line_name):
                pending_name = None
            else:
                total_price = parse_price(price_match.group(2))

                if discount_keywords.search(pending_name):
                    total_discount += abs(total_price)
                    pending_name = None
                    continue

                calc_qty = 1
                if qty_match:
                    q_val = qty_match.group(1) or qty_match.group(2)
                    if q_val:
                        calc_qty = int(q_val)

                if not forbidden_items.search(pending_name):
                    items.append({
                        "name": pending_name,
                        "qty": calc_qty,
                        "unit_price": total_price // calc_qty if calc_qty > 0 else total_price,
                        "line_total": total_price
                    })
                pending_name = None
                continue

        if not pending_name and items and (qty_match or ("@" in line and price_match)):
            qty_val = 1
            if qty_match:
                q = qty_match.group(1) or qty_match.group(2)
                if q:
                    qty_val = int(q)

            if price_match:
                unit = parse_price(price_match.group(2))
                items[-1]['unit_price'] = unit
                if qty_val > 1:
                    items[-1]['qty'] = qty_val
            elif qty_val > 1:
                items[-1]['qty'] = qty_val
            continue

        if price_match:
            name = price_match.group(1).strip()
            total_price = parse_price(price_match.group(2))

            if forbidden_items.search(name):
                continue
            if total_price > 100000000:
                continue

            if len(name) > 1:
                if discount_keywords.search(name):
                    total_discount += abs(total_price)
                    continue

                items.append({
                    "name": name,
                    "qty": 1,
                    "unit_price": total_price,
                    "line_total": total_price
                })
            pending_name = None
            continue

        if not price_match:
            if not forbidden_items.search(line):
                if not re.match(r'^[\-=_]+$', line):
                    pending_name = line

    calc_subtotal = sum(i['line_total'] for i in items)
    final_calc_total = calc_subtotal - total_discount + tax + service

    gap = grand_total - final_calc_total

    if tax > 0 and abs(gap) == tax:
        tax = 0
        final_calc_total = grand_total

    if grand_total == 0:
        if final_calc_total > 0:
            status = "Total Not Found (Auto-Calculated)"
            grand_total = final_calc_total
        else:
            status = "Total Not Found"
    else:
        diff = grand_total - final_calc_total
        status = f"Gap {diff}" if abs(diff) > 1000 else "Balanced"

    return {
        "items": items,
        "summary": {
            "subtotal": calc_subtotal,
            "total_discount": total_discount,
            "tax": tax,
            "service": service,
            "grand_total": grand_total,
            "calculated_total": final_calc_total,
            "diff": grand_total - final_calc_total
        },
        "status": status
    }

# ==============================================================================
# FLASK ROUTES
# ==============================================================================
@app.route('/', methods=['GET'])
def home():
    return jsonify({
        "status": "online",
        "message": "Receipt Parser API 🧾",
        "version": "3.1",
        "approach": "Google Vision OCR + Rule-based Pattern Matching",
        "accuracy": "59.5% (22/37 receipts balanced on test set)",
        "google_vision_status": "connected" if vision_client else "not configured",
        "endpoints": {
            "/parse": "POST - Upload receipt image (multipart/form-data, field: 'file')",
            "/health": "GET - Health check"
        },
        "usage_example": {
            "curl": "curl -X POST -F 'file=@receipt.jpg' https://YOUR-SPACE.hf.space/parse",
            "python": "requests.post(url, files={'file': open('receipt.jpg', 'rb')})"
        }
    }), 200

@app.route('/health', methods=['GET'])
def health():
    if vision_client:
        return jsonify({
            "status": "healthy",
            "google_vision": "connected",
            "memory_usage": "~50MB"
        }), 200
    else:
        return jsonify({
            "status": "unhealthy",
            "google_vision": "not configured",
            "help": "Add GOOGLE_CREDENTIALS_JSON to Hugging Face Secrets (Settings > Repository secrets)"
        }), 500

@app.route('/parse', methods=['POST'])
def parse_receipt():
    """Main parsing endpoint"""
    if not vision_client:
        return jsonify({
            "status": "error",
            "message": "Google Vision API not configured. Add GOOGLE_CREDENTIALS_JSON to Hugging Face Secrets."
        }), 500

    if 'file' not in request.files:
        return jsonify({"status": "error", "message": "No file uploaded. Use field name 'file'"}), 400

    file = request.files['file']

    if file.filename == '':
        return jsonify({"status": "error", "message": "No file selected"}), 400

    if not allowed_file(file.filename):
        return jsonify({
            "status": "error",
            "message": f"Invalid format. Allowed: {', '.join(ALLOWED_EXTENSIONS)}"
        }), 400

    try:
        image_bytes = file.read()

        if len(image_bytes) == 0:
            return jsonify({"status": "error", "message": "Empty file"}), 400

        # Step 1: OCR with Google Vision
        words, boxes, pil_image, error = get_ocr_google(image_bytes)

        if error:
            return jsonify({"status": "error", "message": error}), 500

        if not words:
            return jsonify({
                "status": "success",
                "message": "No text detected in image",
                "data": {
                    "items": [],
                    "summary": {
                        "subtotal": 0,
                        "total_discount": 0,
                        "tax": 0,
                        "service": 0,
                        "grand_total": 0,
                        "calculated_total": 0,
                        "diff": 0
                    },
                    "status": "No Text Detected"
                }
            }), 200

        # Step 2: Group words into lines
        full_text = group_lines_by_height_overlap(words, boxes)

        # Step 3: Filter receipt text
        clean_text = smart_filter_receipt(full_text)

        # Step 4: Parse to JSON
        json_result = parse_receipt_to_json(clean_text)

        return jsonify({
            "status": "success",
            "data": json_result,
            "debug": {
                "words_detected": len(words),
                "lines_after_filter": len(clean_text.split('\n')),
                "raw_text": clean_text
            }
        }), 200

    except Exception as e:
        import traceback
        error_trace = traceback.format_exc()
        print(error_trace)
        return jsonify({
            "status": "error",
            "message": str(e),
            "type": type(e).__name__
        }), 500

@app.route('/parse-mindee', methods=['POST'])
def parse_mindee_endpoint():
    """Endpoint to parse receipt using Mindee model (ML-based). Expects multipart form with 'file'."""
    if not parse_with_mindee:
        return jsonify({"status": "error", "message": "Mindee parser not available."}), 500

    if 'file' not in request.files:
        return jsonify({"status": "error", "message": "No file uploaded. Use field name 'file'"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"status": "error", "message": "No file selected"}), 400

    if not allowed_file(file.filename):
        return jsonify({"status": "error", "message": f"Invalid format. Allowed: {', '.join(ALLOWED_EXTENSIONS)}"}), 400

    # Save to a temp path and call Mindee
    import tempfile
    try:
        with tempfile.NamedTemporaryFile(delete=False, suffix=os.path.splitext(file.filename)[1]) as tmp:
            tmp.write(file.read())
            tmp_path = tmp.name

        # Ensure mindee model was initialized and available
        try:
            # parse_with_mindee returns fields or raises
            result_fields = parse_with_mindee(tmp_path)
            # Convert result_fields to a serializable dict if needed
            try:
                # Some Mindee responses are nested dataclasses - try to convert
                def serialize_field(obj):
                    if hasattr(obj, 'to_dict'):
                        return obj.to_dict()
                    if isinstance(obj, dict):
                        return {k: serialize_field(v) for k, v in obj.items()}
                    if hasattr(obj, '__dict__'):
                        return {k: serialize_field(v) for k, v in obj.__dict__.items()}
                    return obj
                data = serialize_field(result_fields)
            except Exception:
                data = str(result_fields)

            return jsonify({"status": "success", "data": data}), 200
        except Exception as e:
            import traceback
            traceback.print_exc()
            return jsonify({"status": "error", "message": f"Mindee parsing error: {str(e)}"}), 500
    finally:
        try:
            os.unlink(tmp_path)
        except Exception:
            pass

# ==============================================================================
# LAUNCH
# ==============================================================================
if __name__ == "__main__":
    port = int(os.environ.get("PORT", 7860))
    print(f"\n{'='*70}")
    print(f"🚀 Receipt Parser API v3.1")
    print(f"{'='*70}")
    print(f"📡 Port: {port}")
    print(f"🔧 OCR: Google Vision API")
    print(f"⚙️  Parser: Rule-based Pattern Matching")
    print(f"📊 Accuracy: 59.5% (22/37 test receipts)")
    print(f"💾 Resource: ~50MB RAM")
    print(f"{'='*70}\n")
    app.run(debug=False, host="0.0.0.0", port=port)

