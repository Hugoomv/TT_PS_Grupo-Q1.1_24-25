package es.udc.psi.ttprototipo1.UserInterface;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class LocaleHelper {

    private static final String LANGUAGE_KEY = "language_pref";

    // Establecer el idioma seleccionado
    public static void setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

        // Guardar el idioma en SharedPreferences
        saveLanguageToPreferences(context, languageCode);
    }

    // Cargar el idioma guardado
    public static void loadLocale(Context context) {
        String languageCode = getLanguageFromPreferences(context);
        setLocale(context, languageCode);
    }

    // Guardar idioma en SharedPreferences
    private static void saveLanguageToPreferences(Context context, String languageCode) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LANGUAGE_KEY, languageCode);
        editor.apply();
    }

    // Obtener idioma desde SharedPreferences
    private static String getLanguageFromPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        return prefs.getString(LANGUAGE_KEY, "es"); // Idioma por defecto: español
    }
}
