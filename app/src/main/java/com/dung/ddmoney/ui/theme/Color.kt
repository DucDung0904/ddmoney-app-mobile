package com.dung.ddmoney.ui.theme

import androidx.compose.ui.graphics.Color

// DDMoney design tokens, adapted from the new TypeScript theme files.

// Primary blue
val Primary50 = Color(0xFFEFF6FF)
val Primary100 = Color(0xFFDBEAFE)
val Primary200 = Color(0xFFBFDBFE)
val Primary300 = Color(0xFF93C5FD)
val Primary400 = Color(0xFF60A5FA)
val Primary500 = Color(0xFF3B82F6)
val Primary600 = Color(0xFF2563EB)
val Primary700 = Color(0xFF1D4ED8)
val Primary800 = Color(0xFF1E40AF)
val Primary900 = Color(0xFF1E3A8A)

// Secondary violet
val Secondary50 = Color(0xFFF5F3FF)
val Secondary100 = Color(0xFFEDE9FE)
val Secondary500 = Color(0xFF8B5CF6)
val Secondary600 = Color(0xFF7C3AED)

// Neutral gray
val Gray50 = Color(0xFFF9FAFB)
val Gray100 = Color(0xFFF3F4F6)
val Gray200 = Color(0xFFE5E7EB)
val Gray300 = Color(0xFFD1D5DB)
val Gray400 = Color(0xFF9CA3AF)
val Gray500 = Color(0xFF6B7280)
val Gray600 = Color(0xFF4B5563)
val Gray700 = Color(0xFF374151)
val Gray800 = Color(0xFF1F2937)
val Gray900 = Color(0xFF111827)

// Semantic colors
val Success = Color(0xFF22C55E)
val Error = Color(0xFFEF4444)
val Warning = Color(0xFFF59E0B)
val Info = Color(0xFF3B82F6)

// Priority badges
val PriorityLowBg = Color(0xFFDCFCE7)
val PriorityLowText = Color(0xFF15803D)
val PriorityMediumBg = Color(0xFFFEF3C7)
val PriorityMediumText = Color(0xFFD97706)
val PriorityHighBg = Color(0xFFFEE2E2)
val PriorityHighText = Color(0xFFDC2626)
val PriorityUrgentBg = Color(0xFF7F1D1D)
val PriorityUrgentText = Color(0xFFFFFFFF)

// App-level theme roles
val AppBackground = Gray50
val AppSurface = Color(0xFFFFFFFF)
val AppCard = Color(0xFFFFFFFF)
val AppTextPrimary = Gray900
val AppTextSecondary = Gray500
val AppBorder = Gray200
val AppPrimary = Primary500
val AppSecondary = Secondary500

// Compatibility aliases for existing screens.
val OceanBlue50 = Primary50
val OceanBlue100 = Primary100
val OceanBlue400 = Primary400
val OceanBlue600 = AppPrimary
val OceanBlue800 = Primary800
val NumberPadBlue = AppPrimary

val IncomeGreen50 = PriorityLowBg
val IncomeGreen100 = Color(0xFFBBF7D0)
val IncomeGreen400 = Success
val IncomeGreen600 = Success
val IncomeGreen800 = PriorityLowText

val ExpenseRed50 = PriorityHighBg
val ExpenseRed100 = Color(0xFFFECACA)
val ExpenseRed400 = Color(0xFFF87171)
val ExpenseRed600 = Error
val ExpenseRed800 = PriorityHighText

val SavingsTeal50 = Primary50
val SavingsTeal100 = Primary100
val SavingsTeal400 = Primary400
val SavingsTeal600 = AppPrimary
val SavingsTeal800 = Primary800

val InvestAmber50 = PriorityMediumBg
val InvestAmber100 = Color(0xFFFDE68A)
val InvestAmber400 = Color(0xFFFBBF24)
val InvestAmber600 = Warning
val InvestAmber800 = PriorityMediumText

val NeutralGray50 = Gray50
val NeutralGray100 = Gray200
val NeutralGray400 = Gray400
val NeutralGray600 = Gray500
val NeutralGray800 = Gray800

val LuminousBackground = AppBackground
val LuminousSurface = AppSurface
val LuminousSurfaceContainerLowest = AppCard
val LuminousSurfaceContainerLow = Gray100
val LuminousSurfaceContainer = Gray100
val LuminousSurfaceContainerHigh = Gray200
val LuminousSurfaceContainerHighest = Gray200
val LuminousSurfaceVariant = Gray100
val LuminousInverseSurface = Gray800
val LuminousOutline = AppBorder
val LuminousOutlineVariant = Gray200

val LuminousPrimary = AppPrimary
val LuminousPrimaryContainer = Primary100
val LuminousOnPrimary = Color(0xFFFFFFFF)
val LuminousOnPrimaryContainer = Primary800

val LuminousSecondary = AppSecondary
val LuminousSecondaryContainer = Secondary100
val LuminousOnSecondary = Color(0xFFFFFFFF)
val LuminousOnSecondaryContainer = Secondary600

val LuminousTertiary = Success
val LuminousTertiaryContainer = PriorityLowBg
val LuminousOnTertiary = Color(0xFFFFFFFF)
val LuminousOnTertiaryContainer = PriorityLowText

val LuminousError = Error
val LuminousErrorContainer = PriorityHighBg
val LuminousOnError = Color(0xFFFFFFFF)
val LuminousOnErrorContainer = PriorityHighText

val LuminousOnBackground = AppTextPrimary
val LuminousOnSurface = AppTextPrimary
val LuminousOnSurfaceVariant = AppTextSecondary
val LuminousInverseOnSurface = Gray50

val HomeBackgroundTop = Gray50
val HomeBackgroundMid = Gray100
val HomeBackgroundBottom = Gray50
val HomeFrameSurface = AppCard
val HomeFrameBorder = AppBorder

val OceanNavActive = AppPrimary
val OceanNavPill = Primary100
val OceanNavSurface = AppSurface
val OceanNavInactive = AppTextSecondary

val ChipIncomeBg = PriorityLowBg
val ChipIncomeText = PriorityLowText
val ChipIncomeBorder = Success

val ChipExpenseBg = PriorityHighBg
val ChipExpenseText = PriorityHighText
val ChipExpenseBorder = Error

val ChipSavingsBg = Primary50
val ChipSavingsText = Primary800
val ChipSavingsBorder = AppPrimary

val ChipInvestBg = PriorityMediumBg
val ChipInvestText = PriorityMediumText
val ChipInvestBorder = Warning

val CategoryFood = Error
val CategoryTransport = AppPrimary
val CategoryShopping = Warning
val CategoryHealth = Success
val CategoryEntertainment = AppSecondary
val CategorySavings = AppPrimary
val CategoryOther = AppTextSecondary

val DarkBackground = Gray900
val DarkSurface = Gray800
val DarkSurfaceVariant = Gray700
val DarkOnBackground = Gray50
val DarkOnSurface = Gray50
val DarkOnSurfaceVariant = Gray300

val DarkPrimary = Primary400
val DarkOnPrimary = Gray900
val DarkPrimaryContainer = Primary800
val DarkOnPrimaryContainer = Primary50

val DarkSecondary = Secondary500
val DarkOnSecondary = Gray50
val DarkSecondaryContainer = Secondary600
val DarkOnSecondaryContainer = Secondary50

val DarkTertiary = Warning
val DarkOnTertiary = Gray900
val DarkTertiaryContainer = PriorityMediumText
val DarkOnTertiaryContainer = PriorityMediumBg

val DarkError = Error
val DarkOnError = Color(0xFFFFFFFF)
val DarkErrorContainer = PriorityHighText
val DarkOnErrorContainer = PriorityHighBg

val DarkOutline = Gray600
val DarkOutlineVariant = Gray700

val DarkIncomeGreen = Success
val DarkExpenseRed = Error
val DarkSavingsTeal = Primary400
val DarkInvestAmber = Warning
