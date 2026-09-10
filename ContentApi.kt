package ph.gov.deped.region12.soxclmd.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * Thin HTTP client for the SOXCLMD shared content layer, which is served as
 * static JSON from the official GitHub Pages site (the single source of truth).
 *
 * Manifest-first sync: the app downloads the small manifest.json, compares the
 * SHA-256 of each feed with what is cached locally, and re-downloads only the
 * files that actually changed — kind to teachers on limited mobile data.
 */
class ContentApi(private val baseUrl: String) {

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun absolute(pathOrUrl: String): String =
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) pathOrUrl
        else baseUrl.trimEnd('/') + "/" + pathOrUrl.trimStart('/')

    suspend fun fetchText(path: String, noCache: Boolean = false): String =
        withContext(Dispatchers.IO) {
            val builder = Request.Builder().url(absolute(path))
            if (noCache) builder.header("Cache-Control", "no-cache")
            client.newCall(builder.build()).execute().use { resp ->
                if (!resp.isSuccessful) throw ApiException(resp.code, path)
                resp.body?.string() ?: throw ApiException(resp.code, path)
            }
        }

    suspend fun fetchBytes(path: String): ByteArray =
        withContext(Dispatchers.IO) {
            val builder = Request.Builder().url(absolute(path))
            client.newCall(builder.build()).execute().use { resp ->
                if (!resp.isSuccessful) throw ApiException(resp.code, path)
                resp.body?.bytes() ?: throw ApiException(resp.code, path)
            }
        }

    companion object {
        fun sha256(bytes: ByteArray): String {
            val d = MessageDigest.getInstance("SHA-256").digest(bytes)
            return d.joinToString("") { "%02x".format(it) }
        }
    }
}

class ApiException(val code: Int, val path: String) :
    Exception("HTTP $code while fetching $path")
