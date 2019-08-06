package com.boiqin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setListener()
    }

    private fun setListener() {
        findViewById<Button>(R.id.btn_lv).setOnClickListener {
            val intent = Intent()
            intent.setClass(this, ListViewActivity::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.btn_elv).setOnClickListener {
            val intent = Intent()
            intent.setClass(this, ExpandableListViewActivity::class.java)
            startActivity(intent)
        }
    }
}
