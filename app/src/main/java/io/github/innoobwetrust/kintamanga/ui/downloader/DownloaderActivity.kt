package io.github.innoobwetrust.kintamanga.ui.downloader

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.R
import io.github.innoobwetrust.kintamanga.download.Downloader
import io.github.innoobwetrust.kintamanga.service.DownloadService
import kotlinx.android.synthetic.main.activity_downloader.*
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription

class DownloaderActivity : AppCompatActivity(), KodeinGlobalAware {

    /**
     * Subscription list to be cleared during [onDestroy].
     */
    private val subscriptions by lazy { CompositeSubscription() }
    /**
     * Whether the download queue is running or not.
     */
    private var isRunning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloader)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        downloadList?.apply {
            layoutManager = LinearLayoutManager(this@DownloaderActivity)
            adapter = DownloadAdapter(this@DownloaderActivity)
        }
        // Subscribe to changes
        subscriptions.add(instance<Downloader>().runningSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { onQueueStatusChange(it) }
        )
        subscriptions.add(instance<Downloader>().queue.getUpdatedObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { downloadList?.adapter?.notifyDataSetChanged() }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_downloader, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        // Set start button visibility.
        menu?.findItem(R.id.downloader_start)?.isVisible =
                !isRunning && instance<Downloader>().queue.isNotEmpty()
        // Set pause button visibility.
        menu?.findItem(R.id.downloader_pause)?.isVisible = isRunning
        // Set stop button visibility.
        menu?.findItem(R.id.downloader_stop)?.isVisible = isRunning
        // Set clear button visibility.
        menu?.findItem(R.id.downloader_remove_all)?.isVisible =
                instance<Downloader>().queue.isNotEmpty()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (null == item) return false
        when (item.itemId) {
            R.id.downloader_start -> {
                DownloadService.start(this)
                instance<Downloader>().let {
                    if (it.notifier.paused) it.resume()
                }
            }
            R.id.downloader_pause -> instance<Downloader>().pause()
            R.id.downloader_stop -> DownloadService.stop(this)
            R.id.downloader_remove_all -> instance<Downloader>().clearQueue()
        }
        when (item.itemId) {
            in listOf(
                    R.id.downloader_start,
                    R.id.downloader_pause,
                    R.id.downloader_stop,
                    R.id.downloader_remove_all
            ) -> return true.also {
                downloadList?.adapter?.notifyDataSetChanged()
            }
        }
        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onStop() {
        downloadList?.adapter = null
        super.onStop()
    }

    override fun onDestroy() {
        subscriptions.clear()
        super.onDestroy()
    }

    /**
     * Called when the queue's status has changed. Updates the visibility of the buttons.
     *
     * @param running whether the queue is now running or not.
     */
    private fun onQueueStatusChange(running: Boolean) {
        isRunning = running
        invalidateOptionsMenu()
        downloadList?.adapter?.notifyDataSetChanged()
    }
}
