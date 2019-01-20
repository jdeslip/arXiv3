package com.commonsware.android.arXiv;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    public static AdapterView.OnItemClickListener thisActivityListener;

    private static ListView catList;
    private static ListView favList;

    private arXivDB droidDB;
    private int vFlag = 1;
    private static List<History> historys;
    private static List<Feed> favorites;
    private static String[] unreadList;
    private static String[] favoritesList;

    private int mySourcePref = 0;

    static String[] items = { "Astrophysics", "Condensed Matter", "Computer Science",
            "General Relativity", "HEP Experiment", "HEP Lattice",
            "HEP Phenomenology", "HEP Theory", "Mathematics",
            "Mathematical Physics", "Misc Physics", "Nonlinear Sciences",
            "Nuclear Experiment", "Nuclear Theory", "Quantitative Biology",
            "Quantitative Finance", "Quantum Physics", "Statistics" };

    int[] itemsFlag = { 1, 2, 3, 0, 0, 0, 0, 0, 4, 0, 5, 6, 0, 0, 7, 8, 0, 9 };

    String[] shortItems = { "Astrophysics", "Condensed Matter",
            "Computer Science", "General Relativity", "HEP Experiment",
            "HEP Lattice", "HEP Phenomenology", "HEP Theory", "Mathematics",
            "Math. Physics", "Misc Physics", "Nonlinear Sci.", "Nuclear Exp.",
            "Nuclear Theory", "Quant. Biology", "Quant. Finance",
            "Quantum Physics", "Statistics" };

    String[] urls = { "astro-ph", "cond-mat", "cs", "gr-qc", "hep-ex",
            "hep-lat", "hep-ph", "hep-th", "math", "math-ph", "physics",
            "nlin", "nucl-ex", "nucl-th", "q-bio", "q-fin", "quant-ph", "stat" };

    String[] asItems = { "Astrophysics All",
            "Cosmology and Extragalactic Astrophysics",
            "Earth & Planetary Astrophysics", "Galaxy Astrophysics",
            "HE Astrophysical Phenomena",
            "Instrumentation and Methods for Astrophysics",
            "Solar and Stellar Astrophysics" };

    String[] asURLs = { "astro-ph", "astro-ph.CO", "astro-ph.EP",
            "astro-ph.GA", "astro-ph.HE", "astro-ph.IM", "astro-ph.SR" };

    String[] asShortItems = { "Astrophysics All",
            "Cosm. & Ext-Gal. Astrophysics", "Earth & Planetary Astrophysics",
            "Galaxy Astrophysics", "HE Astrophysical Phenomena",
            "Instrumentation and Methods for Astrophysics",
            "Solar and Stellar Astrophysics" };

    String[] cmItems = { "Condensed Matter All",
            "Disordered Systems and Neural Networks", "Materials Science",
            "Mesoscale and Nanoscale Physics", "Other Condensed Matter",
            "Quantum Gases", "Soft Condensed Matter", "Statistical Mechanics",
            "Strongly Correlated Electrons", "Superconductivity" };

    String[] cmURLs = { "cond-mat", "cond-mat.dis-nn", "cond-mat.mtrl-sci",
            "cond-mat.mes-hall", "cond-mat.other", "cond-mat.quant-gas",
            "cond-mat.soft", "cond-mat.stat-mech", "cond-mat.str-el",
            "cond-mat.supr-con" };

    String[] cmShortItems = { "Cond. Matter All",
            "Disord. Systems & Neural Networks", "Materials Science",
            "Mesoscale and Nanoscale Physics", "Other Condensed Matter",
            "Quantum Gases", "Soft Condensed Matter", "Statistical Mechanics",
            "Strongly Correlated Electrons", "Superconductivity" };

    String[] csItems = { "Computer Science All", "Architecture",
            "Artificial Intelligence", "Computation and Language",
            "Computational Complexity",
            "Computational Engineering, Finance and Science",
            "Computational Geometry", "CS and Game Theory",
            "Computer Vision and Pattern Recognition", "Computers and Society",
            "Cryptography and Security", "Data Structures and Algorithms",
            "Databases", "Digital Libraries", "Discrete Mathematics",
            "Distributed, Parallel, and Cluster Computing",
            "Formal Languages and Automata Theory", "General Literature",
            "Graphics", "Human-Computer Interaction", "Information Retrieval",
            "Information Theory", "Learning", "Logic in Computer Science",
            "Mathematical Software", "Multiagent Systems", "Multimedia",
            "Networking and Internet Architecture",
            "Neural and Evolutionary Computing", "Numerical Analysis",
            "Operating Systems", "Other Computer Science", "Performance",
            "Programming Languages", "Robotics", "Software Engineering",
            "Sound", "Symbolic Computation" };

    String[] csURLs = { "cs", "cs.AR", "cs.AI", "cs.CL", "cs.CC", "cs.CE",
            "cs.CG", "cs.GT", "cs.CV", "cs.CY", "cs.CR", "cs.DS", "cs.DB",
            "cs.DL", "cs.DM", "cs.DC", "cs.FL", "cs.GL", "cs.GR", "cs.HC",
            "cs.IR", "cs.IT", "cs.LG", "cs.LO", "cs.MS", "cs.MA", "cs.MM",
            "cs.NI", "cs.NE", "cs.NA", "cs.OS", "cs.OH", "cs.PF", "cs.PL",
            "cs.RO", "cs.SE", "cs.SD", "cs.SC" };

    String[] csShortItems = { "Computer Science All", "Architecture",
            "Artificial Intelligence", "Computation and Language",
            "Computational Complexity",
            "Comp. Eng., Fin. & Science",
            "Computational Geometry", "CS and Game Theory",
            "Computer Vision and Pattern Recognition", "Computers and Society",
            "Cryptography and Security", "Data Structures and Algorithms",
            "Databases", "Digital Libraries", "Discrete Mathematics",
            "Distributed, Parallel, and Cluster Computing",
            "Formal Languages and Automata Theory", "General Literature",
            "Graphics", "Human-Computer Interaction", "Information Retrieval",
            "Information Theory", "Learning", "Logic in Computer Science",
            "Mathematical Software", "Multiagent Systems", "Multimedia",
            "Networking and Internet Architecture",
            "Neural and Evolutionary Computing", "Numerical Analysis",
            "Operating Systems", "Other Computer Science", "Performance",
            "Programming Languages", "Robotics", "Software Engineering",
            "Sound", "Symbolic Computation" };

    String[] mtItems = { "Math All", "Algebraic Geometry",
            "Algebraic Topology", "Analysis of PDEs", "Category Theory",
            "Classical Analysis of ODEs", "Combinatorics",
            "Commutative Algebra", "Complex Variables",
            "Differential Geometry", "Dynamical Systems",
            "Functional Analysis", "General Mathematics", "General Topology",
            "Geometric Topology", "Group Theory", "Math History and Overview",
            "Information Theory", "K-Theory and Homology", "Logic",
            "Mathematical Physics", "Metric Geometry", "Number Theory",
            "Numerical Analysis", "Operator Algebras",
            "Optimization and Control", "Probability", "Quantum Algebra",
            "Representation Theory", "Rings and Algebras", "Spectral Theory",
            "Statistics (Math)", "Symplectic Geometry" };

    String[] mtURLs = { "math", "math.AG", "math.AT", "math.AP", "math.CT",
            "math.CA", "math.CO", "math.AC", "math.CV", "math.DG", "math.DS",
            "math.FA", "math.GM", "math.GN", "math.GT", "math.GR", "math.HO",
            "math.IT", "math.KT", "math.LO", "math.MP", "math.MG", "math.NT",
            "math.NA", "math.OA", "math.OC", "math.PR", "math.QA", "math.RT",
            "math.RA", "math.SP", "math.ST", "math.SG" };

    String[] mtShortItems = { "Math All", "Algebraic Geometry",
            "Algebraic Topology", "Analysis of PDEs", "Category Theory",
            "Classical Analysis of ODEs", "Combinatorics",
            "Commutative Algebra", "Complex Variables",
            "Differential Geometry", "Dynamical Systems",
            "Functional Analysis", "General Mathematics", "General Topology",
            "Geometric Topology", "Group Theory", "Math History and Overview",
            "Information Theory", "K-Theory and Homology", "Logic",
            "Mathematical Physics", "Metric Geometry", "Number Theory",
            "Numerical Analysis", "Operator Algebras",
            "Optimization and Control", "Probability", "Quantum Algebra",
            "Representation Theory", "Rings and Algebras", "Spectral Theory",
            "Statistics (Math)", "Symplectic Geometry" };

    String[] mpItems = { "Physics (Misc) All", "Accelerator Physics",
            "Atmospheric and Oceanic Physics", "Atomic Physics",
            "Atomic and Molecular Clusters", "Biological Physics",
            "Chemical Physics", "Classical Physics", "Computational Physics",
            "Data Analysis, Statistics, and Probability", "Fluid Dynamics",
            "General Physics", "Geophysics", "History of Physics",
            "Instrumentation and Detectors", "Medical Physics", "Optics",
            "Physics Education", "Physics and Society", "Plasma Physics",
            "Popular Physics", "Space Physics" };

    String[] mpURLs = { "physics", "physics.acc-ph", "physics.ao-ph",
            "physics.atom-ph", "physics.atm-clus", "physics.bio-ph",
            "physics.chem-ph", "physics.class-ph", "physics.comp-ph",
            "physics.data-an", "physics.flu-dyn", "physics.gen-ph",
            "physics.geo-ph", "physics.hist-ph", "physics.ins-det",
            "physics.med-ph", "physics.optics", "physics.ed-ph",
            "physics.soc-ph", "physics.plasm-ph", "physics.pop-ph",
            "physics.space-ph" };

    String[] mpShortItems = { "Physics (Misc) All", "Accelerator Physics",
            "Atmospheric and Oceanic Physics", "Atomic Physics",
            "Atomic and Molecular Clusters", "Biological Physics",
            "Chemical Physics", "Classical Physics", "Computational Physics",
            "Data Analysis, Statistics, and Probability", "Fluid Dynamics",
            "General Physics", "Geophysics", "History of Physics",
            "Instrumentation and Detectors", "Medical Physics", "Optics",
            "Physics Education", "Physics and Society", "Plasma Physics",
            "Popular Physics", "Space Physics" };

    String[] nlItems = { "Nonlinear Sciences All",
            "Adaptation and Self-Organizing Systems",
            "Cellular Automata and Lattice Gases", "Chaotic Dynamics",
            "Exactly Solvable and Integrable Systems",
            "Pattern Formation and Solitons" };

    String[] nlURLs = { "nlin", "nlin.AO", "nlin.CG", "nlin.CD", "nlin.SI",
            "nlin.PS" };

    String[] nlShortItems = { "Nonlinear Sciences",
            "Adaptation and Self-Organizing Systems",
            "Cellular Automata and Lattice Gases", "Chaotic Dynamics",
            "Exactly Solvable and Integrable Systems",
            "Pattern Formation and Solitons" };

    String[] qbItems = { "Quant. Biology All", "Biomolecules", "Cell Behavior",
            "Genomics", "Molecular Networks", "Neurons and Cognition",
            "Quant. Biology Other", "Populations and Evolutions",
            "Quantitative Methods", "Subcellular Processes",
            "Tissues and Organs" };

    String[] qbURLs = { "q-bio", "q-bio.BM", "q-bio.CB", "q-bio.GN",
            "q-bio.MN", "q-bio.NC", "q-bio.OT", "q-bio.PE", "q-bio.QM",
            "q-bio.SC", "q-bio.TO" };

    String[] qbShortItems = { "Quant. Bio. All", "Biomolecules",
            "Cell Behavior", "Genomics", "Molecular Networks",
            "Neurons and Cognition", "QB Other", "Populations and Evolutions",
            "Quantitative Methods", "Subcellular Processes",
            "Tissues and Organs" };

    String[] qfItems = { "Quant. Finance All", "Computational Finance",
            "General Finance", "Portfolio Management",
            "Pricing and Securities", "Risk Management", "Statistical Finance",
            "Trading and Market Microstructure" };

    String[] qfURLs = { "q-fin", "q-fin.CP", "q-fin.GN", "q-fin.PM",
            "q-fin.PR", "q-fin.RM", "q-fin.ST", "q-fin.TR" };

    String[] qfShortItems = { "Quant. Fin. All", "Computational Finance",
            "General Finance", "Portfolio Management",
            "Pricing and Securities", "Risk Management", "Statistical Finance",
            "Trading and Market Microstructure" };

    String[] stItems = { "Statistics All", "Stats. Applications",
            "Stats. Computation", "Machine Learning", "Stats. Methodology",
            "Stats. Theory" };

    String[] stURLs = { "stat", "stat.AP", "stat.CO", "stat.ML", "stat.ME",
            "stat.TH" };

    String[] stShortItems = { "Statistics All", "Stats. Applications",
            "Stats. Computation", "Machine Learning", "Stats. Methodology",
            "Stats. Theory" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        thisActivityListener = this;

        Log.d("Arx","Opening Database 3");
        droidDB = new arXivDB(this);
        favorites = droidDB.getFeeds();
        droidDB.close();
        Log.d("Arx","Closed Database 3");


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), SearchWindow.class);
                startActivity(myIntent);
            }
        });

        //SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        //mySourcePref=Integer.parseInt(prefs.getString("sourcelist", "0"));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            String str = getString(R.string.about_text);
            TextView wv = new TextView(this);
            wv.setPadding(16, 0, 16, 16);
            wv.setText(str);

            ScrollView scwv = new ScrollView(this);
            scwv.addView(wv);

            //LinearLayout myLL = new LinearLayout(this);
            //myLL.addView(scwv);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(scwv);
            //XXX change to resource
            builder.setTitle("About arXiv Mobile");
            AlertDialog dialog = builder.create();
            dialog.show();

            //Dialog dialog = new Dialog(this) {
            //    public boolean onKeyDown(int keyCode, KeyEvent event) {
            //        if (keyCode != KeyEvent.KEYCODE_DPAD_LEFT)
            //            this.dismiss();
            //        return true;
            //    }
            //};
            //dialog.setTitle(R.string.about_arxiv_droid);
            //dialog
            //        .addContentView(scwv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
            //dialog.show();
            return (true);
        } else if (id == R.id.action_view_history) {
            Intent myIntent = new Intent(this, HistoryWindow.class);
            startActivity(myIntent);
            return (true);
        } else if (id == R.id.action_clear_history) {
            deleteFiles();
            return (true);
        //} else if (id == R.id.action_preferences) {
        //    startActivity(new Intent(this, EditPreferences.class));
        //    return (true);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);

            int currentTab = getArguments().getInt(ARG_SECTION_NUMBER);

            //textView.setText(getString(R.string.section_format, currentTab));

            if (currentTab == 1) {
                catList = (ListView) rootView.findViewById(R.id.tablist);
                catList.setOnItemClickListener(thisActivityListener);
                catList.setAdapter(new ArrayAdapter<String>(this.getContext(),
                        android.R.layout.simple_list_item_1, items));
                registerForContextMenu(catList);
            } else if (currentTab == 2) {
                favList = (ListView) rootView.findViewById(R.id.tablist);
                favList.setOnItemClickListener(thisActivityListener);
                List<String> lfavorites = new ArrayList<String>();
                List<String> lunread = new ArrayList<String>();
                for (Feed feed : favorites) {
                    String unreadString = "";
                    if (feed.unread > 99) {
                        unreadString = "99+";
                    } else if (feed.unread == -2) {
                        unreadString = "-";
                    } else if (feed.unread <= 0) {
                        unreadString = "0";
                    } else if (feed.unread < 10) {
                        unreadString = ""+feed.unread;
                    } else {
                        unreadString = ""+feed.unread;
                    }
                    lfavorites.add(feed.title);
                    lunread.add(unreadString);
                }

                favoritesList = new String[lfavorites.size()];
                unreadList = new String[lfavorites.size()];

                lfavorites.toArray(favoritesList);
                lunread.toArray(unreadList);

                favList.setAdapter(new ArrayAdapter<String>(this.getContext(),
                        android.R.layout.simple_list_item_1, lfavorites));
                //favList.setAdapter(new myCustomAdapter());
                registerForContextMenu(favList);
            }



            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {

        try {
        } catch (ClassCastException e) {
            return;
        }
        if (view == favList) {
            menu.add(0, 1000, 0, R.string.remove_favorites);
            vFlag = 0;
        } else {
            menu.add(0, 1000, 0, R.string.add_favorites);
            vFlag = 1;
        }
    }

    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            return false;
        }

        Log.d("Arx","Opening Database 2");
        droidDB = new arXivDB(this);
        favorites = droidDB.getFeeds();

        int icount = 0;
        if (vFlag == 0) {
            for (Feed feed : favorites) {
                if (icount == info.position) {
                    droidDB.deleteFeed(feed.feedId);
                }
                icount++;
            }
        } else {
            //XXX mySourcePref Doesn't get updated after a change in prefences until the app is opened again
            if (mySourcePref == 0) {
                String tempquery = "search_query=cat:" + urls[info.position] + "*";
                String tempurl = "https://export.arxiv.org/api/query?" + tempquery
                        + "&sortBy=submittedDate&sortOrder=ascending";
                droidDB.insertFeed(shortItems[info.position], tempquery, tempurl,-1,-1);
            //} else {
            //    String tempquery = urls[info.position];
            //    String tempurl = tempquery;
            //    droidDB.insertFeed(shortItems[info.position]+" (RSS)", shortItems[info.position], tempurl,-2,-2);
            //    Toast.makeText(this, R.string.added_to_favorites_rss,
            //            Toast.LENGTH_SHORT).show();
            }
        }

        droidDB.close();
        Log.d("Arx","Closed Database 2");

        updateFavList();

        return true;
    }

    public void onItemClick(AdapterView<?> a, View v, int position, long id) {

        Log.d("Arx","Im in onItemClick");

        if (a == favList) {

            String tempname = "";
            String tempurl = "";
            String tempquery = "";

            Log.d("Arx","Opening Database 4");
            droidDB = new arXivDB(this);
            favorites = droidDB.getFeeds();
            droidDB.close();
            Log.d("Arx","Closed Database 4");

            int icount = 0;
            for (Feed feed : favorites) {
                if (icount == position) {
                    tempquery = feed.title;
                    tempname = feed.shortTitle;
                    tempurl = feed.url;
                }
                icount++;
            }

            // XXX - What do we do here;
            if (tempurl.contains("query")) {
                Intent myIntent = new Intent(this, SearchListWindow.class);
                myIntent.putExtra("keyquery", tempname);
                myIntent.putExtra("keyname", tempquery);
                myIntent.putExtra("keyurl", tempurl);
                startActivity(myIntent);
            //} else {
            //    Intent myIntent = new Intent(this, RSSListWindow.class);
            //    myIntent.putExtra("keyname", tempname);
            //    myIntent.putExtra("keyurl", tempurl);
            //    startActivity(myIntent);
            }

        } else {
            if (itemsFlag[position] == 0) {
                // XXX
                if (mySourcePref == 1) {
                //    Intent myIntent = new Intent(this, RSSListWindow.class);
                //    myIntent.putExtra("keyname", shortItems[position]);
                //    myIntent.putExtra("keyurl", urls[position]);
                //    startActivity(myIntent);
                } else {
                    Intent myIntent = new Intent(this, SearchListWindow.class);
                    myIntent.putExtra("keyname", shortItems[position]);
                    String tempquery = "search_query=cat:" + urls[position] + "*";
                    myIntent.putExtra("keyquery", tempquery);
                    String tempurl = "https://export.arxiv.org/api/query?"
                            + tempquery
                            + "&sortBy=submittedDate&sortOrder=ascending";
                    myIntent.putExtra("keyurl", tempurl);
                    startActivity(myIntent);
                }
            } else {
                Intent myIntent = new Intent(this, SubArxiv.class);
                myIntent.putExtra("keyname", shortItems[position]);
                // XXX this should be made smarter
                switch (itemsFlag[position]) {
                    case 1:
                        myIntent.putExtra("keyitems", asItems);
                        myIntent.putExtra("keyurls", asURLs);
                        myIntent.putExtra("keyshortitems", asShortItems);
                        break;
                    case 2:
                        myIntent.putExtra("keyitems", cmItems);
                        myIntent.putExtra("keyurls", cmURLs);
                        myIntent.putExtra("keyshortitems", cmShortItems);
                        break;
                    case 3:
                        myIntent.putExtra("keyitems", csItems);
                        myIntent.putExtra("keyurls", csURLs);
                        myIntent.putExtra("keyshortitems", csShortItems);
                        break;
                    case 4:
                        myIntent.putExtra("keyitems", mtItems);
                        myIntent.putExtra("keyurls", mtURLs);
                        myIntent.putExtra("keyshortitems", mtShortItems);
                        break;
                    case 5:
                        myIntent.putExtra("keyitems", mpItems);
                        myIntent.putExtra("keyurls", mpURLs);
                        myIntent.putExtra("keyshortitems", mpShortItems);
                        break;
                    case 6:
                        myIntent.putExtra("keyitems", nlItems);
                        myIntent.putExtra("keyurls", nlURLs);
                        myIntent.putExtra("keyshortitems", nlShortItems);
                        break;
                    case 7:
                        myIntent.putExtra("keyitems", qbItems);
                        myIntent.putExtra("keyurls", qbURLs);
                        myIntent.putExtra("keyshortitems", qbShortItems);
                        break;
                    case 8:
                        myIntent.putExtra("keyitems", qfItems);
                        myIntent.putExtra("keyurls", qfURLs);
                        myIntent.putExtra("keyshortitems", qfShortItems);
                        break;
                    case 9:
                        myIntent.putExtra("keyitems", stItems);
                        myIntent.putExtra("keyurls", stURLs);
                        myIntent.putExtra("keyshortitems", stShortItems);
                        break;
                }
                startActivity(myIntent);
            }
        }
    }

    public void updateFavList() {

        Log.d("Arx","Opening Database 6");
        droidDB = new arXivDB(this);
        favorites = droidDB.getFeeds();
        droidDB.close();
        Log.d("Arx","Closed Database 6");

        List<String> lfavorites = new ArrayList<String>();
        List<String> lunread = new ArrayList<String>();
        for (Feed feed : favorites) {
            String unreadString = "";
            if (feed.unread > 99) {
                unreadString = "99+";
            } else if (feed.unread == -2) {
                unreadString = "-";
            } else if (feed.unread <= 0) {
                unreadString = "0";
            } else if (feed.unread < 10) {
                unreadString = ""+feed.unread;
            } else {
                unreadString = ""+feed.unread;
            }
            lfavorites.add(feed.title);
            lunread.add(unreadString);
        }

        favoritesList = new String[lfavorites.size()];
        unreadList = new String[lfavorites.size()];

        lfavorites.toArray(favoritesList);
        lunread.toArray(unreadList);

        favList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, lfavorites));
        //favList.setAdapter(new myCustomAdapter());
        registerForContextMenu(favList);

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

        Log.d("Arx","Opening Database 1");
        droidDB = new arXivDB(this);
        historys = droidDB.getHistory();

        for (History history : historys) {
            droidDB.deleteHistory(history.historyId);
        }
        droidDB.close();
        Log.d("Arx","Closed Database 1");

        Toast.makeText(this, "Deleted PDF history", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        //mySourcePref=Integer.parseInt(prefs.getString("sourcelist", "0"));

        Log.d("Arx","Opening Database 5");
        droidDB = new arXivDB(this);
        favorites = droidDB.getFeeds();
        droidDB.close();
        Log.d("Arx","Closed Database 5");

        List<String> lfavorites = new ArrayList<String>();
        List<String> lunread = new ArrayList<String>();
        for (Feed feed : favorites) {
            String unreadString = "";
            if (feed.unread > 99) {
                unreadString = "99+";
            } else if (feed.unread == -2) {
                unreadString = "-";
            } else if (feed.unread <= 0) {
                unreadString = "0";
            } else if (feed.unread < 10) {
                unreadString = ""+feed.unread;
            } else {
                unreadString = ""+feed.unread;
            }
            lfavorites.add(feed.title);
            lunread.add(unreadString);
        }

        favoritesList = new String[lfavorites.size()];
        unreadList = new String[lfavorites.size()];

        lfavorites.toArray(favoritesList);
        lunread.toArray(unreadList);

        if (favList != null) {
            favList.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, lfavorites));
            //favList.setAdapter(new myCustomAdapter());
            registerForContextMenu(favList);
        }

    }

}
