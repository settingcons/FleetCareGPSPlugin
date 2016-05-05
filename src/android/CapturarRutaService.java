package com.settingconsultoria.gpstracking;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.jaredrummler.android.device.DeviceName;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by usu_adm on 02/05/2016.
 */
public class CapturarRutaService extends Service {
    private static final String TAG = CapturarRutaService.class.getSimpleName();
    private LocationManager mLocationManager = null;
    public DatabaseHelper databaseHelper;
    private int idRutaActual;
    private int puntosEncontrados = 0;
    private int mIdUsuario = 0;
    private int mIntervaloCaptura = Constants.INVTERRVAL_FETCH_LOCATION;

    public static final String BROADCAST_ACTION = "settingconsultoria.com.fleetcarenativo.enviarpuntosencontrados";
    Intent intent;
    private Timer mTimer;
    private Location mLastLocation;

    private class LocationListener implements android.location.LocationListener {

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);

            if (!checkCeros(location)) {
                if (!checkCoords(location)) {
                    if (!checkDiffIntervalo(location)) {
                        if (location.getAccuracy() <= 50) {
                            puntosEncontrados++;
                            intent = new Intent(BROADCAST_ACTION);
                            intent.putExtra("puntosEncontrados", Integer.toString(puntosEncontrados));
                            sendBroadcast(intent);
                            Helper.setPuntosEncontrados(getApplicationContext(), puntosEncontrados);
                            String pattern = "dd/MM/yyyy HH:mm:ss";
                            String dateInString = new SimpleDateFormat(pattern).format(new Date(location.getTime()));
                            String fechaCaptura = new SimpleDateFormat(pattern).format(new Date());
                            float distance = Helper.calcularDistanciaEntreDosPuntos(location, mLastLocation);

                            RutaPunto rutaPunto = new RutaPunto(0, idRutaActual, 0, location.getLatitude(), location.getLongitude(), dateInString, (location.hasBearing() ? (location.hasSpeed() ? Float.toString(location.getBearing()) : null) : null), Float.toString(location.getSpeed()), Double.toString(location.getAltitude()), Float.toString(location.getAccuracy()), "0", fechaCaptura, 1, Constants.INVTERRVAL_FETCH_LOCATION, distance);
                            long id = databaseHelper.insertRutaPunto(rutaPunto);
                            mLastLocation.set(location);
                        }
                    }
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        mIdUsuario = Integer.valueOf((String) intent.getExtras().get("IdUsuario"));
        mIntervaloCaptura = Integer.valueOf((String) intent.getExtras().get("IntervaloCaptura"));

        Helper.setRunningService(getApplicationContext(), true);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        int idDrawable = getApplicationContext().getResources().getIdentifier("icon", "drawable", getApplicationContext().getPackageName());
        int idAppName = getApplicationContext().getResources().getIdentifier("app_name", "string", getApplicationContext().getPackageName());
        int idString = getApplicationContext().getResources().getIdentifier("recording_route", "string", getApplicationContext().getPackageName());
        Notification note = new NotificationCompat.Builder(this)
                .setSmallIcon(idDrawable)
                .setContentTitle(getApplicationContext().getString(idAppName))
                .setContentText(getApplicationContext().getString(idString))
                .build();
        startForeground(Constants.NOTIFICATION_ID, note);
        initializeLocationManager();
        intent = new Intent(BROADCAST_ACTION);
        databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        puntosEncontrados = Helper.getPuntosEncontrados(getApplicationContext());
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                resolverDesdeTimer();
            }
        }, 60 * 10 * 1000);

        int r = Helper.getRutaActual(getApplicationContext());
        if (r != 0)
            idRutaActual = r;
        else {
            String pattern = "dd/MM/yyyy HH:mm:ss";
            String dateInString = new SimpleDateFormat(pattern).format(new Date());
            Ruta ruta = new Ruta(0, mIdUsuario, dateInString, "", "", "", "", "", "");
            long idNewRuta = databaseHelper.insertRuta(ruta);
            idRutaActual = (int) idNewRuta;
            Helper.setRutaActual(getApplicationContext(), idRutaActual);
        }
        intent.putExtra("idRutaActual", Integer.toString(idRutaActual));
        sendBroadcast(intent);

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, mIntervaloCaptura, Constants.DISTANCE_FETCH_LOCATION,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, mIntervaloCaptura, Constants.DISTANCE_FETCH_LOCATION,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        Helper.setRunningService(getApplicationContext(), false);
        Helper.setRutaActual(getApplicationContext(), 0);
        Helper.setPuntosEncontrados(getApplicationContext(), 0);

        /*
            Procesar la ruta finalizada
        */
        Ruta rutaActual = databaseHelper.getRutaActual(idRutaActual);
        List<RutaPunto> rutaPuntoList = databaseHelper.getRutaPuntos(idRutaActual);
        float distancia = 0;
        String duracion = "0";
        if (rutaPuntoList.size() > 0) {
            Location loc1 = new Location("");
            loc1.setLatitude(rutaPuntoList.get(0).getCoord_x());
            loc1.setLongitude(rutaPuntoList.get(0).getCoord_y());
            Location loc2 = new Location("");
            loc2.setLatitude(rutaPuntoList.get(rutaPuntoList.size() - 1).getCoord_x());
            loc2.setLongitude(rutaPuntoList.get(rutaPuntoList.size() - 1).getCoord_y());
            distancia = Helper.calcularDistanciaEntreDosPuntos(loc1, loc2);

            /**
             * Coger los nombres de las calles de inicio y fin
             **/
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            String direcInicio = "";
            String direcFin = "";
            try {
                List<Address> addressesDirecInicio = geocoder.getFromLocation(loc1.getLatitude(), loc1.getLongitude(), 1);
                if (addressesDirecInicio.size() > 0) {
                    Address addressDirecInicio = addressesDirecInicio.get(0);
                    direcInicio = addressDirecInicio.getAddressLine(1) + "\n" + addressDirecInicio.getAddressLine(0) + "\n" + addressDirecInicio.getAddressLine(2);
                }
                rutaActual.setDirecInicio(direcInicio);

                List<Address> addressesDirecFin = geocoder.getFromLocation(loc2.getLatitude(), loc2.getLongitude(), 1);
                if (addressesDirecFin.size() > 0) {
                    Address addressDirecFin = addressesDirecFin.get(0);
                    direcFin = addressDirecFin.getAddressLine(1) + "\n" + addressDirecFin.getAddressLine(0) + "\n" + addressDirecFin.getAddressLine(2);
                }
                rutaActual.setDirecFin(direcFin);
            } catch (IOException e) {
                e.printStackTrace();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            try {
                Date start = sdf.parse(rutaActual.getFechaHoraInicio());
                Date end = sdf.parse(rutaPuntoList.get(rutaPuntoList.size() - 1).getFechaHoraCaptura());
                long duration = end.getTime() - start.getTime();
                duracion = formatDuration(duration);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            rutaActual.setFechaHoraFin(rutaPuntoList.get(rutaPuntoList.size() - 1).getFechaHoraCaptura());
        }

        rutaActual.setDistancia(Float.toString(distancia / 1000));
        rutaActual.setDuracion(duracion);
        String deviceAndAndroidVersion = DeviceName.getDeviceName();
        deviceAndAndroidVersion += "\nAndroid: " + android.os.Build.VERSION.RELEASE;
        rutaActual.setObservaciones(deviceAndAndroidVersion);

        /*
            Updatear Ruta
         */
        databaseHelper.updateRuta(rutaActual);
        intent = new Intent(BROADCAST_ACTION);
        intent.putExtra("idRutaActual", Integer.toString(idRutaActual));
        sendBroadcast(intent);
        mTimer.cancel();
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private String formatDuration(long duration) {
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void resolverDesdeTimer() {
        String pattern = "dd/MM/yyyy HH:mm:ss";
        Date lastLocationTime = new Date(mLastLocation.getTime());
        Date now = new Date();
        long MAX_DURATION = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);//10 minutos
        long duration = now.getTime() - lastLocationTime.getTime();

        //si esta vencida lanzar aviso
        if (duration >= MAX_DURATION) {
            MediaPlayer mPlayer = null;
            int rawInt = getApplicationContext().getResources().getIdentifier("beep.wav", "raw", getApplicationContext().getPackageName());
            mPlayer = MediaPlayer.create(getApplicationContext(), rawInt);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.start();
            int idDrawable = getApplicationContext().getResources().getIdentifier("icon", "drawable", getApplicationContext().getPackageName());
            int idAppName = getApplicationContext().getResources().getIdentifier("app_name", "string", getApplicationContext().getPackageName());
            int idString = getApplicationContext().getResources().getIdentifier("espera_diez_minutos", "string", getApplicationContext().getPackageName());
            Notification note = new NotificationCompat.Builder(this)
                    .setSmallIcon(idDrawable)
                    .setContentTitle(getApplicationContext().getString(idAppName))
                    .setContentText(getApplicationContext().getString(idString))
                    .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), getApplicationContext().getClass()), 0))
                    .build();
            note.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(0, note);
            intent = new Intent(BROADCAST_ACTION);
            intent.putExtra("tiempoSinCoordenadasNuevas", "true");
            sendBroadcast(intent);
        }

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                resolverDesdeTimer();
            }
        }, 60 * 10 * 1000);
    }

    private Boolean checkCeros(Location location) {
        return !(location.getSpeed() == 0 && location.getAltitude() == 0 && location.getAccuracy() == 0);
    }

    private Boolean checkCoords(Location location) {
        return !(location.getLatitude() == mLastLocation.getLatitude() && location.getLongitude() == mLastLocation.getLongitude());
    }

    private Boolean checkDiffIntervalo(Location location) {
        Date mLocationTime = new Date(mLastLocation.getTime());
        Date actualLocationTime = new Date(location.getTime());
        long diff = actualLocationTime.getTime() - mLocationTime.getTime();

        return diff >= mIntervaloCaptura;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
