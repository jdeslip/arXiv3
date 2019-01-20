package com.commonsware.android.arXiv;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HistoryWindow extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public ListView list;

    private List<History> historys;
    private arXivDB droidDB;
    public static final int CLEAR_ID = Menu.FIRST + 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_window);

        getSupportActionBar().setTitle("History");

        droidDB = new arXivDB(this);
        historys = droidDB.getHistory();
        droidDB.close();

        List<String> lhistory = new ArrayList<String>();
        for (History history : historys) {
            lhistory.add(history.displayText);
        }

        list = (ListView) findViewById(R.id.list);


        //setListAdapter(new ArrayAdapter<String>(this, R.layout.item,
        //        R.id.label, lhistory));

        list.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, lhistory));

        list.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);

        String filename = "";

        int icount = 0;
        for (History history : historys) {
            if (icount == position) {
                filename = history.url;
            }
            icount++;
        }

        File file = new File(filename);

        intent.setDataAndType(Uri.fromFile(file), "application/pdf");


        Intent myIntent = null;
        myIntent = new Intent();
        myIntent.setAction(android.content.Intent.ACTION_VIEW);
        myIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, file);

        myIntent.setDataAndType(uri, "application/pdf");
        try {
            startActivity(myIntent);
        } catch (ActivityNotFoundException e) {
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        populateMenu(menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
    }

    private void populateMenu(Menu menu) {
        menu.add(Menu.NONE, CLEAR_ID, Menu.NONE, R.string.clear_history);
    }

    private boolean applyMenuChoice(MenuItem item) {
        switch (item.getItemId()) {
            case CLEAR_ID:
                deleteFiles();
                return (true);
        }
        return (false);
    }

    private void deleteFiles() {
        File dir = new File("/sdcard/arXiv");

        String[] children = dir.list();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                String filename = children[i];
                File f = new File("/sdcard/arXiv/" + filename);
                if (f.exists()) {
                    f.delete();
                }
            }
        }

        File dir2 = new File("/emmc/arXiv");
        String[] children2 = dir2.list();
        if (children2 != null) {
            for (int i = 0; i < children2.length; i++) {
                String filename = children2[i];
                File f = new File("/emmc/arXiv/" + filename);
                if (f.exists()) {
                    f.delete();
                }
            }
        }

        dir2 = new File("/media/arXiv");
        children2 = dir2.list();
        if (children2 != null) {
            for (int i = 0; i < children2.length; i++) {
                String filename = children2[i];
                File f = new File("/media/arXiv/" + filename);
                if (f.exists()) {
                    f.delete();
                }
            }
        }

        droidDB = new arXivDB(this);
        historys = droidDB.getHistory();

        for (History history : historys) {
            droidDB.deleteHistory(history.historyId);
        }
        droidDB.close();

        droidDB = new arXivDB(this);
        historys = droidDB.getHistory();
        droidDB.close();

        List<String> lhistory = new ArrayList<String>();
        for (History history : historys) {
            lhistory.add(history.displayText);
        }

        //setListAdapter(new ArrayAdapter<String>(this, R.layout.item,
        //        R.id.label, lhistory));

        list.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, lhistory));

        Toast.makeText(this, R.string.deleted_history, Toast.LENGTH_SHORT).show();
    }

}
