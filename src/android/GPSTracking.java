package com.settingconsultoria.gpstracking;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.io.FileFilter;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by usu_adm on 02/05/2016.
 */
public class GPSTracking extends CordovaPlugin {


    private Context mContext;
    private DatabaseHelper mDHelper;
    private Boolean mActivo;
    public static String mActivityName;
    public Intent mIntent;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        mContext = this.cordova.getActivity().getApplicationContext();
        mDHelper = DatabaseHelper.getInstance(mContext);
        mContext.registerReceiver(broadcastReceiver, new IntentFilter(CapturarRutaService.BROADCAST_ACTION));
        mActivo = false;

        mActivityName = cordova.getActivity().getClass().getSimpleName();
        Helper.saveActivityName(mContext, mActivityName);
        Log.e("GPS Tracking", "AN -> " + mActivityName);

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

        if (action == null || !action.matches("iniciarServicio|finalizarServicio|activo|getNuevosPuntos|getRutaActual|getNumeroDeProcesadores")) {
            return false;
        }

        if (action.equals("iniciarServicio")) {
            try {
                JSONObject arg_object = args.getJSONObject(0);
                iniciarServicio(callbackContext, arg_object);
            } catch (JSONException e) {
                e.printStackTrace();
                PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, "{'message': 'Servicio no inicializado', 'code': 2}");
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
		
		if (action.equals("getNumeroDeProcesadores")) {
            getNumeroProcesadores(callbackContext);
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
            int puntosEncontrados;
            try {
                puntosEncontrados = Integer.valueOf(intent.getStringExtra("puntosEncontrados"));
            } catch (Exception e) {
                e.printStackTrace();
                puntosEncontrados = 0;
            }

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
        if (intent.getStringExtra("idRutaActualInicio") != null) {
            String rutaActualId = intent.getStringExtra("idRutaActualInicio");

            String rutaS = "";
            String msg;
            try {
                Ruta ruta = mDHelper.getRutaActual(Integer.valueOf(rutaActualId));
                rutaS = ruta.toJson().toString();
            } catch (Exception e) {
                msg = "{'message':'Ruta no encontrada', 'code': 3}";
                webView.loadUrl("javascript:FleetCareGPSTracking.guardarRutaActual({})");
            }

            webView.loadUrl("javascript:FleetCareGPSTracking.guardarRutaActual('" + rutaS + "', false);");
        }

        if (intent.getStringExtra("idRutaActualFinal") != null) {
            String rutaActualId = intent.getStringExtra("idRutaActualFinal");

            String rutaS = "";
            String msg;
            try {
                Ruta ruta = mDHelper.getRutaActual(Integer.valueOf(rutaActualId));
                List<RutaPunto> rutaPuntoList = mDHelper.getRutaPuntos(ruta.getId());
                JSONArray rutaPuntoJsonList = Helper.getRutaPuntoArrayJson(rutaPuntoList);
                rutaS = ruta.toJson(rutaPuntoJsonList).toString();
                if (rutaPuntoList.size() == 0) {
                    mDHelper.deleteRuta(ruta.getId());
                }
            } catch (Exception e) {
                msg = "{'message':'Ruta no encontrada', 'code': 3}";
                webView.loadUrl("javascript:FleetCareGPSTracking.guardarRutaActual({})");
            }

            webView.loadUrl("javascript:FleetCareGPSTracking.guardarRutaActual('" + rutaS + "', true);");
        }
    }


    private void iniciarServicio(CallbackContext callbackContext, JSONObject opts) {
        PluginResult pluginResult;
        try {
            mActivo = true;
            Intent serviceIntent = new Intent(mContext, CapturarRutaService.class);
            serviceIntent.putExtra("IdUsuario", opts.getString("IdUsuario"));
            serviceIntent.putExtra("IntervaloCaptura", opts.getString("IntervaloCaptura"));
            serviceIntent.putExtra("MensajeDireccionNoEncontrada", opts.getString("MensajeDireccionNoEncontrada"));
            serviceIntent.putExtra("PuntosValidos", opts.getString("PuntosValidos"));

            mContext.startService(serviceIntent);

            JSONObject o = new JSONObject();
            o.put("msg", "Servicio iniciado");

            pluginResult = new PluginResult(PluginResult.Status.OK, o);
        } catch (JSONException e) {
            mActivo = false;
            e.printStackTrace();
            pluginResult = new PluginResult(PluginResult.Status.ERROR, "{'message': 'Servicio no inicializado', 'code': 2}");
        }

        callbackContext.sendPluginResult(pluginResult);
    }

    private void getNuevosPuntos(CallbackContext callbackContext) {
        PluginResult pluginResult;
        try {
            int puntosEncontrados = Helper.getPuntosEncontrados(mContext);

            pluginResult = new PluginResult(PluginResult.Status.OK, puntosEncontrados);
        } catch (Exception e) {
            e.printStackTrace();
            pluginResult = new PluginResult(PluginResult.Status.ERROR, "{'message': 'No se han podido encontrar los puntos encontrados con anterioridad', 'code': 6}");
        }

        callbackContext.sendPluginResult(pluginResult);
    }

    private void activo(CallbackContext callbackContext) {
        PluginResult pluginResult;
        try {
            Boolean activo = isMyServiceRunning(CapturarRutaService.class);
            pluginResult = new PluginResult(PluginResult.Status.OK, activo);

            if (activo) mActivo = true;
        } catch (Exception e) {
            e.printStackTrace();
            pluginResult = new PluginResult(PluginResult.Status.ERROR, "{'message': 'Estado del servicio no se ha podido determinar', 'code': 5}");
        }

        callbackContext.sendPluginResult(pluginResult);
    }

    private void getRutaActual(final CallbackContext callbackContext) {
        Ruta ruta = null;
        String msg = null;
        try {
            int id = Helper.getRutaActual(mContext);
            ruta = mDHelper.getRutaActual(id);
        } catch (Exception e) {
            msg = "{'message':'Ruta no encontrada', 'code': 3}";
        }


        final Ruta finalRuta = ruta;
        final String finalString = msg;
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                PluginResult pluginResult = null;
                if (finalRuta != null) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, finalRuta.toJson());
                } else {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, finalString);
                }
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
        PluginResult pluginResult;
        try {
            mActivo = false;
            mContext.stopService(new Intent(mContext, CapturarRutaService.class));

            JSONObject o = new JSONObject();
            o.put("msg", "Servicio finalizado");

            pluginResult = new PluginResult(PluginResult.Status.OK, o);
        } catch (Exception e) {
            e.printStackTrace();
            pluginResult = new PluginResult(PluginResult.Status.ERROR, "{'message': 'Servicio finalizado incorrectamente', 'code': 4}");
        }

        //getDbFile();
        callbackContext.sendPluginResult(pluginResult);
    }

    /*private void getDbFile() {
        final File f = new File(cordova.getActivity().getFilesDir().getParent() + "/databases/" + DatabaseHelper.DATABASE_NAME);
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(f);
            fos = new FileOutputStream("/mnt/sdcard/db_dump.db");
            while (true) {
                int i = fis.read();
                if (i != -1) {
                    fos.write(i);
                } else {
                    break;
                }
            }
            fos.flush();
            Toast.makeText(cordova.getActivity(), "DB dump OK", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(cordova.getActivity(), "DB dump ERROR", Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ioe) {
            }
        }
    }*/

	private void getNumeroProcesadores(CallbackContext callbackContext) {
        PluginResult pluginResult;
        try {
            int nCores = getNumberOfCores();
            pluginResult = new PluginResult(PluginResult.Status.OK, nCores);
        } catch (Exception e) {
            e.printStackTrace();
            pluginResult = new PluginResult(PluginResult.Status.ERROR, "{'message': 'Estado del servicio no se ha podido determinar', 'code': 5}");
        }

        callbackContext.sendPluginResult(pluginResult);
    }

    private int getNumberOfCores() {
        if (Build.VERSION.SDK_INT >= 17) {
            return Runtime.getRuntime().availableProcessors();
        } else {
            // Use saurabh64's answer
            return getNumCoresOldPhones();
        }
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     *
     * @return The number of cores, or 1 if failed to get result
     */
    private int getNumCoresOldPhones() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                return Pattern.matches("cpu[0-9]+", pathname.getName());
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            //Default to return 1 core
            return 1;
        }
    }
    
	@Override
    public void onDestroy() {
        mContext.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
