package com.todays.learning.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarViewMonth
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material.icons.rounded.Today
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import todayslearning.shared.generated.resources.Res
import todayslearning.shared.generated.resources.title_calender
import todayslearning.shared.generated.resources.title_details
import todayslearning.shared.generated.resources.title_home
import todayslearning.shared.generated.resources.title_me

sealed class NavigationItem(
    val route: String,
    val title: StringResource,
    val icon: ImageVector?
) {

    data object Home : NavigationItem("/home", Res.string.title_home, Icons.Rounded.Today)
    data object Calender : NavigationItem("/calender", Res.string.title_calender, Icons.Rounded.CalendarViewMonth)
    data object Me : NavigationItem("/me", Res.string.title_me, Icons.Rounded.ManageAccounts)
    data object Details : NavigationItem("/details/{subject}", Res.string.title_details, null)
}
