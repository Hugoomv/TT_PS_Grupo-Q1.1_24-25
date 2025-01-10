package es.udc.psi.ttprototipo1.UserInterface;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class ProfileImage {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_PROFILE_IMAGE = "profileImage";

    // Guardar la imagen en SharedPreferences
    public static void saveProfileImage(Context context, Bitmap bitmap) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Convertir Bitmap a Base64
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        // Guardar cadena codificada en SharedPreferences
        editor.putString(KEY_PROFILE_IMAGE, encodedImage);
        editor.apply();
    }

    // Cargar la imagen de SharedPreferences
    public static Bitmap loadProfileImage(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String encodedImage = sharedPreferences.getString(KEY_PROFILE_IMAGE, null);

        if (encodedImage != null) {
            // Convertir Base64 a Bitmap
            byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        }

        return null;  // No hay imagen guardada
    }
}
