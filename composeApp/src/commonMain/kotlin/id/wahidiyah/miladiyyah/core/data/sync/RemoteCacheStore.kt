package id.wahidiyah.miladiyyah.core.data.sync

import id.wahidiyah.miladiyyah.core.utils.AppCache

data class LocalSyncMeta(
    val version: String? = null,
    val updatedAt: String? = null,
    val fingerprint: String? = null
)

object RemoteCacheStore {

    private fun dataKey(name: String) = "SYNC_${name}_DATA"
    private fun versionKey(name: String) = "SYNC_${name}_VERSION"
    private fun updatedAtKey(name: String) = "SYNC_${name}_UPDATED_AT"
    private fun fingerprintKey(name: String) = "SYNC_${name}_FINGERPRINT"

    fun loadData(name: String): String? =
        AppCache.load(dataKey(name))

    fun saveData(
        name: String,
        data: String,
        meta: LocalSyncMeta
    ) {
        AppCache.save(dataKey(name), data)
        AppCache.save(versionKey(name), meta.version.orEmpty())
        AppCache.save(updatedAtKey(name), meta.updatedAt.orEmpty())
        AppCache.save(fingerprintKey(name), meta.fingerprint.orEmpty())
    }

    fun loadMeta(name: String): LocalSyncMeta {
        return LocalSyncMeta(
            version = AppCache.load(versionKey(name))?.takeIf { it.isNotBlank() },
            updatedAt = AppCache.load(updatedAtKey(name))?.takeIf { it.isNotBlank() },
            fingerprint = AppCache.load(fingerprintKey(name))?.takeIf { it.isNotBlank() }
        )
    }
}
