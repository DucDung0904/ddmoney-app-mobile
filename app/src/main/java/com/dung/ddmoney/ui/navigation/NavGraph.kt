package com.dung.ddmoney.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dung.ddmoney.AppViewModel
import com.dung.ddmoney.ui.auth.LoginScreen
import com.dung.ddmoney.ui.auth.OnboardingScreen
import com.dung.ddmoney.ui.auth.RegisterScreen
import com.dung.ddmoney.ui.auth.WelcomeScreen
import com.dung.ddmoney.ui.budget.BudgetScreen
import com.dung.ddmoney.ui.categories.CategoriesScreen
import com.dung.ddmoney.ui.dashboard.DashboardScreen
import com.dung.ddmoney.ui.dashboard.components.BottomNavBar
import com.dung.ddmoney.ui.dashboard.components.AddTransactionFab
import com.dung.ddmoney.ui.settings.SettingsScreen
import com.dung.ddmoney.ui.transactions.AddTransactionScreen
import com.dung.ddmoney.ui.wallets.TransferScreen
import com.dung.ddmoney.ui.wallets.WalletsScreen
import com.dung.ddmoney.network.dto.AuthRequest
import com.dung.ddmoney.network.dto.RegisterRequest

object Routes {
    // ── Auth ──────────────────────────────────────────────────────────
    const val WELCOME     = "welcome"
    const val LOGIN       = "login"
    const val REGISTER    = "register"
    const val ONBOARDING  = "onboarding"
    // ── Main ──────────────────────────────────────────────────────────
    const val HOME     = "home"
    const val STATS    = "stats"
    const val BUDGET   = "budget"
    const val ACCOUNT  = "account"   // Cài đặt (Settings)
    const val WALLETS  = "wallets"   // Quản lý ví (from settings)
    const val CATEGORIES = "categories" // Quản lý danh mục
    const val TRANSFER = "transfer"
    const val ADD_TRANSACTION = "add_transaction"
    const val ADD_BUDGET = "add_budget"

    fun addTransaction(type: String = "EXPENSE") = "add_transaction?type=$type"
}

// Routes hiển thị Bottom Nav Bar
private val mainRoutes = setOf(Routes.HOME, Routes.STATS, Routes.BUDGET, Routes.ACCOUNT)

// Map route hiện tại → nav item được highlight
private fun currentNavItem(route: String?): String = when {
    route == null -> "home"
    route.startsWith(Routes.ADD_TRANSACTION) -> "home"
    route == Routes.STATS -> "stats"
    route == Routes.BUDGET -> "budget"
    route == Routes.ACCOUNT || route == Routes.WALLETS -> "account"
    else -> "home"
}

@Composable
fun NavGraph(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val appState by viewModel.state.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isOnboardingDone by viewModel.isOnboardingDone.collectAsState()
    val authLoading by viewModel.authLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val startDestination = when {
        !isLoggedIn -> Routes.WELCOME
        !isOnboardingDone -> Routes.ONBOARDING
        else -> Routes.HOME
    }

    // Show error messages from API calls
    LaunchedEffect(appState.error) {
        appState.error?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "Retry",
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val showBottomBar = mainRoutes.any { currentRoute == it }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    actionOnNewLine = false
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    selectedRoute = currentNavItem(currentRoute),
                    onItemSelected = { route ->
                        when (route) {
                            "home" -> navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                            "stats" -> navController.navigate(Routes.STATS) {
                                popUpTo(Routes.HOME); launchSingleTop = true
                            }
                            "budget" -> navController.navigate(Routes.BUDGET) {
                                popUpTo(Routes.HOME); launchSingleTop = true
                            }
                            "account" -> navController.navigate(Routes.ACCOUNT) {
                                popUpTo(Routes.HOME); launchSingleTop = true
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                AddTransactionFab(
                    onClick = { navController.navigate(Routes.addTransaction("EXPENSE")) }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            // ── Auth ───────────────────────────────────────────────────
            // ── Welcome (landing) ──────────────────────────────────────
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onSignUpClick = { navController.navigate(Routes.REGISTER) },
                    onLoginClick  = { navController.navigate(Routes.LOGIN) },
                    onGoogleSignIn = { /* TODO: Firebase Google Sign-In */ }
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginClick = { email, password ->
                        viewModel.login(AuthRequest(email, password)) {
                            val dest = if (viewModel.isOnboardingDone.value) Routes.HOME else Routes.ONBOARDING
                            navController.navigate(dest) {
                                popUpTo(Routes.WELCOME) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                    onGoogleSignIn = { /* TODO: Firebase Google Sign-In */ },
                    onBack = { navController.popBackStack() },
                    isLoading = authLoading,
                    errorMessage = appState.error
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegisterClick = { fullName, email, password ->
                        viewModel.register(RegisterRequest(fullName, email, password)) {
                            // After register → always go to onboarding
                            navController.navigate(Routes.ONBOARDING) {
                                popUpTo(Routes.REGISTER) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.WELCOME)
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onGoogleSignIn = { /* TODO: Firebase Google Sign-In */ },
                    isLoading = authLoading,
                    errorMessage = appState.error
                )
            }

            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    userName = appState.userInfo.name,
                    isLoading = appState.isLoading,
                    onComplete = { currency, walletName, walletBalance ->
                        viewModel.completeOnboarding(currency, walletName, walletBalance)
                        viewModel.syncAll()
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            // ── Tổng quan ─────────────────────────────────────────────
            composable(Routes.HOME) {
                DashboardScreen(appState = appState, navController = navController)
            }

            composable(Routes.STATS) {
                com.dung.ddmoney.ui.analytics.AnalyticsScreen()
            }

            // ── Quản lý Danh mục ──────────────────────────────────────
            composable(Routes.CATEGORIES) {
                CategoriesScreen(
                    appState = appState,
                    onAddCategory = { name, icon, color, type -> viewModel.addCategory(name, icon, color, type) },
                    onEditCategory = { viewModel.editCategory(it) },
                    onDeleteCategory = { viewModel.deleteCategory(it) },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Ngân sách ─────────────────────────────────────────────
            composable(Routes.BUDGET) {
                com.dung.ddmoney.ui.budget.BudgetScreen(
                    appState = appState,
                    onAddBudgetClick = { navController.navigate(Routes.ADD_BUDGET) }
                )
            }

            composable(Routes.ADD_BUDGET) {
                com.dung.ddmoney.ui.budget.AddBudgetScreen(
                    appState = appState,
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Cài đặt → SettingsScreen ──────────────────────────────
            composable(Routes.ACCOUNT) {
                val isDarkMode by viewModel.isDarkMode.collectAsState()
                SettingsScreen(
                    appState = appState,
                    isDarkMode = isDarkMode,
                    onDarkModeToggle = { viewModel.setDarkMode(it) },
                    onManageWallets = { navController.navigate(Routes.WALLETS) },
                    onLogout = {
                        viewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    onUpdateAvatar = { url -> viewModel.updateAvatar(url) }
                )
            }

            // ── Quản lý ví (từ Cài đặt, không có bottom nav) ─────────
            composable(Routes.WALLETS) {
                WalletsScreen(
                    appState = appState,
                    onAddWallet = { name, balance, type, bank, color -> viewModel.addWallet(name, balance, type, bank, color) },
                    onEditWallet = { viewModel.editWallet(it) },
                    onDeleteWallet = { viewModel.deleteWallet(it) },
                    onTransfer = { navController.navigate(Routes.TRANSFER) },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Chuyển tiền (không có bottom nav) ────────────────────
            composable(Routes.TRANSFER) {
                TransferScreen(
                    appState = appState,
                    onTransfer = { fromId, toId, amount, date, note ->
                        viewModel.transfer(fromId, toId, amount, date, note)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Thêm giao dịch (không có bottom nav) ─────────────────
            composable(
                route = "${Routes.ADD_TRANSACTION}?type={type}",
                arguments = listOf(navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "EXPENSE"
                })
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "EXPENSE"
                AddTransactionScreen(
                    initialType = type,
                    appState = appState,
                    onSave = { title, categoryId, amount, transactionType, walletId, date, note ->
                        viewModel.addTransaction(title, categoryId, amount, transactionType, walletId, date, note)
                        val popped = navController.popBackStack()
                        if (!popped) navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                    },
                    onBack = {
                        val popped = navController.popBackStack()
                        if (!popped) navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } }
                    }
                )
            }
        }
    }
}
