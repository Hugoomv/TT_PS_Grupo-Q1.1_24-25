package es.udc.psi.ttprototipo1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import es.udc.psi.ttprototipo1.databinding.ActivitySettingsBinding;

public class SettingsFragment extends PreferenceFragmentCompat {

    ActivitySettingsBinding binding;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Cargar las preferencias desde el archivo XML
        setPreferencesFromResource(R.xml.preferences, rootKey);
        Log.d("_TAG","SettingsFragment");

        // Configurar el comportamiento del SwitchPreferenceCompat
        SwitchPreferenceCompat themeSwitch = findPreference("pref_theme");
        if (themeSwitch != null) {
            themeSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isDarkMode = (boolean) newValue;

                // Guardar preferencia en SharedPreferences
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit();
                editor.putBoolean("pref_theme", isDarkMode);
                editor.apply();

                // Aplicar tema inmediatamente
                applyThemeBasedOnPreferences();

                // Reiniciar la actividad para reflejar el cambio de tema
                requireActivity().recreate();

                return true; // Devuelve true para guardar el nuevo valor
            });
        }
    }

    private void applyThemeBasedOnPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean isDarkMode = preferences.getBoolean("pref_theme", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
