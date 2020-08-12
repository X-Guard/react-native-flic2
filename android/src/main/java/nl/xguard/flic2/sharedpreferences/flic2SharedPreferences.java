package nl.xguard.flic2.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;
import nl.xguard.flic2.R;

public class flic2SharedPreferences {

    public static final String PREF_KEY_IS_RUNNING = "PREF_KEY_IS_RUNNING";

    private SharedPreferences sharedPreferences;

    private static flic2SharedPreferences sisSharedPreferences = null;

    public static flic2SharedPreferences getInstance(Context context) {
        if (sisSharedPreferences == null) {
            sisSharedPreferences = new flic2SharedPreferences(context);
        }
        return sisSharedPreferences;
    }

    private flic2SharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_filename), Context.MODE_PRIVATE);
    }

    public void write(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }
}
