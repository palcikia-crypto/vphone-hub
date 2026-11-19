package com.vphone.hub.engine

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import com.vphone.hub.VmProfile

object TermuxEngine {
    private fun distroFor(vm: VmProfile): String? = when (vm.osType) {
        "linux" -> "debian"
        "custom" -> "alpine"
        else -> null
    }

    fun launch(activity: Activity, vm: VmProfile) {
        val distro = distroFor(vm)
        if (distro == null) {
            Toast.makeText(
                activity,
                "Тип «${vm.osType}»: запуск через QEMU-образ в Termux (см. termux-scripts/)",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val intent = Intent("com.termux.RUN_COMMAND").apply {
            setClassName("com.termux", "com.termux.app.RunCommandService")
            putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/proot-distro")
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("login", distro))
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", false)
        }
        try {
            activity.startService(intent)
            Toast.makeText(activity, "Запуск окружения в Termux: $distro", Toast.LENGTH_SHORT).show()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, "Termux не установлен или недоступен", Toast.LENGTH_LONG).show()
        }
    }
}
