package com.commonsware.android.arXiv;

import android.os.Bundle;
import android.preference.PreferenceActivity;

//XXX This is a modern settings activity code below appears to be deprecated

public class EditPreferences extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}