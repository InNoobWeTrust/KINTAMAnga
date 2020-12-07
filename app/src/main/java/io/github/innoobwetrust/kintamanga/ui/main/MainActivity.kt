package io.github.innoobwetrust.kintamanga.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.github.salomonbrys.kodein.conf.KodeinGlobalAware
import com.github.salomonbrys.kodein.instance
import com.google.android.material.navigation.NavigationView
import io.github.innoobwetrust.kintamanga.KINTAMAngaPreferences
import io.github.innoobwetrust.kintamanga.R
import io.github.innoobwetrust.kintamanga.databinding.ActivityMainBinding
import io.github.innoobwetrust.kintamanga.ui.downloader.DownloaderActivity
import io.github.innoobwetrust.kintamanga.ui.main.favorite.FavoriteFragment
import io.github.innoobwetrust.kintamanga.ui.main.list.MangaListFragment

class MainActivity :
        AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        KodeinGlobalAware {
    companion object {
        private enum class Preferences(val key: String) {
            DEFAULT_FRAGMENT("DEFAULT_FRAGMENT_KEY"),
            CURRENT_FRAGMENT("CURRENT_FRAGMENT_SAVE_KEY")
        }

        private val fragmentTagList: List<String> =
                listOf("Latest Updates", "Collection", "Downloaded")
    }

    private var currentFragmentIndex: Int = 0
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.contentMain.toolbar)

        val toggle = ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                binding.contentMain.toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (null == savedInstanceState) {
            currentFragmentIndex = instance<SharedPreferences>(
                    KINTAMAngaPreferences.MAIN_ACTIVITY.key
            ).getInt(Preferences.DEFAULT_FRAGMENT.key, 0)
            doFragmentTransaction()
        } else {
            currentFragmentIndex = savedInstanceState.getInt(Preferences.CURRENT_FRAGMENT.key)
        }
        binding.navView.menu.getItem(currentFragmentIndex).isChecked = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(Preferences.CURRENT_FRAGMENT.key, currentFragmentIndex)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            finish()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        var selected = false
        when (id) {
            R.id.nav_browse -> {
                switchToFragmentIndex(0)
                selected = true
            }
            R.id.nav_collection -> {
                switchToFragmentIndex(1)
                selected = true
            }
            R.id.nav_downloader -> {
                val downloaderIntent = Intent(this, DownloaderActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(downloaderIntent)
            }
            R.id.nav_share -> {
                val sharingIntent = Intent(Intent.ACTION_SEND)
                        .putExtra(Intent.EXTRA_SUBJECT, "KINTAMANga")
                        .putExtra(Intent.EXTRA_TEXT, getString(R.string.navigation_drawer_menu_share_text)
                                + "https://github.com/InNoobWeTrust/KINTAMAnga")
                        .setType("text/plain")
                startActivity(
                        Intent.createChooser(
                                sharingIntent,
                                getString(R.string.manga_share_chooser_title)
                        )
                )
            }
            R.id.nav_fanPage -> {
                val fanPageIntent = Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("https://www.facebook.com/KINTAMAnga/"))
                startActivity(fanPageIntent)
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return selected
    }

    private fun switchToFragmentIndex(fragmentIndex: Int): Boolean {
        return if (fragmentIndex in fragmentTagList.indices &&
                fragmentIndex != currentFragmentIndex) {
            currentFragmentIndex = fragmentIndex
            doFragmentTransaction()
            true
        } else false
    }

    private fun doFragmentTransaction() {
        val toBeAttachedFragment: Fragment =
                supportFragmentManager.findFragmentByTag(fragmentTagList[currentFragmentIndex]) ?: when (currentFragmentIndex) {
                    1 -> FavoriteFragment.newInstance()
                    else -> MangaListFragment.newInstance()
                } as Fragment
        supportFragmentManager
                .beginTransaction()
                .replace(
                        R.id.content,
                        toBeAttachedFragment,
                        fragmentTagList[currentFragmentIndex]
                ).commit()
        binding.contentMain.spinnerPrimary.visibility = View.GONE
        binding.contentMain.spinnerSecondary.visibility = View.GONE
    }
}
