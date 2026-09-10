package ph.gov.deped.region12.soxclmd.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import java.io.File

/**
 * Central document downloader. Uses the system DownloadManager so files land
 * in the shared Downloads folder with a visible progress notification —
 * exactly what teachers expect — without the app holding any storage
 * permission.
 */
object Downloads {

    fun enqueue(context: Context, url: String, fileName: String, title: String): Long {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(title)
            .setDescription("SOXCLMD download")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return dm.enqueue(request)
    }

    fun download(context: Context, url: String, fallbackName: String, title: String) {
        if (url.isBlank()) {
            Toast.makeText(context, "No downloadable file is attached to this item.", Toast.LENGTH_SHORT).show()
            return
        }
        enqueue(context, url, fallbackName, title)
        Toast.makeText(context, "Downloading to your Downloads folder…", Toast.LENGTH_SHORT).show()
    }

    fun cacheDirSize(context: Context): Long =
        File(context.filesDir, "content").walkBottomUp().filter { it.isFile }.sumOf { it.length() }

    fun humanBytes(bytes: Long): String = when {
        bytes >= 1 shl 20 -> String.format("%.1f MB", bytes / 1048576.0)
        bytes >= 1 shl 10 -> String.format("%d KB", bytes / 1024)
        else -> "$bytes B"
    }

    @Suppress("unused")
    private val environmentHint = Environment.DIRECTORY_DOWNLOADS
}
