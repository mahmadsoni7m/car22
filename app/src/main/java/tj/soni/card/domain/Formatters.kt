package tj.soni.card.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val CURRENCY = "TJS"

fun formatMoney(value: Double): String {
    val s = String.format(Locale.US, "%,.2f", value).replace(',', ' ')
    return "$s $CURRENCY"
}

fun formatSigned(value: Double, incoming: Boolean): String =
    (if (incoming) "+" else "-") + formatMoney(value)

fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))

fun formatShortDate(timestamp: Long): String =
    SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(timestamp))

fun initialsOf(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotEmpty() }
    if (parts.isEmpty()) return "S"
    return parts.take(2).joinToString("") { it.first().uppercaseChar().toString() }
}
