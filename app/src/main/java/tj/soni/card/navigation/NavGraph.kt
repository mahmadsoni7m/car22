package tj.soni.card.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
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
import tj.soni.card.ui.screens.*
import tj.soni.card.ui.theme.SoniColors
import tj.soni.card.viewmodel.AppViewModel

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

private fun NavHostController.showTx(id: String) {
    navigate(Routes.tx(id)) { popUpTo(Routes.DASHBOARD) }
}

@Composable
fun SoniApp(vm: AppViewModel = viewModel()) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val settings by vm.settings.collectAsStateWithLifecycle()
    val unlocked = vm.unlocked

    // Guard: protected screens are never shown while the app is locked.
    LaunchedEffect(route, unlocked, settings) {
        val s = settings
        if (route != null && s != null) {
            val pinMode = backStack?.arguments?.getString("mode")
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
                SoniBottomBar(route) { nav.goBottom(it) }
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
                    onTransfer = { nav.navigate(Routes.TRANSFERS) },
                    onTopUp = { nav.navigate(Routes.TOPUP) },
                    onPayments = { nav.goBottom(Routes.PAYMENTS) },
                    onQr = { nav.navigate(Routes.QR) },
                    onNotifications = { nav.navigate(Routes.NOTIFICATIONS) },
                    onTx = { nav.navigate(Routes.tx(it)) },
                    onHistory = { nav.goBottom(Routes.HISTORY) },
                    onStats = { nav.navigate(Routes.STATS) },
                    onCards = { nav.goBottom(Routes.CARDS) }
                )
            }
            composable(Routes.CARDS) {
                CardsScreen(
                    vm,
                    onCard = { nav.navigate(Routes.card(it)) },
                    onTopUp = { nav.navigate(Routes.TOPUP) },
                    onWithdraw = { nav.navigate(Routes.WITHDRAW) }
                )
            }
            composable(
                Routes.CARD_DETAILS,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                CardDetailsScreen(vm, entry.arguments?.getString("id").orEmpty()) { nav.popBackStack() }
            }
            composable(Routes.TRANSFERS) {
                TransfersScreen(vm, onBack = { nav.popBackStack() }, onContinue = { nav.navigate(Routes.TRANSFER_CONFIRM) })
            }
            composable(Routes.TRANSFER_CONFIRM) {
                TransferConfirmScreen(
                    vm,
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
                    }
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

@Composable
private fun SoniBottomBar(current: String?, onSelect: (String) -> Unit) {
    val items = listOf(
        Triple(Routes.DASHBOARD, Icons.Filled.Home, R.string.nav_home),
        Triple(Routes.CARDS, Icons.Filled.CreditCard, R.string.nav_cards),
        Triple(Routes.PAYMENTS, Icons.Filled.Payments, R.string.nav_payments),
        Triple(Routes.HISTORY, Icons.Filled.History, R.string.nav_history),
        Triple(Routes.PROFILE, Icons.Filled.Person, R.string.nav_profile)
    )
    NavigationBar(containerColor = SoniColors.Surface) {
        items.forEach { (route, icon, label) ->
            NavigationBarItem(
                selected = current == route,
                onClick = { onSelect(route) },
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(stringResource(label), fontSize = 11.sp, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SoniColors.Teal,
                    selectedTextColor = SoniColors.Teal,
                    indicatorColor = SoniColors.MintSoft,
                    unselectedIconColor = SoniColors.Muted,
                    unselectedTextColor = SoniColors.Muted
                )
            )
        }
    }
}
