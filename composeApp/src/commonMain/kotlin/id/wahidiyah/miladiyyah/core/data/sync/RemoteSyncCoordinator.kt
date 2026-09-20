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

    private fun meta(raw: String) =
        LocalSyncMeta(fingerprint = fingerprint(raw))

    fun loadCachedKegiatan(): List<KegiatanItem> {
        val raw = RemoteCacheStore.loadData(KEY_KEGIATAN) ?: return emptyList()
        return try {
            json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun loadCachedPustaka(): List<PustakaItem> {
        val raw = RemoteCacheStore.loadData(KEY_PUSTAKA) ?: return emptyList()
        return try {
            json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun loadCachedPengumuman(): PengumumanData? {
        val raw = RemoteCacheStore.loadData(KEY_PENGUMUMAN) ?: return null
        return try {
            json.decodeFromString(raw)
        } catch (_: Exception) {
            null
        }
    }

    fun loadCachedHijri(): List<CascadeAdjustment> {
        val raw = RemoteCacheStore.loadData(KEY_HIJRI) ?: return emptyList()
        return try {
            json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun syncKegiatan(): List<KegiatanItem> {
        val cached = loadCachedKegiatan()
        return try {
            val remote = KegiatanRepository.fetchKegiatan()
            if (remote.isNotEmpty()) {
                val raw = json.encodeToString(remote)
                val newFingerprint = fingerprint(raw)
                val old = RemoteCacheStore.loadMeta(KEY_KEGIATAN)

                if (old.fingerprint != newFingerprint) {
                    RemoteCacheStore.saveData(
                        KEY_KEGIATAN,
                        raw,
                        meta(raw)
                    )
                }
                remote
            } else cached
        } catch (_: Exception) {
            cached
        }
    }

    suspend fun syncPustaka(): List<PustakaItem> {
        val cached = loadCachedPustaka()
        return try {
            val remote = PustakaRepository.fetchPustaka()
            if (remote.isNotEmpty()) {
                val raw = json.encodeToString(remote)
                val newFingerprint = fingerprint(raw)
                val old = RemoteCacheStore.loadMeta(KEY_PUSTAKA)

                if (old.fingerprint != newFingerprint) {
                    RemoteCacheStore.saveData(
                        KEY_PUSTAKA,
                        raw,
                        meta(raw)
                    )
                }
                remote
            } else cached
        } catch (_: Exception) {
            cached
        }
    }

    suspend fun syncPengumuman(): PengumumanData? {
        val cached = loadCachedPengumuman()
        return try {
            val remote = PengumumanRepository.fetchPengumuman()
            if (remote != null) {
                val raw = json.encodeToString(remote)
                val newFingerprint = fingerprint(raw)
                val old = RemoteCacheStore.loadMeta(KEY_PENGUMUMAN)

                if (old.fingerprint != newFingerprint) {
                    RemoteCacheStore.saveData(
                        KEY_PENGUMUMAN,
                        raw,
                        meta(raw)
                    )
                }
                remote
            } else cached
        } catch (_: Exception) {
            cached
        }
    }

    suspend fun syncHijri(remoteList: List<CascadeAdjustment>): List<CascadeAdjustment> {
        if (remoteList.isNotEmpty()) {
            val raw = json.encodeToString(remoteList)
            val newFingerprint = fingerprint(raw)
            val old = RemoteCacheStore.loadMeta(KEY_HIJRI)

            if (old.fingerprint != newFingerprint) {
                RemoteCacheStore.saveData(
                    KEY_HIJRI,
                    raw,
                    meta(raw)
                )
            }
            return remoteList
        }

        return loadCachedHijri()
    }
}
