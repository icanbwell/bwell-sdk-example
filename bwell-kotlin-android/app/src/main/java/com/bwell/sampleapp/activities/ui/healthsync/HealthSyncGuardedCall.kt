package com.bwell.sampleapp.activities.ui.healthsync

import android.util.Log

/**
 * Runs [block], returning [default] and logging if it throws.
 *
 * Every Health Sync SDK-backed loader/fetcher needs this: the feature is
 * reachable before login, and BaseSdk's accessors throw synchronously in
 * that state rather than returning a BWellResult error the caller could
 * branch on normally. Centralized so a future call site inherits the guard
 * instead of needing to remember to re-add it.
 */
suspend fun <T> guardedCall(tag: String, what: String, default: T, block: suspend () -> T): T =
    try {
        block()
    } catch (e: Exception) {
        Log.w(tag, "$what failed", e)
        default
    }
