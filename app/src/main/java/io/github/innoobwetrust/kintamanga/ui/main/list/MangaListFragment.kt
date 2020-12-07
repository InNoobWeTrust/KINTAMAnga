package io.github.innoobwetrust.kintamanga.ui.main.list

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.factory
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.typedToJson
import com.google.gson.Gson
import io.github.innoobwetrust.kintamanga.KINTAMAngaPreferences
import io.github.innoobwetrust.kintamanga.R
import io.github.innoobwetrust.kintamanga.databinding.FragmentMangaListBinding
import io.github.innoobwetrust.kintamanga.source.SourceManager
import io.github.innoobwetrust.kintamanga.source.model.CatalogPage
import io.github.innoobwetrust.kintamanga.source.model.CatalogPages
import io.github.innoobwetrust.kintamanga.source.model.SourceSegment
import io.github.innoobwetrust.kintamanga.source.processor.MangaInfoProcessor
import io.github.innoobwetrust.kintamanga.ui.filter.FilterActivity
import io.github.innoobwetrust.kintamanga.ui.main.ElementInfoInteractionListener
import io.github.innoobwetrust.kintamanga.ui.main.MainActivity
import io.github.innoobwetrust.kintamanga.ui.main.MangaListAdapter
import io.github.innoobwetrust.kintamanga.ui.main.MangaListTypes
import io.github.innoobwetrust.kintamanga.ui.manga.MangaInfoActivity
import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding
import io.github.innoobwetrust.kintamanga.util.extension.toast
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import rx.Single
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.io.InputStream

class MangaListFragment :
        Fragment(),
        KodeinGlobalAware,
        MangaListNetworkLoader,
        ElementInfoInteractionListener {
    companion object {
        // Preference keys
        private enum class Preferences(val key: String){
            MANGA_SOURCE_NAME("MANGA_SOURCE_NAME_PREFERENCE_KEY"),
            MANGA_SEGMENT_INDEX("MANGA_SEGMENT_INDEX_PREFERENCE_KEY"),
//            MANGA_LIST_GRID_COLUMN_KEY("MANGA_LIST_GRID_COLUMN_KEY"),
            FILTER("FILTER_PREFERENCE_KEY")
        }
        // Intent keys
        enum class Intents(val key: String) {
            USER_INPUT("USER_INPUT_INTENT_KEY"),
            SINGLE_CHOICE("SINGLE_CHOICE_INTENT_KEY"),
            MULTIPLE_CHOICES("MULTIPLE_CHOICES_INTENT_KEY")
        }

        fun newInstance(): MangaListFragment = MangaListFragment()
    }

    data class MangaFilter(
            var sourceName: String = "",
            var pathName: String = "",
            var userInput: Map<String, String> = emptyMap(),
            var singleChoice: Map<String, String> = emptyMap(),
            var multipleChoice: Set<Pair<String, String>> = emptySet()
    )

    private var mListType = MangaListTypes.GRID
    private val mColumnCount = 5
//        get() = instance<SharedPreferences>(
//                KINTAMAngaPreferences.MAIN_ACTIVITY.key
//        ).getInt(MANGA_LIST_GRID_COLUMN_KEY, 5) ?: 5

    private val gson: Gson = instance()

    private var mangaSourceName: String
        get() = instance<SharedPreferences>(
                KINTAMAngaPreferences.MAIN_ACTIVITY.key
        ).getString(
                Preferences.MANGA_SOURCE_NAME.key,
                SourceManager.sourceNameList[0]
        )?.run {
            if (SourceManager.sourceNameList.contains(this)) this else SourceManager.sourceNameList[0]
        } ?: SourceManager.sourceNameList[0]
        set(value) = instance<SharedPreferences>(
                KINTAMAngaPreferences.MAIN_ACTIVITY.key
        ).edit().putString(Preferences.MANGA_SOURCE_NAME.key, value).apply()
    private var segmentIndex: Int
        get() = instance<SharedPreferences>(
                KINTAMAngaPreferences.MAIN_ACTIVITY.key
        ).getInt(Preferences.MANGA_SEGMENT_INDEX.key, 0)
        set(value) = instance<SharedPreferences>(
                KINTAMAngaPreferences.MAIN_ACTIVITY.key
        ).edit().putInt(Preferences.MANGA_SEGMENT_INDEX.key, value).apply()
    override val mangaSegment: SourceSegment
        get() {
            return try {
                SourceManager.getMangaSegmentForSource(
                        sourceName = mangaSourceName,
                        segmentIndex = segmentIndex
                )!!
            } catch (e: Exception) {
                Timber.e(e, "source: $mangaSourceName, index: $segmentIndex")
                throw e
            }
        }

    override val mangaInfoProcessor: MangaInfoProcessor
        get() = SourceManager.getMangaInfoProcessorForSourceName(sourceName = mangaSourceName)!!

    override var catalogPages: CatalogPages = CatalogPages()

    private var filtersString: String
        get() = instance<SharedPreferences>(
                KINTAMAngaPreferences.MAIN_ACTIVITY.key
        ).getString(Preferences.FILTER.key, "[]") ?: "[]"
        set(value) = instance<SharedPreferences>(
                KINTAMAngaPreferences.MAIN_ACTIVITY.key
        ).edit().putString(Preferences.FILTER.key, value).apply()
    override var userInput: Map<String, String> = emptyMap()
    override var singleChoice: Map<String, String> = emptyMap()
    override var multipleChoices: Set<Pair<String, String>> = emptySet()

    override var refreshDisposable: Subscription? = null
    override var loadNextPageDisposable: Subscription? = null
    override var loadMissingInfoDisposable: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private lateinit var binding: FragmentMangaListBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentMangaListBinding.inflate(inflater, container, false)
        setupMangaListView()
        setupToolbar()
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val filterOption = menu.findItem(R.id.action_filter)
        if (mangaSegment.run {
            filterByUserInput.isEmpty() &&
                    filterBySingleChoice.isEmpty() &&
                    filterByMultipleChoices.isEmpty()
        }) {
            filterOption?.isVisible = false
        } else {
            filterOption?.apply {
                isVisible = true
                setIcon(if (mangaSegment.filterByUserInput.isEmpty())
                    R.drawable.ic_filter_list_white_24dp
                else R.drawable.ic_search_white_24dp)
            }
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.action_filter == item.itemId) {
            requestFilter()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            Glide.get(it.applicationContext).registry.replace(
                    GlideUrl::class.java,
                    InputStream::class.java,
                    OkHttpUrlLoader.Factory(
                        instance<OkHttpClient>("cover")
                            .newBuilder()
                            .addInterceptor(factory<Headers, Interceptor>("headers")(mangaInfoProcessor.headers()))
                            .build()
                    )
            )
        }
    }

    override fun onPause() {
        disposeAllLoaderDisposables()
        binding.swipeRefreshLayout.isRefreshing = false
        super.onPause()
    }

    override fun onDestroy() {
        binding.listElementInfos.adapter = null
        System.gc()
        super.onDestroy()
    }

    private fun setupMangaListView() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            backgroundRefresh(
                    onRefreshedCatalogPage = onRefreshedCatalogPage,
                    onRefreshError = onRefreshError
            )
        }
        binding.listElementInfos.layoutManager = if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE)
            GridLayoutManager(
                    binding.listElementInfos.context,
                    mColumnCount,
                    RecyclerView.VERTICAL,
                    false
            )
        else
            GridLayoutManager(
                    binding.listElementInfos.context,
                    mColumnCount - 2,
                    RecyclerView.VERTICAL,
                    false
            )
        binding.listElementInfos.adapter = MangaListAdapter(
                catalogPages = catalogPages,
                listType = mListType,
                elementInfoInteractionListener = this
        )
    }

    private fun setupToolbar() {
        (activity as? MainActivity)?.let {
            it.supportActionBar?.title = null
            if (sourceSpinnerAdapter != it.binding.contentMain.spinnerPrimary.adapter ||
                    segmentSpinnerAdapter != it.binding.contentMain.spinnerSecondary.adapter)
                setupSpinners(it)
            else {
                it.binding.contentMain.spinnerPrimary.visibility = View.VISIBLE
                it.binding.contentMain.spinnerSecondary.visibility = View.VISIBLE
            }
        }
    }

    private fun setupSpinners(mainActivity: MainActivity) {
        val spinnerSource = mainActivity.binding.contentMain.spinnerPrimary
        spinnerSource.adapter = sourceSpinnerAdapter
        spinnerSource.onItemSelectedListener = sourceSpinnerOnItemSelectedListener
        val sourcePosition = SourceManager.sourceNameList.indexOf(mangaSourceName)
        if (-1 < sourcePosition) spinnerSource.setSelection(sourcePosition)
        spinnerSource.visibility = View.VISIBLE
    }

    private fun setupSegmentSpinner(resetIndex: Boolean) {
        (activity as? MainActivity)?.let {
            val spinnerSegment = it.binding.contentMain.spinnerSecondary
            spinnerSegment.adapter = segmentSpinnerAdapter
            if (resetIndex) segmentIndex = 0
            spinnerSegment.onItemSelectedListener = segmentSpinnerOnItemSelectedListener
            retrieveFilter()
            // This will start the first refresh
            spinnerSegment.setSelection(segmentIndex)
            spinnerSegment.visibility = View.VISIBLE
        }
    }

    private val sourceSpinnerAdapter: ArrayAdapter<String> by lazy {
        val mangaSourceAdapter: ArrayAdapter<String> = ArrayAdapter(
                this.requireActivity(),
                R.layout.themed_spinner_item,
                SourceManager.sourceNameListWithLang
        )
        mangaSourceAdapter.setDropDownViewResource(
                R.layout.themed_spinner_dropdown_item
        )
        mangaSourceAdapter
    }

    private val sourceSpinnerOnItemSelectedListener: AdapterView.OnItemSelectedListener by lazy {
        object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                binding.swipeRefreshLayout.isRefreshing = false
                disposeAllLoaderDisposables()
                val newSourceName =
                        SourceManager.sourceNameList.getOrElse(
                                position
                        ) { SourceManager.sourceNameList[0] }
                // First start check
                if (newSourceName != mangaSourceName) {
                    mangaSourceName = newSourceName
                    setupSegmentSpinner(resetIndex = true)
                } else {
                    setupSegmentSpinner(resetIndex = false)
                }
            }
        }
    }

    private val segmentSpinnerAdapter: ArrayAdapter<String>
        get() {
            val segmentNameList = SourceManager
                    .getSourceByName(sourceName = mangaSourceName)!!
                    .mangaSegments
                    .map { it.pathName }
            if (segmentNameList.size <= segmentIndex) segmentIndex = 0
            val mangaSegmentAdapter: ArrayAdapter<String> = ArrayAdapter(
                    this.requireActivity(),
                    R.layout.themed_spinner_item,
                    segmentNameList
            )
            mangaSegmentAdapter.setDropDownViewResource(
                    R.layout.themed_spinner_dropdown_item
            )
            return mangaSegmentAdapter
        }

    private val segmentSpinnerOnItemSelectedListener: AdapterView.OnItemSelectedListener by lazy {
        object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                stopLoadingMissingInfo()
                Single.fromCallable { saveFilter() }
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnEach {
                            segmentIndex = position
                            activity?.invalidateOptionsMenu()
                        }
                        .observeOn(Schedulers.newThread())
                        .map { retrieveFilter() }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (userInput.isEmpty() &&
                                    singleChoice.isEmpty() &&
                                    multipleChoices.isEmpty() &&
                                    mangaSegment.filterByUserInput.isNotEmpty()) {
                                requestFilter()
                            } else {
                                binding.swipeRefreshLayout.isRefreshing = true
                                backgroundRefresh(
                                        onRefreshedCatalogPage = onRefreshedCatalogPage,
                                        onRefreshError = onRefreshError
                                )
                            }
                        }
            }
        }
    }

    private fun retrieveFilter() {
        val filters: List<MangaFilter>
        var done = false
        try {
            filters = gson.fromJson(filtersString)
            done = filters
                    .find { it.sourceName == mangaSourceName && it.pathName == mangaSegment.pathName }
                    ?.let {
                        userInput = it.userInput
                        singleChoice = it.singleChoice
                        multipleChoices = it.multipleChoice
                        return@let true
                    } == true
        } catch (e: Exception) {
        }
        if (!done) {
            userInput = emptyMap()
            singleChoice = emptyMap()
            multipleChoices = emptySet()
        }
    }

    private fun saveFilter() {
        var filters: MutableSet<MangaFilter>
        var done = false
        try {
            filters = gson.fromJson(filtersString)
            filters = filters.filterNot {
                it.sourceName == mangaSourceName && it.pathName == mangaSegment.pathName
            }.toMutableSet()
            filters.add(MangaFilter().also {
                it.sourceName = mangaSourceName
                it.pathName = mangaSegment.pathName
                it.userInput = userInput
                it.singleChoice = singleChoice
                it.multipleChoice = multipleChoices
            })
            filtersString = gson.typedToJson(filters)
            done = true
        } catch (e: Exception) {
        }
        if (!done) {
            filtersString = "[]"
        }
    }

    private val onRefreshedCatalogPage: (CatalogPage) -> Unit = { catalogPage ->
        catalogPages.setup(catalogPage = catalogPage)
        binding.listElementInfos.adapter?.notifyDataSetChanged()
        binding.listElementInfos.scrollToPosition(0)
        activity?.invalidateOptionsMenu()
        binding.swipeRefreshLayout.isRefreshing = false
        if (catalogPage.elementInfos.isEmpty())
            context?.toast(R.string.refresh_manga_list_empty)
        else
            backgroundLoadMissingInfo(
                    onNextMissingMangaInfoLoaded = onNextMissingMangaInfoLoaded,
                    onMissingMangaInfoError = onMissingMangaInfoError
            )
    }

    private val onRefreshError: (Throwable) -> Unit = { error ->
        binding.swipeRefreshLayout.isRefreshing = false
        context?.toast(R.string.refresh_manga_list_error)
        Timber.e(error)
    }

    private val onNextCatalogPage: (CatalogPage?) -> Unit = { catalogPage ->
        if (null != catalogPage) {
            catalogPages.appendNextCatalogPage(catalogPage = catalogPage)
            binding.listElementInfos.adapter?.notifyDataSetChanged()
        }
        binding.swipeRefreshLayout.isRefreshing = false
        backgroundLoadMissingInfo(
                onNextMissingMangaInfoLoaded = onNextMissingMangaInfoLoaded,
                onMissingMangaInfoError = onMissingMangaInfoError
        )
    }

    private val onNextCatalogPageError: (Throwable) -> Unit = onRefreshError

    private val onNextMissingMangaInfoLoaded: (MangaBinding) -> Unit = { info ->
        catalogPages.elementInfos.find { it.itemUri == info.mangaUri }
                ?.let {
                    it.itemThumbnailUri = info.mangaThumbnailUri
                    it.itemDescription = info.mangaDescription
                    binding.listElementInfos.adapter
                            ?.notifyItemChanged(catalogPages.elementInfos.indexOf(it))
                }
    }

    private val onMissingMangaInfoError: (Throwable) -> Unit = onRefreshError

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (0 == requestCode && Activity.RESULT_OK == resultCode) {
            userInput = gson.fromJson(data?.getStringExtra(Intents.USER_INPUT.key) ?:
                    throw Exception("Error processing specified filter/search options")
            )
            singleChoice = gson.fromJson(data.getStringExtra(Intents.SINGLE_CHOICE.key) ?:
                    throw Exception("Error processing specified filter/search options")
            )
            multipleChoices = gson.fromJson(data.getStringExtra(Intents.MULTIPLE_CHOICES.key) ?:
                    throw Exception("Error processing specified filter/search options")
            )
            saveFilter()
            catalogPages.elementInfos.clear()
            binding.listElementInfos.adapter?.notifyDataSetChanged()
            binding.swipeRefreshLayout.isRefreshing = true
            backgroundRefresh(
                    onRefreshedCatalogPage = onRefreshedCatalogPage,
                    onRefreshError = onRefreshError
            )
        }
    }

    override fun requestFilter() {
        val filterIntent = Intent(activity, FilterActivity::class.java)
                .putExtra(FilterActivity.MANGA_SEGMENT_INTENT_KEY, mangaSegment)
                .putExtra(Intents.USER_INPUT.key, gson.typedToJson(userInput))
                .putExtra(Intents.SINGLE_CHOICE.key, gson.typedToJson(singleChoice))
                .putExtra(Intents.MULTIPLE_CHOICES.key, gson.typedToJson(multipleChoices))
        startActivityForResult(filterIntent, 0)
    }

    override fun onMangaCardClick(mangaBinding: MangaBinding) {
        if (mangaBinding.mangaUri.isNotEmpty()) {
            val mangaInfoIntent = Intent(activity, MangaInfoActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(MangaInfoActivity.Companion.Intents.MANGA_INFO.key, mangaBinding)
            startActivity(mangaInfoIntent)
        }
    }

    override fun onRequestMoreElement() {
        if (loadNextPageDisposable?.isUnsubscribed == false) return
        if (binding.swipeRefreshLayout.isRefreshing) return
        // Start loading next page when remaining manga is 5 or less
        val catalogHasNextPage = try {
            catalogPages.hasNextPage()
        } catch (e: Exception) {
            false
        }
        if (catalogHasNextPage) {
            binding.swipeRefreshLayout.isRefreshing = true
            backgroundLoadNextCatalogPage(
                    onNextCatalogPage = onNextCatalogPage,
                    onNextCatalogPageError = onNextCatalogPageError
            )
        }
    }
}
