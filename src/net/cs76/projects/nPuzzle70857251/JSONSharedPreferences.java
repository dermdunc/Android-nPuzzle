package net.cs76.projects.nPuzzle70857251;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * Solution pulled from forum response on stackoverflow. See link below
 * http://stackoverflow.com/questions/3876680/is-it-possible-to-add-an-array-or-object-to-sharedpreferences-on-android
 * ********************************* 
 * Student: Dermot Duncan 	     
 * 	 HU ID: 70857251      		
 *	 Email: dermduncan@gmail.com 
 * *********************************
 */
public class JSONSharedPreferences {
    private static final String PREFIX = "json";

    public static void saveJSONArray(Context c, String prefName, String key, JSONArray array) {
        SharedPreferences settings = c.getSharedPreferences(prefName, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(JSONSharedPreferences.PREFIX+key, array.toString());
        editor.commit();
    }

    public static JSONArray loadJSONArray(Context c, String prefName, String key) throws JSONException {
        SharedPreferences settings = c.getSharedPreferences(prefName, 0);
        return new JSONArray(settings.getString(JSONSharedPreferences.PREFIX+key, "[]"));
    }

    public static void remove(Context c, String prefName, String key) {
        SharedPreferences settings = c.getSharedPreferences(prefName, 0);
        if (settings.contains(JSONSharedPreferences.PREFIX+key)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.remove(JSONSharedPreferences.PREFIX+key);
            editor.commit();
        }
    }
}
