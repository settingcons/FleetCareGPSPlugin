package com.settingconsultoria.gpstracking;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by usu_adm on 02/05/2016.
 */
public class Ruta {

    private int id;
    private int idUsuario;
    private String fechaHoraInicio;
    private String fechaHoraFin;
    private String duracion;
    private String distancia;
    private String observaciones;
    private String direcInicio;
    private String direcFin;
    private int esiOS;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(String fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public String getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(String fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getDistancia() {
        return distancia;
    }

    public void setDistancia(String distancia) {
        this.distancia = distancia;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getDirecInicio() {
        return direcInicio;
    }

    public void setDirecInicio(String direcInicio) {
        this.direcInicio = direcInicio;
    }

    public String getDirecFin() {
        return direcFin;
    }

    public void setDirecFin(String direcFin) {
        this.direcFin = direcFin;
    }

    public int getEsiOS() {
        return esiOS;
    }

    public void setEsiOS(int esiOS) {
        this.esiOS = esiOS;
    }

    public Ruta(int id, int idUsuario, String fechaHoraInicio, String fechaHoraFin, String duracion, String distancia, String observaciones, String direcInicio, String direcFin, int esiOS) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.duracion = duracion;
        this.distancia = distancia;
        this.observaciones = observaciones;
        this.direcInicio = direcInicio;
        this.direcFin = direcFin;
        this.esiOS = esiOS;

    }

    public Ruta(int id, int idUsuario, String fechaHoraInicio, String fechaHoraFin, String duracion, String distancia, String observaciones, String direcInicio, String direcFin) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.duracion = duracion;
        this.distancia = distancia;
        this.observaciones = observaciones;
        this.direcInicio = direcInicio;
        this.direcFin = direcFin;
        this.esiOS = 0;
    }

    public JSONObject toJson(JSONArray rutaPuntoList) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("DireccionFin", this.direcFin.replaceAll("[\n]", " "));
            obj.put("DireccionInicio", this.direcInicio.replaceAll("[\n]", " "));
            obj.put("Distancia", this.distancia);
            obj.put("Duracion", this.duracion);
            obj.put("EsIOS", this.esiOS);
            obj.put("FechaHoraFin", this.fechaHoraFin);
            obj.put("FechaHoraInicio", this.fechaHoraInicio);
            obj.put("IdRuta", this.id);
            obj.put("IdUsuario", this.idUsuario);
            obj.put("ListaPuntos", rutaPuntoList);
            obj.put("Observaciones", this.observaciones.replaceAll("[\n]", " "));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("DireccionFin", this.direcFin.replaceAll("[\n]", " "));
            obj.put("DireccionInicio", this.direcInicio.replaceAll("[\n]", " "));
            obj.put("Distancia", this.distancia);
            obj.put("Duracion", this.duracion);
            obj.put("EsIOS", this.esiOS);
            obj.put("FechaHoraFin", this.fechaHoraFin);
            obj.put("FechaHoraInicio", this.fechaHoraInicio);
            obj.put("IdRuta", this.id);
            obj.put("IdUsuario", this.idUsuario);
            obj.put("Observaciones", this.observaciones.replaceAll("[\n]", " "));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

}
