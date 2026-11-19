package com.vphone.hub

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import java.util.UUID

class VmCreateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vm_create)

        val name = findViewById<EditText>(R.id.inputName)
        val os = findViewById<Spinner>(R.id.spinnerOs)
        os.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.os_types)
        )

        val labelRam = findViewById<TextView>(R.id.labelRam)
        val sliderRam = findViewById<Slider>(R.id.sliderRam)
        sliderRam.addOnChangeListener { _, v, _ -> labelRam.text = "Память: ${v.toInt()} МБ" }

        val labelCpu = findViewById<TextView>(R.id.labelCpu)
        val sliderCpu = findViewById<Slider>(R.id.sliderCpu)
        sliderCpu.addOnChangeListener { _, v, _ -> labelCpu.text = "Ядер: ${v.toInt()}" }

        val labelDisk = findViewById<TextView>(R.id.labelDisk)
        val sliderDisk = findViewById<Slider>(R.id.sliderDisk)
        sliderDisk.addOnChangeListener { _, v, _ -> labelDisk.text = "Диск: ${v.toInt()} ГБ" }

        findViewById<MaterialButton>(R.id.btnCreate).setOnClickListener {
            VmManager.vms.add(
                VmProfile(
                    id = UUID.randomUUID().toString(),
                    name = name.text.toString().trim().ifBlank { "ВМ-${VmManager.vms.size + 1}" },
                    osType = os.selectedItem.toString(),
                    ramMb = sliderRam.value.toInt(),
                    cpuCores = sliderCpu.value.toInt(),
                    diskGb = sliderDisk.value.toInt(),
                    imagePath = ""
                )
            )
            VmManager.persist(this)
            finish()
        }
    }
}
