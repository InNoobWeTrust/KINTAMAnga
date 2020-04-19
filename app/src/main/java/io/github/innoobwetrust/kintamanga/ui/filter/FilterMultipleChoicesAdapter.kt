package io.github.innoobwetrust.kintamanga.ui.filter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import io.github.innoobwetrust.kintamanga.R
import kotlinx.android.synthetic.main.holder_filter_multiple_choices.view.*

class FilterMultipleChoicesAdapter(
        private val multipleChoices: MutableSet<Pair<String, String>>,
        private val filterKeyLabel: Map<String, String>,
        filterByMultipleChoices: Map<String, Map<String, String>>
) : RecyclerView.Adapter<FilterMultipleChoicesAdapter.ViewHolder>() {
    private val filterList: List<Pair<String, Map<String, String>>> =
            filterByMultipleChoices.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(
                layoutInflater.inflate(R.layout.holder_filter_multiple_choices, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val key = filterList[position].first
        holder.bind(
                key = key,
                label = filterKeyLabel[key] ?: key,
                dataMap = filterList.find { it.first == key }!!.second,
                multipleChoices = multipleChoices
        )
    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val multipleChoicesOptionLabel: AppCompatTextView =
                view.multipleChoicesOption.multipleChoicesOptionLabel
        private val multipleChoicesOptionValues: LinearLayout =
                view.multipleChoicesOption.multipleChoicesOptionValues
        private lateinit var dataList: List<Pair<String, String>>
        private var checkboxList: MutableList<CheckBox> = mutableListOf()
        private var key: String = ""

        fun bind(
                key: String,
                label: String,
                dataMap: Map<String, String>,
                multipleChoices: MutableSet<Pair<String, String>>
        ) {
            this.key = key
            multipleChoicesOptionLabel.text = label
            dataList = dataMap.toList()
            multipleChoicesOptionValues.let { linearLayout ->
                dataList.forEach { (label, value) ->
                    val inflater = LayoutInflater.from(linearLayout.context)
                    val checkBox = inflater.inflate(
                            R.layout.themed_checkbox_item,
                            linearLayout,
                            false
                    )
                    (checkBox as? CheckBox)?.let {
                        it.text = label
                        if (multipleChoices.any { choice -> choice == key to value }) it.isChecked = true
                        it.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) multipleChoices.add(key to value)
                            else multipleChoices.remove(key to value)
                        }
                        checkboxList.add(it)
                    }
                    linearLayout.addView(checkBox)
                }
            }
        }

        fun reset() {
            checkboxList.forEach { it.isChecked = false }
            multipleChoices.clear()
        }

        override fun toString(): String {
            return super.toString() + multipleChoicesOptionLabel.text
        }
    }
}
