package com.todays.learning.ui.screens.calender

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.intl.Locale
import com.kizitonwose.calendar.core.daysOfWeek
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
actual fun Month.getDisplayName(short: Boolean, locale: Locale): String {
    val cal: Calendar = Calendar.getInstance(locale.platformLocale)
    val monthDate = SimpleDateFormat("MMMM", locale.platformLocale)
    cal[Calendar.MONTH] = Month.entries.indexOf(this)
    return monthDate.format(cal.time)
}

@RequiresApi(Build.VERSION_CODES.O)
actual fun DayOfWeek.getDisplayName(narrow: Boolean, locale: Locale): String {
    val values = if (narrow) {
        DateFormatSymbols(locale.platformLocale).shortWeekdays
    } else {
        DateFormatSymbols(locale.platformLocale).shortWeekdays
    }
    val week = values.toCollection(ArrayList())
    week.removeAt(0)
    return week[sundayBasedWeek.indexOf(this)].toString()
}

@RequiresApi(Build.VERSION_CODES.O)
private val sundayBasedWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY)