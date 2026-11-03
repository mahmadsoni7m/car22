package tj.soni.card.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import tj.soni.card.domain.Validators
import tj.soni.card.domain.formatDay
import tj.soni.card.domain.formatMoney
import tj.soni.card.domain.formatTime
import tj.soni.card.ui.components.*
import tj.soni.card.ui.theme.SoniColors
import tj.soni.card.viewmodel.AppViewModel

// ---------------------------------------------------------------- 12. Transfers (tab: list of types)

@Composable
fun TransfersScreen(onPhone: () -> Unit, onCard: () -> Unit, onInterbank: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Text(
            stringResource(R.string.transfers_title),
            fontSize = 24.sp,
            color = SoniColors.Navy,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
        )
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DemoBanner(stringResource(R.string.transfer_demo_notice))
            TransferTypeCard("transfers/transfer_phone", Icons.Filled.PhoneAndroid, stringResource(R.string.tr_phone_title), stringResource(R.string.tr_phone_desc), onPhone)
            TransferTypeCard("transfers/transfer_card", Icons.Filled.CreditCard, stringResource(R.string.tr_card_title), stringResource(R.string.tr_card_desc), onCard)
            TransferTypeCard("transfers/transfer_interbank", Icons.Filled.AccountBalance, stringResource(R.string.tr_inter_title), stringResource(R.string.tr_inter_desc), onInterbank)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TransferTypeCard(logo: String, icon: ImageVector, title: String, desc: String, onClick: () -> Unit) {
    SurfaceCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LogoBox(logo, icon, 56.dp, SoniColors.Teal, SoniColors.Field, 18.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = SoniColors.Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.height(4.dp))
                Text(desc, color = SoniColors.Muted, fontSize = 14.sp)
            }
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = SoniColors.Muted)
        }
    }
}

// ---------------------------------------------------------------- 13. Transfer form + confirmation sheet + result sheet

@Composable
fun TransferFormScreen(
    vm: AppViewModel,
    type: String,
    onBack: () -> Unit,
    onDetails: (String) -> Unit,
    onDone: () -> Unit
) {
    val isPhone = type == "phone"
    val isInterbank = type == "interbank"
    val context = LocalContext.current
    val balance by vm.balance.collectAsStateWithLifecycle()
    val cards by vm.cards.collectAsStateWithLifecycle()
    val title = stringResource(
        when (type) {
            "card" -> R.string.tr_card_title
            "interbank" -> R.string.tr_inter_title
            else -> R.string.tr_phone_title
        }
    )
    val maxLen = if (isPhone) 9 else 16
    val favKey = "transfer:$type"
    val isFav = favKey in vm.favorites
    val recentPrefix = if (isPhone) "p:" else "c:"
    val recents = vm.recentRecipients.filter { it.startsWith(recentPrefix) }.map { it.drop(2) }
    val primary = cards.firstOrNull { it.primary } ?: cards.firstOrNull()
    val methodMask = "**** **** **** " + (primary?.lastFour ?: "0000")
    val noContactsApp = stringResource(R.string.no_contacts_app)

    var recipient by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var recipientError by remember { mutableStateOf<Int?>(null) }
    var amountError by remember { mutableStateOf<Int?>(null) }
    var verified by remember { mutableStateOf(false) }
    var showRecents by remember { mutableStateOf(false) }
    var sheet by remember { mutableIntStateOf(0) } // 0 none, 1 confirm, 2 result
    var loading by remember { mutableStateOf(false) }
    var sheetError by remember { mutableStateOf<Int?>(null) }
    var result by remember { mutableStateOf<Transaction?>(null) }
    val scope = rememberCoroutineScope()

    // Contact picker: ACTION_PICK on phone data needs no READ_CONTACTS permission.
    val contactLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        val uri = res.data?.data
        if (res.resultCode == Activity.RESULT_OK && uri != null) {
            val number = try {
                context.contentResolver.query(
                    uri, arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER), null, null, null
                )?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
            } catch (e: Exception) {
                null
            }
            val digits = Validators.digitsOnly(number.orEmpty())
            if (digits.length >= 9) {
                recipient = digits.takeLast(9)
                verified = false
                recipientError = null
            }
        }
    }

    fun recipientOk(): Boolean =
        if (isPhone) Validators.isValidPhone(recipient) else Validators.isValidCardNumber(recipient)

    fun feeFor(a: Double): Double =
        if (isInterbank) Math.round(a * 0.01 * 100.0) / 100.0 else 0.0

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(title, onBack, trailing = {
                IconButton(onClick = { vm.toggleFavorite(favKey) }) {
                    Icon(
                        if (isFav) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = stringResource(if (isFav) R.string.remove_favorite else R.string.add_favorite),
                        tint = Color.White
                    )
                }
            })
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DemoBanner(stringResource(R.string.transfer_demo_notice))
                SoniTextField(
                    value = recipient,
                    onValueChange = {
                        recipient = Validators.digitsOnly(it).take(maxLen)
                        recipientError = null
                        verified = false
                    },
                    label = stringResource(if (isPhone) R.string.phone_hint else R.string.card_hint),
                    keyboardType = KeyboardType.Number,
                    prefix = if (isPhone) "+992 " else null,
                    error = recipientError?.let { stringResource(it) },
                    trailing = {
                        Row {
                            IconButton(onClick = { showRecents = true }) {
                                Icon(Icons.Filled.History, contentDescription = stringResource(R.string.recent_title), tint = SoniColors.Teal)
                            }
                            if (isPhone) {
                                IconButton(onClick = {
                                    try {
                                        contactLauncher.launch(
                                            Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                                        )
                                    } catch (e: ActivityNotFoundException) {
                                        Toast.makeText(context, noContactsApp, Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(Icons.Filled.Contacts, contentDescription = stringResource(R.string.pick_contact), tint = SoniColors.Teal)
                                }
                            }
                        }
                    }
                )
                Box(
                    Modifier.fillMaxWidth().height(52.dp)
                        .background(SoniColors.MintSoft, RoundedCornerShape(26.dp))
                        .border(1.dp, Color(0xFFB9D3FF), RoundedCornerShape(26.dp))
                        .clickable {
                            if (recipientOk()) {
                                verified = true
                                recipientError = null
                            } else {
                                verified = false
                                recipientError = R.string.err_recipient
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.check_number), color = SoniColors.Teal, fontSize = 16.sp)
                }
                if (verified) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SoniColors.Success, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.number_ok), color = SoniColors.Success, fontSize = 13.sp)
                    }
                }
                SoniTextField(
                    value = amountText,
                    onValueChange = { amountText = it.take(12); amountError = null },
                    label = stringResource(R.string.amount),
                    keyboardType = KeyboardType.Decimal,
                    prefix = "TJS ",
                    error = amountError?.let { stringResource(it) }
                )
                SoniTextField(
                    value = comment,
                    onValueChange = { comment = it.take(60) },
                    label = stringResource(R.string.comment),
                    singleLine = false,
                    minLines = 2
                )
                Text(
                    stringResource(R.string.min_amount, formatMoney(1.0)),
                    color = SoniColors.Muted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Column(
                        Modifier.background(
                            Brush.linearGradient(listOf(Color(0xFF2B8CFF), Color(0xFF0A68EE))),
                            RoundedCornerShape(20.dp)
                        ).padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Text(methodMask, color = Color(0xCCFFFFFF), fontSize = 13.sp)
                        Spacer(Modifier.height(10.dp))
                        Text(formatMoney(balance), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                Text(stringResource(R.string.tr_note), color = SoniColors.Muted, fontSize = 14.sp)
            }
            Box(Modifier.padding(16.dp)) {
                PrimaryButton(
                    text = stringResource(R.string.next),
                    onClick = {
                        var ok = true
                        if (!recipientOk()) { recipientError = R.string.err_recipient; ok = false }
                        val a = Validators.parseAmount(amountText)
                        if (a == null || a < 1.0) { amountError = R.string.err_amount; ok = false }
                        else if (a + feeFor(a) > balance) { amountError = R.string.err_insufficient; ok = false }
                        if (ok) {
                            sheetError = null
                            sheet = 1
                        }
                    }
                )
            }
        }

        if (sheet != 0) {
            val amount = Validators.parseAmount(amountText) ?: 0.0
            val fee = feeFor(amount)
            val tx = result
            Box(
                Modifier.fillMaxSize().background(Color(0x99141B34))
                    .clickable(enabled = sheet == 1 && !loading) { sheet = 0 }
            )
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .then(if (sheet == 2) Modifier.fillMaxHeight(0.78f) else Modifier)
                    .background(Color.White, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(Modifier.width(48.dp).height(5.dp).background(Color(0xFFD5DEEF), RoundedCornerShape(3.dp)))
                if (sheet == 1) {
                    Spacer(Modifier.height(16.dp))
                    Column(
                        Modifier.fillMaxWidth()
                            .background(SoniColors.MintSoft, RoundedCornerShape(24.dp))
                            .border(1.dp, Color(0xFFB9D3FF), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Text(stringResource(R.string.confirm_payment_title), color = SoniColors.Muted, fontSize = 15.sp)
                        Text(formatMoney(amount), color = SoniColors.Teal, fontWeight = FontWeight.Bold, fontSize = 34.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Column(
                        Modifier.fillMaxWidth()
                            .background(SoniColors.Field, RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        InfoLine(stringResource(R.string.provider), title)
                        RowDivider()
                        InfoLine(stringResource(R.string.account_label), recipient)
                        RowDivider()
                        InfoLine(stringResource(R.string.pay_method), methodMask)
                        RowDivider()
                        InfoLine(stringResource(R.string.credit_amount), formatMoney(amount))
                        RowDivider()
                        InfoLine(stringResource(R.string.fee), formatMoney(fee))
                        RowDivider()
                        InfoLine(stringResource(R.string.debit_amount), formatMoney(amount + fee))
                    }
                    ErrorText(sheetError?.let { stringResource(it) })
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton(
                        text = stringResource(R.string.i_confirm),
                        loading = loading,
                        onClick = {
                            scope.launch {
                                loading = true
                                delay(1000)
                                val done = vm.transfer(TransferDraft(recipient, !isPhone, amount, comment.trim(), fee))
                                loading = false
                                if (done == null) {
                                    sheetError = R.string.err_insufficient
                                } else {
                                    vm.rememberRecipient(recentPrefix + recipient)
                                    result = done
                                    sheet = 2
                                }
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                } else if (tx != null) {
                    Spacer(Modifier.height(20.dp))
                    Box(
                        Modifier.size(76.dp).background(Color(0xFFE5F7EE), CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SoniColors.Success, modifier = Modifier.size(48.dp)) }
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.payment_done), color = SoniColors.Teal, fontWeight = FontWeight.Bold, fontSize = 22.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Column(
                        Modifier.fillMaxWidth()
                            .background(SoniColors.Field, RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        InfoLine(stringResource(R.string.provider), title)
                        RowDivider()
                        InfoLine(stringResource(R.string.recipient), recipient)
                        RowDivider()
                        InfoLine(stringResource(R.string.credit_amount), formatMoney(amount))
                        RowDivider()
                        InfoLine(stringResource(R.string.fee), formatMoney(fee))
                        RowDivider()
                        InfoLine(stringResource(R.string.date), formatDay(tx.timestamp))
                        RowDivider()
                        InfoLine(stringResource(R.string.time_label), formatTime(tx.timestamp))
                        RowDivider()
                        InfoLine(stringResource(R.string.tx_id), tx.id)
                    }
                    Spacer(Modifier.weight(1f))
                    DemoBanner(stringResource(R.string.result_demo_notice))
                    TextButton(onClick = { onDetails(tx.id) }) {
                        Text(stringResource(R.string.view_details), color = SoniColors.Teal, fontWeight = FontWeight.SemiBold)
                    }
                    PrimaryButton(stringResource(R.string.to_home), onClick = onDone, containerColor = SoniColors.Teal)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    if (showRecents) {
        AlertDialog(
            onDismissRequest = { showRecents = false },
            containerColor = SoniColors.Surface,
            shape = RoundedCornerShape(24.dp),
            title = { Text(stringResource(R.string.recent_title), color = SoniColors.Navy, fontWeight = FontWeight.Bold) },
            text = {
                if (recents.isEmpty()) {
                    Text(stringResource(R.string.recent_empty), color = SoniColors.Muted)
                } else {
                    Column {
                        recents.forEach { r ->
                            TextButton(onClick = {
                                recipient = r
                                verified = false
                                recipientError = null
                                showRecents = false
                            }) { Text(r, color = SoniColors.Teal, fontSize = 17.sp) }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRecents = false }) {
                    Text(stringResource(R.string.close), color = SoniColors.Muted)
                }
            }
        )
    }
}
