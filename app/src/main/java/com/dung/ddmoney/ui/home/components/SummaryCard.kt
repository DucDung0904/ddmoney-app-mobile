package com.dung.ddmoney.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dung.ddmoney.ui.theme.*

// ─── Enums ────────────────────────────────────────────────────────────────────
enum class SummaryPeriod { WEEK, MONTH }
enum class SummaryType   { EXPENSE, INCOME }
data class ChartPoint(val label: String, val value: Double)

// ─── Placeholder data (swap with ViewModel later) ────────────────────────────
private val weekExpense  = listOf(
    ChartPoint("T2", 150_000.0), ChartPoint("T3", 280_000.0),
    ChartPoint("T4", 420_000.0), ChartPoint("T5", 180_000.0),
    ChartPoint("T6", 550_000.0), ChartPoint("T7", 220_000.0),
    ChartPoint("CN",  90_000.0),
)
private val weekIncome   = listOf(
    ChartPoint("T2",       0.0), ChartPoint("T3", 500_000.0),
    ChartPoint("T4", 200_000.0), ChartPoint("T5", 800_000.0),
    ChartPoint("T6", 300_000.0), ChartPoint("T7",       0.0),
    ChartPoint("CN", 1_000_000.0),
)
private val monthExpense = listOf(
    ChartPoint("1",  500_000.0), ChartPoint("5",  800_000.0),
    ChartPoint("10", 300_000.0), ChartPoint("15", 650_000.0),
    ChartPoint("20", 420_000.0), ChartPoint("25", 900_000.0),
    ChartPoint("31", 250_000.0),
)
private val monthIncome  = listOf(
    ChartPoint("1",  2_000_000.0), ChartPoint("5",  500_000.0),
    ChartPoint("10", 3_000_000.0), ChartPoint("15",       0.0),
    ChartPoint("20", 1_500_000.0), ChartPoint("25", 800_000.0),
    ChartPoint("31", 2_500_000.0),
)

// ─────────────────────────────────────────────────────────────────────────────
//  ThuChiSummaryCard
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun ThuChiSummaryCard(income: Double, expense: Double) {
    var period by remember { mutableStateOf(SummaryPeriod.WEEK) }
    var type   by remember { mutableStateOf(SummaryType.EXPENSE) }

    val chartData = when {
        period == SummaryPeriod.WEEK  && type == SummaryType.EXPENSE -> weekExpense
        period == SummaryPeriod.WEEK  && type == SummaryType.INCOME  -> weekIncome
        period == SummaryPeriod.MONTH && type == SummaryType.EXPENSE -> monthExpense
        else                                                         -> monthIncome
    }
    val lineColor  = if (type == SummaryType.EXPENSE) ExpenseRed600 else IncomeGreen600
    val totalExp   = chartData.sumOf { it.value }.takeIf { type == SummaryType.EXPENSE } ?: expense
    val totalInc   = chartData.sumOf { it.value }.takeIf { type == SummaryType.INCOME  } ?: income

    Surface(
        modifier        = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 20.dp),
        shape           = RoundedCornerShape(24.dp),
        color           = LuminousSurfaceContainerLowest,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Type tabs: Chi | Thu ──────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth()) {
                TypeTab(
                    label    = "Tổng đã chi",
                    amount   = totalExp,
                    color    = ExpenseRed600,
                    selected = type == SummaryType.EXPENSE,
                    modifier = Modifier.weight(1f)
                ) { type = SummaryType.EXPENSE }

                Box(modifier = Modifier.width(1.dp).height(72.dp)
                    .align(Alignment.CenterVertically)
                    .background(LuminousSurfaceContainerLow))

                TypeTab(
                    label    = "Tổng thu",
                    amount   = totalInc,
                    color    = IncomeGreen600,
                    selected = type == SummaryType.INCOME,
                    modifier = Modifier.weight(1f)
                ) { type = SummaryType.INCOME }
            }

            // Separator
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LuminousSurfaceContainerLow))

            Spacer(modifier = Modifier.height(16.dp))

            // ── Period toggle ─────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                PeriodToggle(selected = period) { period = it }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Line chart ────────────────────────────────────────────────────
            LineChart(
                data      = chartData,
                lineColor = lineColor,
                modifier  = Modifier.fillMaxWidth().height(120.dp)
                    .padding(start = 20.dp, end = 20.dp)
            )

            // ── X-axis labels ─────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                chartData.forEach { pt ->
                    Text(pt.label, fontSize = 9.sp, color = NeutralGray400, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─── Type tab (Chi / Thu) ────────────────────────────────────────────────────
@Composable
private fun TypeTab(
    label:    String,
    amount:   Double,
    color:    Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick:  () -> Unit
) {
    Column(
        modifier            = modifier.clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 11.sp, color = NeutralGray600, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text       = formatAmt(amount),
            fontSize   = 18.sp,
            fontWeight = FontWeight.Black,
            color      = color
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth(0.65f).height(2.5.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (selected) color else Color.Transparent)
        )
    }
}

// ─── Period toggle (Tuần | Tháng) ────────────────────────────────────────────
@Composable
private fun PeriodToggle(selected: SummaryPeriod, onSelect: (SummaryPeriod) -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(50))
            .background(LuminousSurfaceContainerLow)
            .padding(3.dp)
    ) {
        Row {
            PTab("Tuần",  selected == SummaryPeriod.WEEK)  { onSelect(SummaryPeriod.WEEK) }
            PTab("Tháng", selected == SummaryPeriod.MONTH) { onSelect(SummaryPeriod.MONTH) }
        }
    }
}

@Composable
private fun PTab(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (active) OceanBlue600 else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            fontSize   = 12.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
            color      = if (active) Color.White else NeutralGray600
        )
    }
}

// ─── Line chart (Canvas) ─────────────────────────────────────────────────────
@Composable
private fun LineChart(data: List<ChartPoint>, lineColor: Color, modifier: Modifier = Modifier) {
    if (data.size < 2) return

    // Re-key canvas on data change so it recomposes
    key(data) {
        Canvas(modifier = modifier) {
            val w      = size.width
            val h      = size.height
            val maxVal = data.maxOf { it.value }.coerceAtLeast(1.0)

            val pts = data.mapIndexed { i, pt ->
                Offset(
                    x = i * (w / (data.size - 1).toFloat()),
                    y = h - (pt.value / maxVal * h * 0.9f).toFloat()   // 0.9 leaves top breathing room
                )
            }

            // Grid lines (3 horizontal dashed)
            repeat(3) { i ->
                val y = h * (1 - (i + 1) / 4f)
                drawLine(
                    color       = Color.Gray.copy(alpha = 0.13f),
                    start       = Offset(0f, y),
                    end         = Offset(w, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect  = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
                )
            }

            // Area fill
            val fillPath = Path().apply {
                moveTo(pts.first().x, h)
                lineTo(pts.first().x, pts.first().y)
                for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
                lineTo(pts.last().x, h)
                close()
            }
            drawPath(
                fillPath,
                Brush.verticalGradient(
                    colors     = listOf(lineColor.copy(alpha = 0.28f), Color.Transparent),
                    startY     = 0f,
                    endY       = h
                )
            )

            // Line
            val linePath = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
            }
            drawPath(
                linePath,
                color = lineColor,
                style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Dots
            pts.forEach { pt ->
                drawCircle(lineColor,    radius = 3.5.dp.toPx(), center = pt)
                drawCircle(Color.White,  radius = 2.0.dp.toPx(), center = pt)
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────
private fun formatAmt(v: Double) = when {
    v >= 1_000_000 -> "${String.format("%.1f", v / 1_000_000)}tr đ"
    v >= 1_000     -> "${String.format("%,.0f", v / 1_000)}k đ"
    else           -> "${String.format("%,.0f", v)} đ"
}
