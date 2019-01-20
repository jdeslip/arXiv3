package com.commonsware.android.arXiv;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class SingleItemWindow extends AppCompatActivity {

    private LinearLayout linLay;
    private ScrollView scrollView;
    private TextView titleTextView;
    private TextView abstractTextView;
    private TextView fileSizeTextView;

    private String name;
    private String title;
    private String description;
    private String creator;
    private String[] authors;
    private String link;
    private String pdfPath;
    private Boolean vStorage;
    private Boolean vLoop = false;
    private ProgressBar progBar;
    private Activity thisActivity;
    private arXivDB droidDB;
    private int numberOfAuthors;
    private int fontSize;
    private int MY_PERMISSIONS_REQUEST_WES;

    public static final int SHARE_ID = Menu.FIRST + 1;
    public static final int INCREASE_ID = Menu.FIRST + 2;
    public static final int DECREASE_ID = Menu.FIRST + 3;

    private Handler handlerNoViewer = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast
                    .makeText(
                            thisActivity,
                            R.string.install_reader,
                            Toast.LENGTH_SHORT).show();
        }
    };

    private Handler handlerNoStorage = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(thisActivity,
                    R.string.no_storage,
                    Toast.LENGTH_SHORT).show();
        }
    };

    private Handler handlerFailed = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(thisActivity, R.string.download_failed,
                    Toast.LENGTH_SHORT).show();
        }
    };

    private Handler handlerDoneLoading = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setProgressBarIndeterminateVisibility(false);
        }
    };

    private boolean applyMenuChoice(MenuItem item) {
        switch (item.getItemId()) {
            case SHARE_ID:
                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, title + " (arXiv article)");
                i.putExtra(Intent.EXTRA_TEXT, title + " " + link);
                startActivity(Intent.createChooser(i, getString(R.string.share)));
                return (true);
            case INCREASE_ID:
                fontSize = fontSize + 2;
                refreshLinLay();
                droidDB = new arXivDB(thisActivity);
                droidDB.changeSize(fontSize);
                droidDB.close();
                return (true);
            case DECREASE_ID:
                fontSize = fontSize - 2;
                refreshLinLay();
                droidDB = new arXivDB(thisActivity);
                droidDB.changeSize(fontSize);
                droidDB.close();
                return (true);
        }
        return (false);
    }

    public void onClick(View v) {
        int position = v.getId();
        position = position - 1000;

        Intent myIntent = new Intent(this, SearchListWindow.class);
        myIntent.putExtra("keyname", authors[position]);

        String authortext = authors[position].replace("  ", "");
        authortext = authortext.replace(" ", "+").replace("-", "_");
        authortext = "search_query=au:%22" + authortext + "%22";
        // String urlad =
        // "https://export.arxiv.org/api/query?search_query=au:feliciano+giustino&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=20";
        String urlad = "https://export.arxiv.org/api/query?search_query="
                + authortext
                + "&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=20";
        // header.setText(authortext);
        myIntent.putExtra("keyurl", urlad);
        myIntent.putExtra("keyquery", authortext);
        startActivity(myIntent);

    }

    public void authorPressed(View v) {
        int position = v.getId();
        position = position - 1000;

        Intent myIntent = new Intent(this, SearchListWindow.class);
        myIntent.putExtra("keyname", authors[position]);

        String authortext = authors[position].replace("  ", "");
        authortext = authortext.replace(" ", "+").replace("-", "_");
        authortext = "search_query=au:%22" + authortext + "%22";
        // String urlad =
        // "https://export.arxiv.org/api/query?search_query=au:feliciano+giustino&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=20";
        String urlad = "https://export.arxiv.org/api/query?search_query="
                + authortext
                + "&sortBy=lastUpdatedDate&sortOrder=descending&start=0&max_results=20";
        // header.setText(authortext);
        myIntent.putExtra("keyurl", urlad);
        myIntent.putExtra("keyquery", authortext);
        startActivity(myIntent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_item_window);

        getSupportActionBar().setTitle("arXiv Browser");

        Intent myIntent = getIntent();
        name = myIntent.getStringExtra("keyname");
        title = myIntent.getStringExtra("keytitle");
        creator = myIntent.getStringExtra("keycreator");
        description = myIntent.getStringExtra("keydescription");
        link = myIntent.getStringExtra("keylink");
        //link = link.replace("http://", "https://");
        progBar = (ProgressBar) findViewById(R.id.pbar); // Progressbar for
        // download

        fileSizeTextView = (TextView) findViewById(R.id.tsize);

        try {
            description = description.replace("\n", "");
            description = description.replace("<p>", "");
            description = description.replace("</p>", "");
        } catch (Exception ef) {
        }

        titleTextView = new TextView(this);
        abstractTextView = new TextView(this);

        thisActivity = this;

        droidDB = new arXivDB(thisActivity);
        fontSize = droidDB.getSize();
        //Log.d("EMD - ","Fontsize "+fontSize);
        if (fontSize == 0) {
            fontSize = 16;
            droidDB.changeSize(fontSize);
        }
        droidDB.close();

        scrollView = (ScrollView) findViewById(R.id.SV);

        refreshLinLay();

        setProgressBarIndeterminateVisibility(true);
        printSize();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        populateMenu(menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        vLoop = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
    }

    private void populateMenu(Menu menu) {
        menu.add(Menu.NONE, SHARE_ID, Menu.NONE, R.string.share);
        menu.add(Menu.NONE, INCREASE_ID, Menu.NONE, "Increase Font");
        menu.add(Menu.NONE, DECREASE_ID, Menu.NONE, "Decrease Font");
    }

    public void pressedPDFButton(View button) {

        int permissionCheck = ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        Log.d("arXiv - ","Storage permission: "+permissionCheck);

        if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            //if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
            //        Manifest.permission.READ_CONTACTS)) {

            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

            //} else {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WES);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            //}
        } else {

            Thread t = new Thread() {
                public void run() {
                    try {

                        vStorage = false;

                        String storagePath = Environment.getExternalStorageDirectory() + "/arXiv";
                        Log.d("arXiv - ", "Storage path: " + storagePath);

                        File fare = new File(storagePath);
                        boolean success = fare.mkdir();

                        Log.d("arXiv - ", "Storage path: " + success);

                        if (fare.exists()) {
                            pdfPath = storagePath + "/";
                            vStorage = true;
                        } else {
                            File efare = new File("/mnt/sdcard/arXiv");
                            efare.mkdir();
                            if (efare.exists()) {
                                pdfPath = "/mnt/sdcard/arXiv/";
                                vStorage = true;
                            } else {
                                efare = new File("/emmc/arXiv");
                                efare.mkdir();
                                if (efare.exists()) {
                                    pdfPath = "/emmc/arXiv/";
                                    vStorage = true;
                                } else {
                                    efare = new File("/media/arXiv");
                                    efare.mkdir();
                                    if (efare.exists()) {
                                        pdfPath = "/media/arXiv/";
                                        vStorage = true;
                                    }
                                }
                            }
                        }

                        if (vStorage) {

                            vLoop = true;
                            fileSizeTextView.post(new Runnable() {
                                public void run() {
                                    fileSizeTextView.setVisibility(8);
                                }
                            });
                            progBar.post(new Runnable() {
                                public void run() {
                                    progBar.setVisibility(0);
                                }
                            });

                            Log.d("arXiv - ", "PDF url: " + link);

                            String pdfaddress = link.replace("abs", "pdf").replace("http", "https");
                            //String pdfaddress = link.replace("abs", "pdf");

                            Log.d("arXiv - ", "PDF url 2: " + pdfaddress);

                            pdfaddress = pdfaddress + ".pdf";

                            URL u = new URL(pdfaddress);
                            HttpURLConnection c = (HttpURLConnection) u
                                    .openConnection();
                            c.setRequestMethod("GET");
                            //c.setDoOutput(true);
                            c.connect();

                            //if (c.getResponseCode() == 301) {
                            //    // TODO proper handling of HTTP responses and errors
                            //    u = new URL(c.getHeaderField("Location"));
                            //    c.disconnect();
                            //    c = (HttpURLConnection) u.openConnection();
                            //    c.setRequestMethod("GET");
                            //    assert c.getResponseCode() == 200;
                            //}

                            final long ifs = c.getContentLength();
                            InputStream in = c.getInputStream();

                            String filepath = pdfPath;

                            String filename = title.replace(":", "");
                            filename = filename.replace("?", "");
                            filename = filename.replace("*", "");
                            filename = filename.replace("/", "");
                            filename = filename.replace(". ", "");
                            filename = filename.replace("`", "");
                            filename = filename + ".pdf";

                            Boolean vdownload = true;
                            File futureFile = new File(filepath, filename);
                            if (futureFile.exists()) {
                                final long itmp = futureFile.length();
                                if (itmp == ifs && itmp > 301) {
                                    vdownload = false;
                                }
                            }

                            if (vdownload) {
                                FileOutputStream f = new FileOutputStream(
                                        new File(filepath, filename));

                                byte[] buffer = new byte[1024];
                                int len1 = 0;
                                long i = 0;
                                while ((len1 = in.read(buffer)) > 0) {
                                    if (vLoop == false) {
                                        break;
                                    }
                                    f.write(buffer, 0, len1);
                                    i += len1;
                                    long jt = 100 * i / ifs;
                                    final int j = (int) jt;
                                    progBar.post(new Runnable() {
                                        public void run() {
                                            progBar.setProgress(j);
                                        }
                                    });
                                }
                                f.close();
                            } else {
                                progBar.post(new Runnable() {
                                    public void run() {
                                        progBar.setProgress(100);
                                    }
                                });
                            }

                            if (vLoop) {
                                if (vdownload) {
                                    droidDB = new arXivDB(thisActivity);
                                    String displaytext = title;
                                    for (int i = 0; i < numberOfAuthors; i++) {
                                        displaytext = displaytext + " - "
                                                + authors[i];
                                    }
                                    droidDB.insertHistory(displaytext, filepath
                                            + filename);
                                    droidDB.close();
                                }

                                final File file = new File(filepath + filename);

                                Intent myIntent = null;
                                myIntent = new Intent();
                                myIntent.setAction(android.content.Intent.ACTION_VIEW);
                                myIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                Uri uri = FileProvider.getUriForFile(thisActivity, BuildConfig.APPLICATION_ID, file);

                                myIntent.setDataAndType(uri, "application/pdf");
                                try {
                                    startActivity(myIntent);
                                } catch (ActivityNotFoundException e) {
                                    handlerNoViewer.sendEmptyMessage(0);
                                }

                            } else {
                                File fd = new File(filepath, filename);
                                fd.delete();
                            }

                        } else {
                            handlerNoStorage.sendEmptyMessage(0);
                        }
                    } catch(Exception e) {
                        Log.d("arxiv", "error " + e);
                        e.printStackTrace();
                        handlerFailed.sendEmptyMessage(0);
                    }
                };
            };
            t.start();
        }

    }

    private void printSize() {
        Thread t4 = new Thread() {
            public void run() {

                try {
                    String pdfaddress = link.replace("abs", "pdf").replace("http","https");
                    //String pdfaddress = link.replace("abs", "pdf");

                    pdfaddress = pdfaddress + ".pdf";

                    URL u = new URL(pdfaddress);
                    HttpURLConnection c = (HttpURLConnection) u
                            .openConnection();
                    c.setRequestMethod("GET");
                    //c.setDoOutput(true);
                    c.connect();

                    final long ifs = c.getContentLength();
                    c.disconnect();
                    final long jfs = ifs * 100 / 1024 / 1024;
                    final double rfs = (double) jfs / 100.0;
                    fileSizeTextView.post(new Runnable() {
                        public void run() {
                            fileSizeTextView.setText("Size: " + rfs + " MB");
                        }
                    });
                } catch (Exception e) {
                }
                handlerDoneLoading.sendEmptyMessage(0);
            }
        };
        t4.start();
    }

    public void refreshLinLay() {

        titleTextView.setText(title);
        titleTextView.setTextSize(fontSize);
        titleTextView.setPadding(5, 5, 5, 5);
        //titleTextView.setTextColor(0xffffffff);

        try {
            linLay.removeAllViews();
        } catch (Exception e) {
        }

        linLay = new LinearLayout(this);
        //linLay.setOrientation(1);
        linLay.addView(titleTextView);

        abstractTextView.setText("Abstract: " + description);
        abstractTextView.setPadding(5, 5, 5, 5);
        abstractTextView.setTextSize(fontSize);
        //abstractTextView.setTextColor(0xffffffff);

        // The Below Gets the Authors Names
        String creatort = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<begin>"
                + creator + "\n</begin>";
        try {

            Resources res = getResources();

            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            XMLHandlerCreator myXMLHandler = new XMLHandlerCreator();
            xr.setContentHandler(myXMLHandler);
            xr.parse(new InputSource(new StringReader(creatort)));
            authors = new String[myXMLHandler.numItems];
            numberOfAuthors = myXMLHandler.numItems;
            for (int i = 0; i < myXMLHandler.numItems; i++) {
                authors[i] = myXMLHandler.creators[i] + "  ";

                LinearLayout authorLL = (LinearLayout) LayoutInflater.from(thisActivity).inflate(R.layout.author, null);

                TextView temptv = (TextView) authorLL.findViewById(R.id.authortv);
                temptv.setText("   " + authors[i]);
                authorLL.setClickable(true);
                authorLL.setFocusable(true);

                authorLL.setBackgroundDrawable(
                            res.getDrawable(android.R.drawable.list_selector_background));

                authorLL.setId(i + 1000);
                temptv.setTextSize(fontSize);
                linLay.addView(authorLL);
                View rulerin = new View(this);
                View blankview1 = new View(this);
                View blankview2 = new View(this);
                rulerin.setBackgroundColor(0xFF3f3b3b);
                //blankview.setPadding(5,5,5,5);
                linLay.addView(blankview1, new ViewGroup.LayoutParams(320, 2));
                linLay.addView(rulerin, new ViewGroup.LayoutParams(320, 1));
                linLay.addView(blankview2, new ViewGroup.LayoutParams(320, 2));
            }

        } catch (Exception e) {
            authors = new String[0];
        }

        linLay.addView(abstractTextView);

        try {
            scrollView.removeAllViews();
        } catch (Exception e) {
        }
        scrollView.addView(linLay);
    }

}
