package com.example.appmow;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import androidx.annotation.Nullable;

import java.sql.Date;
import java.sql.Time;

public final class Tarea {
    private int id;
    private String asunto;
    private Time hora;
    private Date fecha;
    private String origen;
    private String destino;
    private Date alarma;
    private String transporte;

    public Tarea(int id, String asunto, Time hora, Date fecha, String origen, String destino, Date alarma, String transporte) {
        this.id = id;
        this.asunto = asunto;
        this.hora = hora;
        this.fecha = fecha;
        this.origen = origen;
        this.destino = destino;
        this.alarma = alarma;
        this.transporte = transporte;
    }

    public String getAsunto() {
        return asunto;
    }

    public Time getHora() {
        return hora;
    }

    public Date getFecha() {
        return fecha;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public Date getAlarma() {
        return alarma;
    }

    public String getTransporte() {
        return transporte;
    }

    public int getId() {
        return id;
    }





}



