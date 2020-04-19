package io.github.innoobwetrust.kintamanga.ui.downloader

import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.R
import io.github.innoobwetrust.kintamanga.databinding.HolderDownloadBinding
import io.github.innoobwetrust.kintamanga.download.Downloader
import io.github.innoobwetrust.kintamanga.model.Download
import io.github.innoobwetrust.kintamanga.model.DownloadStatus
import kotlinx.android.synthetic.main.holder_download.view.*

class DownloadAdapter(
        private var downloaderActivity: DownloaderActivity?
) : RecyclerView.Adapter<DownloadAdapter.ViewHolder>(), KodeinGlobalAware {
    private var downloads: List<Download>? = instance<Downloader>().queue
    private var contextWrapper: ContextThemeWrapper? =
            ContextThemeWrapper(downloaderActivity, R.style.popupMenuStyle)

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return downloads?.getOrNull(position)?.chapter?.id ?: -1L
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: HolderDownloadBinding =
                HolderDownloadBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val download = downloads?.getOrNull(position)
        holder.bind(download)
        download?.let {
            holder.mangaTitle.isSelected = true
            holder.downloadStatus.isSelected = true
            holder.chapterTitle.isSelected = true
            holder.downloadProgressText.isSelected = true
            holder.downloadOption.setOnClickListener {
                val downloadItemOption =
                        PopupMenu(contextWrapper!!, holder.downloadOption)
                downloadItemOption.inflate(R.menu.menu_download_item)
                downloadItemOption.menu.apply {
                    // Set start button visibility.
                    findItem(R.id.download_item_option_resume)?.isVisible =
                            download.downloadStatus == DownloadStatus.STOPPED
                    // Set pause button visibility.
                    findItem(R.id.download_item_option_stop)?.isVisible =
                            download.downloadStatus in listOf(DownloadStatus.DOWNLOADING, DownloadStatus.QUEUE)
                }
                downloadItemOption.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.download_item_option_resume -> {
                            if (downloads?.getOrNull(holder.adapterPosition)
                                    ?.let(instance<Downloader>()::resume) == true) {
                                notifyItemChanged(holder.adapterPosition)
                                return@setOnMenuItemClickListener true
                            }
                        }
                        R.id.download_item_option_stop -> {
                            if (downloads?.getOrNull(holder.adapterPosition)
                                    ?.let(instance<Downloader>()::stop) == true) {
                                notifyItemChanged(holder.adapterPosition)
                                return@setOnMenuItemClickListener true
                            }
                        }
                        R.id.download_item_option_remove -> {
                            if (downloads?.getOrNull(holder.adapterPosition)
                                    ?.let(instance<Downloader>()::remove) == true) {
                                notifyItemRemoved(holder.adapterPosition)
                                return@setOnMenuItemClickListener true
                            }
                        }
                    }
                    return@setOnMenuItemClickListener false
                }
                downloadItemOption.show()
            }
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.downloadOption.setOnClickListener(null)
        holder.bind(null)
        System.gc()
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int {
        return downloads?.size ?: 0
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        contextWrapper = null
        downloaderActivity = null
        downloads = null
        System.gc()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    inner class ViewHolder(
            val binding: HolderDownloadBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val root: View
            get() = binding.root
        val mangaTitle: AppCompatTextView
            get() = binding.root.mangaTitle
        val downloadStatus: AppCompatTextView
            get() = binding.root.downloadStatus
        val chapterTitle: AppCompatTextView
            get() = binding.root.chapterTitle
        val downloadProgressText: AppCompatTextView
            get() = binding.root.downloadProgressText
        val downloadOption: AppCompatImageButton
            get() = binding.root.downloadOption

        fun bind(download: Download?) {
            binding.download = download
            binding.executePendingBindings()
        }
    }
}
