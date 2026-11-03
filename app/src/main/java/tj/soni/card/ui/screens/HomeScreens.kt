package tj.soni.card.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tj.soni.card.R
import tj.soni.card.data.models.TxType
import tj.soni.card.domain.Stats
import tj.soni.card.domain.formatMoney
import tj.soni.card.domain.formatShortDate
import tj.soni.card.ui.components.*
import tj.soni.card.ui.theme.SoniColors
import tj.soni.card.viewmodel.AppViewModel

// ---------------------------------------------------------------- 9. Dashboard

@Composable
fun DashboardScreen(
    vm: AppViewModel,
    onTransfer: () -> Unit,
    onTopUp: () -> Unit,
    onPayments: () -> Unit,
    onQr: () -> Unit,
    onNotifications: () -> Unit,
    onTx: (String) -> Unit,
    onHistory: () -> Unit,
    onStats: () -> Unit,
    onCards: () -> Unit
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val balance by vm.balance.collectAsStateWithLifecycle()
    val txs by vm.transactions.collectAsStateWithLifecycle()
    val cards by vm.cards.collectAsStateWithLifecycle()
    val notifs by vm.notifications.collectAsStateWithLifecycle()

    val hidden = settings?.balanceHidden ?: false
    val name = settings?.name.orEmpty().ifBlank { AppViewModel.DEFAULT_NAME }
    val unread = notifs.count { !it.read }
    val now = remember { System.currentTimeMillis() }
    val monthSummary = remember(txs) { Stats.summary(Stats.filterSince(txs, now - 30 * Stats.DAY_MS)) }
    val weekExpense = remember(txs) { Stats.dailyTotals(txs, 7, now, TxType.OUTGOING) }
    val weekLabels = remember(txs) {
        (6 downTo 0).map { formatShortDate(now - it * Stats.DAY_MS) }
    }
    val primary = cards.firstOrNull { it.primary } ?: cards.firstOrNull()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SoniWordmark(Modifier.weight(1f))
                Box {
                    IconButton(onClick = onNotifications) {
                        Icon(Icons.Filled.Notifications, contentDescription = stringResource(R.string.notifications), tint = SoniColors.Navy)
                    }
                    if (unread > 0) {
                        Box(
                            Modifier.align(Alignment.TopEnd).padding(8.dp).size(16.dp)
                                .background(SoniColors.Error, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(unread.toString(), color = androidx.compose.ui.graphics.Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        item {
            Column {
                Text(stringResource(R.string.greeting, name), style = MaterialTheme.typography.headlineMedium, color = SoniColors.Navy)
                Text(stringResource(R.string.greeting_sub), color = SoniColors.Muted)
            }
        }
        item { DemoBanner() }
        item {
            SurfaceCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.total_balance), color = SoniColors.Muted, modifier = Modifier.weight(1f))
                    DemoBadge()
                    IconButton(onClick = { vm.setBalanceHidden(!hidden) }) {
                        Icon(
                            if (hidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = stringResource(if (hidden) R.string.show_balance else R.string.hide_balance),
                            tint = SoniColors.Teal
                        )
                    }
                }
                Text(
                    if (hidden) "******* TJS" else formatMoney(balance),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoniColors.Navy
                )
            }
        }
        if (primary != null) {
            item {
                Box(Modifier.clickable(onClick = onCards)) { SoniBankCard(primary, name) }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                QuickAction(Icons.Filled.SwapHoriz, stringResource(R.string.transfer), onTransfer)
                QuickAction(Icons.Filled.AddCard, stringResource(R.string.top_up), onTopUp)
                QuickAction(Icons.Filled.Payments, stringResource(R.string.payments), onPayments)
                QuickAction(Icons.Filled.QrCodeScanner, stringResource(R.string.qr_scanner), onQr)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowSummaryTile(true, stringResource(R.string.incoming), monthSummary.income, Modifier.weight(1f))
                FlowSummaryTile(false, stringResource(R.string.outgoing), monthSummary.expense, Modifier.weight(1f))
            }
        }
        item {
            SurfaceCard(onClick = onStats) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.expenses_7_days),
                        style = MaterialTheme.typography.titleMedium,
                        color = SoniColors.Navy,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = SoniColors.Muted)
                }
                Spacer(Modifier.height(12.dp))
                BarChart(weekExpense, weekLabels)
            }
        }
        item { SectionTitle(stringResource(R.string.recent_transactions), stringResource(R.string.see_all), onHistory) }
        if (txs.isEmpty()) {
            item { EmptyState(Icons.Filled.History, stringResource(R.string.empty_tx_title), stringResource(R.string.empty_tx_text)) }
        } else {
            item {
                SurfaceCard {
                    txs.take(5).forEachIndexed { i, tx ->
                        if (i > 0) RowDivider()
                        TxRow(tx) { onTx(tx.id) }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        Modifier.width(76.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(56.dp).background(SoniColors.Surface, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = SoniColors.Teal, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = SoniColors.Navy, fontSize = 12.sp, textAlign = TextAlign.Center, maxLines = 2)
    }
}

// ---------------------------------------------------------------- 10. My cards

@Composable
fun CardsScreen(vm: AppViewModel, onCard: (String) -> Unit, onTopUp: () -> Unit, onWithdraw: () -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val cards by vm.cards.collectAsStateWithLifecycle()
    val name = settings?.name.orEmpty().ifBlank { AppViewModel.DEFAULT_NAME }
    var showAdd by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.my_cards), null)
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { DemoBanner(stringResource(R.string.cards_demo_notice)) }
            if (cards.isEmpty()) {
                item { EmptyState(Icons.Filled.CreditCard, stringResource(R.string.empty_cards_title), stringResource(R.string.empty_cards_text)) }
            }
            items(cards, key = { it.id }) { card ->
                Column(Modifier.clickable { onCard(card.id) }) {
                    SoniBankCard(card, name)
                    Spacer(Modifier.height(8.dp))
                    Text(card.nickname, color = SoniColors.Navy, fontWeight = FontWeight.SemiBold)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { PrimaryButton(stringResource(R.string.top_up), onTopUp) }
                    Box(Modifier.weight(1f)) { SecondaryButton(stringResource(R.string.withdraw), onWithdraw) }
                }
            }
            item {
                SecondaryButton(stringResource(R.string.add_card), onClick = { showAdd = true })
            }
        }
    }

    if (showAdd) AddCardDialog(
        onDismiss = { showAdd = false },
        onCreate = { nickname -> vm.addCard(nickname); showAdd = false }
    )
}

@Composable
private fun AddCardDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var nickname by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<Int?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SoniColors.Surface,
        shape = RoundedCornerShape(24.dp),
        title = { Text(stringResource(R.string.add_card), color = SoniColors.Navy, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(stringResource(R.string.add_card_text), color = SoniColors.Muted, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                SoniTextField(
                    value = nickname,
                    onValueChange = { nickname = it.take(24); error = null },
                    label = stringResource(R.string.card_nickname),
                    error = error?.let { stringResource(it) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nickname.trim().length < 2) error = R.string.err_nickname else onCreate(nickname)
            }) { Text(stringResource(R.string.create), color = SoniColors.Teal, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = SoniColors.Muted) }
        }
    )
}

// ---------------------------------------------------------------- 11. Card details

@Composable
fun CardDetailsScreen(vm: AppViewModel, cardId: String, onBack: () -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val cards by vm.cards.collectAsStateWithLifecycle()
    val card = cards.firstOrNull { it.id == cardId }
    val name = settings?.name.orEmpty().ifBlank { AppViewModel.DEFAULT_NAME }
    var confirm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.card_details), onBack)
        if (card == null) {
            EmptyState(Icons.Filled.CreditCard, stringResource(R.string.not_found_title), stringResource(R.string.not_found_text))
        } else {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SoniBankCard(card, name)
                SurfaceCard {
                    InfoLine(stringResource(R.string.card_name), card.nickname)
                    RowDivider()
                    InfoLine(stringResource(R.string.card_number), "**** **** **** " + card.lastFour)
                    RowDivider()
                    InfoLine(stringResource(R.string.card_expires), card.expiry)
                    RowDivider()
                    InfoLine(
                        stringResource(R.string.card_status),
                        stringResource(if (card.frozen) R.string.card_frozen else R.string.card_active)
                    )
                    RowDivider()
                    InfoLine(stringResource(R.string.card_limit), "5 000.00 TJS")
                }
                DemoBanner(stringResource(R.string.card_secret_notice))
                PrimaryButton(
                    text = stringResource(if (card.frozen) R.string.unfreeze_card else R.string.freeze_card),
                    onClick = { confirm = true }
                )
            }
        }
    }

    if (confirm && card != null) {
        ConfirmDialog(
            title = stringResource(if (card.frozen) R.string.unfreeze_card else R.string.freeze_card),
            message = stringResource(if (card.frozen) R.string.unfreeze_message else R.string.freeze_message),
            confirmText = stringResource(R.string.confirm),
            dismissText = stringResource(R.string.cancel),
            onConfirm = { vm.toggleFreeze(card.id); confirm = false },
            onDismiss = { confirm = false }
        )
    }
}
