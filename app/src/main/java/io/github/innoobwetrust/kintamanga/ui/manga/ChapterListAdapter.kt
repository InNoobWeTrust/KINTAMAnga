package io.github.innoobwetrust.kintamanga.ui.manga

import android.graphics.drawable.ColorDrawable
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.dragselectrecyclerview.IDragSelectAdapter
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import io.github.innoobwetrust.kintamanga.R
import io.github.innoobwetrust.kintamanga.databinding.HolderChapterBinding
import io.github.innoobwetrust.kintamanga.model.DownloadStatus
import io.github.innoobwetrust.kintamanga.ui.model.ChapterBinding
import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding
import kotlinx.android.synthetic.main.holder_chapter.view.*

class ChapterListAdapter(
        private var mangaInfoActivity: MangaInfoActivity?,
        private var mangaBinding: MangaBinding?,
        val selectedIndices: MutableSet<Int>
) : RecyclerView.Adapter<ChapterListAdapter.ViewHolder>(),
        IDragSelectAdapter,
        KodeinGlobalAware {
    private var contextWrapper: ContextThemeWrapper? =
            ContextThemeWrapper(mangaInfoActivity, R.style.popupMenuStyle)

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: HolderChapterBinding =
                HolderChapterBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mangaBinding?.apply {
            val chapterBinding = chapters[chapters.size - position - 1]
            holder.bind(chapterBinding)
            holder.root.chapterInfoLayout.setOnClickListener {
                holder.binding.chapterBinding?.let {
                    mangaInfoActivity?.onChapterClick(chapterBinding = it)
                }
            }
            holder.root.chapterInfoLayout.setOnLongClickListener {
                mangaInfoActivity?.onLongClick(holder.adapterPosition)
                true
            }
            holder.root.let {
                it.chapterTitle.isSelected = true
                it.chapterDescription.isSelected = true
                it.chapterUpdateTime.isSelected = true
            }
            holder.chapterOfflinePin.setOnClickListener {
                toggleSelected(holder.adapterPosition)
            }
            holder.chapterOfflinePin.setOnLongClickListener {
                mangaInfoActivity?.onLongClick(holder.adapterPosition)
                true
            }
            holder.chapterOption.setOnClickListener {
                val chapterItemOption =
                        PopupMenu(contextWrapper!!, holder.chapterOption)
                chapterItemOption.inflate(R.menu.menu_chapter_item)
                chapterItemOption.menu.apply {
                    findItem(R.id.chapter_item_option_download)?.isVisible =
                            chapterBinding.chapterDownloadStatus == DownloadStatus.NOT_DOWNLOADED
                    findItem(R.id.chapter_item_option_delete)?.isVisible =
                            chapterBinding.chapterDownloadStatus == DownloadStatus.DOWNLOADED
                }
                chapterItemOption.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.chapter_item_option_download -> {
                            if (DownloadStatus.NOT_DOWNLOADED == chapterBinding.chapterDownloadStatus) {
                                mangaInfoActivity?.onDownloadRequest(listOf(chapterBinding), false)
                                return@setOnMenuItemClickListener true
                            }
                        }
                        R.id.chapter_item_option_delete -> {
                            if (DownloadStatus.DOWNLOADED == chapterBinding.chapterDownloadStatus) {
                                mangaInfoActivity?.onDeleteRequest(listOf(chapterBinding), false)
                                return@setOnMenuItemClickListener true
                            }
                        }
                        R.id.chapter_item_option_toggle_read_status -> {
                            chapterBinding.chapterViewed = !chapterBinding.chapterViewed
                            notifyItemChanged(position)
                            mangaInfoActivity?.onReadStatusToggled(listOf(chapterBinding))
                            return@setOnMenuItemClickListener true
                        }
                    }
                    return@setOnMenuItemClickListener false
                }
                chapterItemOption.show()
            }
            holder.chapterCard.foreground =
                    if (position in selectedIndices)
                        ColorDrawable(
                                ContextCompat.getColor(
                                        holder.chapterCard.context,
                                        R.color.color_overlay
                                )
                        )
                    else
                        null
        }
    }

    override fun getItemCount(): Int {
        return mangaBinding?.chapters?.size ?: 0
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        mangaBinding = null
        contextWrapper = null
        mangaInfoActivity = null
        System.gc()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.root.setOnClickListener(null)
        holder.binding.chapterOfflinePin.setOnClickListener(null)
        holder.bind(null)
        System.gc()
        super.onViewRecycled(holder)
    }

    override fun isIndexSelectable(index: Int): Boolean = true

    override fun setSelected(index: Int, selected: Boolean) {
        if (!selected) {
            selectedIndices.remove(index)
        } else {
            selectedIndices.add(index)
        }
        notifyItemChanged(index)
        mangaInfoActivity?.onSelectionChanged(selectedIndices.size)
    }

    private fun toggleSelected(index: Int) {
        if (selectedIndices.contains(index)) {
            selectedIndices.remove(index)
        } else {
            selectedIndices.add(index)
        }
        notifyItemChanged(index)
        mangaInfoActivity?.onSelectionChanged(selectedIndices.size)
    }

    fun clearSelected() {
        if (selectedIndices.isEmpty()) return
        selectedIndices.clear()
        notifyDataSetChanged()
        mangaInfoActivity?.onSelectionChanged(0)
    }

    fun selectAll() {
        selectedIndices.clear()
        selectedIndices.addAll(0 until itemCount)
        notifyDataSetChanged()
        mangaInfoActivity?.onSelectionChanged(selectedIndices.size)
    }

    fun invertSelection() {
        val newSelections = (0 until itemCount).filter { it !in selectedIndices }
        selectedIndices.clear()
        selectedIndices.addAll(newSelections)
        notifyDataSetChanged()
        mangaInfoActivity?.onSelectionChanged(selectedIndices.size)
    }

    inner class ViewHolder(
            val binding: HolderChapterBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val root: View
            get() = binding.root
        val chapterCard: CardView
            get() = binding.chapterCard
        val chapterOfflinePin: AppCompatImageView
            get() = binding.chapterOfflinePin
        val chapterOption: AppCompatImageButton
            get() = binding.chapterOption

        fun bind(chapterBinding: ChapterBinding?) {
            binding.chapterBinding = chapterBinding
            binding.executePendingBindings()
        }

        override fun toString(): String {
            return super.toString() + binding.chapterBinding?.chapterTitle
        }
    }
}
