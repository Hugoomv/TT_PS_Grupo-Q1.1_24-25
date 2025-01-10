package es.udc.psi.ttprototipo1;

import android.os.Bundle;
import android.util.Log;

import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Cargar las preferencias desde el archivo XML
        setPreferencesFromResource(R.xml.preferences, rootKey);
        Log.d("_TAG","SettingsFragment");
    }
}
