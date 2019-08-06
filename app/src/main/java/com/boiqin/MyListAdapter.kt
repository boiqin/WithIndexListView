package com.boiqin

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.boiqin.listview.WithIndexListAdapter

class MyListAdapter : WithIndexListAdapter() {
    private lateinit var data: List<String>

    override val indexList: List<String>
        get() = data

    fun setData(data: List<String>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = if (convertView == null) {
            View.inflate(parent?.context, android.R.layout.simple_list_item_1, null) as TextView
        } else {
            convertView as TextView
        }
        view.text = data[position]
        return view
    }


    override fun onIndexSelect(position: Int) {
        // when index selected
        Log.e("", "index selected")
    }
}
