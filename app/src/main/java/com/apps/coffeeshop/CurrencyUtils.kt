package com.apps.coffeeshop

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun toRupiah(amount: Double): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(amount)
    }

    fun toSimpleString(amount: Double): String {
        return when {
            amount >= 1_000_000_000 -> String.format("Rp%.1fM", amount / 1_000_000_000).replace(".0", "")
            amount >= 1_000_000 -> String.format("Rp%.1fjt", amount / 1_000_000).replace(".0", "")
            amount >= 1_000 -> String.format("Rp%.0frb", amount / 1_000)
            else -> String.format("Rp%.0f", amount)
        }
    }
}
