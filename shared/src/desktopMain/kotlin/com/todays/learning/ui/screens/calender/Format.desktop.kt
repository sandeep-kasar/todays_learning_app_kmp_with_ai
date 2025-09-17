package com.todays.learning.ui.screens.calender

import androidx.compose.ui.text.intl.Locale
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month

actual fun Month.getDisplayName(short: Boolean, locale: Locale): String {

    return "March"
}

actual fun DayOfWeek.getDisplayName(narrow: Boolean, locale: Locale): String {

    return "Sun"
}

private val sundayBasedWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY)