package com.example.luca.util

object AvatarUtils {
    /**
     * Generate DiceBear Avatar URL based on username/seed
     * Format: https://api.dicebear.com/9.x/avataaars/png?seed={seed}
     *
     * @param seed The name/seed to generate unique avatar (e.g., "Bablu", "Bablu1", "JohnDoe")
     * @return DiceBear API URL
     */
    fun getDiceBearUrl(seed: String): String {
        return "https://api.dicebear.com/9.x/avataaars/png?seed=$seed"
    }
}