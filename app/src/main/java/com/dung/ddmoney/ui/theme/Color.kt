package com.dung.ddmoney.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════════════════
//  DDMoney Color System — "Ocean Blueprint"
//  Primary: Ocean Blue (#185FA5)  •  Stop 600 = brand anchor
//  Semantic: Green (income) · Red (expense) · Teal (savings) · Amber (invest)
//  Neutral: Gray #888780 for secondary text, dividers, neutral icons
// ═══════════════════════════════════════════════════════════════════════════

// ─── Ocean Blue — Primary / CTA / Header / Balance highlight ────────────────
val OceanBlue50  = Color(0xFFE6F1FB)   // chip / badge background
val OceanBlue100 = Color(0xFFB5D4F4)   // hover tint, subtle fill
val OceanBlue400 = Color(0xFF378ADD)   // icon accent, underline
val OceanBlue600 = Color(0xFF185FA5)   // ★ brand anchor — buttons, CTA, border
val OceanBlue800 = Color(0xFF0C447C)   // header text, pressed state, badge text

// ─── Income Green — always "positive" ───────────────────────────────────────
val IncomeGreen50  = Color(0xFFEDF5DB)  // chip background
val IncomeGreen100 = Color(0xFFCDE69D)  // progress bar tint
val IncomeGreen400 = Color(0xFF82BB44)  // icon, sparkline
val IncomeGreen600 = Color(0xFF639922)  // ★ anchor — income amount, badge border
val IncomeGreen800 = Color(0xFF3D5E14)  // dark badge text

// ─── Expense Red — spending / warning ───────────────────────────────────────
val ExpenseRed50  = Color(0xFFFCECEC)   // chip background
val ExpenseRed100 = Color(0xFFF5BABA)   // light fill
val ExpenseRed400 = Color(0xFFEB7070)   // icon, soft alert
val ExpenseRed600 = Color(0xFFE24B4A)   // ★ anchor — expense amount, warning
val ExpenseRed800 = Color(0xFF8E1F1F)   // dark badge text

// ─── Savings Teal — calm, wealth-building ───────────────────────────────────
val SavingsTeal50  = Color(0xFFDFF5EE)  // chip background
val SavingsTeal100 = Color(0xFFA6E4D0)  // progress bar tint
val SavingsTeal400 = Color(0xFF3DBFA0)  // icon accent
val SavingsTeal600 = Color(0xFF1D9E75)  // ★ anchor — savings label, badge border
val SavingsTeal800 = Color(0xFF0F5C43)  // dark badge text

// ─── Investment Amber — standout, never conflicts ────────────────────────────
val InvestAmber50  = Color(0xFFFEF4E0)  // chip background
val InvestAmber100 = Color(0xFFFCDDA0)  // light fill
val InvestAmber400 = Color(0xFFF3BC56)  // icon accent, sparkline
val InvestAmber600 = Color(0xFFEF9F27)  // ★ anchor — investment label
val InvestAmber800 = Color(0xFF8A5606)  // dark badge text

// ─── Neutral Gray — secondary text, dividers, neutral icons ─────────────────
val NeutralGray50  = Color(0xFFF2F2F0)
val NeutralGray100 = Color(0xFFDEDDD9)
val NeutralGray400 = Color(0xFFAAAB9C) // Note: adjusted slightly warmer than #888780 for 400
val NeutralGray600 = Color(0xFF888780)  // ★ anchor — secondary text, dividers, neutral icons
val NeutralGray800 = Color(0xFF3E3D38)

// ─── Surface & Background (Light Mode — Luminous Architect) ─────────────────
val LuminousBackground            = Color(0xFFF7F9FF)
val LuminousSurface               = Color(0xFFF7F9FF)
val LuminousSurfaceContainerLowest = Color(0xFFFFFFFF)
val LuminousSurfaceContainerLow   = Color(0xFFF1F3F9)
val LuminousSurfaceContainer      = Color(0xFFECEEF4)
val LuminousSurfaceContainerHigh  = Color(0xFFE6E8EE)
val LuminousSurfaceContainerHighest = Color(0xFFE0E2E8)
val LuminousSurfaceVariant        = Color(0xFFE0E2E8)
val LuminousInverseSurface        = Color(0xFF2D3135)
val LuminousOutline               = NeutralGray600      // reuse neutral anchor
val LuminousOutlineVariant        = NeutralGray100

// ─── Material Role Mapping (Light) ──────────────────────────────────────────
val LuminousPrimary               = OceanBlue600        // #185FA5
val LuminousPrimaryContainer      = OceanBlue400        // #378ADD — gradient end
val LuminousOnPrimary             = Color(0xFFFFFFFF)
val LuminousOnPrimaryContainer    = OceanBlue50         // #E6F1FB

val LuminousSecondary             = OceanBlue400
val LuminousSecondaryContainer    = OceanBlue100
val LuminousOnSecondary           = Color(0xFFFFFFFF)
val LuminousOnSecondaryContainer  = OceanBlue800

val LuminousTertiary              = SavingsTeal600      // #1D9E75
val LuminousTertiaryContainer     = SavingsTeal100
val LuminousOnTertiary            = Color(0xFFFFFFFF)
val LuminousOnTertiaryContainer   = SavingsTeal800

val LuminousError                 = ExpenseRed600       // #E24B4A
val LuminousErrorContainer        = ExpenseRed50
val LuminousOnError               = Color(0xFFFFFFFF)
val LuminousOnErrorContainer      = ExpenseRed800

val LuminousOnBackground          = Color(0xFF181C20)
val LuminousOnSurface             = Color(0xFF181C20)
val LuminousOnSurfaceVariant      = NeutralGray600      // #888780
val LuminousInverseOnSurface      = Color(0xFFEFF1F7)

// Home surface treatment: soft blue canvas + raised white frames, without patterns.
val HomeBackgroundTop             = Color(0xFFE8F3FD)
val HomeBackgroundMid             = Color(0xFFF8FBFE)
val HomeBackgroundBottom          = Color(0xFFEEF4FA)
val HomeFrameSurface              = Color(0xFFFBFEFF)
val HomeFrameBorder               = Color(0xFFE1ECF6)

// ─── Ocean Blue Nav Palette (BottomNavBar) ───────────────────────────────────
val OceanNavActive   = OceanBlue600    // active icon / label
val OceanNavPill     = OceanBlue100    // active pill background tint
val OceanNavSurface  = OceanBlue50    // nav bar background wash
val OceanNavInactive = NeutralGray600  // inactive icon / label

// ─── Category Chip / Badge tokens ───────────────────────────────────────────
// Usage: chipBg = *50, chipText = *800, chipBorder = *600
// Income
val ChipIncomeBg     = IncomeGreen50
val ChipIncomeText   = IncomeGreen800
val ChipIncomeBorder = IncomeGreen600
// Expense
val ChipExpenseBg     = ExpenseRed50
val ChipExpenseText   = ExpenseRed800
val ChipExpenseBorder = ExpenseRed600
// Savings
val ChipSavingsBg     = SavingsTeal50
val ChipSavingsText   = SavingsTeal800
val ChipSavingsBorder = SavingsTeal600
// Investment
val ChipInvestBg     = InvestAmber50
val ChipInvestText   = InvestAmber800
val ChipInvestBorder = InvestAmber600

// ─── Category icon/progress colors (convenience aliases) ────────────────────
val CategoryFood          = ExpenseRed600
val CategoryTransport     = OceanBlue600
val CategoryShopping      = InvestAmber600
val CategoryHealth        = IncomeGreen600
val CategoryEntertainment = InvestAmber400
val CategorySavings       = SavingsTeal600
val CategoryOther         = NeutralGray600

// ─── Dark Theme ─────────────────────────────────────────────────────────────
val DarkBackground        = Color(0xFF111820)   // deep navy-black
val DarkSurface           = Color(0xFF192130)   // card surface
val DarkSurfaceVariant    = Color(0xFF1F2C40)   // inner card / chip
val DarkOnBackground      = Color(0xFFEDF2FF)
val DarkOnSurface         = Color(0xFFEDF2FF)
val DarkOnSurfaceVariant  = Color(0xFFB0BFDB)

val DarkPrimary           = OceanBlue400        // #378ADD — readable on dark
val DarkOnPrimary         = Color(0xFFFFFFFF)
val DarkPrimaryContainer  = OceanBlue800        // #0C447C
val DarkOnPrimaryContainer = OceanBlue50        // #E6F1FB

val DarkSecondary         = SavingsTeal400
val DarkOnSecondary       = Color(0xFF001A12)
val DarkSecondaryContainer = SavingsTeal800
val DarkOnSecondaryContainer = SavingsTeal50

val DarkTertiary          = InvestAmber400
val DarkOnTertiary        = Color(0xFF1A0E00)
val DarkTertiaryContainer = InvestAmber800
val DarkOnTertiaryContainer = InvestAmber50

val DarkError             = ExpenseRed400       // softer on dark
val DarkOnError           = Color(0xFFFFFFFF)
val DarkErrorContainer    = ExpenseRed800
val DarkOnErrorContainer  = ExpenseRed50

val DarkOutline           = Color(0xFF3D5275)
val DarkOutlineVariant    = Color(0xFF2A3D5C)

// Dark semantic convenience
val DarkIncomeGreen = IncomeGreen400   // #82BB44
val DarkExpenseRed  = ExpenseRed400    // #EB7070
val DarkSavingsTeal = SavingsTeal400   // #3DBFA0
val DarkInvestAmber = InvestAmber400   // #F3BC56
