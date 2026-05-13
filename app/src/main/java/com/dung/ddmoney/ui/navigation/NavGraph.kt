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
import com.dung.ddmoney.ui.auth.*
import com.dung.ddmoney.ui.home.HomeScreen
import com.dung.ddmoney.ui.wallets.WalletListScreen

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
            scaleIn(initialScale = 0.95f, animationSpec = tween(duration, easing = microInteractionEasing))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(duration, easing = microInteractionEasing)) +
            scaleOut(targetScale = 0.95f, animationSpec = tween(duration, easing = microInteractionEasing))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(duration, easing = microInteractionEasing)) +
            scaleIn(initialScale = 0.95f, animationSpec = tween(duration, easing = microInteractionEasing))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(duration, easing = microInteractionEasing)) +
            scaleOut(targetScale = 0.95f, animationSpec = tween(duration, easing = microInteractionEasing))
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
                    viewModel.completeOnboarding(currency, walletName, walletBalance, walletIcon, walletType)
                    navController.navigate(Routes.MAIN) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // --- MAIN APP FLOW (WITH BOTTOM NAV) ---
        composable(Routes.MAIN) {
            MainContainer(viewModel)
        }
    }
}

@Composable
fun MainContainer(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    
    // Sử dụng route gốc để thanh điều hướng không highlight nhầm tab khi ở màn hình con
    val rawRoute = navBackStackEntry?.destination?.route
    val currentRoute = rawRoute ?: NavItem.Home.route
    
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = Color.Transparent, 
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    // Nếu nhấn vào Tab Trang chủ khi đang ở màn hình con (wallet_list), quay lại thay vì navigate
                    if (route == NavItem.Home.route && rawRoute == "wallet_list") {
                        navController.popBackStack()
                    } else {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onAddClick = { /* Show Add Transaction Bottom Sheet */ }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Nội dung chính
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
            ) {
                NavHost(
                    navController = navController,
                    startDestination = NavItem.Home.route,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = {
                        fadeIn(animationSpec = tween(duration, easing = microInteractionEasing)) +
                        scaleIn(initialScale = 0.95f, animationSpec = tween(duration, easing = microInteractionEasing))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(duration, easing = microInteractionEasing)) +
                        scaleOut(targetScale = 0.95f, animationSpec = tween(duration, easing = microInteractionEasing))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(duration, easing = microInteractionEasing)) +
                        scaleIn(initialScale = 0.95f, animationSpec = tween(duration, easing = microInteractionEasing))
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(duration, easing = microInteractionEasing)) +
                        scaleOut(targetScale = 0.95f, animationSpec = tween(duration, easing = microInteractionEasing))
                    }
                ) {
                    composable(NavItem.Home.route) {
                        HomeScreen(
                            userName = state.userInfo.name,
                            totalBalance = state.wallets.sumOf { it.balance },
                            wallets = state.wallets,
                            recentTransactions = state.transactions.take(10),
                            onSeeAllWallets = { navController.navigate("wallet_list") }
                        )
                    }
                    composable("wallet_list") {
                        WalletListScreen(
                            wallets = state.wallets,
                            onBack = { navController.popBackStack() },
                            onAddWallet = { /* Navigate to Add Wallet */ },
                            onWalletClick = { /* Navigate to Wallet Details */ }
                        )
                    }
                    composable(NavItem.Budget.route) { PlaceholderScreen("Ngân sách") }
                    composable(NavItem.Analytics.route) { PlaceholderScreen("Phân tích") }
                    composable(NavItem.Profile.route) { PlaceholderScreen("Cá nhân") }
                }
            }

            // Lớp phủ mờ (Fog Effect) ở đáy màn hình
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp) 
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFE9EEF7).copy(alpha = 0.5f),
                                Color(0xFFE9EEF7).copy(alpha = 0.95f)
                            )
                        )
                    )
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
