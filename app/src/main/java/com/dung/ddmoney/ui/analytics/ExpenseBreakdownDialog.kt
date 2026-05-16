package com.dung.ddmoney.ui.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dung.ddmoney.ui.components.CategoryIcon
import com.dung.ddmoney.ui.theme.ExpenseRed600
import com.dung.ddmoney.ui.theme.InvestAmber400
import com.dung.ddmoney.ui.theme.LuminousOnSurface
import com.dung.ddmoney.ui.theme.LuminousSurfaceContainerLow
import com.dung.ddmoney.ui.theme.LuminousSurfaceContainerLowest
import com.dung.ddmoney.ui.theme.NeutralGray100
import com.dung.ddmoney.ui.theme.NeutralGray600
import com.dung.ddmoney.ui.theme.OceanBlue50
import com.dung.ddmoney.ui.theme.OceanBlue100
import com.dung.ddmoney.ui.theme.OceanBlue400
import com.dung.ddmoney.ui.theme.OceanBlue600
import com.dung.ddmoney.ui.theme.OceanBlue800
import com.dung.ddmoney.ui.theme.SavingsTeal50
import com.dung.ddmoney.ui.theme.SavingsTeal600
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ExpenseBreakdownDialog(report: ExpenseReport, onDismiss: () -> Unit) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val sheetDragOffset = remember { Animatable(0f) }
    var sheetVisible by remember { mutableStateOf(false) }
    var closeRequested by remember { mutableStateOf(false) }
    val categoryBreakdowns = report.categoryBreakdowns
    val parentCategories = categoryBreakdowns.map { it.parent }
    var selectedCategoryId by remember(categoryBreakdowns) {
        mutableStateOf(parentCategories.firstOrNull()?.id)
    }
    val selectedBreakdown =
            categoryBreakdowns.firstOrNull { it.parent.id == selectedCategoryId }
    val requestClose = {
        if (!closeRequested) closeRequested = true
    }
    val swipeToDismissModifier =
            Modifier.pointerInput(Unit) {
                detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            val nextOffset = (sheetDragOffset.value + dragAmount).coerceAtLeast(0f)
                            scope.launch { sheetDragOffset.snapTo(nextOffset) }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (sheetDragOffset.value > 120f) {
                                    requestClose()
                                } else {
                                    sheetDragOffset.animateTo(
                                            targetValue = 0f,
                                            animationSpec =
                                                    tween(180, easing = FastOutSlowInEasing)
                                    )
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                sheetDragOffset.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(180, easing = FastOutSlowInEasing)
                                )
                            }
                        }
                )
            }

    LaunchedEffect(Unit) {
        sheetDragOffset.snapTo(0f)
        delay(16)
        sheetVisible = true
    }

    LaunchedEffect(closeRequested) {
        if (closeRequested) {
            sheetVisible = false
            delay(300)
            onDismiss()
        }
    }

    Dialog(
            onDismissRequest = requestClose,
            properties =
                    DialogProperties(
                            usePlatformDefaultWidth = false,
                            dismissOnBackPress = true,
                            dismissOnClickOutside = true
                    )
    ) {
        Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                    modifier =
                            Modifier.fillMaxSize()
                                    .pointerInput(closeRequested) {
                                        detectTapGestures { requestClose() }
                                    }
            )

            AnimatedVisibility(
                    visible = sheetVisible,
                    enter =
                            slideInVertically(
                                    initialOffsetY = { fullHeight -> fullHeight + 96 },
                                    animationSpec = tween(340, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(180, delayMillis = 40)),
                    exit =
                            slideOutVertically(
                                    targetOffsetY = { fullHeight -> fullHeight + 96 },
                                    animationSpec = tween(260, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(180))
            ) {
                Surface(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .fillMaxHeight(0.96f)
                                        .offset {
                                            IntOffset(
                                                    x = 0,
                                                    y = sheetDragOffset.value.roundToInt()
                                            )
                                        }
                                        .pointerInput(Unit) {
                                            detectTapGestures { /* Chặn tap bên trong sheet. */ }
                                        },
                        shape =
                                RoundedCornerShape(
                                        topStart = 24.dp,
                                        topEnd = 24.dp,
                                        bottomStart = 0.dp,
                                        bottomEnd = 0.dp
                                ),
                        color = LuminousSurfaceContainerLowest,
                        shadowElevation = 12.dp
                ) {
                    Column(
                            modifier =
                                    Modifier.fillMaxSize()
                                            .padding(top = 10.dp, bottom = 20.dp)
                                            .verticalScroll(scrollState)
                    ) {
                        ExpenseBreakdownHeader(
                                report = report,
                                onClose = requestClose,
                                modifier = swipeToDismissModifier
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (parentCategories.isEmpty()) {
                            EmptyBreakdown()
                        } else {
                            DonutBreakdownChart(
                                    categories = parentCategories,
                                    selectedCategoryId = selectedCategoryId,
                                    onCategorySelected = { tapped ->
                                        selectedCategoryId =
                                                if (selectedCategoryId == tapped.id) null else tapped.id
                                    },
                                    modifier = Modifier.fillMaxWidth().height(300.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            selectedBreakdown?.let { breakdown ->
                                SelectedCategoryDetails(
                                        breakdown = breakdown,
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(horizontal = 10.dp)
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            parentCategories.forEach { category ->
                                BreakdownRow(
                                        category = category,
                                        isSelected = category.id == selectedCategoryId,
                                        onClick = { selectedCategoryId = category.id }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseBreakdownHeader(
        report: ExpenseReport,
        onClose: () -> Unit,
        modifier: Modifier = Modifier
) {
    Row(
            modifier = modifier.fillMaxWidth().padding(start = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                    text = "Khoản chi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OceanBlue600
            )
            Text(
                    text = formatVnd(report.currentTotal),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ExpenseRed600
            )
        }

        IconButton(onClick = onClose) {
            Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Đóng",
                    tint = NeutralGray600
            )
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NeutralGray100.copy(alpha = 0.55f)))
}

@Composable
private fun DonutBreakdownChart(
        categories: List<CategoryExpense>,
        selectedCategoryId: String?,
        onCategorySelected: (CategoryExpense) -> Unit,
        modifier: Modifier = Modifier
) {
    val visibleCategories = categories.take(6)
    val totalVisiblePercentage = visibleCategories.sumOf { it.percentage.toDouble() }.toFloat()
    val normalizeFactor = if (totalVisiblePercentage > 0f) 1f / totalVisiblePercentage else 1f

    val animatedProgress by
            animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(800, easing = FastOutSlowInEasing),
                    label = "expense_donut_progress"
            )

    val selectedIndex = visibleCategories.indexOfFirst { it.id == selectedCategoryId }

    // Per-segment lift (pop-out) animations
    val segmentLiftAnimations =
            visibleCategories.mapIndexed { index, _ ->
                animateFloatAsState(
                        targetValue = if (index == selectedIndex) 1f else 0f,
                        animationSpec = tween(380, easing = FastOutSlowInEasing),
                        label = "donut_lift_$index"
                )
            }

    // Per-segment alpha – unselected dims slightly
    val segmentAlphaAnimations =
            visibleCategories.mapIndexed { index, _ ->
                animateFloatAsState(
                        targetValue =
                                when {
                                    selectedIndex < 0 -> 1f
                                    index == selectedIndex -> 1f
                                    else -> 0.55f
                                },
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        label = "donut_alpha_$index"
                )
            }

    // Center label fade-in
    val centerLabelAlpha by
            animateFloatAsState(
                    targetValue = if (selectedIndex >= 0) 1f else 0f,
                    animationSpec = tween(280),
                    label = "center_label_alpha"
            )

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val density = LocalDensity.current

        // Chart occupies 80% of the available square so badges have room
        val canvasSizeDp = minOf(this.maxWidth, this.maxHeight)
        val chartSizeDp  = canvasSizeDp * 0.80f
        val chartSizePx  = with(density) { chartSizeDp.toPx() }
        val strokeWidthDp = 62.dp
        val strokeWidthPx = with(density) { strokeWidthDp.toPx() }
        val liftDp = 14.dp

        Canvas(
                modifier = Modifier
                        .size(canvasSizeDp)
                        .pointerInput(visibleCategories) {
                            detectTapGestures { offset ->
                                val tappedIndex = findTappedDonutSegment(
                                        offset         = offset,
                                        canvasWidth    = size.width.toFloat(),
                                        canvasHeight   = size.height.toFloat(),
                                        categories     = visibleCategories,
                                        chartSizePx    = chartSizePx,
                                        strokeWidthPx  = strokeWidthPx,
                                        normalizeFactor = normalizeFactor
                                )
                                if (tappedIndex != null) {
                                    onCategorySelected(visibleCategories[tappedIndex])
                                }
                            }
                        }
        ) {
            val center     = Offset(size.width / 2f, size.height / 2f)
            val arcRadius  = (chartSizePx - strokeWidthPx) / 2f   // radius to arc centre-line
            val innerHoleR = arcRadius - strokeWidthPx / 2f        // inner edge of ring

            // ── 1. Draw each segment ────────────────────────────────────────────────
            var startAngle = -90f
            visibleCategories.forEachIndexed { index, category ->
                val normPct  = category.percentage * normalizeFactor
                val sweep    = normPct.coerceIn(0f, 1f) * 360f * animatedProgress
                val gapSweep = if (sweep > 2f) sweep - 1.5f else sweep  // small white gap

                val liftProgress = segmentLiftAnimations[index].value
                val alpha        = segmentAlphaAnimations[index].value

                // Only grow thicker in place — no translation
                val extraStroke = with(density) { 14.dp.toPx() } * liftProgress
                val drawStroke  = strokeWidthPx + extraStroke

                // arcRect expands symmetrically so the ring grows outward AND inward equally
                val arcRect = Rect(
                        center.x - arcRadius - extraStroke / 2f,
                        center.y - arcRadius - extraStroke / 2f,
                        center.x + arcRadius + extraStroke / 2f,
                        center.y + arcRadius + extraStroke / 2f
                )

                drawArc(
                        color      = donutColor(index, category.color).copy(alpha = alpha),
                        startAngle = startAngle,
                        sweepAngle = gapSweep,
                        useCenter  = false,
                        topLeft    = arcRect.topLeft,
                        size       = arcRect.size,
                        style      = Stroke(width = drawStroke, cap = StrokeCap.Butt)
                )

                startAngle += sweep
            }

            // ── 2. White inner circle (covers center, creates donut hole) ─────────
            drawCircle(
                    color  = Color.White.copy(alpha = 0.95f),
                    radius = innerHoleR,
                    center = center
            )
            // Subtle soft ring at hole edge
            drawCircle(
                    color  = NeutralGray100.copy(alpha = 0.6f),
                    radius = innerHoleR,
                    center = center,
                    style  = Stroke(2.dp.toPx())
            )

            // ── 3. Center: only category name when selected ───────────────────────
            if (centerLabelAlpha > 0f && selectedIndex >= 0) {
                val cat = visibleCategories[selectedIndex]
                drawContext.canvas.nativeCanvas.apply {
                    // Category name, centered vertically in hole
                    val namePaint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        setColor(android.graphics.Color.rgb(30, 41, 59))
                        alpha = (centerLabelAlpha * 230).toInt()
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize  = 13.sp.toPx()
                        typeface  = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    drawText(cat.name, center.x, center.y + 5.dp.toPx(), namePaint)
                }
            }

            // ── 4. % on the selected segment ─────────────────────────────────────
            if (selectedIndex >= 0 && centerLabelAlpha > 0.4f) {
                val cat = visibleCategories[selectedIndex]
                val normPct = cat.percentage * normalizeFactor
                val sweep   = normPct.coerceIn(0f, 1f) * 360f * animatedProgress

                // Recompute startAngle for the selected segment
                var selStart = -90f
                for (i in 0 until selectedIndex) {
                    val p = visibleCategories[i].percentage * normalizeFactor
                    selStart += p.coerceIn(0f, 1f) * 360f * animatedProgress
                }
                val midAngleRad = Math.toRadians((selStart + sweep / 2f).toDouble())
                val extraStroke = with(density) { 14.dp.toPx() }
                // Draw text at mid-arc radius of the expanded segment
                val textR = arcRadius + extraStroke / 2f
                val textCenter = Offset(
                        center.x + cos(midAngleRad).toFloat() * textR,
                        center.y + sin(midAngleRad).toFloat() * textR
                )
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        setColor(android.graphics.Color.BLACK)
                        alpha = (centerLabelAlpha * 255).toInt()
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize  = 23.sp.toPx()
                        typeface  = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    drawText(
                            "${(cat.percentage * 100).roundToInt()}%",
                            textCenter.x,
                            textCenter.y + 5.dp.toPx(),
                            paint
                    )
                }
            }
        }

        // ── 4. Icon badges positioned outside the ring ───────────────────────────
        var badgeStart = -90f
        visibleCategories.forEachIndexed { index, category ->
            val normPct   = category.percentage * normalizeFactor
            val sweep     = normPct.coerceIn(0f, 1f) * 360f
            val midAngle  = Math.toRadians((badgeStart + sweep / 2f).toDouble())

            val liftProgress  = segmentLiftAnimations[index].value
            val badgeOuterDp  = chartSizeDp / 2f + 18.dp
            val liftExtra     = liftDp * liftProgress
            val totalRadiusDp = badgeOuterDp + liftExtra

            CategoryIconBadge(
                    category = category,
                    isSelected = category.id == selectedCategoryId,
                    modifier = Modifier
                            .align(Alignment.Center)
                            .offset(
                                    x = (cos(midAngle) * totalRadiusDp.value).dp,
                                    y = (sin(midAngle) * totalRadiusDp.value).dp
                            )
            )
            badgeStart += sweep
        }
    }
}

@Composable
private fun CategoryIconBadge(
        category: CategoryExpense,
        modifier: Modifier = Modifier,
        isSelected: Boolean = false
) {
    val badgeSize by
            animateDpAsState(
                    targetValue = if (isSelected) 40.dp else 34.dp,
                    animationSpec = tween(320, easing = FastOutSlowInEasing),
                    label = "category_badge_size"
            )
    val badgeColor by
            animateColorAsState(
                    targetValue = if (isSelected) OceanBlue100 else SavingsTeal50,
                    animationSpec = tween(320, easing = FastOutSlowInEasing),
                    label = "category_badge_color"
            )

    val badgeBorderColor = SavingsTeal600.copy(alpha = 0.45f)
    Box(
            modifier =
                    modifier.size(badgeSize)
                            .drawBehind {
                                drawRoundRect(
                                        color = badgeBorderColor,
                                        style = Stroke(
                                                width = 1.2f.dp.toPx(),
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                        ),
                                        cornerRadius = CornerRadius(if (isSelected) 14.dp.toPx() else 12.dp.toPx())
                                )
                            }
                            .clip(RoundedCornerShape(if (isSelected) 14.dp else 12.dp))
                            .background(badgeColor),
            contentAlignment = Alignment.Center
    ) {
        CategoryIcon(
                icon = category.icon,
                modifier = Modifier.size(18.dp),
                tint = category.color,
                fallbackFontSize = 17.sp
        )
    }
}

@Composable
private fun SelectedCategoryDetails(
        breakdown: CategoryExpenseGroup,
        modifier: Modifier = Modifier
) {
    val parent = breakdown.parent
    val children = breakdown.children
    val largestChild = children.firstOrNull()

    Column(
            modifier =
                    modifier.clip(RoundedCornerShape(22.dp))
                            .background(OceanBlue50.copy(alpha = 0.72f))
                            .padding(16.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIconBadge(category = parent, isSelected = true)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = parent.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OceanBlue800
                )
                Text(
                        text = "Chiếm ${formatPercent(parent.percentage)} tổng chi kỳ này",
                        fontSize = 12.sp,
                        color = NeutralGray600
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                        text = formatVnd(parent.amount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ExpenseRed600
                )
                Text(
                        text = formatPercent(parent.percentage),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlue600
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DetailMetric(
                    label = "Nhóm con",
                    value = "${children.size}",
                    modifier = Modifier.weight(1f)
            )
            DetailMetric(
                    label = "Lớn nhất",
                    value = largestChild?.name ?: "Chưa có",
                    modifier = Modifier.weight(1f)
            )
        }

        if (children.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                    text = "Chi tiết danh mục con",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LuminousOnSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            children.forEach { child ->
                ChildBreakdownRow(child = child)
            }
        }
    }
}

@Composable
private fun DetailMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 11.sp, color = NeutralGray600)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = LuminousOnSurface
        )
    }
}

@Composable
private fun ChildBreakdownRow(child: CategoryExpense) {
    Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
                modifier =
                        Modifier.size(34.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(child.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
        ) {
            CategoryIcon(
                    icon = child.icon,
                    modifier = Modifier.size(17.dp),
                    tint = child.color,
                    fallbackFontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = child.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LuminousOnSurface
                )
                Text(
                        text = formatPercent(child.percentage),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlue600
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                    progress = { child.percentage.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = child.color,
                    trackColor = LuminousSurfaceContainerLowest.copy(alpha = 0.86f)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                    text = formatVnd(child.amount),
                    fontSize = 12.sp,
                    color = NeutralGray600
            )
        }
    }
}

@Composable
private fun BreakdownRow(category: CategoryExpense, isSelected: Boolean, onClick: () -> Unit) {
    val rowColor by
            animateColorAsState(
                    targetValue = if (isSelected) Color(0xFFF1EBFD) else Color.Transparent,
                    animationSpec = tween(320, easing = FastOutSlowInEasing),
                    label = "breakdown_row_color"
            )
    val horizontalPadding by
            animateDpAsState(
                    targetValue = if (isSelected) 10.dp else 0.dp,
                    animationSpec = tween(320, easing = FastOutSlowInEasing),
                    label = "breakdown_row_padding"
            )

    Column(
            modifier =
                    Modifier.fillMaxWidth()
                            .clickable(onClick = onClick)
                            .drawBehind {
                                drawRect(rowColor)
                                if (isSelected) {
                                    val dotRadius = 1.dp.toPx()
                                    val spacing = 8.dp.toPx()
                                    var x = 0f
                                    while (x < size.width) {
                                        var y = 0f
                                        while (y < size.height) {
                                            drawCircle(
                                                    color = Color(0xFFDCD0F8).copy(alpha = 0.6f),
                                                    radius = dotRadius,
                                                    center = Offset(x, y)
                                            )
                                            y += spacing
                                        }
                                        x += spacing
                                    }
                                }
                            }
                            .padding(horizontal = horizontalPadding + 10.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIconBadge(category = category, isSelected = isSelected)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = category.name,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) OceanBlue800 else LuminousOnSurface
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                        text = formatVnd(category.amount),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ExpenseRed600
                )
                Text(
                        text = formatPercent(category.percentage),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) OceanBlue600 else NeutralGray600
                )
            }
        }

        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .height(1.dp)
                                .padding(start = 48.dp)
                                .background(NeutralGray100.copy(alpha = 0.45f))
        )
    }
}

@Composable
private fun EmptyBreakdown() {
    Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 260.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Text(
                text = "Chưa có khoản chi",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = LuminousOnSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
                text = "Khi có giao dịch chi tiêu, bảng phân bổ sẽ hiện ở đây.",
                fontSize = 13.sp,
                color = NeutralGray600,
                textAlign = TextAlign.Center
        )
    }
}

private fun donutColor(index: Int, fallback: Color): Color {
    val palette =
            listOf(
                    Color(0xFFE83E78),
                    Color(0xFFFF980E),
                    Color(0xFFF6DD63),
                    Color(0xFF86C68A),
                    OceanBlue400,
                    SavingsTeal600
            )
    return palette.getOrNull(index) ?: fallback
}

private fun formatPercent(value: Float): String {
    val percent = value.coerceAtLeast(0f) * 100f
    return if (percent > 0f && percent < 1f) {
        "<1%"
    } else {
        "${percent.roundToInt()}%"
    }
}

private fun findTappedDonutSegment(
        offset: Offset,
        canvasWidth: Float,
        canvasHeight: Float,
        categories: List<CategoryExpense>,
        chartSizePx: Float,
        strokeWidthPx: Float,
        normalizeFactor: Float
): Int? {
    val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
    val dx = offset.x - center.x
    val dy = offset.y - center.y
    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
    
    val outerRadius = chartSizePx / 2f + 16f
    val innerRadius = chartSizePx / 2f - strokeWidthPx + 16f

    if (distance < innerRadius - 18f || distance > outerRadius + 18f) return null

    val tapAngle = ((Math.toDegrees(atan2(dy, dx).toDouble()) + 450.0) % 360.0).toFloat()
    var startAngle = 0f
    categories.forEachIndexed { index, category ->
        val normalizedPercentage = category.percentage * normalizeFactor
        val sweep = normalizedPercentage.coerceIn(0f, 1f) * 360f
        if (tapAngle >= startAngle && tapAngle <= startAngle + sweep) {
            return index
        }
        startAngle += sweep
    }

    return null
}
