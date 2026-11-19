package com.vphone.hub.mail

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vphone.hub.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MailActivity : AppCompatActivity() {
    private lateinit var adapter: MailAdapter
    private val fmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mail)
        val list = findViewById<RecyclerView>(R.id.mailList)
        adapter = MailAdapter(fmt)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        MailConfig.load(this)?.let { cfg ->
            findViewById<EditText>(R.id.mailImap).setText(cfg.imapHost)
            findViewById<EditText>(R.id.mailSmtp).setText(cfg.smtpHost)
            findViewById<EditText>(R.id.mailUser).setText(cfg.username)
            findViewById<EditText>(R.id.mailPass).setText(cfg.password)
        }
        findViewById<View>(R.id.btnSave).setOnClickListener { saveConfig(true) }
        findViewById<View>(R.id.btnInbox).setOnClickListener { fetchInbox() }
        findViewById<View>(R.id.btnCompose).setOnClickListener { sendDialog() }
    }

    private fun status(t: String) {
        findViewById<TextView>(R.id.mailStatus).text = t
    }

    private fun currentConfig(): MailConfig? {
        val imap = findViewById<EditText>(R.id.mailImap).text.toString().trim()
        val smtp = findViewById<EditText>(R.id.mailSmtp).text.toString().trim()
        val user = findViewById<EditText>(R.id.mailUser).text.toString().trim()
        val pass = findViewById<EditText>(R.id.mailPass).text.toString()
        if (imap.isBlank() || smtp.isBlank() || user.isBlank() || pass.isBlank()) {
            status("Заполните серверы, логин и пароль (для Gmail — пароль приложения).")
            return null
        }
        return MailConfig(imap, smtp, user, pass)
    }

    private fun saveConfig(toast: Boolean) {
        val cfg = currentConfig() ?: return
        MailConfig.save(this, cfg)
        if (toast) Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show()
    }

    private fun fetchInbox() {
        val cfg = currentConfig() ?: return
        saveConfig(false)
        status("Подключение к ${cfg.imapHost}…")
        lifecycleScope.launch {
            try {
                val msgs = withContext(Dispatchers.IO) { JavaMailClient(cfg).fetchInbox() }
                adapter.submit(msgs)
                status(if (msgs.isEmpty()) "Входящие пусты." else "Писем: ${msgs.size}")
            } catch (e: Exception) {
                status("Ошибка IMAP: ${e.message ?: e.javaClass.simpleName}")
            }
        }
    }

    private fun sendDialog() {
        val pad = (16 * resources.displayMetrics.density).toInt()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad / 2, pad, 0)
        }
        val etTo = EditText(this).apply { hint = "Кому (email)"; setSingleLine(true) }
        val etSubject = EditText(this).apply { hint = "Тема"; setSingleLine(true) }
        val etBody = EditText(this).apply { hint = "Текст письма"; minLines = 4 }
        listOf(etTo, etSubject, etBody).forEach { layout.addView(it) }
        AlertDialog.Builder(this)
            .setTitle("Новое письмо (SMTP)")
            .setView(layout)
            .setPositiveButton("Отправить") { _, _ ->
                send(etTo.text.toString().trim(), etSubject.text.toString().trim(), etBody.text.toString())
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun send(to: String, subject: String, body: String) {
        if (to.isBlank()) {
            status("Укажите адрес получателя.")
            return
        }
        val cfg = currentConfig() ?: return
        saveConfig(false)
        status("Отправка через ${cfg.smtpHost}…")
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) { JavaMailClient(cfg).send(to, subject, body) }
                status("Отправлено → $to")
            } catch (e: Exception) {
                status("Ошибка SMTP: ${e.message ?: e.javaClass.simpleName}")
            }
        }
    }

    class MailAdapter(private val fmt: SimpleDateFormat) : RecyclerView.Adapter<MailAdapter.Holder>() {
        private val items = mutableListOf<MailMessage>()

        fun submit(list: List<MailMessage>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        class Holder(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.mailTitle)
            val subtitle: TextView = v.findViewById(R.id.mailSubtitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_mail, parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(h: Holder, pos: Int) {
            val m = items[pos]
            val date = if (m.date > 0) " · " + fmt.format(Date(m.date)) else ""
            h.title.text = m.subject.ifBlank { "(без темы)" }
            h.subtitle.text = m.from + date + "\n" + m.preview
        }
    }
}
