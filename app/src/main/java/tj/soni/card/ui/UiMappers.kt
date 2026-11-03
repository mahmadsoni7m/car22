package tj.soni.card.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import tj.soni.card.R
import tj.soni.card.data.models.TxCategory

@StringRes
fun TxCategory.labelRes(): Int = when (this) {
    TxCategory.TRANSFER -> R.string.cat_transfer
    TxCategory.PAYMENT -> R.string.cat_payment
    TxCategory.TOPUP -> R.string.cat_topup
    TxCategory.WITHDRAW -> R.string.cat_withdraw
    TxCategory.SALARY -> R.string.cat_salary
    TxCategory.SHOPPING -> R.string.cat_shopping
    TxCategory.FOOD -> R.string.cat_food
    TxCategory.TRANSPORT -> R.string.cat_transport
    TxCategory.UTILITIES -> R.string.cat_utilities
}

fun TxCategory.icon(): ImageVector = when (this) {
    TxCategory.TRANSFER -> Icons.Filled.SwapHoriz
    TxCategory.PAYMENT -> Icons.Filled.Payments
    TxCategory.TOPUP -> Icons.Filled.AddCard
    TxCategory.WITHDRAW -> Icons.Filled.AccountBalanceWallet
    TxCategory.SALARY -> Icons.Filled.Work
    TxCategory.SHOPPING -> Icons.Filled.ShoppingBag
    TxCategory.FOOD -> Icons.Filled.Restaurant
    TxCategory.TRANSPORT -> Icons.Filled.DirectionsBus
    TxCategory.UTILITIES -> Icons.Filled.ElectricBolt
}
