package com.vphone.hub.automations

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class Automation(
    val id: String,
    var name: String,
    var command: String,
    var intervalMin: Long,
    var lastRun: Long = 0L
)

object AutomationStore {
    fun file(ctx: Context) = File(ctx.filesDir, "automations.json")

    fun load(ctx: Context): MutableList<Automation> {
        val f = file(ctx)
        if (!f.exists()) return mutableListOf()
        val arr = JSONArray(f.readText())
        return MutableList(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            Automation(
                id = o.getString("id"),
                name = o.getString("name"),
                command = o.getString("command"),
                intervalMin = o.getLong("intervalMin"),
                lastRun = o.optLong("lastRun", 0L)
            )
        }
    }

    fun save(ctx: Context, list: List<Automation>) {
        val arr = JSONArray()
        list.forEach { a ->
            arr.put(
                JSONObject()
                    .put("id", a.id)
                    .put("name", a.name)
                    .put("command", a.command)
                    .put("intervalMin", a.intervalMin)
                    .put("lastRun", a.lastRun)
            )
        }
        file(ctx).writeText(arr.toString())
    }
}

class AutomationEngine(private val ctx: Context) {
    @Volatile
    private var running = false
    private var thread: Thread? = null

    fun start() {
        if (running) return
        running = true
        thread = Thread {
            while (running) {
                val now = System.currentTimeMillis()
                val list = AutomationStore.load(ctx)
                var dirty = false
                list.forEach { a ->
                    if (a.intervalMin > 0 && now - a.lastRun >= a.intervalMin * 60_000) {
                        try {
                            Runtime.getRuntime().exec(arrayOf("sh", "-c", a.command))
                        } catch (_: Exception) {
                        }
                        a.lastRun = now
                        dirty = true
                    }
                }
                if (dirty) AutomationStore.save(ctx, list)
                try {
                    Thread.sleep(15_000)
                } catch (_: InterruptedException) {
                }
            }
        }.also { t ->
            t.isDaemon = true
            t.start()
        }
    }

    fun stop() {
        running = false
        thread?.interrupt()
    }
}
