import os
import cv2
import numpy as np
from flask import Flask, request, jsonify
from paddleocr import PaddleOCR
import difflib
import re

# Inisialisasi Aplikasi Flask
app = Flask(__name__)

# --- 1. INIT MODEL ML (Otaknya) ---
print("🚀 Loading PaddleOCR Model...")
ocr_engine = PaddleOCR(use_angle_cls=True, lang='en', use_gpu=False, show_log=False)
print("✅ Model Ready!")

# ==========================================
# BAGIAN A: FUNGSI-FUNGSI ML & IMAGE PROCESSING
# ==========================================

def unsharp_mask(image, kernel_size=(5, 5), sigma=1.0, amount=1.0, threshold=0):
    blurred = cv2.GaussianBlur(image, kernel_size, sigma)
    sharpened = float(amount + 1) * image - float(amount) * blurred
    sharpened = np.maximum(sharpened, 0)
    sharpened = np.minimum(sharpened, 255)
    return sharpened.astype(np.uint8)

def order_points(pts):
    rect = np.zeros((4, 2), dtype="float32")
    s = pts.sum(axis=1)
    rect[0] = pts[np.argmin(s)]; rect[2] = pts[np.argmax(s)]
    diff = np.diff(pts, axis=1)
    rect[1] = pts[np.argmin(diff)]; rect[3] = pts[np.argmax(diff)]
    return rect

def four_point_transform(image, pts):
    rect = order_points(pts)
    (tl, tr, br, bl) = rect
    widthA = np.sqrt(((br[0] - bl[0]) ** 2) + ((br[1] - bl[1]) ** 2))
    widthB = np.sqrt(((tr[0] - tl[0]) ** 2) + ((tr[1] - tl[1]) ** 2))
    maxWidth = max(int(widthA), int(widthB))
    heightA = np.sqrt(((tr[0] - br[0]) ** 2) + ((tr[1] - br[1]) ** 2))
    heightB = np.sqrt(((tl[0] - bl[0]) ** 2) + ((tl[1] - bl[1]) ** 2))
    maxHeight = max(int(heightA), int(heightB))
    dst = np.array([[0, 0],[maxWidth - 1, 0],[maxWidth - 1, maxHeight - 1],[0, maxHeight - 1]], dtype="float32")
    M = cv2.getPerspectiveTransform(rect, dst)
    return cv2.warpPerspective(image, M, (maxWidth, maxHeight), borderMode=cv2.BORDER_CONSTANT, borderValue=(255, 255, 255))

def safe_crop_with_padding(image, x, y, w, h, padding_pct=0.1):
    h_img, w_img = image.shape[:2]
    pad_w = int(w * padding_pct); pad_h = int(h * padding_pct)
    x1 = max(0, x - pad_w); y1 = max(0, y - pad_h)
    x2 = min(w_img, x + w + pad_w); y2 = min(h_img, y + h + pad_h)
    return image[y1:y2, x1:x2]

def auto_scan_document(image):
    orig = image.copy()
    ratio = image.shape[0] / 500.0
    h = 500
    w = int(image.shape[1] / ratio)
    small_img = cv2.resize(image, (w, h))
    total_area_calc = h * w
    gray = cv2.cvtColor(small_img, cv2.COLOR_BGR2GRAY) if len(small_img.shape) == 3 else small_img
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))
    gray = clahe.apply(gray)
    blurred = cv2.GaussianBlur(gray, (5, 5), 0)
    _, thresh = cv2.threshold(blurred, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    contours, _ = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
    contours = sorted(contours, key=cv2.contourArea, reverse=True)[:5]

    screenCnt = None; largest_contour = None; max_area = 0
    for c in contours:
        area = cv2.contourArea(c)
        if area > max_area: max_area = area; largest_contour = c
        if area < (total_area_calc * 0.05): continue
        peri = cv2.arcLength(c, True)
        approx = cv2.approxPolyDP(c, 0.02 * peri, True)
        if len(approx) == 4: screenCnt = approx; break

    if screenCnt is not None:
        pts = screenCnt.reshape(4, 2) * ratio
        return four_point_transform(orig, pts), True
    elif largest_contour is not None and max_area > (total_area_calc * 0.1):
        x, y, w_box, h_box = cv2.boundingRect(largest_contour)
        x = int(x * ratio); y = int(y * ratio); w_box = int(w_box * ratio); h_box = int(h_box * ratio)
        return safe_crop_with_padding(orig, x, y, w_box, h_box, padding_pct=0.1), False
    else:
        h_orig, w_orig = orig.shape[:2]
        crop_h, crop_w = int(h_orig * 0.7), int(w_orig * 0.7)
        y1 = (h_orig - crop_h)//2; x1 = (w_orig - crop_w)//2
        return orig[y1:y1+crop_h, x1:x1+crop_w], False

def rotate_image(image, angle):
    (h, w) = image.shape[:2]
    (cX, cY) = (w // 2, h // 2)
    M = cv2.getRotationMatrix2D((cX, cY), angle, 1.0)
    cos = np.abs(M[0, 0]); sin = np.abs(M[0, 1])
    nW = int((h * sin) + (w * cos)); nH = int((h * cos) + (w * sin))
    M[0, 2] += (nW / 2) - cX; M[1, 2] += (nH / 2) - cY
    return cv2.warpAffine(image, M, (nW, nH), borderMode=cv2.BORDER_CONSTANT, borderValue=(255, 255, 255))

def detect_orientation_and_deskew(image):
    img_padded = cv2.copyMakeBorder(image, 20, 20, 20, 20, cv2.BORDER_CONSTANT, value=(255, 255, 255))
    gray = cv2.cvtColor(img_padded, cv2.COLOR_BGR2GRAY) if len(img_padded.shape) == 3 else img_padded
    thresh = cv2.adaptiveThreshold(gray, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY_INV, 25, 15)
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (15, 3))
    dilated = cv2.dilate(thresh, kernel, iterations=1)
    contours, _ = cv2.findContours(dilated, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    horizontal_votes = 0; vertical_votes = 0; angles = []
    for c in contours:
        area = cv2.contourArea(c)
        if area < 100 or area > (image.shape[0] * image.shape[1] * 0.5): continue
        rect = cv2.minAreaRect(c)
        (center, (w, h), angle) = rect
        if w < h: w, h = h, w; angle += 90
        aspect_ratio = w / float(h) if h > 0 else 0
        if aspect_ratio > 2.0:
            horizontal_votes += 1
            if angle > 45: angle -= 90
            elif angle < -45: angle += 90
            angles.append(angle)
        elif aspect_ratio < 0.5: vertical_votes += 1

    if vertical_votes > horizontal_votes * 1.5:
        return cv2.rotate(image, cv2.ROTATE_90_CLOCKWISE)
    else:
        if len(angles) > 5:
            median = np.median(angles)
            if abs(median) > 20 or abs(median) < 0.5: return image
            return rotate_image(image, median)
        return image

def tuning_lab(image, blur_kernel=21, denoise_h=10, upscale=False):
    if image is None: return None
    processed_img, success = auto_scan_document(image)
    processed_img = detect_orientation_and_deskew(processed_img)
    if upscale:
        processed_img = cv2.resize(processed_img, None, fx=2.0, fy=2.0, interpolation=cv2.INTER_CUBIC)
    processed_img = unsharp_mask(processed_img, amount=1.5)
    return processed_img

def apply_spell_correction(text_line):
    RECEIPT_KEYWORDS = ["TOTAL", "SUBTOTAL", "CASH", "CHANGE", "PAYMENT", "TAX", "PAJAK", "PPN", "HARGA", "QTY", "RECEIPT", "STRUK", "TUNAI", "DISKON"]
    words = text_line.split()
    corrected_words = []
    for word in words:
        clean_word = ''.join(e for e in word if e.isalnum())
        if len(clean_word) < 3: corrected_words.append(word); continue
        matches = difflib.get_close_matches(clean_word.upper(), RECEIPT_KEYWORDS, n=1, cutoff=0.75)
        corrected_words.append(matches[0] if matches else word)
    return " ".join(corrected_words)

def cleanup_text_spacing(text):
    text = re.sub(r'(\d)\s*([.,])\s*(\d)', r'\1\2\3', text)
    text = re.sub(r'(@)\s+(\d)', r'\1\2', text)
    text = re.sub(r'(Rp)\s+(\d)', r'\1\2', text, flags=re.IGNORECASE)
    return text

def group_lines_by_height_overlap(results, overlap_threshold=0.5):
    if not results: return ""
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
                if distance > 20: line_str += " \t " + text
                else: line_str += " " + text
            else: line_str += text
        clean_line = cleanup_text_spacing(apply_spell_correction(line_str))
        final_text_lines.append(clean_line)
    return "\n".join(final_text_lines)

def smart_filter_receipt(text, context_window=1):
    if not text or text.strip() == "": return ""
    lines = text.split('\n')
    blacklist_patterns = [
        r'No\s*[:.]?\s*[A-Z0-9]{10,}', r'\d{2}[-./]\d{2}[-./]\d{2,4}',
        r'Tanggal\s*:', r'Info\s*:', r'Mode\s*:', r'Table\s*:?', r'Bill\s*:',
        r'Receipt\s*:', r'[A-Z\s]{5,}\d{5,}', r'/\d+\.\d+\.\d+/',
        r'[A-Z]{2,}\d{2,4}-\d{4,}', r'Phone|Fax|Email|Website',
        r'Welcome|Thank|Terima',
    ]
    blacklist_compiled = [re.compile(pattern, re.IGNORECASE) for pattern in blacklist_patterns]
    strong_money_patterns = [
        r'(?:Rp\.?\s*|@\s*)\d[\d.,]+', r'\b\d{1,3}[.,]\d{3}(?:[.,]\d{3})*\b', r'^\s*\d{3,}\s*$'
    ]
    strong_money_compiled = [re.compile(p, re.IGNORECASE) for p in strong_money_patterns]
    keywords = [
        'total', 'subtotal', 'tax', 'pajak', 'ppn', 'cash', 'tunai',
        'change', 'kembalian', 'payment', 'bayar', 'diskon', 'discount',
        'harga', 'price', 'qty', 'jumlah', 'item', 'grand', 'service',
        'dana', 'qris', 'gopay', 'debit', 'credit', 'card', 'paid'
    ]
    keyword_pattern = re.compile(r'\b(' + '|'.join(keywords) + r')\b', re.IGNORECASE)
    relevant_indices = set()
    for i, line in enumerate(lines):
        line_stripped = line.strip()
        if not line_stripped: continue
        is_blacklisted = any(pattern.search(line_stripped) for pattern in blacklist_compiled)
        if is_blacklisted: continue
        has_keyword = keyword_pattern.search(line_stripped)
        has_strong_money = any(pattern.search(line_stripped) for pattern in strong_money_compiled)
        if has_keyword or has_strong_money:
            relevant_indices.add(i)
            for j in range(max(0, i - context_window), i): relevant_indices.add(j)
            for j in range(i + 1, min(len(lines), i + context_window + 1)): relevant_indices.add(j)
    
    if not relevant_indices: return "Tidak ada transaksi terdeteksi"
    sorted_indices = sorted(relevant_indices)
    filtered_lines = [lines[i] for i in sorted_indices]
    return '\n'.join(filtered_lines)

# ==========================================
# BAGIAN B: API SERVER (Pintu Masuk Android)
# ==========================================

@app.route('/', methods=['GET', 'POST'])
def process_receipt_api():
    # Handle GET request
    if request.method == 'GET':
        return jsonify({
            "status": "online",
            "message": "Luca OCR API Running 🚀",
            "version": "1.0"
        }), 200
    
    # Handle POST request
    if 'file' not in request.files:
        return jsonify({"status": "error", "message": "No file uploaded"}), 400
    
    file = request.files['file']
    
    try:
        file_bytes = np.frombuffer(file.read(), np.uint8)
        original_img = cv2.imdecode(file_bytes, cv2.IMREAD_COLOR)
        
        if original_img is None:
            return jsonify({"status": "error", "message": "Image corrupt/unreadable"}), 400

        result_image = tuning_lab(original_img, denoise_h=10, upscale=True)

        if result_image is None:
             return jsonify({"status": "error", "message": "Preprocessing failed"}), 500

        paddle_raw_result = ocr_engine.ocr(result_image)
        
        formatted_results = []
        if paddle_raw_result:
            source = paddle_raw_result[0] if len(paddle_raw_result) > 0 and isinstance(paddle_raw_result[0], list) else paddle_raw_result
            
            for line in source:
                try:
                    box = line[0]
                    txt = line[1][0]
                    conf = line[1][1]
                    formatted_results.append((box, txt, conf))
                except: 
                    continue

        if formatted_results:
            full_text = group_lines_by_height_overlap(formatted_results, overlap_threshold=0.5)
            filtered_text = smart_filter_receipt(full_text, context_window=1)
            
            return jsonify({
                "status": "success",
                "raw_text": full_text,
                "filtered_text": filtered_text
            })
        else:
            return jsonify({
                "status": "success", 
                "message": "No text detected", 
                "filtered_text": ""
            })

    except Exception as e:
        print(f"Server Error: {e}")
        return jsonify({"status": "error", "message": str(e)}), 500

# ==========================================
# MAIN ENTRY POINT
# ==========================================
if __name__ == "__main__":
    port = int(os.environ.get("PORT", 8080))
    app.run(debug=True, host="0.0.0.0", port=port)