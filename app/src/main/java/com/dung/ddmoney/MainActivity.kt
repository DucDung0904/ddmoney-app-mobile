package com.dung.ddmoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.dung.ddmoney.ui.navigation.NavGraph
import com.dung.ddmoney.ui.theme.DDMoneyTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {

    // AndroidViewModelFactory cung cấp Application context cho AppViewModel
    private val appViewModel: AppViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { 
            val isDarkMode by appViewModel.isDarkMode.collectAsState()
            DDMoneyTheme(darkTheme = isDarkMode) { 
                NavGraph(viewModel = appViewModel) 
            } 
        }
    }
}
