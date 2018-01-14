package io.github.innoobwetrust.kintamanga.ui.filter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import io.github.innoobwetrust.kintamanga.R
import kotlinx.android.synthetic.main.holder_filter_single_choice.view.*

class FilterSingleChoiceAdapter(
        private val singleChoice: MutableMap<String, String>,
        private val filterKeyLabel: Map<String, String>,
        filterBySingleChoice: Map<String, Map<String, String>>,
        private val filterRequiredDefaultSingleChoice: Map<String, String>
) : RecyclerView.Adapter<FilterSingleChoiceAdapter.ViewHolder>() {
    private val filterList: List<Pair<String, Map<String, String>>> = filterBySingleChoice.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.holder_filter_single_choice, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val key = filterList[position].first
        holder.bind(
                key = key,
                label = filterKeyLabel[key] ?: key,
                dataMap = filterList[position].second,
                defaultMap = filterRequiredDefaultSingleChoice
        )
        holder.singleChoiceOption.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        if (defaultIndex >= 0) holder.singleChoiceOption.setSelection(defaultIndex)
    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    inner class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        private val singleChoiceLabel: AppCompatTextView
            get() = view.singleChoiceLabel
        val singleChoiceOption: AppCompatSpinner
            get() = view.singleChoiceOption
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
            singleChoiceLabel.text = label
            dataList = dataMap.toList()
            val singleChoiceDataAdapter: ArrayAdapter<String> = ArrayAdapter(
                    view.context,
                    R.layout.themed_spinner_item,
                    dataList.map { it.first }
            ).also { it.setDropDownViewResource(R.layout.themed_spinner_dropdown_item) }
            singleChoiceOption.adapter = singleChoiceDataAdapter
            this.defaultMap = defaultMap
        }

        fun reset() {
            val defaultIndex = dataList
                    .map { it.second }
                    .indexOf(defaultMap[key])
            if (defaultIndex >= 0) singleChoiceOption.setSelection(defaultIndex)
        }

        override fun toString(): String {
            return super.toString() + singleChoiceLabel.text
        }
    }
}
