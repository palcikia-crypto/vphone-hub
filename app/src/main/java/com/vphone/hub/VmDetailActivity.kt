package com.vphone.hub

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.vphone.hub.engine.TermuxEngine

class VmDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vm_detail)
        val id = intent.getStringExtra("id") ?: run { finish(); return }
        val vm = VmManager.vms.firstOrNull { it.id == id } ?: run { finish(); return }

        val nameView = findViewById<TextView>(R.id.detailName)
        val statsView = findViewById<TextView>(R.id.detailStats)
        val statusView = findViewById<TextView>(R.id.detailStatus)
        val toggle = findViewById<MaterialButton>(R.id.btnToggle)

        fun render() {
            nameView.text = vm.name
            statsView.text = "ОС: ${vm.osType}\nRAM: ${vm.ramMb} МБ · CPU: ${vm.cpuCores} · Диск: ${vm.diskGb} ГБ"
            statusView.text = if (vm.running) "Состояние: запущена" else "Состояние: остановлена"
            toggle.text = if (vm.running) "Остановить" else "Запустить"
        }
        render()

        toggle.setOnClickListener {
            if (vm.running) {
                VmManager.stop(this, vm)
                Toast.makeText(this, "ВМ остановлена", Toast.LENGTH_SHORT).show()
            } else {
                val msg = VmManager.start(this, vm)
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                if (vm.running) TermuxEngine.launch(this, vm)
            }
            render()
        }

        findViewById<MaterialButton>(R.id.btnTerminal).setOnClickListener {
            startActivity(Intent(this, TerminalActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnDelete).setOnClickListener {
            VmManager.stop(this, vm)
            VmManager.vms.remove(vm)
            VmManager.persist(this)
            finish()
        }
    }
}
