package ph.gov.deped.region12.soxclmd.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ph.gov.deped.region12.soxclmd.AppApplication
import ph.gov.deped.region12.soxclmd.data.ContentRepository

/**
 * Single app-scoped ViewModel exposing the repository's reactive content
 * flows to every screen. Kept deliberately thin: all heavy lifting lives in
 * ContentRepository (offline-first cache + manifest sync).
 */
class AppViewModel(val repo: ContentRepository) : ViewModel() {

    val syncStatus = repo.syncStatus
    val manifest = repo.manifest
    val news = repo.news
    val events = repo.events
    val memoranda = repo.memoranda
    val learningAreas = repo.learningAreas
    val resources = repo.resources
    val site = repo.site
    val searchIndex = repo.searchIndex
    val highlights = repo.highlights

    var hasSyncedOnce by mutableStateOf(false)
        private set

    init {
        refresh()
    }

    fun refresh(force: Boolean = false) {
        viewModelScope.launch {
            repo.sync(force = force)
            hasSyncedOnce = true
        }
    }

    fun absoluteUrl(path: String): String = repo.absoluteUrl(path)

    @Suppress("UNCHECKED_CAST")
    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repository = (app as AppApplication).repository
            return AppViewModel(repository) as T
        }
    }
}
