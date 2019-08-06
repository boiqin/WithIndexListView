package com.boiqin

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.boiqin.listview.WithIndexExpandableListAdapter
import java.util.*

class MyExpandableListAdapter : WithIndexExpandableListAdapter() {
    private lateinit var group: List<String>
    private lateinit var data: List<ArrayList<String>>

    override val indexList: List<String>
        get() = group

    fun setData(group: List<String>, data: List<ArrayList<String>>) {
        this.group = group
        this.data = data
        notifyDataSetChanged()
    }

    override fun getGroupCount(): Int {
        return group.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return data[groupPosition].size
    }

    override fun getGroup(groupPosition: Int): Any {
        return data[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        val children = getGroup(groupPosition) as ArrayList<*>

        return children[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
        val view = if (convertView == null) {
            View.inflate(parent.context, android.R.layout.simple_list_item_1, null) as TextView
        } else {
            convertView as TextView
        }

        view.setBackgroundResource(android.R.color.white)
        view.text = group[groupPosition]

        return view
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view = if (convertView == null) {
            View.inflate(parent.context, android.R.layout.simple_list_item_1, null) as TextView
        } else {
            convertView as TextView
        }
        view.text = getChild(groupPosition, childPosition) as String
        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false
    }

    override fun onIndexSelect(position: Int) {
        // when index selected
        Log.e("", "index selected")
    }
}
