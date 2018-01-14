package io.github.innoobwetrust.kintamanga.ui.filter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.crashlytics.android.Crashlytics
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.typedToJson
import com.google.gson.Gson
import io.github.innoobwetrust.kintamanga.R
import io.github.innoobwetrust.kintamanga.source.model.SourceSegment
import io.github.innoobwetrust.kintamanga.ui.main.list.MangaListFragment
import io.github.innoobwetrust.kintamanga.util.extension.toast
import kotlinx.android.synthetic.main.activity_filter.*
import rx.Subscription
import timber.log.Timber

class FilterActivity : AppCompatActivity(), KodeinGlobalAware, FilterNetworkLoader {
    companion object {
        const val MANGA_SEGMENT_INTENT_KEY = "MANGA_SEGMENT_INTENT_KEY"
    }

    override lateinit var mangaSegment: SourceSegment
    override var refreshDisposable: Subscription? = null

    private val gson: Gson = instance()
    private lateinit var userInput: MutableMap<String, String>
    private lateinit var singleChoice: MutableMap<String, String>
    private lateinit var multipleChoices: MutableSet<Pair<String, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        mangaSegment = (intent.getSerializableExtra(MANGA_SEGMENT_INTENT_KEY) as? SourceSegment) ?:
                throw Exception("Error retrieve manga segment for generating filter options")
        userInput = gson.fromJson(
                intent.getStringExtra(
                        MangaListFragment.Companion.Intents.USER_INPUT.key
                )
        )
        singleChoice =
                gson.fromJson(
                        intent.getStringExtra(
                                MangaListFragment.Companion.Intents.SINGLE_CHOICE.key
                        )
                )
        multipleChoices =
                gson.fromJson(
                        intent.getStringExtra(
                                MangaListFragment.Companion.Intents.MULTIPLE_CHOICES.key
                        )
                )
    }

    override fun onResume() {
        super.onResume()
        backgroundRefreshFilterData(
                onRefreshedFilterData = onRefreshedFilterData,
                onRefreshError = onRefreshError
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        disposeAllLoaderDisposables()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onPause() {
        disposeAllLoaderDisposables()
        super.onPause()
    }

    private val onRefreshedFilterData: (Boolean) -> Unit = { success ->
        progress?.visibility = View.GONE
        if (success) {
            filterButton?.setOnClickListener {
                val resultIntent = Intent()
                        .putExtra(
                                MangaListFragment.Companion.Intents.USER_INPUT.key,
                                gson.typedToJson(userInput)
                        )
                        .putExtra(
                                MangaListFragment.Companion.Intents.SINGLE_CHOICE.key,
                                gson.typedToJson(singleChoice)
                        )
                        .putExtra(
                                MangaListFragment.Companion.Intents.MULTIPLE_CHOICES.key,
                                gson.typedToJson(multipleChoices)
                        )
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            if (mangaSegment.filterByUserInput.isNotEmpty()) {
                userInputRecyclerView?.visibility = View.VISIBLE
                userInputRecyclerView?.apply {
                    layoutManager = LinearLayoutManager(this@FilterActivity)
                    adapter = FilterUserInputAdapter(
                            userInput = this@FilterActivity.userInput,
                            filterKeyLabel = mangaSegment.filterKeyLabel,
                            filterByUserInput = mangaSegment.filterByUserInput,
                            filterRequiredDefaultUserInput =
                            mangaSegment.filterRequiredDefaultUserInput
                    )
                }
                userInputRecyclerView?.isNestedScrollingEnabled = false
            }
            if (mangaSegment.filterBySingleChoice.isNotEmpty()) {
                singleChoiceRecyclerView?.visibility = View.VISIBLE
                singleChoiceRecyclerView?.apply {
                    layoutManager = LinearLayoutManager(this@FilterActivity)
                    adapter = FilterSingleChoiceAdapter(
                            singleChoice = this@FilterActivity.singleChoice,
                            filterKeyLabel = mangaSegment.filterKeyLabel,
                            filterBySingleChoice = mangaSegment.filterBySingleChoice,
                            filterRequiredDefaultSingleChoice =
                            mangaSegment.filterRequiredDefaultSingleChoice
                    )
                }
                singleChoiceRecyclerView?.isNestedScrollingEnabled = false
            }
            if (mangaSegment.filterByMultipleChoices.isNotEmpty()) {
                multipleChoicesRecyclerView?.visibility = View.VISIBLE
                multipleChoicesRecyclerView?.apply {
                    layoutManager = LinearLayoutManager(this@FilterActivity)
                    adapter = FilterMultipleChoicesAdapter(
                            multipleChoices = multipleChoices,
                            filterKeyLabel = mangaSegment.filterKeyLabel,
                            filterByMultipleChoices = mangaSegment.filterByMultipleChoices
                    )
                }
                multipleChoicesRecyclerView?.isNestedScrollingEnabled = false
            }
        } else {
            filterButton?.visibility = View.GONE
            toast(R.string.filter_activity_load_filter_error_text)
        }
        resetButton?.setOnClickListener {
            userInputRecyclerView?.apply {
                mangaSegment.filterByUserInput.forEachIndexed { index, _ ->
                    (findViewHolderForAdapterPosition(index)
                            as? FilterUserInputAdapter.ViewHolder)?.reset()
                }
            }
            singleChoiceRecyclerView?.apply {
                mangaSegment.filterBySingleChoice.toList().forEachIndexed { index, _ ->
                    (findViewHolderForAdapterPosition(index)
                            as? FilterSingleChoiceAdapter.ViewHolder)?.reset()
                }
            }
            multipleChoicesRecyclerView?.apply {
                mangaSegment.filterByMultipleChoices.toList().forEachIndexed { index, _ ->
                    (findViewHolderForAdapterPosition(index)
                            as? FilterMultipleChoicesAdapter.ViewHolder)?.reset()
                }
            }
        }
        cancelButton?.setOnClickListener { onBackPressed() }
        progress?.visibility = View.GONE
    }

    private val onRefreshError: (Throwable) -> Unit = { error ->
        toast(R.string.filter_activity_load_filter_error_text)
        Timber.e(error)
        Crashlytics.logException(error)
    }
}
