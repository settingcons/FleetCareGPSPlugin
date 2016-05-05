package com.settingconsultoria.gpstracking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by usu_adm on 02/05/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final Logger log = Logger.getLogger(DatabaseHelper.class.getName());
    private static DatabaseHelper sInstance;

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    public static final String DATABASE_NAME = "bdFleetCare.db";

    //App context
    Context context;

    //Nombres Tablas
    public static final String TABLE_RUTA = "Ruta";
    public static final String TABLE_RUTAPUNTO = "RutaPunto";

    private static final String CREATE_TABLE_RUTA = "CREATE TABLE IF NOT EXISTS "
            + TABLE_RUTA + " ("
            + " ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + " IDUSUARIO INTEGER,"
            + " FECHAHORAINICIO TEXT,"
            + " FECHAHORAFIN TEXT, "
            + " DURACION TEXT, "
            + " DISTANCIA TEXT, "
            + " OBSERVACIONES TEXT, "
            + " DIRECINICIO TEXT, "
            + " DIRECFIN TEXT, "
            + " ESIOS INTEGER) ";

    private static final String CREATE_TABLE_RUTAPUNTO = "CREATE TABLE IF NOT EXISTS "
            + TABLE_RUTAPUNTO + " ("
            + " ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + " IDRUTALOCAL INTEGER, "
            + " IDUSUARIO INTEGER, "
            + " COORD_X TEXT, "
            + " COORD_Y TEXT, "
            + " FECHAHORA TEXT, "
            + " HEADING TEXT, "
            + " VELOCIDAD TEXT, "
            + " ALTITUD TEXT, "
            + " ACCURACY TEXT, "
            + " ALTITUDACCURACY TEXT, "
            + " FECHAHORACAPTURA TEXT, "
            + " VALIDO INTEGER, "
            + " DIFF INTEGER, "
            + " DISTANCIA DOUBLE) ";

    public static synchronized DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_RUTA);
        db.execSQL(CREATE_TABLE_RUTAPUNTO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RUTA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RUTAPUNTO);

        // create new tables
        onCreate(db);
    }

    public long insertRuta(Ruta ruta) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("IDUSUARIO", ruta.getIdUsuario());
        values.put("FECHAHORAINICIO", ruta.getFechaHoraInicio());
        values.put("FECHAHORAFIN", ruta.getFechaHoraFin());
        values.put("DURACION", ruta.getDuracion());
        values.put("DISTANCIA", ruta.getDistancia());
        values.put("OBSERVACIONES", ruta.getObservaciones());
        values.put("DIRECINICIO", ruta.getDirecInicio());
        values.put("DIRECFIN", ruta.getDirecFin());
        values.put("ESIOS", ruta.getEsiOS());

        // insert row

        return db.insert(TABLE_RUTA, null, values);
    }

    public long insertRutaPunto(RutaPunto rutaPunto) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("IDRUTALOCAL", rutaPunto.getIdRutaLocal());
        values.put("IDUSUARIO", rutaPunto.getIdUsuario());
        values.put("COORD_X", rutaPunto.getCoord_x());
        values.put("COORD_Y", rutaPunto.getCoord_y());
        values.put("FECHAHORA", rutaPunto.getFechaHora());
        values.put("HEADING", rutaPunto.getHeading());
        values.put("VELOCIDAD", rutaPunto.getVelocidad());
        values.put("ALTITUD", rutaPunto.getAltitud());
        values.put("ACCURACY", rutaPunto.getAccuracy());
        values.put("ALTITUDACCURACY", rutaPunto.getAltitudAccuracy());
        values.put("FECHAHORACAPTURA", rutaPunto.getFechaHoraCaptura());
        values.put("VALIDO", rutaPunto.getValido());
        values.put("DIFF", rutaPunto.getDiff());
        values.put("DISTANCIA", rutaPunto.getDistancia());

        // insert row

        return db.insert(TABLE_RUTAPUNTO, null, values);
    }

    public List<RutaPunto> getRutaPuntos(int idRutaActual) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<RutaPunto> rutaPuntoList = new ArrayList<RutaPunto>();

        Cursor c = null;
        try {
            c = db.query(TABLE_RUTAPUNTO,
                    null,
                    "IDRUTALOCAL = " + idRutaActual,
                    null, null, null, null);

            if (c.moveToFirst()) {
                do {

                    RutaPunto rutaPunto = new RutaPunto(c.getInt(c.getColumnIndex("ID")),
                            c.getInt(c.getColumnIndex("IDRUTALOCAL")),
                            c.getInt(c.getColumnIndex("IDUSUARIO")),
                            c.getDouble(c.getColumnIndex("COORD_X")),
                            c.getDouble(c.getColumnIndex("COORD_Y")),
                            c.getString(c.getColumnIndex("FECHAHORA")),
                            c.getString(c.getColumnIndex("HEADING")),
                            c.getString(c.getColumnIndex("VELOCIDAD")),
                            c.getString(c.getColumnIndex("ALTITUD")),
                            c.getString(c.getColumnIndex("ACCURACY")),
                            c.getString(c.getColumnIndex("ALTITUDACCURACY")),
                            c.getString(c.getColumnIndex("FECHAHORACAPTURA")),
                            c.getInt(c.getColumnIndex("VALIDO")),
                            c.getInt(c.getColumnIndex("DIFF")),
                            c.getFloat(c.getColumnIndex("DISTANCIA"))
                    );
                    rutaPuntoList.add(rutaPunto);
                } while (c.moveToNext());
            }
        } finally {
            if (c != null) c.close();
        }


        return rutaPuntoList;
    }

    public Ruta getRutaActual(int idRutaActual) {
        Ruta ruta = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_RUTA,
                null,
                "ID = ?",
                new String[]{Integer.toString(idRutaActual)}, null, null, null);

        try {
            if (c.moveToFirst()) {
                ruta = new Ruta(c.getInt(c.getColumnIndex("ID")),
                        c.getInt(c.getColumnIndex("IDUSUARIO")),
                        c.getString(c.getColumnIndex("FECHAHORAINICIO")),
                        c.getString(c.getColumnIndex("FECHAHORAFIN")),
                        c.getString(c.getColumnIndex("DURACION")),
                        c.getString(c.getColumnIndex("DISTANCIA")),
                        c.getString(c.getColumnIndex("OBSERVACIONES")),
                        c.getString(c.getColumnIndex("DIRECINICIO")),
                        c.getString(c.getColumnIndex("DIRECFIN"))
                );
            }
        } finally {
            if (c != null) c.close();
        }

        return ruta;
    }

    public void updateRuta(Ruta ruta) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("FECHAHORAFIN", ruta.getFechaHoraFin());
        cv.put("DURACION", ruta.getDuracion());
        cv.put("DISTANCIA", ruta.getDistancia());
        cv.put("OBSERVACIONES", ruta.getObservaciones());
        cv.put("DIRECINICIO", ruta.getDirecInicio());
        cv.put("DIRECFIN", ruta.getDirecFin());

        db.update(TABLE_RUTA, cv, "ID" + " = " + ruta.getId(), null);
    }

    public void deleteRuta(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_RUTA, "ID = ?", new String[]{Integer.toString(id),});
    }
}
