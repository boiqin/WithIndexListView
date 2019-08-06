package com.boiqin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.boiqin.listview.WithIndexExpandableListView
import java.util.*

class ExpandableListViewActivity : AppCompatActivity() {
    private var listView: WithIndexExpandableListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expandablelistview)
        initView()
        initData()
    }

    private fun initView() {
        listView = findViewById(R.id.indexer)
    }

    private fun initData() {
        val charList = ArrayList<String>(26)
        val data = ArrayList<ArrayList<String>>()
        var i = 'A'.toInt()
        while (i <= 'Z'.toInt()) {
            val contact = ArrayList<String>(10)
            for (j in 0..9) {
                contact.add(i.toChar().toString() + (j + 1))
            }
            data.add(contact)
            charList.add(i.toChar().toString())
            i++
        }
        val adapter = MyExpandableListAdapter()
        adapter.setData(charList, data)
        listView?.setWithIndexAdapter(adapter)
    }
}
