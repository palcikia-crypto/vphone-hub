package com.vphone.hub

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TerminalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terminal)
        val scroll = findViewById<ScrollView>(R.id.scroll)
        val output = findViewById<TextView>(R.id.output)
        val input = findViewById<EditText>(R.id.input)

        findViewById<MaterialButton>(R.id.btnRun).setOnClickListener {
            val cmd = input.text.toString().trim()
            if (cmd.isEmpty()) return@setOnClickListener
            input.setText("")
            lifecycleScope.launch(Dispatchers.IO) {
                val result = try {
                    val p = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
                    val stdout = p.inputStream.bufferedReader().readText()
                    val stderr = p.errorStream.bufferedReader().readText()
                    p.waitFor()
                    (stdout + stderr).ifBlank { "(нет вывода)" }
                } catch (e: Exception) {
                    "Ошибка: ${e.message}"
                }
                withContext(Dispatchers.Main) {
                    val block = if (result.endsWith("\n")) "\n$ $cmd\n$result" else "\n$ $cmd\n$result\n"
                    output.append(block)
                    scroll.fullScroll(View.FOCUS_DOWN)
                }
            }
        }
    }
}
