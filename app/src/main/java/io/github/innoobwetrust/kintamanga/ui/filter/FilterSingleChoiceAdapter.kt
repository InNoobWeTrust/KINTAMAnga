package io.github.innoobwetrust.kintamanga.ui.filter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.innoobwetrust.kintamanga.R
import io.github.innoobwetrust.kintamanga.databinding.HolderFilterSingleChoiceBinding

class FilterSingleChoiceAdapter(
        private val singleChoice: MutableMap<String, String>,
        private val filterKeyLabel: Map<String, String>,
        filterBySingleChoice: Map<String, Map<String, String>>,
        private val filterRequiredDefaultSingleChoice: Map<String, String>
) : RecyclerView.Adapter<FilterSingleChoiceAdapter.ViewHolder>() {
    private val filterList: List<Pair<String, Map<String, String>>> = filterBySingleChoice.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = HolderFilterSingleChoiceBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val key = filterList[position].first
        holder.bind(
                key = key,
                label = filterKeyLabel[key] ?: key,
                dataMap = filterList[position].second,
                defaultMap = filterRequiredDefaultSingleChoice
        )
        holder.binding.singleChoiceOption.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    itemPosition: Int,
                    id: Long
            ) {
                singleChoice[key] = holder.dataList[itemPosition].second
            }
        }
        val defaultIndex = holder.dataList.map { it.second }.run {
            when (singleChoice[key]) {
                null -> indexOf(filterRequiredDefaultSingleChoice[key])
                else -> indexOf(singleChoice[key])
            }
        }
        if (defaultIndex >= 0) holder.binding.singleChoiceOption.setSelection(defaultIndex)
    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    inner class ViewHolder(val binding: HolderFilterSingleChoiceBinding) : RecyclerView.ViewHolder(binding.root) {
        // List pair of label-value
        lateinit var dataList: List<Pair<String, String>>
        private lateinit var defaultMap: Map<String, String>
        lateinit var key: String

        fun bind(
                key: String,
                label: String,
                dataMap: Map<String, String>,
                defaultMap: Map<String, String>
        ) {
            this.key = key
            binding.singleChoiceLabel.text = label
            dataList = dataMap.toList()
            val singleChoiceDataAdapter: ArrayAdapter<String> = ArrayAdapter(
                    binding.root.context,
                    R.layout.themed_spinner_item,
                    dataList.map { it.first }
            ).also { it.setDropDownViewResource(R.layout.themed_spinner_dropdown_item) }
            binding.singleChoiceOption.adapter = singleChoiceDataAdapter
            this.defaultMap = defaultMap
        }

        fun reset() {
            val defaultIndex = dataList
                    .map { it.second }
                    .indexOf(defaultMap[key])
            if (defaultIndex >= 0) binding.singleChoiceOption.setSelection(defaultIndex)
        }

        override fun toString(): String {
            return super.toString() + binding.singleChoiceLabel.text
        }
    }
}
