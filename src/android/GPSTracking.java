package com.settingconsultoria.gpstracking;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by usu_adm on 02/05/2016.
 */
public class GPSTracking extends CordovaPlugin {


    private Context mContext;
    private DatabaseHelper mDHelper;
    private Boolean mActivo;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        mContext = this.cordova.getActivity().getApplicationContext();
        mDHelper = DatabaseHelper.getInstance(mContext);
        mContext.registerReceiver(broadcastReceiver, new IntentFilter(CapturarRutaService.BROADCAST_ACTION));
        mActivo = false;

        super.initialize(cordova, webView);
    }

    /**
     * Executes theboolean request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArry of arguments for the plugin.
     * @param callbackContext The callback id used when calling back into JavaScript.
     * @return True if the action was valid, or false if not.
     * @throws JSONException
     */
    public boolean execute(final String action, final JSONArray args,
                           final CallbackContext callbackContext) {

        if (action == null || !action.matches("iniciarServicio|finalizarServicio|activo|getNuevosPuntos|getRutaActual")) {
            return false;
        }

        if (action.equals("iniciarServicio")) {
            try {
                JSONObject arg_object = args.getJSONObject(0);
                iniciarServicio(callbackContext, arg_object);
            } catch (JSONException e) {
                e.printStackTrace();
                PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
                callbackContext.sendPluginResult(pluginResult);
            }

            return true;
        }

        if (action.equals("finalizarServicio")) {
            finalizarServicio(callbackContext);
            return true;
        }

        if (action.equals("activo")) {
            activo(callbackContext);
            return true;
        }

        if (action.equals("getNuevosPuntos")) {
            getNuevosPuntos(callbackContext);
            return true;
        }

        if (action.equals("getRutaActual")) {
            getRutaActual(callbackContext);
            return true;
        }

        return true;
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sendResult(intent);
        }
    };

    private void sendResult(Intent intent) {
        /**
         * Esto es que simplemente ha encontrado un punto nuevo
         */
        if (intent.getStringExtra("puntosEncontrados") != null) {
            int puntosEncontrados = Integer.valueOf(intent.getStringExtra("puntosEncontrados"));
            webView.loadUrl("javascript:FleetCareGPSTracking.guardarNuevosPuntos(" + puntosEncontrados + ");");
        }

        /**
         * Esto es que lleva más de 10 minutos sin recibir coordenadas nuevas
         */
        if (intent.getStringExtra("tiempoSinCoordenadasNuevas") != null) {
            webView.loadUrl("javascript:FleetCareGPSTracking.avisoNoCoordenadas();");
        }

        /**
         * Si se recibe esto es que ha finalizado la ruta, por lo tanto envío
         */
        if (intent.getStringExtra("idRutaActual") != null) {
            String rutaActualId = intent.getStringExtra("idRutaActual");

            String rutaS = "";
            if (!mActivo) {
                Ruta ruta = mDHelper.getRutaActual(Integer.valueOf(rutaActualId));
                List<RutaPunto> rutaPuntoList = mDHelper.getRutaPuntos(ruta.getId());
                JSONArray rutaPuntoJsonList = Helper.getRutaPuntoArrayJson(rutaPuntoList);
                rutaS = ruta.toJson(rutaPuntoJsonList).toString();
                if (rutaPuntoList.size() == 0) {
                    mDHelper.deleteRuta(ruta.getId());
                }
            } else {
                Ruta ruta = mDHelper.getRutaActual(Integer.valueOf(rutaActualId));
                rutaS = ruta.toJson().toString();
            }

            webView.loadUrl("javascript:FleetCareGPSTracking.guardarRutaActual('" + rutaS + "', " + ((mActivo) ? "false" : "true") + ");");
        }
    }


    private void iniciarServicio(CallbackContext callbackContext, JSONObject opts) {
        Intent serviceIntent = new Intent(mContext, CapturarRutaService.class);
        try {
            serviceIntent.putExtra("IdUsuario", opts.getString("IdUsuario"));
            serviceIntent.putExtra("IntervaloCaptura", opts.getString("IntervaloCaptura"));
            serviceIntent.putExtra("MensajeDireccionNoEncontrada", opts.getString("MensajeDireccionNoEncontrada"));
        } catch (JSONException e) {
            e.printStackTrace();
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
            callbackContext.sendPluginResult(pluginResult);
        }
        mActivo = true;
        mContext.startService(serviceIntent);

        JSONObject o = new JSONObject();
        try {
            o.put("msg", "Servicio iniciado");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, o);

        callbackContext.sendPluginResult(pluginResult);
    }

    private void getNuevosPuntos(CallbackContext callbackContext) {
        int puntosEncontrados = Helper.getPuntosEncontrados(mContext);

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, puntosEncontrados);

        callbackContext.sendPluginResult(pluginResult);
    }

    private void activo(CallbackContext callbackContext) {
        Boolean activo = isMyServiceRunning(CapturarRutaService.class);
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, activo);

        if (activo) mActivo = true;

        callbackContext.sendPluginResult(pluginResult);
    }

    private void getRutaActual(final CallbackContext callbackContext) {
        int id = Helper.getRutaActual(mContext);
        Ruta ruta = mDHelper.getRutaActual(id);
        final PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, ruta.toJson());

        this.cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                callbackContext.sendPluginResult(pluginResult);
            }
        });
    }

    /**
     * @param serviceClass Clase del servicio a comprobar
     * @return true|false
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void finalizarServicio(CallbackContext callbackContext) {
        mContext.stopService(new Intent(mContext, CapturarRutaService.class));
        mActivo = false;

        JSONObject o = new JSONObject();
        try {
            o.put("msg", "Servicio finalizado");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, o);

        callbackContext.sendPluginResult(pluginResult);
    }

}
