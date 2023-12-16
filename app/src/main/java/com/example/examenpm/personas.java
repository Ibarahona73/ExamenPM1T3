package com.example.examenpm;

import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.PropertyName;

public class personas {

    String idOrden;
    String periodista;
    String descripcion;
    String foto;
    String audio;
    String fecha;

    // Default (no-argument) constructor
    //Ivan Barahona 201930010221
    public personas() {
    }

    public personas(String idOrden, String periodista, String descripcion, String foto, String audio, String fecha) {
        this.idOrden = idOrden;
        this.periodista = periodista;
        this.descripcion = descripcion;
        this.foto = foto;
        this.audio = audio;
        this.fecha = fecha;
    }

    @PropertyName("idOrden")
    public String getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(String numOrden) {
        this.idOrden = numOrden;
    }

    @PropertyName("periodista")
    public String getPeriodista() {
        return periodista;
    }

    public void setPeriodista(String periodista) {
        this.periodista = periodista;
    }

    @PropertyName("descripcion")
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @PropertyName("foto")
    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    @PropertyName("audio")
    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    @PropertyName("fecha")
    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return  "Periodista: " + periodista + '\n' +
                "Fecha: " + fecha + '\n'+
                //", foto='" + foto + '\'' +
                //", audio='" + audio + '\'' +
                "Descripcion: " + descripcion;

    }
}