package com.jackalopeapps.android.arxiv3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class SearchListWindow extends AppCompatActivity {

    public SearchListWindow thisActivity;

    //UI-Views
    private TextView txtInfo;
    public ListView list;

    private Feed favFeed;
    private String name;
    private String catName;
    private String urlAddress;
    private String urlInput;
    private String query;
    private String[] titles;
    private String[] categories;
    private String[] updatedDates;
    private String[] publishedDates;
    private String[] links;
    private String[] listText;
    private String[] listText2;
    private String[] descriptions;
    private String[] creators;
    private int iFirstResultOnPage = 1;
    private int nResultsPerPage = 30;
    private int numberOfResultsOnPage;
    private int numberOfTotalResults;
    private int fontSize;
    private Boolean vCategory;
    private Boolean vFavorite=false;
    private Boolean vLoaded=false;
    private int version;

    private arXivDB droidDB;

    public static final int INCREASE_ID = Menu.FIRST + 1;
    public static final int DECREASE_ID = Menu.FIRST + 2;
    public static final int FAVORITE_ID = Menu.FIRST + 3;

    private static final Class[] mRemoveAllViewsSignature = new Class[] {
            int.class};
    private static final Class[] mAddViewSignature = new Class[] {
            int.class, RemoteViews.class};
    private static final Class[] mInvalidateOptionsMenuSignature = new Class[] {};
    private Method mRemoveAllViews;
    private Method mAddView;
    private Method mInvalidateOptionsMenu;
    private Object[] mRemoveAllViewsArgs = new Object[1];
    private Object[] mAddViewArgs = new Object[2];
    private Object[] mInvalidateOptionsMenuArgs = new Object[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_search_list_window);
        list = (ListView) findViewById(R.id.mylist);

        version = android.os.Build.VERSION.SDK_INT;

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        query = myIntent.getStringExtra("keyquery");
        urlInput = myIntent.getStringExtra("keyurl");

        getSupportActionBar().setTitle(name);

        urlAddress = "http://export.arxiv.org/api/query?" + query
                + "&sortBy=lastUpdatedDate&sortOrder=descending&start="
                + (iFirstResultOnPage - 1) + "&max_results=" + nResultsPerPage;

        Log.d("arXiv - ", urlAddress);

        if (query.contains("cat:")) {
            vCategory=true;
        } else {
            vCategory=false;
        }

        thisActivity = this;

        txtInfo = (TextView) findViewById(R.id.txt);

        droidDB = new arXivDB(thisActivity);
        fontSize = droidDB.getSize();
        //Log.d("EMD - ","Fontsize "+fontSize);
        if (fontSize == 0) {
            fontSize = 16;
            try {
                droidDB.changeSize(fontSize);
            } catch (Exception ef) {
            }
        }
        //See if this is a favorite
        List<Feed> favorites = droidDB.getFeeds();
        for (Feed feed : favorites) {
            if (query.equals(feed.shortTitle)) {
                favFeed=feed;
                vFavorite=true;
                if (version > 10) {
                    //invalidateOptionsMenu();
                    try {
                        mInvalidateOptionsMenu = Activity.class.getMethod("InvalidateOptionsMenu",
                                mInvalidateOptionsMenuSignature);
                        mInvalidateOptionsMenu.invoke(this, mInvalidateOptionsMenuArgs);
                    } catch (Exception ef) {
                    }
                }
            }
        }
        droidDB.close();

        getInfoFromXML();

    }

    private boolean applyMenuChoice(MenuItem item) {
        switch (item.getItemId()) {
            case INCREASE_ID:
                if (fontSize < 22) {
                    if (fontSize < 10) {
                        fontSize = 10;
                    }
                    fontSize = fontSize + 2;
                    droidDB = new arXivDB(thisActivity);
                    droidDB.changeSize(fontSize);
                    droidDB.close();
                    if (vLoaded) {
                        handlerSetList.sendEmptyMessage(0);
                    }
                }
                return (true);
            case DECREASE_ID:
                if (fontSize > 10) {
                    if (fontSize > 22) {
                        fontSize = 22;
                    }
                    fontSize = fontSize - 2;
                    droidDB = new arXivDB(thisActivity);
                    droidDB.changeSize(fontSize);
                    droidDB.close();
                    if (vLoaded) {
                        handlerSetList.sendEmptyMessage(0);
                    }
                }
                return (true);
            case FAVORITE_ID:
                favoritePressed(null);
                return (true);
        }
        return (false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        populateMenu(menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        populateMenu(menu);
        return (super.onCreateOptionsMenu(menu));
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
    // XXX
    //    Intent myIntent = new Intent(this, SingleItemWindow.class);
    //    myIntent.putExtra("keytitle", titles[position]);
    //    myIntent.putExtra("keylink", links[position]);
    //    myIntent.putExtra("keydescription", descriptions[position]);
    //    myIntent.putExtra("keycreator", creators[position]);
    //    myIntent.putExtra("keyname", name);
    //    startActivity(myIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
    }

    private void populateMenu(Menu menu) {
        menu.add(Menu.NONE, INCREASE_ID, Menu.NONE, "Increase Font");
        menu.add(Menu.NONE, DECREASE_ID, Menu.NONE, "Decrease Font");
        if (!vFavorite) {
            menu.add(Menu.NONE, FAVORITE_ID, Menu.NONE, "Add to Favorites");
        }
    }

    public void favoritePressed(View button) {
        droidDB = new arXivDB(this);
        int unread = -1;
        droidDB.insertFeed(name, query, urlInput, numberOfTotalResults, unread);
        Toast.makeText(this, R.string.added_to_favorites,
                Toast.LENGTH_SHORT).show();
        droidDB.close();
        vFavorite=true;
        if (version > 10) {
            try {
                mInvalidateOptionsMenu = Activity.class.getMethod("InvalidateOptionsMenu",
                        mInvalidateOptionsMenuSignature);
                mInvalidateOptionsMenu.invoke(this, mInvalidateOptionsMenuArgs);
            } catch (Exception ef) {
            }
            //invalidateOptionsMenu();
        }
    }

    class myCustomAdapter extends ArrayAdapter {

        myCustomAdapter() {
            super(SearchListWindow.this, R.layout.searchrow, listText);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            View row=convertView;
            ViewHolder holder;

            if (row==null) {
                LayoutInflater inflater=getLayoutInflater();
                row=inflater.inflate(R.layout.searchrow, parent, false);
                holder=new ViewHolder();
                holder.text1=(TextView)row.findViewById(R.id.text1);
                holder.text2=(TextView)row.findViewById(R.id.text2);
                holder.linLay=(LinearLayout)row.findViewById(R.id.linlay);
                row.setTag(holder);
            } else {
                holder=(ViewHolder)row.getTag();
            }
            try {
                holder.text1.setText(listText[position]);
                holder.text1.setTextSize(fontSize);
                holder.text2.setText(listText2[position]);
                holder.text2.setTextSize(fontSize - 2);
                //XXX Color

                Log.d("arxiv","drawing list item "+position);

                if (position%2 == 0) {
                    holder.linLay.setBackgroundResource(R.color.colorList);
                } else {
                    holder.linLay.setBackgroundResource(R.color.myWhite);
                }
            } catch (Exception ef) {
            }
            return(row);

        }

        public class ViewHolder{
            public TextView text1;
            public TextView text2;
            public LinearLayout linLay;
        }

    }

    private void getInfoFromXML() {

        final ProgressDialog dialog = ProgressDialog.show(this, "",
                getString(R.string.loading), true, true);
        setProgressBarIndeterminateVisibility(true);

        Thread t3 = new Thread() {
            public void run() {

                waiting(200);
                txtInfo.post(new Runnable() {
                    public void run() {
                        txtInfo.setText(R.string.searching);
                    }
                });

                try {

                    URL url = new URL(urlAddress);
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser sp = spf.newSAXParser();
                    XMLReader xr = sp.getXMLReader();
                    XMLHandlerSearch myXMLHandler = new XMLHandlerSearch();
                    xr.setContentHandler(myXMLHandler);
                    xr.parse(new InputSource(url.openStream()));

                    numberOfResultsOnPage = myXMLHandler.numItems;
                    numberOfTotalResults = myXMLHandler.numTotalItems;
                    final int fnmin = iFirstResultOnPage;
                    final int fnmax = iFirstResultOnPage + numberOfResultsOnPage - 1;
                    final int fntotalitems = numberOfTotalResults;


                    txtInfo.post(new Runnable() {
                        public void run() {
                            txtInfo.setText("Showing " + fnmin + " through "
                                    + fnmax + " of " + fntotalitems);
                        }
                    });

                    titles = new String[numberOfResultsOnPage];
                    updatedDates = new String[numberOfResultsOnPage];
                    publishedDates = new String[numberOfResultsOnPage];
                    creators = new String[numberOfResultsOnPage];
                    links = new String[numberOfResultsOnPage];
                    listText = new String[numberOfResultsOnPage];
                    listText2 = new String[numberOfResultsOnPage];
                    descriptions = new String[numberOfResultsOnPage];
                    categories = new String[numberOfResultsOnPage];

                    for (int i = 0; i < numberOfResultsOnPage; i++) {
                        titles[i] = myXMLHandler.titles[i]
                                .replaceAll("\n", " ").replaceAll(" +"," ");
                        creators[i] = myXMLHandler.creators[i];
                        updatedDates[i] = myXMLHandler.updatedDates[i];
                        publishedDates[i] = myXMLHandler.publishedDates[i];
                        categories[i] = myXMLHandler.categories[i];
                        links[i] = myXMLHandler.links[i];
                        descriptions[i] = myXMLHandler.descriptions[i]
                                .replaceAll("\n", " ");
                        ;
                        listText[i] = titles[i];
                        listText2[i] = "";

                        String creatort = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<begin>"
                                + creators[i] + "\n</begin>";
                        try {
                            SAXParserFactory spf2 = SAXParserFactory
                                    .newInstance();
                            SAXParser sp2 = spf2.newSAXParser();
                            XMLReader xr2 = sp2.getXMLReader();
                            XMLHandlerCreator myXMLHandler2 = new XMLHandlerCreator();
                            xr2.setContentHandler(myXMLHandler2);
                            xr2.parse(new InputSource(
                                    new StringReader(creatort)));
                            listText2[i] = listText2[i] + "-Authors: "
                                    + myXMLHandler2.creators[0];
                            for (int j = 1; j < myXMLHandler2.numItems; j++) {
                                listText2[i] = listText2[i] + ", "
                                        + myXMLHandler2.creators[j];
                            }
                        } catch (Exception e) {
                        }
                        if (updatedDates[i].equals(publishedDates[i])) {
                            listText2[i] = listText2[i] + "\n-Published: " + publishedDates[i].replace("T"," ").replace("Z","");
                        } else {
                            listText2[i] = listText2[i] + "\n-Updated: " + updatedDates[i].replace("T"," ").replace("Z","");
                            listText2[i] = listText2[i] + "\n-Published: " + publishedDates[i].replace("T"," ").replace("Z","");
                        }
                        if (!query.contains(categories[i]) && vCategory) {
                            listText2[i] = listText2[i] + "\n-Cross-Ref: "+categories[i];
                        } else if (!vCategory) {
                            listText2[i] = listText2[i] + "\n-Category: "+categories[i];
                        }
                    }

                    if (vFavorite && favFeed.count != numberOfTotalResults && numberOfTotalResults > 0) {
                        try {
                            droidDB = new arXivDB(thisActivity);
                            int unread = 0;
                            droidDB.updateFeed(favFeed.feedId,favFeed.title,favFeed.shortTitle,favFeed.url,numberOfTotalResults,unread);
                            droidDB.close();
                            favFeed.count = numberOfTotalResults;
                        } catch (Exception enf) {
                        }
                    }

                    if (numberOfResultsOnPage > 0) {
                        handlerSetList.sendEmptyMessage(0);
                    }

                    dialog.dismiss();
                    handlerDoneLoading.sendEmptyMessage(0);

                } catch (Exception e) {

                    final Exception ef = e;
                    txtInfo.post(new Runnable() {
                        public void run() {
                            //txtInfo.setText(R.string.couldnt_parse);
                            txtInfo.setText("Error "+ef);
                        }
                    });

                    dialog.dismiss();
                    handlerDoneLoading.sendEmptyMessage(0);

                }
            }
        };
        t3.start();
    }

    private void waiting(int n) {
        long t0, t1;
        t0 = System.currentTimeMillis();
        do {
            t1 = System.currentTimeMillis();
        } while (t1 - t0 < n);
    }

    private Handler handlerSetList = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            list.setAdapter(new myCustomAdapter());

        }
    };

    private Handler handlerDoneLoading = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            vLoaded = true;
            setProgressBarIndeterminateVisibility(false);
        }
    };

}
