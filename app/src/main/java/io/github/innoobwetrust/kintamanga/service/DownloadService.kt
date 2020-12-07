package io.github.innoobwetrust.kintamanga.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo.State.CONNECTED
import android.net.NetworkInfo.State.DISCONNECTED
import android.os.IBinder
import android.os.PowerManager
import android.widget.Toast
import com.github.pwittchen.reactivenetwork.library.Connectivity
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.R
import io.github.innoobwetrust.kintamanga.download.Downloader
import io.github.innoobwetrust.kintamanga.util.extension.powerManager
import io.github.innoobwetrust.kintamanga.util.extension.toast
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subscriptions.CompositeSubscription
import timber.log.Timber

/**
 * This service is used to manage the downloader. The system can decide to stop the service, in
 * which case the downloader is also stopped. It's also stopped while there's no network available.
 * While the downloader is running, a wake lock will be held.
 */
class DownloadService : Service(), KodeinGlobalAware {

    companion object {

        /**
         * Subject used to know when the service is running.
         */
        val runningSubject: BehaviorSubject<Boolean> = BehaviorSubject.create(false)

        /**
         * Starts this service.
         *
         * @param context the application context.
         */
        fun start(context: Context) {
            context.startService(Intent(context, DownloadService::class.java))
        }

        /**
         * Stops this service.
         *
         * @param context the application context.
         */
        fun stop(context: Context) {
            context.stopService(Intent(context, DownloadService::class.java))
        }
    }

    /**
     * Downloader
     */
    private val downloader: Downloader = instance()

    /**
     * Wake lock to prevent the device to enter sleep mode.
     */
    private val wakeLock by lazy {
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DownloadService:WakeLock")
    }

    /**
     * Subscriptions to store while the service is running.
     */
    private lateinit var subscriptions: CompositeSubscription

    /**
     * Called when the service is created.
     */
    override fun onCreate() {
        super.onCreate()
        runningSubject.onNext(true)
        subscriptions = CompositeSubscription()
        listenDownloaderState()
        listenNetworkChanges()
    }

    /**
     * Called when the service is destroyed.
     */
    override fun onDestroy() {
        runningSubject.onNext(false)
        subscriptions.unsubscribe()
        downloader.stop()
        wakeLock.releaseIfNeeded()
        super.onDestroy()
    }

    /**
     * Not used.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    /**
     * Not used.
     */
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * Listens to network changes.
     *
     * @see onNetworkStateChanged
     */
    private fun listenNetworkChanges() {
        subscriptions.add(ReactiveNetwork.observeNetworkConnectivity(applicationContext)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state ->
                    onNetworkStateChanged(state)
                }, { error ->
                    toast(R.string.download_service_error, Toast.LENGTH_LONG)
                    Timber.e(error)
                    stopSelf()
                })
        )
    }

    /**
     * Called when the network state changes.
     *
     * @param connectivity the new network state.
     */
    private fun onNetworkStateChanged(connectivity: Connectivity) {
        when (connectivity.state) {
            CONNECTED -> {
                val started = downloader.start()
                if (!started) stopSelf()
            }
            DISCONNECTED -> {
                downloader.stop(getString(R.string.download_notifier_no_network))
            }
            else -> { /* Do nothing */
            }
        }
    }

    /**
     * Listens to downloader status. Enables or disables the wake lock depending on the status.
     */
    private fun listenDownloaderState() {
        subscriptions.add(downloader.runningSubject.subscribe { running ->
            if (running)
                wakeLock.acquireIfNeeded()
            else
                wakeLock.releaseIfNeeded()
        })
    }

    /**
     * Releases the wake lock if it's held.
     */
    private fun PowerManager.WakeLock.releaseIfNeeded() {
        if (isHeld) {
            stopForeground(true)
            release()
        }
    }

    /**
     * Acquires the wake lock if it's not held.
     */
    private fun PowerManager.WakeLock.acquireIfNeeded() {
        if (!isHeld) acquire(1800000)
    }
}
