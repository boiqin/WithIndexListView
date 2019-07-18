package com.boji.withindexlistview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.boji.listview.WithIndexExpandableListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WithIndexExpandableListView listView ;
    private MyAdapter adapter = new MyAdapter();
    private ArrayList<String> charactes ;
    private List<ArrayList<String>> data ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData() ;
    }

    private void initView() {
        listView = findViewById(R.id.indexer);
    }

    private void initData() {
        charactes = new ArrayList<>(26) ;
        data = new ArrayList<>() ;
        char c = 'A' ;
        for (int i=c;i<='Z';i++) {
            ArrayList<String> contact = new ArrayList<>(10) ;
            for (int j=0;j<10;j++) {
                contact.add(String.valueOf((char)i) + (j + 1)) ;
            }
            data.add(contact) ;
            charactes.add(String.valueOf((char)i)) ;
        }

        adapter.setData(charactes,data);
        listView.setWithIndexAdapter(adapter);
    }
}
