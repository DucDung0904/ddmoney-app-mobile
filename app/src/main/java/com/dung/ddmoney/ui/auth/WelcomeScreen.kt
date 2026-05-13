package com.dung.ddmoney.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.R
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
        onSignUpClick: () -> Unit,
        onLoginClick: () -> Unit,
        onGoogleSignIn: () -> Unit = {}
) {


    Box(
            modifier =
                    Modifier.fillMaxSize()
                            .background(Color.White)
                            .navigationBarsPadding() // No statusBarsPadding so the background goes
            // to the very top edge
            ) {
        // ── Background Wave (Dải băng) ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path =
                    androidx.compose.ui.graphics.Path().apply {
                        // Start midway on the left (shifted up)
                        moveTo(0f, size.height * 0.35f)

                        // Top edge of ribbon: curving smoothly to top-right
                        cubicTo(
                                size.width * 0.35f,
                                size.height * 0.15f,
                                size.width * 0.65f,
                                size.height * -0.05f,
                                size.width,
                                size.height * 0.05f
                        )

                        // Drop down on the right side (shifted up)
                        lineTo(size.width, size.height * 0.50f)

                        // Bottom edge of ribbon: curving back down to bottom-left
                        cubicTo(
                                size.width * 0.65f,
                                size.height * 0.55f,
                                size.width * 0.35f,
                                size.height * 0.75f,
                                0f,
                                size.height * 0.65f
                        )

                        close()
                    }

            drawPath(
                    path = path,
                    brush =
                            Brush.linearGradient(
                                    colors =
                                            listOf(
                                                    Color(0xFF4060FB)
                                                            .copy(
                                                                    alpha = 0.08f
                                                            ), // Signature blue, highly translucent
                                                    Color(0xFF4060FB).copy(alpha = 0.15f)
                                            ),
                                    start = Offset.Zero,
                                    end = Offset(size.width, size.height)
                            )
            )
        }
        // ── Foreground Content ──
        Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Illustration (Top) ──────────────────────────────────────────────
                Image(
                        painter = painterResource(id = R.drawable.logo_login),
                        contentDescription = "Finance Illustration",
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).scale(1.2f),
                        contentScale = ContentScale.FillWidth
                )

            Spacer(modifier = Modifier.weight(1f))

            // ── Title & Buttons (Bottom) ────────────────────────────────────────
            Column(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(bottom = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                        text = "Chào mừng đến với DDMoney",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        color = Color(0xFF181C20),
                        letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Log In Button (filled)
                    Button(
                            onClick = onLoginClick,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = CircleShape,
                            colors =
                                    ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4060FB)
                                    )
                    ) {
                        Text(
                                text = "Đăng nhập",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                        )
                    }

                    // Sign Up Button (outlined)
                    OutlinedButton(
                            onClick = onSignUpClick,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = CircleShape,
                            border = BorderStroke(2.dp, Color(0xFF4060FB))
                    ) {
                        Text(
                                text = "Đăng ký",
                                color = Color(0xFF4060FB),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
