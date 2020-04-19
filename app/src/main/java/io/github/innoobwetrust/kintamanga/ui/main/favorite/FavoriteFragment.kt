package io.github.innoobwetrust.kintamanga.ui.main.favorite

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.crashlytics.android.Crashlytics
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import io.github.innoobwetrust.kintamanga.R
import io.github.innoobwetrust.kintamanga.database.DatabaseHelper
import io.github.innoobwetrust.kintamanga.database.model.MangaDb
import io.github.innoobwetrust.kintamanga.source.SourceManager
import io.github.innoobwetrust.kintamanga.source.model.CatalogPages
import io.github.innoobwetrust.kintamanga.ui.main.ElementInfoInteractionListener
import io.github.innoobwetrust.kintamanga.ui.main.MainActivity
import io.github.innoobwetrust.kintamanga.ui.main.MangaListAdapter
import io.github.innoobwetrust.kintamanga.ui.main.MangaListTypes
import io.github.innoobwetrust.kintamanga.ui.manga.MangaInfoActivity
import io.github.innoobwetrust.kintamanga.ui.model.ElementInfo
import io.github.innoobwetrust.kintamanga.ui.model.MangaBinding
import io.github.innoobwetrust.kintamanga.util.extension.toast
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_manga_list.*
import kotlinx.android.synthetic.main.fragment_manga_list.view.*
import okhttp3.OkHttpClient
import rx.Subscription
import timber.log.Timber
import java.io.InputStream

class FavoriteFragment :
        Fragment(),
        KodeinGlobalAware,
        FavoriteMangaListDatabaseLoader,
        ElementInfoInteractionListener {
    companion object {
        fun newInstance(): FavoriteFragment = FavoriteFragment()
    }

    private val mListType = MangaListTypes.GRID
    private val mColumnCount = 5

    private var catalogPages: CatalogPages = CatalogPages()

    override val databaseHelper: DatabaseHelper = instance()
    override var observeDatabaseDisposable: Subscription? = null

    override var sourceNameFilter: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_manga_list, container, false)
        setupFavoriteListView(view = view)
        setupToolbar()
        return view
    }

    override fun onResume() {
        super.onResume()
        this.activity?.let {
            Glide.get(it.applicationContext).registry.replace(
                    GlideUrl::class.java,
                    InputStream::class.java,
                    OkHttpUrlLoader.Factory(instance<OkHttpClient>("cover"))
            )
        }
    }

    override fun onPause() {
        disposeAllLoaderDisposables()
        swipeRefreshLayout?.isRefreshing = false
        super.onPause()
    }

    override fun onDestroy() {
        listElementInfos?.adapter = null
        System.gc()
        super.onDestroy()
    }

    private fun setupFavoriteListView(view: View) {
        if (view.listElementInfos is RecyclerView) {
            when (activity?.resources?.configuration?.orientation) {
                Configuration.ORIENTATION_PORTRAIT ->
                    view.listElementInfos.layoutManager =
                            GridLayoutManager(
                                    view.listElementInfos.context,
                                    mColumnCount - 2,
                                    RecyclerView.VERTICAL,
                                    false
                            )
                Configuration.ORIENTATION_LANDSCAPE ->
                    view.listElementInfos.layoutManager =
                            GridLayoutManager(
                                    view.listElementInfos.context,
                                    mColumnCount,
                                    RecyclerView.VERTICAL,
                                    false
                            )
            }
            view.listElementInfos.adapter = MangaListAdapter(
                    catalogPages = catalogPages,
                    listType = mListType,
                    elementInfoInteractionListener = this
            )
        }
    }

    private fun setupToolbar() {
        (activity as? MainActivity)?.supportActionBar?.title = null
        if (activity?.spinnerPrimary is AppCompatSpinner) {
            if (groupSpinnerAdapter != activity?.spinnerPrimary?.adapter)
                setupSpinners()
            else {
                activity?.spinnerPrimary?.visibility = View.VISIBLE
            }
        }
    }

    private fun setupSpinners() {
        if (activity?.spinnerPrimary is AppCompatSpinner) {
            activity?.spinnerSecondary?.let {
                it.adapter = sourceSpinnerAdapter
                it.onItemSelectedListener = sourceSpinnerOnItemSelectedListener
                it.visibility = View.VISIBLE
            }
            activity?.spinnerPrimary?.let {
                it.adapter = groupSpinnerAdapter
                it.onItemSelectedListener = groupSpinnerOnItemSelectedListener
                it.visibility = View.VISIBLE
            }
        }
    }

    private val groupSpinnerAdapter: ArrayAdapter<String> by lazy {
        val mangaGroupAdapter: ArrayAdapter<String> = ArrayAdapter(
                this.requireActivity(),
                R.layout.themed_spinner_item,
                resources.getStringArray(R.array.manga_group_list)
        )
        mangaGroupAdapter.setDropDownViewResource(
                R.layout.themed_spinner_dropdown_item
        )
        mangaGroupAdapter
    }

    private val groupSpinnerOnItemSelectedListener: AdapterView.OnItemSelectedListener by lazy {
        object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                swipeRefreshLayout?.isRefreshing = false
                disposeAllLoaderDisposables()
                observeDataBase(
                        groupType = position,
                        onNextDatabaseChange = onNextDatabaseChange,
                        onDatabaseError = onDatabaseError
                )
                swipeRefreshLayout?.setOnRefreshListener {
                    disposeAllLoaderDisposables()
                    observeDataBase(
                            groupType = position,
                            onNextDatabaseChange = onNextDatabaseChange,
                            onDatabaseError = onDatabaseError
                    )
                }
                swipeRefreshLayout?.isRefreshing = true
            }
        }
    }

    private val sourceSpinnerAdapter: ArrayAdapter<String> by lazy {
        val sourceFilterAdapter: ArrayAdapter<String> = ArrayAdapter(
                this.requireActivity(),
                R.layout.themed_spinner_item,
                SourceManager.sourceNameList
                        .toMutableList()
                        .also { it.add(0, getString(R.string.source_filter_all)) }
        )
        sourceFilterAdapter.setDropDownViewResource(
                R.layout.themed_spinner_dropdown_item
        )
        sourceFilterAdapter
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
                sourceNameFilter = when (position) {
                    0 -> null
                    else -> sourceSpinnerAdapter.getItem(position)
                }
                activity?.spinnerPrimary?.let {
                    it.onItemSelectedListener?.onItemSelected(
                            null, null, it.selectedItemPosition, it.selectedItemId
                    )
                }
            }
        }
    }

    private val onNextDatabaseChange: (List<MangaDb>) -> Unit = { listMangaDb ->
        catalogPages.dataSetup(listMangaDb.map { mangaDb ->
            ElementInfo().apply {
                sourceName = mangaDb.mangaSourceName
                itemUri = mangaDb.mangaUri
                itemTitle = mangaDb.mangaTitle
                itemDescription = mangaDb.mangaDescription
                itemThumbnailUri = mangaDb.mangaThumbnailUri
            }
        })
        listElementInfos?.adapter?.notifyDataSetChanged()
        swipeRefreshLayout?.isRefreshing = false
    }

    private val onDatabaseError: (Throwable) -> Unit = { error ->
        context?.toast(R.string.refresh_database_manga_list_error)
        Timber.e(error)
        Crashlytics.logException(error)
    }

    override fun onMangaCardClick(mangaBinding: MangaBinding) {
        if (mangaBinding.mangaUri.isNotEmpty()) {
            val mangaInfoIntent = Intent(activity, MangaInfoActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(MangaInfoActivity.Companion.Intents.MANGA_INFO.key, mangaBinding)
            startActivity(mangaInfoIntent)
        }
    }
}
