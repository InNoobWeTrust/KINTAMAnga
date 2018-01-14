package io.github.innoobwetrust.kintamanga

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.conf.global
import timber.log.Timber

class KINTAMAngaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupKodein()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }

    // MultiDEX support
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun setupKodein() {
        Kodein.global.apply {
            addImport(appModule(this@KINTAMAngaApp))
            addImport(serviceModule(this@KINTAMAngaApp))
            addImport(preferencesModule(this@KINTAMAngaApp))
            addImport(okHttpModule)
        }
    }
}
