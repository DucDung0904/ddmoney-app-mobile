package com.dung.ddmoney

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dung.ddmoney.ui.theme.DDMoneyTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

  
        splashScreen.setKeepOnScreenCondition { false }

        setContent {
            DDMoneyTheme {
                SplashScreenContent(
                    onAnimationFinished = {
                        startActivity(Intent(this, MainActivity::class.java))
                       
                        @Suppress("DEPRECATION")
                        overridePendingTransition(0, 0)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun SplashScreenContent(onAnimationFinished: () -> Unit) {

    // --- Animation states ---
    val logoAlpha    = remember { Animatable(0f) }
    val logoScale    = remember { Animatable(0.6f) }
    val taglineAlpha = remember { Animatable(0f) }
    // screenAlpha: alpha của toàn bộ màn hình – dùng để fade-out khi thoát.
    val screenAlpha  = remember { Animatable(1f) }

    // LaunchedEffect(Unit) chạy 1 lần khi Composable mount, tự cancel khi unmount.
    LaunchedEffect(Unit) {
        val logoAnimSpec = tween<Float>(durationMillis = 800, easing = FastOutSlowInEasing)

        // Bước 1: Logo fade-in + scale-up song song (800ms)
        coroutineScope {
            val alphaJob = async { logoAlpha.animateTo(1f, animationSpec = logoAnimSpec) }
            val scaleJob = async { logoScale.animateTo(1f, animationSpec = logoAnimSpec) }
            alphaJob.await()
            scaleJob.await()
        }

        // Bước 2: Tagline stagger fade-in (delay 200ms + 600ms)
        delay(200)
        taglineAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )

        // Bước 3: Giữ màn hình (600ms – tổng vẫn ~2.5s khi tính cả fade-out)
        delay(600)

        // Bước 4: Fade-out TOÀN MÀN HÌNH (screenAlpha 1 → 0, 500ms)
        // Người dùng thấy nội dung mờ dần trước khi MainActivity xuất hiện.
        screenAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )

        // Bước 5: Navigate sau khi fade-out hoàn tất
        onAnimationFinished()
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)            // ← Fade-out toàn màn hình
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1D4ED8), // Primary700 – trên cùng
                        Color(0xFF3B82F6), // Primary500 – giữa
                        Color(0xFF60A5FA)  // Primary400 – dưới
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Logo: fade-in + scale-up
            Image(
                painter = painterResource(id = R.drawable.logo_appdd),
                contentDescription = "Logo DDMoney",
                modifier = Modifier
                    .size(160.dp)
                    .alpha(logoAlpha.value)
                    .scale(logoScale.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "DDMoney",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline – stagger fade-in
            Text(
                text = "Kiểm soát tài chính, nhẹ nhàng hơn",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }
    }
}
