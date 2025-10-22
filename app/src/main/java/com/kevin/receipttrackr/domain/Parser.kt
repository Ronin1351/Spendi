package com.kevin.receipttrackr.domain

import com.kevin.receipttrackr.data.db.ParsedLineItem
import com.kevin.receipttrackr.data.db.ParsedReceipt
import com.kevin.receipttrackr.debug.Logger
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Parser @Inject constructor(
    private val categorizer: Categorizer
) {
    private val tag = "Parser"

    suspend fun parse(ocrText: String): ParsedReceipt {
        Logger.d(tag, "Parsing OCR text (${ocrText.length} chars)")
        val lines = ocrText.lines().map { it.trim() }.filter { it.isNotEmpty() }

        val merchant = extractMerchant(lines)
        val dateEpochMs = extractDate(lines)
        val totals = extractTotals(lines)
        val items = extractLineItems(lines, totals)

        Logger.d(tag, "Parsed: merchant=$merchant, items=${items.size}, total=${totals["total"]}")

        return ParsedReceipt(
            merchant = merchant,
            dateEpochMs = dateEpochMs,
            subtotalCents = totals["subtotal"],
            taxCents = totals["tax"],
            totalCents = totals["total"],
            items = items,
            rawText = ocrText
        )
    }

    private fun extractMerchant(lines: List<String>): String {
        // Take first non-empty line as merchant (heuristic)
        return lines.firstOrNull()?.take(50) ?: "Unknown Merchant"
    }

    private fun extractDate(lines: List<String>): Long? {
        val datePatterns = listOf(
            // YYYY-MM-DD or YYYY/MM/DD
            Regex("""(\d{4}[-/]\d{1,2}[-/]\d{1,2})"""),
            // DD-MM-YYYY or MM-DD-YYYY
            Regex("""(\d{1,2}[-/]\d{1,2}[-/]\d{2,4})"""),
            // DD Mon YYYY
            Regex("""(\d{1,2}\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{2,4})""", RegexOption.IGNORE_CASE)
        )

        val dateFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
            SimpleDateFormat("yyyy/MM/dd", Locale.US),
            SimpleDateFormat("dd-MM-yyyy", Locale.US),
            SimpleDateFormat("MM-dd-yyyy", Locale.US),
            SimpleDateFormat("dd/MM/yyyy", Locale.US),
            SimpleDateFormat("MM/dd/yyyy", Locale.US),
            SimpleDateFormat("dd MMM yyyy", Locale.US),
            SimpleDateFormat("dd MMMM yyyy", Locale.US)
        )

        for (line in lines) {
            for (pattern in datePatterns) {
                val match = pattern.find(line)
                if (match != null) {
                    val dateStr = match.groupValues[1]
                    for (format in dateFormats) {
                        try {
                            val date = format.parse(dateStr)
                            if (date != null) {
                                Logger.d(tag, "Extracted date: $dateStr -> ${date.time}")
                                return date.time
                            }
                        } catch (e: Exception) {
                            // Try next format
                        }
                    }
                }
            }
        }

        Logger.d(tag, "No date found, returning null")
        return null
    }

    private fun extractTotals(lines: List<String>): Map<String, Int?> {
        var subtotal: Int? = null
        var tax: Int? = null
        var total: Int? = null

        val priceRegex = Regex("""([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{2})?|\d+\.\d{2}|\d+)$""")

        for (line in lines) {
            val lowerLine = line.lowercase()
            val priceMatch = priceRegex.find(line)

            if (priceMatch != null) {
                val priceStr = priceMatch.groupValues[1].replace(",", "")
                val priceCents = parsePriceToCents(priceStr)

                when {
                    lowerLine.contains("subtotal") && subtotal == null -> subtotal = priceCents
                    (lowerLine.contains("tax") || lowerLine.contains("vat") || lowerLine.contains("service")) && tax == null -> tax = priceCents
                    lowerLine.contains("total") && !lowerLine.contains("subtotal") && total == null -> total = priceCents
                }
            }
        }

        Logger.d(tag, "Totals: subtotal=$subtotal, tax=$tax, total=$total")
        return mapOf("subtotal" to subtotal, "tax" to tax, "total" to total)
    }

    private suspend fun extractLineItems(lines: List<String>, totals: Map<String, Int?>): List<ParsedLineItem> {
        val items = mutableListOf<ParsedLineItem>()
        val priceRegex = Regex("""([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{2})?|\d+\.\d{2}|\d+)$""")
        val qtyRegex = Regex("""^(\d+)\s*[@x]""", RegexOption.IGNORE_CASE)

        val totalKeywords = setOf("subtotal", "tax", "vat", "gst", "service", "balance", "change", "payment", "amount due", "cash")

        for (line in lines) {
            val lowerLine = line.lowercase().trim()

            // Skip total lines - use word boundary matching to avoid false positives
            val isTotalLine = totalKeywords.any { keyword ->
                lowerLine.startsWith(keyword) || lowerLine.endsWith(keyword) ||
                lowerLine.matches(Regex(".*\\b${Regex.escape(keyword)}\\b.*"))
            }
            if (isTotalLine && lowerLine.split(" ").size <= 3) continue

            // Must have a price at end
            val priceMatch = priceRegex.find(line) ?: continue
            val priceStr = priceMatch.groupValues[1].replace(",", "")
            val priceCents = parsePriceToCents(priceStr)

            // Skip if price is 0
            if (priceCents == 0) continue

            // Extract name (everything before the price)
            val nameEnd = priceMatch.range.first
            var name = line.substring(0, nameEnd).trim()

            // Extract quantity
            var qty = 1
            val qtyMatch = qtyRegex.find(name)
            if (qtyMatch != null) {
                qty = qtyMatch.groupValues[1].toIntOrNull() ?: 1
                name = name.substring(qtyMatch.value.length).trim()
            }

            // Skip if name is too short
            if (name.length < 2) continue

            val category = categorizer.categorize(name)

            items.add(
                ParsedLineItem(
                    name = name,
                    qty = qty,
                    amountCents = priceCents,
                    category = category,
                    rawText = line
                )
            )
        }

        Logger.d(tag, "Extracted ${items.size} line items")
        return items
    }

    private fun parsePriceToCents(priceStr: String): Int {
        return try {
            // Remove currency symbols and whitespace
            var cleanStr = priceStr.replace(Regex("[\\s$€£¥₹]"), "")

            // Handle negative prices (refunds)
            val isNegative = cleanStr.startsWith("-")
            cleanStr = cleanStr.removePrefix("-")

            // Handle comma as thousand separator (e.g., 1,250.99)
            if (cleanStr.contains(",") && cleanStr.contains(".")) {
                cleanStr = cleanStr.replace(",", "")
            }
            // Handle comma as decimal separator (European format, e.g., 12,50)
            else if (cleanStr.contains(",") && !cleanStr.contains(".")) {
                cleanStr = cleanStr.replace(",", ".")
            }

            val price = cleanStr.toDoubleOrNull() ?: return 0
            val cents = (price * 100).toInt()

            if (isNegative) -cents else cents
        } catch (e: Exception) {
            Logger.e(tag, "Failed to parse price: $priceStr - ${e.message}")
            0
        }
    }
}
