package com.dung.ddmoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider

import androidx.navigation.compose.rememberNavController
import com.dung.ddmoney.ui.navigation.NavGraph
import com.dung.ddmoney.ui.theme.DDMoneyTheme

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by appViewModel.isDarkMode.collectAsState()
            val navController = rememberNavController()

            // Tính toán 1 lần duy nhất khi Composition được tạo lần đầu.
            // remember đảm bảo không bị recompute khi xoay màn hình hay recompose.
            val startDestination = remember { appViewModel.resolveStartDestination() }

            DDMoneyTheme(darkTheme = isDarkMode) {
                NavGraph(
                    navController = navController,
                    viewModel = appViewModel,
                    startDestination = startDestination
                )
            }
        }
    }
}
