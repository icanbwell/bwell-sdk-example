package com.bwell.sampleapp.utils

import android.content.Context
import android.os.IBinder
import android.view.inputmethod.InputMethodManager

fun hideKeyboard(context : Context,windowToken : IBinder){
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}