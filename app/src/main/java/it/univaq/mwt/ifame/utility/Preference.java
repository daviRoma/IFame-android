package it.univaq.mwt.ifame.utility;

import android.content.Context;
import android.content.SharedPreferences;

public class Preference {

    private static final String NAME = "ifame_preferences";

    public static void saveBoolean(Context context, String key, boolean value){

        SharedPreferences.Editor editor = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean loadBoolean(Context context, String key, boolean defValue){

        SharedPreferences preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(key, defValue);

    }

    public static void saveString(Context context, String key, String value){

        SharedPreferences.Editor editor = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String loadString(Context context, String key, String defValue){

        SharedPreferences preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return preferences.getString(key, defValue);

    }

    public static void saveDouble(Context context, String key, double value){

        SharedPreferences.Editor editor = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit();
        editor.putString(key, String.valueOf(value));
        editor.apply();
    }

    public static double loadDouble(Context context, String key, double defValue){

        SharedPreferences preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return Double.parseDouble(preferences.getString(key, String.valueOf(defValue)));

    }
}
