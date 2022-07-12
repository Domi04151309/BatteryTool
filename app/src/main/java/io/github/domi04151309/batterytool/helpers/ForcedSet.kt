package io.github.domi04151309.batterytool.helpers

import android.content.SharedPreferences
import org.json.JSONArray

class ForcedSet(private val preferences: SharedPreferences) {

    companion object {
        private var forcedSet: HashSet<String>? = null
    }

    init {
        if (forcedSet == null) {
            val forcedJson = JSONArray(
                preferences.getString(P.PREF_FORCED_LIST, P.PREF_FORCED_LIST_DEFAULT)
            )
            forcedSet = HashSet<String>(forcedJson.length()).apply {
                for (i in 0 until forcedJson.length()) {
                    add(forcedJson.getString(i))
                }
            }
        }
    }

    fun add(e: String) {
        forcedSet?.add(e)
    }

    fun contains(e: String): Boolean {
        return forcedSet?.contains(e) ?: false
    }

    fun remove(e: String) {
        forcedSet?.remove(e)
    }

    fun save() {
        val json = JSONArray()
        forcedSet?.forEach {
            json.put(it)
        }
        preferences.edit().putString(P.PREF_FORCED_LIST, json.toString()).apply()
    }

}