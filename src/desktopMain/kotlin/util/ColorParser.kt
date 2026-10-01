package util

import androidx.compose.ui.graphics.Color

object ColorParser {
    fun parse(hex: String, defaultColor: Color = Color(0xFF6366F1)): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            when (cleanHex.length) {
                6 -> {
                    val r = cleanHex.substring(0, 2).toInt(16)
                    val g = cleanHex.substring(2, 4).toInt(16)
                    val b = cleanHex.substring(4, 6).toInt(16)
                    Color(r, g, b)
                }
                8 -> {
                    val a = cleanHex.substring(0, 2).toInt(16)
                    val r = cleanHex.substring(2, 4).toInt(16)
                    val g = cleanHex.substring(4, 6).toInt(16)
                    val b = cleanHex.substring(6, 8).toInt(16)
                    Color(r, g, b, a)
                }
                else -> defaultColor
            }
        } catch (e: Exception) {
            defaultColor
        }
    }
}
