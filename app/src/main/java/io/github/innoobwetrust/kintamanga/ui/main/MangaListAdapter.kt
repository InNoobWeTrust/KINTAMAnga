package io.github.innoobwetrust.kintamanga.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.innoobwetrust.kintamanga.databinding.HolderElementCardGridBinding
import io.github.innoobwetrust.kintamanga.databinding.HolderElementCardListBinding
import io.github.innoobwetrust.kintamanga.source.model.CatalogPages
import io.github.innoobwetrust.kintamanga.ui.model.ElementInfo

class MangaListAdapter(
        catalogPages: CatalogPages,
        private val listType: MangaListTypes,
        private var elementInfoInteractionListener: ElementInfoInteractionListener?
) : RecyclerView.Adapter<MangaListAdapter.ViewHolder>() {
    private var elementInfos: ArrayList<ElementInfo>? = catalogPages.elementInfos

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
        return when (listType) {
            MangaListTypes.LIST -> {
                val bindingList: HolderElementCardListBinding =
                        HolderElementCardListBinding.inflate(layoutInflater, parent, false)
                ViewHolder(bindingList = bindingList)
            }
            else -> {
                val bindingGrid: HolderElementCardGridBinding =
                        HolderElementCardGridBinding.inflate(layoutInflater, parent, false)
                ViewHolder(bindingGrid = bindingGrid)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(elementInfos!![position])
        holder.root.setOnClickListener {
            elementInfoInteractionListener
                    ?.onMangaCardClick(holder.elementInfo!!.asMangaBinding())
        }
        holder.elementInfoTitle.isSelected = true
        if (position >= elementInfos!!.size - 6) {
            elementInfoInteractionListener?.onRequestMoreElement()
        }
    }

    override fun getItemCount(): Int {
        return elementInfos!!.size
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        elementInfos = null
        elementInfoInteractionListener = null
        System.gc()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.root.setOnClickListener(null)
        holder.bind(null)
        System.gc()
        super.onViewRecycled(holder)
    }

    inner class ViewHolder(
            val bindingList: HolderElementCardListBinding? = null,
            val bindingGrid: HolderElementCardGridBinding? = null
    ) : RecyclerView.ViewHolder(bindingList?.root ?: bindingGrid!!.root) {
        val elementInfo: ElementInfo?
            get() = bindingList?.elementInfo ?: bindingGrid!!.elementInfo
        val root: View
            get() = bindingList?.root ?: bindingGrid!!.root
        val elementInfoTitle
            get() = bindingList?.elementInfoTitle ?: bindingGrid!!.elementInfoTitle

        fun bind(elementInfo: ElementInfo?) {
            bindingList?.elementInfo = elementInfo
            bindingList?.executePendingBindings()
            bindingGrid?.elementInfo = elementInfo
            bindingGrid?.executePendingBindings()
        }

        override fun toString(): String {
            return super.toString() + (bindingList?.elementInfo?.itemTitle ?:
                    bindingGrid!!.elementInfo?.itemTitle)
        }
    }
}
