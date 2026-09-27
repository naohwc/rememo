package com.noteremind.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

data class IconEntry(val key: String, val image: ImageVector)

/** 内置 40 个简约矢量小图标 */
object IconSet {

    val all: List<IconEntry> = listOf(
        IconEntry("star", Icons.Filled.Star),
        IconEntry("heart", Icons.Filled.Favorite),
        IconEntry("heart_border", Icons.Filled.FavoriteBorder),
        IconEntry("cake", Icons.Filled.Cake),
        IconEntry("celebration", Icons.Filled.Celebration),
        IconEntry("alarm", Icons.Filled.Alarm),
        IconEntry("bell", Icons.Filled.Notifications),
        IconEntry("home", Icons.Filled.Home),
        IconEntry("work", Icons.Filled.Work),
        IconEntry("school", Icons.Filled.School),
        IconEntry("book", Icons.Filled.Book),
        IconEntry("menu_book", Icons.Filled.MenuBook),
        IconEntry("flight", Icons.Filled.Flight),
        IconEntry("car", Icons.Filled.DirectionsCar),
        IconEntry("bike", Icons.Filled.DirectionsBike),
        IconEntry("train", Icons.Filled.Train),
        IconEntry("restaurant", Icons.Filled.Restaurant),
        IconEntry("cafe", Icons.Filled.LocalCafe),
        IconEntry("cart", Icons.Filled.ShoppingCart),
        IconEntry("card", Icons.Filled.CreditCard),
        IconEntry("money", Icons.Filled.AttachMoney),
        IconEntry("wallet", Icons.Filled.AccountBalanceWallet),
        IconEntry("hospital", Icons.Filled.LocalHospital),
        IconEntry("medical", Icons.Filled.MedicalServices),
        IconEntry("fitness", Icons.Filled.FitnessCenter),
        IconEntry("basketball", Icons.Filled.SportsBasketball),
        IconEntry("music", Icons.Filled.MusicNote),
        IconEntry("headphones", Icons.Filled.Headphones),
        IconEntry("movie", Icons.Filled.Movie),
        IconEntry("camera", Icons.Filled.CameraAlt),
        IconEntry("photo", Icons.Filled.Photo),
        IconEntry("brush", Icons.Filled.Brush),
        IconEntry("pets", Icons.Filled.Pets),
        IconEntry("park", Icons.Filled.Park),
        IconEntry("beach", Icons.Filled.BeachAccess),
        IconEntry("sunny", Icons.Filled.WbSunny),
        IconEntry("night", Icons.Filled.NightsStay),
        IconEntry("cloud", Icons.Filled.Cloud),
        IconEntry("water", Icons.Filled.WaterDrop),
        IconEntry("trophy", Icons.Filled.EmojiEvents)
    )

    fun byKey(key: String?): ImageVector =
        all.firstOrNull { it.key == key }?.image ?: Icons.Filled.Star
}
