package com.vphone.hub

import android.content.Context
import org.json.JSONArray
import java.io.File

object VmRepository {
    fun file(ctx: Context) = File(ctx.filesDir, "vms.json")

    fun load(ctx: Context): MutableList<VmProfile> {
        val f = file(ctx)
        if (!f.exists()) return mutableListOf()
        val arr = JSONArray(f.readText())
        return MutableList(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            VmProfile(
                id = o.getString("id"),
                name = o.getString("name"),
                osType = o.optString("osType", "custom"),
                ramMb = o.getInt("ramMb"),
                cpuCores = o.optInt("cpuCores", 2),
                diskGb = o.optInt("diskGb", 16),
                imagePath = o.optString("imagePath", ""),
                running = o.optBoolean("running", false),
                createdAt = o.optLong("createdAt", 0L)
            )
        }
    }

    fun save(ctx: Context, vms: List<VmProfile>) {
        val arr = JSONArray()
        vms.forEach { vm ->
            arr.put(
                org.json.JSONObject()
                    .put("id", vm.id)
                    .put("name", vm.name)
                    .put("osType", vm.osType)
                    .put("ramMb", vm.ramMb)
                    .put("cpuCores", vm.cpuCores)
                    .put("diskGb", vm.diskGb)
                    .put("imagePath", vm.imagePath)
                    .put("running", vm.running)
                    .put("createdAt", vm.createdAt)
            )
        }
        file(ctx).writeText(arr.toString())
    }
}
