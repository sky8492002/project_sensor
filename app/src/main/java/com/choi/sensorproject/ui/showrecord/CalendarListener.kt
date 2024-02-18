package com.choi.sensorproject.ui.showrecord

import java.util.Date

interface CalendarListener {
    fun onSelectedDateUpdate(selectedDate: Date)
    fun onShowingYearMonthUpdate(showingYearMonth: String)
}