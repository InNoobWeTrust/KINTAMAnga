package io.github.innoobwetrust.kintamanga.ui.filter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import io.github.innoobwetrust.kintamanga.R
import kotlinx.android.synthetic.main.holder_filter_user_input.view.*

class FilterUserInputAdapter(
        private val userInput: MutableMap<String, String>,
        private val filterKeyLabel: Map<String, String>,
        private val filterByUserInput: List<String>,
        private val filterRequiredDefaultUserInput: Map<String, String>
) : RecyclerView.Adapter<FilterUserInputAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.holder_filter_user_input, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val key = filterByUserInput[position]
        holder.bind(
                key = key,
                label = filterKeyLabel[key] ?: key,
                inputValue = userInput[key]
        )
        holder.userInputInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userInput[key] = s.toString()
            }
        })
    }

    override fun getItemCount(): Int {
        return filterByUserInput.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var userInputLabel: AppCompatTextView = view.userInputLabel
        var userInputInput: AppCompatEditText = view.userInputInput
        lateinit var key: String

        fun bind(key: String, label: String, inputValue: String?) {
            this.key = key
            userInputLabel.text = label
            val defaultInputValue = inputValue ?: filterRequiredDefaultUserInput[label]
            if (null != defaultInputValue) userInputInput.text =
                    Editable.Factory.getInstance().newEditable(defaultInputValue)
        }

        fun reset() {
            val defaultInputValue = filterRequiredDefaultUserInput[key]
            if (null != defaultInputValue) userInputInput.text =
                    Editable.Factory.getInstance().newEditable(defaultInputValue)
            else userInputInput.text = Editable.Factory.getInstance().newEditable("")
        }

        override fun toString(): String {
            return super.toString() + userInputLabel.text
        }
    }
}
