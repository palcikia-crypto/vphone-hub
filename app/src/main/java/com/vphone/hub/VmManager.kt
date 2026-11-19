package com.vphone.hub

import android.app.ActivityManager
import android.content.Context

object VmManager {
    val vms: MutableList<VmProfile> = mutableListOf()

    fun init(ctx: Context) {
        if (vms.isEmpty()) vms.addAll(VmRepository.load(ctx))
    }

    fun persist(ctx: Context) = VmRepository.save(ctx, vms)

    fun totalRamMb(ctx: Context): Long {
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return mi.totalMem / (1024 * 1024)
    }

    fun availableRamMb(ctx: Context): Long {
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return mi.availMem / (1024 * 1024)
    }

    fun start(ctx: Context, vm: VmProfile): String {
        val total = totalRamMb(ctx)
        if (vm.ramMb > total) {
            return "Недостаточно памяти: профиль ${vm.ramMb} МБ, у устройства только $total МБ"
        }
        vm.running = true
        persist(ctx)
        return "ВМ «${vm.name}» запущена: RAM ${vm.ramMb} МБ, CPU ${vm.cpuCores}"
    }

    fun stop(ctx: Context, vm: VmProfile) {
        vm.running = false
        persist(ctx)
    }
}
