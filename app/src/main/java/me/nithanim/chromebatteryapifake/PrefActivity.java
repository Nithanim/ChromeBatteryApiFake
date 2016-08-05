package me.nithanim.chromebatteryapifake;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PrefActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        PreferenceManager pm = getPreferenceManager();
        pm.setSharedPreferencesName("pref");
        pm.setSharedPreferencesMode(MODE_WORLD_READABLE);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        addPreferencesFromResource(R.xml.preferences);
    }
}
