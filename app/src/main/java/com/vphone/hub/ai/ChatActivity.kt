package com.vphone.hub.ai

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.vphone.hub.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {
    private val history = mutableListOf<Pair<String, String>>(
        "system" to
            "Ты — встроенный ИИ-ассистент виртуального телефона VPhone Hub. " +
            "Помогай с кодом, автоматизациями, настройкой окружения и почты. Отвечай кратко."
    )
    private val visible = mutableListOf<String>()
    private lateinit var adapter: ChatAdapter
    private lateinit var input: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        input = findViewById(R.id.input)
        adapter = ChatAdapter()
        val list = findViewById<RecyclerView>(R.id.messages)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        findViewById<MaterialButton>(R.id.btnSend).setOnClickListener { send() }
        findViewById<MaterialButton>(R.id.btnSettings).setOnClickListener { showSettings() }
    }

    private fun showSettings() {
        val (baseUrl, apiKey, model) = AiClient.loadConfig(this)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad / 2, pad, 0)
        }
        val etBase = EditText(this).apply { hint = "Base URL"; setSingleLine(true); setText(baseUrl) }
        val etKey = EditText(this).apply { hint = "API-ключ"; setSingleLine(true); setText(apiKey) }
        val etModel = EditText(this).apply { hint = "Модель"; setSingleLine(true); setText(model) }
        listOf(etBase, etKey, etModel).forEach { layout.addView(it) }
        AlertDialog.Builder(this)
            .setTitle("Настройки ИИ")
            .setView(layout)
            .setPositiveButton("Сохранить") { _, _ ->
                AiClient.saveConfig(
                    this,
                    etBase.text.toString().trim().ifBlank { AiClient.DEFAULT_BASE_URL },
                    etKey.text.toString().trim(),
                    etModel.text.toString().trim().ifBlank { AiClient.DEFAULT_MODEL }
                )
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun send() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) return
        input.setText("")
        history.add("user" to text)
        visible.add("Вы: $text")
        adapter.submit(visible)

        val (_, apiKey, _) = AiClient.loadConfig(this)
        if (apiKey.isBlank()) {
            visible.add("ИИ: задайте API-ключ через ⚙ Настройки (OpenRouter или любой OpenAI-совместимый эндпоинт)")
            adapter.submit(visible)
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val reply = try {
                val (base, key, model) = AiClient.loadConfig(this@ChatActivity)
                AiClient(base, key, model).chat(history)
            } catch (e: Exception) {
                "Ошибка ИИ: ${e.message}"
            }
            withContext(Dispatchers.Main) {
                history.add("assistant" to reply)
                visible.add("ИИ: $reply")
                adapter.submit(visible)
            }
        }
    }

    class ChatAdapter : RecyclerView.Adapter<ChatAdapter.Holder>() {
        private val items = mutableListOf<String>()

        fun submit(list: List<String>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        class Holder(v: android.view.View) : RecyclerView.ViewHolder(v) {
            val text: TextView = v.findViewById(R.id.chatText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(h: Holder, pos: Int) {
            h.text.text = items[pos]
        }
    }
}
