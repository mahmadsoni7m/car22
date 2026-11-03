package tj.soni.card.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tj.soni.card.R
import tj.soni.card.data.models.Transaction
import tj.soni.card.data.models.TransferDraft
import tj.soni.card.data.models.TxCategory
import tj.soni.card.data.repository.DemoRepository
import tj.soni.card.domain.Validators
import tj.soni.card.domain.formatMoney
import tj.soni.card.ui.components.*
import tj.soni.card.ui.theme.SoniColors
import tj.soni.card.viewmodel.AppViewModel

// ---------------------------------------------------------------- shared result view

@Composable
private fun ResultView(
    title: String,
    message: String,
    txId: String,
    onDetails: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(104.dp).background(SoniColors.MintSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SoniColors.Success, modifier = Modifier.size(64.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, color = SoniColors.Navy, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(message, color = SoniColors.Muted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(txId, color = SoniColors.Navy, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(16.dp))
        DemoBanner(stringResource(R.string.result_demo_notice))
        Spacer(Modifier.height(32.dp))
        PrimaryButton(stringResource(R.string.view_details), onDetails)
        Spacer(Modifier.height(12.dp))
        SecondaryButton(stringResource(R.string.done), onDone)
    }
}

// ---------------------------------------------------------------- 12. Transfers

@Composable
fun TransfersScreen(vm: AppViewModel, onBack: () -> Unit, onContinue: () -> Unit) {
    val balance by vm.balance.collectAsStateWithLifecycle()
    var recipient by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var recipientError by remember { mutableStateOf<Int?>(null) }
    var amountError by remember { mutableStateOf<Int?>(null) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader(stringResource(R.string.transfer), onBack)
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DemoBanner(stringResource(R.string.transfer_demo_notice))
            SurfaceCard {
                Text(stringResource(R.string.available_balance), color = SoniColors.Muted, fontSize = 13.sp)
                Text(formatMoney(balance), color = SoniColors.Navy, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
            SoniTextField(
                value = recipient,
                onValueChange = { recipient = Validators.digitsOnly(it).take(16); recipientError = null },
                label = stringResource(R.string.recipient_hint),
                keyboardType = KeyboardType.Number,
                error = recipientError?.let { stringResource(it) }
            )
            SoniTextField(
                value = amountText,
                onValueChange = { amountText = it.take(12); amountError = null },
                label = stringResource(R.string.amount),
                keyboardType = KeyboardType.Decimal,
                error = amountError?.let { stringResource(it) }
            )
            SoniTextField(
                value = comment,
                onValueChange = { comment = it.take(60) },
                label = stringResource(R.string.comment_optional)
            )
            PrimaryButton(
                text = stringResource(R.string.continue_text),
                onClick = {
                    var ok = true
                    val isCard = Validators.isValidCardNumber(recipient)
                    val isPhone = Validators.isValidPhone(recipient)
                    if (!isCard && !isPhone) { recipientError = R.string.err_recipient; ok = false }
                    val amount = Validators.parseAmount(amountText)
                    if (amount == null) { amountError = R.string.err_amount; ok = false }
                    else if (amount > balance) { amountError = R.string.err_insufficient; ok = false }
                    if (ok && amount != null) {
                        vm.pendingTransfer = TransferDraft(recipient, isCard, amount, comment.trim())
                        onContinue()
                    }
                }
            )
        }
    }
}

// ---------------------------------------------------------------- 13. Transfer confirmation

@Composable
fun TransferConfirmScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onDetails: (String) -> Unit,
    onDone: () -> Unit
) {
    val draft = remember { vm.pendingTransfer }
    var loading by remember { mutableStateOf(false) }
    var errorRes by remember { mutableStateOf<Int?>(null) }
    var result by remember { mutableStateOf<Transaction?>(null) }
    val scope = rememberCoroutineScope()

    val done = result
    if (done != null) {
        ResultView(
            title = stringResource(R.string.transfer_done),
            message = formatMoney(done.amount),
            txId = done.id,
            onDetails = { vm.pendingTransfer = null; onDetails(done.id) },
            onDone = { vm.pendingTransfer = null; onDone() }
        )
        return
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader(stringResource(R.string.transfer_confirm), onBack)
        if (draft == null) {
            EmptyState(Icons.Filled.SwapHoriz, stringResource(R.string.not_found_title), stringResource(R.string.not_found_text))
        } else {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SurfaceCard {
                    InfoLine(stringResource(R.string.recipient), DemoRepository.maskRecipient(draft))
                    RowDivider()
                    InfoLine(stringResource(R.string.amount), formatMoney(draft.amount))
                    RowDivider()
                    InfoLine(stringResource(R.string.fee), formatMoney(0.0))
                    RowDivider()
                    InfoLine(stringResource(R.string.total), formatMoney(draft.amount))
                    if (draft.comment.isNotBlank()) {
                        RowDivider()
                        InfoLine(stringResource(R.string.comment), draft.comment)
                    }
                }
                DemoBanner(stringResource(R.string.transfer_demo_notice))
                ErrorText(errorRes?.let { stringResource(it) })
                PrimaryButton(
                    text = stringResource(R.string.confirm_transfer),
                    loading = loading,
                    onClick = {
                        scope.launch {
                            loading = true
                            delay(1200)
                            val tx = vm.transfer(draft)
                            if (tx == null) errorRes = R.string.err_insufficient else result = tx
                            loading = false
                        }
                    }
                )
                SecondaryButton(stringResource(R.string.cancel), onBack)
            }
        }
    }
}

// ---------------------------------------------------------------- 14. Payments

private data class PayService(val nameRes: Int, val icon: ImageVector, val category: TxCategory)

private val payServices = listOf(
    PayService(R.string.svc_mobile, Icons.Filled.PhoneAndroid, TxCategory.PAYMENT),
    PayService(R.string.svc_internet, Icons.Filled.Wifi, TxCategory.PAYMENT),
    PayService(R.string.svc_electricity, Icons.Filled.ElectricBolt, TxCategory.UTILITIES),
    PayService(R.string.svc_water, Icons.Filled.WaterDrop, TxCategory.UTILITIES),
    PayService(R.string.svc_gas, Icons.Filled.LocalFireDepartment, TxCategory.UTILITIES),
    PayService(R.string.svc_tv, Icons.Filled.Tv, TxCategory.PAYMENT),
    PayService(R.string.svc_education, Icons.Filled.School, TxCategory.PAYMENT),
    PayService(R.string.svc_transport, Icons.Filled.DirectionsBus, TxCategory.TRANSPORT)
)

@Composable
fun PaymentsScreen(vm: AppViewModel, onQr: () -> Unit, onDetails: (String) -> Unit) {
    var selected by remember { mutableStateOf<PayService?>(null) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.payments), null)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DemoBanner(stringResource(R.string.payments_demo_notice))
            SurfaceCard(onClick = onQr) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = SoniColors.Teal, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.qr_scanner), fontWeight = FontWeight.SemiBold, color = SoniColors.Navy)
                        Text(stringResource(R.string.qr_pay_text), color = SoniColors.Muted, fontSize = 12.sp)
                    }
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = SoniColors.Muted)
                }
            }
            SectionTitle(stringResource(R.string.pay_services))
            payServices.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { svc ->
                        val isSel = selected == svc
                        Column(
                            Modifier.weight(1f)
                                .background(if (isSel) SoniColors.MintSoft else SoniColors.Surface, RoundedCornerShape(18.dp))
                                .border(1.dp, if (isSel) SoniColors.Teal else SoniColors.Border, RoundedCornerShape(18.dp))
                                .clickable { selected = svc }
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(svc.icon, contentDescription = null, tint = SoniColors.Teal, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(svc.nameRes), color = SoniColors.Navy, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            val sel = selected
            if (sel != null) {
                PaymentForm(vm, sel, onDetails) { selected = null }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PaymentForm(vm: AppViewModel, service: PayService, onDetails: (String) -> Unit, onFinished: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val serviceName = stringResource(service.nameRes)
    val successText = stringResource(R.string.payment_done)
    var account by remember(service) { mutableStateOf("") }
    var amountText by remember(service) { mutableStateOf("") }
    var accountError by remember(service) { mutableStateOf<Int?>(null) }
    var amountError by remember(service) { mutableStateOf<Int?>(null) }
    var loading by remember(service) { mutableStateOf(false) }
    var confirm by remember(service) { mutableStateOf(false) }

    SurfaceCard {
        Text(serviceName, style = MaterialTheme.typography.titleMedium, color = SoniColors.Navy)
        Spacer(Modifier.height(12.dp))
        SoniTextField(
            value = account,
            onValueChange = { account = Validators.digitsOnly(it).take(16); accountError = null },
            label = stringResource(R.string.account_number),
            keyboardType = KeyboardType.Number,
            error = accountError?.let { stringResource(it) }
        )
        Spacer(Modifier.height(12.dp))
        SoniTextField(
            value = amountText,
            onValueChange = { amountText = it.take(12); amountError = null },
            label = stringResource(R.string.amount),
            keyboardType = KeyboardType.Decimal,
            error = amountError?.let { stringResource(it) }
        )
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = stringResource(R.string.pay),
            loading = loading,
            onClick = {
                var ok = true
                if (account.length < 4) { accountError = R.string.err_account; ok = false }
                if (Validators.parseAmount(amountText) == null) { amountError = R.string.err_amount; ok = false }
                if (ok) confirm = true
            }
        )
    }

    if (confirm) {
        val amount = Validators.parseAmount(amountText) ?: 0.0
        ConfirmDialog(
            title = stringResource(R.string.pay),
            message = stringResource(R.string.confirm_payment_message, formatMoney(amount), serviceName),
            confirmText = stringResource(R.string.confirm),
            dismissText = stringResource(R.string.cancel),
            onConfirm = {
                confirm = false
                scope.launch {
                    loading = true
                    delay(1000)
                    val tx = vm.pay(serviceName, amount, service.category)
                    loading = false
                    if (tx == null) {
                        amountError = R.string.err_insufficient
                    } else {
                        Toast.makeText(context, successText + " " + tx.id, Toast.LENGTH_LONG).show()
                        onFinished()
                    }
                }
            },
            onDismiss = { confirm = false }
        )
    }
}

// ---------------------------------------------------------------- 15. QR scanner (DEMO viewfinder)

@Composable
fun QrScannerScreen(vm: AppViewModel, onBack: () -> Unit, onDetails: (String) -> Unit) {
    var scanned by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<Int?>(null) }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Transaction?>(null) }
    val scope = rememberCoroutineScope()
    val merchant = stringResource(R.string.qr_demo_merchant)

    val transition = rememberInfiniteTransition(label = "scan")
    val line by transition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "scanLine"
    )

    val done = result
    if (done != null) {
        ResultView(
            title = stringResource(R.string.payment_done),
            message = formatMoney(done.amount) + " • " + merchant,
            txId = done.id,
            onDetails = { onDetails(done.id) },
            onDone = onBack
        )
        return
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader(stringResource(R.string.qr_scanner), onBack)
        Column(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DemoBanner(stringResource(R.string.qr_demo_notice))
            Box(
                Modifier.size(260.dp).background(SoniColors.Navy, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize().padding(24.dp)) {
                    val w = size.width
                    val h = size.height
                    val c = 36f
                    val stroke = 8f
                    val color = SoniColors.Mint
                    fun seg(a: Offset, b: Offset) = drawLine(color, a, b, stroke, StrokeCap.Round)
                    seg(Offset(0f, 0f), Offset(c, 0f)); seg(Offset(0f, 0f), Offset(0f, c))
                    seg(Offset(w, 0f), Offset(w - c, 0f)); seg(Offset(w, 0f), Offset(w, c))
                    seg(Offset(0f, h), Offset(c, h)); seg(Offset(0f, h), Offset(0f, h - c))
                    seg(Offset(w, h), Offset(w - c, h)); seg(Offset(w, h), Offset(w, h - c))
                    if (!scanned) {
                        drawLine(color.copy(alpha = 0.9f), Offset(8f, h * line), Offset(w - 8f, h * line), 4f)
                    }
                }
                if (scanned) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SoniColors.Mint, modifier = Modifier.size(72.dp))
                } else {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = Color.White.copy(alpha = 0.25f), modifier = Modifier.size(96.dp))
                }
            }
            if (!scanned) {
                Text(stringResource(R.string.qr_hint), color = SoniColors.Muted, textAlign = TextAlign.Center)
                PrimaryButton(stringResource(R.string.qr_simulate), onClick = { scanned = true })
            } else {
                SurfaceCard {
                    InfoLine(stringResource(R.string.merchant), merchant)
                    Spacer(Modifier.height(8.dp))
                    SoniTextField(
                        value = amountText,
                        onValueChange = { amountText = it.take(12); amountError = null },
                        label = stringResource(R.string.amount),
                        keyboardType = KeyboardType.Decimal,
                        error = amountError?.let { stringResource(it) }
                    )
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(
                        text = stringResource(R.string.pay),
                        loading = loading,
                        onClick = {
                            val amount = Validators.parseAmount(amountText)
                            if (amount == null) {
                                amountError = R.string.err_amount
                            } else {
                                scope.launch {
                                    loading = true
                                    delay(1000)
                                    val tx = vm.pay(merchant, amount, TxCategory.SHOPPING)
                                    loading = false
                                    if (tx == null) amountError = R.string.err_insufficient else result = tx
                                }
                            }
                        }
                    )
                }
                SecondaryButton(stringResource(R.string.qr_scan_again), onClick = { scanned = false; amountText = "" })
            }
        }
    }
}

// ---------------------------------------------------------------- 16/17. Top up & withdraw (shared flow)

@Composable
private fun AmountFlow(
    title: String,
    intro: String,
    optionLabels: List<String>,
    quickAmounts: List<Int>,
    checkBalance: Boolean,
    balance: Double,
    onBack: () -> Unit,
    onDetails: (String) -> Unit,
    perform: (Double, String) -> Transaction?
) {
    var amountText by remember { mutableStateOf("") }
    var option by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<Int?>(null) }
    var confirm by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Transaction?>(null) }
    val scope = rememberCoroutineScope()

    val done = result
    if (done != null) {
        ResultView(
            title = title + " ✓",
            message = formatMoney(done.amount),
            txId = done.id,
            onDetails = { onDetails(done.id) },
            onDone = onBack
        )
        return
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader(title, onBack)
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DemoBanner(intro)
            SurfaceCard {
                Text(stringResource(R.string.available_balance), color = SoniColors.Muted, fontSize = 13.sp)
                Text(formatMoney(balance), color = SoniColors.Navy, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
            SoniTextField(
                value = amountText,
                onValueChange = { amountText = it.take(12); error = null },
                label = stringResource(R.string.amount),
                keyboardType = KeyboardType.Decimal,
                error = error?.let { stringResource(it) }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                quickAmounts.forEach { q ->
                    SoniChip(q.toString(), selected = false, onClick = { amountText = q.toString(); error = null })
                }
            }
            Text(stringResource(R.string.method), color = SoniColors.Muted, fontSize = 13.sp)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                optionLabels.forEachIndexed { i, label ->
                    SoniChip(label, selected = option == i, onClick = { option = i })
                }
            }
            PrimaryButton(
                text = stringResource(R.string.continue_text),
                loading = loading,
                onClick = {
                    val a = Validators.parseAmount(amountText)
                    if (a == null) error = R.string.err_amount
                    else if (checkBalance && a > balance) error = R.string.err_insufficient
                    else confirm = true
                }
            )
        }
    }

    if (confirm) {
        val a = Validators.parseAmount(amountText) ?: 0.0
        val method = optionLabels.getOrElse(option) { "" }
        ConfirmDialog(
            title = title,
            message = stringResource(R.string.confirm_flow_message, formatMoney(a), method),
            confirmText = stringResource(R.string.confirm),
            dismissText = stringResource(R.string.cancel),
            onConfirm = {
                confirm = false
                scope.launch {
                    loading = true
                    delay(1000)
                    val tx = perform(a, method)
                    loading = false
                    if (tx == null) error = R.string.err_insufficient else result = tx
                }
            },
            onDismiss = { confirm = false }
        )
    }
}

@Composable
fun TopUpScreen(vm: AppViewModel, onBack: () -> Unit, onDetails: (String) -> Unit) {
    val balance by vm.balance.collectAsStateWithLifecycle()
    AmountFlow(
        title = stringResource(R.string.top_up),
        intro = stringResource(R.string.topup_demo_notice),
        optionLabels = listOf(
            stringResource(R.string.src_demo_card),
            stringResource(R.string.src_demo_cash),
            stringResource(R.string.src_demo_bank)
        ),
        quickAmounts = listOf(100, 500, 1000),
        checkBalance = false,
        balance = balance,
        onBack = onBack,
        onDetails = onDetails,
        perform = { amount, method -> vm.topUp(amount, method) }
    )
}

@Composable
fun WithdrawScreen(vm: AppViewModel, onBack: () -> Unit, onDetails: (String) -> Unit) {
    val balance by vm.balance.collectAsStateWithLifecycle()
    AmountFlow(
        title = stringResource(R.string.withdraw),
        intro = stringResource(R.string.withdraw_demo_notice),
        optionLabels = listOf(
            stringResource(R.string.wd_demo_atm),
            stringResource(R.string.wd_demo_desk)
        ),
        quickAmounts = listOf(100, 300, 500),
        checkBalance = true,
        balance = balance,
        onBack = onBack,
        onDetails = onDetails,
        perform = { amount, method -> vm.withdraw(amount, method) }
    )
}
