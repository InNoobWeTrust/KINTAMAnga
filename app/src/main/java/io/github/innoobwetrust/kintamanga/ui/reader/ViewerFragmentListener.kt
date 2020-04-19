package io.github.innoobwetrust.kintamanga.ui.reader

interface ViewerFragmentListener {
    fun onViewerToggleControl()
    fun onTapPreviousPage()
    fun onTapNextPage()
    fun onViewerPageChanged(newPagePosition: Int)
}