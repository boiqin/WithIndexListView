package com.boiqin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.boiqin.listview.WithIndexListView
import java.util.*

class ListViewActivity : AppCompatActivity() {
    private var listView: WithIndexListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listview)
        initView()
        initData()
    }

    private fun initView() {
        listView = findViewById(R.id.listview)
    }

    private fun initData() {
        val data = ArrayList<String>(26)
        var i = 'A'.toInt()
        while (i <= 'Z'.toInt()) {
            data.add(i.toChar().toString())
            i++
        }
        val adapter = MyListAdapter()
        adapter.setData(data)
        listView?.setWithIndexAdapter(adapter)
    }
}
