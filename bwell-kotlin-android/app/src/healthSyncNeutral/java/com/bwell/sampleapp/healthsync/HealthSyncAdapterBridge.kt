package com.bwell.sampleapp.healthsync

import android.content.Context
import androidx.fragment.app.Fragment

/**
 * Single call site for registering an on-device Health Sync adapter with
 * [com.bwell.healthsync.BWellHealthSync]. Exactly one implementation of
 * this file compiles at a time - this one (the committed default) or the
 * one under `src/healthSyncLocal/java/...` - chosen by a source-set swap in
 * `app/build.gradle.kts` based on whether that local, untracked directory
 * exists. This is the Gradle analogue of Swift's
 * `#if canImport(BWellHealthSyncAdapterAggregator)`.
 *
 * This is the neutral half: no adapter, no vendor dependency, nothing to
 * configure. [com.bwell.healthsync.BWellHealthSync.isConfigured] stays
 * `false`, and the Health Sync UI falls back to its vendor-neutral copy.
 *
 * See `healthsync-local.app.gradle.kts.example` at the repo root for how to
 * opt into the real adapter locally, without touching any committed file.
 */
object HealthSyncAdapterBridge {
    /**
     * Called once, early in [fragment]'s lifecycle (field initializer /
     * `onAttach`) - before the fragment reaches `CREATED`, which is the
     * only window `registerForActivityResult` can be called in. A real
     * adapter's permission flow (e.g. Health Connect) needs an
     * `ActivityResultContract` registered here; the neutral build has
     * nothing to register.
     */
    fun registerPermissionLauncher(fragment: Fragment) {
        // No-op: no on-device adapter is available in this build.
    }

    /** Registers the adapter with [com.bwell.healthsync.BWellHealthSync], if present. */
    fun configureIfAvailable(context: Context) {
        // No-op: no on-device adapter is available in this build.
    }
}
