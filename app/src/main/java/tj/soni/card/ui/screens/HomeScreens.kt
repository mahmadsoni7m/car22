package tj.soni.card.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import android.content.Intent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import tj.soni.card.BuildConfig
import tj.soni.card.domain.formatPlain
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import tj.soni.card.domain.formatStamp
import tj.soni.card.domain.initialsOf
import tj.soni.card.navigation.Routes
import kotlinx.coroutines.launch
import tj.soni.card.ui.components.*
import tj.soni.card.ui.theme.SoniColors
import tj.soni.card.viewmodel.AppViewModel

// ---------------------------------------------------------------- 9. Dashboard

private data class FavDef(val key: String, val label: Int, val icon: ImageVector, val logo: String, val route: String)

private val favDefs = listOf(
    FavDef("transfer:phone", R.string.tr_phone_title, Icons.Filled.PhoneAndroid, "transfers/transfer_phone", Routes.transferForm("phone")),
    FavDef("transfer:card", R.string.tr_card_title, Icons.Filled.CreditCard, "transfers/transfer_card", Routes.transferForm("card")),
    FavDef("transfer:interbank", R.string.tr_inter_title, Icons.Filled.AccountBalance, "transfers/transfer_interbank", Routes.transferForm("interbank")),
    FavDef("svc:mobile", R.string.svc_mobile, Icons.Filled.PhoneAndroid, "services/mobile", Routes.PAYMENTS),
    FavDef("svc:internet", R.string.svc_internet, Icons.Filled.Wifi, "services/internet", Routes.PAYMENTS),
    FavDef("topup", R.string.top_up, Icons.Filled.AddCard, "tiles/topup", Routes.TOPUP)
)

@Composable
fun DashboardScreen(
    vm: AppViewModel,
    onNavigate: (String) -> Unit,
    onTx: (String) -> Unit,
    onLogout: () -> Unit
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val balance by vm.balance.collectAsStateWithLifecycle()
    val txs by vm.transactions.collectAsStateWithLifecycle()
    val notifs by vm.notifications.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var updatedAt by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var confirmLogout by remember { mutableStateOf(false) }

    val hidden = settings?.balanceHidden ?: false
    val name = settings?.name.orEmpty().ifBlank { AppViewModel.DEFAULT_NAME }
    val phone = settings?.phone.orEmpty()
    val unread = notifs.count { !it.read }
    val shareText = stringResource(R.string.share_text)
    val versionLine = stringResource(R.string.version_build, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    val favorites = vm.favorites.toList()

    fun go(route: String) {
        scope.launch { drawerState.close() }
        onNavigate(route)
    }

    val share: () -> Unit = {
        scope.launch { drawerState.close() }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(send, null))
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = SoniColors.Surface) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    DrawerHeader(name, phone) { go(Routes.PROFILE) }
                    Spacer(Modifier.height(8.dp))
                    val main = listOf<Triple<ImageVector, Int, () -> Unit>>(
                        Triple(Icons.Filled.Security, R.string.security_center, { go(Routes.SECURITY) }),
                        Triple(Icons.Filled.SupportAgent, R.string.support, { go(Routes.SUPPORT) }),
                        Triple(Icons.Filled.Settings, R.string.settings, { go(Routes.SETTINGS) }),
                        Triple(Icons.Filled.Quiz, R.string.faq, { go(Routes.FAQ) }),
                        Triple(Icons.Filled.Share, R.string.drawer_share, share)
                    )
                    main.forEach { (icon, label, action) ->
                        Box(Modifier.padding(horizontal = 16.dp)) {
                            SettingsRow(icon, stringResource(label), onClick = action)
                        }
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = SoniColors.Border)
                    val more = listOf<Triple<ImageVector, Int, () -> Unit>>(
                        Triple(Icons.Filled.CreditCard, R.string.my_cards, { go(Routes.CARDS) }),
                        Triple(Icons.Filled.PieChart, R.string.statistics, { go(Routes.STATS) }),
                        Triple(Icons.Filled.Language, R.string.language, { go(Routes.LANGUAGE) }),
                        Triple(Icons.Filled.Info, R.string.about, { go(Routes.ABOUT) })
                    )
                    more.forEach { (icon, label, action) ->
                        Box(Modifier.padding(horizontal = 16.dp)) {
                            SettingsRow(icon, stringResource(label), onClick = action)
                        }
                    }
                    Box(
                        Modifier.padding(16.dp).fillMaxWidth()
                            .background(Color(0xFFFDECEC), RoundedCornerShape(22.dp))
                            .clickable { confirmLogout = true }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PowerSettingsNew, contentDescription = null, tint = SoniColors.Error)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.logout), color = SoniColors.Error, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }
                    }
                    Text(
                        versionLine,
                        color = SoniColors.Muted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    )
                }
            }
        }
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.menu), tint = SoniColors.Teal, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.weight(1f))
                Box {
                    IconButton(onClick = { onNavigate(Routes.NOTIFICATIONS) }) {
                        Icon(Icons.Filled.NotificationsNone, contentDescription = stringResource(R.string.notifications), tint = SoniColors.Teal, modifier = Modifier.size(28.dp))
                    }
                    if (unread > 0) {
                        Box(
                            Modifier.align(Alignment.TopEnd).padding(8.dp).size(16.dp)
                                .background(SoniColors.Error, CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text(unread.toString(), color = Color.White, fontSize = 10.sp) }
                    }
                }
                IconButton(onClick = { onNavigate(Routes.SUPPORT) }) {
                    Icon(Icons.Filled.SupportAgent, contentDescription = stringResource(R.string.support), tint = SoniColors.Teal, modifier = Modifier.size(28.dp))
                }
                IconButton(onClick = { onNavigate(Routes.HISTORY) }) {
                    Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search), tint = SoniColors.Teal, modifier = Modifier.size(28.dp))
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier.weight(1f).height(100.dp)
                        .shadow(6.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x660A74F5), spotColor = Color(0x660A74F5))
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF0A78FF), Color(0xFF0A68EE))),
                            RoundedCornerShape(28.dp)
                        )
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    if (hidden) "*****" else formatPlain(balance),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 30.sp,
                                    maxLines = 1
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("TJS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 5.dp))
                            }
                            Text(formatStamp(updatedAt), color = Color(0xB3FFFFFF), fontSize = 13.sp)
                        }
                        IconButton(onClick = { vm.setBalanceHidden(!hidden) }) {
                            Icon(
                                if (hidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = stringResource(if (hidden) R.string.show_balance else R.string.hide_balance),
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { updatedAt = System.currentTimeMillis() }) {
                            Icon(Icons.Filled.Sync, contentDescription = stringResource(R.string.refresh), tint = Color.White)
                        }
                    }
                }
                Box(
                    Modifier.width(52.dp).height(100.dp)
                        .shadow(3.dp, RoundedCornerShape(24.dp), ambientColor = Color(0x330A74F5), spotColor = Color(0x330A74F5))
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .clickable { onNavigate(Routes.QR) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.QrCode2, contentDescription = stringResource(R.string.qr_scanner), tint = SoniColors.Teal, modifier = Modifier.size(30.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Column(
                Modifier.fillMaxWidth().defaultMinSize(minHeight = 640.dp)
                    .background(Color(0xFFF8FAFF), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DemoBanner()
                if (favorites.isNotEmpty()) {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        favorites.mapNotNull { key -> favDefs.firstOrNull { it.key == key } }.forEach { def ->
                            FavTile(def, onClick = { onNavigate(def.route) }, onRemove = { vm.removeFavorite(def.key) })
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Apps, contentDescription = null, tint = SoniColors.Teal, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.miniapps_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = SoniColors.Navy,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onNavigate(Routes.PAYMENTS) }) {
                        Text(stringResource(R.string.see_all), color = SoniColors.Teal, fontWeight = FontWeight.SemiBold)
                    }
                }
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniChip(Icons.Filled.Payments, "tiles/payments", stringResource(R.string.payments)) { onNavigate(Routes.PAYMENTS) }
                    MiniChip(Icons.Filled.AddCard, "tiles/topup", stringResource(R.string.top_up)) { onNavigate(Routes.TOPUP) }
                    MiniChip(Icons.Filled.AccountBalanceWallet, "tiles/withdraw", stringResource(R.string.withdraw)) { onNavigate(Routes.WITHDRAW) }
                    MiniChip(Icons.Filled.PieChart, "tiles/statistics", stringResource(R.string.statistics)) { onNavigate(Routes.STATS) }
                }
                SurfaceCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        ServiceMini(Icons.Filled.PhoneAndroid, "services/mobile", stringResource(R.string.svc_mobile), Color(0xFF2E9E5B), Modifier.weight(1f)) { onNavigate(Routes.PAYMENTS) }
                        Box(Modifier.width(1.dp).height(32.dp).background(SoniColors.Border))
                        ServiceMini(Icons.Filled.Wifi, "services/internet", stringResource(R.string.svc_internet), Color(0xFF8E44AD), Modifier.weight(1f)) { onNavigate(Routes.PAYMENTS) }
                        Box(Modifier.width(1.dp).height(32.dp).background(SoniColors.Border))
                        ServiceMini(Icons.Filled.ElectricBolt, "services/electricity", stringResource(R.string.svc_electricity), SoniColors.Orange, Modifier.weight(1f)) { onNavigate(Routes.PAYMENTS) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Tile(stringResource(R.string.payments), stringResource(R.string.svc_mobile), Icons.Filled.Payments, "tiles/payments", Color(0xFF0A74F5), Modifier.weight(2f)) { onNavigate(Routes.PAYMENTS) }
                    Tile(stringResource(R.string.my_cards), null, Icons.Filled.CreditCard, "tiles/cards", Color(0xFFE0A800), Modifier.weight(1f)) { onNavigate(Routes.CARDS) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Tile(stringResource(R.string.top_up), null, Icons.Filled.AddCard, "tiles/topup", Color(0xFF2E9E5B), Modifier.weight(1f)) { onNavigate(Routes.TOPUP) }
                    Tile(stringResource(R.string.withdraw), null, Icons.Filled.AccountBalanceWallet, "tiles/withdraw", Color(0xFFD6457B), Modifier.weight(1f)) { onNavigate(Routes.WITHDRAW) }
                    Tile(stringResource(R.string.statistics), null, Icons.Filled.PieChart, "tiles/statistics", Color(0xFF8E44AD), Modifier.weight(1f)) { onNavigate(Routes.STATS) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Tile(stringResource(R.string.history), null, Icons.Filled.History, "tiles/history", Color(0xFF607D8B), Modifier.weight(1f)) { onNavigate(Routes.HISTORY) }
                    Tile(stringResource(R.string.notifications), null, Icons.Filled.Notifications, "tiles/notifications", Color(0xFFFF7525), Modifier.weight(1f)) { onNavigate(Routes.NOTIFICATIONS) }
                    Tile(stringResource(R.string.security_center), null, Icons.Filled.Security, "tiles/security", Color(0xFF0A74F5), Modifier.weight(1f)) { onNavigate(Routes.SECURITY) }
                }
                SectionTitle(stringResource(R.string.recent_transactions), stringResource(R.string.see_all)) { onNavigate(Routes.HISTORY) }
                if (txs.isEmpty()) {
                    EmptyState(Icons.Filled.History, stringResource(R.string.empty_tx_title), stringResource(R.string.empty_tx_text))
                } else {
                    SurfaceCard {
                        txs.take(5).forEachIndexed { i, tx ->
                            if (i > 0) RowDivider()
                            TxRow(tx) { onTx(tx.id) }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (confirmLogout) {
        ConfirmDialog(
            title = stringResource(R.string.logout),
            message = stringResource(R.string.logout_message),
            confirmText = stringResource(R.string.logout),
            dismissText = stringResource(R.string.cancel),
            onConfirm = { confirmLogout = false; onLogout() },
            onDismiss = { confirmLogout = false }
        )
    }
}

@Composable
private fun DrawerHeader(name: String, phone: String, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF2B8CFF), Color(0xFF0A5FD6))))
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(64.dp)
                    .background(Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) { Text(initialsOf(name), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp) }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.size(56.dp).background(Color.White, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) { AppLogo(40.dp) }
        }
        Spacer(Modifier.height(16.dp))
        Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        if (phone.isNotBlank()) Text("+992 $phone", color = Color(0xCCFFFFFF), fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).background(Color(0xFF4CD964), CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.profile_badge), color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
private fun FavTile(def: FavDef, onClick: () -> Unit, onRemove: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Box(Modifier.width(92.dp)) {
        Column(
            Modifier.fillMaxWidth()
                .shadow(2.dp, shape, ambientColor = Color(0x330A74F5), spotColor = Color(0x330A74F5))
                .background(Color.White, shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoBox(def.logo, def.icon, 40.dp, SoniColors.Teal, SoniColors.MintSoft, 20.dp)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(def.label),
                color = SoniColors.Navy,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 3,
                lineHeight = 14.sp
            )
        }
        Box(
            Modifier.align(Alignment.TopEnd).padding(4.dp).size(22.dp).clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.remove_favorite), tint = SoniColors.Muted, modifier = Modifier.size(15.dp))
        }
    }
}

@Composable
private fun MiniChip(icon: ImageVector, logo: String, label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Row(
        Modifier.background(Color.White, shape)
            .border(1.5.dp, Color(0xFFCFE0FF), shape)
            .clickable(onClick = onClick)
            .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoBox(logo, icon, 30.dp, SoniColors.Teal, SoniColors.MintSoft, 15.dp)
        Spacer(Modifier.width(8.dp))
        Text(label, color = SoniColors.Navy, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ServiceMini(icon: ImageVector, logo: String, label: String, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier.clickable(onClick = onClick).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        LogoBox(logo, icon, 34.dp, tint, tint.copy(alpha = 0.15f), 17.dp)
        Spacer(Modifier.width(6.dp))
        Text(label, color = SoniColors.Navy, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
    }
}

@Composable
private fun Tile(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    logo: String,
    tint: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier.height(100.dp)
            .shadow(3.dp, shape, ambientColor = Color(0x330A74F5), spotColor = Color(0x330A74F5))
            .background(Color.White, shape)
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = SoniColors.Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2, lineHeight = 18.sp)
            if (subtitle != null) Text(subtitle, color = SoniColors.Muted, fontSize = 12.sp)
        }
        LogoBox(
            logo, icon, 42.dp, tint, tint.copy(alpha = 0.16f), 14.dp,
            Modifier.align(Alignment.BottomEnd).padding(10.dp)
        )
    }
}

// ---------------------------------------------------------------- 10. My cards

@Composable
fun CardsScreen(vm: AppViewModel, onCard: (String) -> Unit, onTopUp: () -> Unit, onWithdraw: () -> Unit, onBack: () -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val cards by vm.cards.collectAsStateWithLifecycle()
    val name = settings?.name.orEmpty().ifBlank { AppViewModel.DEFAULT_NAME }
    var showAdd by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.my_cards), onBack)
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
