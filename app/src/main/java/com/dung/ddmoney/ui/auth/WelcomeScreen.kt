package com.dung.ddmoney.ui.auth

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.withStyle
import com.dung.ddmoney.R
import com.dung.ddmoney.ui.theme.Space2
import com.dung.ddmoney.ui.theme.Space3
import com.dung.ddmoney.ui.theme.Space4
import com.dung.ddmoney.ui.theme.Space6

private data class WelcomeSlide(
    @param:DrawableRes val bannerRes: Int,
    val title: String,
    val description: String,
    val imageAlignment: Alignment,
)

@Composable
fun WelcomeScreen(
    onSignUpClick: () -> Unit,
    onLoginClick: () -> Unit,
) {
    val slides = listOf(
        WelcomeSlide(
            bannerRes = R.drawable.banner1,
            title = stringResource(R.string.welcome_slide_overview_title),
            description = stringResource(R.string.welcome_slide_overview_description),
            imageAlignment = BiasAlignment(horizontalBias = 0f, verticalBias = 0.2f),
        ),
        WelcomeSlide(
            bannerRes = R.drawable.banner2,
            title = stringResource(R.string.welcome_slide_insights_title),
            description = stringResource(R.string.welcome_slide_insights_description),
            imageAlignment = Alignment.TopCenter,
        ),
        WelcomeSlide(
            bannerRes = R.drawable.banner3,
            title = stringResource(R.string.welcome_slide_goals_title),
            description = stringResource(R.string.welcome_slide_goals_description),
            imageAlignment = Alignment.TopCenter,
        ),
    )
    val pagerState = rememberPagerState(pageCount = { slides.size })

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        val compact = maxHeight < 720.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Space6)
                .padding(
                    top = if (compact) Space2 else Space3,
                    bottom = Space3,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 420.dp else 488.dp),
                pageSpacing = Space3,
                beyondViewportPageCount = 1,
            ) { page ->
                WelcomeSlideContent(
                    slide = slides[page],
                    compact = compact,
                )
            }

            Spacer(Modifier.height(Space4))
            WelcomePageIndicator(
                pageCount = slides.size,
                selectedPage = pagerState.currentPage,
            )
            Spacer(Modifier.height(if (compact) Space3 else Space4))

            AuthPrimaryButton(
                text = stringResource(R.string.auth_login),
                onClick = onLoginClick,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
            )
            Spacer(Modifier.height(Space3))
            OutlinedButton(
                onClick = onSignUpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    MaterialTheme.colorScheme.primary,
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.welcome_signup),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(if (compact) Space3 else Space4))
            WelcomeLegalFooter()
        }
    }
}

@Composable
private fun WelcomeSlideContent(
    slide: WelcomeSlide,
    compact: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Image(
            painter = painterResource(slide.bannerRes),
            contentDescription = slide.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 276.dp else 348.dp),
            alignment = slide.imageAlignment,
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.height(if (compact) Space3 else Space4))
        Text(
            text = slide.title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = if (compact) 26.sp else 30.sp,
                lineHeight = if (compact) 32.sp else 36.sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        Spacer(Modifier.height(Space2))
        Text(
            text = slide.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = if (compact) 14.sp else 15.sp,
                lineHeight = if (compact) 20.sp else 22.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.96f),
            maxLines = 3,
        )
    }
}

@Composable
private fun WelcomeLegalFooter(
    modifier: Modifier = Modifier,
) {
    val linkStyle = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
    )
    Text(
        text = buildAnnotatedString {
            append(stringResource(R.string.login_terms_prefix))
            append(" ")
            withStyle(linkStyle) {
                append(stringResource(R.string.auth_terms))
            }
            append(" ")
            append(stringResource(R.string.login_terms_join))
            append(" ")
            withStyle(linkStyle) {
                append(stringResource(R.string.auth_privacy))
            }
            append(" ")
            append(stringResource(R.string.welcome_terms_suffix))
        },
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines = 2,
    )
}

@Composable
private fun WelcomePageIndicator(
    pageCount: Int,
    selectedPage: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val size by animateDpAsState(
                targetValue = if (selectedPage == index) 12.dp else 8.dp,
                animationSpec = tween(220),
                label = "welcomeIndicatorSize",
            )
            val color by animateColorAsState(
                targetValue = if (selectedPage == index) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                animationSpec = tween(180),
                label = "welcomeIndicatorColor",
            )
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .width(size)
                    .height(size)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
