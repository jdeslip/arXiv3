package com.commonsware.android.arXiv;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.ArrayList;
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
    private ArrayList<String> titles;
    private ArrayList<String> categories;
    private ArrayList<String> updatedDates;
    private ArrayList<String> publishedDates;
    private ArrayList<String> links;
    private ArrayList<String> listText;
    private ArrayList<String> listText2;
    private ArrayList<String> descriptions;
    private ArrayList<String> creators;
    private int iFirstResultOnPage = 1;
    private int nResultsPerPage = 30;
    private int numberOfResultsOnPage;
    private int numberOfTotalResults;
    private int fontSize;
    private Boolean vListNotSet = true;
    private Boolean vDone = false;
    private Boolean vCategory;
    private Boolean vFavorite=false;
    private Boolean vLoaded=false;
    private int version;
    private myCustomAdapter adapter;

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

        urlAddress = "https://export.arxiv.org/api/query?" + query
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
        listText = new ArrayList<String>();
        listText2 = new ArrayList<String>();
        titles = new ArrayList<String>();
        updatedDates = new ArrayList<String>();
        publishedDates = new ArrayList<String>();
        creators = new ArrayList<String>();
        links = new ArrayList<String>();
        descriptions = new ArrayList<String>();
        categories = new ArrayList<String>();

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
                holder.text1.setText(listText.get(position));
                holder.text1.setTextSize(fontSize);
                holder.text2.setText(listText2.get(position));
                holder.text2.setTextSize(fontSize - 2);
                //XXX Color

                //Log.d("arxiv","drawing list item "+position);

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



                    if (fnmax == fntotalitems) {
                        vDone = true;
                        Log.d("arxiv", "I set vDone to True");
                    }

                    txtInfo.post(new Runnable() {
                        public void run() {
                            txtInfo.setText("Showing 1 through "
                                    + fnmax + " of " + fntotalitems);
                        }
                    });

                    Log.d("arxiv", "begining loop, fnmin is "+fnmin+" numberOfTotalResults is "+numberOfTotalResults);

                    for (int i = 0; i < numberOfResultsOnPage; i++) {
                        //Log.d("arxiv", "starting "+i);
                        titles.add(myXMLHandler.titles[i]
                                .replaceAll("\n", " ").replaceAll(" +"," "));
                        creators.add(myXMLHandler.creators[i]);
                        updatedDates.add(myXMLHandler.updatedDates[i]);
                        publishedDates.add(myXMLHandler.publishedDates[i]);
                        categories.add(myXMLHandler.categories[i]);
                        links.add(myXMLHandler.links[i]);
                        descriptions.add(myXMLHandler.descriptions[i]
                                .replaceAll("\n", " "));
                        ;
                        listText.add(titles.get(fnmin-1+i));
                        String listText2i = "";
                        String creatort = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<begin>"
                                + creators.get(fnmin-1+i) + "\n</begin>";
                        //Log.d("arxiv", "starting 2 "+i);

                        try {
                            SAXParserFactory spf2 = SAXParserFactory
                                    .newInstance();
                            SAXParser sp2 = spf2.newSAXParser();
                            XMLReader xr2 = sp2.getXMLReader();
                            XMLHandlerCreator myXMLHandler2 = new XMLHandlerCreator();
                            xr2.setContentHandler(myXMLHandler2);
                            xr2.parse(new InputSource(
                                    new StringReader(creatort)));
                            listText2i = listText2i + "-Authors: "
                                    + myXMLHandler2.creators[0];
                            for (int j = 1; j < myXMLHandler2.numItems; j++) {
                                listText2i = listText2i + ", "
                                        + myXMLHandler2.creators[j];
                            }
                        } catch (Exception e) {
                        }
                        //Log.d("arxiv", "starting 3 "+i);

                        if (updatedDates.get(fnmin-1+i).equals(publishedDates.get(fnmin-1+i))) {
                            listText2i = listText2i + "\n-Published: " + publishedDates.get(fnmin-1+i).replace("T"," ").replace("Z","");
                        } else {
                            listText2i = listText2i + "\n-Updated: " + updatedDates.get(fnmin-1+i).replace("T"," ").replace("Z","");
                            listText2i = listText2i + "\n-Published: " + publishedDates.get(fnmin-1+i).replace("T"," ").replace("Z","");
                        }
                        if (!query.contains(categories.get(fnmin-1+i)) && vCategory) {
                            listText2i = listText2i + "\n-Cross-Ref: "+categories.get(fnmin-1+i);
                        } else if (!vCategory) {
                            listText2i = listText2i + "\n-Category: "+categories.get(fnmin-1+i);
                        }
                        //Log.d("arxiv", "starting 4 "+i);

                        listText2.add(listText2i);
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
                            Log.e("arxiv", "Error!! "+ef+" "+Log.getStackTraceString(ef));

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

            if (vListNotSet) {
                adapter = new myCustomAdapter();
                list.setAdapter(adapter);
                list.setOnItemClickListener(
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                Intent myIntent = new Intent(v.getContext(), SingleItemWindow.class);
                                myIntent.putExtra("keytitle", titles.get(position));
                                myIntent.putExtra("keylink", links.get(position));
                                myIntent.putExtra("keydescription", descriptions.get(position));
                                myIntent.putExtra("keycreator", creators.get(position));
                                myIntent.putExtra("keyname", name);
                                startActivity(myIntent);
                            }
                        }
                );
                list.setOnScrollListener(new AbsListView.OnScrollListener() {
                    private int currentVisibleItemCount;
                    private int currentScrollState;
                    private int currentFirstVisibleItem;
                    private int totalItem;
                    private LinearLayout lBelow;


                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        // TODO Auto-generated method stub
                        this.currentScrollState = scrollState;
                        this.isScrollCompleted();
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem,
                                         int visibleItemCount, int totalItemCount) {
                        // TODO Auto-generated method stub
                        this.currentFirstVisibleItem = firstVisibleItem;
                        this.currentVisibleItemCount = visibleItemCount;
                        this.totalItem = totalItemCount;


                    }

                    private void isScrollCompleted() {
                        if (totalItem - currentFirstVisibleItem == currentVisibleItemCount
                                && this.currentScrollState == SCROLL_STATE_IDLE) {
                            /** To do code here*/
                            nextArticles();
                        }
                    }
                });
                vListNotSet = false;
            } else {
                adapter.notifyDataSetChanged();
            }

        }
    };

    private Handler handlerDoneLoading = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            vLoaded = true;
            setProgressBarIndeterminateVisibility(false);
        }
    };

    public void nextArticles() {
        Log.d("arxiv", "Calling nextArticles");
        if (!vDone) {
            iFirstResultOnPage = iFirstResultOnPage + nResultsPerPage;
            urlAddress = "https://export.arxiv.org/api/query?" + query
                    + "&sortBy=lastUpdatedDate&sortOrder=descending&start="
                    + (iFirstResultOnPage - 1) + "&max_results=" + nResultsPerPage;
            //Toast.makeText(thisActivity, "Loading More Results",
            //        Toast.LENGTH_SHORT).show();
            getInfoFromXML();
        }
    }

}
