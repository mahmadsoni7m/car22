package tj.soni.card.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import tj.soni.card.R
import tj.soni.card.security.findFragmentActivity
import tj.soni.card.ui.screens.*
import tj.soni.card.ui.theme.SoniColors
import tj.soni.card.viewmodel.AppViewModel
import androidx.compose.ui.text.font.FontWeight

private fun NavHostController.goTop(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavHostController.goBottom(route: String) {
    navigate(route) {
        popUpTo(Routes.DASHBOARD) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.go(route: String) {
    if (route in Routes.bottomBarRoutes) goBottom(route) else navigate(route)
}

private fun NavHostController.showTx(id: String) {
    navigate(Routes.tx(id)) { popUpTo(Routes.DASHBOARD) }
}

@Composable
fun SoniApp(vm: AppViewModel = viewModel()) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val pinMode = backStack?.arguments?.getString("mode")
    val settings by vm.settings.collectAsStateWithLifecycle()
    val unlocked = vm.unlocked
    val view = LocalView.current

    // Status bar: light on tab screens, blue on screens that have the blue header.
    val lightBar = route == null || route in Routes.lightRoutes ||
        (route == Routes.PIN && pinMode == Routes.PIN_LOGIN)
    SideEffect {
        val window = view.context.findFragmentActivity()?.window
        if (window != null) {
            val dark = route == Routes.QR
            window.statusBarColor = (
                if (dark) Color.Black else if (lightBar) SoniColors.Background else SoniColors.Teal
            ).toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = lightBar && !dark
        }
    }

    // Guard: protected screens are never shown while the app is locked.
    LaunchedEffect(route, unlocked, settings) {
        val s = settings
        if (route != null && s != null) {
            if (route !in Routes.publicRoutes && !unlocked) {
                nav.goTop(if (s.hasPin) Routes.pin(Routes.PIN_LOGIN) else Routes.WELCOME)
            } else if (route == Routes.PIN && pinMode == Routes.PIN_LOGIN && !s.hasPin) {
                nav.goTop(Routes.WELCOME)
            }
        }
    }

    Scaffold(
        containerColor = SoniColors.Background,
        bottomBar = {
            if (route in Routes.bottomBarRoutes) {
                SoniBottomBar(route, onSelect = { nav.goBottom(it) }, onQr = { nav.navigate(Routes.QR) })
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(padding),
            enterTransition = { slideInHorizontally(tween(250)) { it / 6 } + fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(250)) },
            popExitTransition = { slideOutHorizontally(tween(250)) { it / 6 } + fadeOut(tween(200)) }
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(vm) { target ->
                    nav.navigate(target) { popUpTo(Routes.SPLASH) { inclusive = true } }
                }
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen {
                    vm.completeOnboarding()
                    nav.navigate(Routes.WELCOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                }
            }
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onLogin = { nav.navigate(Routes.PHONE_LOGIN) },
                    onRegister = { nav.navigate(Routes.REGISTRATION) }
                )
            }
            composable(Routes.PHONE_LOGIN) {
                PhoneLoginScreen(vm, onBack = { nav.popBackStack() }, onContinue = { nav.navigate(Routes.SMS) })
            }
            composable(Routes.REGISTRATION) {
                RegistrationScreen(vm, onBack = { nav.popBackStack() }, onContinue = { nav.navigate(Routes.SMS) })
            }
            composable(Routes.SMS) {
                SmsVerificationScreen(vm, onBack = { nav.popBackStack() }, onVerified = {
                    if (vm.settings.value?.hasPin == true) {
                        vm.unlock()
                        nav.goTop(Routes.DASHBOARD)
                    } else {
                        nav.navigate(Routes.pin(Routes.PIN_CREATE))
                    }
                })
            }
            composable(
                Routes.PIN,
                arguments = listOf(navArgument("mode") { type = NavType.StringType })
            ) { entry ->
                val mode = entry.arguments?.getString("mode") ?: Routes.PIN_LOGIN
                PinScreen(
                    vm = vm,
                    mode = mode,
                    onBack = if (mode == Routes.PIN_LOGIN) null else ({ nav.popBackStack() }),
                    onSuccess = {
                        when (mode) {
                            Routes.PIN_LOGIN -> nav.goTop(Routes.DASHBOARD)
                            Routes.PIN_CREATE -> nav.navigate(Routes.BIOMETRIC) {
                                popUpTo(Routes.PIN) { inclusive = true }
                            }
                            else -> nav.popBackStack()
                        }
                    },
                    onForgot = {
                        vm.wipeEverything()
                        nav.goTop(Routes.WELCOME)
                    }
                )
            }
            composable(Routes.BIOMETRIC) {
                BiometricScreen(vm) { nav.goTop(Routes.DASHBOARD) }
            }
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    vm = vm,
                    onNavigate = { nav.go(it) },
                    onTx = { nav.navigate(Routes.tx(it)) },
                    onLogout = {
                        val hasPin = vm.settings.value?.hasPin == true
                        vm.lock()
                        nav.goTop(if (hasPin) Routes.pin(Routes.PIN_LOGIN) else Routes.WELCOME)
                    }
                )
            }
            composable(Routes.CARDS) {
                CardsScreen(
                    vm,
                    onCard = { nav.navigate(Routes.card(it)) },
                    onTopUp = { nav.navigate(Routes.TOPUP) },
                    onWithdraw = { nav.navigate(Routes.WITHDRAW) },
                    onBack = { nav.popBackStack() }
                )
            }
            composable(
                Routes.CARD_DETAILS,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                CardDetailsScreen(vm, entry.arguments?.getString("id").orEmpty()) { nav.popBackStack() }
            }
            composable(Routes.TRANSFERS) {
                TransfersScreen(
                    onPhone = { nav.navigate(Routes.transferForm("phone")) },
                    onCard = { nav.navigate(Routes.transferForm("card")) },
                    onInterbank = { nav.navigate(Routes.transferForm("interbank")) }
                )
            }
            composable(
                Routes.TRANSFER_FORM,
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { entry ->
                TransferFormScreen(
                    vm = vm,
                    type = entry.arguments?.getString("type").orEmpty(),
                    onBack = { nav.popBackStack() },
                    onDetails = { nav.showTx(it) },
                    onDone = { nav.popBackStack(Routes.DASHBOARD, false) }
                )
            }
            composable(Routes.PAYMENTS) {
                PaymentsScreen(vm, onQr = { nav.navigate(Routes.QR) }, onDetails = { nav.showTx(it) })
            }
            composable(Routes.QR) {
                QrScannerScreen(vm, onBack = { nav.popBackStack() }, onDetails = { nav.showTx(it) })
            }
            composable(Routes.TOPUP) {
                TopUpScreen(vm, onBack = { nav.popBackStack() }, onDetails = { nav.showTx(it) })
            }
            composable(Routes.WITHDRAW) {
                WithdrawScreen(vm, onBack = { nav.popBackStack() }, onDetails = { nav.showTx(it) })
            }
            composable(Routes.HISTORY) {
                HistoryScreen(vm) { nav.navigate(Routes.tx(it)) }
            }
            composable(
                Routes.TX_DETAILS,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                TransactionDetailsScreen(vm, entry.arguments?.getString("id").orEmpty()) { nav.popBackStack() }
            }
            composable(Routes.STATS) {
                StatisticsScreen(vm) { nav.popBackStack() }
            }
            composable(Routes.NOTIFICATIONS) {
                NotificationsScreen(vm) { nav.popBackStack() }
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    vm = vm,
                    onSecurity = { nav.navigate(Routes.SECURITY) },
                    onSettings = { nav.navigate(Routes.SETTINGS) },
                    onLanguage = { nav.navigate(Routes.LANGUAGE) },
                    onSupport = { nav.navigate(Routes.SUPPORT) },
                    onFaq = { nav.navigate(Routes.FAQ) },
                    onAbout = { nav.navigate(Routes.ABOUT) },
                    onLogout = {
                        val hasPin = vm.settings.value?.hasPin == true
                        vm.lock()
                        nav.goTop(if (hasPin) Routes.pin(Routes.PIN_LOGIN) else Routes.WELCOME)
                    },
                    onBack = { nav.popBackStack() }
                )
            }
            composable(Routes.SECURITY) {
                SecurityScreen(
                    vm,
                    onBack = { nav.popBackStack() },
                    onChangePin = { nav.navigate(Routes.pin(Routes.PIN_CHANGE)) }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    vm,
                    onBack = { nav.popBackStack() },
                    onLanguage = { nav.navigate(Routes.LANGUAGE) },
                    onWiped = { nav.goTop(Routes.WELCOME) }
                )
            }
            composable(Routes.LANGUAGE) {
                LanguageScreen { nav.popBackStack() }
            }
            composable(Routes.SUPPORT) {
                SupportScreen(onBack = { nav.popBackStack() }, onFaq = { nav.navigate(Routes.FAQ) })
            }
            composable(Routes.FAQ) {
                FaqScreen { nav.popBackStack() }
            }
            composable(Routes.ABOUT) {
                AboutScreen { nav.popBackStack() }
            }
        }
    }
}

/** Floating pill navigation bar with the QR button in the middle. */
@Composable
private fun SoniBottomBar(current: String?, onSelect: (String) -> Unit, onQr: () -> Unit) {
    val shape = RoundedCornerShape(36.dp)
    Box(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 12.dp)) {
        Row(
            Modifier.fillMaxWidth().height(68.dp)
                .shadow(8.dp, shape, ambientColor = Color(0x440A74F5), spotColor = Color(0x440A74F5))
                .background(Color.White, shape)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BarItem(Icons.Filled.Home, R.string.nav_home, current == Routes.DASHBOARD, Modifier.weight(1f)) { onSelect(Routes.DASHBOARD) }
            BarItem(Icons.Filled.SwapHoriz, R.string.nav_transfers, current == Routes.TRANSFERS, Modifier.weight(1f)) { onSelect(Routes.TRANSFERS) }
            Spacer(Modifier.width(72.dp))
            BarItem(Icons.Filled.Apps, R.string.nav_miniapps, current == Routes.PAYMENTS, Modifier.weight(1f)) { onSelect(Routes.PAYMENTS) }
            BarItem(Icons.Filled.History, R.string.nav_history, current == Routes.HISTORY, Modifier.weight(1f)) { onSelect(Routes.HISTORY) }
        }
        Box(
            Modifier.align(Alignment.TopCenter).offset(y = (-30).dp).size(64.dp)
                .shadow(10.dp, CircleShape, ambientColor = Color(0x660A74F5), spotColor = Color(0x660A74F5))
                .background(SoniColors.Teal, CircleShape)
                .clickable(onClick = onQr),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = stringResource(R.string.qr_scanner), tint = Color.White, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun BarItem(icon: ImageVector, label: Int, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val tint = if (selected) SoniColors.Teal else SoniColors.Muted
    Box(modifier.clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Column(
            Modifier.background(if (selected) SoniColors.MintSoft else Color.Transparent, RoundedCornerShape(24.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Text(
                stringResource(label),
                color = tint,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}
