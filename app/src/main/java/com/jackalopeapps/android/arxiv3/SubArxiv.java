package com.jackalopeapps.android.arxiv3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.lang.reflect.Method;

public class SubArxiv extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public Context thisActivity;

    //UI-Views
    //XXX probably can delete
    private android.support.v7.widget.Toolbar headerText;
    public ListView list;

    private String name;
    private String[] items;
    private String[] urls;
    private String[] shortItems;

    private static final Class[] mRemoveAllViewsSignature = new Class[] {
            int.class};
    private static final Class[] mAddViewSignature = new Class[] {
            int.class, RemoteViews.class};
    private Method mRemoveAllViews;
    private Method mAddView;
    private Object[] mRemoveAllViewsArgs = new Object[1];
    private Object[] mAddViewArgs = new Object[2];
    private int mySourcePref =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_arxiv);

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        urls = myIntent.getStringArrayExtra("keyurls");
        items = myIntent.getStringArrayExtra("keyitems");
        shortItems = myIntent.getStringArrayExtra("keyshortitems");

        list = (ListView) findViewById(R.id.listsm);

        thisActivity = this;

        //XXX Need to fix
        //getActionBar().setTitle(name);
        getSupportActionBar().setTitle(name);

        list.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items));

        list.setOnItemClickListener(this);
        registerForContextMenu(list);

        //SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        //mySourcePref=Integer.parseInt(prefs.getString("sourcelist", "0"));

    }

    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, 1000, 0, R.string.add_favorites);
    }

    public void onItemClick(AdapterView<?> a, View v, int position, long id) {

        // XXX
        if (mySourcePref == 0) {
            Intent myIntent = new Intent(this, SearchListWindow.class);
            myIntent.putExtra("keyname", shortItems[position]);
            String tempquery = "search_query=cat:" + urls[position];
            if (position == 0) {
                tempquery = tempquery + "*";
            }
            myIntent.putExtra("keyquery", tempquery);
            String tempurl = "https://export.arxiv.org/api/query?" + tempquery
                    + "&sortBy=submittedDate&sortOrder=ascending";
            myIntent.putExtra("keyurl", tempurl);
            startActivity(myIntent);
        //} else {
        //    Intent myIntent = new Intent(this, RSSListWindow.class);
        //    myIntent.putExtra("keyname", shortItems[position]);
        //    myIntent.putExtra("keyurl", urls[position]);
        //    startActivity(myIntent);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            return false;
        }

        arXivDB droidDB = new arXivDB(this);

        if (mySourcePref == 0) {
            String tempquery = "search_query=cat:" + urls[info.position];
            if (info.position == 0) {
                tempquery = tempquery + "*";
            }
            String tempurl = "https://export.arxiv.org/api/query?" + tempquery
                    + "&sortBy=submittedDate&sortOrder=ascending";
            droidDB.insertFeed(shortItems[info.position],
                    tempquery, tempurl, -1,-1);
        //} else {
        //    String tempquery = urls[info.position];
        //    String tempurl = tempquery;
        //    droidDB.insertFeed(shortItems[info.position]+" (RSS)", shortItems[info.position], tempurl,-2,-2);
        //    Toast.makeText(this, R.string.added_to_favorites_rss,
        //            Toast.LENGTH_SHORT).show();
        }
        droidDB.close();

        return true;
    }
}
