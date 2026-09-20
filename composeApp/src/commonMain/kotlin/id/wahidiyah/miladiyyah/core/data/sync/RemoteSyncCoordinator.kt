package id.wahidiyah.miladiyyah.core.data.sync

import id.wahidiyah.miladiyyah.core.data.source.remote.CascadeAdjustment
import id.wahidiyah.miladiyyah.core.data.source.remote.KegiatanItem
import id.wahidiyah.miladiyyah.core.data.source.remote.KegiatanRepository
import id.wahidiyah.miladiyyah.core.data.source.remote.PengumumanData
import id.wahidiyah.miladiyyah.core.data.source.remote.PengumumanRepository
import id.wahidiyah.miladiyyah.core.data.source.remote.PustakaItem
import id.wahidiyah.miladiyyah.core.data.source.remote.PustakaRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object RemoteSyncCoordinator {

    const val KEY_HIJRI = "HIJRI"
    const val KEY_KEGIATAN = "KEGIATAN"
    const val KEY_PENGUMUMAN = "PENGUMUMAN"
    const val KEY_PUSTAKA = "PUSTAKA"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private fun fingerprint(text: String): String {
        var hash = 1125899906842597L
        for (char in text) {
            hash = hash * 31L + char.code.toLong()
        }
        return hash.toULong().toString(16)
    }

    private fun meta(raw: String) = LocalSyncMeta(
        fingerprint = fingerprint(raw)
    )

    fun loadCachedKegiatan(): List<KegiatanItem> {
        val raw = RemoteCacheStore.loadData(KEY_KEGIATAN) ?: return emptyList()
        return try {
            json.decodeFromString<List<KegiatanItem>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun loadCachedPustaka(): List<PustakaItem> {
        val raw = RemoteCacheStore.loadData(KEY_PUSTAKA) ?: return emptyList()
        return try {
            json.decodeFromString<List<PustakaItem>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun loadCachedPengumuman(): PengumumanData? {
        val raw = RemoteCacheStore.loadData(KEY_PENGUMUMAN) ?: return null
        return try {
            json.decodeFromString<PengumumanData?>(raw)
        } catch (_: Exception) {
            null
        }
    }

    fun loadCachedHijri(): List<CascadeAdjustment> {
        val raw = RemoteCacheStore.loadData(KEY_HIJRI) ?: return emptyList()
        return try {
            json.decodeFromString<List<CascadeAdjustment>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun syncKegiatan(): List<KegiatanItem> {
        val cached = loadCachedKegiatan()
        val result = KegiatanRepository.fetchKegiatan()

        if (!result.success) {
            println(
                "[CACHE-KEGIATAN] Remote gagal -> memakai cache"
            )
            return cached
        }

        val raw =
            json.encodeToString<List<KegiatanItem>>(result.data)

        val newFingerprint = fingerprint(raw)
        val old = RemoteCacheStore.loadMeta(KEY_KEGIATAN)

        if (old.fingerprint != newFingerprint) {
            RemoteCacheStore.saveData(
                name = KEY_KEGIATAN,
                data = raw,
                meta = meta(raw)
            )

            println(
                "[CACHE-KEGIATAN] Cache diperbarui. " +
                    "items=${result.data.size}"
            )
        } else {
            println(
                "[CACHE-KEGIATAN] Data sama. " +
                    "Cache tidak ditulis ulang."
            )
        }

        return result.data
    }

    suspend fun syncPustaka(): List<PustakaItem> {
        val cached = loadCachedPustaka()
        val result = PustakaRepository.fetchPustaka()

        if (!result.success) {
            println(
                "[CACHE-PUSTAKA] Remote gagal -> memakai cache"
            )
            return cached
        }

        val raw =
            json.encodeToString<List<PustakaItem>>(result.data)

        val newFingerprint = fingerprint(raw)
        val old = RemoteCacheStore.loadMeta(KEY_PUSTAKA)

        if (old.fingerprint != newFingerprint) {
            RemoteCacheStore.saveData(
                name = KEY_PUSTAKA,
                data = raw,
                meta = meta(raw)
            )

            println(
                "[CACHE-PUSTAKA] Cache diperbarui. " +
                    "items=${result.data.size}"
            )
        } else {
            println(
                "[CACHE-PUSTAKA] Data sama. " +
                    "Cache tidak ditulis ulang."
            )
        }

        return result.data
    }

    suspend fun syncPengumuman(): PengumumanData? {
        val cached = loadCachedPengumuman()
        val result = PengumumanRepository.fetchPengumuman()

        if (!result.success) {
            println(
                "[CACHE-PENGUMUMAN] Remote gagal -> memakai cache"
            )
            return cached
        }

        val raw =
            json.encodeToString<PengumumanData?>(result.data)

        val newFingerprint = fingerprint(raw)
        val old = RemoteCacheStore.loadMeta(KEY_PENGUMUMAN)

        if (old.fingerprint != newFingerprint) {
            RemoteCacheStore.saveData(
                name = KEY_PENGUMUMAN,
                data = raw,
                meta = meta(raw)
            )

            println(
                "[CACHE-PENGUMUMAN] Cache diperbarui. " +
                    "active=${result.data != null}"
            )
        } else {
            println(
                "[CACHE-PENGUMUMAN] Data sama. " +
                    "Cache tidak ditulis ulang."
            )
        }

        return result.data
    }

    fun cacheHijri(remoteList: List<CascadeAdjustment>): List<CascadeAdjustment> {
        val raw = json.encodeToString<List<CascadeAdjustment>>(remoteList)
        val newFingerprint = fingerprint(raw)
        val old = RemoteCacheStore.loadMeta(KEY_HIJRI)

        if (old.fingerprint != newFingerprint) {
            RemoteCacheStore.saveData(KEY_HIJRI, raw, meta(raw))
            println("[CACHE-HIJRI] Cache diperbarui records=${remoteList.size}")
        } else {
            println("[CACHE-HIJRI] Data sama; cache tidak ditulis ulang")
        }

        return remoteList
    }
    suspend fun syncHijri(
        remoteList: List<CascadeAdjustment>
    ): List<CascadeAdjustment> {

        val raw =
            json.encodeToString<List<CascadeAdjustment>>(remoteList)

        val newFingerprint = fingerprint(raw)
        val old = RemoteCacheStore.loadMeta(KEY_HIJRI)

        if (old.fingerprint != newFingerprint) {
            RemoteCacheStore.saveData(
                name = KEY_HIJRI,
                data = raw,
                meta = meta(raw)
            )

            println(
                "[CACHE-HIJRI] Cache diperbarui. " +
                    "records=${remoteList.size}"
            )
        } else {
            println(
                "[CACHE-HIJRI] Data sama. " +
                    "Cache tidak ditulis ulang."
            )
        }

        return remoteList
    }
}
