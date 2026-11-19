package com.vphone.hub.mail

import android.content.Context
import java.util.Date
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Folder
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

data class MailMessage(
    val from: String,
    val subject: String,
    val date: Long,
    val preview: String
)

interface MailClient {
    fun fetchInbox(folder: String = "INBOX", limit: Int = 20): List<MailMessage>
    fun send(to: String, subject: String, body: String)
}

data class MailConfig(
    val imapHost: String,
    val smtpHost: String,
    val username: String,
    val password: String
) {
    companion object {
        private const val PREFS = "mail_config"

        fun load(ctx: Context): MailConfig? {
            val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val user = p.getString("username", null) ?: return null
            return MailConfig(
                imapHost = p.getString("imap", "") ?: "",
                smtpHost = p.getString("smtp", "") ?: "",
                username = user,
                password = p.getString("password", "") ?: ""
            )
        }

        fun save(ctx: Context, cfg: MailConfig) {
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString("imap", cfg.imapHost)
                .putString("smtp", cfg.smtpHost)
                .putString("username", cfg.username)
                .putString("password", cfg.password)
                .apply()
        }
    }
}

class JavaMailClient(private val cfg: MailConfig) : MailClient {

    override fun fetchInbox(folder: String, limit: Int): List<MailMessage> {
        val props = Properties().apply {
            put("mail.store.protocol", "imaps")
            put("mail.imaps.connectiontimeout", "15000")
            put("mail.imaps.timeout", "20000")
        }
        val session = Session.getInstance(props, null)
        val store = session.getStore("imaps")
        store.connect(cfg.imapHost, cfg.username, cfg.password)
        try {
            val fd = store.getFolder(folder)
            fd.open(Folder.READ_ONLY)
            try {
                val msgs = fd.messages.takeLast(limit).reversed()
                return msgs.map { m ->
                    MailMessage(
                        from = m.from?.firstOrNull()?.toString() ?: "?",
                        subject = m.subject ?: "",
                        date = m.sentDate?.time ?: 0L,
                        preview = preview(m)
                    )
                }
            } finally {
                fd.close(false)
            }
        } finally {
            store.close()
        }
    }

    override fun send(to: String, subject: String, body: String) {
        val props = Properties().apply {
            put("mail.transport.protocol", "smtp")
            put("mail.smtp.host", cfg.smtpHost)
            put("mail.smtp.port", "587")
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.connectiontimeout", "15000")
            put("mail.smtp.timeout", "20000")
        }
        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() =
                PasswordAuthentication(cfg.username, cfg.password)
        })
        val msg = MimeMessage(session).apply {
            setFrom(InternetAddress(cfg.username))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false))
            setSubject(subject)
            setText(body)
            sentDate = Date()
        }
        Transport.send(msg)
    }

    private fun preview(m: Message): String {
        val content = try {
            m.content
        } catch (e: Exception) {
            return ""
        }
        return when (content) {
            is String -> content.take(160)
            else -> "(составное письмо / вложения)"
        }
    }
}
