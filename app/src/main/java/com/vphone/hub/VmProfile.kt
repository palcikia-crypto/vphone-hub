package com.vphone.hub

data class VmProfile(
    val id: String,
    var name: String,
    val osType: String,
    var ramMb: Int,
    var cpuCores: Int,
    var diskGb: Int,
    var imagePath: String,
    var running: Boolean = false,
    var createdAt: Long = System.currentTimeMillis()
)
