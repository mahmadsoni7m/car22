package tj.big.card.navigation

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val WELCOME = "welcome"
    const val PHONE_LOGIN = "phone_login"
    const val SMS = "sms"
    const val PIN = "pin/{mode}"
    const val BIOMETRIC = "biometric"
    const val REGISTRATION = "registration"
    const val DASHBOARD = "dashboard"
    const val CARDS = "cards"
    const val CARD_DETAILS = "card/{id}"
    const val TRANSFERS = "transfers"
    const val TRANSFER_FORM = "transfer/{type}"
    const val PAYMENTS = "payments"
    const val QR = "qr"
    const val TOPUP = "topup"
    const val WITHDRAW = "withdraw"
    const val HISTORY = "history"
    const val TX_DETAILS = "tx/{id}"
    const val STATS = "stats"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val SECURITY = "security"
    const val SETTINGS = "settings"
    const val LANGUAGE = "language"
    const val SUPPORT = "support"
    const val FAQ = "faq"
    const val ABOUT = "about"

    const val PIN_LOGIN = "login"
    const val PIN_CREATE = "create"
    const val PIN_CHANGE = "change"

    fun pin(mode: String) = "pin/$mode"
    fun card(id: String) = "card/$id"
    fun transferForm(type: String) = "transfer/$type"
    fun tx(id: String) = "tx/$id"

    /** Screens reachable without being unlocked. */
    val publicRoutes = setOf(SPLASH, ONBOARDING, WELCOME, PHONE_LOGIN, SMS, PIN, BIOMETRIC, REGISTRATION)

    /** Screens that show the bottom navigation bar. */
    val bottomBarRoutes = setOf(DASHBOARD, TRANSFERS, PAYMENTS, HISTORY)

    /** Screens drawn on the light status bar (everything else has the blue header). */
    val lightRoutes = bottomBarRoutes + setOf(SPLASH, ONBOARDING, WELCOME, BIOMETRIC)
}
