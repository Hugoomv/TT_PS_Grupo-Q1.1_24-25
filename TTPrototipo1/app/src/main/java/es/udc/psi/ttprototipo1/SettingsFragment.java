package es.udc.psi.ttprototipo1;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.google.firebase.auth.FirebaseAuth;

import es.udc.psi.ttprototipo1.databinding.ActivitySettingsBinding;

public class SettingsFragment extends PreferenceFragmentCompat {

    ActivitySettingsBinding binding;
    private RTFireBaseManagement rtFireBaseManagement = RTFireBaseManagement.getInstance();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Cargar las preferencias desde el archivo XML
        setPreferencesFromResource(R.xml.preferences, rootKey);
        Log.d("_TAG","SettingsFragment");

        // Configurar el comportamiento del SwitchPreferenceCompat
        SwitchPreferenceCompat themeSwitch = findPreference("pref_theme");
        SwitchPreferenceCompat disturbSwitch = findPreference("notifications");

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

        if(disturbSwitch != null){
            disturbSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean allowInvites = (boolean) newValue;

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit();
                editor.putBoolean("notifications", allowInvites);
                editor.apply();

                doNotDisturb(allowInvites);

                return true;
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

    private void doNotDisturb(boolean disturbOrNot){
        rtFireBaseManagement.changeDoNotDisturb(FirebaseAuth.getInstance().getCurrentUser().getUid(), disturbOrNot, new DoNotDisturbCallback() {
            @Override
            public void onSuccess(boolean policy) {
                if (policy){
                    Log.d("_TAG","No molestar desactivado");
                }else{
                    Log.d("_TAG","No molestar activado");
                }
            }

            @Override
            public void onFailure(String errorMsg) {
                Log.d("_TAG",errorMsg);
            }
        });
    }
}
