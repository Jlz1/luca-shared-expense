package com.example.luca.utils

import com.example.luca.model.ParsedReceiptData
import com.example.luca.model.ParsedReceiptItem
import java.util.Locale

object ReceiptParser {

    /**
     * Parse OCR text into structured receipt data
     */
    fun parseReceiptText(ocrText: String): ParsedReceiptData {
        if (ocrText.isBlank()) {
            return ParsedReceiptData()
        }

        val lines = ocrText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        val items = mutableListOf<ParsedReceiptItem>()
        var subtotalValue = 0.0
        var taxValue = 0.0
        var discountValue = 0.0
        var totalBillValue = 0.0

        for (line in lines) {
            // Check if line contains tax information
            if (line.contains("tax", ignoreCase = true) ||
                line.contains("pajak", ignoreCase = true) ||
                line.contains("ppn", ignoreCase = true)) {
                taxValue = extractPrice(line)
                continue
            }

            // Check if line contains discount information
            if (line.contains("diskon", ignoreCase = true) ||
                line.contains("discount", ignoreCase = true)) {
                discountValue = extractPrice(line)
                continue
            }

            // Check if line contains total bill
            if (line.contains("total", ignoreCase = true) &&
                !line.contains("subtotal", ignoreCase = true)) {
                totalBillValue = extractPrice(line)
                continue
            }

            // Check if line contains subtotal
            if (line.contains("subtotal", ignoreCase = true)) {
                subtotalValue = extractPrice(line)
                continue
            }

            // Try to parse as item line
            val item = parseItemLine(line)
            if (item != null) {
                items.add(item)
            }
        }

        // Calculate subtotal and total if not found in receipt
        if (items.isNotEmpty()) {
            if (subtotalValue == 0.0) {
                subtotalValue = items.sumOf { it.itemPrice * it.itemQuantity - it.itemDiscount }
            }

            if (totalBillValue == 0.0) {
                totalBillValue = subtotalValue + taxValue - discountValue
            }
        }

        return ParsedReceiptData(
            items = items,
            subtotal = subtotalValue,
            tax = taxValue,
            discount = discountValue,
            totalBill = totalBillValue,
            rawText = ocrText
        )
    }

    /**
     * Parse a single line to extract item information
     * Expected formats:
     * - "2x Item Name 100000"
     * - "Item Name @ 50000"
     * - "Item Name Rp 50000"
     * - "Item Name 50,000"
     */
    private fun parseItemLine(line: String): ParsedReceiptItem? {
        // Skip lines that are likely headers or totals
        if (line.contains("qty", ignoreCase = true) ||
            line.contains("item", ignoreCase = true) && line.length < 10 ||
            line.contains("harga", ignoreCase = true) && line.length < 15) {
            return null
        }

        // Extract quantity (e.g., "2x" or "x2")
        var quantity = 1
        var workingLine = line

        val qtyPattern = Regex("""(\d+)\s*[xX×]""")
        val qtyMatch = qtyPattern.find(workingLine)
        if (qtyMatch != null) {
            quantity = qtyMatch.groupValues[1].toIntOrNull() ?: 1
            workingLine = workingLine.replace(qtyMatch.value, "").trim()
        }

        // Extract price (looking for numbers with optional Rp, @, or separators)
        val price = extractPrice(workingLine)

        if (price <= 0.0) {
            return null // No valid price found
        }

        // Remove price from line to get item name
        val pricePattern = Regex("""(?:Rp\.?\s*|@\s*)?[\d.,]+""", RegexOption.IGNORE_CASE)
        val itemName = pricePattern.replace(workingLine, "").trim()

        // Filter out lines that are too short or don't look like item names
        if (itemName.length < 2 || itemName.matches(Regex("""^\d+$"""))) {
            return null
        }

        return ParsedReceiptItem(
            itemName = itemName,
            itemPrice = price,
            itemQuantity = quantity,
            itemDiscount = 0.0,
            itemTax = 0.0
        )
    }

    /**
     * Extract price value from a string
     * Handles formats like: Rp50000, 50,000, 50.000, @50000, etc.
     */
    private fun extractPrice(text: String): Double {
        // Remove common prefixes
        var cleanText = text.replace(Regex("""Rp\.?\s*""", RegexOption.IGNORE_CASE), "")
        cleanText = cleanText.replace(Regex("""@\s*"""), "")

        // Find all number patterns (with dots or commas as separators)
        val numberPattern = Regex("""[\d.,]+""")
        val matches = numberPattern.findAll(cleanText)

        // Get the largest number (likely to be the price)
        var maxNumber = 0.0
        for (match in matches) {
            val numStr = match.value
                .replace(".", "")  // Remove thousand separators
                .replace(",", "")  // Remove thousand separators

            val num = numStr.toDoubleOrNull()
            if (num != null && num > maxNumber) {
                maxNumber = num
            }
        }

        return maxNumber
    }

    /**
     * Format price to Indonesian Rupiah format
     */
    fun formatPrice(price: Double): String {
        return String.format(Locale.getDefault(), "%,.0f", price)
    }
}

