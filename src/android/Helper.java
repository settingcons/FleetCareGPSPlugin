package com.settingconsultoria.gpstracking;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by usu_adm on 02/05/2016.
 */
public class Helper {
    public static void setRutaActual(Context context, int idRuta) {
        SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.ID_RUTA_ACTUAL, idRuta);
        editor.apply();
    }

    public static int getRutaActual(Context context) {
        SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getInt(Constants.ID_RUTA_ACTUAL, 0);
    }

    public static void setPuntosEncontrados(Context context, int puntosEncontrados) {
        SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.PUNTOS_ENCONTRADOS, puntosEncontrados);
        editor.apply();
    }

    public static int getPuntosEncontrados(Context context) {
        SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getInt(Constants.PUNTOS_ENCONTRADOS, 0);
    }

    public static float calcularDistanciaEntreDosPuntos(Location loc1, Location loc2) {
        return loc1.distanceTo(loc2);
    }

    public static void mostrarToast(Context context, String mensaje) {
        Toast.makeText(context, mensaje,
                Toast.LENGTH_SHORT).show();
    }

    public static void setButtonIniciarVisibleState(Context context, boolean visible) {
        SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.BUTTON_INICIAR_VISIBLE, visible);
        editor.apply();
    }

    public static Boolean getButtonIniciarVisibleState(Context context) {
        SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean(Constants.BUTTON_INICIAR_VISIBLE, true);
    }

    public static void setRunningService(Context context, boolean runnning) {
        SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.RUNNING_SERVICE, runnning);
        editor.apply();
    }

    public static boolean getRunningService(Context context) {
        SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean(Constants.RUNNING_SERVICE, false);
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo i = conMgr.getActiveNetworkInfo();
        return i != null && i.isConnected() && i.isAvailable();

    }

    public static JSONArray getRutaPuntoArrayJson(List<RutaPunto> rutaPuntoList) {
        JSONArray jsonArray = new JSONArray();

        for(RutaPunto rp: rutaPuntoList) {
            JSONObject rpJson = rp.toJson();
            jsonArray.put(rpJson);
        }

        return jsonArray;
    }
}
