package hazem.nurmontage.videoquran.Utils

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object PriceFormatter {

    fun formatPrice(amount: String, currencyCode: String = "USD"): String {
        return try {
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            format.currency = Currency.getInstance(currencyCode)
            format.format(amount.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0)
        } catch (_: Exception) {
            amount
        }
    }
}
