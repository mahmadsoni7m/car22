package tj.soni.card.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tj.soni.card.BuildConfig
import tj.soni.card.R
import tj.soni.card.domain.Validators
import tj.soni.card.domain.initialsOf
import tj.soni.card.localization.LocaleManager
import tj.soni.card.security.BiometricHelper
import tj.soni.card.security.findFragmentActivity
import tj.soni.card.ui.components.*
import tj.soni.card.ui.theme.SoniColors
import tj.soni.card.viewmodel.AppViewModel

// ---------------------------------------------------------------- 22. Profile

@Composable
fun ProfileScreen(
    vm: AppViewModel,
    onSecurity: () -> Unit,
    onSettings: () -> Unit,
    onLanguage: () -> Unit,
    onSupport: () -> Unit,
    onFaq: () -> Unit,
    onAbout: () -> Unit,
    onLogout: () -> Unit
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val name = settings?.name.orEmpty().ifBlank { AppViewModel.DEFAULT_NAME }
    val phone = settings?.phone.orEmpty()
    var confirmLogout by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.profile), null)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SurfaceCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(64.dp).background(SoniColors.Teal, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initialsOf(name), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(name, style = MaterialTheme.typography.titleLarge, color = SoniColors.Navy)
                        if (phone.isNotBlank()) Text("+992 $phone", color = SoniColors.Muted)
                        Spacer(Modifier.height(4.dp))
                        DemoBadge()
                    }
                }
            }
            SurfaceCard {
                SettingsRow(Icons.Filled.Security, stringResource(R.string.security_center), onClick = onSecurity)
                RowDivider()
                SettingsRow(Icons.Filled.Settings, stringResource(R.string.settings), onClick = onSettings)
                RowDivider()
                SettingsRow(Icons.Filled.Language, stringResource(R.string.language), onClick = onLanguage)
                RowDivider()
                SettingsRow(Icons.Filled.SupportAgent, stringResource(R.string.support), onClick = onSupport)
                RowDivider()
                SettingsRow(Icons.Filled.Quiz, stringResource(R.string.faq), onClick = onFaq)
                RowDivider()
                SettingsRow(Icons.Filled.Info, stringResource(R.string.about), onClick = onAbout)
            }
            SecondaryButton(stringResource(R.string.logout), onClick = { confirmLogout = true })
            Spacer(Modifier.height(8.dp))
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

// ---------------------------------------------------------------- 23. Security center

@Composable
fun SecurityScreen(vm: AppViewModel, onBack: () -> Unit, onChangePin: () -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val available = remember { BiometricHelper.isAvailable(context) }
    val enabled = settings?.biometricEnabled == true
    val title = stringResource(R.string.bio_prompt_title)
    val subtitle = stringResource(R.string.bio_prompt_subtitle)
    val cancel = stringResource(R.string.cancel)
    val failedText = stringResource(R.string.bio_failed)

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.security_center), onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SurfaceCard {
                SettingsRow(
                    icon = Icons.Filled.Lock,
                    title = stringResource(R.string.change_pin),
                    subtitle = stringResource(R.string.change_pin_sub),
                    onClick = onChangePin
                )
                RowDivider()
                SettingsRow(
                    icon = Icons.Filled.Fingerprint,
                    title = stringResource(R.string.bio_title),
                    subtitle = stringResource(if (available) R.string.bio_toggle_sub else R.string.bio_unavailable),
                    trailing = {
                        Switch(
                            checked = enabled,
                            enabled = available,
                            onCheckedChange = { want ->
                                if (!want) {
                                    vm.setBiometric(false)
                                } else {
                                    val activity = context.findFragmentActivity()
                                    if (activity == null) {
                                        Toast.makeText(context, failedText, Toast.LENGTH_SHORT).show()
                                    } else {
                                        BiometricHelper.authenticate(
                                            activity, title, subtitle, cancel,
                                            onSuccess = { vm.setBiometric(true) },
                                            onFailure = { Toast.makeText(context, failedText, Toast.LENGTH_SHORT).show() }
                                        )
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = SoniColors.Teal)
                        )
                    }
                )
            }
            SurfaceCard {
                Text(stringResource(R.string.security_tips), style = MaterialTheme.typography.titleMedium, color = SoniColors.Navy)
                Spacer(Modifier.height(8.dp))
                listOf(R.string.tip1, R.string.tip2, R.string.tip3, R.string.tip4).forEach { tip ->
                    Row(Modifier.padding(vertical = 4.dp)) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SoniColors.Success, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(tip), color = SoniColors.Navy, fontSize = 14.sp)
                    }
                }
            }
            DemoBanner(stringResource(R.string.security_demo_notice))
        }
    }
}

// ---------------------------------------------------------------- 24. Settings

@Composable
fun SettingsScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onLanguage: () -> Unit,
    onWiped: () -> Unit
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resetDone = stringResource(R.string.reset_done)
    var confirmReset by remember { mutableStateOf(false) }
    var confirmWipe by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.settings), onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SurfaceCard {
                SettingsRow(
                    icon = Icons.Filled.Language,
                    title = stringResource(R.string.language),
                    subtitle = languageName(LocaleManager.current()),
                    onClick = onLanguage
                )
                RowDivider()
                SettingsRow(
                    icon = Icons.Filled.Notifications,
                    title = stringResource(R.string.notifications),
                    subtitle = stringResource(R.string.notif_setting_sub),
                    trailing = {
                        Switch(
                            checked = settings?.notificationsOn ?: true,
                            onCheckedChange = { vm.setNotifications(it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = SoniColors.Teal)
                        )
                    }
                )
                RowDivider()
                SettingsRow(
                    icon = Icons.Filled.LightMode,
                    title = stringResource(R.string.theme),
                    subtitle = stringResource(R.string.theme_light)
                )
            }
            SurfaceCard {
                SettingsRow(
                    icon = Icons.Filled.Refresh,
                    title = stringResource(R.string.reset_demo),
                    subtitle = stringResource(R.string.reset_demo_sub),
                    onClick = { confirmReset = true }
                )
                RowDivider()
                SettingsRow(
                    icon = Icons.Filled.DeleteForever,
                    title = stringResource(R.string.wipe_data),
                    subtitle = stringResource(R.string.wipe_data_sub),
                    onClick = { confirmWipe = true }
                )
            }
        }
    }

    if (confirmReset) {
        ConfirmDialog(
            title = stringResource(R.string.reset_demo),
            message = stringResource(R.string.reset_demo_message),
            confirmText = stringResource(R.string.reset),
            dismissText = stringResource(R.string.cancel),
            onConfirm = {
                vm.resetDemoData()
                Toast.makeText(context, resetDone, Toast.LENGTH_SHORT).show()
                confirmReset = false
            },
            onDismiss = { confirmReset = false }
        )
    }
    if (confirmWipe) {
        ConfirmDialog(
            title = stringResource(R.string.wipe_data),
            message = stringResource(R.string.wipe_data_message),
            confirmText = stringResource(R.string.reset),
            dismissText = stringResource(R.string.cancel),
            onConfirm = { confirmWipe = false; vm.wipeEverything(); onWiped() },
            onDismiss = { confirmWipe = false }
        )
    }
}

@Composable
private fun languageName(tag: String): String = when (tag) {
    LocaleManager.RUSSIAN -> stringResource(R.string.lang_ru)
    LocaleManager.ENGLISH -> stringResource(R.string.lang_en)
    else -> stringResource(R.string.lang_tg)
}

// ---------------------------------------------------------------- 25. Language

@Composable
fun LanguageScreen(onBack: () -> Unit) {
    val current = LocaleManager.current()
    val options = listOf(
        LocaleManager.TAJIK to R.string.lang_tg,
        LocaleManager.RUSSIAN to R.string.lang_ru,
        LocaleManager.ENGLISH to R.string.lang_en
    )
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.language), onBack)
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.language_text), color = SoniColors.Muted)
            SurfaceCard {
                options.forEachIndexed { i, (tag, nameRes) ->
                    if (i > 0) RowDivider()
                    Row(
                        Modifier.fillMaxWidth().clickable { if (tag != current) LocaleManager.apply(tag) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = tag == current,
                            onClick = { if (tag != current) LocaleManager.apply(tag) },
                            colors = RadioButtonDefaults.colors(selectedColor = SoniColors.Teal)
                        )
                        Text(stringResource(nameRes), color = SoniColors.Navy, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- 26. Support

@Composable
fun SupportScreen(onBack: () -> Unit, onFaq: () -> Unit) {
    val context = LocalContext.current
    val savedText = stringResource(R.string.support_saved)
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var subjectError by remember { mutableStateOf<Int?>(null) }
    var messageError by remember { mutableStateOf<Int?>(null) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.support), onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DemoBanner(stringResource(R.string.support_demo_notice))
            SurfaceCard(onClick = onFaq) {
                SettingsRow(Icons.Filled.Quiz, stringResource(R.string.faq), stringResource(R.string.faq_sub), onClick = onFaq)
            }
            SoniTextField(
                value = subject,
                onValueChange = { subject = it.take(60); subjectError = null },
                label = stringResource(R.string.subject),
                error = subjectError?.let { stringResource(it) }
            )
            SoniTextField(
                value = message,
                onValueChange = { message = it.take(500); messageError = null },
                label = stringResource(R.string.message),
                singleLine = false,
                minLines = 4,
                error = messageError?.let { stringResource(it) }
            )
            PrimaryButton(stringResource(R.string.send), onClick = {
                var ok = true
                if (subject.trim().length < 3) { subjectError = R.string.err_subject; ok = false }
                if (!Validators.isValidMessage(message)) { messageError = R.string.err_message; ok = false }
                if (ok) {
                    Toast.makeText(context, savedText, Toast.LENGTH_LONG).show()
                    subject = ""
                    message = ""
                }
            })
        }
    }
}

// ---------------------------------------------------------------- 27. FAQ

@Composable
fun FaqScreen(onBack: () -> Unit) {
    val items = listOf(
        R.string.faq_q1 to R.string.faq_a1,
        R.string.faq_q2 to R.string.faq_a2,
        R.string.faq_q3 to R.string.faq_a3,
        R.string.faq_q4 to R.string.faq_a4,
        R.string.faq_q5 to R.string.faq_a5,
        R.string.faq_q6 to R.string.faq_a6
    )
    var expanded by remember { mutableIntStateOf(-1) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.faq), onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.forEachIndexed { i, (q, a) ->
                SurfaceCard(onClick = { expanded = if (expanded == i) -1 else i }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(q),
                            color = SoniColors.Navy,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            if (expanded == i) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = SoniColors.Teal
                        )
                    }
                    if (expanded == i) {
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(a), color = SoniColors.Muted, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- 28. About

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(stringResource(R.string.about), onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SoniLogoMark(logoSize = 88.dp)
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium, color = SoniColors.Navy, letterSpacing = 2.sp)
            Text(stringResource(R.string.version_format, BuildConfig.VERSION_NAME), color = SoniColors.Muted)
            Text(stringResource(R.string.about_text), color = SoniColors.Navy, textAlign = TextAlign.Center)
            DemoBanner(stringResource(R.string.about_demo_notice))
            SurfaceCard {
                InfoLine(stringResource(R.string.about_package), "tj.soni.card")
                RowDivider()
                InfoLine(stringResource(R.string.about_mode), stringResource(R.string.about_mode_value))
                RowDivider()
                InfoLine(stringResource(R.string.about_license), "MIT")
            }
        }
    }
}
