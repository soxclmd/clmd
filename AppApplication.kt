package ph.gov.deped.region12.soxclmd

import android.app.Application
import ph.gov.deped.region12.soxclmd.data.ContentApi
import ph.gov.deped.region12.soxclmd.data.ContentRepository
import ph.gov.deped.region12.soxclmd.data.ContentStore
import ph.gov.deped.region12.soxclmd.data.SyncWorker

/**
 * Application entry point: builds the single content repository
 * (manual dependency injection — small, testable, framework-free) and
 * schedules the background sync workers.
 */
class AppApplication : Application() {

    lateinit var repository: ContentRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val api = ContentApi(ContentRepository.BASE_URL)
        repository = ContentRepository(this, api, ContentStore(this))
        SyncWorker.schedule(this)
    }
}
