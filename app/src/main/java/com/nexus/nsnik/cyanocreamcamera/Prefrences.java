package com.nexus.nsnik.cyanocreamcamera;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


public class Prefrences extends AppCompatActivity {

    Toolbar prefToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container);
        initilize();
        getFragmentManager().beginTransaction().add(R.id.container, new Prefrag()).commit();
        setSupportActionBar(prefToolbar);
    }

    private void initilize() {
        prefToolbar = (Toolbar) findViewById(R.id.containerToolbar);
    }

    public static class Prefrag extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);
            ListPreference camPref = (ListPreference) findPreference(getResources().getString(R.string.camResolutionKey));
            ListPreference vidPref = (ListPreference) findPreference(getResources().getString(R.string.vidResolutionKey));
            setPrefData(camPref);
            setPrefData(vidPref);
        }

        private void setPrefData(ListPreference pref) {
            CharSequence[] ent2 = {"Test","Test"};
            CharSequence[] entVal2 = {"1","2"};
            pref.setEntries(ent2);
            pref.setEntryValues(entVal2);
        }
    }
}
