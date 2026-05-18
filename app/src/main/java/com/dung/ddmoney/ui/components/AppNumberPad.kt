package com.dung.ddmoney.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardBackspace
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.theme.LuminousOnSurface
import com.dung.ddmoney.ui.theme.LuminousSurfaceContainerLow
import com.dung.ddmoney.ui.theme.LuminousSurfaceContainerLowest
import com.dung.ddmoney.ui.theme.NumberPadBlue
import com.dung.ddmoney.ui.theme.OceanBlue50

@Composable
fun AppNumberPad(
    onKeyPress: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyHeight = 42.dp
    val keyGap = 5.dp
    val leftRows =
        listOf(
            listOf("C", "÷", "×"),
            listOf("7", "8", "9"),
            listOf("4", "5", "6"),
            listOf("1", "2", "3"),
            listOf("0", "000", ".")
        )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(keyGap)
    ) {
        Column(
            modifier = Modifier.weight(3f),
            verticalArrangement = Arrangement.spacedBy(keyGap)
        ) {
            leftRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(keyGap)
                ) {
                    row.forEach { key ->
                        AppNumberPadButton(
                            text = key,
                            modifier = Modifier
                                .weight(1f)
                                .height(keyHeight),
                            onClick = { onKeyPress(key) },
                            isOperator = key in setOf("C", "÷", "×")
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(keyGap)
        ) {
            AppNumberPadButton(
                text = "DEL",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(keyHeight),
                onClick = { onKeyPress("DEL") },
                isOperator = true,
                icon = Icons.AutoMirrored.Filled.KeyboardBackspace
            )
            AppNumberPadButton(
                text = "-",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(keyHeight),
                onClick = { onKeyPress("-") },
                isOperator = true
            )
            AppNumberPadButton(
                text = "+",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(keyHeight),
                onClick = { onKeyPress("+") },
                isOperator = true
            )
            AppNumberPadDoneButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(keyHeight * 2 + keyGap),
                onClick = onDone
            )
        }
    }
}

@Composable
private fun AppNumberPadButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit,
    isOperator: Boolean = false,
    icon: ImageVector? = null
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = LuminousSurfaceContainerLow,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = NumberPadBlue,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = text,
                    fontSize = if (text.length > 1) 17.sp else 19.sp,
                    fontWeight = if (isOperator) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isOperator) NumberPadBlue else LuminousOnSurface
                )
            }
        }
    }
}

@Composable
private fun AppNumberPadDoneButton(
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = NumberPadBlue,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "XONG",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = LuminousSurfaceContainerLowest
            )
        }
    }
}

@Composable
fun AppMoneyNumberPadPanel(
    amountText: String,
    onAmountChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 16.dp),
    showSuggestions: Boolean = true
) {
    Column(modifier = modifier.padding(contentPadding)) {
        if (showSuggestions) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                defaultMoneySuggestions.forEach { suggestion ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clickable { onAmountChange(suggestion.rawValue) },
                        shape = RoundedCornerShape(10.dp),
                        color = OceanBlue50,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = suggestion.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NumberPadBlue
                            )
                        }
                    }
                }
            }
        }

        AppNumberPad(
            onKeyPress = { key -> onAmountChange(applyMoneyInputKey(amountText, key)) },
            onDone = onDone
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("UNUSED_PARAMETER")
fun AppMoneyNumberPadSheet(
    visible: Boolean,
    amountText: String,
    onAmountChange: (String) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Nhập số tiền",
    showSuggestions: Boolean = true
) {
    if (!visible) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = LuminousSurfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        scrimColor = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            AppMoneyNumberPadPanel(
                amountText = amountText,
                onAmountChange = onAmountChange,
                onDone = onDismiss,
                showSuggestions = showSuggestions
            )
        }
    }
}

private data class MoneySuggestion(
    val label: String,
    val rawValue: String
)

private val defaultMoneySuggestions =
    listOf(
        MoneySuggestion("20.000", "20000"),
        MoneySuggestion("200.000", "200000"),
        MoneySuggestion("2.000.000", "2000000"),
        MoneySuggestion("20m", "20000000")
    )
