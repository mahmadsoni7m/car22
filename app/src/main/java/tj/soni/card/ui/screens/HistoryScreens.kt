package tj.soni.card.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tj.soni.card.R
import tj.soni.card.data.models.TxType
import tj.soni.card.domain.Stats
import tj.soni.card.domain.formatDate
import tj.soni.card.domain.formatMoney
import tj.soni.card.domain.formatShortDate
import tj.soni.card.domain.formatSigned
import tj.soni.card.ui.components.*
import tj.soni.card.ui.labelRes
import tj.soni.card.ui.theme.SoniColors
import tj.soni.card.viewmodel.AppViewModel

// ---------------------------------------------------------------- 18. History

@Composable
fun HistoryScreen(vm: AppViewModel, onTx: (String) -> Unit) {
    val txs by vm.transactions.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<TxType?>(null) }
    val shown = remember(txs, query, filter) { Stats.search(txs, query, filter) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.history), null)
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SoniTextField(
                value = query,
                onValueChange = { query = it.take(40) },
                label = stringResource(R.string.search)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SoniChip(stringResource(R.string.filter_all), filter == null, { filter = null })
                SoniChip(stringResource(R.string.incoming), filter == TxType.INCOMING, { filter = TxType.INCOMING })
                SoniChip(stringResource(R.string.outgoing), filter == TxType.OUTGOING, { filter = TxType.OUTGOING })
            }
            DemoBanner(stringResource(R.string.history_demo_notice))
        }
        if (shown.isEmpty()) {
            if (txs.isEmpty()) {
                EmptyState(Icons.Filled.History, stringResource(R.string.empty_tx_title), stringResource(R.string.empty_tx_text))
            } else {
                EmptyState(Icons.Filled.Search, stringResource(R.string.empty_search_title), stringResource(R.string.empty_search_text))
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(shown, key = { it.id }) { tx ->
                    TxRow(tx) { onTx(tx.id) }
                    RowDivider()
                }
            }
        }
    }
}

// ---------------------------------------------------------------- 19. Transaction details

@Composable
fun TransactionDetailsScreen(vm: AppViewModel, txId: String, onBack: () -> Unit) {
    val txs by vm.transactions.collectAsStateWithLifecycle()
    val tx = txs.firstOrNull { it.id == txId }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val copiedText = stringResource(R.string.copied)

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.tx_details), onBack)
        if (tx == null) {
            EmptyState(Icons.Filled.History, stringResource(R.string.not_found_title), stringResource(R.string.not_found_text))
        } else {
            val incoming = tx.type == TxType.INCOMING
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SurfaceCard {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.size(56.dp).background(SoniColors.MintSoft, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (incoming) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                                contentDescription = null,
                                tint = if (incoming) SoniColors.Success else SoniColors.Navy
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            formatSigned(tx.amount, incoming),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (incoming) SoniColors.Success else SoniColors.Navy
                        )
                        Spacer(Modifier.height(4.dp))
                        DemoBadge()
                    }
                }
                SurfaceCard {
                    InfoLine(stringResource(R.string.counterparty), tx.counterparty)
                    RowDivider()
                    InfoLine(stringResource(R.string.category), stringResource(tx.category.labelRes()))
                    RowDivider()
                    InfoLine(
                        stringResource(R.string.type),
                        stringResource(if (incoming) R.string.incoming else R.string.outgoing)
                    )
                    RowDivider()
                    InfoLine(stringResource(R.string.date), formatDate(tx.timestamp))
                    RowDivider()
                    InfoLine(stringResource(R.string.tx_id), tx.id)
                    if (tx.comment.isNotBlank()) {
                        RowDivider()
                        InfoLine(stringResource(R.string.comment), tx.comment)
                    }
                }
                DemoBanner(stringResource(R.string.tx_demo_notice))
                SecondaryButton(stringResource(R.string.copy_id), onClick = {
                    clipboard.setText(AnnotatedString(tx.id))
                    Toast.makeText(context, copiedText, Toast.LENGTH_SHORT).show()
                })
            }
        }
    }
}

// ---------------------------------------------------------------- 20. Statistics

@Composable
fun StatisticsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val txs by vm.transactions.collectAsStateWithLifecycle()
    var days by remember { mutableIntStateOf(30) }
    val now = remember { System.currentTimeMillis() }
    val period = remember(txs, days) { Stats.filterSince(txs, now - days * Stats.DAY_MS) }
    val summary = remember(period) { Stats.summary(period) }
    val categories = remember(period) { Stats.byCategory(period) }
    val weekExpense = remember(txs) { Stats.dailyTotals(txs, 7, now, TxType.OUTGOING) }
    val weekIncome = remember(txs) { Stats.dailyTotals(txs, 7, now, TxType.INCOMING) }
    val labels = remember { (6 downTo 0).map { formatShortDate(now - it * Stats.DAY_MS) } }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.statistics), onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DemoBanner(stringResource(R.string.stats_demo_notice))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SoniChip(stringResource(R.string.period_7), days == 7, { days = 7 })
                SoniChip(stringResource(R.string.period_30), days == 30, { days = 30 })
                SoniChip(stringResource(R.string.period_90), days == 90, { days = 90 })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowSummaryTile(true, stringResource(R.string.incoming), summary.income, Modifier.weight(1f))
                FlowSummaryTile(false, stringResource(R.string.outgoing), summary.expense, Modifier.weight(1f))
            }
            SurfaceCard {
                Text(stringResource(R.string.net_result), color = SoniColors.Muted, fontSize = 13.sp)
                val net = summary.income - summary.expense
                Text(
                    (if (net >= 0) "+" else "-") + formatMoney(kotlin.math.abs(net)),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (net >= 0) SoniColors.Success else SoniColors.Error
                )
            }
            SurfaceCard {
                Text(stringResource(R.string.income_7_days), style = MaterialTheme.typography.titleMedium, color = SoniColors.Navy)
                Spacer(Modifier.height(12.dp))
                BarChart(weekIncome, labels, barColor = SoniColors.Mint)
            }
            SurfaceCard {
                Text(stringResource(R.string.expenses_7_days), style = MaterialTheme.typography.titleMedium, color = SoniColors.Navy)
                Spacer(Modifier.height(12.dp))
                BarChart(weekExpense, labels)
            }
            SurfaceCard {
                Text(stringResource(R.string.by_category), style = MaterialTheme.typography.titleMedium, color = SoniColors.Navy)
                Spacer(Modifier.height(12.dp))
                if (categories.isEmpty()) {
                    Text(stringResource(R.string.empty_stats_text), color = SoniColors.Muted)
                } else {
                    val total = categories.sumOf { it.second }.coerceAtLeast(1.0)
                    categories.forEach { (cat, sum) ->
                        Row(Modifier.fillMaxWidth()) {
                            Text(stringResource(cat.labelRes()), color = SoniColors.Navy, modifier = Modifier.weight(1f))
                            Text(formatMoney(sum), color = SoniColors.Navy, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        FractionBar((sum / total).toFloat())
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- 21. Notifications

@Composable
fun NotificationsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val items by vm.notifications.collectAsStateWithLifecycle()
    val enabled = settings?.notificationsOn ?: true

    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ScreenHeader(stringResource(R.string.notifications), onBack, Modifier.weight(1f))
            if (enabled && items.any { !it.read }) {
                TextButton(onClick = { vm.markAllNotificationsRead() }) {
                    Text(stringResource(R.string.mark_all_read), color = SoniColors.Teal)
                }
            }
        }
        if (!enabled) {
            EmptyState(Icons.Filled.Notifications, stringResource(R.string.notif_off_title), stringResource(R.string.notif_off_text))
        } else if (items.isEmpty()) {
            EmptyState(Icons.Filled.Notifications, stringResource(R.string.empty_notif_title), stringResource(R.string.empty_notif_text))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { n ->
                    SurfaceCard(onClick = { vm.markNotificationRead(n.id) }) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                Modifier.padding(top = 6.dp).size(10.dp).background(
                                    if (n.read) SoniColors.Border else SoniColors.Teal, CircleShape
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(stringResource(n.titleRes), color = SoniColors.Navy, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(4.dp))
                                Text(stringResource(n.bodyRes), color = SoniColors.Muted, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
