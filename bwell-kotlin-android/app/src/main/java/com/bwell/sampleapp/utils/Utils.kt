package com.bwell.sampleapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.text.Html
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bwell.sampleapp.R
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun hideKeyboard(context : Context,windowToken : IBinder){
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun removeHtmlTags(htmlString: String): String {
    val cleanedString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(htmlString).toString()
    }
    return cleanedString.trim()
}

fun formatDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) {
        return ""
    }
    try {
        // Try parsing as ISO-8601 format
        val instant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.parse(dateString)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.getDefault()).parse(dateString).toInstant()
            } else {
                TODO("VERSION.SDK_INT < O")
            }
        }
        val localDateTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId())
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val outputFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.getDefault())
        return localDateTime.format(outputFormat)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    //  parsing as a different format for observation effective date
    try {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
        return outputFormat.format(date)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

fun parseDateStringToDate(dateString: String, dateFormatPattern: String): Date? {
    return try {
        val dateFormat = SimpleDateFormat(dateFormatPattern)
        dateFormat.parse(dateString)
    } catch (e: Exception) {
        null
    }
}

fun loadRemoteImageIntoImageView(imageView: ImageView, imageUrl: String?) {
    // Launch a new coroutine in the background to download the image
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = java.net.URL(imageUrl)
            val connection = url.openConnection()
            connection.connect()
            val inputStream = connection.getInputStream()

            val bitmap = BitmapFactory.decodeStream(inputStream)

            withContext(Dispatchers.Main) {
                imageView.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun getEncryptedSharedPreferences(context: Context): EncryptedSharedPreferences {
    var masterKeyAlias = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return EncryptedSharedPreferences.create(
        context,
        R.string.encrypted_shared_preferences.toString(),
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    ) as EncryptedSharedPreferences
}

fun getSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(R.string.shared_preferences.toString(), Context.MODE_PRIVATE)
}
