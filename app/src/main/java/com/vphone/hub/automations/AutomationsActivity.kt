package com.vphone.hub.automations

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vphone.hub.R
import java.util.UUID

class AutomationsActivity : AppCompatActivity() {
    private lateinit var adapter: AutomationAdapter
    private lateinit var engine: AutomationEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_automations)
        engine = AutomationEngine(applicationContext)
        engine.start()

        val list = findViewById<RecyclerView>(R.id.automationList)
        adapter = AutomationAdapter()
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        findViewById<FloatingActionButton>(R.id.fabAddAuto).setOnClickListener { addDialog() }
    }

    override fun onResume() {
        super.onResume()
        adapter.submit(AutomationStore.load(this))
    }

    override fun onDestroy() {
        engine.stop()
        super.onDestroy()
    }

    private fun addDialog() {
        val pad = (16 * resources.displayMetrics.density).toInt()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad / 2, pad, 0)
        }
        val etName = EditText(this).apply { hint = "Название"; setSingleLine(true) }
        val etCmd = EditText(this).apply { hint = "shell-команда"; setSingleLine(true) }
        val etMin = EditText(this).apply { hint = "Интервал, минут"; setSingleLine(true); setText("60") }
        listOf(etName, etCmd, etMin).forEach { layout.addView(it) }
        AlertDialog.Builder(this)
            .setTitle("Новая автоматизация")
            .setView(layout)
            .setPositiveButton("Создать") { _, _ ->
                val interval = etMin.text.toString().toLongOrNull() ?: 0L
                if (etCmd.text.toString().isBlank()) {
                    Toast.makeText(this, "Команда не задана", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val list = AutomationStore.load(this)
                list.add(
                    Automation(
                        id = UUID.randomUUID().toString(),
                        name = etName.text.toString().trim().ifBlank { "Автоматизация" },
                        command = etCmd.text.toString().trim(),
                        intervalMin = interval
                    )
                )
                AutomationStore.save(this, list)
                adapter.submit(list)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    class AutomationAdapter : RecyclerView.Adapter<AutomationAdapter.Holder>() {
        private val items = mutableListOf<Automation>()

        fun submit(list: List<Automation>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        class Holder(v: android.view.View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.autoTitle)
            val subtitle: TextView = v.findViewById(R.id.autoSubtitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_automation, parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(h: Holder, pos: Int) {
            val a = items[pos]
            h.title.text = "${a.name} · каждые ${a.intervalMin} мин"
            h.subtitle.text = "$ ${a.command}"
        }
    }
}
