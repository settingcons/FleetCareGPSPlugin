package com.settingconsultoria.gpstracking;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by usu_adm on 02/05/2016.
 */
public class RutaPunto {

    private int id;
    private int idRutaLocal;
    private int idUsuario;
    private Double coord_x;
    private Double coord_y;
    private String fechaHora;
    private String heading;
    private String velocidad;
    private String altitud;
    private String accuracy;
    private String altitudAccuracy;
    private String fechaHoraCaptura;
    private int valido;
    private int diff;
    private Float distancia;

    public RutaPunto(int id, int idRutaLocal, int idUsuario, Double coord_x, Double coord_y, String fechaHora, String heading, String velocidad, String altitud, String accuracy, String altitudAccuracy, String fechaHoraCaptura, int valido, int diff, Float distancia) {
        this.id = id;
        this.idRutaLocal = idRutaLocal;
        this.idUsuario = idUsuario;
        this.coord_x = coord_x;
        this.coord_y = coord_y;
        this.fechaHora = fechaHora;
        this.heading = heading;
        this.velocidad = velocidad;
        this.altitud = altitud;
        this.accuracy = accuracy;
        this.altitudAccuracy = altitudAccuracy;
        this.fechaHoraCaptura = fechaHoraCaptura;
        this.valido = valido;
        this.diff = diff;
        this.distancia = distancia;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdRutaLocal() {
        return idRutaLocal;
    }

    public void setIdRutaLocal(int idRutaLocal) {
        this.idRutaLocal = idRutaLocal;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Double getCoord_x() {
        return coord_x;
    }

    public void setCoord_x(Double coord_x) {
        this.coord_x = coord_x;
    }

    public Double getCoord_y() {
        return coord_y;
    }

    public void setCoord_y(Double coord_y) {
        this.coord_y = coord_y;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(String velocidad) {
        this.velocidad = velocidad;
    }

    public String getAltitud() {
        return altitud;
    }

    public void setAltitud(String altitud) {
        this.altitud = altitud;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    public String getAltitudAccuracy() {
        return altitudAccuracy;
    }

    public void setAltitudAccuracy(String altitudAccuracy) {
        this.altitudAccuracy = altitudAccuracy;
    }

    public String getFechaHoraCaptura() {
        return fechaHoraCaptura;
    }

    public void setFechaHoraCaptura(String fechaHoraCaptura) {
        this.fechaHoraCaptura = fechaHoraCaptura;
    }

    public int getValido() {
        return valido;
    }

    public void setValido(int valido) {
        this.valido = valido;
    }

    public int getDiff() {
        return diff;
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }

    public Float getDistancia() {
        return distancia;
    }

    public void setDistancia(Float distancia) {
        this.distancia = distancia;
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("Accuracy", this.accuracy);
            obj.put("Altitud", this.altitud);
            obj.put("CoordX", this.coord_x);
            obj.put("CoordY", this.coord_y);
            obj.put("FechaHora", this.fechaHora);
            obj.put("FechaHoraCapturaRuta", this.fechaHoraCaptura);
            obj.put("Heading", this.heading);
            obj.put("IdPunto", this.id);
            obj.put("IdRuta", this.idRutaLocal);
            obj.put("IdUsuario", this.idUsuario);
            obj.put("Valido", this.valido);
            obj.put("Velocidad", this.velocidad);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

}
