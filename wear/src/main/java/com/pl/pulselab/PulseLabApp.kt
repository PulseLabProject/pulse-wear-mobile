package com.pl.pulselab

import android.app.Application
import com.clerk.api.Clerk

class PulseLabApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Clerk.initialize(this, BuildConfig.CLERK_PUBLISHABLE_KEY)
    }
}
