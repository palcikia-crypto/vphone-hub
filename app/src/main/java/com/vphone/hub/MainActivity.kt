package com.vphone.hub

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vphone.hub.accounts.AccountsActivity
import com.vphone.hub.ai.ChatActivity
import com.vphone.hub.automations.AutomationsActivity
import com.vphone.hub.mail.MailActivity

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: VmAdapter
    private lateinit var stats: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        VmManager.init(this)

        stats = findViewById(R.id.stats)
        val list = findViewById<RecyclerView>(R.id.vmList)
        adapter = VmAdapter { openDetail(it.id) }
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            startActivity(Intent(this, VmCreateActivity::class.java))
        }
        findViewById<View>(R.id.btnChat).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
        findViewById<View>(R.id.btnAccounts).setOnClickListener {
            startActivity(Intent(this, AccountsActivity::class.java))
        }
        findViewById<View>(R.id.btnAutomations).setOnClickListener {
            startActivity(Intent(this, AutomationsActivity::class.java))
        }
        findViewById<View>(R.id.btnMail).setOnClickListener {
            startActivity(Intent(this, MailActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        adapter.submit(VmManager.vms)
        val running = VmManager.vms.count { it.running }
        stats.text = "ВМ: ${VmManager.vms.size} · запущено: $running\n" +
            "Свободно ОЗУ: ${VmManager.availableRamMb(this)} МБ"
    }

    private fun openDetail(id: String) {
        startActivity(Intent(this, VmDetailActivity::class.java).putExtra("id", id))
    }
}
