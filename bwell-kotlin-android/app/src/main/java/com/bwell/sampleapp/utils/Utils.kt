package com.bwell.sampleapp.utils

import android.content.Context
import android.os.Build
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

fun hideKeyboard(context : Context,windowToken : IBinder){
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}
fun formatDate(dateString: String): String {
    try {
        val instant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.parse(dateString)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val localDateTime = LocalDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId())
        val outputFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.getDefault())
        return localDateTime.format(outputFormat)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}
