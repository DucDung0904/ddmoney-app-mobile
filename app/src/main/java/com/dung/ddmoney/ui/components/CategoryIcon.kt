package com.dung.ddmoney.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CarRepair
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.HomeRepairService
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun CategoryIcon(
    icon: String,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    fallbackFontSize: TextUnit = 18.sp
) {
    val imageVector = categoryIconVector(icon)
    if (imageVector != null) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = modifier,
            tint = if (tint == Color.Unspecified) Color.Unspecified else tint
        )
    } else {
        Text(
            text = icon,
            modifier = modifier,
            fontSize = fallbackFontSize,
            textAlign = TextAlign.Center
        )
    }
}

fun categoryIconVector(icon: String): ImageVector? =
    when (icon.trim().lowercase()) {
        "category" -> Icons.Outlined.Category
        "restaurant" -> Icons.Outlined.Restaurant
        "breakfast" -> Icons.Outlined.Fastfood
        "coffee" -> Icons.Outlined.Coffee
        "local_dining" -> Icons.Outlined.LocalDining
        "directions_car" -> Icons.Outlined.DirectionsCar
        "local_gas_station" -> Icons.Outlined.LocalGasStation
        "local_taxi" -> Icons.Outlined.LocalTaxi
        "car_repair" -> Icons.Outlined.CarRepair
        "shopping_bag" -> Icons.Outlined.ShoppingBag
        "chair" -> Icons.Outlined.Chair
        "checkroom" -> Icons.Outlined.Checkroom
        "receipt_long" -> Icons.AutoMirrored.Outlined.ReceiptLong
        "electric_bolt" -> Icons.Outlined.ElectricBolt
        "water_drop" -> Icons.Outlined.WaterDrop
        "wifi" -> Icons.Outlined.Wifi
        "phone_android" -> Icons.Outlined.PhoneAndroid
        "house" -> Icons.Outlined.House
        "home_work" -> Icons.Outlined.HomeWork
        "home_repair_service" -> Icons.Outlined.HomeRepairService
        "construction" -> Icons.Outlined.Construction
        "movie" -> Icons.Outlined.Movie
        "flight_takeoff" -> Icons.Outlined.FlightTakeoff
        "sports_esports" -> Icons.Outlined.SportsEsports
        "local_hospital" -> Icons.Outlined.LocalHospital
        "medication" -> Icons.Outlined.Medication
        "school" -> Icons.Outlined.School
        "menu_book" -> Icons.AutoMirrored.Outlined.MenuBook
        "payments" -> Icons.Outlined.Payments
        "celebration" -> Icons.Outlined.Celebration
        "work" -> Icons.Outlined.Work
        "card_giftcard" -> Icons.Outlined.CardGiftcard
        "trending_up" -> Icons.AutoMirrored.Outlined.TrendingUp
        else -> null
    }
