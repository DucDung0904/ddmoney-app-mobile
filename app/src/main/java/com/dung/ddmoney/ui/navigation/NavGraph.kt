package com.dung.ddmoney.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.dung.ddmoney.AppViewModel
import com.dung.ddmoney.network.dto.AuthRequest
import com.dung.ddmoney.network.dto.RegisterRequest
import com.dung.ddmoney.ui.analytics.AnalyticsScreen
import com.dung.ddmoney.ui.auth.*
import com.dung.ddmoney.ui.home.HomeScreen
import com.dung.ddmoney.ui.profile.ProfileScreen
import com.dung.ddmoney.ui.wallets.WalletEditorScreen
import com.dung.ddmoney.ui.wallets.WalletListScreen
import com.dung.ddmoney.ui.budget.BudgetScreen
import com.dung.ddmoney.ui.dashboard.model.Wallet
import kotlinx.coroutines.launch

// Định nghĩa hiệu ứng Micro-interaction chung
private val duration = 400
private val microInteractionEasing = FastOutSlowInEasing

@Composable
fun NavGraph(
        navController: NavHostController,
        viewModel: AppViewModel,
        startDestination: String = Routes.WELCOME
) {
    val authLoading by viewModel.authLoading.collectAsState()
    val appState by viewModel.state.collectAsState()

    NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = {
                fadeIn(animationSpec = tween(duration, easing = microInteractionEasing)) +
                        scaleIn(
                                initialScale = 0.95f,
                                animationSpec = tween(duration, easing = microInteractionEasing)
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(duration, easing = microInteractionEasing)) +
                        scaleOut(
                                targetScale = 0.95f,
                                animationSpec = tween(duration, easing = microInteractionEasing)
                        )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(duration, easing = microInteractionEasing)) +
                        scaleIn(
                                initialScale = 0.95f,
                                animationSpec = tween(duration, easing = microInteractionEasing)
                        )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(duration, easing = microInteractionEasing)) +
                        scaleOut(
                                targetScale = 0.95f,
                                animationSpec = tween(duration, easing = microInteractionEasing)
                        )
            }
    ) {
        // --- AUTH FLOW ---
        composable(Routes.WELCOME) {
            WelcomeScreen(
                    onLoginClick = { navController.navigate(Routes.LOGIN) },
                    onSignUpClick = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                    onLoginClick = { email, password ->
                        viewModel.login(AuthRequest(email, password)) { isNewUser ->
                            if (isNewUser) {
                                navController.navigate(Routes.ONBOARDING)
                            } else {
                                navController.navigate(Routes.MAIN) {
                                    popUpTo(Routes.WELCOME) { inclusive = true }
                                }
                            }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                    onBack = { navController.popBackStack() },
                    isLoading = authLoading,
                    errorMessage = appState.error
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                    onRegisterClick = { fullName, email, password ->
                        viewModel.register(RegisterRequest(fullName, email, password)) {
                            navController.navigate(Routes.LOGIN)
                        }
                    },
                    onNavigateToLogin = { navController.navigate(Routes.LOGIN) },
                    onBack = { navController.popBackStack() },
                    isLoading = authLoading,
                    errorMessage = appState.error
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                    userName = appState.userInfo.name,
                    isLoading = appState.isLoading,
                    onComplete = { currency, walletName, walletBalance, walletIcon, walletType ->
                        viewModel.completeOnboarding(
                                currency,
                                walletName,
                                walletBalance,
                                walletIcon,
                                walletType
                        )
                        navController.navigate(Routes.MAIN) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
            )
        }

        // --- MAIN APP FLOW (WITH BOTTOM NAV) ---
        composable(Routes.MAIN) { MainContainer(viewModel, navController) }
    }
}

@Composable
fun MainContainer(viewModel: AppViewModel, rootNavController: NavHostController) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Sử dụng route gốc để thanh điều hướng không highlight nhầm tab khi ở màn hình con
    val rawRoute = navBackStackEntry?.destination?.route
    val currentRoute = rawRoute ?: NavItem.Home.route

    val state by viewModel.state.collectAsState()
    var showAddTransaction by remember { mutableStateOf(false) }
    var walletEditorTarget by remember { mutableStateOf<Wallet?>(null) }
    var showWalletEditor by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    BottomNavBar(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                // Nếu nhấn vào Tab Trang chủ khi đang ở màn hình con (wallet_list),
                                // quay lại thay vì navigate
                                if (route == NavItem.Home.route && rawRoute == "wallet_list") {
                                    navController.popBackStack()
                                } else {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            onAddClick = { showAddTransaction = true }
                    )
                }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Nội dung chính
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(top = paddingValues.calculateTopPadding())
                ) {
                    NavHost(
                            navController = navController,
                            startDestination = NavItem.Home.route,
                            modifier = Modifier.fillMaxSize(),
                            enterTransition = {
                                fadeIn(
                                        animationSpec =
                                                tween(duration, easing = microInteractionEasing)
                                ) +
                                        scaleIn(
                                                initialScale = 0.95f,
                                                animationSpec =
                                                        tween(
                                                                duration,
                                                                easing = microInteractionEasing
                                                        )
                                        )
                            },
                            exitTransition = {
                                fadeOut(
                                        animationSpec =
                                                tween(duration, easing = microInteractionEasing)
                                ) +
                                        scaleOut(
                                                targetScale = 0.95f,
                                                animationSpec =
                                                        tween(
                                                                duration,
                                                                easing = microInteractionEasing
                                                        )
                                        )
                            },
                            popEnterTransition = {
                                fadeIn(
                                        animationSpec =
                                                tween(duration, easing = microInteractionEasing)
                                ) +
                                        scaleIn(
                                                initialScale = 0.95f,
                                                animationSpec =
                                                        tween(
                                                                duration,
                                                                easing = microInteractionEasing
                                                        )
                                        )
                            },
                            popExitTransition = {
                                fadeOut(
                                        animationSpec =
                                                tween(duration, easing = microInteractionEasing)
                                ) +
                                        scaleOut(
                                                targetScale = 0.95f,
                                                animationSpec =
                                                        tween(
                                                                duration,
                                                                easing = microInteractionEasing
                                                        )
                                        )
                            }
                    ) {
                        composable(NavItem.Home.route) {
                            HomeScreen(
                                    userName = state.userInfo.name,
                                    totalBalance =
                                            state.wallets
                                                    .filter { it.isIncludedInTotal }
                                                    .sumOf { it.balance },
                                    wallets = state.wallets,
                                    categories = state.categories,
                                    transactions = state.transactions,
                                    recentTransactions = state.transactions.take(10),
                                    onSeeAllWallets = { navController.navigate("wallet_list") },
                                    onAddWallet = {
                                        walletEditorTarget = null
                                        showWalletEditor = true
                                    }
                            )
                        }
                        composable("wallet_list") {
                            WalletListScreen(
                                    wallets = state.allWallets,
                                    onBack = { navController.popBackStack() },
                                    onAddWallet = {
                                        walletEditorTarget = null
                                        showWalletEditor = true
                                    },
                                    onWalletClick = { wallet ->
                                        walletEditorTarget = wallet
                                        showWalletEditor = true
                                    },
                                    onUnarchiveWallet = { wallet ->
                                        viewModel.unarchiveWallet(wallet.id) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Đã bỏ lưu trữ ví")
                                            }
                                        }
                                    }
                            )
                        }
                        composable(NavItem.Budget.route) { BudgetScreen(viewModel) }
                        composable(NavItem.Analytics.route) {
                            AnalyticsScreen(
                                    transactions = state.transactions,
                                    categories = state.categories
                            )
                        }
                        composable(NavItem.Profile.route) {
                            ProfileScreen(
                                    userName = state.userInfo.name,
                                    userEmail = state.userInfo.email,
                                    avatarUrl = state.userInfo.avatarUrl,
                                    onManageWallets = { navController.navigate("wallet_list") },
                                    onUpdateAvatar = { url -> viewModel.updateAvatar(url) },
                                    onLogout = {
                                        viewModel.logout()
                                        rootNavController.navigate(Routes.WELCOME) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                            )
                        }
                    }
                }

                // Lớp phủ mờ (Fog Effect) ở đáy màn hình
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(140.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(
                                                brush =
                                                        Brush.verticalGradient(
                                                                colors =
                                                                        listOf(
                                                                                Color.Transparent,
                                                                                Color(0xFFE9EEF7)
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.5f
                                                                                        ),
                                                                                Color(0xFFE9EEF7)
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.95f
                                                                                        )
                                                                        )
                                                        )
                                        )
                )
            }
        } // Close Scaffold

        // Add Transaction Modal Overlay
        androidx.compose.animation.AnimatedVisibility(
                visible = showAddTransaction,
                enter =
                        androidx.compose.animation.slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ),
                exit =
                        androidx.compose.animation.slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = tween(400, easing = FastOutSlowInEasing)
                        )
        ) {
            com.dung.ddmoney.ui.transaction.AddTransactionScreen(
                    wallets = state.wallets,
                    categories = state.categories,
                    onSave = { amount, walletId, categoryId, type, note, date ->
                        viewModel.addTransaction(
                                com.dung.ddmoney.network.dto.TransactionRequest(
                                        title = null, // Will be filled by category name in
                                        // repo/server if needed
                                        amount = amount,
                                        type = type,
                                        date = date.toString(),
                                        walletId = walletId.toLong(),
                                        categoryId = categoryId.toLong(),
                                        note = note
                                )
                        ) {
                            showAddTransaction = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Đã thêm giao dịch thành công!")
                            }
                        }
                    },
                    onDismiss = { showAddTransaction = false }
            )
        }

        AnimatedVisibility(
                visible = showWalletEditor,
                modifier = Modifier.fillMaxSize(),
                enter =
                        slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(360, easing = FastOutSlowInEasing)
                        ) +
                                fadeIn(
                                        animationSpec =
                                                tween(220, easing = FastOutSlowInEasing)
                                ),
                exit =
                        slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) +
                                fadeOut(
                                        animationSpec =
                                                tween(180, easing = FastOutSlowInEasing)
                                )
        ) {
            WalletEditorScreen(
                    wallet = walletEditorTarget,
                    wallets = state.wallets,
                    onDismiss = { showWalletEditor = false },
                    onSave = { request ->
                        val editingWallet = walletEditorTarget
                        if (editingWallet == null) {
                            viewModel.createWallet(request) {
                                showWalletEditor = false
                            }
                        } else {
                            viewModel.updateWallet(editingWallet.id, request) {
                                showWalletEditor = false
                            }
                        }
                    },
                    onArchive = { wallet ->
                        viewModel.archiveWallet(wallet.id) {
                            showWalletEditor = false
                        }
                    },
                    onTransfer = { fromWallet, toWallet, amount ->
                        viewModel.transferWallet(fromWallet.id, toWallet.id, amount) {
                            showWalletEditor = false
                        }
                    }
            )
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Màn hình $name đang phát triển", fontSize = 18.sp, color = Color.Gray)
    }
}

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"
}
