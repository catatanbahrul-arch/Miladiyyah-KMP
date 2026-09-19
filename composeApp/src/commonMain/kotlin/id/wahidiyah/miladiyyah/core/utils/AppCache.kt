package id.wahidiyah.miladiyyah.core.utils

object AppCache {
    var save: (String, String) -> Unit = { _, _ -> }
    var load: (String) -> String? = { null }
    
    fun saveBoolean(key: String, value: Boolean) { save(key, value.toString()) }
    fun loadBoolean(key: String, default: Boolean = true): Boolean {
        val res = load(key)
        return res?.toBooleanStrictOrNull() ?: default
    }
}
