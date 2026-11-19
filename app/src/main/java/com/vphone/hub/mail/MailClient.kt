package com.vphone.hub.mail

data class MailMessage(
    val from: String,
    val subject: String,
    val date: Long,
    val preview: String
)

interface MailClient {
    fun fetchUnread(folder: String = "INBOX"): List<MailMessage>
    fun send(to: String, subject: String, body: String)
}

class JavaMailClient(
    private val imapHost: String,
    private val smtpHost: String,
    private val username: String
) : MailClient {
    override fun fetchUnread(folder: String): List<MailMessage> {
        throw UnsupportedOperationException(
            "Требуется Jakarta Mail (com.sun.mail:android-mail). Подключите зависимость и реализуйте IMAP-сессию."
        )
    }

    override fun send(to: String, subject: String, body: String) {
        throw UnsupportedOperationException(
            "Требуется Jakarta Mail. Реализуйте SMTP-сессию и Transport.send()."
        )
    }
}
