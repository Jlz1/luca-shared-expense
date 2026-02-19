package com.example.luca.algorithm

import com.example.luca.model.Settlement
import com.example.luca.model.SettlementResult
import java.util.UUID

/**
 * SmartSplitBill Algorithm - Kotlin Implementation
 *
 * Algoritma ini mengoptimalkan jumlah transaksi yang diperlukan untuk menyelesaikan
 * hutang-piutang dalam sebuah grup dengan 3 teknik:
 * 1. Bilateral Simplification - Menghapus hutang timbal balik (A->B dan B->A)
 * 2. Cycle Elimination - Menghapus hutang yang membentuk siklus tertutup
 * 3. Transitive Triangle Optimization - Mengoptimalkan hutang segitiga
 */
class SmartSplitBill {

    // Graph: debtor -> creditor -> amount
    // graph[A][B] = 100 artinya A hutang ke B sebesar 100
    private val graph: MutableMap<String, MutableMap<String, Long>> = mutableMapOf()

    /**
     * Menambahkan expense ke graph.
     *
     * @param payer Nama orang yang membayar
     * @param details Map dari consumer ke jumlah yang harus dibayar
     *
     * Contoh: addExpense("Alice", mapOf("Bob" to 50000L, "Charlie" to 30000L))
     * Artinya Alice membayar, dan Bob hutang 50rb, Charlie hutang 30rb ke Alice.
     */
    fun addExpense(payer: String, details: Map<String, Long>) {
        for ((consumer, amount) in details) {
            if (consumer == payer) continue
            if (amount <= 0) continue

            // Inisialisasi nested map jika belum ada
            if (!graph.containsKey(consumer)) {
                graph[consumer] = mutableMapOf()
            }

            val currentDebt = graph[consumer]?.get(payer) ?: 0L
            graph[consumer]!![payer] = currentDebt + amount
        }
    }

    /**
     * Step 1: Simplify bilateral debts.
     * Jika A hutang ke B dan B juga hutang ke A, hapus yang lebih kecil.
     */
    private fun simplifyBilateral() {
        val users = graph.keys.toList()

        for (i in users) {
            val creditorsOfI = graph[i]?.keys?.toList() ?: continue

            for (j in creditorsOfI) {
                val iOwesJ = graph[i]?.get(j) ?: 0L
                val jOwesI = graph[j]?.get(i) ?: 0L

                if (iOwesJ > 0 && jOwesI > 0) {
                    val minAmount = minOf(iOwesJ, jOwesI)

                    // Kurangi kedua arah
                    graph[i]!![j] = iOwesJ - minAmount

                    if (!graph.containsKey(j)) {
                        graph[j] = mutableMapOf()
                    }
                    graph[j]!![i] = jOwesI - minAmount

                    // Hapus jika 0
                    if ((graph[i]?.get(j) ?: 0L) <= 0L) {
                        graph[i]?.remove(j)
                    }
                    if ((graph[j]?.get(i) ?: 0L) <= 0L) {
                        graph[j]?.remove(i)
                    }
                }
            }
        }

        // Clean up empty maps
        cleanupEmptyMaps()
    }

    /**
     * Step 2: Find and eliminate cycles.
     * Siklus hutang (A->B->C->A) bisa direduksi.
     */
    private fun simplifyCycles() {
        var maxIterations = 100
        var iterations = 0

        while (iterations < maxIterations) {
            val cycle = findCycle()
            if (cycle == null || cycle.size < 2) break

            // Find minimum amount in cycle
            var minAmount = Long.MAX_VALUE
            for (k in 0 until cycle.size - 1) {
                val u = cycle[k]
                val v = cycle[k + 1]
                val amount = graph[u]?.get(v) ?: 0L
                if (amount > 0 && amount < minAmount) {
                    minAmount = amount
                }
            }

            if (minAmount == Long.MAX_VALUE || minAmount <= 0) break

            // Reduce cycle
            for (k in 0 until cycle.size - 1) {
                val u = cycle[k]
                val v = cycle[k + 1]
                val currentAmount = graph[u]?.get(v) ?: 0L
                val newAmount = currentAmount - minAmount

                if (newAmount <= 0) {
                    graph[u]?.remove(v)
                } else {
                    graph[u]!![v] = newAmount
                }
            }

            cleanupEmptyMaps()
            iterations++
        }
    }

    /**
     * Find a cycle in the debt graph using DFS.
     */
    private fun findCycle(): List<String>? {
        val visited = mutableSetOf<String>()

        fun dfs(u: String, path: MutableList<String>): List<String>? {
            visited.add(u)
            path.add(u)

            val neighbors = graph[u] ?: emptyMap()
            for ((v, amount) in neighbors) {
                if (amount > 0) {
                    // Check if v is already in current path (cycle found)
                    val cycleStart = path.indexOf(v)
                    if (cycleStart >= 0) {
                        // Return the cycle: from v back to v
                        return path.subList(cycleStart, path.size) + v
                    }

                    if (v !in visited) {
                        val result = dfs(v, path)
                        if (result != null) return result
                    }
                }
            }

            path.removeAt(path.size - 1)
            return null
        }

        for (user in graph.keys.toList()) {
            if (user !in visited) {
                val result = dfs(user, mutableListOf())
                if (result != null) return result
            }
        }

        return null
    }

    /**
     * Step 3: Optimize transitive triangles.
     * Jika A->B, B->C, dan A->C, kita bisa "titipkan" hutang A->C melalui B.
     * Ini mengurangi jumlah total transaksi.
     */
    private fun optimizeTransitiveTriangles() {
        val maxIterations = 50
        var count = 0

        while (count < maxIterations) {
            var hasChanged = false
            val debtors = graph.keys.toList()

            outerLoop@ for (a in debtors) {
                val creditorsOfA = graph[a]?.keys?.toList() ?: continue

                for (b in creditorsOfA) {
                    val aOwesB = graph[a]?.get(b) ?: 0L
                    if (aOwesB <= 0) continue

                    val creditorsOfB = graph[b]?.keys?.toList() ?: continue

                    for (c in creditorsOfB) {
                        if (a == c || b == c || a == b) continue

                        val bOwesC = graph[b]?.get(c) ?: 0L
                        if (bOwesC <= 0) continue

                        val aOwesC = graph[a]?.get(c) ?: 0L
                        if (aOwesC > 0) {
                            // A owes C directly, "titipkan" through B
                            val amountTitip = aOwesC

                            // Remove A->C
                            graph[a]?.remove(c)

                            // Increase A->B
                            graph[a]!![b] = (graph[a]?.get(b) ?: 0L) + amountTitip

                            // Increase B->C
                            if (!graph.containsKey(b)) {
                                graph[b] = mutableMapOf()
                            }
                            graph[b]!![c] = (graph[b]?.get(c) ?: 0L) + amountTitip

                            hasChanged = true
                            break@outerLoop
                        }
                    }
                }
            }

            if (!hasChanged) break
            count++
        }

        cleanupEmptyMaps()
    }

    /**
     * Remove empty nested maps from the graph.
     */
    private fun cleanupEmptyMaps() {
        val emptyDebtors = graph.filter { it.value.isEmpty() }.keys.toList()
        for (debtor in emptyDebtors) {
            graph.remove(debtor)
        }
    }

    /**
     * Run the full calculation pipeline and return the optimized debt graph.
     */
    fun calculate(): Map<String, Map<String, Long>> {
        simplifyBilateral()
        simplifyCycles()
        optimizeTransitiveTriangles()
        return graph.mapValues { it.value.toMap() }.toMap()
    }

    /**
     * Convert the calculated graph to a list of Settlement objects.
     *
     * @param participantAvatars Map dari nama participant ke avatar name
     * @return List of Settlement objects
     */
    fun toSettlements(participantAvatars: Map<String, String> = emptyMap()): List<Settlement> {
        val settlements = mutableListOf<Settlement>()

        for ((debtor, creditors) in graph) {
            for ((creditor, amount) in creditors) {
                if (amount > 0) {
                    settlements.add(
                        Settlement(
                            id = UUID.randomUUID().toString(),
                            fromName = debtor,
                            fromAvatarName = participantAvatars[debtor] ?: "avatar_1",
                            toName = creditor,
                            toAvatarName = participantAvatars[creditor] ?: "avatar_1",
                            amount = amount,
                            isPaid = false
                        )
                    )
                }
            }
        }

        // Sort by amount descending for better UX
        return settlements.sortedByDescending { it.amount }
    }

    /**
     * Reset the graph for a new calculation.
     */
    fun reset() {
        graph.clear()
    }

    companion object {
        /**
         * Helper function untuk menghitung split bill dari data activity items.
         *
         * @param items List of items dari Firebase (Map format)
         * @param payer Nama orang yang membayar untuk activity ini
         * @param participantAvatars Map dari nama ke avatar name
         * @return SettlementResult
         */
        fun calculateFromItems(
            items: List<Map<String, Any>>,
            payer: String,
            participantAvatars: Map<String, String> = emptyMap()
        ): SettlementResult {
            val splitBill = SmartSplitBill()
            var totalExpense = 0L

            for (item in items) {
                val price = when (val p = item["price"]) {
                    is Long -> p
                    is Int -> p.toLong()
                    is Double -> p.toLong()
                    is String -> p.toLongOrNull() ?: 0L
                    else -> 0L
                }

                val quantity = when (val q = item["quantity"]) {
                    is Int -> q
                    is Long -> q.toInt()
                    is String -> q.toIntOrNull() ?: 1
                    else -> 1
                }

                @Suppress("UNCHECKED_CAST")
                val memberNames = (item["memberNames"] as? List<String>) ?: emptyList()

                val taxPercentage = when (val t = item["taxPercentage"]) {
                    is Double -> t
                    is Long -> t.toDouble()
                    is Int -> t.toDouble()
                    else -> 0.0
                }

                val discountAmount = when (val d = item["discountAmount"]) {
                    is Double -> d
                    is Long -> d.toDouble()
                    is Int -> d.toDouble()
                    else -> 0.0
                }

                if (memberNames.isEmpty()) continue

                // Calculate item total with tax and discount
                val itemTotal = (price * quantity).toDouble()
                val afterTax = itemTotal * (1 + taxPercentage / 100)
                val afterDiscount = afterTax - discountAmount
                val finalAmount = maxOf(0.0, afterDiscount)

                // Split equally among members
                val amountPerPerson = (finalAmount / memberNames.size).toLong()

                totalExpense += finalAmount.toLong()

                // Add to split bill graph
                val details = memberNames.associateWith { amountPerPerson }
                splitBill.addExpense(payer, details)
            }

            // Run optimization
            splitBill.calculate()

            return SettlementResult(
                settlements = splitBill.toSettlements(participantAvatars),
                totalExpense = totalExpense,
                calculatedAt = System.currentTimeMillis()
            )
        }

        /**
         * Calculate settlements from multiple activities.
         *
         * @param activitiesWithItems List of Pair(activity payer name, items list)
         * @param participantAvatars Map dari nama ke avatar name
         * @return SettlementResult
         */
        fun calculateFromMultipleActivities(
            activitiesWithItems: List<Pair<String, List<Map<String, Any>>>>,
            participantAvatars: Map<String, String> = emptyMap()
        ): SettlementResult {
            val splitBill = SmartSplitBill()
            var totalExpense = 0L

            for ((payer, items) in activitiesWithItems) {
                for (item in items) {
                    val price = when (val p = item["price"]) {
                        is Long -> p
                        is Int -> p.toLong()
                        is Double -> p.toLong()
                        is String -> p.toLongOrNull() ?: 0L
                        else -> 0L
                    }

                    val quantity = when (val q = item["quantity"]) {
                        is Int -> q
                        is Long -> q.toInt()
                        is String -> q.toIntOrNull() ?: 1
                        else -> 1
                    }

                    @Suppress("UNCHECKED_CAST")
                    val memberNames = (item["memberNames"] as? List<String>) ?: emptyList()

                    val taxPercentage = when (val t = item["taxPercentage"]) {
                        is Double -> t
                        is Long -> t.toDouble()
                        is Int -> t.toDouble()
                        else -> 0.0
                    }

                    val discountAmount = when (val d = item["discountAmount"]) {
                        is Double -> d
                        is Long -> d.toDouble()
                        is Int -> d.toDouble()
                        else -> 0.0
                    }

                    if (memberNames.isEmpty()) continue

                    // Calculate item total with tax and discount
                    val itemTotal = (price * quantity).toDouble()
                    val afterTax = itemTotal * (1 + taxPercentage / 100)
                    val afterDiscount = afterTax - discountAmount
                    val finalAmount = maxOf(0.0, afterDiscount)

                    // Split equally among members
                    val amountPerPerson = (finalAmount / memberNames.size).toLong()

                    totalExpense += finalAmount.toLong()

                    // Add to split bill graph
                    val details = memberNames.associateWith { amountPerPerson }
                    splitBill.addExpense(payer, details)
                }
            }

            // Run optimization
            splitBill.calculate()

            return SettlementResult(
                settlements = splitBill.toSettlements(participantAvatars),
                totalExpense = totalExpense,
                calculatedAt = System.currentTimeMillis()
            )
        }
    }
}

