package com.vphone.hub

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VmAdapter(private val onClick: (VmProfile) -> Unit) : RecyclerView.Adapter<VmAdapter.Holder>() {
    private val items = mutableListOf<VmProfile>()

    fun submit(vms: List<VmProfile>) {
        items.clear()
        items.addAll(vms)
        notifyDataSetChanged()
    }

    class Holder(v: android.view.View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.vmName)
        val subtitle: TextView = v.findViewById(R.id.vmSubtitle)
        val status: TextView = v.findViewById(R.id.vmStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_vm, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: Holder, pos: Int) {
        val vm = items[pos]
        h.name.text = vm.name
        h.subtitle.text = "${vm.osType} · ${vm.ramMb} МБ · ${vm.cpuCores} CPU · ${vm.diskGb} ГБ"
        h.status.text = if (vm.running) "● запущена" else "○ выключена"
        h.itemView.setOnClickListener { onClick(vm) }
    }
}
