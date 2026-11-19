package com.vphone.hub.accounts

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Account(val type: String, val label: String, val login: String) {
    fun toJson() = JSONObject()
        .put("type", type)
        .put("label", label)
        .put("login", login)
}

object AccountStore {
    private const val PREFS = "vp_accounts"
    private const val KEY = "list"

    fun load(ctx: Context): MutableList<Account> {
        val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
            ?: return mutableListOf()
        val arr = JSONArray(raw)
        return MutableList(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            Account(
                type = o.optString("type", "account"),
                label = o.optString("label", "Без имени"),
                login = o.optString("login", "")
            )
        }
    }

    fun save(ctx: Context, accounts: List<Account>) {
        val arr = JSONArray()
        accounts.forEach { arr.put(it.toJson()) }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY, arr.toString()).apply()
    }
}
