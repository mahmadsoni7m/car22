package tj.soni.card.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import tj.soni.card.domain.Validators
import tj.soni.card.localization.LocaleManager
import tj.soni.card.navigation.Routes
import tj.soni.card.security.BiometricHelper
import tj.soni.card.security.findFragmentActivity
import tj.soni.card.ui.components.*
import tj.soni.card.ui.theme.SoniColors
import tj.soni.card.viewmodel.AppViewModel

const val DEMO_SMS_CODE = "123456"

// ---------------------------------------------------------------- 1. Splash

@Composable
fun SplashScreen(vm: AppViewModel, onRoute: (String) -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val ready = settings != null
    var started by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (started) 1f else 0.6f, tween(700), label = "logoScale")
    val alpha by animateFloatAsState(if (started) 1f else 0f, tween(700), label = "logoAlpha")

    LaunchedEffect(Unit) { started = true }
    LaunchedEffect(ready) {
        val s = settings
        if (s != null) {
            delay(1500)
            onRoute(
                when {
                    !s.onboarded -> Routes.ONBOARDING
                    s.hasPin -> Routes.pin(Routes.PIN_LOGIN)
                    else -> Routes.WELCOME
                }
            )
        }
    }

    Box(Modifier.fillMaxSize().background(SoniColors.Background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SoniLogoMark(logoSize = 96.dp, modifier = Modifier.scale(scale))
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.app_name),
                color = SoniColors.Navy,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.scale(scale)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.splash_tagline),
                color = SoniColors.Muted,
                modifier = Modifier.background(Color.Transparent),
                fontSize = 14.sp * alpha
            )
            Spacer(Modifier.height(32.dp))
            CircularProgressIndicator(
                color = SoniColors.Teal,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// ---------------------------------------------------------------- 2. Onboarding

private data class OnboardPage(val icon: ImageVector, val title: Int, val text: Int)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardPage(Icons.Filled.CreditCard, R.string.ob1_title, R.string.ob1_text),
        OnboardPage(Icons.Filled.SwapHoriz, R.string.ob2_title, R.string.ob2_text),
        OnboardPage(Icons.Filled.Security, R.string.ob3_title, R.string.ob3_text)
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val last = pagerState.currentPage == pages.size - 1

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SoniWordmark(Modifier.weight(1f))
            if (!last) {
                TextButton(onClick = onFinish) {
                    Text(stringResource(R.string.skip), color = SoniColors.Muted)
                }
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
            val p = pages[page]
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    Modifier.size(160.dp).background(SoniColors.MintSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(p.icon, contentDescription = null, tint = SoniColors.Teal, modifier = Modifier.size(80.dp))
                }
                Spacer(Modifier.height(32.dp))
                Text(
                    stringResource(p.title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = SoniColors.Navy,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(p.text),
                    color = SoniColors.Muted,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { i ->
                Box(
                    Modifier.padding(4.dp)
                        .size(if (i == pagerState.currentPage) 24.dp else 8.dp, 8.dp)
                        .background(
                            if (i == pagerState.currentPage) SoniColors.Teal else SoniColors.Border,
                            CircleShape
                        )
                )
            }
        }
        PrimaryButton(
            text = stringResource(if (last) R.string.get_started else R.string.next),
            onClick = {
                if (last) onFinish()
                else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            }
        )
    }
}

// ---------------------------------------------------------------- 3. Welcome

@Composable
fun WelcomeScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    val current = LocaleManager.current()
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        SoniLogoMark(logoSize = 88.dp)
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            color = SoniColors.Navy,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.welcome_text),
            color = SoniColors.Muted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        DemoBanner()
        Spacer(Modifier.height(32.dp))
        PrimaryButton(stringResource(R.string.login), onLogin)
        Spacer(Modifier.height(12.dp))
        SecondaryButton(stringResource(R.string.register), onRegister)
        Spacer(Modifier.height(32.dp))
        Text(stringResource(R.string.language), color = SoniColors.Muted, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SoniChip("TJ", current == LocaleManager.TAJIK, { LocaleManager.apply(LocaleManager.TAJIK) })
            SoniChip("RU", current == LocaleManager.RUSSIAN, { LocaleManager.apply(LocaleManager.RUSSIAN) })
            SoniChip("EN", current == LocaleManager.ENGLISH, { LocaleManager.apply(LocaleManager.ENGLISH) })
        }
    }
}

// ---------------------------------------------------------------- 4. Phone login

@Composable
fun PhoneLoginScreen(vm: AppViewModel, onBack: () -> Unit, onContinue: () -> Unit) {
    var phone by remember { mutableStateOf(vm.pendingPhone) }
    var error by remember { mutableStateOf<Int?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.phone_login_title), onBack)
        Column(Modifier.padding(24.dp)) {
            Text(stringResource(R.string.phone_login_text), color = SoniColors.Muted)
            Spacer(Modifier.height(24.dp))
            SoniTextField(
                value = phone,
                onValueChange = { phone = Validators.digitsOnly(it).take(9); error = null },
                label = stringResource(R.string.phone_number),
                keyboardType = KeyboardType.Phone,
                prefix = "+992 ",
                error = error?.let { stringResource(it) }
            )
            Spacer(Modifier.height(16.dp))
            DemoBanner(stringResource(R.string.sms_demo_notice))
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = stringResource(R.string.continue_text),
                loading = loading,
                onClick = {
                    if (!Validators.isValidPhone(phone)) {
                        error = R.string.err_phone
                    } else {
                        scope.launch {
                            loading = true
                            delay(800)
                            vm.pendingPhone = phone
                            vm.pendingIsRegistration = false
                            loading = false
                            onContinue()
                        }
                    }
                }
            )
        }
    }
}

// ---------------------------------------------------------------- 5. SMS verification

@Composable
fun SmsVerificationScreen(vm: AppViewModel, onBack: () -> Unit, onVerified: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<Int?>(null) }
    var loading by remember { mutableStateOf(false) }
    var resendTick by remember { mutableIntStateOf(0) }
    var seconds by remember { mutableIntStateOf(30) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(resendTick) {
        seconds = 30
        while (seconds > 0) {
            delay(1000)
            seconds--
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.sms_title), onBack)
        Column(Modifier.padding(24.dp)) {
            Text(
                stringResource(R.string.sms_text) + " +992 " + vm.pendingPhone,
                color = SoniColors.Muted
            )
            Spacer(Modifier.height(16.dp))
            DemoBanner(stringResource(R.string.sms_demo_code, DEMO_SMS_CODE))
            Spacer(Modifier.height(24.dp))
            SoniTextField(
                value = code,
                onValueChange = { code = Validators.digitsOnly(it).take(6); error = null },
                label = stringResource(R.string.sms_code),
                keyboardType = KeyboardType.NumberPassword,
                error = error?.let { stringResource(it) }
            )
            Spacer(Modifier.height(16.dp))
            if (seconds > 0) {
                Text(stringResource(R.string.sms_resend_in, seconds), color = SoniColors.Muted, fontSize = 13.sp)
            } else {
                TextButton(onClick = { resendTick++; code = "" }) {
                    Text(stringResource(R.string.sms_resend), color = SoniColors.Teal, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = stringResource(R.string.verify),
                loading = loading,
                onClick = {
                    if (!Validators.isValidSmsCode(code)) {
                        error = R.string.err_sms_format
                    } else if (code != DEMO_SMS_CODE) {
                        error = R.string.err_sms_wrong
                    } else {
                        scope.launch {
                            loading = true
                            delay(600)
                            vm.completePhoneVerification()
                            loading = false
                            onVerified()
                        }
                    }
                }
            )
        }
    }
}

// ---------------------------------------------------------------- 8. Registration

@Composable
fun RegistrationScreen(vm: AppViewModel, onBack: () -> Unit, onContinue: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var accepted by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<Int?>(null) }
    var phoneError by remember { mutableStateOf<Int?>(null) }
    var termsError by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader(stringResource(R.string.register_title), onBack)
        Column(Modifier.padding(24.dp)) {
            Text(stringResource(R.string.register_text), color = SoniColors.Muted)
            Spacer(Modifier.height(24.dp))
            SoniTextField(
                value = name,
                onValueChange = { name = it.take(50); nameError = null },
                label = stringResource(R.string.full_name),
                error = nameError?.let { stringResource(it) }
            )
            Spacer(Modifier.height(16.dp))
            SoniTextField(
                value = phone,
                onValueChange = { phone = Validators.digitsOnly(it).take(9); phoneError = null },
                label = stringResource(R.string.phone_number),
                keyboardType = KeyboardType.Phone,
                prefix = "+992 ",
                error = phoneError?.let { stringResource(it) }
            )
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().clickable { accepted = !accepted; termsError = false },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = accepted,
                    onCheckedChange = { accepted = it; termsError = false },
                    colors = CheckboxDefaults.colors(checkedColor = SoniColors.Teal)
                )
                Text(stringResource(R.string.terms_demo), color = SoniColors.Navy, fontSize = 14.sp)
            }
            ErrorText(if (termsError) stringResource(R.string.err_terms) else null)
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = stringResource(R.string.continue_text),
                onClick = {
                    var ok = true
                    if (!Validators.isValidName(name)) { nameError = R.string.err_name; ok = false }
                    if (!Validators.isValidPhone(phone)) { phoneError = R.string.err_phone; ok = false }
                    if (!accepted) { termsError = true; ok = false }
                    if (ok) {
                        vm.pendingName = name.trim()
                        vm.pendingPhone = phone
                        vm.pendingIsRegistration = true
                        onContinue()
                    }
                }
            )
        }
    }
}

// ---------------------------------------------------------------- 6. PIN

@Composable
fun PinScreen(
    vm: AppViewModel,
    mode: String,
    onBack: (() -> Unit)?,
    onSuccess: () -> Unit,
    onForgot: () -> Unit
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(if (mode == Routes.PIN_CREATE) 1 else 0) }
    var entry by remember { mutableStateOf("") }
    var firstNew by remember { mutableStateOf("") }
    var errorRes by remember { mutableStateOf<Int?>(null) }
    var busy by remember { mutableStateOf(false) }
    var failures by remember { mutableIntStateOf(0) }
    var lockedUntil by remember { mutableLongStateOf(0L) }
    var showForgot by remember { mutableStateOf(false) }

    val bioTitle = stringResource(R.string.bio_prompt_title)
    val bioSub = stringResource(R.string.bio_prompt_subtitle)
    val cancelText = stringResource(R.string.cancel)
    val bioAvailable = remember { BiometricHelper.isAvailable(context) }
    val bioEnabled = mode == Routes.PIN_LOGIN && bioAvailable && settings?.biometricEnabled == true

    fun promptBiometric() {
        val activity = context.findFragmentActivity() ?: return
        BiometricHelper.authenticate(
            activity, bioTitle, bioSub, cancelText,
            onSuccess = { vm.unlock(); onSuccess() },
            onFailure = { }
        )
    }

    LaunchedEffect(bioEnabled) {
        if (bioEnabled) promptBiometric()
    }

    fun submit(pin: String) {
        scope.launch {
            busy = true
            when (step) {
                0 -> {
                    if (System.currentTimeMillis() < lockedUntil) {
                        errorRes = R.string.err_pin_locked
                    } else if (vm.verifyPin(pin)) {
                        failures = 0
                        if (mode == Routes.PIN_LOGIN) {
                            vm.unlock()
                            onSuccess()
                        } else {
                            step = 1
                        }
                    } else {
                        failures++
                        if (failures >= 5) {
                            lockedUntil = System.currentTimeMillis() + 30_000L
                            failures = 0
                            errorRes = R.string.err_pin_locked
                        } else {
                            errorRes = R.string.err_pin_wrong
                        }
                    }
                }
                1 -> {
                    firstNew = pin
                    step = 2
                }
                else -> {
                    if (pin == firstNew) {
                        if (vm.savePin(pin)) {
                            vm.unlock()
                            onSuccess()
                        } else {
                            errorRes = R.string.err_storage
                            step = 1
                            firstNew = ""
                        }
                    } else {
                        errorRes = R.string.err_pin_mismatch
                        step = 1
                        firstNew = ""
                    }
                }
            }
            entry = ""
            busy = false
        }
    }

    fun press(d: String) {
        if (busy || entry.length >= 4) return
        errorRes = null
        entry += d
        if (entry.length == 4) submit(entry)
    }

    val titleRes = when {
        step == 0 && mode == Routes.PIN_CHANGE -> R.string.pin_enter_current
        step == 0 -> R.string.pin_enter
        step == 1 -> R.string.pin_create
        else -> R.string.pin_confirm
    }

    Column(Modifier.fillMaxSize()) {
        if (onBack != null) ScreenHeader(stringResource(R.string.pin_title), onBack)
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SoniLogoMark(logoSize = 56.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge,
                color = SoniColors.Navy,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { i ->
                    Box(
                        Modifier.size(16.dp).background(
                            if (i < entry.length) SoniColors.Teal else SoniColors.Border,
                            CircleShape
                        )
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.height(24.dp)) {
                ErrorText(errorRes?.let { stringResource(it) })
            }
            Spacer(Modifier.height(16.dp))
            val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"))
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    row.forEach { d -> PinKey(onClick = { press(d) }) { Text(d, fontSize = 26.sp, color = SoniColors.Navy) } }
                }
                Spacer(Modifier.height(16.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                if (bioEnabled) {
                    PinKey(onClick = { promptBiometric() }) {
                        Icon(Icons.Filled.Fingerprint, contentDescription = stringResource(R.string.bio_title), tint = SoniColors.Teal)
                    }
                } else {
                    Spacer(Modifier.size(72.dp))
                }
                PinKey(onClick = { press("0") }) { Text("0", fontSize = 26.sp, color = SoniColors.Navy) }
                PinKey(onClick = { if (!busy && entry.isNotEmpty()) entry = entry.dropLast(1) }) {
                    Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = stringResource(R.string.delete), tint = SoniColors.Navy)
                }
            }
            if (mode == Routes.PIN_LOGIN) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { showForgot = true }) {
                    Text(stringResource(R.string.pin_forgot), color = SoniColors.Teal)
                }
            }
        }
    }

    if (showForgot) {
        ConfirmDialog(
            title = stringResource(R.string.pin_forgot),
            message = stringResource(R.string.pin_forgot_message),
            confirmText = stringResource(R.string.reset),
            dismissText = stringResource(R.string.cancel),
            onConfirm = { showForgot = false; onForgot() },
            onDismiss = { showForgot = false }
        )
    }
}

@Composable
private fun PinKey(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.size(72.dp).background(SoniColors.Surface, CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

// ---------------------------------------------------------------- 7. Biometric

@Composable
fun BiometricScreen(vm: AppViewModel, onDone: () -> Unit) {
    val context = LocalContext.current
    val available = remember { BiometricHelper.isAvailable(context) }
    var failed by remember { mutableStateOf(false) }
    val title = stringResource(R.string.bio_prompt_title)
    val subtitle = stringResource(R.string.bio_prompt_subtitle)
    val cancel = stringResource(R.string.cancel)

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(140.dp).background(SoniColors.MintSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Fingerprint, contentDescription = null, tint = SoniColors.Teal, modifier = Modifier.size(72.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.bio_title),
            style = MaterialTheme.typography.headlineMedium,
            color = SoniColors.Navy,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(if (available) R.string.bio_text else R.string.bio_unavailable),
            color = SoniColors.Muted,
            textAlign = TextAlign.Center
        )
        ErrorText(if (failed) stringResource(R.string.bio_failed) else null)
        Spacer(Modifier.height(32.dp))
        if (available) {
            PrimaryButton(stringResource(R.string.bio_enable), onClick = {
                val activity = context.findFragmentActivity()
                if (activity == null) {
                    failed = true
                } else {
                    BiometricHelper.authenticate(
                        activity, title, subtitle, cancel,
                        onSuccess = { vm.setBiometric(true); onDone() },
                        onFailure = { failed = true }
                    )
                }
            })
            Spacer(Modifier.height(12.dp))
        }
        SecondaryButton(stringResource(if (available) R.string.bio_skip else R.string.continue_text), onDone)
    }
}
