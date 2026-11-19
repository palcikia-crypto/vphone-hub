package com.vphone.hub.accounts

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vphone.hub.R

class AccountsActivity : AppCompatActivity() {
    private lateinit var adapter: AccountAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accounts)
        val list = findViewById<RecyclerView>(R.id.accountList)
        adapter = AccountAdapter()
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        findViewById<FloatingActionButton>(R.id.fabAddAcc).setOnClickListener { addDialog() }
    }

    override fun onResume() {
        super.onResume()
        adapter.submit(AccountStore.load(this))
    }

    private fun addDialog() {
        val pad = (16 * resources.displayMetrics.density).toInt()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad / 2, pad, 0)
        }
        val etType = EditText(this).apply { hint = "Тип (email / соцсеть / другое)"; setSingleLine(true) }
        val etLabel = EditText(this).apply { hint = "Название"; setSingleLine(true) }
        val etLogin = EditText(this).apply { hint = "Логин / email"; setSingleLine(true) }
        listOf(etType, etLabel, etLogin).forEach { layout.addView(it) }
        AlertDialog.Builder(this)
            .setTitle("Подключить аккаунт")
            .setView(layout)
            .setPositiveButton("Добавить") { _, _ ->
                val accounts = AccountStore.load(this)
                accounts.add(
                    Account(
                        type = etType.text.toString().trim().ifBlank { "account" },
                        label = etLabel.text.toString().trim().ifBlank { "Без имени" },
                        login = etLogin.text.toString().trim()
                    )
                )
                AccountStore.save(this, accounts)
                adapter.submit(accounts)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    class AccountAdapter : RecyclerView.Adapter<AccountAdapter.Holder>() {
        private val items = mutableListOf<Account>()

        fun submit(list: List<Account>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        class Holder(v: android.view.View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.accTitle)
            val subtitle: TextView = v.findViewById(R.id.accSubtitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(h: Holder, pos: Int) {
            val a = items[pos]
            h.title.text = "${a.label} (${a.type})"
            h.subtitle.text = a.login
        }
    }
}
